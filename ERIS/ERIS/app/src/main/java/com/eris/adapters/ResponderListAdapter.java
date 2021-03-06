package com.eris.adapters;

import android.content.Context;
import android.graphics.drawable.Icon;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ImageView;

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

        // Get responder object.
        Responder responder = getItem(position);

        // Gain access to view.
        View root = inflater.inflate(R.layout.list_item_responder, parent, false);

        // Get view elements.
        TextView nameTextView = (TextView) root.findViewById(R.id.responder_name);
        TextView heartRateTextView = (TextView) root.findViewById(R.id.responder_heart_rate);
        ImageView userPhotoImageView = (ImageView) root.findViewById(R.id.responder_image);

        // Set content of view elements.
        nameTextView.setText(responder.getName());
        heartRateTextView.setText(Float.toString(responder.getHeartRate()) + " bpm");
        userPhotoImageView.setImageResource(R.drawable.ic_account_circle_black_24dp);

        // Make name TextView scroll if marque is enabled.
        nameTextView.setSelected(true);

        // Return the altered view.
        return root;

    }
}
