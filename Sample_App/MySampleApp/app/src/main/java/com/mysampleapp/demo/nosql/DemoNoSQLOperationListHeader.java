package com.mysampleapp.demo.nosql;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.mysampleapp.R;

public class DemoNoSQLOperationListHeader implements DemoNoSQLOperationListItem {
    final String headerText;

    DemoNoSQLOperationListHeader(final String headerText) {
        this.headerText = headerText;
    }

    @Override
    public int getViewType() {
        return DemoNoSQLOperationListAdapter.ViewType.HEADER.ordinal();
    }

    @Override
    public View getView(final LayoutInflater inflater, final View convertView) {
        final View itemView = inflater.inflate(R.layout.demo_nosql_select_operation_list_header, null);
        final TextView headerTextView = (TextView) itemView.findViewById(R.id.nosql_operation_list_header);
        headerTextView.setText(headerText);
        return itemView;
    }
}
