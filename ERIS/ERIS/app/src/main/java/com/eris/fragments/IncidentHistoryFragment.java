package com.eris.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.eris.R;
import com.eris.activities.MainActivity;
import com.eris.adapters.IncidentListAdapter;
import com.eris.classes.Incident;
import com.eris.classes.Responder;
import com.eris.services.DatabaseService;
import com.google.android.gms.maps.model.LatLng;

import org.joda.time.Instant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.amazonaws.mobile.util.ThreadUtils.runOnUiThread;

/**
 * A simple {@link Fragment} subclass.
 */
public class IncidentHistoryFragment extends Fragment {

    public static final String TAG = IncidentHistoryFragment.class.getSimpleName();
    public static final String GET_INCIDENT_NUMBERED = TAG + ".get_numbered_incident";


    /*
     * Private Members
     */
    private ListView incidentListView;
    private IncidentListAdapter incidentHistoryAdapter;
    private Responder currentUser;
    private int historyLength;
    private Incident[] historyStorage;
    private int itemsRecieved;

    public BroadcastReceiver receiver;
    private IntentFilter filter;


    public IncidentHistoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Obtain access to the root layout view.
        View root = inflater.inflate(R.layout.fragment_incident_history, container, false);

        // Obtain reference to the ListView for incident history.
        incidentListView = (ListView) root.findViewById(R.id.incident_history_view);
        incidentListView.setDivider(null);
        incidentListView.setDividerHeight(0);


        filter = new IntentFilter();
        filter.addAction(DatabaseService.DATABASE_SERVICE_ACTION);
        this.receiver = new IncidentHistoryFragmentReceiver();
        this.getActivity().registerReceiver(receiver, filter);

        // Inflate the modified layout for this fragment.
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = ((MainActivity) getActivity()).databaseService.getCurrentUser();

        // Create new adapter object.
        incidentHistoryAdapter = new IncidentListAdapter(getActivity());

        // Set the adapter for the ListView of incidents.
        incidentListView.setAdapter(incidentHistoryAdapter);

        // Set OnItemClickListener for items within the ListView.
        incidentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                // Get the incident that was clicked.
                final Incident incident = incidentHistoryAdapter.getItem(i);

                // Toast the ID.
                Toast.makeText(getActivity(),"showing history for incident " + incident.getSceneId(), Toast.LENGTH_SHORT).show();

//                // Create new bundle for fragment arguments.
//                Bundle bundle = new Bundle();
//                bundle.putSerializable("incident",incident); // Place incident into the bundle.
//
//                // Obtain reference to fragment manager.
//                final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//
//                // Create a new instance of the fragment.
//                IncidentInfoFragment infoFragment = new IncidentInfoFragment();
//                infoFragment.setArguments(bundle); // Pass the incident to the fragment.
//
//                // Display the fragment.
//                fragmentManager.beginTransaction()
//                        .replace(R.id.main_fragment_container, infoFragment, IncidentInfoFragment.class.getSimpleName())
//                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//                        .commit();
//
//                // Set the title for the fragment.
//                ((MainActivity) getActivity()).setActionBarTitle("Incident Info");
            }
        });

        // Add some items to the adapter.
        //TODO Ok we need to change this but I need more info on whats going on.
        //incidentHistoryAdapter.add(new Incident("1234", "Fire at the aquarium.\nSave Dori!", "42 Wallaby Way, Sydney", "37.2286649", "-80.4190468", "13:00", "Structure Fire", new ArrayList<String>(Arrays.asList("Fire","EMS"))));
        //incidentHistoryAdapter.add(new Incident("5678", "Flooding on highway by Virginia Tech front gate.", "i-460 @ Virginia Tech front gate", "37.2286", "-80.4190", "06:30", "Flooding", new ArrayList<String>(Arrays.asList("Police","Fire"))));
        updateIncidents();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unregister location updates broadcast receiver.
        this.getActivity().unregisterReceiver(receiver);
    }

    private void updateIncidents() {
        (new Thread(new RefreshIncidentHistoryThread())).start();
    }

    class RefreshIncidentHistoryThread implements Runnable {
        public RefreshIncidentHistoryThread() {
            //Required constructor
        }

        @Override
        public void run() {
            //TODO wait for current user to definitly load.
            List<String> userIncidentHistory = currentUser.getIncidentHistory();
            if (userIncidentHistory.size() > 0) {
                historyLength = userIncidentHistory.size();
                historyStorage = new Incident[historyLength];
                itemsRecieved = 0;
                for (int i = 0; i < historyLength; i++) {
                    String[] elems = currentUser.getIncidentHistory().get(i).split(":");
                    Log.e(TAG, "Size" + historyLength);
                    Log.e(TAG, "Sending element request " + elems[0]);
                    Log.e(TAG, "Sending element request p2 " + elems[1]);
                    ((MainActivity) getActivity()).databaseService.getIncidentData(elems[0],
                            GET_INCIDENT_NUMBERED + ":" + i + ":" + elems[1]);
                }
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        incidentHistoryAdapter.add(new Incident("___", "None", "None", "0", "0", "00:00", "", new ArrayList<String>(Arrays.asList("None"))));
                    }
                });
            }
        }
    }


    //broadcast reciever method, creates an array and fills it.
    //For now, just get the first one.
    public class IncidentHistoryFragmentReceiver extends BroadcastReceiver {

        public IncidentHistoryFragmentReceiver() {
            //Idk if things should be here.
        }

        public void onReceive(Context context, Intent intent) {
            String[] data = intent.getStringExtra(DatabaseService.CALLING_METHOD_IDENTIFIER).split(":");
            for(int i = 0; i < data.length; i++) {
                Log.e(TAG, "Data ? " + data[i]);
            }
            if (data.length < 3) {
                Log.d(TAG, "data was too short. was " + data.toString());
                return;
            }
            //Substring on length.
            String callingMethodIdentifier = data[0];
            int itemIndex = Integer.parseInt(data[1]);
            String itemTime = data[2];

            if (callingMethodIdentifier.equals(GET_INCIDENT_NUMBERED)) {
                Incident i = intent.getParcelableExtra(DatabaseService.DATA);
                historyStorage[itemIndex] = i;
                itemsRecieved++;
                if(itemsRecieved >= historyLength) {
                    for (int k = 0; k < historyLength; k++) {
                        incidentHistoryAdapter.add(historyStorage[k]);
                    }
                }
            }
        }
    }
}
