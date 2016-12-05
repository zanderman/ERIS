package io.github.zanderman.eris.classes;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by derieux on 12/4/16.
 */

public class SimpleResponder {

    /*
     * Members
     */
    public String id;
    public float heartRate;
    public LatLng location;
    public String name;

    public SimpleResponder(String id, String name, LatLng location, float heartRate) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.heartRate = heartRate;
    }

    @Override
    public boolean equals(Object obj) {
        SimpleResponder r = (SimpleResponder) obj;
        return (this.id.equals(r.id));
    }
}
