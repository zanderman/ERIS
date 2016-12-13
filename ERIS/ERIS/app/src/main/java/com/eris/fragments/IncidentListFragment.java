package com.eris.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.eris.R;
import com.eris.activities.MainActivity;
import com.eris.adapters.IncidentListAdapter;
import com.eris.classes.Incident;
import com.eris.classes.Responder;
import com.eris.services.DatabaseService;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 */
public class IncidentListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    /*
     * Private Members
     */
    private static final String TAG = IncidentListFragment.class.getSimpleName();

    public static final String GET_ALL_INCIDENTS = TAG + ".get_all_incidents";


    private ListView incidentListView;
    private IncidentListAdapter incidentListAdapter;
    private Responder currentUser;
    private BroadcastReceiver receiver;
    private IntentFilter filter;
    private SwipeRefreshLayout swipeRefreshLayout;



    public IncidentListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        filter = new IntentFilter();
        filter.addAction(DatabaseService.DATABASE_SERVICE_ACTION);
        this.receiver = new IncidentListFragmentReceiver();
        this.getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public void onRefresh() {
        //This actually needs to be a handler or whatever TODO TODO.
        ((MainActivity) getActivity()).databaseService.getAllIncidents(GET_ALL_INCIDENTS);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Obtain access to the root layout view.
        View root = inflater.inflate(R.layout.fragment_incident_list, container, false);

        // Obtain reference to the ListView for incidents.
        incidentListView = (ListView) root.findViewById(R.id.incident_list_view);
        incidentListView.setDivider(null);
        incidentListView.setDividerHeight(0);

        swipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.incident_list_swipe_refresh);
        Log.d(TAG, "layout: " + swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.md_red_600,
                R.color.md_indigo_A400,
                R.color.md_green_500);
        // Inflate the modified layout for this fragment.
        return root;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Create new adapter object.
        incidentListAdapter = new IncidentListAdapter(getActivity());

        // Set the adapter for the ListView of incidents.
        incidentListView.setAdapter(incidentListAdapter);

        // Set OnItemClickListener for items within the ListView.
        incidentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                // Get the incident that was clicked.
                final Incident incident = incidentListAdapter.getItem(i);

                // Create new bundle for fragment arguments.
                Bundle bundle = new Bundle();
                bundle.putSerializable("incident",incident); // Place incident into the bundle.

                // Obtain reference to fragment manager.
                final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

                // Create a new instance of the fragment.
                IncidentInfoFragment infoFragment = new IncidentInfoFragment();
                infoFragment.setArguments(bundle); //Pass the incident to the fragment.

                // Display the fragment.
                fragmentManager.beginTransaction()
                        .replace(R.id.main_fragment_container, infoFragment, IncidentInfoFragment.class.getSimpleName())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();

                // Set the title for the fragment.
                ((MainActivity) getActivity()).setActionBarTitle("Incident Info");
            }
        });

        //Find the correct incidents.
        (new Thread(new OnDatabaseStartedThread())).start();
        //TODO remove this and update.
        // Add some items to the adapter.
        //incidentListAdapter.add(new Incident("1234", "Building on fire. Help needed!", "7777 Main Ave.", "37.2286649", "-80.4190468", "00:00", "Structure Fire", new ArrayList<String>(Arrays.asList("Police","Fire", "EMS"))));
    }

    class OnDatabaseStartedThread implements Runnable {
        public OnDatabaseStartedThread() {
            //Required constructor
        }

        @Override
        public void run() {
            try {
                ((MainActivity)getActivity()).databaseServiceLatch.await();
                ((MainActivity)getActivity()).databaseService.currentUserLatch.await();
            } catch (InterruptedException e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
            currentUser = ((MainActivity) getActivity()).databaseService.getCurrentUser();
            (new Thread(new RefreshIncidentsThread())).start();
        }
    }

    class RefreshIncidentsThread implements Runnable {
        public RefreshIncidentsThread() {
            //Required constructor
        }

        @Override
        public void run() {
            try {
                ((MainActivity)getActivity()).databaseServiceLatch.await();
            } catch (InterruptedException e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
            ((MainActivity) getActivity()).databaseService.getAllIncidents(GET_ALL_INCIDENTS);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister location updates broadcast receiver.
        //this.getActivity().unregisterReceiver(receiver); TODO reenable this.
    }


    public class IncidentListFragmentReceiver extends BroadcastReceiver {

        public IncidentListFragmentReceiver() {
            //Idk if things should be here.
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            String callingMethodIdentifier = intent.getStringExtra(DatabaseService.CALLING_METHOD_IDENTIFIER);

            if (callingMethodIdentifier.equals(GET_ALL_INCIDENTS)) {

                Parcelable parcelables[] = intent.getParcelableArrayExtra(DatabaseService.DATA);

                Incident incidents[] = new Incident[parcelables.length];
                for(int i = 0; i < parcelables.length; i++) {
                    incidents[i] = (Incident) parcelables[i];
                }

                //TODO sort incidents by time

                incidentListAdapter.clear();
                for (int i = 0; i < incidents.length; i++) {
                    if (incidents[i].getOrganizations().contains(currentUser.getOrganization())) {
                        incidentListAdapter.add(incidents[i]);
                    }
                }
                swipeRefreshLayout.setRefreshing(false);

            } else {
                //Do nothing
            }
        }
    }
}
