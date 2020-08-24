package com.gospy.gospytracker.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.gospy.gospytracker.LocationUpdateProvider;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String ACTION_PROCESS_ALARM =
            "com.gospy.gospytracker.action" +
                    ".PROCESS_ALARM";
    private static final String TAG = AlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context arg0, Intent arg1) {
        // For our recurring task, we'll request a location update
        //Toast.makeText(arg0, "I'm running", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "alarm received --> trigger lu ");
        // request location update
        LocationUpdateProvider.getSingletonLocationUpdateProvider(arg0).requestLocationUpdates();
    }

}
