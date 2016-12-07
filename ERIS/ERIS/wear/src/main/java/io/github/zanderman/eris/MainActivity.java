package io.github.zanderman.eris;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.view.DismissOverlayView;
import android.support.wearable.view.WatchViewStub;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends Activity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        DataApi.DataListener {

    private TextView mTextView;
    private Button showTeamButton;
    private LinearLayout connectingLayout;
    private GoogleApiClient googleApiClient;
    private boolean connected = false;
    private DismissOverlayView mDismissOverlay;
    private GestureDetector mDetector;

    /*
     * TODO:
     * - write handshake method
     * - advance to incident list once connected to phone app
     * - if phone app is already in incident, go straight to team view
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {

                // Obtain the DismissOverlayView element
                mDismissOverlay = (DismissOverlayView) stub.findViewById(R.id.dismiss_overlay);
                mDismissOverlay.setIntroText("Long press to exit app");
                mDismissOverlay.showIntroIfNecessary();

                connectingLayout = (LinearLayout) stub.findViewById(R.id.waitLayout);
                showTeamButton = (Button) stub.findViewById(R.id.showTeamButton);
                showTeamButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(getApplicationContext(), WheelActivity.class);
//                        intent.putParcelableArrayListExtra("responders", entries);
                        startActivity(intent);
                    }
                });

//
                if (!connected) {
                    showTeamButton.setVisibility(View.GONE);
                    connectingLayout.setVisibility(View.VISIBLE);

                    buildGoogleApiClient();
                    connectGoogleApiClient();
                }
                else {
                    showTeamButton.setVisibility(View.VISIBLE);
                    connectingLayout.setVisibility(View.GONE);
                }

            }
        });

        setupGestureDetectors();
    }

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
        connected = true;
        showTeamButton.setVisibility(View.VISIBLE);
        connectingLayout.setVisibility(View.GONE);
//        Wearable.DataApi.addListener(googleApiClient, this); // this is how we'll communicate with the phone.
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

    }

    /**
     * Helper method that allows setup of the gesture detector.
     */
    private void setupGestureDetectors() {

        // Configure a gesture detector
        mDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            public void onLongPress(MotionEvent ev) {
                mDismissOverlay.show();
            }
        });
    }

    /**
     * Capture long presses
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mDetector.onTouchEvent(ev) || super.onTouchEvent(ev);
    }
}
