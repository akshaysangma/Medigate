package com.coep.medigate.medig;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserView extends AppCompatActivity {
    String User;
    final String invervalURL = "http://192.168.43.223:8000/android_api/sensor_data/";
    final String ContURL = "http://192.168.43.223:8000/android_api/contdata/";
    TextView pulse, temp;
    String Pulse,Temp;
    Button bt;
    Handler bluetoothIn;
    private BluetoothSocket btSocket = null;
    private UserView.ConnectedThread mConnectedThread;
    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // String for MAC address
    private static String address;
    int Pulsevalue;


    //GRAPH RELATED STUFFS 07/03/2018

    private LineGraphSeries<DataPoint> series;
    private int lastX = 0;
    BluetoothAdapter mBluetoothAdapter;
    private final static int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_view);
        bt = (Button) findViewById(R.id.bt);
        temp = (TextView) findViewById(R.id.temp);
        pulse = (TextView) findViewById(R.id.pulse);

        //Initilizing Graph
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
        final SharedPreferences mSharedPreference = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        User = (mSharedPreference.getString("User", "NoUser"));

        if (User.equals("NoUser")) {
            Toast.makeText(UserView.this, "Login Failed !Check Your Network" + User, Toast.LENGTH_LONG).show();
            finish();

        } else {
            Toast.makeText(UserView.this, "Logged in As " + User, Toast.LENGTH_LONG).show();
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
                            final Dialog dialog = new Dialog(UserView.this);
                            dialog.setContentView(R.layout.bt_list);
                            dialog.setTitle("Paired Medigate Devices");
                            final ListView btlist = (ListView) dialog.findViewById(R.id.List);
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(UserView.this, android.R.layout.simple_list_item_1, s);
                            btlist.setAdapter(adapter);
                            dialog.show();
                            btlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                                    Toast.makeText(UserView.this, "Connected to (" + address + ") " + btlist.getItemAtPosition(i), Toast.LENGTH_LONG).show();
                                    dialog.dismiss();
                                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

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
                                    mConnectedThread = new UserView.ConnectedThread(btSocket);
                                    mConnectedThread.start();
                                    bluetoothIn = new Handler() {
                                        String buffer = "";

                                        public void handleMessage(android.os.Message msg) {
                                            String strIncom = null;
                                            strIncom = buffer + (String) msg.obj;
                                            StringBuilder sb = new StringBuilder();
                                            StringBuilder sb2 = new StringBuilder(strIncom);

                                            if (strIncom.charAt(strIncom.length() - 1) != '\n') {
                                                int count = strIncom.length() - 1;
                                                char ch;
                                                while ((count >= 0 && (ch = strIncom.charAt(count)) != '\n')) {
                                                    sb2.deleteCharAt(count);
                                                    sb.append(ch);
                                                    count--;
                                                }
                                                sb.reverse();
                                                buffer = sb.toString();
                                            } else {
                                                buffer = "";
                                            }
                                            strIncom = sb2.toString();
                                            String values[] = strIncom.split("\n");
                                            for (String str : values) {
                                                try {

                                                    if (str.charAt(0) == 'P') {

                                                        pulse.setText("Pulse Rate [" + str.substring(1) + "]");
                                                        Pulse = str.substring(1).toString();

                                                    }
                                                    if (str.charAt(0) == 'T') {

                                                        temp.setText("Temperature [" + str.substring(1) + "°F]");
                                                        Temp = str.substring(1).toString();
                                                        InsertData(invervalURL,0);

                                                    } else {
                                                        Pulsevalue = Integer.parseInt(str);
                                                        series.appendData(new DataPoint(lastX++, Pulsevalue), true, 100);
                                                        InsertData(ContURL,1);
                                                    }

                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    };
                                }
                            });
                        }
                    }
                });


            }


        }


    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    public class ConnectedThread extends Thread {
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
        super.onStop();
        finish();
        System.exit(1);
    }

    public void InsertData(final String Url,final int type){

        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
                JSONObject jsonParam = new JSONObject();
                try {
                    if(type == 0){
                    jsonParam.put("user", User);
                    jsonParam.put("temp", Temp);
                    jsonParam.put("pulse", Pulse);
                    }
                    else
                    {
                    jsonParam.put("user", User);
                    jsonParam.put("pulsevalue",""+Pulsevalue);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    OkHttpClient client = new OkHttpClient();
                    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                    RequestBody body = RequestBody.create(JSON, jsonParam.toString());
                    Request request = new Request.Builder()
                            .url(Url)
                            .post(body)
                            .build();
                    Response response = null;
                    response = client.newCall(request).execute();
                    String resStr = response.body().string();
                    return resStr;
                } catch (IOException e) {
                    e.printStackTrace();
                    return ""+e;
                }

            }


            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
               Toast.makeText(UserView.this, "" + result, Toast.LENGTH_LONG).show();
            }
        }

        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();

        sendPostReqAsyncTask.execute();
    }







}

