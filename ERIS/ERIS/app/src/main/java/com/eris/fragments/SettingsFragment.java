package com.eris.fragments;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.eris.R;
import com.eris.activities.MainActivity;
import com.eris.classes.NotificationDispatcher;
import com.eris.classes.Responder;
import com.eris.services.DatabaseService;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {
    private SeekBar broadcastBar;
    private TextView broadcastLabel;
    private Button testAlertButton;
    private CheckBox phoneCheck;
    private CheckBox watchCheck;
    private CheckBox glassesCheck;
    private EditText userFirstNameEditText;
    private EditText userLastNameEditText;
    private EditText userSuperiorIdEditText;
    private EditText userOrganizationEditText;
    private Button updateUserInfoButton;

    private BroadcastReceiver receiver;
    private IntentFilter receiverFilter;
    private String userUpdateInfoRequestMethodIdentifier;

    private SharedPreferences settings;

    public String broadcastPref;
    public String phonePref;
    public String watchPref;
    public String glassPref;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String settingsFile = getResources().getString(R.string.sharedpreferences_user_settings);
        settings = getContext().getSharedPreferences(settingsFile, 0);

        broadcastPref = getResources().getString(R.string.preferences_broadcast);
        phonePref = getResources().getString(R.string.preferences_phone_alerts);
        watchPref = getResources().getString(R.string.preferences_watch_alerts);
        glassPref = getResources().getString(R.string.preferences_glass_alerts);

        // Create an intent filter
        receiverFilter = new IntentFilter();
        receiverFilter.addAction(DatabaseService.DATABASE_SERVICE_ACTION);

        // Create broadcast receiver object.
        this.receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // Determine which broadcast was sent.
                String callingMethodIdentifier = intent.getStringExtra(DatabaseService.CALLING_METHOD_IDENTIFIER);
                if (callingMethodIdentifier != null) {
                    if (callingMethodIdentifier.equals(userUpdateInfoRequestMethodIdentifier)) {
                        Toast.makeText(getActivity(), "Settings Updated", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
        this.getActivity().registerReceiver(receiver, receiverFilter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        broadcastBar = (SeekBar) view.findViewById(R.id.broadcastBar);
        broadcastLabel = (TextView) view.findViewById(R.id.broadcastLabel);
        testAlertButton = (Button) view.findViewById(R.id.testAlert);
        phoneCheck = (CheckBox) view.findViewById(R.id.phoneCheck);
        watchCheck = (CheckBox) view.findViewById(R.id.watchCheck);
        glassesCheck = (CheckBox) view.findViewById(R.id.glassesCheck);
        userFirstNameEditText = (EditText) view.findViewById(R.id.userFirstNameEditText);
        userLastNameEditText = (EditText) view.findViewById(R.id.userLastNameEditText);
        userSuperiorIdEditText = (EditText) view.findViewById(R.id.userSuperiorIdEditText);
        userOrganizationEditText = (EditText) view.findViewById(R.id.userOrganizationEditText);
        updateUserInfoButton = (Button) view.findViewById(R.id.updateUserInfoButton);

        // Load current settings from memory
        broadcastBar.setProgress(
                convertSecondsToBroadcastBar(settings.getInt(broadcastPref, 20)));
        phoneCheck.setChecked(settings.getBoolean(phonePref, true));
        watchCheck.setChecked(settings.getBoolean(watchPref, false));
        glassesCheck.setChecked(settings.getBoolean(glassPref, false));

        broadcastLabel.setText(broadcastLabelText(settings.getInt(broadcastPref, 20)));
        broadcastBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int newProg, boolean b) {
                int seconds = convertBroadcastBarToSeconds(newProg);
                broadcastLabel.setText(broadcastLabelText(seconds));
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt(broadcastPref, seconds);
                editor.commit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        phoneCheck.setOnCheckedChangeListener(createCheckedChangeListener(phonePref));
        watchCheck.setOnCheckedChangeListener(createCheckedChangeListener(watchPref));
        glassesCheck.setOnCheckedChangeListener(createCheckedChangeListener(glassPref));

        testAlertButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                // placeholder code for sending notifications
                NotificationDispatcher.send("ERIS Alert", "Test Notification", getContext());
            }
        });

        final DatabaseService databaseService = ((MainActivity) getActivity()).databaseService;
        final Responder currUser = databaseService.getCurrentUser();
        if (currUser == null) {
            Toast.makeText(getActivity(), "User info not yet found.", Toast.LENGTH_SHORT).show();
        }
        else {
            userFirstNameEditText.setText(currUser.getFirstName());
            userLastNameEditText.setText(currUser.getLastName());
            userSuperiorIdEditText.setText(currUser.getOrgSuperior());
            userOrganizationEditText.setText(currUser.getOrganization());
        }

        updateUserInfoButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                // Set values for the current user and ask the database service to push this update
                currUser.setFirstName(userFirstNameEditText.getText().toString());
                currUser.setLastName(userLastNameEditText.getText().toString());
                currUser.setOrgSuperior(userSuperiorIdEditText.getText().toString());
                currUser.setOrganization(userOrganizationEditText.getText().toString());

                userUpdateInfoRequestMethodIdentifier = this.getClass().getSimpleName()
                        + "broadcast_action_database_update_user_settings"
                        + currUser.getUserID();
                databaseService.pushUpdatedResponderData(currUser, userUpdateInfoRequestMethodIdentifier);
            }
        });

        return view;
    }

    private String broadcastLabelText(int val) {
        return "Broadcast interval: " + val + " seconds.";
    }

    private CompoundButton.OnCheckedChangeListener createCheckedChangeListener(final String name) {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(name, b);
                editor.commit();
            }
        };
    }

    private int convertBroadcastBarToSeconds(int bar) {
        return (bar + 1) * 5;
    }

    private int convertSecondsToBroadcastBar(int seconds) {
        return (seconds / 5) - 1;
    }
}
