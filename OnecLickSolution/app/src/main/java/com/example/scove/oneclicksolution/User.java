package com.example.scove.oneclicksolution;

import java.util.Map;

/**
 * Created by scove on 10/03/2017.
 */

public class User {

    public String fireBaseUsername, name, email, phoneNum;
    public double latitude, longitude;



    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String firebaseUserName,String username,String email, String phoneNum) {

        this.fireBaseUsername = firebaseUserName;
        this.name = username;
        this.phoneNum = phoneNum;
        this.email = email;

    }

    public String getEmail() {
        return email;
    }
    public String getFireBaseUsername() {
        return fireBaseUsername;
    }
    public String getName() {
        return name;
    }
    public String getPhoneNum()  {
        return phoneNum;
    }
    public double getLatitude() {
        return latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public String toString()
    {
        return getFireBaseUsername();
    }
}
