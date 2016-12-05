package io.github.zanderman.eris;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
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

public class WheelActivity extends Activity
        implements SensorEventListener,
            OnMapReadyCallback,
            GoogleMap.OnMapLongClickListener,
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener,
            DataApi.DataListener {

    private static final float ZOOM_LEVEL = 16f;
    private LinearLayout connectingLayout;
    private RelativeLayout infoLayout;
//    private TextView mTextView;
    private WheelView wheelView;
    private ArrayList<SimpleResponder> responders;
    private DismissOverlayView mDismissOverlay;
    private GestureDetector mDetector;
    private final int REQUEST_CODE_BODY_SENSORS = 1111;

    /*
     * Google API Client
     */
    private GoogleApiClient googleApiClient;
    private boolean connected = false;

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

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wheel);

        // Get intent reference that was used to create this activity.
        Intent intent = getIntent();

        // Gain access to list of responders current being used.
        responders = new ArrayList<SimpleResponder>();//intent.getParcelableArrayListExtra("responders");
        Log.d("goober","num responders: " + responders.size());

        // Setup sensors.
        setupSensors();

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {

                // Get view elements.
//                mTextView = (TextView) stub.findViewById(R.id.text);
                mapFrame = (FrameLayout) stub.findViewById(R.id.mapFrame);
                connectingLayout = (LinearLayout) stub.findViewById(R.id.waitLayout);
                infoLayout = (RelativeLayout) stub.findViewById(R.id.infoLayout);
                wheelView = (WheelView) stub.findViewById(R.id.wheelview);

                // Obtain the DismissOverlayView element
                mDismissOverlay = (DismissOverlayView) stub.findViewById(R.id.dismiss_overlay);
                mDismissOverlay.setIntroText("Long press to exit app");
                mDismissOverlay.showIntroIfNecessary();

                if (!connected) {
                    infoLayout.setVisibility(View.GONE);
                    connectingLayout.setVisibility(View.VISIBLE);

                    // Setup Google API Client.
                    buildGoogleApiClient();
                    connectGoogleApiClient();
                }
                else {
                    infoLayout.setVisibility(View.VISIBLE);
                    connectingLayout.setVisibility(View.GONE);

                    // Setup dismissal overlay.
                    setupGestureDetectors();

                    // Create Google Map
                    inflateMap();

                    // Creation of WheelView
                    setupWheel();
                }

            }
        });
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
                Toast.makeText(WheelActivity.this, "clicked: " + r.name, Toast.LENGTH_SHORT).show();


                // TODO: clicking on item opens Google Map to where the responder is located (possibly add distance to that responder)
            }
        });
        wheelView.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectListener() {
            @Override
            public void onWheelItemSelected(WheelView parent, Drawable itemDrawable, int position) {
                SimpleResponder r = responders.get(position);
                Toast.makeText(WheelActivity.this, "selected: " + r.name, Toast.LENGTH_SHORT).show();

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
            System.out.println("Has HR: " + heartrateSensor);
            sm.registerListener(this, heartrateSensor, SensorManager.SENSOR_DELAY_NORMAL);
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

                Log.d("heartrate", "------------");
                for (float value : event.values) {
                    Log.d("heartrate", value + "");
                }
                Log.d("heartrate", "------------");
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
        map.getUiSettings().setAllGesturesEnabled(false);
        map.setOnMapLongClickListener(this);

        this.centerMapOnLocation(new LatLng(43.1, -87.9));
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        mDismissOverlay.show();
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
        infoLayout.setVisibility(View.VISIBLE);
        connectingLayout.setVisibility(View.GONE);
        Wearable.DataApi.addListener(googleApiClient, this); // this is how we'll communicate with the phone.

        // Setup dismissal overlay.
        setupGestureDetectors();

        // Create Google Map
        inflateMap();

        // DUMMY add to wheel
        for (int i = 1; i <= 3; i++) {
            responders.add(new SimpleResponder("" + i, "" + (char)(i + 96), new LatLng(-5678.34/i, 123.45), (float)(80.5 + 3*i)));
        }

        // TODO: must call setupWheel() after updating the responder list.


        // Creation of WheelView
        setupWheel();

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
    public class SendDataTask extends AsyncTask<Float, Void, Void> {

        @Override
        protected Void doInBackground(Float... params) {

            PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/heartrate");

            Log.d("hudwear", "params.length: " + params.length);
            putDataMapReq.getDataMap().putFloat("heartrate", params[0]);

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
                if (item.getUri().getPath().compareTo("/responderlist") == 0) {
                    DataMap dm = DataMapItem.fromDataItem(item).getDataMap();

                    Message m = new Message();
                    Bundle b = new Bundle();
                    b.putStringArrayList("responderlist", (ArrayList<String>) dm.get("responderlist"));
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
    Handler handler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            ArrayList<String> elements = msg.getData().getStringArrayList("responderlist");


            /*
             * Parse each string into a new SimpleResponder object.
             */
            for (String e : elements) {
                String[] tokens = e.split(",");
                SimpleResponder r = new SimpleResponder(tokens[0], tokens[1], new LatLng(Double.parseDouble(tokens[2]),Double.parseDouble(tokens[3])),Float.parseFloat(tokens[4]));

                // Add responder to list if we can.
                if (!responders.contains(r)) {
                    responders.add(r);
                }

                // Update responder entry corresponding to 'r'.
                else {
                    responders.set(responders.indexOf(r), r);
                }
            }

            // Creation of WheelView
            setupWheel();
        }
    };

//    public class CustomWheelAdapter extends WheelArrayAdapter {
//        private
//        public CustomWheelAdapter(List items) {
//            super(items);
//        }
//
//        @Override
//        public Drawable getDrawable(int position) {
//            TextDrawable nameTD = new TextDrawable(responders.get(position).name);
//            TextDrawable heartrateTD = new TextDrawable(responders.get(position).heartRate + "");
//            Drawable background = getResources().getDrawable(R.drawable.circle);
//            background.setBounds(0,0,40,40); // Make background circle 40x40
//            Drawable[] layers = {background, nameTD, heartrateTD};
//            LayerDrawable ld = new LayerDrawable(layers);
//            ld.setLayerInset(0, 0,0,0,0); // background
//            ld.setLayerInset(1, 0,20,0,20); // name
//            ld.setLayerInset(2, 0,35,0,5); // heartrate
//            return ld;
//        }
//
//        @Override
//        public int getCount() {
//            return responders.size();
//        }
//
//        @Override
//        public Object getItem(int position) {
//            return responders.get(position);
//        }
//    }

}
