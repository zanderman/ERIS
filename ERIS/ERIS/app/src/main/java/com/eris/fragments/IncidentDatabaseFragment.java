package com.eris.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.eris.R;
import com.eris.activities.MainActivity;
import com.eris.classes.Incident;
import com.eris.classes.Responder;
import com.eris.services.DatabaseService;

import java.util.ArrayList;

///**
// * A simple {@link Fragment} subclass.
// * Activities that contain this fragment must implement the
// * {@link IncidentDatabaseFragment.OnFragmentInteractionListener} interface
// * to handle interaction events.
// * Use the {@link IncidentDatabaseFragment#newInstance} factory method to
// * create an instance of this fragment.
// */
public class IncidentDatabaseFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER



    public static final String TAG = IncidentDatabaseFragment.class.getSimpleName();
    //This needs to be the format for everything in this.
    public static final String GET_SINGLE_RESPONDER = TAG + ".get_single_responder";
    public static final String GET_ORG_SUBORDINATES = TAG + ".get_org_subordinates";
    public static final String GET_ALL_RESPONDERS = TAG + ".get_all_responders";
    public static final String GET_ORG_RESPONDERS = TAG + ".get_org_responders";
    public static final String GET_INCIDENT_RESPONDERS = TAG + ".get_incident_responders";
    public static final String GET_ALL_INCIDENTS = TAG + ".get_all_incidents";

    private BroadcastReceiver receiver;
    private View root;
    private Button buttonGetResponder;
    private Button buttonGetOrgSubordinates;
    private Button buttonGetOrgResponders;
    private Button buttonGetAllResponders;
    private Button buttonGetRespondersByIncident;
    private Button buttonGetAllIncidents;

//    private OnFragmentInteractionListener mListener;

    public IncidentDatabaseFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment IncidentDatabaseFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static IncidentDatabaseFragment newInstance(String param1, String param2) {
        IncidentDatabaseFragment fragment = new IncidentDatabaseFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create an intent filter
        IntentFilter filter = new IntentFilter();
        filter.addAction(DatabaseService.DATABASE_SERVICE_ACTION);
        this.receiver = new DemoSceneFragmentReceiver();
        this.getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_demo_scene, container, false);
        buttonGetResponder = (Button) root.findViewById(R.id.button_get_responder);
        buttonGetOrgSubordinates = (Button) root.findViewById(R.id.button_get_org_subordinates);
        buttonGetOrgResponders = (Button) root.findViewById(R.id.button_get_org_responders);
        buttonGetAllResponders = (Button) root.findViewById(R.id.button_get_all_responders);
        buttonGetRespondersByIncident = (Button) root.findViewById(R.id.button_get_responders_by_incident);
        buttonGetAllIncidents = (Button) root.findViewById(R.id.button_get_all_incidents);

        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        buttonGetResponder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).databaseService.getResponderData("5555555555", GET_SINGLE_RESPONDER);
            }
        });

        buttonGetOrgSubordinates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView tv = (TextView)root.findViewById(R.id.scene_main_text);
                tv.setText("this button is disabled.");
            }
        });

        buttonGetOrgResponders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).databaseService.getOrgResponders(Incident.Department.EMS, GET_ORG_SUBORDINATES);
            }
        });

        buttonGetAllResponders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).databaseService.getAllResponders(GET_ALL_RESPONDERS);
            }
        });

        buttonGetRespondersByIncident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               ((MainActivity)getActivity()).databaseService.getRespondersByIncident("5842", GET_INCIDENT_RESPONDERS);
            }
        });

        buttonGetAllIncidents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).databaseService.getAllIncidents(GET_ALL_INCIDENTS);
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

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unregister location updates broadcast receiver.
        this.getActivity().unregisterReceiver(receiver);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onFragmentInteraction(Uri uri);
//    }

    public class DemoSceneFragmentReceiver extends BroadcastReceiver {

        public DemoSceneFragmentReceiver() {
            //Idk if things should be here.
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Got the broadcast.");
            //This seems not to work.
            TextView tv = (TextView)root.findViewById(R.id.scene_main_text);

            String callingMethodIdentifier = intent.getStringExtra(DatabaseService.CALLING_METHOD_IDENTIFIER);
            if (callingMethodIdentifier.equals(GET_SINGLE_RESPONDER)) {
                if (intent.getStringExtra(DatabaseService.ERROR_STATUS).equals(Responder.NO_ERROR)) {
                    Responder r = intent.getParcelableExtra(DatabaseService.DATA);
                    tv.setText(r.getName()+ r.getHeartrateRecord() + r.getOrgSubordinates());
                    Log.d(TAG, "Info: " + r.getName() + r.getHeartrateRecord() + r.getOrgSubordinates());
                } else if (intent.getStringExtra(DatabaseService.ERROR_STATUS).equals(Responder.QUERY_FAILED)) {
                    tv.setText("Failed to get responder [insert name here]");
                    Log.d(TAG, "Failed to get data.");
                } else {
                    Log.e(TAG, "Unexpected status " + intent.getStringExtra(DatabaseService.ERROR_STATUS));
                }
                Log.d(TAG, "Textview text: " + tv.getText().toString());
            } else if (callingMethodIdentifier.equals(GET_ORG_SUBORDINATES)) {
                Parcelable parcelables[] = intent.getParcelableArrayExtra(DatabaseService.DATA);
                Responder subordinates[] = new Responder[parcelables.length];
                for(int i = 0; i < parcelables.length; i++) {
                    subordinates[i] = (Responder) parcelables[i];
                }
                tv.setText(subordinates[0].getUserID() + subordinates[0].getOrganization() + subordinates[0].getLatitude() + subordinates[1].getUserID() + subordinates[1].getOrganization() + subordinates[1].getLatitude() + subordinates[2].getUserID());
            } else if (callingMethodIdentifier.equals(GET_ORG_RESPONDERS)) {
                Parcelable parcelables[] = intent.getParcelableArrayExtra(DatabaseService.DATA);
                Responder responders[] = new Responder[parcelables.length];
                for(int i = 0; i < parcelables.length; i++) {
                    responders[i] = (Responder) parcelables[i];
                }
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < responders.length; i++) {
                    builder.append(responders[i].getName());
                }
                tv.setText(builder.toString());

            } else if (callingMethodIdentifier.equals(GET_ALL_RESPONDERS)) {
                Log.d(TAG, "Get all responders intent.");
                Parcelable parcelables[] = intent.getParcelableArrayExtra(DatabaseService.DATA);
                Responder responderArray[] = new Responder[parcelables.length];
                for(int i = 0; i < parcelables.length; i++) {
                    responderArray[i] = (Responder) parcelables[i];
                }
                //TODO this is not robust.  But it's a demo method.
                tv.setText(responderArray[0].getUserID() + responderArray[0].getOrganization() + responderArray[0].getLatitude() + responderArray[1].getUserID() + responderArray[1].getOrganization() + responderArray[1].getLatitude() + responderArray[2].getUserID());
            } else if (callingMethodIdentifier.equals(GET_INCIDENT_RESPONDERS)) {
                Log.d(TAG, "Get all responders by incident intent.");
                Parcelable parcelables[] = intent.getParcelableArrayExtra(DatabaseService.DATA);
                Log.d(TAG, "Items: " + parcelables.length);
                Responder responderArray[] = new Responder[parcelables.length];
                for(int i = 0; i < parcelables.length; i++) {
                    responderArray[i] = (Responder) parcelables[i];
                }
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < responderArray.length; i++) {
                    builder.append(responderArray[i].getName());
                }
                tv.setText(builder.toString());
            } else if (callingMethodIdentifier.equals(GET_ALL_INCIDENTS)) {
                Log.d(TAG, "Get all incidents intent.");
                Parcelable parcelables[] = intent.getParcelableArrayExtra(DatabaseService.DATA);
                Log.d(TAG, "Items: " + parcelables.length);
                Incident incidents[] = new Incident[parcelables.length];
                for(int i = 0; i < parcelables.length; i++) {
                    incidents[i] = (Incident) parcelables[i];
                }
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < incidents.length; i++) {
                    builder.append(incidents[i].getOrganizations());
                }
                tv.setText(builder.toString());

            } else {
                Log.e(TAG, "Unexpected method data: " + intent.getStringExtra(DatabaseService.CALLING_METHOD_IDENTIFIER));
            }
        }
    }
}
