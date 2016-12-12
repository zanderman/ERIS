package com.eris.classes;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;


import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;


public class Responder implements Parcelable {

    public static final String TAG = Responder.class.getSimpleName();
    public static final String NO_ERROR = "no_error";
    public static final String QUERY_FAILED = "query_failed";
    public static final String NO_INCIDENT = "INCIDENT_NONE";

    /*
     * Public Members
     */
    private String userID;
    private String sceneID;
    private float heartRate;
    private String heartRateDate;
    private String rank;
    private LatLng location;
    private String locationDate;
    private String firstName;
    private String lastName;
    private String name;
    private Marker marker;

    private List<String> incidentHistory;//TODO now just how to encode/decode this.
    //private Set<String> incidentHistory;


    private String organization;
    private List<String> heartrateRecord;
    private String orgSuperior;
    private List<String> orgSubordinates;
    private String incidentSuperior;
    private List<String> incidentSubordinates;

    //private DateTimeZone EASTERN_TIME = DateTimeZone.forOffsetHours(-5);
    public DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("MM dd YYYY HH mm ss SSS");

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
        this.name = lastName + "," + firstName;

        /*
         * Initialize unknown values.
         */
        this.sceneID = null;
        this.heartRate = 0;
        this.rank = null;
        this.location = null;

        heartrateRecord = new ArrayList<String>(20);
        incidentHistory = new ArrayList<>();
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
        this.name = lastName + "," + firstName;
        this.sceneID = sceneID;
        this.heartRate = heartRate;
        this.location = location;
        this.rank = rank;

        this.heartrateRecord = new ArrayList<String>(20);
        this.heartrateRecord.add(Float.toString(heartRate));

        incidentHistory = new ArrayList<>();//TODO replace all these with constructor things.
    }

    public Responder(String userID, String name,  String organization, List<String> heartrateRecord,
                     String heartRateDate, String orgSuperior,  List<String> orgSubordinates, String latitude,
                     String longitude, String locationDate, String sceneID, String incidentSuperior,
                     List<String> incidentSubordinates, List<String> incidentHistory) {
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
        if ((heartrateRecord != null) && (!heartrateRecord.isEmpty())) {
            this.heartRate = Float.parseFloat(heartrateRecord.get(0));
        } else {
            this.heartRate = -999;
            Log.e(TAG, "No valid heart rate found.");
        }
        this.heartRateDate = heartRateDate;
        this.orgSuperior = orgSuperior;
        this.orgSubordinates = orgSubordinates;
        this.location = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
        this.locationDate = locationDate;
        this.sceneID = sceneID;
        this.incidentSuperior = incidentSuperior;
        this.incidentSubordinates = incidentSubordinates;

        if (incidentHistory == null) {
            this.incidentHistory = new ArrayList<>();
        } else {
            this.incidentHistory = incidentHistory;
        }
    }

    private Responder(Parcel in) {
        userID = in.readString();
        name = in.readString();
        organization = in.readString();
        heartrateRecord = in.createStringArrayList();
        heartRateDate = in.readString();
        orgSuperior = in.readString();
        orgSubordinates = in.createStringArrayList();
        location = in.readParcelable(LatLng.class.getClassLoader());
        locationDate = in.readString();
        sceneID = in.readString();
        incidentSuperior = in.readString();
        incidentSubordinates = in.createStringArrayList();
        incidentHistory = in.createStringArrayList();

        if (heartrateRecord.isEmpty()) {
            heartRate = -999;
        } else {
            heartRate = Float.parseFloat(heartrateRecord.get(0));
        }
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
        outParcel.writeString(heartRateDate);
        outParcel.writeString(orgSuperior);
        outParcel.writeStringList(orgSubordinates);
        outParcel.writeParcelable(location, 0);
        outParcel.writeString(locationDate);
        outParcel.writeString(sceneID);
        outParcel.writeString(incidentSuperior);
        outParcel.writeStringList(incidentSubordinates);
        outParcel.writeStringList(incidentHistory);
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
    public String getHeartRateDate() {return this.heartRateDate;}
    public float getHeartRate() { return this.heartRate; }
    public String getOrgSuperior() {return this.orgSuperior;}
    public List<String> getOrgSubordinates() {return this.orgSubordinates;}
    public String getLatitude() {return Double.toString(this.location.latitude);}
    public String getLongitude() {return Double.toString(this.location.longitude);}
    public LatLng getLocation() {return this.location;}
    public String getLocationDate() {return this.locationDate;}
    public String getIncidentSuperior() {return  this.incidentSuperior;}
    public List<String> getIncidentSubordinates() {return this.incidentSubordinates;}
    public Marker getMarker() {return this.marker;}
    public List<String> getIncidentHistory() {return this.incidentHistory;}

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        this.name = this.lastName + "," + this.firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        this.name = this.lastName + "," + this.firstName;
    }

    public void setLocation(LatLng location) {
        if (location == null) {
            throw new IllegalArgumentException("location cannot be null");
        }
        this.location = location;
    }

    public void setLocationDate(String locationDate) {
        if (locationDate == null) {
            throw new IllegalArgumentException("locationDate cannot be null");
        }
        this.locationDate = locationDate;
    }

    public void setHeartRateDate(String heartRateDate) {
        if (heartRateDate == null) {
            throw new IllegalArgumentException("heartRateDate cannot be null");
        }
        this.heartRateDate = heartRateDate;
    }

    public void setMarker(Marker marker) {
        if (marker == null) {
            throw new IllegalArgumentException("marker cannot be null");
        }
        this.marker = marker;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public void setOrgSuperior(String superiorId) {
        if (superiorId == null) {
            throw new IllegalArgumentException("superiorId cannot be null");
        }
        this.orgSuperior = superiorId;
    }

    public void setOrgSubordinates(List<String> subordinateIds) {
        if (subordinateIds == null) {
            throw new IllegalArgumentException("subordinateIds cannot be null");
        }
        this.orgSubordinates = subordinateIds;
    }

    public void setIncidentSuperior(String superiorId) {
        if (superiorId == null) {
            throw new IllegalArgumentException("superiorId cannot be null");
        }
        this.incidentSuperior = superiorId;
    }

    public void setIncidentSubordinates(List<String> subordinateIds) {
        if (subordinateIds == null) {
            throw new IllegalArgumentException("subordinateIds cannot be null");
        }
        this.incidentSubordinates = subordinateIds;
    }

    public void setHeartRate(float hr) {
        this.heartRate = hr; // Update current heartrate value.
        if (this.heartrateRecord.size() >= 20) this.heartrateRecord.remove(19);
        this.heartrateRecord.add(0, Float.toString(hr));
    }

    @Override
    public boolean equals(Object o) {
        Responder r = (Responder) o;
        return (this.getUserID().equals(r.getUserID()));
    }
}
