package com.gospy.gospytracker.firebase;

import android.app.PendingIntent;
import android.content.Context;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.gospy.gospytracker.LocationUpdateProvider;
import com.gospy.gospytracker.Spyapp;
import com.gospy.gospytracker.receivers.LocationUpdatesBroadcastReceiver;
import com.gospy.gospytracker.utils.Utils;

import java.sql.Time;

public class FBMessageWorker extends Worker {

    private static final String TAG = "FBMessageWorker";

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequestGSM = null;
    private LocationRequest mLocationRequestGPS = null;
    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient = null;
    Intent mIntent = null;

    public FBMessageWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i(TAG, "Performing long running task in scheduled job");
        // TODO(developer): add long running task here.
        // checking -> forcing network to come up
        //Utils.isNetwork();
        // request location update
        // LocationUpdateProvider.getSingletonLocationUpdateProvider().requestLocationUpdates();


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(Spyapp.getContext());


        if (mLocationRequestGSM == null || mLocationRequestGPS == null) {
            mLocationRequestGSM = new LocationRequest();
            mLocationRequestGPS = new LocationRequest();
        }

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        // Note: apps running on "O" devices (regardless of targetSdkVersion) may receive updates
        // less frequently than this interval when the app is no longer in the foreground.
        mLocationRequestGSM.setInterval(20000);
        mLocationRequestGPS.setInterval(20000);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequestGSM.setFastestInterval(20000);
        mLocationRequestGPS.setFastestInterval(20000);

        if (mIntent == null) {
            mIntent = new Intent(Spyapp.getContext(), LocationUpdatesBroadcastReceiver.class);
        }
        mIntent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        PendingIntent pi = PendingIntent.getBroadcast(Spyapp.getContext(), 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // this is where the 2 requests differ
        mLocationRequestGSM.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequestGPS.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Sets the maximum time when batched location updates are delivered. Updates may be
        // delivered sooner than this interval.
        mLocationRequestGSM.setMaxWaitTime(60000);
        mLocationRequestGPS.setMaxWaitTime(60000);

        if (mFusedLocationClient != null) {
            try {
                PowerManager pm = (PowerManager) Spyapp.getContext().getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        "GoSpyTracker:WakelockForLU");
                wl.acquire();

                if (wl.isHeld()) {
                    Log.i(TAG, "Starting location updates");
                    LocationUpdatesBroadcastReceiver.setmWakeLockForLU(wl);

                    mFusedLocationClient.requestLocationUpdates(mLocationRequestGSM, pi);
                    mFusedLocationClient.requestLocationUpdates(mLocationRequestGPS, pi);
                }
            } catch (SecurityException e) {

                e.printStackTrace();
            }
        }

        // delay return

        long currentTime = System.currentTimeMillis();
        while (System.currentTimeMillis() < currentTime + 6000){
           // Log.i(TAG,"tik tok");
        }

        /*Looper.prepare();

        new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {
                Log.i(TAG, "tick " + millisUntilFinished);
            }

            public void onFinish() {

            }
        }.start();

        Looper.loop();*/

        return Result.success();
    }
}
