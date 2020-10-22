package com.gospy.gospytracker;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.gospy.gospytracker.receivers.LocationUpdatesBroadcastReceiver;

public class LocationUpdateProvider {

    private static final String TAG = LocationUpdateProvider.class.getSimpleName();

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL = 40000; // Every 40 seconds.

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value, but they may be less frequent.
     */
    private static final long FASTEST_UPDATE_INTERVAL = 20000; // Every 20 seconds

    /**
     * The max time before batched results are delivered by location services. Results may be
     * delivered sooner than this interval.
     */
    private static final long MAX_WAIT_TIME =  3 * 60 * 1000; // Every 3 minute.
    private static LocationUpdateProvider mSingletonObject = null;
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

    private LocationUpdateProvider() {

    }

    public static LocationUpdateProvider getSingletonLocationUpdateProvider() {
        if (mSingletonObject == null) {
            mSingletonObject = new LocationUpdateProvider();
        }

        return mSingletonObject;
    }


    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private void createLocationRequest() {
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
        mLocationRequestGSM.setInterval(UPDATE_INTERVAL);
        mLocationRequestGPS.setInterval(UPDATE_INTERVAL);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequestGSM.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        mLocationRequestGPS.setFastestInterval(FASTEST_UPDATE_INTERVAL);

        // this is where the 2 requests differ
        mLocationRequestGSM.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequestGPS.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Sets the maximum time when batched location updates are delivered. Updates may be
        // delivered sooner than this interval.
        mLocationRequestGSM.setMaxWaitTime(MAX_WAIT_TIME);
        mLocationRequestGPS.setMaxWaitTime(MAX_WAIT_TIME);
    }

    private PendingIntent getPendingIntent() {
        // Note: for apps targeting API level 25 ("Nougat") or lower, either
        // PendingIntent.getService() or PendingIntent.getBroadcast() may be used when requesting
        // location updates. For apps targeting API level O, only
        // PendingIntent.getBroadcast() should be used. This is due to the limits placed on services
        // started in the background in "O".

        // TODO(developer): uncomment to use PendingIntent.getService().
//        Intent intent = new Intent(this, LocationUpdatesIntentService.class);
//        intent.setAction(LocationUpdatesIntentService.ACTION_PROCESS_UPDATES);
//        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (mIntent == null) {
            mIntent = new Intent(Spyapp.getContext(), LocationUpdatesBroadcastReceiver.class);
        }
        mIntent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(Spyapp.getContext(), 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Handles the Request Updates button and requests start of location updates.
     * Only access it through the singleton object
     */
    public void requestLocationUpdates() {

        if (mFusedLocationClient == null) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(Spyapp.getContext());
        }

        createLocationRequest();

        if (mFusedLocationClient != null) {
            try {

                    Log.i(TAG, "Starting location updates");
                    //LocationUpdatesBroadcastReceiver.setmWakeLockForLU(wl);
                    mFusedLocationClient.requestLocationUpdates(mLocationRequestGSM, getPendingIntent());
                    //mFusedLocationClient.requestLocationUpdates(mLocationRequestGPS, getPendingIntent());

            } catch (SecurityException e) {

                e.printStackTrace();
            }
        }
    }

    /**
     * requests removal of location updates from the fused lu client.
     * Only access it through the singleton object
     */
    public void removeLocationUpdates() {
        Log.i(TAG, "Removing location updates from fused client");
        if (mFusedLocationClient != null) {
            if (mIntent != null) {
                mFusedLocationClient.removeLocationUpdates(PendingIntent.getBroadcast(Spyapp.getContext(), 0, mIntent, PendingIntent.FLAG_CANCEL_CURRENT));
                Log.i(TAG, "Removed location updates");
            }
        }
    }
}
