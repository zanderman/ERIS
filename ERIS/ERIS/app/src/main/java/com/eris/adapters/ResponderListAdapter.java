package com.eris.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.eris.R;
import com.eris.classes.Responder;

public class ResponderListAdapter extends ArrayAdapter<Responder> {

    private LayoutInflater inflater;

    public ResponderListAdapter(Context context) {
        super(context, R.layout.list_item_responder);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        return super.getView(position, convertView, parent);

        // Get responder object.
        Responder responder = getItem(position);

        // Gain access to view.
        View root = inflater.inflate(R.layout.list_item_responder, parent, false);

        // Get view elements.
        TextView nameTextView = (TextView) root.findViewById(R.id.nameTextView);
        TextView uidTextView = (TextView) root.findViewById(R.id.uidTextView);
        TextView healthTextView = (TextView) root.findViewById(R.id.healthTextView);
        TextView locationTextView = (TextView) root.findViewById(R.id.locationTextView);

        // Set content of view elements.
        nameTextView.setText(responder.firstName + " " + responder.lastName);
        uidTextView.setText(responder.userID);
        healthTextView.setText(responder.heartRate + " bpm");
        if (responder.location != null) {
            locationTextView.setText(responder.location.latitude + ", " + responder.location.longitude);
        }
        else {
            locationTextView.setText("location unknown");
        }

        // Return the altered view.
        return root;

    }
}
