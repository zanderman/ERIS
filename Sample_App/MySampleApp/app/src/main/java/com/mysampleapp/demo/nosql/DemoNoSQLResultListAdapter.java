package com.mysampleapp.demo.nosql;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class DemoNoSQLResultListAdapter extends ArrayAdapter<DemoNoSQLResult> {
    final Context context;

    public DemoNoSQLResultListAdapter(final Context context) {
        super(context, -1);
        this.context = context;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final DemoNoSQLResult listItem = getItem(position);
        return listItem.getView(context, convertView, position);
    }

}
