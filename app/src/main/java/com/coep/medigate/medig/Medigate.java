package com.coep.medigate.medig;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Medigate extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    //private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status
    TextView pulse , temp;
    Button bt;
    BluetoothAdapter mBluetoothAdapter;
    private final static int REQUEST_ENABLE_BT = 1;

    private final static int delimiter  = 1;
    byte[] readBuffer;
    int readBufferPosition;

    Handler bluetoothIn;
    private int heartBeatCount = 0;
    final int handlerState = 0;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();
    private ConnectedThread mConnectedThread;
    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // String for MAC address
    private static String address;


    //GRAPH RELATED STUFFS 07/03/2018

    private LineGraphSeries<DataPoint> series;
    private int lastX = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medigate);
        bt = (Button) findViewById(R.id.bt);
        temp = (TextView) findViewById(R.id.temp);
        pulse = (TextView) findViewById(R.id.pulse);


        //Graph related  07/03/2018
        GraphView graph = (GraphView) findViewById(R.id.graph);
        series = new LineGraphSeries<DataPoint>();
        graph.addSeries(series);
        Viewport viewport = graph.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setXAxisBoundsManual(true);
        viewport.setMinX(0);
        viewport.setMaxX(100);
        viewport.setMinY(10);
        viewport.setMaxY(500);
        viewport.setScrollable(true);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        final SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String User=(mSharedPreference.getString("User", "NoUser"));

        if(User.equals("NoUser")){
            Toast.makeText(Medigate.this,"Login Failed !Please try after some time.."+User,Toast.LENGTH_LONG).show();
            finish();

        }
        else{
            Toast.makeText(Medigate.this,"Logged in As"+User,Toast.LENGTH_LONG).show();
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (mBluetoothAdapter == null) {
                Toast.makeText(getApplicationContext(), " Device Not Supported ", Toast.LENGTH_LONG).show();
            } else {
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
                bt.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {


                        final Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                        if (pairedDevices.size() > 0) {
                            final List<String> s = new ArrayList<String>();
                            for (BluetoothDevice btl : pairedDevices) {
                                s.add(btl.getName());
                                address = btl.getAddress().toString();
                            }
                            final Dialog dialog = new Dialog(Medigate.this);
                            dialog.setContentView(R.layout.bt_list);
                            dialog.setTitle("Paired Medigate Devices");
                            final ListView btlist = (ListView) dialog.findViewById(R.id.List);
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(Medigate.this, android.R.layout.simple_list_item_1, s);
                            btlist.setAdapter(adapter);
                            dialog.show();


                            btlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                                    Toast.makeText(Medigate.this, "Connected to (" + address + ") " + btlist.getItemAtPosition(i), Toast.LENGTH_LONG).show();
                                    dialog.dismiss();
                                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                        /*        Thread recurring = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            while (true) {
                                                Thread.sleep(60000);
                                                Toast.makeText(MainActivity.this, heartBeatCount, Toast.LENGTH_SHORT).show();
                                                heartBeatCount = 0;
                                            }
                                        }
                                        catch (InterruptedException e) {

                                        }
                                    }
                                });*/
                                    try {
                                        btSocket = createBluetoothSocket(device);
                                    } catch (IOException e) {
                                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
                                    }
                                    // Establish the Bluetooth socket connection.
                                    try {
                                        btSocket.connect();
                                    } catch (IOException e) {
                                        try {
                                            btSocket.close();
                                        } catch (IOException e2) {
                                            //insert code to deal with this
                                        }
                                    }
                                    mConnectedThread = new ConnectedThread(btSocket);
                                    mConnectedThread.start();
                                    ;
                                    //code starts here
                                    bluetoothIn = new Handler() {
                                        String buffer = "";
                                        public void handleMessage(android.os.Message msg) {
                                            String newstr = "";
                                            String strIncom = null;
                                            strIncom = buffer + (String) msg.obj;
                                            StringBuilder sb = new StringBuilder();
                                            StringBuilder sb2 = new StringBuilder(strIncom);

                                            if(strIncom.charAt(strIncom.length() - 1) != '\n') {
                                                int count = strIncom.length() - 1;
                                                char ch;
                                                while ((count >=0 && (ch = strIncom.charAt(count)) != '\n')) {
                                                    sb2.deleteCharAt(count);
                                                    sb.append(ch);
                                                    count--;
                                                }
                                                sb.reverse();
                                                buffer = sb.toString();
                                            }
                                            else {
                                                buffer = "";
                                            }
                                            strIncom = sb2.toString();
                                            Log.d(TAG, strIncom + " strIncom\n");               // extract string
                                            //Log.d(TAG, strIncom);
                                            String values[] = strIncom.split("\n");
                                            for(String str : values) {
                                                try {
                                                    if (str.charAt(0) == 'P') {
                                                        // Toast.makeText(MainActivity.this, "P =" +  str.substring(1), Toast.LENGTH_SHORT).show();
                                                        pulse.setText("Pulse Rate ["+str.substring(1)+"]");
                                                    }
                                                    if(str.charAt(0) == 'T'){
                                                        // Toast.makeText(MainActivity.this, "T =" +  str.substring(1), Toast.LENGTH_SHORT).show();
                                                        temp.setText("Temperature ["+str.substring(1)+"]");
                                                    }
                                                    else {
                                                        int pulse = Integer.parseInt(str);
                                                        series.appendData(new DataPoint(lastX++, pulse), true, 100);
                                           /*     if(pulse >= 400) {
                                                    heartBeatCount++;
                                                }*/
                                                    }

                                                }
                                                catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    };
                                }
                            });
                        }
                    }
//            });
                });


            }

        }

    }
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    private class ConnectedThread extends Thread {
        private final InputStream In;


        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;


            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();

            } catch (IOException e) {
            }

            In = tmpIn;

        }

        public void run() {

            String strIncom;
            while (true) {
                try {
                    byte[] s = new byte[128];
                    int bytes = In.read(s);
                    strIncom = new String(s);
                    bluetoothIn.obtainMessage(1, strIncom.length(), -1, strIncom).sendToTarget();

                } catch (IOException e) {
                    break;
                }
            }
        }
    }
    @Override
    protected void onStop() {
        // call the superclass method first
        super.onStop();        finish();
        System.exit(1);}
}






