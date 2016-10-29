package com.eris.fragments;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.eris.R;
import com.eris.classes.Incident;
import com.eris.services.LocationService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * A simple {@link Fragment} subclass.
 */
public class IncidentInfoFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    /*
     * Constants
     */
    private static final float ZOOM_LEVEL = 17.5f;
    private final int REQUEST_CODE_ENABLE_MY_LOCATION = 222;

    /*
     * Flags
     */
    private boolean resize_flipflop;
    private boolean checkin_flipflop;

    /*
     * Private Members
     */
    private LinearLayout infoContainer;
    private FloatingActionButton resizeFloatingActionButton,
            incidentFloatingActionButton,
            checkinFloatingActionButton;
    private RelativeLayout mapRelativeLayout;
    private GoogleMap googleMap;
    private BroadcastReceiver receiver;
    private Incident incident;


    public IncidentInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        // Obtain reference to incident object.
        incident = (Incident) args.getSerializable("incident");
        Log.d(this.getClass().getSimpleName(),"got incident: " + incident.id);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the flipflops.
        resize_flipflop = true;
        checkin_flipflop = false;

        // Create an intent filter
        IntentFilter filter = new IntentFilter();
        filter.addAction(LocationService.BROADCAST_ACTION_LOCATION_UPDATE);

        // Create broadcast receiver object.
        this.receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // Determine which broadcast was sent.
                switch ( intent.getAction() ) {

                    // Updated Location
                    case LocationService.BROADCAST_ACTION_LOCATION_UPDATE:
                        double latitude = intent.getDoubleExtra(LocationService.KEY_LOCATION_LATITUDE, 0.0);
                        double longitude = intent.getDoubleExtra(LocationService.KEY_LOCATION_LONGITUDE, 0.0);
                        LatLng location = new LatLng(latitude, longitude);
                        Log.d("location", "UPDATE: " + location);
                        break;

                    // Unhandled broadcast.
                    default:
                        break;
                }
            }
        };
        this.getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Obtain access to the root layout.
        View root = inflater.inflate(R.layout.fragment_incident_info, container, false);

        // Set references to FrameLayouts.
        infoContainer = (LinearLayout) root.findViewById(R.id.incident_info_container);
        mapRelativeLayout = (RelativeLayout) root.findViewById(R.id.incident_map_layout);

        // Set references to FloatingActionButtons.
        resizeFloatingActionButton = (FloatingActionButton) root.findViewById(R.id.resize_floatingActionButton);
        incidentFloatingActionButton = (FloatingActionButton) root.findViewById(R.id.incident_floatingActionButton);
        checkinFloatingActionButton = (FloatingActionButton) root.findViewById(R.id.checkin_floatingActionButton);

        // Add Google Map to fragment.
        final SupportMapFragment fragment = SupportMapFragment.newInstance();
        final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.incident_map_fragment_container,fragment);
        fragmentTransaction.commit();

        // Set this fragment as the callback center for Google Maps.
        fragment.getMapAsync(this);

        // Inflate the modified layout for this fragment.
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /*
         * Set FloatingActionButton actions.
         */

        resizeFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resizeMap(); // Resize the Google Map.
            }
        });

        incidentFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                centerMapOnLocation(incident.location); // Center the map on the incident location.
            }
        });

        checkinFloatingActionButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                /*
                 * User is not currently checked-in.
                 * So check them in.
                 */
                if (!checkin_flipflop) {
                    Toast.makeText(getActivity(), "checked-in", Toast.LENGTH_SHORT).show();
                    checkinFloatingActionButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_cancel_white_24dp));
                } else {
                    Toast.makeText(getActivity(), "checked-out", Toast.LENGTH_SHORT).show();
                    checkinFloatingActionButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_done_white_24dp));
                }

                // Alternate the flipflop value.
                checkin_flipflop = !checkin_flipflop;

                // The callback has consumed the long click.
                return true;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unregister location updates broadcast receiver.
        this.getActivity().unregisterReceiver(receiver);
    }

    /**
     *
     */
    private void enableMyLocation() {

        // User has pre-allowed location permissions.
        if ( ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED )
        {
            // Turn my-location ON.
            this.googleMap.setMyLocationEnabled(true);
        }

        // Need to prompt user to allow location permissions.
        else  {
            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_ENABLE_MY_LOCATION);
        }
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
            case REQUEST_CODE_ENABLE_MY_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation();
                } else {
                    // Permission Denied
                    Toast.makeText(getActivity(), "ENABLE_MY_LOCATION Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    /**
     * Helper method to resize the Google Map.
     */
    private void resizeMap() {

        // Change parameters based on flipflop.
        if (resize_flipflop) {
            infoContainer.setVisibility(View.GONE);
            mapRelativeLayout.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                    )
            );
        } else {
            mapRelativeLayout.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            0,
                            (float)0.6
                    )
            );
            infoContainer.setVisibility(View.VISIBLE);
        }

        // Change the flipflop value.
        resize_flipflop = !resize_flipflop;

        Toast.makeText(getActivity(), "resized", Toast.LENGTH_SHORT).show();
    }


    /*
     * Google Map Methods
     */

    @Override
    public void onMapLongClick(LatLng latLng) {
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        // Obtain reference to Google Map object.
        this.googleMap = googleMap;
        this.googleMap.setOnMapLongClickListener(this);

        // Enable my-location
        this.enableMyLocation();

        // Drop marker on incident location.
        this.googleMap.addMarker(
                new MarkerOptions()
                        .position(incident.location)
                        .title(incident.address)
                        .snippet(incident.description)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        );

        /*
         * Create concentric circles around the incident.
         */
        for (int radius = 25; radius<=100; radius=radius+25) {

            // Draw proximity circle around incident.
            this.googleMap.addCircle(
                    new CircleOptions()
                            .center(incident.location)
                            .radius(radius) /* meters */
                            .strokeColor(0x20000000) /* 20F44336, opaque red (500)*/
                            .fillColor(0x05000000) /* 0x55E57373, opaque red */
                            .strokeWidth(5)
            );
        }
    }

    /**
     *
     * @param location
     */
    public void centerMapOnLocation(LatLng location) {
        this.googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, ZOOM_LEVEL));
    }
}
