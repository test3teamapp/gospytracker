package com.gospy.gospytracker.firebase;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.gospy.gospytracker.LocationUpdateProvider;

public class FBMessageWorker extends Worker{

    private static final String TAG = "FBMessageWorker";

    public FBMessageWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i(TAG, "Performing long running task in scheduled job");
        // TODO(developer): add long running task here.
        // request location update
        LocationUpdateProvider.getSingletonLocationUpdateProvider(super.getApplicationContext()).requestLocationUpdates();

        return Result.success();
    }
}
