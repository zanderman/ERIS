package com.eris.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;

import com.eris.R;

/**
 * Created by Will Schrag on 10/16/2016.
 */

public class DemoIncidentDatabaseFragment extends Fragment{

    public DemoIncidentDatabaseFragment() {
        //Required?
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //TODO this is the wrong layout.
        return inflater.inflate(R.layout.fragment_demo_responder_database, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
