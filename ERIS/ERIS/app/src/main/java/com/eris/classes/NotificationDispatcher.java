package com.eris.classes;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;

import com.eris.R;

/**
 * Static methods to send notifications
 */

public class NotificationDispatcher {

    public static int send(
            int icon, String title, String text, Context context, PendingIntent intent) {
        String phonePref = context.getResources().getString(R.string.preferences_phone_alerts);
        String settingsFile =
                context.getResources().getString(R.string.sharedpreferences_user_settings);
        SharedPreferences settings = context.getSharedPreferences(settingsFile, 0);

        String idColumn = context.getResources().getString(R.string.preferences_notification_id);
        String idFile =
                context.getResources().getString(R.string.sharedpreferences_notification_info);
        SharedPreferences ids = context.getSharedPreferences(idFile, 0);

        int id = ids.getInt(idColumn, 0) + 1;
        SharedPreferences.Editor editor = ids.edit();
        editor.putInt(idColumn, id);
        editor.commit();

        if (settings.getBoolean(phonePref, true)) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(icon)
                            .setContentTitle(title)
                            .setContentText(text);
            if (intent != null) {
                mBuilder.setContentIntent(intent);
            }
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(id, mBuilder.build());
        }

        return id;
    }

    public static int send(String title, String text, Context context) {
        return send(R.mipmap.ic_launcher, title, text, context, null);
    }

    public static int send(String title, String text, Context context, PendingIntent intent) {
        return send(R.mipmap.ic_launcher, title, text, context, intent);
    }

    public static int send(int icon, String title, String text, Context context) {
        return send(icon, title, text, context, null);
    }
}
