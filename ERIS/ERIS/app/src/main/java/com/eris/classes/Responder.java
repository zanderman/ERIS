package com.eris.classes;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;


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
    public Marker marker;

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

    public String getUserID() {
        return this.userID;
    }

    public String getSceneID() {
        return this.sceneID;
    }

    public float getHeartRate() {
        return this.heartRate;
    }

    public String getRank() {
        return this.rank;
    }

    public LatLng getLocation() {
        return this.location;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public Marker getMarker() {
        return this.marker;
    }
}
