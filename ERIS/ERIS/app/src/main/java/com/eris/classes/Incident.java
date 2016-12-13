package com.eris.classes;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import org.joda.time.Instant;

/**
 * Incident
 *
 * Description:
 *  Represents an incident. Contains all pertinent information regarding the incident.
 */
public class Incident implements Parcelable, Serializable{

    /*
     * Public Members
     */
    public enum Department {
        POLICE ("POLICE"), FIRE_RESCUE ("FIRE_RESCUE"), EMS ("EMS");

        //TODO this string seems not needed.
        public String Name;

        Department(String name) {
            this.Name = name;
        }

        public String getName() {
            return this.Name;
        }
    }
    //TODO really, these should be private.
    private Instant startTime;
    private Instant endTime;
    private String time;
    private String title;
    private String description;
    private String address;
    private LatLng location;
    private String sceneId;
    private List<String> organizations;
    public String localCheckInTime;


    public Incident(String sceneId, String description, String address, String latitude, String longitude, String time, String title, List<String> organizations) {
        this(sceneId, description, address, new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)), time, title, organizations);
    }

    public Incident(String sceneId, String description, String address, LatLng location, String time, String title, List<String> organizations) {
        this.sceneId = sceneId;
        this.description = description;
        this.address = address;
        this.location = location;
        this.time = time;
        this.title = title;
        this.organizations = organizations;
    }

    private Incident(Parcel in) {
        sceneId = in.readString();
        description = in.readString();
        address = in.readString();
        location = in.readParcelable(LatLng.class.getClassLoader());
        time = in.readString();
        title = in.readString();
        organizations = in.createStringArrayList();
    }

    @Override
    public void writeToParcel(Parcel outParcel, int flags) {
        outParcel.writeString(sceneId);
        outParcel.writeString(description);
        outParcel.writeString(address);
        outParcel.writeParcelable(location, 0);
        outParcel.writeString(time);
        outParcel.writeString(title);
        outParcel.writeStringList(organizations);
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

    public List<String> getOrganizations() {
        return this.organizations;
    }
    public String getTitle() {
        return this.title;
    }
    public String getTime() {
        return this.time;
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
    public LatLng getLocation() { return location; }
}
