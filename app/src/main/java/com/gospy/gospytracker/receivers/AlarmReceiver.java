package com.gospy.gospytracker.receivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.gospy.gospytracker.LocationUpdateProvider;
import com.gospy.gospytracker.MainWorker;
import com.gospy.gospytracker.Spyapp;
import com.gospy.gospytracker.utils.PostToServerWorker;
import com.gospy.gospytracker.utils.Utils;

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
        // we use a worker
        // hopefully it will wake up the device to get access to the network
        WorkRequest requestLURequest =
                new OneTimeWorkRequest.Builder(MainWorker.class)
                        .build();

        WorkManager
                .getInstance(Spyapp.getContext())
                .enqueue(requestLURequest);

        //LocationUpdateProvider.getSingletonLocationUpdateProvider().requestLocationUpdates();

    }

}
