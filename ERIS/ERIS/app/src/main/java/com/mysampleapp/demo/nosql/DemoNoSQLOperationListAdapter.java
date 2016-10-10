package com.mysampleapp.demo.nosql;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class DemoNoSQLOperationListAdapter extends ArrayAdapter<DemoNoSQLOperationListItem> {

    public enum ViewType {
        HEADER, OPERATION
    }

    public DemoNoSQLOperationListAdapter(final Context context, final int resource) {
        super(context, resource);
    }

    @Override
    public int getItemViewType(final int position) {
        return getItem(position).getViewType();
    }

    @Override
    public int getViewTypeCount() {
        return ViewType.values().length;
    }

    @Override
    public View getView(final int position, final View convertView,
                        final ViewGroup parent) {
        final LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        final DemoNoSQLOperationListItem listItem = getItem(position);
        return listItem.getView(layoutInflater, convertView);
    }
}
