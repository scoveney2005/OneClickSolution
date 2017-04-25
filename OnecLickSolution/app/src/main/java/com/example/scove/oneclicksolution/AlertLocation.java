package com.example.scove.oneclicksolution;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by scove on 21/03/2017.
 */
//collect our intent

public class AlertLocation implements  Parcelable{

    double longitude, latitude;
    String country, city, attackDescription;

    public AlertLocation() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public AlertLocation(String country, String city, double longitude, double latitude) {
        this.country = country;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;

    }

    public AlertLocation(Parcel in) {
        String[] data = new String[4];
        this.country = data[0];
        this.city = data[1];
        this.longitude = Double.parseDouble(data[2]);
        this.latitude = Double.parseDouble(data[3]);
}


    public String getCountry(){
        return country;
    }
    public String getCity(){
        return city;
    }
    public double getLongitude(){
        return longitude;
    }
    public double getLatitude(){
        return latitude;
    }
    public void setCity(String city){
        this.city = city;
    }
    public void setAttackDescription(String description){
        this.attackDescription = description;
    }
    public String getAttackDescription(){
        return attackDescription;
    }

    public String toString(){

        return "**ALERT in "+getCity()+", "+getCountry()+" Press for more details***";
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override   //method used to transfer Objects between activities
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new  String[] {
                this.country,
                this.city,
                String.valueOf(longitude),
                String.valueOf(latitude)
                }
        );
    }

    public static final Parcelable.Creator<AlertLocation> CREATOR = new Parcelable.Creator<AlertLocation>() {

        @Override
        public AlertLocation createFromParcel(Parcel in) {
            return new AlertLocation(in);
        }

        @Override
        public AlertLocation[] newArray(int size) {
            return new AlertLocation[size];
        }
    };
}
