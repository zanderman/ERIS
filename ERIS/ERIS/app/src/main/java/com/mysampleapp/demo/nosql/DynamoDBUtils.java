package com.mysampleapp.demo.nosql;

import android.app.AlertDialog;
import android.support.v4.app.FragmentActivity;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.util.ThreadUtils;

import java.util.HashMap;
import java.util.Map;

public final class DynamoDBUtils {
    private static final String DDB_TYPE_STRING = "S";
    private static final String DDB_TYPE_NUMBER = "N";
    private static final String DDB_TYPE_BINARY = "B";
    private static final String TYPE_STRING = "String";
    private static final String TYPE_NUMBER = "Number";
    private static final String TYPE_BINARY = "Binary";
    private static final Map<String, String> typeLookup;
    static {
        typeLookup = new HashMap<>();
        typeLookup.put(DDB_TYPE_STRING, TYPE_STRING);
        typeLookup.put(DDB_TYPE_NUMBER, TYPE_NUMBER);
        typeLookup.put(DDB_TYPE_BINARY, TYPE_BINARY);
    }

    /**
     * Utility class has private constructor.
     */
    private DynamoDBUtils() {
    }

    /**
     * Convert a DynamoDB attribute type to a human readable type.
     * @param attributeType the DynamoDB attribute type
     * @return a human readable attribute type.
     */
    public static String humanReadableTypeFromDynamoDBAttributeType(final String attributeType) {
        final String humanReadableType;
        humanReadableType = typeLookup.get(attributeType);
        if (humanReadableType != null) {
            return humanReadableType;
        }
        return attributeType;
    }

    public static void showErrorDialogForServiceException(final FragmentActivity activity,
                                                                 final String title,
                                                                 final AmazonClientException ex) {
        if (activity == null) {
            return;
        }

        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(activity)
                    .setTitle(title)
                    .setMessage(ex.getMessage())
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            }
        });
    }
}
