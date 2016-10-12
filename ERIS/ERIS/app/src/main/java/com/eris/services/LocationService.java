package com.eris.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class LocationService extends Service implements LocationListener {

    /*
     * Final Members
     */
    public static final String BROADCAST_ACTION_LOCATION_UPDATE = "location_update";
    public static final String KEY_LOCATION_LATITUDE = "key_location_latitude";
    public static final String KEY_LOCATION_LONGITUDE = "key_location_longitude";
    public static final String KEY_LOCATION_PROVIDER = "key_location_provider";

    /*
     * Private Members
     */
    private LocationManager locationManager;

    /**
     * Constructor for LocationService
     */
    public LocationService() {
        Log.d("service","LocationService created");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null; // No need to bind with this service yet.
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Request location updates if possible.
        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 0, this);
        }
        catch (SecurityException e) {
            Log.e("service","Location security exception");
        }

        Log.d("service","STARTED!");

        // If the OS runs out of memory, START_STICKY tells the OS to start this service back up
        // again once enough memory has been freed.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Remove location updates if possible.
        try {
            locationManager.removeUpdates(this);
        }
        catch (SecurityException e) {
            Log.e("service","Location security exception");
        }
    }

    /**
     * Runs every time a location update is requested. Interval is set through LocationManager.
     * <p>
     * This method determine the "freshness" of the location data and broadcast it app-wide if needed.
     *
     * @param location Construct holding latitude and longitude information.
     */
    @Override
    public void onLocationChanged(Location location) {

        Log.d("service","Latitude: " + location.getLatitude() + "\t" + "Longitude: " + location.getLongitude());

        // Put data into an intent and broadcast it.
        Intent intent = new Intent(BROADCAST_ACTION_LOCATION_UPDATE);
        intent.putExtra(KEY_LOCATION_LATITUDE, location.getLatitude());
        intent.putExtra(KEY_LOCATION_LONGITUDE, location.getLongitude());
        intent.putExtra(KEY_LOCATION_PROVIDER, location.getProvider());
        sendBroadcast(intent);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
