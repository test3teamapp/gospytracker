package com.gospy.gospytracker;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.gospy.gospytracker.receivers.AlarmReceiver;
import com.gospy.gospytracker.receivers.LocationUpdatesBroadcastReceiver;
import com.gospy.gospytracker.utils.Utils;

import java.util.Calendar;

public class MainWorker extends Worker {


    private static final String TAG = "MainWorker";

    public MainWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @SuppressLint("MissingPermission")
    @Override
    public Result doWork() {

        // Do the work here--in this case, upload the images.

        LocationUpdateProvider.getSingletonLocationUpdateProvider().requestLocationUpdates();

        Utils.sendPingToServer(Utils.PING_REASONS.PING_HI);
        // Indicate whether the work finished successfully with the Result
        return Result.success();
    }
}
