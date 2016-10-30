package com.eris.classes;

import java.io.Serializable;

import android.content.Intent;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Will Schrag on 10/24/2016.
 */



public class Incident implements Parcelable, Serializable{
    public enum Department {
        POLICE ("POLICE"), FIRE_RESCUE ("FIRE_RESCUE"), EMT ("EMT");

        //TODO this string seems not needed.
        public String Name;

        Department(String name) {
            this.Name = name;
        }

        public String getName() {
            return this.Name;
        }
    }

    public String sceneId;
    public String description;
    public String address;
    public LatLng location;


    public Incident(String sceneId, String description, String address, String latitude, String longitude) {
        this(sceneId, description, address, new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)));
    }

    public Incident(String sceneId, String description, String address, LatLng location) {
        this.sceneId = sceneId;
        this.description = description;
        this.address = address;
        this.location = location;
    }

    private Incident(Parcel in) {
        sceneId = in.readString();
        description = in.readString();
        address = in.readString();
        location = in.readParcelable(LatLng.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel outParcel, int flags) {
        outParcel.writeString(sceneId);
        outParcel.writeString(description);
        outParcel.writeString(address);
        outParcel.writeParcelable(location, 0);
    }

    public static final Parcelable.Creator<Incident> CREATOR = new Parcelable.Creator<Incident>() {
        public Incident createFromParcel(Parcel in) {
            return new Incident(in);
        }

        public Incident[] newArray(int size) {
            return new Incident[size];
        }
    };


    @Override
    //Useless override method required.
    public int describeContents() {
        return 0;
    }


    public String getSceneId() {
        return sceneId;
    }
    public String getDescription() {
        return description;
    }
    public String getAddress() { return address;}
    public String getLatitude() {
        return Double.toString(location.latitude);
    }
    public String getLongitude() {
        return Double.toString(location.longitude);
    }
}
