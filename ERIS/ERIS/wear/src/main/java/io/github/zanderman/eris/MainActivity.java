package io.github.zanderman.eris;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.view.DismissOverlayView;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

import io.github.zanderman.eris.services.CommunicationService;

public class MainActivity extends Activity {

    private Button showTeamButton;
    private LinearLayout connectingLayout;
    private DismissOverlayView mDismissOverlay;
    private GestureDetector mDetector;
    private BroadcastReceiver receiver;
    private IntentFilter receiverFilter;

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

            }
        });

        // Setup gesture detector for DismissOverlay.
        setupGestureDetectors();

        // Setup broadcast receiver.
        setupBroadcastReceiver();

        // Start services.
        startService(new Intent(this, CommunicationService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.registerReceiver(this.receiver,receiverFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(this.receiver);
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
