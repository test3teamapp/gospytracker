package com.gospy.gospytracker.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.gospy.gospytracker.LocationUpdateProvider;
import com.gospy.gospytracker.Spyapp;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class UniqueRepeatableLUWorker extends Worker {


    private static final String TAG = UniqueRepeatableLUWorker.class.getSimpleName();


    public UniqueRepeatableLUWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {

        // Do the work here--in this case, upload the images.

        LocationUpdateProvider.getSingletonLocationUpdateProvider().requestLocationUpdates();


        new StartUniqueRepeatableLUWorkerAsyncTask().execute();


        return Result.success();
    }

}
