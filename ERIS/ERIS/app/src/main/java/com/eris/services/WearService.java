package com.eris.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.eris.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
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

/**
 * Created by derieux on 12/5/16.
 */

public class WearService extends Service
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        DataApi.DataListener,
        ResultCallback<NodeApi.GetConnectedNodesResult>,
        NodeApi.NodeListener {

    /*
     * Constants
     */
    private final IBinder wearServiceBinder = new WearServiceBinder();

    /*
     * Broadcast keys
     */
    public static final String BROADCAST_ACTION_WEARABLE_UPDATE = "wearable_update";
    public static final String KEY_WEARABLE_HEARTRATE = "key_wearable_heartrate";
    public static final String KEY_WEARABLE_CONNECTION_STATUS = "key_wearable_connection_status";

    /*
     * Google API Client
     */
    private GoogleApiClient googleApiClient;
    private boolean connected_mobile = false;
    private boolean connected_google_api = false;

    private SharedPreferences sharedPreferences;

    /**
     * Constructor for WearService
     */
    public WearService() {
        Log.d("service", "WearService created");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.sharedPreferences = getSharedPreferences(getString(R.string.sharedpreferences_communication_info), Context.MODE_PRIVATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Connect to wearable if possible.
        if (!connected_google_api) {
            Log.d("service", "WearService - connecting to google APIs");

            // Setup Google API Client.
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
            editor.putBoolean(KEY_WEARABLE_CONNECTION_STATUS,true);
            editor.commit();
            // Send broadcast signifying that the watch is connected with a phone.
            Intent i = new Intent(BROADCAST_ACTION_WEARABLE_UPDATE);
            i.putExtra(KEY_WEARABLE_CONNECTION_STATUS,true);
            sendBroadcast(i);
        }

        Log.d("service", "WearService STARTED!");

        // If the OS runs out of memory, START_STICKY tells the OS to start this service back up
        // again once enough memory has been freed.
        return START_STICKY;
    }



    @Override
    public IBinder onBind(Intent intent) {
        return wearServiceBinder;
    }

    private void buildGoogleApiClient() {
        // Iitialize Google API client.
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this.getApplicationContext())
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
        // Now that the Google API client is connected,
        // ensure that we're connected to a phone.
        this.checkNodeAPI();
        Wearable.NodeApi.addListener(googleApiClient, this);
        Wearable.DataApi.addListener(googleApiClient, this); // this is how we'll communicate with the watch.
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
    public class SendDataTask extends AsyncTask<ArrayList<String>, Void, Void> {
        @Override
        protected Void doInBackground(ArrayList<String>... arrayLists) {

            // Create the data packet to be sent.
            PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/responderlist");
            putDataMapReq.getDataMap().putStringArrayList("responderlist", arrayLists[0]);

            // Send the data packet.
            PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
            PendingResult<DataApi.DataItemResult> pendingResult =
                    Wearable.DataApi.putDataItem(googleApiClient, putDataReq);

            return null;
        }
    }

    /**
     * RECEIVE
     *
     * Handle information sent from the connected mobile device.
     * @param dataEventBuffer
     */
    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        for (DataEvent event : dataEventBuffer) {
            Log.d("service","Event received: " + event.getDataItem().getUri());

            if (event.getType() == DataEvent.TYPE_CHANGED) {

                // DataItem changed
                DataItem item = event.getDataItem();

                // Process reception of responder list.
                if (item.getUri().getPath().compareTo("/heartrate") == 0) {
                    DataMap dm = DataMapItem.fromDataItem(item).getDataMap();

                    Message m = new Message();
                    Bundle b = new Bundle();
                    b.putFloat("heartrate",dm.getFloat("heartrate"));
                    m.setData(b);
                    handler.sendMessage(m);
                }

            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }

    /**
     * Update information on UI thread.
     */
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Float hr = msg.getData().getFloat("heartrate");
            Log.d("service", "Handling message from watch on UI thread. Got heartrate of: " + hr + " bpm");
        }
    };




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
                    Log.d("service", "connected to watch");
                    connected_mobile = true;
                }
            }

            // Put value into shared preferences.
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_WEARABLE_CONNECTION_STATUS,connected_mobile);
            editor.commit();
            // Send broadcast signifying the status of mobile device connection.
            Intent i = new Intent(BROADCAST_ACTION_WEARABLE_UPDATE);
            i.putExtra(KEY_WEARABLE_CONNECTION_STATUS,connected_mobile);
            sendBroadcast(i);
        }
    }

    @Override
    public void onPeerConnected(Node node) {
        Log.d("service", "connected to watch");
        connected_mobile = true;

        // Put value into shared preferences.
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_WEARABLE_CONNECTION_STATUS,connected_mobile);
        editor.commit();
        // Send broadcast signifying that the watch is connected with a phone.
        Intent i = new Intent(BROADCAST_ACTION_WEARABLE_UPDATE);
        i.putExtra(KEY_WEARABLE_CONNECTION_STATUS,connected_mobile);
        sendBroadcast(i);
    }

    @Override
    public void onPeerDisconnected(Node node) {
        Log.d("service", "disconnected from watch");
        connected_mobile = false;

        // Put value into shared preferences.
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_WEARABLE_CONNECTION_STATUS,connected_mobile);
        editor.commit();
        // Send broadcast signifying that the watch is disconnected with a phone.
        Intent i = new Intent(BROADCAST_ACTION_WEARABLE_UPDATE);
        i.putExtra(KEY_WEARABLE_CONNECTION_STATUS,connected_mobile);
        sendBroadcast(i);
    }






    public class WearServiceBinder extends Binder {
        public WearService getService() { return WearService.this; }
    }
}
