package com.eris.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import com.google.android.gms.maps.model.LatLng;

/**
 * A simple {@link Fragment} subclass.
 */
public class IncidentHistoryFragment extends Fragment {

    /*
     * Private Members
     */
    private ListView incidentListView;
    private IncidentListAdapter incidentListAdapter;


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

                // Toast the ID.
                Toast.makeText(getActivity(),"ID: " + incident.id, Toast.LENGTH_SHORT).show();

                // Create new bundle for fragment arguments.
                Bundle bundle = new Bundle();
                bundle.putSerializable("incident",incident); // Place incident into the bundle.

                // Obtain reference to fragment manager.
                final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

                // Create a new instance of the fragment.
                IncidentInfoFragment infoFragment = new IncidentInfoFragment();
                infoFragment.setArguments(bundle); // Pass the incident to the fragment.

                // Display the fragment.
                fragmentManager.beginTransaction()
                        .replace(R.id.main_fragment_container, infoFragment, IncidentInfoFragment.class.getSimpleName())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();

                // Set the title for the fragment.
                ((MainActivity) getActivity()).setActionBarTitle("Incident Info");
            }
        });

        // Add some items to the adapter.
        incidentListAdapter.add(new Incident("1234","Structure Fire","42 Wallaby Way, Sydney",new LatLng(37.2286649,-80.4190468)));
        incidentListAdapter.add(new Incident("5678","Flooding on highway","i-460 at Virginia Tech front gate",new LatLng(37.2286,-80.4190)));
    }

}
