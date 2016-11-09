package com.eris.classes;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.Arrays;
import java.util.List;


public class Responder implements Parcelable {

    public static final String TAG = Responder.class.getSimpleName();
    public static final String NO_ERROR = "no_error";
    public static final String QUERY_FAILED = "query_failed";

    /*
     * Public Members
     */
    //Why are these all public?  seems bad.
    private String userID;
    private String sceneID;
    private float heartRate;
    private String rank;
    private LatLng location;
    private String firstName;
    private String lastName;
    private String name;
    private Marker marker;

    private String organization;
    private List<String> heartrateRecord;
    private String orgSuperior;
    private List<String> orgSubordinates;
    private String incidentSuperior;
    private List<String> incidentSubordinates;

    /**
     * Constructs new responder objects.
     *
     * @param userID User identification string.
     * @param firstName First name of the responder.
     * @param lastName Last name of the responder.
     */
    public Responder(String userID, String firstName, String lastName) {

        /*
         * Initialize known values.
         */
        this.userID = userID;
        this.firstName = firstName;
        this.lastName = lastName;

        /*
         * Initialize unknown values.
         */
        this.sceneID = null;
        this.heartRate = 0;
        this.rank = null;
        this.location = null;
    }

    /**
     * Constructs new responder objects.
     *
     * @param userID User identification string.
     * @param firstName First name of the responder.
     * @param lastName Last name of the responder.
     */
    public Responder(String userID, String firstName, String lastName, String sceneID,
                     float heartRate, LatLng location, String rank) {

        // Initialize all given values
        this.userID = userID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.sceneID = sceneID;
        this.heartRate = heartRate;
        this.location = location;
        this.rank = rank;
    }

    public Responder(String userID, String name,  String organization, List<String> heartrateRecord,
                     String orgSuperior,  List<String> orgSubordinates, String latitude,
                     String longitude, String sceneID, String incidentSuperior,
                     List<String> incidentSubordinates) {
        //Should do null checks.

        this.userID = userID;
        this.name = name;
        if (name == null) {
            Log.e(TAG, "Name was null.");
            this.firstName = "NULLY";
            this.lastName = "NULLNULL";
        } else {
            String[] splitName = name.split(",");
            if (splitName.length == 2) {
                this.firstName = splitName[1];
                this.lastName = splitName[0];
            } else if (splitName.length == 1) {
                this.lastName = splitName[0];
                this.firstName = "NONE";
            } else {
                this.firstName = "INVALID";
                this.lastName = "NAME";
            }
        }
        this.organization = organization;
        this.heartrateRecord = heartrateRecord;
        if ((heartrateRecord != null) && (heartrateRecord.size() > 0)) {
            this.heartRate = Float.parseFloat(heartrateRecord.get(0));
        } else {
            this.heartRate = -999;
            Log.e(TAG, "No heartrate found.  Dead?");
        }
        this.orgSuperior = orgSuperior;
        this.orgSubordinates = orgSubordinates;
        this.location = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
        this.sceneID = sceneID;
        this.incidentSuperior = incidentSuperior;
        this.incidentSubordinates = incidentSubordinates;
    }

    private Responder(Parcel in) {
        userID = in.readString();
        name = in.readString();
        organization = in.readString();
        heartrateRecord = in.createStringArrayList();
        orgSuperior = in.readString();
        orgSubordinates = in.createStringArrayList();
        location = in.readParcelable(LatLng.class.getClassLoader());
        sceneID = in.readString();
        incidentSuperior = in.readString();
        incidentSubordinates = in.createStringArrayList();
    }

    //Override function, has no other use.
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel outParcel, int flags) {
        outParcel.writeString(userID);
        outParcel.writeString(name);
        outParcel.writeString(organization);
        outParcel.writeStringList(heartrateRecord);
        outParcel.writeString(orgSuperior);
        outParcel.writeStringList(orgSubordinates);
        outParcel.writeParcelable(location, 0);
        outParcel.writeString(sceneID);
        outParcel.writeString(incidentSuperior);
        outParcel.writeStringList(incidentSubordinates);
    }

    public static final Parcelable.Creator<Responder> CREATOR = new Parcelable.Creator<Responder>() {
        public Responder createFromParcel(Parcel in) {
            return new Responder(in);
        }

        public Responder[] newArray(int size) {
            return new Responder[size];
        }
    };



    public String getUserID() {
        return this.userID;
    }
    public String getName() {
        return this.name;
    }
    public String getFirstName() { return this.firstName; }
    public String getLastName() { return this.lastName; }
    public String getSceneID() {
        return this.sceneID;
    }
    public String getOrganization() {return  this.organization;}
    public List<String> getHeartrateRecord() {return this.heartrateRecord;}
    public float getHeartRate() { return  this.heartRate; }
    public String getOrgSuperior() {return  this.orgSuperior;}
    public List<String> getOrgSubordinates() {return this.orgSubordinates;}
    public String getLatitude() {return Double.toString(this.location.latitude);}
    public String getLongitude() {return Double.toString(this.location.longitude);}
    public LatLng getLocation() {return this.location;}
    public String getIncidentSuperior() {return  this.incidentSuperior;}
    public List<String> getIncidentSubordinates() {return this.incidentSubordinates;}
    public Marker getMarker() {return this.marker;}


    public void setLocation(LatLng location) {
        if (location == null) {
            throw new IllegalArgumentException("location cannot be null");
        }
        this.location = location;
    }
    public void setMarker(Marker marker) {
        if (marker == null) {
            throw new IllegalArgumentException("marker cannot be null");
        }
        this.marker = marker;}
    public void setIncidentSuperior(String superiorId) {
        if (superiorId == null) {
            throw new IllegalArgumentException("superiorId cannot be null");
        }
        this.incidentSuperior = superiorId;}
    public void setIncidentSubordinates(List<String> subordinateIds) {
        if (subordinateIds == null) {
            throw new IllegalArgumentException("subordinateIds cannot be null");
        }
        this.incidentSubordinates = subordinateIds;}

    /**
     * Set the incident/scene ID for this responder.
     *
     * @param id  The Incident ID to be set
     */
    public void setSceneID(String id) {
        this.sceneID = id;
    }
}