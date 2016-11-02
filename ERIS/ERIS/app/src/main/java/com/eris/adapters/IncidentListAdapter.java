package com.eris.adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eris.R;
import com.eris.classes.Incident;

import java.util.ArrayList;


/**
 * IncidentListAdapter
 *
 * Description
 *  Adapter for controlling a list of incidents.
 */
public class IncidentListAdapter extends ArrayAdapter<Incident> {

    /*
     * Private Members
     */
    private LayoutInflater inflater;
    private TextView titleTextView, addressTextView, descriptionTextView, timeTextView;
    private ArrayList<ImageView> organizations;

    /**
     * Constructor
     *
     * Description:
     *  Creates the layout for the adapter.
     *
     * @param context Context in which the adapter will be used.
     */
    public IncidentListAdapter(Context context) {
        super(context, R.layout.list_item_incident);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Obtain reference to the root view.
        View root = inflater.inflate(R.layout.list_item_incident, parent, false);

        // Get reference to incident.
        Incident incident = this.getItem(position);

        // Get references to view elements.
        titleTextView = (TextView) root.findViewById(R.id.incident_title);
        addressTextView = (TextView) root.findViewById(R.id.incident_address);
        descriptionTextView = (TextView) root.findViewById(R.id.incident_description);
        timeTextView = (TextView) root.findViewById(R.id.incident_time);

        organizations = new ArrayList<>();
        organizations.add((ImageView) root.findViewById(R.id.incident_request_image_1));
        organizations.add((ImageView) root.findViewById(R.id.incident_request_image_2));
        organizations.add((ImageView) root.findViewById(R.id.incident_request_image_3));

        // Alter content of view elements.
        titleTextView.setText(incident.getTitle());// TODO: add title field to incident
        addressTextView.setText(incident.getAddress());
        descriptionTextView.setText(incident.getDescription());
        timeTextView.setText(incident.getTime());

        // Add requested organization.
        int imageIndex = 0;
        for (String org : incident.getOrganizations()) {
            switch (org.toLowerCase()) {
                case "fire":
                    organizations.get(imageIndex).setImageResource(R.drawable.ic_fire); // Set image icon.
                    LinearLayout.LayoutParams paramsFire = new LinearLayout.LayoutParams(70,70); // width=70px, height=70px
                    paramsFire.gravity = Gravity.CENTER; // Center the image in the view.
                    organizations.get(imageIndex).setLayoutParams(paramsFire); // Apply the layout parameters.
                    imageIndex++;
                    break;
                case "ems":
                    organizations.get(imageIndex).setImageResource(R.drawable.ic_ems); // Set image icon.
                    LinearLayout.LayoutParams paramsEms = new LinearLayout.LayoutParams(70,70); // width=70px, height=70px
                    paramsEms.gravity = Gravity.CENTER; // Center the image in the view.
                    organizations.get(imageIndex).setLayoutParams(paramsEms); // Apply the layout parameters.
                    imageIndex++;
                    break;
                case "police":
                    organizations.get(imageIndex).setImageResource(R.drawable.ic_police); // Set image icon.
                    LinearLayout.LayoutParams paramsPolice = new LinearLayout.LayoutParams(60,60); // width=60px, height=60px
                    paramsPolice.gravity = Gravity.CENTER; // Center the image in the view.
                    organizations.get(imageIndex).setLayoutParams(paramsPolice); // Apply the layout parameters.
                    imageIndex++;
                    break;
                default:
                    break;
            }
        }

        // Return the modified root view.
        return root;
    }
}
