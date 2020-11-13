package com.gospy.gospytracker.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.gospy.gospytracker.GeofencesProvider;
import com.gospy.gospytracker.LocationUpdateProvider;
import com.gospy.gospytracker.utils.Utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class GeofenceBroadcastReceiver  extends BroadcastReceiver {

    private static final String TAG = GeofenceBroadcastReceiver.class.getSimpleName();

    public static final String ACTION_PROCESS_GEOFENCE_UPDATE =
            "com.gospy.gospytracker.action" +
                    ".ACTION_PROCESS_GEOFENCE_UPDATE";


    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceStatusCodes
                    .getStatusCodeString(geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER){

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            Log.i(TAG, "Number of Geofences triggered : " + triggeringGeofences.size());

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );

            // Send notification and log the transition details.
            //Utils.sendNotification(geofenceTransitionDetails);
            Log.i(TAG, geofenceTransitionDetails);
            ArrayList<Double> coords = getGeofenceCoords(triggeringGeofences);
            // post the coords to the server. we have no jason data
            String jsonData = "{}";
            String urlString = null;
            try {
                urlString = "http://" + Utils.getSPStringValue(Utils.KEY_SERVER_IP) +
                        ":8080/api/v1/user/" +
                        URLEncoder.encode(Utils.getSPStringValue(Utils.KEY_TRACKED_DEVICE_APP_UID), "UTF-8") + "/jsonload/" +
                        URLEncoder.encode(jsonData,"UTF-8") + "?lat=" + coords.get(0) +
                        "&lng="+coords.get(1);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            //String urlString = "http://192.168.1.5:8080/api/v1/user/" +
            //        Utils.getDeviceAppUID() + "/jsonload/" + jsonData + "?lat=" + location.getLatitude() +
            //        "&lng="+ location.getLongitude()
            // The factory instance is re-useable and thread safe.

            Utils.postDataToServer(urlString);
            // reset the geofence to create field around our new location
            GeofencesProvider.getSingletonLocationUpdateProvider().setGeofencesAroundPoint(coords.get(0), coords.get(1));

        } else {
            // Log the error.
            Log.e(TAG, "Invalid Geofence Transition : " + geofenceTransition);
        }
    }

    private String getGeofenceTransitionDetails(GeofenceBroadcastReceiver geofenceBroadcastReceiver, int geofenceTransition, List<Geofence> triggeringGeofences) {

        return new String("GeoFence : " + triggeringGeofences.get(0).getRequestId() + " triggered transition : " + geofenceTransition);
    }

    private ArrayList<Double> getGeofenceCoords(List<Geofence> triggeringGeofences) {

        ArrayList<Double> coords = new ArrayList<Double>();
        // split the request id (lat/lng) to get the coords
        try {

            String[] strCoords = triggeringGeofences.get(0).getRequestId().split("/");
            if (!Double.valueOf(strCoords[0]).isNaN()) {
                coords.add(Double.valueOf(strCoords[0]));
            }
            if (!Double.valueOf(strCoords[1]).isNaN()) {
                coords.add(Double.valueOf(strCoords[1]));
            }
        }catch (Exception exc){
            Log.i(TAG,exc.toString());
        }
        return coords;
    }

}
