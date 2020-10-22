package com.gospy.gospytracker.firebase;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.gospy.gospytracker.LocationUpdateProvider;
import com.gospy.gospytracker.utils.Utils;

public class FBMessageWorkerStartTracking extends Worker {

    private static final String TAG = "FBMessageWorkerStartTracking";

    public FBMessageWorkerStartTracking(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i(TAG, "Performing long running task in scheduled job");
        // TODO(developer): add long running task here.

        Utils.requestLocationUpdates();

        return Result.success();
    }
}
