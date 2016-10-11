package com.eris.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.eris.R;
import com.eris.adapters.ResponderListAdapter;
import com.eris.classes.Responder;

///**
// * A simple {@link Fragment} subclass.
// * Activities that contain this fragment must implement the
// * {@link DemoLocationFragment.OnFragmentInteractionListener} interface
// * to handle interaction events.
// * Use the {@link DemoLocationFragment#newInstance} factory method to
// * create an instance of this fragment.
// */
public class DemoLocationFragment extends Fragment {

    /*
     * Private Members
     */
    private Button buttonAddResponder;
    private ListView responderListView;
    private ResponderListAdapter responderListAdapter;

    public DemoLocationFragment() {
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
        View root = inflater.inflate(R.layout.fragment_demo_location, container, false);

        // Obtain access to fragment UI elements.
        buttonAddResponder = (Button) root.findViewById(R.id.buttonAddResponder);
        responderListView = (ListView) root.findViewById(R.id.responderListView);

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
            }
        });

        // Set ListView actions on item long click.
        responderListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
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
                final Responder responder = new Responder("abc123","John","Wayne");
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
}
