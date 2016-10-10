package com.eris.demo.nosql;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.models.nosql.UserDataDO;

import java.util.Set;

public class DemoNoSQLUserDataResult implements DemoNoSQLResult {
    private static final int KEY_TEXT_COLOR = 0xFF333333;
    private final UserDataDO result;

    DemoNoSQLUserDataResult(final UserDataDO result) {
        this.result = result;
    }
    @Override
    public void updateItem() {
        final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
        final String originalValue = result.getCurrentIncidentId();
        result.setCurrentIncidentId(DemoSampleDataGenerator.getRandomSampleString("currentIncidentId"));
        try {
            mapper.save(result);
        } catch (final AmazonClientException ex) {
            // Restore original data if save fails, and re-throw.
            result.setCurrentIncidentId(originalValue);
            throw ex;
        }
    }

    @Override
    public void deleteItem() {
        final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
        mapper.delete(result);
    }

    private void setKeyTextViewStyle(final TextView textView) {
        textView.setTextColor(KEY_TEXT_COLOR);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(dp(5), dp(2), dp(5), 0);
        textView.setLayoutParams(layoutParams);
    }

    /**
     * @param dp number of design pixels.
     * @return number of pixels corresponding to the desired design pixels.
     */
    private int dp(int dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
    private void setValueTextViewStyle(final TextView textView) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(dp(15), 0, dp(15), dp(2));
        textView.setLayoutParams(layoutParams);
    }

    private void setKeyAndValueTextViewStyles(final TextView keyTextView, final TextView valueTextView) {
        setKeyTextViewStyle(keyTextView);
        setValueTextViewStyle(valueTextView);
    }

    private static String bytesToHexString(byte[] bytes) {
        final StringBuilder builder = new StringBuilder();
        builder.append(String.format("%02X", bytes[0]));
        for(int index = 1; index < bytes.length; index++) {
            builder.append(String.format(" %02X", bytes[index]));
        }
        return builder.toString();
    }

    private static String byteSetsToHexStrings(Set<byte[]> bytesSet) {
        final StringBuilder builder = new StringBuilder();
        int index = 0;
        for (byte[] bytes : bytesSet) {
            builder.append(String.format("%d: ", ++index));
            builder.append(bytesToHexString(bytes));
            if (index < bytesSet.size()) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    @Override
    public View getView(final Context context, final View convertView, int position) {
        final LinearLayout layout;
        final TextView resultNumberTextView;
        final TextView userIdKeyTextView;
        final TextView userIdValueTextView;
        final TextView currentIncidentIdKeyTextView;
        final TextView currentIncidentIdValueTextView;
        final TextView heartbeatRecordKeyTextView;
        final TextView heartbeatRecordValueTextView;
        final TextView incidentSubordinatesKeyTextView;
        final TextView incidentSubordinatesValueTextView;
        final TextView incidentSuperiorKeyTextView;
        final TextView incidentSuperiorValueTextView;
        final TextView locationKeyTextView;
        final TextView locationValueTextView;
        final TextView nameKeyTextView;
        final TextView nameValueTextView;
        final TextView orgSubordinatesKeyTextView;
        final TextView orgSubordinatesValueTextView;
        final TextView orgSuperiorKeyTextView;
        final TextView orgSuperiorValueTextView;
        final TextView orginizationKeyTextView;
        final TextView orginizationValueTextView;
        if (convertView == null) {
            layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            resultNumberTextView = new TextView(context);
            resultNumberTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            layout.addView(resultNumberTextView);


            userIdKeyTextView = new TextView(context);
            userIdValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(userIdKeyTextView, userIdValueTextView);
            layout.addView(userIdKeyTextView);
            layout.addView(userIdValueTextView);

            currentIncidentIdKeyTextView = new TextView(context);
            currentIncidentIdValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(currentIncidentIdKeyTextView, currentIncidentIdValueTextView);
            layout.addView(currentIncidentIdKeyTextView);
            layout.addView(currentIncidentIdValueTextView);

            heartbeatRecordKeyTextView = new TextView(context);
            heartbeatRecordValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(heartbeatRecordKeyTextView, heartbeatRecordValueTextView);
            layout.addView(heartbeatRecordKeyTextView);
            layout.addView(heartbeatRecordValueTextView);

            incidentSubordinatesKeyTextView = new TextView(context);
            incidentSubordinatesValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(incidentSubordinatesKeyTextView, incidentSubordinatesValueTextView);
            layout.addView(incidentSubordinatesKeyTextView);
            layout.addView(incidentSubordinatesValueTextView);

            incidentSuperiorKeyTextView = new TextView(context);
            incidentSuperiorValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(incidentSuperiorKeyTextView, incidentSuperiorValueTextView);
            layout.addView(incidentSuperiorKeyTextView);
            layout.addView(incidentSuperiorValueTextView);

            locationKeyTextView = new TextView(context);
            locationValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(locationKeyTextView, locationValueTextView);
            layout.addView(locationKeyTextView);
            layout.addView(locationValueTextView);

            nameKeyTextView = new TextView(context);
            nameValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(nameKeyTextView, nameValueTextView);
            layout.addView(nameKeyTextView);
            layout.addView(nameValueTextView);

            orgSubordinatesKeyTextView = new TextView(context);
            orgSubordinatesValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(orgSubordinatesKeyTextView, orgSubordinatesValueTextView);
            layout.addView(orgSubordinatesKeyTextView);
            layout.addView(orgSubordinatesValueTextView);

            orgSuperiorKeyTextView = new TextView(context);
            orgSuperiorValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(orgSuperiorKeyTextView, orgSuperiorValueTextView);
            layout.addView(orgSuperiorKeyTextView);
            layout.addView(orgSuperiorValueTextView);

            orginizationKeyTextView = new TextView(context);
            orginizationValueTextView = new TextView(context);
            setKeyAndValueTextViewStyles(orginizationKeyTextView, orginizationValueTextView);
            layout.addView(orginizationKeyTextView);
            layout.addView(orginizationValueTextView);
        } else {
            layout = (LinearLayout) convertView;
            resultNumberTextView = (TextView) layout.getChildAt(0);

            userIdKeyTextView = (TextView) layout.getChildAt(1);
            userIdValueTextView = (TextView) layout.getChildAt(2);

            currentIncidentIdKeyTextView = (TextView) layout.getChildAt(3);
            currentIncidentIdValueTextView = (TextView) layout.getChildAt(4);

            heartbeatRecordKeyTextView = (TextView) layout.getChildAt(5);
            heartbeatRecordValueTextView = (TextView) layout.getChildAt(6);

            incidentSubordinatesKeyTextView = (TextView) layout.getChildAt(7);
            incidentSubordinatesValueTextView = (TextView) layout.getChildAt(8);

            incidentSuperiorKeyTextView = (TextView) layout.getChildAt(9);
            incidentSuperiorValueTextView = (TextView) layout.getChildAt(10);

            locationKeyTextView = (TextView) layout.getChildAt(11);
            locationValueTextView = (TextView) layout.getChildAt(12);

            nameKeyTextView = (TextView) layout.getChildAt(13);
            nameValueTextView = (TextView) layout.getChildAt(14);

            orgSubordinatesKeyTextView = (TextView) layout.getChildAt(15);
            orgSubordinatesValueTextView = (TextView) layout.getChildAt(16);

            orgSuperiorKeyTextView = (TextView) layout.getChildAt(17);
            orgSuperiorValueTextView = (TextView) layout.getChildAt(18);

            orginizationKeyTextView = (TextView) layout.getChildAt(19);
            orginizationValueTextView = (TextView) layout.getChildAt(20);
        }

        resultNumberTextView.setText(String.format("#%d", + position+1));
        userIdKeyTextView.setText("userId");
        userIdValueTextView.setText(result.getUserId());
        currentIncidentIdKeyTextView.setText("currentIncidentId");
        currentIncidentIdValueTextView.setText(result.getCurrentIncidentId());
        heartbeatRecordKeyTextView.setText("heartbeatRecord");
        heartbeatRecordValueTextView.setText(result.getHeartbeatRecord().toString());
        incidentSubordinatesKeyTextView.setText("incidentSubordinates");
        incidentSubordinatesValueTextView.setText(result.getIncidentSubordinates().toString());
        incidentSuperiorKeyTextView.setText("incidentSuperior");
        incidentSuperiorValueTextView.setText(result.getIncidentSuperior());
        locationKeyTextView.setText("location");
        locationValueTextView.setText(result.getLocation().toString());
        nameKeyTextView.setText("name");
        nameValueTextView.setText(result.getName());
        orgSubordinatesKeyTextView.setText("orgSubordinates");
        orgSubordinatesValueTextView.setText(result.getOrgSubordinates().toString());
        orgSuperiorKeyTextView.setText("orgSuperior");
        orgSuperiorValueTextView.setText(result.getOrgSuperior());
        orginizationKeyTextView.setText("orginization");
        orginizationValueTextView.setText(result.getOrginization());
        return layout;
    }
}
