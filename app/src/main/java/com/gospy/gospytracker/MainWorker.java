package com.gospy.gospytracker;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.gospy.gospytracker.receivers.AlarmReceiver;
import com.gospy.gospytracker.receivers.LocationUpdatesBroadcastReceiver;
import com.gospy.gospytracker.utils.StartUniqueRepeatableLUWorkerAsyncTask;
import com.gospy.gospytracker.utils.Utils;

import java.util.Calendar;

public class MainWorker extends Worker {


    private static final String TAG = MainWorker.class.getSimpleName();


    public MainWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {

        // Do the work here--in this case, upload the images.

        LocationUpdateProvider.getSingletonLocationUpdateProvider().requestLocationUpdates();

        // in case it has stopped
        // restart the every2minutes repeated worker for triggering LU
        new StartUniqueRepeatableLUWorkerAsyncTask().execute();

        return Result.success();
    }
}
