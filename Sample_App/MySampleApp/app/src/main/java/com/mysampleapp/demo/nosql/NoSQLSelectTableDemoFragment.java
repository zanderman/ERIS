package com.mysampleapp.demo.nosql;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mysampleapp.R;
import com.mysampleapp.demo.DemoFragmentBase;

public class NoSQLSelectTableDemoFragment extends DemoFragmentBase {

    private final class ListItemViewHolder {
        private final TextView tableName;
        private final TextView partitionLabel;
        private final TextView partitionKey;
        private final TextView sortLabel;
        private final TextView sortKey;
        private final TextView indexLabel;
        private final TextView indexCount;

        ListItemViewHolder(final TextView tableName, final TextView partitionLabel, final TextView partitionKey,
                           final TextView sortLabel, final TextView sortKey, final TextView indexLabel,
                           final TextView indexCount) {
            this.tableName = tableName;
            this.partitionLabel = partitionLabel;
            this.partitionKey = partitionKey;
            this.sortLabel = sortLabel;
            this.sortKey = sortKey;
            this.indexLabel = indexLabel;
            this.indexCount = indexCount;
        }
    }

    private ArrayAdapter<DemoNoSQLTableBase> tablesAdapter;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        final View fragmentView = inflater.inflate(
            R.layout.fragment_demo_nosql_select_table, container, false);

        ((AppCompatActivity) getActivity())
            .getSupportActionBar()
            .setTitle(R.string.main_fragment_title_nosql_select_table);

        return fragmentView;
    }

    public void createTablesList(final View fragmentView) {
        final ListView listView = (ListView) fragmentView.findViewById(R.id.nosql_table_list);

        // Load the table information
        tablesAdapter = new ArrayAdapter<DemoNoSQLTableBase>(getActivity(), R.layout.demo_nosql_select_table_list_item) {
            @Override
            public View getView(final int position, final View convertView,
                                final ViewGroup parent) {
                final LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
                final View itemView;
                final ListItemViewHolder viewHolder;
                final TextView tableNameTextView, partitionLabelTextView, partitionKeyTextView, sortLabelTextView,
                               sortKeyTextView, indexLabelTextView, indexCountTextView;

                if (convertView != null) {
                    itemView = convertView;
                    viewHolder = (ListItemViewHolder) itemView.getTag();
                    tableNameTextView = viewHolder.tableName;
                    partitionLabelTextView = viewHolder.partitionLabel;
                    partitionKeyTextView = viewHolder.partitionKey;
                    sortLabelTextView = viewHolder.sortLabel;
                    sortKeyTextView = viewHolder.sortKey;
                    indexLabelTextView = viewHolder.indexLabel;
                    indexCountTextView = viewHolder.indexCount;
                } else {
                    itemView = layoutInflater.inflate(R.layout.demo_nosql_select_table_list_item, null);
                    tableNameTextView = (TextView) itemView.findViewById(R.id.nosql_table_name);
                    sortLabelTextView = (TextView) itemView.findViewById(R.id.nosql_table_sort_label);
                    partitionLabelTextView = (TextView) itemView.findViewById(R.id.nosql_table_partition_label);
                    partitionKeyTextView = (TextView) itemView.findViewById(R.id.nosql_table_partition_key);
                    sortKeyTextView = (TextView) itemView.findViewById(R.id.nosql_table_sort_key);
                    indexLabelTextView = (TextView) itemView.findViewById(R.id.nosql_table_indexes_label);
                    indexCountTextView = (TextView) itemView.findViewById(R.id.nosql_table_index_count);
                    viewHolder = new ListItemViewHolder(tableNameTextView, partitionLabelTextView, partitionKeyTextView,
                        sortLabelTextView, sortKeyTextView, indexLabelTextView, indexCountTextView);
                    itemView.setTag(viewHolder);
                }

                final DemoNoSQLTableBase item = getItem(position);
                tableNameTextView.setText(item.getTableName());

                partitionLabelTextView.setVisibility(View.VISIBLE);
                indexLabelTextView.setVisibility(View.VISIBLE);

                partitionKeyTextView.setText(String.format("%s (%s)", item.getPartitionKeyName(), item.getPartitionKeyType()));
                partitionKeyTextView.setVisibility(View.VISIBLE);
                final int resIdAboveIndex;
                if (item.getSortKeyName() != null) {
                    sortKeyTextView.setText(String.format("%s (%s)", item.getSortKeyName(), item.getSortKeyType()));
                    sortKeyTextView.setVisibility(View.VISIBLE);
                    sortLabelTextView.setVisibility(View.VISIBLE);
                    resIdAboveIndex = R.id.nosql_table_sort_label;
                } else {
                    resIdAboveIndex = R.id.nosql_table_partition_label;
                    sortLabelTextView.setVisibility(View.GONE);
                    sortKeyTextView.setVisibility(View.GONE);
                }
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) indexLabelTextView.getLayoutParams();
                lp.addRule(RelativeLayout.BELOW, resIdAboveIndex);
                indexLabelTextView.setLayoutParams(lp);

                indexCountTextView.setText(String.format("%d", item.getNumIndexes()));

                return itemView;
            }
        };
        listView.setAdapter(tablesAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                final NoSQLSelectOperationDemoFragment fragment = new NoSQLSelectOperationDemoFragment();

                final Bundle args = new Bundle();
                args.putString(NoSQLSelectOperationDemoFragment.BUNDLE_ARGS_TABLE_TITLE,
                    tablesAdapter.getItem(position).getTableName());
                fragment.setArguments(args);

                getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
            }
        });
    }

    @Override
    public void onViewCreated(final View fragmentView, final Bundle savedInstanceState) {
        super.onViewCreated(fragmentView, savedInstanceState);

        createTablesList(fragmentView);

        final DemoNoSQLTableFactory demoNoSQLTableFactory =
            DemoNoSQLTableFactory.instance(getContext().getApplicationContext());

        for (DemoNoSQLTableBase table : demoNoSQLTableFactory.getNoSQLSupportedTables()) {
            tablesAdapter.add(table);
        }

        tablesAdapter.notifyDataSetChanged();
    }
}
