package io.github.zanderman.eris.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class CommunicationService extends Service
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        DataApi.DataListener,
        ResultCallback<NodeApi.GetConnectedNodesResult>, NodeApi.NodeListener {

    /*
     * Private members
     */
    private GoogleApiClient googleApiClient;
    private boolean connected_mobile = false;
    private boolean connected_google_api = false;
    private final IBinder connectionServiceBinder = new ConnectionServiceBinder();

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

        // Now that the Google API client is connected,
        // ensure that we're connected to a phone.
        this.checkNodeAPI();
        Wearable.NodeApi.addListener(googleApiClient, this);
        Wearable.DataApi.addListener(googleApiClient, this); // this is how we'll communicate with the phone.
    }

    @Override
    public void onConnectionSuspended(int i) {
        this.connected_google_api = false;
        Wearable.NodeApi.removeListener(googleApiClient,this);
        Wearable.DataApi.removeListener(googleApiClient,this);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

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

        // Send broadcast signifying that the watch is connected with a phone.
        Intent i = new Intent(BROADCAST_ACTION_COMMUNICATION_UPDATE);
        i.putExtra(KEY_COMMUNICATION_CONNECTION_STATUS,connected_mobile);
        sendBroadcast(i);
    }

    @Override
    public void onPeerDisconnected(Node node) {
        Log.d("service", "disconnected from phone");
        connected_mobile = false;

        // Send broadcast signifying that the watch is disconnected with a phone.
        Intent i = new Intent(BROADCAST_ACTION_COMMUNICATION_UPDATE);
        i.putExtra(KEY_COMMUNICATION_CONNECTION_STATUS,connected_mobile);
        sendBroadcast(i);
    }
}
