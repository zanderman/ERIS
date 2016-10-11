package com.eris.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.eris.R;

public class HomeFragment extends Fragment {

    /*
     * Private Members
     */
    private Button buttonDemoLocation;
    private Button buttonDemoScene;

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
        buttonDemoScene = (Button) root.findViewById(R.id.buttonDemoScene);

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
            }
        });

        // Instantiate the scene demo.
        buttonDemoScene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                final Fragment fragment = Fragment.instantiate(getActivity(),DemoSceneFragment.class.getName());
                fragmentManager.beginTransaction()
                        .replace(R.id.main_fragment_container, fragment,DemoSceneFragment.class.getSimpleName())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
            }
        });
    }
}
