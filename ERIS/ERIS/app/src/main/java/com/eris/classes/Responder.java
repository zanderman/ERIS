package com.eris.classes;

import android.location.Location;


public class Responder {

    /*
     * Public Members
     */
    public String userID;
    public String sceneID;
    public float heartRate;
    public String rank;
    public Location location;
    public String firstName;
    public String lastName;

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
}
