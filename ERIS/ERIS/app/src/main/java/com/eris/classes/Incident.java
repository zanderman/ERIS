package com.eris.classes;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 * Incident
 *
 * Description:
 *  Represents an incident. Contains all pertinent information regarding the incident.
 */
public class Incident implements Serializable {

    /*
     * Public Members
     */
    public String id;
    public String description;
    public String address;
    public LatLng location;


    /**
     * Constructor
     *
     * Description:
     *  Only initializes the description of the incident.
     *
     * @param description Information about the incident.
     * @param address Address of the incident.
     * @param location Latitude/Longitude of the incident.
     */
    public Incident(String id, String description, String address, LatLng location) {
        this.id = id;
        this.description = description;
        this.address = address;
        this.location = location;
    }

}
