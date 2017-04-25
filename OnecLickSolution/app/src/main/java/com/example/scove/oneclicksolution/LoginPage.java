package com.example.scove.oneclicksolution;

import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;


import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import static android.R.attr.onClick;
import static android.provider.AlarmClock.EXTRA_MESSAGE;


public class LoginPage extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{
    public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";

    GoogleApiClient googleApiClient;
    GoogleSignInOptions gso;
    private SignInButton signInButton;
    private Button userdetails;
    int RC_SIGN_IN = 1;

    SQLiteDatabase myDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        userdetails = (Button) findViewById(R.id.userDetails);


        myDB = this.openOrCreateDatabase("UserDetails", MODE_PRIVATE, null);    //Creating the database here so we can validate for user information

        //Create a Table in the Database.
        myDB.execSQL("CREATE TABLE IF NOT EXISTS "
                + "user"
                + " (Username VARCHAR,Email VARCHAR,Name VARCHAR,PhoneNumber VARCHAR,Longitude DOUBLE(4),Latitude DOUBLE(4)" +  //didnt get Country column in phones but did in emulator
                ");");

        myDB.close();
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (v.getId()) {
                    case R.id.sign_in_button:
                        //check if there is any user details held in the database
                        Boolean allowed =checkUser();
                        if(allowed==true) {
                            signIn();
                        }
                        else
                            Toast.makeText(LoginPage.this,"First time users of the app must provide details before continuing. Please press the \"Register New User\" button  below", Toast.LENGTH_LONG).show();
                        break;
                }
            }});

        userdetails.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                switch (v.getId()) {
                    case R.id.userDetails:
                        showUserDetailsPage();
                        break;
                }
            }});


        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();




    }







    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void showUserDetailsPage(){

        Intent intent = new Intent(this, Userdetails.class);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            Intent intent = new Intent(this, HomePage.class);
            startActivity(intent);
        } else {
            // Signed out, show unauthenticated UI.
            Toast.makeText(LoginPage.this, "Signin unsuccessful", Toast.LENGTH_LONG).show();

        }
    }

    private Boolean checkUser(){
        Boolean allowed;
        myDB = this.openOrCreateDatabase("UserDetails", MODE_PRIVATE, null);

        long numRows = DatabaseUtils.queryNumEntries(myDB, "user");

        //if there is an entry in the SQLite database then user may progress to Home Page
        if (numRows>0) {
            allowed = true;

        }
        //otherwise force them to enter details in User page
        else
            allowed=false;

        myDB.close();
        return allowed;
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
