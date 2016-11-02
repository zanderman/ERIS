package com.eris.fragments;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.eris.R;
import com.eris.classes.NotificationDispatcher;

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
