package com.globalapp.aswandriver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.kinvey.android.push.KinveyGCMService;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Smiley on 7/6/2016.
 */
public class GCMService extends KinveyGCMService {

    private static long time;

    @Override
    public void onMessage(String message) {
        if (time + 3000 > System.currentTimeMillis()) {
            return;
        }
        time = System.currentTimeMillis();

        Intent trip = new Intent(getApplicationContext(), TripActivity.class);
        try {
            JSONObject details = new JSONObject(message);
            TripActivity.CustomerName = details.getString("user_phone");
            MapActivity.CustomerName = details.getString("user_phone");
            TripActivity.CustomerDes = details.getString("user_dist");
            TripActivity.ID = details.getString("_id");
            TripActivity.CustomerGeo = details.getString("user_lat") + "," + details.getString("user_long");

            trip.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(trip);
            return;
        } catch (Exception ex) {
            Log.e("error", ex.getMessage());
        }
        displayNotification(message);

    }

    @Override
    public void onError(String error) {
        displayNotification(error);
    }

    @Override
    public void onDelete(String deleted) {
        displayNotification(deleted);
    }

    @Override
    public void onRegistered(String gcmID) {
        displayNotification(gcmID);
    }

    @Override
    public void onUnregistered(String oldID) {
        displayNotification(oldID);
    }

    //This method will return the WakefulBroadcastReceiver class you define in the next step
    public Class getReceiver() {
        return GCMReceiver.class;
    }

    private void displayNotification(String message) {


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notify)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle(getApplicationContext().getResources().getString(R.string.app_name))

                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentText(message);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }
}
