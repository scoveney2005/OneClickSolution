package com.example.scove.oneclicksolution;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;


public class Userdetails extends AppCompatActivity {

    private Button enter;
    private EditText email, name, phNum, heading;
    FirebaseDatabase database;
    DatabaseReference mDatabase;

    private ListView listView;

    String userName;

    SQLiteDatabase myDB;
    ContentValues values;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userdetails);

        enter = (Button) findViewById(R.id.button2);
        email = (EditText) findViewById(R.id.email);
        name = (EditText) findViewById(R.id.name);
        heading = (EditText) findViewById(R.id.editText5);
        heading.setEnabled(false); //have the heading as read only
        phNum = (EditText) findViewById(R.id.phnum);
        database = FirebaseDatabase.getInstance();


        mDatabase = database.getReference();            //getting a reference to the created instance so we can input the information

        openOrCreateDatabase("UserDetails", MODE_PRIVATE, null);

        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (v.getId()) {
                    case R.id.button2:
                        writeToDatabase();
                        break;
                    // ...
                }
            }
        });

    }

    //will write all entered data as a new user in firebase
    private void writeToDatabase() {

        //gathering info from the user
        String userEmail = email.getText().toString();
        String nameEntry = name.getText().toString();
        String userNum = phNum.getText().toString();


        if((userEmail.compareToIgnoreCase("Email")!=0) && (nameEntry.compareToIgnoreCase("Name")!=0) && (userNum.compareToIgnoreCase("Phone num")!=0) ) {
            userName = userEmail.replace(".", "");           //firebase does not allow a full stop in there userIDs (keys), this takes the email and strips away the full stops

            User user = new User(userName, nameEntry, userEmail, userNum);         //Creating a User object
            mDatabase.child("users").child(userName).setValue(user);            //placing the info of the User into Firebase
            createSQLDatabase(userName, userEmail, nameEntry, userNum);
        }
        else
            Toast.makeText(Userdetails.this, "Please Enter Valid Details", Toast.LENGTH_SHORT).show();


    }

    private void createSQLDatabase(String username, String email, String name, String phNum) {

        try {
            myDB= this.openOrCreateDatabase("UserDetails", MODE_PRIVATE, null);
            long numRows = DatabaseUtils.queryNumEntries(myDB, "user");

            values = new ContentValues();
            values.put("Username", username);
            values.put("Email", email);
            values.put("Name", name);
            values.put("PhoneNumber", phNum);


            //only one user is allowed
            if(numRows>0)
            {
                //user given the option to overwrite data if already entered information
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int choice) {
                        switch (choice) {
                            case DialogInterface.BUTTON_POSITIVE:
                                String where = "rowid=(SELECT MIN(rowid) FROM user)";   //gets the lowest row in the database. We should only have 1 row
                                myDB.update("user", values, where, null);
                                Toast.makeText(Userdetails.this, "The user information has been changed", Toast.LENGTH_SHORT).show();
                                finish();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                finish();
                                break;
                        }
                    }
                };

                openDialogBox(dialogClickListener); //popup to ask user whether they want to overwrite the data

            }
            else {
                myDB.insert("user", null, values);
                finish();
            }


        } catch (NullPointerException e) {
            System.out.print("Null Pointer exception");
        }




    }

    private void openDialogBox(DialogInterface.OnClickListener dialogClickListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(Userdetails.this);
        builder.setMessage("Only one user allowed. Do you wish to overwrite the existing data?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }



}
