package com.eris.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.models.nosql.UserDataDO;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.eris.R;
import com.eris.adapters.ResponderDatabaseListAdapter;
import com.eris.classes.Responder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by Will Schrag on 10/16/2016.
 */

public class DemoResponderDatabaseFragment extends Fragment{
    private final String TAG = DemoResponderDatabaseFragment.class.getSimpleName();

    private DynamoDBMapper mapper;

    private ResponderDatabaseListAdapter responderDatabaseListAdapter;
    private ListView responderDatabaseListView;
    private TextView userIdKeyTextView;
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

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_demo_responder_database, container, false);

        // Initialize UI elements
        userIdKeyTextView = (TextView) root.findViewById(R.id.responderIdField);
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

        responderDatabaseListAdapter = new ResponderDatabaseListAdapter(getActivity());
        responderDatabaseListView = (ListView) root.findViewById(R.id.responderDatabaseListView);
        responderDatabaseListView.setAdapter(responderDatabaseListAdapter);
        responderDatabaseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Update the scroll view's content to match the selected responder
                final Responder responder = responderDatabaseListAdapter.getItem(i);

                userIdKeyTextView.setText(responder.getUserID());
                responderNameField.setText(responder.getName());
                responderOrganizationField.setText(responder.getOrganization());
                responderOrgSuperiorField.setText(responder.getOrgSuperior());
                responderLatitudeField.setText(responder.getLatitude());
                responderLongitudeField.setText(responder.getLongitude());
                responderHeartbeatField.setText(responder.getHeartrateRecord().get(0));
                responderOrgSubordinatesField.setText(responder.getOrgSubordinates().toString());
                responderIncidentIdField.setText(responder.getSceneID());
                responderIncidentSuperiorField.setText(responder.getIncidentSuperior());
                responderIncidentSubordinatesField.setText(responder.getIncidentSubordinates().toString());
            }
        });

        FetchTask task = new FetchTask();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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

        ArrayList<Responder> data = new ArrayList<Responder>();

        @Override
        protected Void doInBackground(Void... voids) {
            // Perform a scan using a filter condition.
            HashMap<String, AttributeValue> attrValues = new HashMap<>();
            attrValues.put(":org", new AttributeValue().withS("EMS"));

            DynamoDBScanExpression scanExpr = new DynamoDBScanExpression()
                    .withFilterExpression("organization = :org")
                    .withExpressionAttributeValues(attrValues);
            List<UserDataDO> results = mapper.scan(UserDataDO.class, scanExpr);

            for (UserDataDO user : results ) {
                data.add(new Responder(user.getUserId(), user.getName(), user.getOrganization(),
                        user.getHeartbeatRecord(), user.getOrgSuperior(), user.getOrgSubordinates(),
                        user.getLatitude(), user.getLongitude(), user.getCurrentIncidentId(),
                        user.getIncidentSuperior(), user.getIncidentSubordinates()));
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // Add each data element to the list adapter.
            for ( Responder element : data ) {
                responderDatabaseListAdapter.add(element);
            }
        }
    }
}
