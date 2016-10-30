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
import android.test.suitebuilder.TestMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.eris.R;
import com.eris.adapters.ResponderListAdapter;
import com.eris.classes.Incident;
import com.eris.classes.Responder;
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

import java.util.ArrayList;

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
    private boolean information_flipflop;

    /*
     * Private Members
     */
    private LinearLayout infoContainer, hierarchyContainer;
    private RelativeLayout mapContainer;
    private FloatingActionButton hierarchyFloatingActionButton,
            incidentFloatingActionButton,
            checkinFloatingActionButton,
            informationFloatingActionButton;
    private GoogleMap googleMap;
    private BroadcastReceiver receiver;
    private Incident incident;
    private ListView responderListView, subordinateListView;
    private ResponderListAdapter responderAdapter, subordinateAdapter;
    private ArrayList<Responder> subordinates, responders;

    /*
     * Information Layout Members
     */
    private TextView addressTextView, descriptionTextView, runtimeTextView, statusTextView;


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
        information_flipflop = true;

        // Setup responder arrays.
        subordinates = new ArrayList<Responder>();
        responders= new ArrayList<Responder>();


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
        hierarchyContainer = (LinearLayout) root.findViewById(R.id.incident_hierarchy_layout);
        mapContainer = (RelativeLayout) root.findViewById(R.id.incident_map_layout);

        // Set references to FloatingActionButtons.
        hierarchyFloatingActionButton = (FloatingActionButton) root.findViewById(R.id.hierarchy_floatingActionButton);
        incidentFloatingActionButton = (FloatingActionButton) root.findViewById(R.id.incident_floatingActionButton);
        checkinFloatingActionButton = (FloatingActionButton) root.findViewById(R.id.checkin_floatingActionButton);
        informationFloatingActionButton = (FloatingActionButton) root.findViewById(R.id.information_floatingActionButton);

        // Set references to information display elements.
        addressTextView = (TextView) root.findViewById(R.id.info_address);
        descriptionTextView = (TextView) root.findViewById(R.id.info_description);
        runtimeTextView = (TextView) root.findViewById(R.id.info_runtime);
        statusTextView = (TextView) root.findViewById(R.id.info_status);

        // Get reference to ListView objects.
        responderListView = (ListView) root.findViewById(R.id.incident_responder_list);
        subordinateListView = (ListView) root.findViewById(R.id.incident_subordinate_list);

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
         * Display incident information.
         */
        addressTextView.setText(this.incident.address);
        descriptionTextView.setText(this.incident.description);
        runtimeTextView.setText(5 + " minutes");
        statusTextView.setText("In Progress");
        statusTextView.setTextColor(getResources().getColor(R.color.md_red_800));

        /*
         * Set list adapters.
         */
        responderAdapter = new ResponderListAdapter(getActivity());
        responderListView.setAdapter(responderAdapter);
        subordinateAdapter = new ResponderListAdapter(getActivity());
        subordinateListView.setAdapter(subordinateAdapter);

        // add dummy items.
        responders.add(new Responder("0","John", "Smith","0",(float) 77.7, null, null));
        responders.add(new Responder("0","Johnny", "Johnson","0",(float) 86.7, null, null));
        responders.add(new Responder("0","John", "Smith","0",(float) 77.7, null, null));
        responders.add(new Responder("0","Johnny", "Johnson","0",(float) 86.7, null, null));
        responders.add(new Responder("0","John", "Smith","0",(float) 77.7, null, null));
        responders.add(new Responder("0","Johnny", "Johnson","0",(float) 86.7, null, null));
        responderAdapter.addAll(responders);

        subordinates.add(new Responder("0","Greg", "DaDubious","0",(float) 57.7, null, null));
        subordinates.add(new Responder("0","Brian", "Yasar","0",(float) 67.7, null, null));
        subordinates.add(new Responder("0","Greg", "DaDubious","0",(float) 57.7, null, null));
        subordinates.add(new Responder("0","Brian", "Yasar","0",(float) 67.7, null, null));
        subordinateAdapter.addAll(subordinates);

        /*
         * Set FloatingActionButton actions.
         */
        hierarchyFloatingActionButton.setOnClickListener(new View.OnClickListener() {
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

        informationFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // show/hide information card.
                displayInformation();
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
//            infoContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(),R.anim.fade_out));
            hierarchyContainer.setVisibility(View.GONE);
            mapContainer.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                    )
            );

        } else {
//            infoContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(),R.anim.fade_in));
            mapContainer.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            0,
                            (float) 0.7
                    )
            );
            infoContainer.setVisibility(View.VISIBLE);

            // Repopulate the lists with all stored responders.
            responderAdapter.addAll(responders);
            subordinateAdapter.addAll(subordinates);
        }

        // Change the flipflop value.
        resize_flipflop = !resize_flipflop;
    }

    /**
     * Helper method to show/hide information card over top of map.
     */
    private void displayInformation() {

        // Change parameters based on flipflop.
        if (information_flipflop) {
            infoContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(),R.anim.fade_out));
            infoContainer.setVisibility(View.INVISIBLE);
        } else {
            infoContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(),R.anim.fade_in));
            infoContainer.setVisibility(View.VISIBLE);
        }

        // Change the flipflop value.
        information_flipflop = !information_flipflop;
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

        // Center map on incident location.
        centerMapOnLocation(incident.location);
    }

    /**
     *
     * @param location
     */
    public void centerMapOnLocation(LatLng location) {
        this.googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, ZOOM_LEVEL));
    }
}
