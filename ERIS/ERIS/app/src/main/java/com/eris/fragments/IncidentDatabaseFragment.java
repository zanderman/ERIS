package com.eris.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eris.R;
import com.eris.activities.MainActivity;
import com.eris.classes.Responder;
import com.eris.services.DatabaseService;

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

    private BroadcastReceiver receiver;
    private View root;

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

        ((MainActivity)this.getActivity()).databaseService.getResponderData("5555555555", GET_SINGLE_RESPONDER);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_demo_scene, container, false);
        return root;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mListener = null;
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
            Log.d(TAG, "textview: " + tv.toString());

            if (intent.getStringExtra(DatabaseService.CALLING_METHOD_INFO).equals(GET_SINGLE_RESPONDER)) {
                if (intent.getStringExtra(DatabaseService.ERROR_STATUS).equals(Responder.NO_ERROR)) {
                    Responder r = intent.getParcelableExtra(DatabaseService.DATA);
                    tv.setText(r.getName());
                    Log.d(TAG, "Info: " + r.getName() + r.getHeartrateRecord().toString());
                } else if (intent.getStringExtra(DatabaseService.ERROR_STATUS).equals(Responder.RESPONDER_NOT_FOUND)) {
                    tv.setText("Failed to get responder [insert name here]");
                    Log.d(TAG, "Failed to get data.");
                } else {
                    Log.e(TAG, "Unexpected status " + intent.getStringExtra(DatabaseService.ERROR_STATUS));
                }
                Log.d(TAG, "Textview text: " + tv.getText().toString());
            } else {
                Log.e(TAG, "Unexpected method data: " + intent.getStringExtra(DatabaseService.CALLING_METHOD_INFO));
            }
        }
    }
}
