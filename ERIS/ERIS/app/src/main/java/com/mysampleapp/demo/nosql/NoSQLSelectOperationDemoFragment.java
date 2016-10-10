package com.mysampleapp.demo.nosql;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.util.ThreadUtils;
import com.mysampleapp.R;
import com.mysampleapp.demo.DemoFragmentBase;

import java.util.List;

public class NoSQLSelectOperationDemoFragment extends DemoFragmentBase implements AdapterView.OnItemClickListener {
    private static final String LOG_TAG = NoSQLSelectOperationDemoFragment.class.getSimpleName();

    /** Bundle key for retrieving the table name from the fragment's arguments. */
    public static final String BUNDLE_ARGS_TABLE_TITLE = "tableTitle";

    /** The NoSQL Table demo operations will be run against. */
    private DemoNoSQLTableBase demoTable;

    /** The List View containing the NoSQL Operations that may be selected */
    private ListView operationsListView;

    /** The Adapter for the NoSQL Operations List. */
    private ArrayAdapter<DemoNoSQLOperationListItem> operationsListAdapter;

    /** The Application context. */
    private Context appContext;

    /** The Runnable posted to show the spinner. */
    private SpinnerRunner spinnerRunner;

    /** The delay that must pass before showing a spinner. */
    private static final int SPINNER_DELAY_MS = 300;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        appContext = getActivity().getApplicationContext();
        final Bundle args = getArguments();
        final String tableName = args.getString(BUNDLE_ARGS_TABLE_TITLE);
        demoTable = DemoNoSQLTableFactory.instance(getContext().getApplicationContext())
            .getNoSQLTableByTableName(tableName);

        // Inflate the layout for this fragment.
        final View fragmentView = inflater.inflate(
            R.layout.fragment_demo_nosql_select_operation, container, false);

        ((AppCompatActivity) getActivity())
            .getSupportActionBar()
            .setTitle(String.format(appContext.getString(
                R.string.main_fragment_title_nosql_select_operation), demoTable.getTableName()));

        return fragmentView;
    }

    public void createOperationsList(final View fragmentView) {
        operationsListView = (ListView) fragmentView.findViewById(R.id.nosql_operation_list);
        operationsListAdapter = new DemoNoSQLOperationListAdapter(getActivity(),
            R.layout.demo_nosql_select_operation_list_item);
        operationsListView.setOnItemClickListener(this);
        operationsListView.setAdapter(operationsListAdapter);
        demoTable.getSupportedDemoOperations(appContext, new DemoNoSQLTableBase.SupportedDemoOperationsHandler() {
            @Override
            public void onSupportedOperationsReceived(final List<DemoNoSQLOperationListItem> supportedOperations) {
                // Populate the operations list.
                operationsListAdapter.addAll(supportedOperations);
            }
        });
    }

    private void insertSampleData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    demoTable.insertSampleData();
                } catch (final AmazonClientException ex) {
                    // The insertSampleData call already logs the error, so we only need to
                    // show the error dialog to the user at this point.
                    DynamoDBUtils.showErrorDialogForServiceException(getActivity(),
                        getString(R.string.nosql_dialog_title_failed_operation_text), ex);
                    return;
                }
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                        dialogBuilder.setTitle(R.string.nosql_dialog_title_added_sample_data_text);
                        dialogBuilder.setMessage(R.string.nosql_dialog_message_added_sample_data_text);
                        dialogBuilder.setNegativeButton(R.string.nosql_dialog_ok_text, null);
                        dialogBuilder.show();
                    }
                });
            }
        }).start();
    }

    private void removeSampleData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    demoTable.removeSampleData();
                } catch (final AmazonClientException ex) {
                    // The removeSampleData call already logs the error, so we only need to
                    // show the error dialog to the user at this point.
                    DynamoDBUtils.showErrorDialogForServiceException(getActivity(),
                        getString(R.string.nosql_dialog_title_failed_operation_text), ex);
                    return;
                }
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final Activity activity = getActivity();
                        if (activity != null) {
                            final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                            dialogBuilder.setTitle(R.string.nosql_dialog_title_removed_sample_data_text);
                            dialogBuilder.setMessage(R.string.nosql_dialog_message_removed_sample_data_text);
                            dialogBuilder.setNegativeButton(R.string.nosql_dialog_ok_text, null);
                            dialogBuilder.show();
                        } else {
                            // if our activity has left the foreground, alert the user via a toast.
                            Toast.makeText(appContext,
                                R.string.nosql_dialog_title_removed_sample_data_text,
                                Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    private void promptToDeleteSampleData() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
            .setTitle(R.string.nosql_dialog_title_remove_confirmation)
            .setNegativeButton(android.R.string.no, null);
        builder.setMessage(R.string.nosql_dialog_message_remove_confirmation);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                removeSampleData();
            }
        });
        builder.show();
    }

    @Override
    public void onViewCreated(final View fragmentView, final Bundle savedInstanceState) {
        spinnerRunner = new SpinnerRunner();
        createOperationsList(fragmentView);

        final Button insertSampleDataButton = (Button) fragmentView.findViewById(R.id.button_insert_sample_data);
        insertSampleDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                insertSampleData();
            }
        });

        Button removeSampleDataButton = (Button) fragmentView.findViewById(R.id.button_remove_sample_data);
        removeSampleDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                promptToDeleteSampleData();
            }
        });
    }

    private void handleNoResultsFound() {
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Re-enable the operations list view.
                operationsListView.setEnabled(true);
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                dialogBuilder.setTitle(R.string.nosql_dialog_title_no_results_text);
                dialogBuilder.setMessage(R.string.nosql_dialog_message_no_results_text);
                dialogBuilder.setNegativeButton(R.string.nosql_dialog_ok_text, null);
                dialogBuilder.show();
            }
        });
    }

    private void showResultsForOperation(final DemoNoSQLOperation operation) {
        // On execution complete, open the NoSQLShowResultsDemoFragment.
        final NoSQLShowResultsDemoFragment resultsDemoFragment = new NoSQLShowResultsDemoFragment();
        resultsDemoFragment.setOperation(operation);

        final FragmentActivity fragmentActivity = getActivity();

        if (fragmentActivity != null) {
            fragmentActivity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment_container, resultsDemoFragment)
                .addToBackStack(null)
                .commitAllowingStateLoss();
        }
    }

    private class SpinnerRunner implements Runnable {
        /** A Handler for showing a spinner if service call latency becomes too long. */
        private Handler spinnerHandler;
        private volatile boolean isCanceled = false;
        private volatile ProgressDialog progressDialog = null;

        private SpinnerRunner() {
            spinnerHandler = new Handler();
        }

        @Override
        public synchronized void run() {
            if (isCanceled) {
                return;
            }
            final FragmentActivity activity = getActivity();
            if (activity != null) {
                progressDialog = ProgressDialog.show(activity,
                    getString(R.string.nosql_dialog_title_pending_results_text),
                    getString(R.string.nosql_dialog_message_pending_results_text));
            }
        }

        private void schedule() {
            isCanceled = false;
            // Post delayed runnable so that the spinner will be shown if the delay
            // expires and results haven't come back.
            spinnerHandler.postDelayed(this, SPINNER_DELAY_MS);
        }

        private synchronized void cancelOrDismiss() {
            isCanceled = true;
            // Cancel showing the spinner if it hasn't been shown yet.
            spinnerHandler.removeCallbacks(this);

            if (progressDialog != null) {
                // if the spinner has been shown, dismiss it.
                progressDialog.dismiss();
                progressDialog = null;
            }
        }
    }

    private void showSpinner() {
        // Disable the operations list until query executes.
        operationsListView.setEnabled(false);

        spinnerRunner.schedule();
    }

    private void dismissSpinner() {
        spinnerRunner.cancelOrDismiss();
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {

        if (operationsListAdapter.getItem(position).getViewType() == DemoNoSQLOperationListAdapter.ViewType.OPERATION.ordinal()) {
            final DemoNoSQLOperation operation = (DemoNoSQLOperation) operationsListAdapter.getItem(position);

            showSpinner();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean foundResults = false;
                    try {
                        foundResults = operation.executeOperation();
                    } catch (final AmazonClientException ex) {
                        ThreadUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                operationsListView.setEnabled(true);
                                Log.e(LOG_TAG,
                                    String.format("Failed executing selected DynamoDB table (%s) operation (%s) : %s",
                                        demoTable.getTableName(), operation.getTitle(), ex.getMessage()), ex);
                                DynamoDBUtils.showErrorDialogForServiceException(getActivity(),
                                    getString(R.string.nosql_dialog_title_failed_operation_text), ex);
                            }
                        });
                        return;
                    } finally {
                        dismissSpinner();
                    }

                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (operation.isScan()) {
                                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                                dialogBuilder.setTitle(R.string.nosql_dialog_title_scan_warning_text);
                                dialogBuilder.setMessage(R.string.nosql_dialog_message_scan_warning_text);
                                dialogBuilder.setNegativeButton(R.string.nosql_dialog_ok_text, null);
                                dialogBuilder.show();
                            }
                        }
                    });

                    if (!foundResults) {
                        handleNoResultsFound();
                    } else {
                        showResultsForOperation(operation);
                    }
                }
            }).start();
        }
    }
}
