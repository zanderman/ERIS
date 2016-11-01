package com.eris.classes;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.List;


public class Responder {

    /*
     * Public Members
     */
    public String userID;
    public String sceneID;
    public float heartRate;
    public String rank;
    public LatLng location;
    public String firstName;
    public String lastName;
    public String name;
    public Marker marker;

    private String organization;
    private List<String> heartrateRecord;
    private String orgSuperior;
    private List<String> orgSubordinates;
    private String latitude;
    private String longitude;
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
        this.userID = userID;
        this.name = name;
        String[] splitName = name.split(",");
        this.firstName = splitName[1];
        this.lastName = splitName[0];
        this.organization = organization;
        this.heartrateRecord = heartrateRecord;
        this.heartRate = Float.parseFloat(heartrateRecord.get(0));
        this.orgSuperior = orgSuperior;
        this.orgSubordinates = orgSubordinates;
        this.latitude = latitude;
        this.longitude = longitude;
        this.location = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
        this.sceneID = sceneID;
        this.incidentSuperior = incidentSuperior;
        this.incidentSubordinates = incidentSubordinates;
    }

    public String getUserID() {
        return this.userID;
    }
    public String getName() {
        return this.name;
    }
    public String getSceneID() {
        return this.sceneID;
    }
    public String getOrganization() {return  this.organization;}
    public List<String> getHeartrateRecord() {return this.heartrateRecord;}
    public String getOrgSuperior() {return  this.orgSuperior;}
    public List<String> getOrgSubordinates() {return this.orgSubordinates;}
    public String getLatitude() {return this.latitude;}
    public String getLongitude() {return  this.longitude;}
    public String getIncidentSuperior() {return  this.incidentSuperior;}
    public List<String> getIncidentSubordinates() {return this.incidentSubordinates;}

}
