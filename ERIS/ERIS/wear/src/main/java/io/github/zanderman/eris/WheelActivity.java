package io.github.zanderman.eris;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.wearable.view.DismissOverlayView;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.lukedeighton.wheelview.Circle;
import com.lukedeighton.wheelview.WheelView;
import com.lukedeighton.wheelview.adapter.WheelAdapter;
import com.lukedeighton.wheelview.adapter.WheelArrayAdapter;
import com.lukedeighton.wheelview.transformer.WheelSelectionTransformer;

import java.util.ArrayList;
import java.util.List;

import io.github.zanderman.eris.classes.SimpleResponder;
import io.github.zanderman.eris.drawables.TextDrawable;
import io.github.zanderman.eris.services.CommunicationService;

public class WheelActivity extends Activity
        implements SensorEventListener,
            OnMapReadyCallback {

    private static final float ZOOM_LEVEL = 16f;
    private LinearLayout connectingLayout;
    private RelativeLayout infoLayout;
    private WheelView wheelView;
    private ArrayList<SimpleResponder> responders;
    private DismissOverlayView mDismissOverlay;
    private GestureDetector mDetector;
    private final int REQUEST_CODE_BODY_SENSORS = 1111;
    private BroadcastReceiver receiver;
    private IntentFilter receiverFilter;
    private CommunicationService communicationService;
    private SharedPreferences sharedPreferences;

    /*
     * Sensors
     */
    private SensorManager sm;
    private Sensor heartrateSensor;

    /*
     * Google Maps
     */
    private GoogleMap map;
    private FrameLayout mapFrame;
    private Marker liveMarker = null;


    /*
     * Service connectivity.
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            if (iBinder instanceof CommunicationService.ConnectionServiceBinder) {
                communicationService = ((CommunicationService.ConnectionServiceBinder)iBinder).getService();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wheel);

        // Get intent reference that was used to create this activity.
        Intent intent = getIntent();

        // Gain access to list of responders current being used.
        responders = new ArrayList<SimpleResponder>();//intent.getParcelableArrayListExtra("responders");
        responders.add(new SimpleResponder("0","Johnson, J.",new LatLng(0.0,0.0),(float)88.7));
        responders.add(new SimpleResponder("1","Fife, B.",new LatLng(0.0,0.0),(float)86.4));
        responders.add(new SimpleResponder("2","Taylor, O.",new LatLng(0.0,0.0),(float)82.8));
        responders.add(new SimpleResponder("3","Wilson, D.",new LatLng(0.0,0.0),(float)91.3));
        responders.add(new SimpleResponder("4","Smith, K.",new LatLng(0.0,0.0),(float)83.1));
        responders.add(new SimpleResponder("5","Wright, N.",new LatLng(0.0,0.0),(float)85.9));
        responders.add(new SimpleResponder("6","Sherman, P.",new LatLng(0.0,0.0),(float)92.8));

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {

                // Get view elements.
                mapFrame = (FrameLayout) stub.findViewById(R.id.mapFrame);
                connectingLayout = (LinearLayout) stub.findViewById(R.id.waitLayout);
                infoLayout = (RelativeLayout) stub.findViewById(R.id.infoLayout);
                wheelView = (WheelView) stub.findViewById(R.id.wheelview);

                // Obtain the DismissOverlayView element
                mDismissOverlay = (DismissOverlayView) stub.findViewById(R.id.dismiss_overlay);
                mDismissOverlay.setIntroText("Long press to exit app");
                mDismissOverlay.showIntroIfNecessary();
                mDismissOverlay.bringToFront();
                mDismissOverlay.hasFocus();

                // Create Google Map
                inflateMap();

                // Creation of WheelView
                setupWheel();

            }
        });

        // Setup sensors.
        setupSensors();

        // Setup dismissal overlay.
        setupGestureDetectors();

        // Setup broadcast receiver.
        setupBroadcastReceiver();

        // Bind with necessary services.
        this.bindService(new Intent(this, CommunicationService.class), serviceConnection, Context.BIND_AUTO_CREATE);

        // Gain access to shared preferences.
        this.sharedPreferences = getSharedPreferences(getString(R.string.communication_prefs), Context.MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Determine if we're connected or not.
        if ((sharedPreferences != null)
                && (sharedPreferences.getBoolean(CommunicationService.KEY_COMMUNICATION_CONNECTION_STATUS,false))) {
            sm.registerListener(this, heartrateSensor, 300000); // sample every 30 milliseconds.
            this.registerReceiver(this.receiver,receiverFilter);
        } else {
            Toast.makeText(getApplicationContext(),"Connection lost", Toast.LENGTH_SHORT).show();
            this.finish(); // Exit the activity.
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        sm.unregisterListener(this);
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

                        // Quit activity if we're not connected with a phone anymore.
                        if (!status) {
                            Toast.makeText(getApplicationContext(),"Connection lost", Toast.LENGTH_SHORT).show();
                            finish();
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
     * Sets up the wheel view.
     */
    private void setupWheel() {

        wheelView.setWheelItemCount(responders.size());
        wheelView.setAdapter(new WheelAdapter() {
            @Override
            public Drawable getDrawable(int position) {
                SimpleResponder r = (SimpleResponder) getItem(position);
                TextDrawable nameTD = new TextDrawable(r.name);
                TextDrawable heartrateTD = new TextDrawable(r.heartRate + "");
                Drawable background = getResources().getDrawable(R.drawable.circle);
                background.setBounds(0,0,40,40); // Make background circle 40x40
                Drawable[] layers = {background, nameTD, heartrateTD};
                LayerDrawable ld = new LayerDrawable(layers);
                ld.setLayerInset(0, 0,0,0,0); // background
                ld.setLayerInset(1, 0,20,0,20); // name
                ld.setLayerInset(2, 0,35,0,5); // heartrate
                return ld;
            }

            @Override
            public int getCount() {
                return responders.size();
            }

            @Override
            public Object getItem(int position) {
                return responders.get(position);
            }
        });


        wheelView.setOnWheelItemClickListener(new WheelView.OnWheelItemClickListener() {
            @Override
            public void onWheelItemClick(WheelView parent, int position, boolean isSelected) {
                SimpleResponder r = responders.get(position);
//                Toast.makeText(WheelActivity.this, "clicked: " + r.name, Toast.LENGTH_SHORT).show();


                // TODO: clicking on item opens Google Map to where the responder is located (possibly add distance to that responder)
            }
        });
        wheelView.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectListener() {
            @Override
            public void onWheelItemSelected(WheelView parent, Drawable itemDrawable, int position) {
                SimpleResponder r = responders.get(position);
//                Toast.makeText(WheelActivity.this, "selected: " + r.name, Toast.LENGTH_SHORT).show();

                // Center map on responder location.
                centerMapOnLocation(r.location);
//                        if ( liveMarker != null ) liveMarker.remove();
//                        liveMarker = map.addMarker(
//                                new MarkerOptions()
//                                        .position(new LatLng(Double.parseDouble(r.getLatitude()),
//                                                Double.parseDouble(r.getLongitude())))
//                                        .title(r.getName())
//                                        .snippet(r.getOrganization() + "  //  "
//                                                + r.getHeartrateRecord().get(0))
//                        );
//
//
//                        // DUMMY -- don't add
//                        map.addMarker(r.getMarker());
            }
        });
    }

    /**
     *
     */
    private void setupSensors() {

        // User has pre-allowed location permissions.
        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED )
        {
            sm = (SensorManager) this.getSystemService(SENSOR_SERVICE);
            heartrateSensor = sm.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        }

        // Need to prompt user to allow location permissions.
        else  {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BODY_SENSORS},
                    REQUEST_CODE_BODY_SENSORS);
        }
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
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_BODY_SENSORS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupSensors();
                } else {
                    // Permission Denied
                    Toast.makeText(this, "BODY_SENSORS permission Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
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

    @Override
    public void onSensorChanged(SensorEvent event) {

        // Determine which type of sensor changed.
        switch (event.sensor.getType()) {

            // HeartRate sensor
            case Sensor.TYPE_HEART_RATE:

                // TODO: send HeartRate data to phone.

                for (float value : event.values) {
                    Log.d("heartrate", value + "");
                    if (communicationService != null) communicationService.transmit(value);
                    break;
                }

                break;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    /**
     * Helper method that allows access to overarching Activity, which implements the Map callback methods.
     */
    private void inflateMap() {
//        mapView.getMapAsync(this);

        // Get fragment manager.
        FragmentManager fm = getFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentByTag("map_fragment");
        if (mapFragment == null) {
            GoogleMapOptions mapOptions = new GoogleMapOptions();
            mapOptions.mapType(GoogleMap.MAP_TYPE_NORMAL)
                    .compassEnabled(true)
                    .rotateGesturesEnabled(true)
                    .tiltGesturesEnabled(true);
            mapFragment = MapFragment.newInstance(mapOptions);
        }
        mapFragment.getMapAsync(this);

        // Add map to DismissOverlayView
        fm.beginTransaction().add(R.id.mapFrame, mapFragment).commit();

    }

    public void centerMapOnLocation(LatLng location) {
        this.map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, ZOOM_LEVEL));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setMyLocationButtonEnabled(false);
//        map.getUiSettings().setAllGesturesEnabled(false);
//        map.setOnMapLongClickListener(this);

        this.centerMapOnLocation(new LatLng(43.1, -87.9));
    }

}
