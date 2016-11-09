package com.eris.fragments;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.eris.R;
import com.eris.activities.MainActivity;
import com.eris.adapters.ResponderListAdapter;
import com.eris.classes.Incident;
import com.eris.classes.Responder;
import com.eris.services.DatabaseService;
import com.eris.services.LocationService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class IncidentInfoFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    /*
     * Constants
     */
    private static final String TAG = IncidentInfoFragment.class.getSimpleName();
    private static final float ZOOM_LEVEL = 17f;
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
    private LinearLayout infoContainer, hierarchyContainer, cardVisibilityContainer, listVisibilityContainer;
    private ImageView cardVisibilityImage, listVisibilityImage;
    private RelativeLayout mapContainer;
    private FloatingActionButton incidentFloatingActionButton,
            checkinFloatingActionButton,
            informationFloatingActionButton;
    private GoogleMap googleMap;
    private BroadcastReceiver receiver;
    private IntentFilter receiverFilter;
    private Thread incidentResponderUpdateThread;
    private boolean responderListUpdateFlag;
    private String respondersByIncidentRequestMethodIdentifier;
    private Incident incident;
    private Responder currentUser;
    private ListView responderListView, subordinateListView, superiorListView;
    private ResponderListAdapter responderAdapter, subordinateAdapter, superiorAdapter;
    private ArrayList<Responder> subordinates, responders, superiors;
    private HashMap<String, Marker> markers; // HashMap of Google Map markers.

    /*
     * Information Layout Members
     */
    private TextView addressTextView, descriptionTextView, runtimeTextView;


    public IncidentInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        // Obtain reference to incident object.
        incident = (Incident) args.getSerializable("incident");
        Log.d(this.getClass().getSimpleName(),"got incident: " + incident.getSceneId());
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
        superiors = new ArrayList<Responder>();

        // Setup the map markers hash map
        markers = new HashMap<String, Marker>();

        // Get the reference to the Responder object for the current user
//        // TODO 44444444 update this to use actual user pulled from database on login
//        ArrayList<String> heartrates = new ArrayList<String>();
//        heartrates.add("72.4");
//        heartrates.add("75.1");
//        heartrates.add("80.2");
//        ArrayList<String> currSubordinates = new ArrayList<String>();
//        currSubordinates.add("2");
//        currSubordinates.add("3");
//        currSubordinates.add("4");
//        currentUser = new Responder("1", "Albertson,Al", "EMS", heartrates, "11",
//                currSubordinates, "37.229601", "-80.417308", "5842", "11", currSubordinates);
        currentUser = ((MainActivity) getActivity()).databaseService.getCurrentUser();

        // Create an intent filter
        receiverFilter = new IntentFilter();
        receiverFilter.addAction(LocationService.BROADCAST_ACTION_LOCATION_UPDATE);
        receiverFilter.addAction(DatabaseService.DATABASE_SERVICE_ACTION);

        // Create broadcast receiver object.
        this.receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // Determine which broadcast was sent.
                String callingMethodIdentifier = intent.getStringExtra(DatabaseService.CALLING_METHOD_IDENTIFIER);
                if (callingMethodIdentifier != null) {
                    if (callingMethodIdentifier.equals(respondersByIncidentRequestMethodIdentifier)) {
                        Parcelable updatedResponders[] = intent.getParcelableArrayExtra(DatabaseService.DATA);
                        Log.d("receiving responders", "got response :" + updatedResponders);
                        respondToUpdatedResponderBroadcast(updatedResponders);
                    }
                } else {
                    switch ( intent.getAction() ) {

                        // Updated Location
                        case LocationService.BROADCAST_ACTION_LOCATION_UPDATE:
                            double latitude = intent.getDoubleExtra(LocationService.KEY_LOCATION_LATITUDE, 0.0);
                            double longitude = intent.getDoubleExtra(LocationService.KEY_LOCATION_LONGITUDE, 0.0);
                            LatLng location = new LatLng(latitude, longitude);
                            Log.d("location", "UPDATE: " + location);
                            break;

                        // Unhandled broadcast
                        default:
                            break;
                    }
                }

            }
        };
        //this.getActivity().registerReceiver(receiver, receiverFilter);

        // Create a thread to request updates from the database periodically
        this.responderListUpdateFlag = true;
        respondersByIncidentRequestMethodIdentifier = this.getClass().getSimpleName()
                + "broadcast_action_database_incident_responders"
                + incident.getSceneId();
        incidentResponderUpdateThread = new Thread(
                new PeriodicCallToDatabaseServiceForIncidentResponders(incident.getSceneId(),
                        respondersByIncidentRequestMethodIdentifier)
        );
        incidentResponderUpdateThread.start();
    }

    private class PeriodicCallToDatabaseServiceForIncidentResponders implements Runnable {
        String callingMethodIdentifier;
        String incidentId;

        public PeriodicCallToDatabaseServiceForIncidentResponders(String incidentId, String callingMethodIdentifier) {
            this.callingMethodIdentifier = callingMethodIdentifier;
            this.incidentId = incidentId;
        }

        @Override
        public void run() {
            DatabaseService databaseService = ((MainActivity) getActivity()).databaseService;
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                // oops
            } finally {
                databaseService.getRespondersByIncident("5842", callingMethodIdentifier);
            }

            SharedPreferences preferences = getActivity().getSharedPreferences(
                    getResources().getString(R.string.sharedpreferences_user_settings),
                    0
            );
            int waitTimeSeconds = preferences.getInt(
                    getResources().getString(R.string.preferences_broadcast), 30);
            long waitTimeMilliseconds = 1000 * ((long) waitTimeSeconds);

            while (responderListUpdateFlag) {
                try {
                    Thread.sleep(waitTimeMilliseconds);
                } catch (Exception e) {
                    // oops
                } finally {
                    databaseService.getRespondersByIncident("5842", callingMethodIdentifier);
                }
            }
        }
    }

    /**
     * Helper method used to update markers on the map and responders listed in in the
     * IncidentInfoFragment's associated view.
     *
     * @param updatedResponders
     */
    private void respondToUpdatedResponderBroadcast(Parcelable[] updatedResponders) {

        responders.clear();
        subordinates.clear();
        superiors.clear();
        for (String key : markers.keySet()) {
            markers.get(key).remove();
        }
        markers.clear();
        responderAdapter.clear();
        subordinateAdapter.clear();
        superiorAdapter.clear();

        if (currentUser == null) {
            return;
        }

        for (Parcelable parcelableResponder : updatedResponders) {
            Responder responder = (Responder) parcelableResponder;

            if (currentUser.getUserID().equals(responder.getUserID())) {
                continue;
            }

            responder.setLocation(new LatLng(Double.parseDouble(responder.getLatitude()), Double.parseDouble(responder.getLongitude())));
            if (currentUser.getIncidentSuperior().equals(responder.getUserID())) {
                superiors.add(responder);
            }
            else if (currentUser.getIncidentSubordinates().contains(responder.getUserID())) {
                subordinates.add(responder);
            }
            else {
                responders.add(responder);
            }

            BitmapDescriptor bitmapDescriptor;

            float[] hsv = new float[3];
            switch (responder.getOrganization()) {
                case "EMS":
                    // Subordinate color.
                    if (currentUser.getIncidentSubordinates().contains(responder.getUserID())) {
                        Color.colorToHSV(Color.parseColor("#9acd32"), hsv); // EMS green
                    }
                    // Superior color.
                    else if (currentUser.getIncidentSuperior().equals(responder.getUserID())) {
                        Color.colorToHSV(Color.parseColor("#00c78c"), hsv); // EMS green
                    }
                    // default color.
                    else {
                        Color.colorToHSV(getResources().getColor(R.color.md_green_600), hsv); // EMS green
                    }
                    bitmapDescriptor = BitmapDescriptorFactory.defaultMarker(hsv[0]);
                    break;
                case "POLICE":
                    // Subordinate color.
                    if (currentUser.getIncidentSubordinates().contains(responder.getUserID())) {
                        Color.colorToHSV(getResources().getColor(R.color.md_blue_400), hsv); // Police blue
                    }
                    // Superior color.
                    else if (currentUser.getIncidentSuperior().equals(responder.getUserID())) {
                        Color.colorToHSV(getResources().getColor(R.color.md_blue_900), hsv); // Police blue
                    }
                    // default color.
                    else {
                        Color.colorToHSV(getResources().getColor(R.color.md_blue_700), hsv); // Police blue
                    }
                    bitmapDescriptor = BitmapDescriptorFactory.defaultMarker(hsv[0]);
                    break;
                case "FIRE":
                    // Subordinate color.
                    if (currentUser.getIncidentSubordinates().contains(responder.getUserID())) {
                        Color.colorToHSV(Color.parseColor("#ff83fa"), hsv); // Fire red
                    }
                    // Superior color.
                    else if (currentUser.getIncidentSuperior().equals(responder.getUserID())) {
                        Color.colorToHSV(Color.parseColor("#8B1C62"), hsv); // Fire red
                    }
                    // default color.
                    else {
                        Color.colorToHSV(getResources().getColor(R.color.md_red_500), hsv); // Fire red
                    }
                    bitmapDescriptor = BitmapDescriptorFactory.defaultMarker(hsv[0]);
                    break;
                default:
                    bitmapDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
            }
            Marker marker = googleMap.addMarker(
                    new MarkerOptions()
                            .position(new LatLng(Double.parseDouble(responder.getLatitude()),
                                    Double.parseDouble(responder.getLongitude())))
                            .title(responder.getName())
                            .snippet(responder.getOrganization() + "  //  "
                                    + responder.getHeartrateRecord().get(0))
                            .icon(bitmapDescriptor)
            );
            markers.put(responder.getUserID(), marker);
        }
        responderAdapter.addAll(responders);
        responderAdapter.notifyDataSetChanged();
        subordinateAdapter.addAll(subordinates);
        subordinateAdapter.notifyDataSetChanged();
        superiorAdapter.addAll(superiors);
        superiorAdapter.notifyDataSetChanged();
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
        cardVisibilityContainer = (LinearLayout) root.findViewById(R.id.card_visibility_layout);
        listVisibilityContainer = (LinearLayout) root.findViewById(R.id.list_visibility_layout);

        // Set reference to image views.
        cardVisibilityImage = (ImageView) root.findViewById(R.id.card_visibility_image);
        listVisibilityImage = (ImageView) root.findViewById(R.id.list_visibility_image);

        // Set references to FloatingActionButtons.
        incidentFloatingActionButton = (FloatingActionButton) root.findViewById(R.id.incident_floatingActionButton);
        checkinFloatingActionButton = (FloatingActionButton) root.findViewById(R.id.checkin_floatingActionButton);
        informationFloatingActionButton = (FloatingActionButton) root.findViewById(R.id.information_floatingActionButton);

        // Set references to information display elements.
        addressTextView = (TextView) root.findViewById(R.id.info_address);
        descriptionTextView = (TextView) root.findViewById(R.id.info_description);
        runtimeTextView = (TextView) root.findViewById(R.id.info_runtime);

        // Get reference to ListView objects.
        responderListView = (ListView) root.findViewById(R.id.incident_responder_list);
        subordinateListView = (ListView) root.findViewById(R.id.incident_subordinate_list);
        superiorListView = (ListView) root.findViewById(R.id.incident_superior_list);

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
        addressTextView.setText(this.incident.getAddress());
        descriptionTextView.setText(this.incident.getDescription());
        runtimeTextView.setText("00:00");

        /*
         * Set list adapters.
         */
        responderAdapter = new ResponderListAdapter(getActivity());
        responderListView.setAdapter(responderAdapter);
        subordinateAdapter = new ResponderListAdapter(getActivity());
        subordinateListView.setAdapter(subordinateAdapter);
        superiorAdapter = new ResponderListAdapter(getActivity());
        superiorListView.setAdapter(superiorAdapter);

        /*
         * Set ListView item actions.
         */
        responderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                centerMapOnLocation( responderAdapter.getItem(i).getLocation() ); // Center map on responder location.
                markers.get(responderAdapter.getItem(i).getUserID()).showInfoWindow();
            }
        });
        subordinateListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                centerMapOnLocation( subordinateAdapter.getItem(i).getLocation() ); // Center map on responder location.
                markers.get(subordinateAdapter.getItem(i).getUserID()).showInfoWindow();
            }
        });
        superiorListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                centerMapOnLocation( superiorAdapter.getItem(i).getLocation() ); // Center map on responder location.
                markers.get(superiorAdapter.getItem(i).getUserID()).showInfoWindow();
            }
        });

        /*
         * Set FloatingActionButton actions.
         */
        incidentFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                centerMapOnLocation(incident.getLocation()); // Center the map on the incident location.
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

                // Ensure the information windows are in the same visibility state.
                if (information_flipflop != resize_flipflop) {
                    if (information_flipflop) displayInformation();
                    if (resize_flipflop) resizeMap();
                }
                else {
                    // show/hide information.
                    displayInformation();
                    resizeMap(); // Resize the Google Map.
                }
            }
        });

        // Set click action for info card.
        cardVisibilityContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayInformation();
            }
        });

        // Set click action for responder list.
        listVisibilityContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resizeMap();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unregister location updates broadcast receiver.
        this.getActivity().unregisterReceiver(receiver);
        this.responderListUpdateFlag = false;
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
            Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.move_down_list);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    listVisibilityImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_up_white_24dp));
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) hierarchyContainer.getLayoutParams();
                    int offset = hierarchyContainer.getHeight() - listVisibilityContainer.getHeight();
                    params.setMargins(0,offset,0,-1*(offset));
                    hierarchyContainer.setLayoutParams(params);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            hierarchyContainer.startAnimation(animation);
        } else {
            Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.move_up_list);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    listVisibilityImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_down_white_24dp));
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) hierarchyContainer.getLayoutParams();
                    params.setMargins(0, 0, 0, 0);
                    hierarchyContainer.setLayoutParams(params);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            hierarchyContainer.startAnimation(animation);
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
            Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.move_out_card);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    cardVisibilityImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_fast_forward_white_24dp));
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) infoContainer.getLayoutParams();
//                    int offset = (int)(infoContainer.getWidth() * 0.85);
                    int offset = (int)((infoContainer.getWidth() - cardVisibilityContainer.getWidth())*0.90);
                    params.setMargins(-1*(offset), 0, (offset), 0);
                    infoContainer.setLayoutParams(params);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            infoContainer.startAnimation(animation);
        } else {
            Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.move_in_card);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    cardVisibilityImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_fast_rewind_white_24dp));
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) infoContainer.getLayoutParams();
                    params.setMargins(0, 0, 0, 0);
                    infoContainer.setLayoutParams(params);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            infoContainer.startAnimation(animation);
        }

        // Change the flipflop value.
        information_flipflop = !information_flipflop;
    }


    /*
     * Google Map Methods
     */

    @Override
    public void onMapLongClick(LatLng latLng) {
//        // TODO 44444444 remove dummy values & update response call, only here to simulate update from database
//        ArrayList<String> heartrates2 = new ArrayList<String>();
//        ArrayList<String> heartrates3 = new ArrayList<String>();
//        ArrayList<String> heartrates4 = new ArrayList<String>();
//        ArrayList<String> heartrates5 = new ArrayList<String>();
//        ArrayList<String> heartrates6 = new ArrayList<String>();
//        ArrayList<String> heartrates7 = new ArrayList<String>();
//        ArrayList<String> heartrates8 = new ArrayList<String>();
//        ArrayList<String> heartrates9 = new ArrayList<String>();
//        ArrayList<String> heartrates10 = new ArrayList<String>();
//        ArrayList<String> heartrates11 = new ArrayList<String>();
//        heartrates2.add("86.7");
//        heartrates3.add("82.3");
//        heartrates4.add("84.1");
//        heartrates5.add("78.3");
//        heartrates6.add("74.2");
//        heartrates7.add("83.5");
//        heartrates8.add("87.1");
//        heartrates9.add("81.4");
//        heartrates10.add("79.8");
//        heartrates11.add("71.2");
//        ArrayList<Responder> respondersTestList2 = new ArrayList<Responder>();
//
//        ArrayList<String> heartrates = new ArrayList<String>();
//        heartrates.add("72.4");
//        heartrates.add("75.1");
//        heartrates.add("80.2");
//        ArrayList<String> currSubordinates = new ArrayList<String>();
//        currSubordinates.add("2");
//        currSubordinates.add("3");
//        currSubordinates.add("4");
//        currSubordinates.add("5");
//        currentUser = new Responder("1", "Albertson,Al", "EMS", heartrates, "11",
//                currSubordinates, "37.229601", "-80.417308", "5842", "11", currSubordinates);
//
//        respondersTestList2.add(currentUser);
//        respondersTestList2.add(new Responder("2", "Johnson,Johnny", "EMS", heartrates2, "1",
//                null, "37.209601", "-80.429308", "5842", "1", null));
//        respondersTestList2.add(new Responder("3", "Doe,John", "EMS", heartrates3, "1",
//                null, "37.304601", "-80.507308", "5842", "1", null));
//        respondersTestList2.add(new Responder("4", "James,Jim", "EMS", heartrates4, "1",
//                null, "37.247601", "-80.399308", "5842", "1", null));
//        respondersTestList2.add(new Responder("5", "Mathews,Robby", "EMS", heartrates5, "1",
//                null, "37.419601", "-80.319308", "5842", "1", null));
//        respondersTestList2.add(new Responder("6", "Smith,Emma", "EMS", heartrates6, "11",
//                null, "37.519601", "-80.479308", "5842", "11", null));
//
//        respondersTestList2.add(new Responder("7", "DaDubious,Greg", "EMS", heartrates7, "11",
//                null, "37.389601", "-80.389308", "5842", "11", null));
//        respondersTestList2.add(new Responder("8", "Yasar,Brian", "EMS", heartrates8, "11",
//                null, "37.429601", "-80.549308", "5842", "11", null));
//        respondersTestList2.add(new Responder("9", "McFubious,Dan", "EMS", heartrates9, "11",
//                null, "37.419601", "-80.519308", "5842", "11", null));
//        respondersTestList2.add(new Responder("10", "Watson,Ally", "EMS", heartrates10, "11",
//                null, "37.439601", "-80.509308", "5842", "11", null));
//
//        respondersTestList2.add(new Responder("11", "Dr,T", "EMS", heartrates11, null,
//                null, "37.409601", "-80.529308", "5842", null, null));
//        respondToUpdatedResponderBroadcast(respondersTestList2);
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
                        .position(incident.getLocation())
                        .title(incident.getAddress())
                        .snippet(incident.getDescription())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
        );

        /*
         * Create concentric circles around the incident.
         */
        for (int radius = 25; radius<=100; radius=radius+25) {

            // Draw proximity circle around incident.
            this.googleMap.addCircle(
                    new CircleOptions()
                            .center(incident.getLocation())
                            .radius(radius) /* meters */
                            .strokeColor(0x20000000) /* 20F44336, opaque red (500)*/
                            .fillColor(0x05000000) /* 0x55E57373, opaque red */
                            .strokeWidth(5)
            );
        }

        // Center map on incident location.
        centerMapOnLocation(incident.getLocation());

        this.getActivity().registerReceiver(receiver, receiverFilter);

//        // add dummy items.
//        ArrayList<String> heartrates2 = new ArrayList<String>();
//        ArrayList<String> heartrates3 = new ArrayList<String>();
//        ArrayList<String> heartrates4 = new ArrayList<String>();
//        ArrayList<String> heartrates5 = new ArrayList<String>();
//        ArrayList<String> heartrates6 = new ArrayList<String>();
//        ArrayList<String> heartrates7 = new ArrayList<String>();
//        ArrayList<String> heartrates8 = new ArrayList<String>();
//        ArrayList<String> heartrates9 = new ArrayList<String>();
//        ArrayList<String> heartrates10 = new ArrayList<String>();
//        ArrayList<String> heartrates11 = new ArrayList<String>();
//        heartrates2.add("86.7");
//        heartrates3.add("82.3");
//        heartrates4.add("84.1");
//        heartrates5.add("78.3");
//        heartrates6.add("74.2");
//        heartrates7.add("83.5");
//        heartrates8.add("87.1");
//        heartrates9.add("81.4");
//        heartrates10.add("79.8");
//        heartrates11.add("71.2");
//        ArrayList<Responder> respondersTestList1 = new ArrayList<Responder>();
//        respondersTestList1.add(currentUser);
//        respondersTestList1.add(new Responder("2", "Johnson,Johnny", "EMS", heartrates2, "1",
//                null, "37.209601", "-80.429308", "5842", "1", null));
//        respondersTestList1.add(new Responder("3", "Doe,John", "EMS", heartrates3, "1",
//                null, "37.304601", "-80.507308", "5842", "1", null));
//        respondersTestList1.add(new Responder("4", "James,Jim", "EMS", heartrates4, "1",
//                null, "37.247601", "-80.399308", "5842", "1", null));
//        respondersTestList1.add(new Responder("5", "Mathews,Robby", "EMS", heartrates5, "11",
//                null, "37.519601", "-80.529308", "5842", "11", null));
//        respondersTestList1.add(new Responder("6", "Smith,Emma", "EMS", heartrates6, "11",
//                null, "37.489601", "-80.499308", "5842", "11", null));
//
//        respondersTestList1.add(new Responder("7", "DaDubious,Greg", "EMS", heartrates7, "11",
//                null, "37.389601", "-80.389308", "5842", "11", null));
//        respondersTestList1.add(new Responder("8", "Yasar,Brian", "EMS", heartrates8, "11",
//                null, "37.429601", "-80.549308", "5842", "11", null));
//        respondersTestList1.add(new Responder("9", "McFubious,Dan", "EMS", heartrates9, "11",
//                null, "37.419601", "-80.519308", "5842", "11", null));
//        respondersTestList1.add(new Responder("10", "Watson,Ally", "EMS", heartrates10, "11",
//                null, "37.439601", "-80.509308", "5842", "11", null));
//
//        respondersTestList1.add(new Responder("11", "Dr,T", "EMS", heartrates11, null,
//                null, "37.409601", "-80.529308", "5842", null, null));
//        respondToUpdatedResponderBroadcast(respondersTestList1);
    }

    /**
     *
     * @param location
     */
    public void centerMapOnLocation(LatLng location) {
        this.googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, ZOOM_LEVEL));
    }
}
