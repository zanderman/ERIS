package com.eris.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;

import com.eris.R;

public class CommunicationService extends Service
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        DataApi.DataListener,
        ResultCallback<NodeApi.GetConnectedNodesResult>,
        NodeApi.NodeListener {

    /*
     * Private members
     */
    private GoogleApiClient googleApiClient;
    private boolean connected_mobile = false;
    private boolean connected_google_api = false;
    public final IBinder connectionServiceBinder = new ConnectionServiceBinder();
    private SharedPreferences sharedPreferences;

    /*
     * Broadcast keys
     */
    public static final String BROADCAST_ACTION_COMMUNICATION_UPDATE = "communication_update";
    public static final String KEY_COMMUNICATION_CONNECTION_STATUS = "key_communication_connection_status";
    public static final String KEY_COMMUNICATION_RESPONDER = "key_communication_responder";

    public CommunicationService() {
        Log.d("service", "CommunicationService created");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.sharedPreferences = getSharedPreferences(getString(R.string.communication_prefs), Context.MODE_PRIVATE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return connectionServiceBinder;
    }

    public class ConnectionServiceBinder extends Binder {
        public CommunicationService getService() { return CommunicationService.this; }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        /*
         * Connect Google API clients if needed.
         */
        if (!connected_google_api) {
            buildGoogleApiClient();
            connectGoogleApiClient();
        }
        else if (!connected_mobile) {
            this.checkNodeAPI();
            Wearable.NodeApi.addListener(googleApiClient, this);
        }
        else {

            // Put value into shared preferences.
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_COMMUNICATION_CONNECTION_STATUS,true);
            editor.commit();
            // Send broadcast signifying that the watch is connected with a phone.
            Intent i = new Intent(BROADCAST_ACTION_COMMUNICATION_UPDATE);
            i.putExtra(KEY_COMMUNICATION_CONNECTION_STATUS,true);
            sendBroadcast(i);
        }

        // If the OS runs out of memory, START_STICKY tells the OS to start this service back up
        // again once enough memory has been freed.
        return START_STICKY;
    }

    /*
     * Section: Google API Client
     */

    private void buildGoogleApiClient() {
        // Iitialize Google API client.
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Wearable.API) // Request access only to the wearable API.
                    .build();
        }
    }

    private void connectGoogleApiClient() {
        if (!googleApiClient.isConnected()) googleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("service", "connected");

        // Now that the Google API client is connected,
        // ensure that we're connected to a phone.
        this.checkNodeAPI();
        Wearable.NodeApi.addListener(googleApiClient, this);
        Wearable.DataApi.addListener(googleApiClient, this); // this is how we'll communicate with the phone.
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("service", "connection suspended");
        this.connected_google_api = false;
        Wearable.NodeApi.removeListener(googleApiClient,this);
        Wearable.DataApi.removeListener(googleApiClient,this);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("service", "connection failed");
    }

    /**
     * SEND
     */
    public class SendDataTask extends AsyncTask<Float, Void, Void> {

        @Override
        protected Void doInBackground(Float... params) {

            PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/heartrate");

//            Log.d("hudwear", "params.length: " + params.length);
            putDataMapReq.getDataMap().putFloat("heartrate", params[0]);

            PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
            DataApi.DataItemResult dataItemResult =
                    Wearable.DataApi.putDataItem(googleApiClient, putDataReq).await();

            return null;
        }
    }

    /**
     * Helper method to send data to phone via AsyncTask.
     *
     * @param args Data to send to the phone.
     */
    public void transmit(Float... args) {
        Log.d("service", "transmitting");
        SendDataTask t = new SendDataTask();
        t.execute(args);
    }

    /**
     * RECEIVE
     *
     * Handle information sent from the connected mobile device.
     * @param dataEventBuffer
     */
    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        Log.d("service", "data was changed");
        for (DataEvent event : dataEventBuffer) {
            Log.d("service","Event received: " + event.getDataItem().getUri());

            if (event.getType() == DataEvent.TYPE_CHANGED) {

                // DataItem changed
                DataItem item = event.getDataItem();

                // Process reception of responder list.
                if (item.getUri().getPath().compareTo("/responderlist") == 0) {
                    DataMap dm = DataMapItem.fromDataItem(item).getDataMap();

                    // Send broadcast containing new responders.
                    Intent intent = new Intent(BROADCAST_ACTION_COMMUNICATION_UPDATE);
                    intent.putStringArrayListExtra(KEY_COMMUNICATION_RESPONDER,(ArrayList<String>) dm.get("responderlist"));
                    sendBroadcast(intent);
                }

            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }






    /*
     * Section: NodeAPI
     */

    private void checkNodeAPI(){
        Wearable.NodeApi.getConnectedNodes(googleApiClient).setResultCallback(this);
    }

    @Override
    public void onResult(@NonNull NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
        if (getConnectedNodesResult != null && getConnectedNodesResult.getNodes() != null){
            for (Node node : getConnectedNodesResult.getNodes()) {
                if (node.isNearby()) {
                    Log.d("service", "connected to phone");
                    connected_mobile = true;
                }
            }

            // Put value into shared preferences.
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_COMMUNICATION_CONNECTION_STATUS,connected_mobile);
            editor.commit();
            // Send broadcast signifying the status of mobile device connection.
            Intent i = new Intent(BROADCAST_ACTION_COMMUNICATION_UPDATE);
            i.putExtra(KEY_COMMUNICATION_CONNECTION_STATUS,connected_mobile);
            sendBroadcast(i);
        }
    }

    @Override
    public void onPeerConnected(Node node) {
        Log.d("service", "connected to phone");
        connected_mobile = true;

        // Put value into shared preferences.
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_COMMUNICATION_CONNECTION_STATUS,connected_mobile);
        editor.commit();
        // Send broadcast signifying that the watch is connected with a phone.
        Intent i = new Intent(BROADCAST_ACTION_COMMUNICATION_UPDATE);
        i.putExtra(KEY_COMMUNICATION_CONNECTION_STATUS,connected_mobile);
        sendBroadcast(i);
    }

    @Override
    public void onPeerDisconnected(Node node) {
        Log.d("service", "disconnected from phone");
        connected_mobile = false;

        // Put value into shared preferences.
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_COMMUNICATION_CONNECTION_STATUS,connected_mobile);
        editor.commit();
        // Send broadcast signifying that the watch is disconnected with a phone.
        Intent i = new Intent(BROADCAST_ACTION_COMMUNICATION_UPDATE);
        i.putExtra(KEY_COMMUNICATION_CONNECTION_STATUS,connected_mobile);
        sendBroadcast(i);
    }

}
