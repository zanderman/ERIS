package io.github.zanderman.eris;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                showTeamButton = (Button) stub.findViewById(R.id.showTeamButton);
                showTeamButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

//                        ArrayList<Responder> entries = new ArrayList<Responder>();
//                        entries.add( new Responder("0", "Cena, John", "EMS", Arrays.asList("100","101"), "dummy", null, "0000", "0000", "123", "dummy", null) );
//                        entries.add( new Responder("1", "Jonas, Mike", "EMS", Arrays.asList("98","99"), "dummy", null, "0000", "0000", "123", "dummy", null) );
//                        entries.add( new Responder("2", "Billy, Bob", "EMS", Arrays.asList("98","99"), "dummy", null, "0000", "0000", "123", "dummy", null) );
//                        entries.add( new Responder("3", "Jonny, Wayne", "EMS", Arrays.asList("98","99"), "dummy", null, "0000", "0000", "123", "dummy", null) );
//                        entries.add( new Responder("4", "Dude, Wilson", "EMS", Arrays.asList("98","99"), "dummy", null, "0000", "0000", "123", "dummy", null) );
//                        entries.add( new Responder("5", "Willard, Denison", "EMS", Arrays.asList("98","99"), "dummy", null, "0000", "0000", "123", "dummy", null) );
//                        entries.add( new Responder("6", "Goob, DaNoob", "EMS", Arrays.asList("98","99"), "dummy", null, "0000", "0000", "123", "dummy", null) );
//                        entries.add( new Responder("0", "John", "Cena", "233", 123f, null, "asdf") );
//                        entries.add( new Responder("1", "Mike", "Jonas", "233", 100f, null, "asdf") );

                        Intent intent = new Intent(getApplicationContext(), WheelActivity.class);
//                        intent.putParcelableArrayListExtra("responders", entries);
                        startActivity(intent);
                    }
                });

//                connectingLayout = (LinearLayout) stub.findViewById(R.id.waitLayout);
//
//                if (!connected) {
//                    showTeamButton.setVisibility(View.GONE);
//                    connectingLayout.setVisibility(View.VISIBLE);
//
//                    // TODO: connect to phone
//                    buildGoogleApiClient();
//                    connectGoogleApiClient();
//                }
//                else {
//                    showTeamButton.setVisibility(View.VISIBLE);
//                    connectingLayout.setVisibility(View.GONE);
//                }

            }
        });
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
}
