package com.eris.activities;

import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
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

    public SettingsFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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


        broadcastLabel.setText(broadcastLabelText(broadcastBar.getProgress()));
        broadcastBar.setMax(9);
        broadcastBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int newProg, boolean b) {
                broadcastLabel.setText(broadcastLabelText(newProg));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        testAlertButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (phoneCheck.isChecked()) {
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private String broadcastLabelText(int val) {
        return "Broadcast interval: " + (val + 1)
                + (val == 0 ? " second." : " seconds.");
    }
}
