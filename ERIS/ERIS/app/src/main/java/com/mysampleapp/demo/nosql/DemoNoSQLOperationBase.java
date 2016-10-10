package com.mysampleapp.demo.nosql;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.mysampleapp.R;

public abstract class DemoNoSQLOperationBase implements DemoNoSQLOperation {
    protected final String title, example;

    DemoNoSQLOperationBase(final String title, final String example) {
        this.title = title;
        this.example = example;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public int getViewType() {
        return DemoNoSQLOperationListAdapter.ViewType.OPERATION.ordinal();
    }

    private class SelectOpViewHolder {
        private final TextView titleTextView;
        private final TextView exampleTextView;

        SelectOpViewHolder(final TextView titleTextView, final TextView exampleTextView) {
            this.titleTextView = titleTextView;
            this.exampleTextView = exampleTextView;
        }
    }

    @Override
    public View getView(final LayoutInflater inflater, final View convertView) {
        final RelativeLayout listItemLayout;

        final SelectOpViewHolder selectOpViewHolder;
        if (convertView != null) {
            listItemLayout = (RelativeLayout) convertView;
            selectOpViewHolder = (SelectOpViewHolder) listItemLayout.getTag();
        } else {
            listItemLayout = (RelativeLayout) inflater.inflate(R.layout.demo_nosql_select_operation_list_item, null);
            selectOpViewHolder = new SelectOpViewHolder(
                (TextView) listItemLayout.findViewById(R.id.nosql_query_operation_title),
                (TextView) listItemLayout.findViewById(R.id.nosql_query_operation_example));
            listItemLayout.setTag(selectOpViewHolder);
        }

        selectOpViewHolder.titleTextView.setText(title);
        selectOpViewHolder.exampleTextView.setText(example);

        return listItemLayout;
    }

    @Override
    public boolean isScan() {
        return false;
    }
}
