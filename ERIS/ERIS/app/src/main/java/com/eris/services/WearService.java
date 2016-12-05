package com.eris.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
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
        DataApi.DataListener {

    /*
     * Broadcast keys
     */
    public static final String BROADCAST_ACTION_WEARABLE_UPDATE = "wearable_update";
    public static final String KEY_WEARABLE_HEARTRATE = "key_wearable_heartrate";

    /*
     * Google API Client
     */
    private GoogleApiClient googleApiClient;
    private boolean connected = false;

    /**
     * Constructor for WearService
     */
    public WearService() {
        Log.d("service", "WearService created");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Connect to wearable if possible.
        if (!connected) {

            // Setup Google API Client.
            buildGoogleApiClient();
            connectGoogleApiClient();
        }

        // If the OS runs out of memory, START_STICKY tells the OS to start this service back up
        // again once enough memory has been freed.
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
        Wearable.DataApi.addListener(googleApiClient, this); // this is how we'll communicate with the watch.
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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
}
