package com.coep.medigate.medig;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;


  /*
        To Do List
        1.Validation (Comfirmation Mail to Verify Account)
        2.Auto Login after First time on Launch (pre-stored auto validate user table at server on open ??)
        3.Social Login (or Just Google Account) ( how to validate social login ??)
        4.Create a Activity to redirect after successful registration when user can input pre-medical data (Form)
        5.DataBase for user login in server
        6.Color Theme
        7.https://developers.google.com/identity/sign-in/android/sign-in
        8.https://developers.google.com/identity/sign-in/android/backend-auth
        */


public class login extends AppCompatActivity {
    Button signInButton;
    GoogleSignInClient mGoogleSignInClient;
    static final int RC_SIGN_IN = 9000;
    private static final String TAG = "login";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(login.this, gso);
        signInButton = (Button) findViewById(R.id.sign_in_button);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }

        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);

        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI
                /*You can also get the user's email address with getEmail,
                the user's Google ID (for client-side use) with getId,
                and an ID token for the user with with getIdToken.
                If you need to pass the currently signed-in user to a backend server,
                send the ID token to your backend server and validate the token on the server.*/

            Toast.makeText(login.this,"Logged As "+ account.getEmail(),Toast.LENGTH_SHORT).show();
            Intent Form = new Intent(login.this,Form.class);
            startActivity(Form);

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(login.this,"Login Failed "+ e.getStatusCode(),Toast.LENGTH_SHORT).show();
        }
}

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account !=null) {
            Intent Medigate = new Intent(login.this, Medigate.class);
            startActivity(Medigate);
        }
    }


}