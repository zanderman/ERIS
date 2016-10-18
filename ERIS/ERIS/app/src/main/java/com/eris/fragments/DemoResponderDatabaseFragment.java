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

import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.models.nosql.UserDataDO;
import com.eris.R;


/**
 * Created by Will Schrag on 10/16/2016.
 */

public class DemoResponderDatabaseFragment extends Fragment{
    private final String TAG = DemoResponderDatabaseFragment.class.getSimpleName();

    private UserDataDO result = new UserDataDO();
    private final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
    private TextView userIdKeyTextView;

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
        return view;
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
