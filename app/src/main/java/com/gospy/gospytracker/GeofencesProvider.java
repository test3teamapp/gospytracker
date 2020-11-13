package com.gospy.gospytracker;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.gospy.gospytracker.receivers.GeofenceBroadcastReceiver;

import java.util.ArrayList;

public class GeofencesProvider {
    private static final String TAG = GeofencesProvider.class.getSimpleName();

    private static final int GEOFENCE_RADIUS_IN_METERS = 25;


    /**
     * Provides access to the Geofence Provider API.
     */
    private static GeofencesProvider mSingletonObject = null;
    private GeofencingClient mGeofencingClient = null;
    PendingIntent mGeofencePendingIntent = null;

    private GeofencesProvider() {

        mGeofencingClient = LocationServices.getGeofencingClient(Spyapp.getContext());

    }

    public static GeofencesProvider getSingletonLocationUpdateProvider() {
        if (mSingletonObject == null) {
            mSingletonObject = new GeofencesProvider();
        }

        return mSingletonObject;
    }

    @SuppressLint("MissingPermission")
    public void setGeofencesAroundPoint(Location location) {
        setGeofencesAroundPoint(location.getLatitude(), location.getLongitude());
    }

    public void removeAllGeofences() {

        mGeofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences removed
                        Log.i(TAG, "Geofences removed");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to remove geofences
                        Log.i(TAG, "Failed to remove geofences");
                    }
                });
    }

    private ArrayList<Geofence> computeGeofencePoints(double latitude, double longitude) {

        ArrayList<Geofence> geoObjects = new ArrayList<Geofence>();

        int arrayPosition = 0;
        int reduction = 90;
        //1st perimeter
        for (int i = 1; i < 220; i = i + 58) { // 37 gives a circle around a block (80 to 90 meters) (with reduction of 90%)
            // it produces 10 points , with no. 10 overlapping with no. 1
            // with a 25 meters radius for each geolocation point
            // they overlap nicely to cover the whole circle
            double latCoord =
                    latitude + Math.cos(i) / 180 - ((Math.cos(i) / 180) * reduction) / 100;
            double lngCoord =
                    longitude + Math.sin(i) / 180 - ((Math.sin(i) / 180) * reduction) / 100;
            arrayPosition++;
            geoObjects.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    // set a string with the lat / lng so that we can extract the coords later
                    // when the user enters the geofence
                    .setRequestId(String.valueOf(latCoord) + "/" + String.valueOf(lngCoord))
                    .setCircularRegion(
                            latCoord,
                            lngCoord,
                            50
                    )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build());
        }
        //2nd perimeter
        reduction = 70;
        for (int i = 1; i < 320; i = i + 64) { // 37 gives a circle around a block (80 to 90 meters) (with reduction of 90%)
            // it produces 10 points , with no. 10 overlapping with no. 1
            // with a 25 meters radius for each geolocation point
            // they overlap nicely to cover the whole circle
            double latCoord =
                    latitude + Math.cos(i) / 180 - ((Math.cos(i) / 180) * reduction) / 100;
            double lngCoord =
                    longitude + Math.sin(i) / 180 - ((Math.sin(i) / 180) * reduction) / 100;
            arrayPosition++;
            geoObjects.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    // set a string with the lat / lng so that we can extract the coords later
                    // when the user enters the geofence
                    .setRequestId(String.valueOf(latCoord) + "/" + String.valueOf(lngCoord))
                    .setCircularRegion(
                            latCoord,
                            lngCoord,
                            100
                    )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build());

        }
        //3rd perimeter
        reduction = 30;
        for (int i = 1; i < 520; i = i + 43) { // 37 gives a circle around a block (80 to 90 meters) (with reduction of 90%)
            // it produces 10 points , with no. 10 overlapping with no. 1
            // with a 25 meters radius for each geolocation point
            // they overlap nicely to cover the whole circle
            double latCoord =
                    latitude + Math.cos(i) / 180 - ((Math.cos(i) / 180) * reduction) / 100;
            double lngCoord =
                    longitude + Math.sin(i) / 180 - ((Math.sin(i) / 180) * reduction) / 100;
            arrayPosition++;
            geoObjects.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    // set a string with the lat / lng so that we can extract the coords later
                    // when the user enters the geofence
                    .setRequestId(String.valueOf(latCoord) + "/" + String.valueOf(lngCoord))
                    .setCircularRegion(
                            latCoord,
                            lngCoord,
                            150
                    )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build());

        }
        //4th perimeter
        reduction = -10;
        for (int i = 1; i < 780; i = i + 38) { // 37 gives a circle around a block (80 to 90 meters) (with reduction of 90%)
            // it produces 10 points , with no. 10 overlapping with no. 1
            // with a 25 meters radius for each geolocation point
            // they overlap nicely to cover the whole circle
            double latCoord =
                    latitude + Math.cos(i) / 180 - ((Math.cos(i) / 180) * reduction) / 100;
            double lngCoord =
                    longitude + Math.sin(i) / 180 - ((Math.sin(i) / 180) * reduction) / 100;
            arrayPosition++;
            geoObjects.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    // set a string with the lat / lng so that we can extract the coords later
                    // when the user enters the geofence
                    .setRequestId(String.valueOf(latCoord) + "/" + String.valueOf(lngCoord))
                    .setCircularRegion(
                            latCoord,
                            lngCoord,
                            150
                    )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build());

        }
        //5th perimeter
        reduction = -50;
        for (int i = 1; i < 780; i = i + 38) { // 37 gives a circle around a block (80 to 90 meters) (with reduction of 90%)
            // it produces 10 points , with no. 10 overlapping with no. 1
            // with a 25 meters radius for each geolocation point
            // they overlap nicely to cover the whole circle
            double latCoord =
                    latitude + Math.cos(i) / 180 - ((Math.cos(i) / 180) * reduction) / 100;
            double lngCoord =
                    longitude + Math.sin(i) / 180 - ((Math.sin(i) / 180) * reduction) / 100;
            arrayPosition++;
            geoObjects.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    // set a string with the lat / lng so that we can extract the coords later
                    // when the user enters the geofence
                    .setRequestId(String.valueOf(latCoord) + "/" + String.valueOf(lngCoord))
                    .setCircularRegion(
                            latCoord,
                            lngCoord,
                            200
                    )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build());

        }
        //6th perimeter
        reduction = -100;
        for (int i = 1; i < 780; i = i + 38) { // 37 gives a circle around a block (80 to 90 meters) (with reduction of 90%)
            // it produces 10 points , with no. 10 overlapping with no. 1
            // with a 25 meters radius for each geolocation point
            // they overlap nicely to cover the whole circle
            double latCoord =
                    latitude + Math.cos(i) / 180 - ((Math.cos(i) / 180) * reduction) / 100;
            double lngCoord =
                    longitude + Math.sin(i) / 180 - ((Math.sin(i) / 180) * reduction) / 100;
            arrayPosition++;
            geoObjects.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    // set a string with the lat / lng so that we can extract the coords later
                    // when the user enters the geofence
                    .setRequestId(String.valueOf(latCoord) + "/" + String.valueOf(lngCoord))
                    .setCircularRegion(
                            latCoord,
                            lngCoord,
                            200
                    )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build());

        }

        return geoObjects;

    }

    @SuppressLint("MissingPermission")
    public void setGeofencesAroundPoint(double latitude, double longitude) {


        // remove the old ones
        removeAllGeofences();

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(computeGeofencePoints(latitude, longitude));
        GeofencingRequest gfr = builder.build();

        mGeofencingClient.addGeofences(gfr, getGeofencePendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences added
                        Log.i(TAG, "Geofences added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to add geofences
                        Log.i(TAG, "Failed to add geofences");
                    }
                });


    }


    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(Spyapp.getContext(), GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getBroadcast(Spyapp.getContext(), 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }


}
