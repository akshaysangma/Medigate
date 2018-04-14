package com.coep.medigate.medig;

import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class Form extends AppCompatActivity {

    final String Post_info = "http://192.168.43.223:8000/android_api/post_user/";
    final String invervalURL = "http://192.168.43.223:8000/android_api/docinfo/";



    EditText username,name,dateofbirth,city,phone,docid;
    RadioButton yesD,noD,yesA,noA;
    String Email,Password,Username,Name,DOB,Age="",Gender,City,Phone,Doc = "N/A";
    TextView tex;
    Spinner bloodgrp,gender;
    Button save;
    RadioGroup join;
    String BloodGrp;
    String DStatus = "0",AStatus = "0";
    private static final String TAG = "MyActivity";
    private static final String[]blood = {"AB+", "A+", "O+" , "B+","AB-", "A-", "O-" , "B-","Unknown"};
    private static final String[]gen= {"Female", "Male", "Others"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Calendar myCalendar = Calendar.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);
        Bundle extras = getIntent().getExtras();
        tex = (TextView)findViewById(R.id.tetx) ;
        username =(EditText)findViewById(R.id.user);
        name =(EditText)findViewById(R.id.name);
        dateofbirth =(EditText)findViewById(R.id.date);
        bloodgrp = (Spinner) findViewById(R.id.bloodgrp);
        gender =(Spinner)findViewById(R.id.gender);
        city =(EditText)findViewById(R.id.city);
        phone =(EditText)findViewById(R.id.phone);
        save = (Button)findViewById(R.id.Save);
        yesD = (RadioButton) findViewById(R.id.yesD);
        noD = (RadioButton) findViewById(R.id.noD);
        yesA = (RadioButton) findViewById(R.id.yesA);
        noA = (RadioButton) findViewById(R.id.noA);
        join = (RadioGroup)findViewById(R.id.join);
        docid =(EditText)findViewById(R.id.docid);

        ArrayAdapter<String> GenAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, gen);
        GenAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender.setAdapter(GenAdapter);

        ArrayAdapter<String> bloodAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, blood);
        bloodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bloodgrp.setAdapter(bloodAdapter);


        final String TAG = "MyActivity";


        gender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Gender = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                 Gender = "Female";
            }
        });


        final DatePickerDialog.OnDateSetListener DateOFbirth = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar today = Calendar.getInstance();
                Calendar birthday;
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                dateofbirth.setText(sdf.format(myCalendar.getTime()));

                birthday = new GregorianCalendar(year, month, dayOfMonth);
                int yourAge = today.get(Calendar.YEAR) - birthday.get(Calendar.YEAR);
                birthday.add(Calendar.YEAR, yourAge);
                if (today.before(birthday)) {
                    yourAge--;
                }

                Age += yourAge;

               }
        };

            dateofbirth.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new DatePickerDialog(Form.this, DateOFbirth, myCalendar
                            .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                }
            });








        bloodgrp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                BloodGrp = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                BloodGrp = "AB+";
            }
        });










        Email = extras.getString("email");
         Password = extras.getString("password");

       yesD.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

                   tex.setVisibility(View.VISIBLE);
                   join.setVisibility(View.VISIBLE);
                   DStatus = "1";
                   noA.setChecked(true);

           }
       });

        noD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    tex.setVisibility(View.GONE);
                    join.setVisibility(View.GONE);
                    DStatus = "0";
                    AStatus = "0";

            }
        });

        yesA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    docid.setVisibility(View.VISIBLE);
                    AStatus = "1";

            }
        });

        noA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                   docid.setVisibility(View.GONE);
                    AStatus = "0";
                    Doc = "N/A";

            }
        });





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


                if(city.getText().toString().trim().isEmpty()){
                    city.setError("This Cannot be Empty");
                }
                else {
                    City = city.getText().toString();
                }

                if(phone.getText().toString().trim().isEmpty()){
                    phone.setError("This Cannot be Empty");
                }
                else {
                    Phone = phone.getText().toString();
                }
                if (AStatus.equals("1")){

                    if(docid.getText().toString().trim().isEmpty()){
                        docid.setError("This Cannot be Empty");

                    }
                        else{
                            Doc = docid.getText().toString();
                    }

                InsertData();
                Intent Medi = new Intent(Form.this,UserView.class);
                startActivity(Medi);
                finish();


                }



            }

        });

}
    public void InsertData(){

        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
                JSONObject jsonParam = new JSONObject();
                JSONObject doc = new JSONObject();
                try {
                    jsonParam.put("username", Username);
                    jsonParam.put("password", Password);
                    jsonParam.put("email", Email);
                    jsonParam.put("name", Name);
                    jsonParam.put("dob", DOB);
                    jsonParam.put("age", Age);
                    jsonParam.put("gender", Gender);
                    jsonParam.put("bloodgrp", BloodGrp);
                    jsonParam.put("phone", Phone);
                    jsonParam.put("city", City);
                    jsonParam.put("doctor", DStatus);
                    jsonParam.put("service", AStatus);
                    doc.put("user", Username);
                    doc.put("docid",Doc);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    OkHttpClient client = new OkHttpClient();
                    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                    RequestBody body = RequestBody.create(JSON, jsonParam.toString());
                    Request request = new Request.Builder()
                            .url(Post_info)
                            .post(body)
                            .build();
                    Response response = null;
                    response = client.newCall(request).execute();
                    String resStr = response.body().string();


                    RequestBody docbody = RequestBody.create(JSON, doc.toString());
                    Request docrequest = new Request.Builder()
                            .url(invervalURL)
                            .post(docbody)
                            .build();
                    Response docresponse = null;
                    docresponse = client.newCall(docrequest).execute();
                   String DresStr = docresponse.body().string();

                    return "U: "+resStr + " D: "+ DresStr ;
                } catch (IOException e) {
                    e.printStackTrace();
                    return ""+e;
                }

            }


            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);

                    Log.v(TAG, result);
                    Toast.makeText(Form.this, "" + result, Toast.LENGTH_LONG).show();
                               }
        }

        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();

        sendPostReqAsyncTask.execute();
    }


}
