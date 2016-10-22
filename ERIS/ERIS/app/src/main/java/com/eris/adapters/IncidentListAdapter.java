package com.eris.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.eris.R;
import com.eris.classes.Incident;


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
        View root = super.getView(position, convertView, parent);

        // TODO: Manipulate the root here.

        // Return the modified root view.
        return root;
    }
}
