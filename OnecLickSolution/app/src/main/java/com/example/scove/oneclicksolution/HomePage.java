package com.example.scove.oneclicksolution;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.R.id.list;
import static com.example.scove.oneclicksolution.R.id.text_view;
import static com.example.scove.oneclicksolution.R.layout.mylist;

public class HomePage extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{

    private GoogleMap mMap, mGoogleMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation, lastKnownLocation;
    private double mLatitude, mLongitude;
    private int LOCATION_REQUEST_CODE;
    LocationManager locationManager;
    Marker mCurrLocationMarker;
    SupportMapFragment mapFragment;

    private String TAG;
    private Button alertButton;

    //FirebaseDatabase database;
    DatabaseReference mDatabase;
    private ListView listView;

    //For the list view
    AlertLocation alertLocation;
    ArrayList<AlertLocation> list;
    ArrayList<String> displayList;
    ArrayAdapter adapter;

    Boolean writeNotAllowed;

    SQLiteDatabase myDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page2);
        alertButton = (Button) findViewById(R.id.alertbutton);

        findViewById(R.id.alertbutton).setOnClickListener(this);
        //creating instance of the database
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //checkSimilarAlerts(); //starts off this method to get information from database DOES NOT WORK
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Create an instance of GoogleAPIClient for GPS

        if ((mGoogleApiClient == null)) {
            //Toast.makeText(HomePage.this, "connecting to Google API", Toast.LENGTH_LONG).show();
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);





        //creating my list
        listView = (ListView) findViewById(R.id.listings);

        createUpdateAlertList();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,int position, long arg3)
            {

                arg1.setSelected(true);

                Intent intent = new Intent(HomePage.this, AlertPage2.class);
                AlertLocation aLocation = list.get(position);
                intent.putExtra("city", aLocation.getCity());
                intent.putExtra("country", aLocation.getCountry());

                startActivity(intent);


            }
        });


    }



    protected void onStart() {

        mGoogleApiClient.connect();
        super.onStart();

    }

    @Override
    public void onConnected(Bundle connectionHint) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(HomePage.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE);
            return;
        }

        lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        //if Location object is not null then find the coordinates
        if (lastKnownLocation != null) {
            mLatitude = lastKnownLocation.getLatitude();
            mLongitude = lastKnownLocation.getLongitude();

            //write the new info to the users account.
            writeCoordsToFirebase();

            //longitude kept coming back positive so i had to do this
            mLongitude = mLongitude * -1;
            //Toast.makeText(HomePage.this, mLatitude + " " + " " + -mLongitude, Toast.LENGTH_LONG).show();
        }


         //Manipulates the map once available.
        LatLng currentLocation = new LatLng(mLatitude, -mLongitude);

        //This is where we can add markers and move the camera to users location
        mMap.addMarker(new MarkerOptions().position(currentLocation).title("Marker in " + getCountry()));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        //initializing the global variable with the map sent by the Google API
        mMap = googleMap;
    }


    @Override
    public void onClick(View v) {
        //when the button is pressed the method is called
        switch (v.getId()) {
            case R.id.alertbutton:
                //creating alert and sending to database
                writeToFirebaseDatabase();
                //creating the alert list
                createUpdateAlertList();
                break;
            // ...
        }
    }


    public void writeToFirebaseDatabase() {


        String country = getCountry();  //Getting the users location to use an alert

        //in case location is not working we dont want to write to the database
        if(country!="") {
            writeNotAllowed =false;
            //writeNotAllowed = checkSimilarAlerts(); //will check if an alert has already occurred DOES NOT WORK FAST ENOUGH
            if(writeNotAllowed==false) {
                String cityLocale = getCity();  //gets the local area of the alert if there is one
                AlertLocation alert = new AlertLocation(country, cityLocale, mLatitude, mLongitude);
                mDatabase.child("alerts").child(country).setValue(alert);   //using key value "alerts" and placing all info about the created Alert object into the database
            }
        }

    }


    //Method that will create an alert on the list by getting the users country
    private void createUpdateAlertList() {

        //two lists, one to keep the object and one to display
        list = new ArrayList<>();
        displayList = new ArrayList<>();

        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, displayList);

        //listener to the database but only in the alerts category
        mDatabase.child("alerts").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //loop through to get all children in alerts
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                    //Object returned from database placed into an Alert Class
                    alertLocation = singleSnapshot.getValue(AlertLocation.class);
                    //Alert object placed into list
                    //***this list will be used to know which object is pressed by user***
                    list.add(alertLocation);
                    //this list is a String array for display purposes
                    displayList.add(alertLocation.toString());
                    //new list is then displayed on screen
                    listView.setAdapter(adapter);

                }
            }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


    }

    //method will get location details based on coordinates
    private String getCountry() {
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        String countryName = "";
        try {

            List<Address> addresses = gcd.getFromLocation(mLatitude, -mLongitude, 1); //lat then long


            if (addresses.size() > 0) {


                countryName = addresses.get(0).getCountryName();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return countryName;
    }

    private String getCity(){
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        String adminArea="";
        try {

            List<Address> addresses = gcd.getFromLocation(mLatitude, -mLongitude, 1);



            if (addresses.size() > 0) {

                adminArea = addresses.get(0).getAdminArea();
            }


        } catch (IOException e) {
            Toast.makeText(HomePage.this, "Cant get city name", Toast.LENGTH_LONG).show();
        }

        return adminArea;
    }


    @Override
    public void onConnectionSuspended(int i) {

    }


    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

        //move map camera
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,11));

        //optionally, stop location updates if only current location is needed
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }
/*
    private Boolean checkSimilarAlerts(){
        DatabaseReference ref = mDatabase.child("alerts");  //path to alerts
        isSame=false;
        //THIS METHOD DOES WORK BUT FIREBASE ONLY ALLOWS DATA RETREIVAL USING LISTENERS AND THIS IS TOO SLOW TO USE AS VALIDATION
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userCountry = getCountry();          //gets the country of the new alert
                //loop through the alerts and see if there is a similar alert
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren() ){
                    AlertLocation alert = singleSnapshot.getValue(AlertLocation.class);

                    String alertCountry = alert.getCountry();   //gets the country of the old alerts
                    int result = alertCountry.compareToIgnoreCase(userCountry);
                    Toast.makeText(HomePage.this, "Result is "+ result, Toast.LENGTH_LONG).show();
                    //if the two countries are the same
                    if(alertCountry.compareToIgnoreCase(userCountry)==0){
                        isSame=true;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
        Toast.makeText(HomePage.this, "Boolean is "+ isSame, Toast.LENGTH_LONG).show();
        return isSame;
    }
*/
    private void writeCoordsToFirebase(){
        String userID = readUserName();
        mDatabase.child("users").child(userID).child("latitude").setValue(mLatitude);
        mDatabase.child("users").child(userID).child("longitude").setValue(mLongitude);
        writeCoordsToSQLiteDatabase();
    }

    private String readUserName(){

        myDB = this.openOrCreateDatabase("UserDetails", MODE_PRIVATE, null);
        Cursor cursor = myDB.query("user", new String[] { "*" },
                null,
                null, null, null, null, null);

        String username="";
        if(cursor != null)
        {
            if (cursor.moveToFirst()) {

                username = cursor.getString(0); // firebase username
                cursor.close();

            }
        }
        myDB.close();
        return username;
    }

    private void writeCoordsToSQLiteDatabase(){
        myDB = this.openOrCreateDatabase("UserDetails", MODE_PRIVATE, null);

        ContentValues values = new ContentValues();


        values.put("Longitude",mLatitude );
        values.put("Longitude",mLongitude);
        //values.put("Country",getCountry());

        try {
            String where = "rowid=(SELECT MIN(rowid) FROM user)";
            myDB.update("user", values, where, null);
            myDB.close();
        }
        catch (SQLiteException s)
        {
            Toast.makeText(HomePage.this, "Error found, please try again", Toast.LENGTH_LONG).show();
        }
    }
}


