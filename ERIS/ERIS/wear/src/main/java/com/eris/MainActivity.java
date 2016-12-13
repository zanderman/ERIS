package com.eris;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.wearable.view.DismissOverlayView;
import android.support.wearable.view.WatchViewStub;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.eris.R;

import com.eris.services.CommunicationService;

public class MainActivity extends Activity {

    private Button showTeamButton;
    private LinearLayout connectingLayout;
    private DismissOverlayView mDismissOverlay;
    private GestureDetector mDetector;
    private BroadcastReceiver receiver;
    private IntentFilter receiverFilter;
    private SharedPreferences sharedPreferences;

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
                        startActivity(intent);
                    }
                });

                // Initially set the connecting layout to be visible.
                showTeamButton.setVisibility(View.GONE);
                connectingLayout.setVisibility(View.VISIBLE);

                // Start services.
                startService(new Intent(getApplicationContext(), CommunicationService.class));
            }
        });

        // Setup gesture detector for DismissOverlay.
        setupGestureDetectors();

        // Setup broadcast receiver.
        setupBroadcastReceiver();

        // Gain access to shared preferences.
        this.sharedPreferences = getSharedPreferences(getString(R.string.communication_prefs), Context.MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.registerReceiver(this.receiver,receiverFilter);

        // Determine if we're connected or not.
        if ((showTeamButton != null) && (connectingLayout != null)) {
            if ((sharedPreferences != null)
                    && (sharedPreferences.getBoolean(CommunicationService.KEY_COMMUNICATION_CONNECTION_STATUS,false))) {
                showTeamButton.setVisibility(View.VISIBLE);
                connectingLayout.setVisibility(View.GONE);
            } else {
                showTeamButton.setVisibility(View.GONE);
                connectingLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(this.receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Stop the communication service if we quit the app.
        this.stopService(new Intent(getApplicationContext(), CommunicationService.class));
    }

    public void setupBroadcastReceiver() {

        this.receiverFilter = new IntentFilter();
        this.receiverFilter.addAction(CommunicationService.BROADCAST_ACTION_COMMUNICATION_UPDATE);
        this.receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // Determine which broadcast was sent.
                switch (intent.getAction()) {

                    // Communication update.
                    case CommunicationService.BROADCAST_ACTION_COMMUNICATION_UPDATE:
                        boolean status = intent.getBooleanExtra(CommunicationService.KEY_COMMUNICATION_CONNECTION_STATUS, false);

                        // Allow progression through app if we're connected to a device.
                        if (status) {
                            showTeamButton.setVisibility(View.VISIBLE);
                            connectingLayout.setVisibility(View.GONE);
                        }else {
                            showTeamButton.setVisibility(View.GONE);
                            connectingLayout.setVisibility(View.VISIBLE);
                        }

                        break;

                    // Unhandled broadcast
                    default:
                        break;
                }
            }
        };
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
