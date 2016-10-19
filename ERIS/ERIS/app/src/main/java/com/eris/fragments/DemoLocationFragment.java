package com.eris.fragments;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapperConfig;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.models.nosql.UserDataDO;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.eris.R;
import com.eris.activities.MainActivity;
import com.eris.adapters.ResponderListAdapter;
import com.eris.classes.Responder;
import com.eris.services.DatabaseService;
import com.eris.services.LocationService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


public class DemoLocationFragment extends Fragment implements OnMapReadyCallback {

    /*
     * Private members
     */
    private static final int ZOOM_LEVEL = 18;
    private final int REQUEST_CODE_ENABLE_MY_LOCATION = 222;
    private BroadcastReceiver receiver;
    private DynamoDBMapper mapper;

    //DatabaseService db = (DatabaseService) getSystemService(DatabaseService.class);
    //db.getUserData("4093820716");


    /*
     * Private Members
     */
    private Button buttonAddResponder;
    private ListView responderListView;
    private ResponderListAdapter responderListAdapter;
    private GoogleMap googleMap;

    public DemoLocationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();

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
                        double latitude = intent.getDoubleExtra(LocationService.KEY_LOCATION_LATITUDE,0.0);
                        double longitude = intent.getDoubleExtra(LocationService.KEY_LOCATION_LONGITUDE,0.0);
                        LatLng location = new LatLng(latitude, longitude);
                        Log.d("location","UPDATE: " + location);
                        centerMapOnLocation(location);

                        SharedPreferences preferences = getActivity().getSharedPreferences(
                                getResources().getString(R.string.sharedpreferences_curr_user_account_info),
                                Context.MODE_PRIVATE
                        );
                        String savedUserIDToken = preferences.getString(
                                getResources().getString(R.string.sharedpreferences_entry_userID), "");

                        Log.d("blah blah", savedUserIDToken);
                        UserDataDO currUser = new UserDataDO();
                        currUser.setUserId(savedUserIDToken);
                        currUser.setLatitude(Double.toString(latitude));
                        currUser.setLongitude(Double.toString(longitude));

                        FetchTask task = new FetchTask();
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, currUser);
                        break;
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
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_demo_location, container, false);

        // Obtain access to fragment UI elements.
        buttonAddResponder = (Button) root.findViewById(R.id.buttonAddResponder);
        responderListView = (ListView) root.findViewById(R.id.responderListView);

        // Add Google Map to fragment.
        final SupportMapFragment fragment = SupportMapFragment.newInstance();
        final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.map_fragment_container,fragment);
        fragmentTransaction.commit();

        // Set this fragment as the callback center for Google Maps.
        fragment.getMapAsync(this);

        // Return the root view.
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set ListView adapter.
        responderListAdapter = new ResponderListAdapter(getActivity());
        responderListView.setAdapter(responderListAdapter);

        // Set ListView actions on item click.
        responderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Show responder location on map.
                final Responder responder = responderListAdapter.getItem(i);
                Toast.makeText(getActivity(),"Clicked: " + responder.firstName + " " + responder.lastName,Toast.LENGTH_SHORT).show();

                centerMapOnLocation(responder.location);
            }
        });

        // Set ListView actions on item long click.
        responderListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                // Remove corresponding marker.
                responderListAdapter.getItem(i).marker.remove();

                // Remove the responder from the list.
                responderListAdapter.remove(responderListAdapter.getItem(i));
                return true;
            }
        });
        // Setup button onClick method.
        buttonAddResponder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Add item to adapter.
                Responder responder = new Responder("abc123","John","Wayne");
                responder.location = new LatLng(37.229491,-80.421531);
                responder.marker = googleMap.addMarker(new MarkerOptions()
                        .position(responder.location)
                        .title(responder.firstName + " " + responder.lastName)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                responderListAdapter.add(responder);
                Toast.makeText(getActivity(),"Added: " + responder.firstName + " " + responder.lastName,Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    /**
     *
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        // Obtain reference to Google Map object.
        this.googleMap = googleMap;

        // Enable my-location
        this.enableMyLocation();
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
     *
     * @param location
     */
    public void centerMapOnLocation(LatLng location) {
        Log.d("demo","HEY: "+this.googleMap);
        this.googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, ZOOM_LEVEL));
    }

    private class FetchTask extends AsyncTask<UserDataDO, Void, Void> {

        @Override
        protected Void doInBackground(UserDataDO... users) {
            // Attempt to update the user in the database with the new latitude and longitude.
            UserDataDO user = users[0];
            try {
                mapper.save(user, new DynamoDBMapperConfig(DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES));
            } catch (final AmazonClientException ex) {
                // Restore original data if save fails, and re-throw.
                //throw ex;
            }
            return null;
        }
    }
}
