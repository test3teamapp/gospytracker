/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gospy.gospytracker.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;

import com.google.android.gms.location.LocationResult;
import com.gospy.gospytracker.utils.Utils;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;


import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

/**
 * Receiver for handling location updates.
 * <p>
 * For apps targeting API level O
 * {@link android.app.PendingIntent#getBroadcast(Context, int, Intent, int)} should be used when
 * requesting location updates. Due to limits on background services,
 * {@link android.app.PendingIntent#getService(Context, int, Intent, int)} should not be used.
 * <p>
 * Note: Apps running on "O" devices (regardless of targetSdkVersion) may receive updates
 * less frequently than the interval specified in the
 * {@link com.google.android.gms.location.LocationRequest} when the app is no longer in the
 * foreground.
 */
public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_PROCESS_UPDATES =
            "com.gospy.gospytracker.action" +
                    ".PROCESS_UPDATES";
    private static final String TAG = "LUBroadcastReceiver";

    private static PowerManager.WakeLock mWakeLockForLU = null;

    public static PowerManager.WakeLock getmWakeLockForLU() {
        return mWakeLockForLU;
    }

    public static void setmWakeLockForLU(PowerManager.WakeLock wakeLockForLU) {
        mWakeLockForLU = wakeLockForLU;
    }



    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PROCESS_UPDATES.equals(action)) {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    List<Location> locations = result.getLocations();
                    if (!locations.isEmpty()) {
                        for (Location location : locations
                        ) {
                            //final Location  finalLocation = location;
                            try {
                                String jsonData = Utils.getLocationResultJson(location);
                                String urlString = "http://" + Utils.getSPStringValue(Utils.KEY_SERVER_IP) +
                                        ":8080/api/v1/user/" +
                                URLEncoder.encode(Utils.getSPStringValue(Utils.KEY_TRACKED_DEVICE_APP_UID), "UTF-8") + "/jsonload/" +
                                        URLEncoder.encode(jsonData,"UTF-8") + "?lat=" + location.getLatitude() +
                                                                        "&lng="+ location.getLongitude();
                                //String urlString = "http://192.168.1.5:8080/api/v1/user/" +
                                //        Utils.getDeviceAppUID() + "/jsonload/" + jsonData + "?lat=" + location.getLatitude() +
                                //        "&lng="+ location.getLongitude()
                                // The factory instance is re-useable and thread safe.

                                // needs new thread
//                                new AsyncTask<String,Void, Void>() {
//
//                                    @Override
//                                    protected Void doInBackground(String... params) {
//                                        Twitter twitter = TwitterFactory.getSingleton();
//                                        try {
//                                            twitter4j.Status status = twitter.updateStatus(Utils.getSPStringValue(Utils.KEY_TRACKED_DEVICE_APP_UID) + " : " + params[0]);
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                        //System.out.println("Successfully updated the status to [" + status.getText() + "].");
//                                        return null;
//                                    }
//
//                                    @Override
//                                    protected void onPostExecute(Void result) {
//
//                                    }
//                                }.execute(jsonData);




                                Utils.postDataToServer(urlString, mWakeLockForLU);

                            } catch (JSONException | UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }

                        Utils.storeSPLocationUpdatesResult(locations);
                        //Utils.sendNotification(context, Utils.getLocationResultTitle(context, locations));
                        Log.i(TAG, Utils.getSPLocationUpdatesResult());

                    }

                }
            }
        }
    }
}
