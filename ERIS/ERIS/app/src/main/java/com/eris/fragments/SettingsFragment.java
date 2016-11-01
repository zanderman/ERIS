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

    public static final String BROADCAST_PREF = "BroadcastPref";
    public static final String PHONE_PREF = "PhonePref";
    public static final String WATCH_PREF = "WatchPref";
    public static final String GLASSES_PREF = "GlassesPref";

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
        broadcastBar.setProgress(settings.getInt(BROADCAST_PREF, 3));
        phoneCheck.setChecked(settings.getBoolean(PHONE_PREF, true));
        watchCheck.setChecked(settings.getBoolean(WATCH_PREF, false));
        glassesCheck.setChecked(settings.getBoolean(GLASSES_PREF, false));

        broadcastLabel.setText(broadcastLabelText(broadcastBar.getProgress()));
        broadcastBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int newProg, boolean b) {
                broadcastLabel.setText(broadcastLabelText(newProg));
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt(BROADCAST_PREF, newProg);
                editor.commit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        phoneCheck.setOnCheckedChangeListener(createCheckedChangeListener(PHONE_PREF));
        watchCheck.setOnCheckedChangeListener(createCheckedChangeListener(WATCH_PREF));
        glassesCheck.setOnCheckedChangeListener(createCheckedChangeListener(GLASSES_PREF));

        testAlertButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                // placeholder code for sending notifications
                if (settings.getBoolean(PHONE_PREF, true)) {
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(getContext())
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setContentTitle("ERIS Alert")
                                    .setContentText("Test notification");
                    NotificationManager mNotificationManager =
                            (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(0, mBuilder.build());
                }
            }
        });
        return view;
    }

    private String broadcastLabelText(int val) {
        return "Broadcast interval: " + (val + 1)
                + (val == 0 ? " second." : " seconds.");
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
}
