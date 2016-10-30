package com.eris.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.eris.R;
import com.eris.activities.MainActivity;

public class HomeFragment extends Fragment {

    /*
     * Private Members
     */
    private Button buttonDemoLocation;
    private Button buttonResponderDatabase;
    private Button buttonDemoResponderDatabase;
    private Button buttonDemoSceneDatabase;
    private Button buttonIncidentList;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize buttons.
        buttonDemoLocation = (Button) root.findViewById(R.id.buttonDemoLocation);
        buttonResponderDatabase = (Button) root.findViewById(R.id.buttonResponderDatabase);
        buttonDemoResponderDatabase = (Button) root.findViewById(R.id.buttonDemoResponderDatabase);
        buttonDemoSceneDatabase = (Button) root.findViewById(R.id.buttonDemoSceneDatabase);
        buttonIncidentList = (Button) root.findViewById(R.id.button_incident_list);

        // Return the root view.
        return root;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Instantiate the location demo.
        buttonDemoLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                final Fragment fragment = Fragment.instantiate(getActivity(),DemoLocationFragment.class.getName());
                fragmentManager.beginTransaction()
                        .replace(R.id.main_fragment_container, fragment,DemoLocationFragment.class.getSimpleName())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();

                // Set the title for the fragment.
                ((MainActivity) getActivity()).setActionBarTitle("Demo: Location");
            }
        });

        // Instantiate the scene demo.
        buttonResponderDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                final Fragment fragment = Fragment.instantiate(getActivity(),IncidentDatabaseFragment.class.getName());
                fragmentManager.beginTransaction()
                        .replace(R.id.main_fragment_container, fragment,IncidentDatabaseFragment.class.getSimpleName())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();

                // Set the title for the fragment.
                ((MainActivity) getActivity()).setActionBarTitle("Demo: Scene");
            }
        });

        // Instantiate the responder database demo.
        buttonDemoResponderDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                final Fragment fragment = Fragment.instantiate(getActivity(),DemoResponderDatabaseFragment.class.getName());
                fragmentManager.beginTransaction()
                        .replace(R.id.main_fragment_container, fragment,DemoResponderDatabaseFragment.class.getSimpleName())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();

                // Set the title for the fragment.
                ((MainActivity) getActivity()).setActionBarTitle("Demo: Responder Database");
            }
        });     

        // Instantiate the scene database demo.
        buttonDemoSceneDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                final Fragment fragment = Fragment.instantiate(getActivity(),DemoIncidentDatabaseFragment.class.getName());
                fragmentManager.beginTransaction()
                        .replace(R.id.main_fragment_container, fragment,DemoIncidentDatabaseFragment.class.getSimpleName())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();

                // Set the title for the fragment.
                ((MainActivity) getActivity()).setActionBarTitle("Demo: Incident Database");
            }
        });

        buttonIncidentList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                final Fragment fragment = Fragment.instantiate(getActivity(),IncidentListFragment.class.getName());
                fragmentManager.beginTransaction()
                        .replace(R.id.main_fragment_container, fragment,IncidentListFragment.class.getSimpleName())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();

                // Set the title for the fragment.
                ((MainActivity) getActivity()).setActionBarTitle("Incident List");
            }
        });
    }
}
