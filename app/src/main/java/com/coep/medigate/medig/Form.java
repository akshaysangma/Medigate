package com.coep.medigate.medig;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.opengl.GLException;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class Form extends AppCompatActivity {

    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String User = "User";

    SharedPreferences sharedpreferences;



    String ServerURL = "http://192.168.137.119/medigate/newuser_entry.php";
    EditText username,name,dateofbirth,age,gender,city,phone;
    RadioButton yesD,noD,yesA,noA;
    String Email,Password,Username,Name,DOB,Age,Gender,City,Phone;
    TextView tex;
    Button save;
    String DStatus = "0",AStatus = "0";
    private static final String TAG = "MyActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);
        Bundle extras = getIntent().getExtras();
        tex = (TextView)findViewById(R.id.tetx) ;
        username =(EditText)findViewById(R.id.user);
        name =(EditText)findViewById(R.id.name);
        dateofbirth =(EditText)findViewById(R.id.date);
        age =(EditText)findViewById(R.id.age);
        gender =(EditText)findViewById(R.id.gender);
        city =(EditText)findViewById(R.id.city);
        phone =(EditText)findViewById(R.id.phone);
        save = (Button)findViewById(R.id.Save);
        yesD = (RadioButton) findViewById(R.id.yesD);
        noD = (RadioButton) findViewById(R.id.noD);
        yesA = (RadioButton) findViewById(R.id.yesA);
        noA = (RadioButton) findViewById(R.id.noA);
        final String TAG = "MyActivity";
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);


         Email = extras.getString("email");
         Password = extras.getString("password");




        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(username.getText().toString().trim().isEmpty()){
                    username.setError("This Cannot be Empty");
                }
                else {
                    Username = username.getText().toString();
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Form.this);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("User", Username);
                    editor.commit();


                }
                if(name.getText().toString().trim().isEmpty()){
                    name.setError("This Cannot be Empty");
                }
                else {
                    Name = name.getText().toString();
                }
                if(dateofbirth.getText().toString().trim().isEmpty()){
                    dateofbirth.setError("This Cannot be Empty");
                }
                else {
                    DOB = dateofbirth.getText().toString();
                }
                if(age.getText().toString().trim().isEmpty()){
                    age.setError("This Cannot be Empty");
                }
                else {
                    Age = age.getText().toString();
                }
                if(city.getText().toString().trim().isEmpty()){
                    city.setError("This Cannot be Empty");
                }
                else {
                    City = city.getText().toString();
                }
                if(gender.getText().toString().trim().isEmpty()){
                    gender.setError("This Cannot be Empty");
                }
                else {
                    Gender = gender.getText().toString();
                }
                if(phone.getText().toString().trim().isEmpty()){
                    phone.setError("This Cannot be Empty");
                }
                else {
                    Phone = phone.getText().toString();
                }
                if(yesD.isChecked()){
                    DStatus="1";
                    if(yesA.isChecked()){
                        AStatus = "1";
                    }
                    else{
                        AStatus = "0";
                    }
                }
                else{
                    DStatus="0";
                }




           InsertData();
                Intent Medi = new Intent(Form.this,Medigate.class);
                startActivity(Medi);
                finish();


            }

        });
}
    public void InsertData(){

        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
                try {

                    String data = URLEncoder.encode("username", "UTF-8") + "=" +
                            URLEncoder.encode(Username, "UTF-8");
                    data += "&" + URLEncoder.encode("email", "UTF-8") + "=" +
                            URLEncoder.encode(Email, "UTF-8");
                    data += "&" + URLEncoder.encode("password", "UTF-8") + "=" +
                            URLEncoder.encode(Password, "UTF-8");
                    data += "&" + URLEncoder.encode("name", "UTF-8") + "=" +
                            URLEncoder.encode(Name, "UTF-8");
                    data += "&" + URLEncoder.encode("Date_of_birth", "UTF-8") + "=" +
                            URLEncoder.encode(DOB, "UTF-8");
                    data += "&" + URLEncoder.encode("Age", "UTF-8") + "=" +
                            URLEncoder.encode(Age, "UTF-8");
                    data += "&" + URLEncoder.encode("Gender", "UTF-8") + "=" +
                            URLEncoder.encode(Gender, "UTF-8");
                    data += "&" + URLEncoder.encode("City", "UTF-8") + "=" +
                            URLEncoder.encode(City, "UTF-8");
                    data += "&" + URLEncoder.encode("Phone_No", "UTF-8") + "=" +
                            URLEncoder.encode(Phone, "UTF-8");
                    data += "&" + URLEncoder.encode("doctor", "UTF-8") + "=" +
                            URLEncoder.encode(DStatus, "UTF-8");
                    data += "&" + URLEncoder.encode("avail", "UTF-8") + "=" +
                            URLEncoder.encode(AStatus, "UTF-8");


                    Log.v(TAG,data);
                    URL url = new URL(ServerURL);
                    URLConnection conn = url.openConnection();

                    conn.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                    wr.write(data);
                    wr.flush();

                    BufferedReader reader = new BufferedReader(new
                            InputStreamReader(conn.getInputStream()));

                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    // Read Server Response
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                        break;
                    }

                    return sb.toString();
                } catch (Exception e) {
                    return new String("Exception: " + e.getMessage());
                                    }

            }
            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                /*if(!result.equals("Saved")){
                    Intent reopen = new Intent(Form.this,Form.class);
                    reopen.putExtra("email",Email);
                    reopen.putExtra("password",Password);
                    startActivity(reopen);
                    finish();

                }
                else {

                    */
                    Log.v(TAG, result);
                    Toast.makeText(Form.this, "" + result, Toast.LENGTH_LONG).show();
                //}
                }
        }

        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();

        sendPostReqAsyncTask.execute();
    }


}
