package com.eris.classes;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;


public class Responder {

    /*
     * Public Members
     */
    private String userID;
    private String sceneID;
    private String superiorID;
    private float heartRate;
    private String rank;
    private LatLng location;
    private String firstName;
    private String lastName;
    private Marker marker;
    private Branch branch;

    /**
     * Constructs new responder objects.
     *
     * @param userID User identification string.
     * @param firstName First name of the responder.
     * @param lastName Last name of the responder.
     */
    public Responder(String userID, String firstName, String lastName,
                     String rank, String superior, Branch branch) {

        /*
         * Initialize known values.
         */
        this.userID = userID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.rank = rank;
        this.superiorID = superior;
        this.branch = branch;

        /*
         * Initialize unknown values.
         */
        this.sceneID = null;
        this.heartRate = 0;
        this.location = null;
    }

    public String getID() {
        return userID;
    }

    public String getSceneID() {
        return sceneID;
    }

    public String getSuperior() {
        return superiorID;
    }

    public float getHeartRate() {
        return heartRate;
    }

    public String getRank() {
        return rank;
    }

    public LatLng getLocation() {
        return location;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastname() {
        return lastName;
    }

    public Marker getMarker() {
        return marker;
    }

    public Branch getBranch() {
        return branch;
    }
}
