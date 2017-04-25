package com.example.scove.oneclicksolution;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

import static android.R.id.list;

public class AlertPage2 extends AppCompatActivity implements View.OnClickListener {

    String country, city, userCountry;
    Button button;
    EditText editText;

    private ListView listView;
    User user;
    ArrayList<String> displayList;
    ArrayAdapter adapter;


    DatabaseReference mDatabase;

    SQLiteDatabase myDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_page2);
        button = (Button) findViewById(R.id.safebutton);
        editText = (EditText) findViewById(R.id.editText2);
        listView = (ListView) findViewById(R.id.listings);


        Intent intent = this.getIntent();
        city = intent.getStringExtra("city");
        country = intent.getStringExtra("country");
        setTitle(city+","+country);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //creating instance of the database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        findViewById(R.id.safebutton).setOnClickListener(AlertPage2.this);

        fillList();
    }


    public void writeSafeUserToFirebase() {

        User user = getInfoOfUser();            //creating a user to place in "Safe Users"

        //if(userCountry.compareToIgnoreCase(country)==0) {
            DatabaseReference childRef = mDatabase.child("SafeUsers");

            try {
                DatabaseReference childRef2 = childRef.child(country);
                childRef2.child(user.getFireBaseUsername()).setValue(user);
                childRef2.child(user.getFireBaseUsername()).child("latitude").setValue(user.getLatitude());
                childRef2.child(user.getFireBaseUsername()).child("longitude").setValue(user.getLongitude());


            } catch (NullPointerException n) {
                Toast.makeText(AlertPage2.this, "Unable to write to Database with Null Pointer", Toast.LENGTH_LONG).show();
            }

       // }
        //else
           // Toast.makeText(AlertPage2.this, "You are unable to declare yourself safe in a location you are not in", Toast.LENGTH_LONG).show();
    }




    @Override
    public void onClick(View v) {
        //when the button is pressed the method is called
        switch (v.getId()) {
            case R.id.safebutton:
                //creating alert and sending to database
                writeSafeUserToFirebase();
                fillList();
                break;
            // ...
        }
    }
    //Method that will create an alert on the list by getting the users country
    private void fillList() {

        displayList = new ArrayList<>();

        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, displayList);

        //branching down through the database
        DatabaseReference childRef = mDatabase.child("SafeUsers");
        DatabaseReference childRef2 = childRef.child(country);





        //listener to the database but only in the safeusers category of the selected alert
        childRef2.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //loop through to get all children in alerts
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                    //Object returned from database placed into an Alert Class
                    user = singleSnapshot.getValue(User.class);
                    //Alert object placed into list
                    //this list is a String array for display purposes
                    displayList.add(user.toString());
                    //new list is then displayed on screen
                    listView.setAdapter(adapter);

                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
    private User getInfoOfUser(){

        User user = null;
        myDB = this.openOrCreateDatabase("UserDetails", MODE_PRIVATE, null);
        Cursor cursor = myDB.query("user", new String[] { "*" },
                null,
                null, null, null, null, null);

        if(cursor != null)
        {
            if (cursor.moveToFirst()) {

                String username = cursor.getString(0); // firebase username
                String email = cursor.getString(1); //actually name of user
                String name = cursor.getString(2); //email of user
                String phNum = cursor.getString(3); //phone number of user
                double lat = cursor.getDouble(4);
                double lon = cursor.getDouble(5);
               // userCountry = cursor.getString(6);    //throwing exception because it doest recognise the column name

                //Create a User object from the SQLite info
                user = new User(username,name,email,phNum);
                user.setLatitude(lat);
                user.setLongitude(lon);
                cursor.close();

            }
        }


        myDB.close();
        return user;
    }
}
