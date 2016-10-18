package com.eris.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ScrollView;

import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.models.nosql.UserDataDO;
import com.eris.R;
import com.eris.classes.PrototypeDatabaseHelper;
import com.eris.classes.Responder;


/**
 * Created by Will Schrag on 10/16/2016.
 */

public class DemoResponderDatabaseFragment extends Fragment{
    private final String TAG = DemoResponderDatabaseFragment.class.getSimpleName();

    private UserDataDO result = new UserDataDO();
    private final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
    private TextView userIdKeyTextView;

    /*
     * Private Members
     */
    private EditText responderNameField;
    private EditText responderOrganizationField;
    private EditText responderOrgSuperiorField;
    private EditText responderLatitudeField;
    private EditText responderLongitudeField;
    private EditText responderHeartbeatField;
    private EditText responderOrgSubordinatesField;
    private EditText responderIncidentIdField;
    private EditText responderIncidentSuperiorField;
    private EditText responderIncidentSubordinatesField;

    public DemoResponderDatabaseFragment() {
        //Required?
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //result.setOrganization("EMS");
        //result = mapper.load(UserDataDO.class, "4093820716");
        FetchTask task = new FetchTask();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_demo_responder_database, container, false);
        userIdKeyTextView = (TextView) view.findViewById(R.id.responderIdField);
        userIdKeyTextView.setText(result.getUserId());

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_demo_responder_database, container, false);

        // Initialize UI elements
        responderNameField = (EditText) root.findViewById(R.id.responderNameField);
        responderOrganizationField = (EditText) root.findViewById(R.id.responderOrganizationField);
        responderOrgSuperiorField = (EditText) root.findViewById(R.id.responderOrgSuperiorField);
        responderLatitudeField = (EditText) root.findViewById(R.id.responderLatitudeField);
        responderLongitudeField = (EditText) root.findViewById(R.id.responderLongitudeField);
        responderHeartbeatField = (EditText) root.findViewById(R.id.responderHeartbeatField);
        responderOrgSubordinatesField = (EditText) root.findViewById(R.id.responderOrgSubordinatesField);
        responderIncidentIdField = (EditText) root.findViewById(R.id.responderIncidentIdField);
        responderIncidentSuperiorField = (EditText) root.findViewById(R.id.responderIncidentSuperiorField);
        responderIncidentSubordinatesField = (EditText) root.findViewById(R.id.responderIncidentSubordinatesField);

        // TODO this is temporary
        PrototypeDatabaseHelper dbHelper = new PrototypeDatabaseHelper();
        Responder responder = dbHelper.getResponderData("4093820716");
        responderNameField.setText(responder.lastName + ", " + responder.firstName);
        responderOrganizationField.setText("unknown");
        responderOrgSuperiorField.setText("unknown");
        responderLatitudeField.setText(Double.toString(responder.location.latitude));
        responderLongitudeField.setText(Double.toString(responder.location.longitude));
        responderHeartbeatField.setText(Float.toString(responder.heartRate));
        responderOrgSubordinatesField.setText("unknown");
        responderIncidentIdField.setText(responder.sceneID);
        responderIncidentSuperiorField.setText("unknown");
        responderIncidentSubordinatesField.setText("unknown");

        // Return the root view
        return root;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    private class FetchTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            final UserDataDO item = mapper.load(UserDataDO.class, "4093820716");
            Log.d(TAG, item.getUserId());

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    userIdKeyTextView.setText(item.getUserId());
                }
            });
            return null;
        }

    }
}
