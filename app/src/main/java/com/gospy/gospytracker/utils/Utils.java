
package com.gospy.gospytracker.utils;

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

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.gospy.gospytracker.LocationUpdateProvider;
import com.gospy.gospytracker.MainActivity;
import com.gospy.gospytracker.MainWorker;
import com.gospy.gospytracker.R;
import com.gospy.gospytracker.receivers.AlarmReceiver;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility methods used in this sample.
 */
public class Utils {

    public static Context mAppContext;
    public final static String KEY_IS_LOCATION_UPDATES_REQUESTED = "location-updates-requested";
    public final static String KEY_LOCATION_UPDATES_RESULT = "location-update-result";
    public final static String KEY_SERVER_IP = "server-ip";
    public final static String KEY_TRACKED_USER_ID = "tracked-user-id";
    public final static String KEY_TRACKED_DEVICE_APP_UID = "tracked-device-app-uid";
    public final static String KEY_CURRENT_DEVICE_FIREBASE_UID = "tracked-device-firebase-uid";

    public final static String KEY_IS_USER_MOVING = "user-is-moving";
    public final static String KEY_IS_GEOFENCE_SET = "geofence-is-set";
    public final static String KEY_LAST_KNOWN_LOCATION = "last-location";
    public final static String IS_KEY_LOCATION_UPDATES_REQUESTED = "location-updates-requested";

    public final static String CHANNEL_ID = "channel_01";
    private static final String TAG = Utils.class.getSimpleName();

    public final static String mDefaultDeviceAppUID = "UNKNOWN";
    public final static String mDefaultUserID = "UNKNOWN_USER_ID";
    public final static String mDefaultServerIp = "158.101.171.124";
    private static final String mDefaultTAGLuWorker = "periodicLuWorkRequest";

    private static WorkManager mWorkManager;
    private static PowerManager.WakeLock mWakeLock;
    private static AlarmManager mAlarmManager;
    private static PendingIntent mAlarmPendingIntent;

    // location updates interval - 10sec
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    // fastest updates interval - 5 sec
    // location updates will be received if another app is requesting the locations
    // than your app can handle
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5000;

    private static final int REQUEST_CHECK_SETTINGS = 100;
    private static final int ALARM_NOTIFICATION_ID = 101;

    static public void setmAppContext(Context ctx){
        mAppContext = ctx;
    }

    static public void generateDeviceAppUID(Context context) {

        if (getSPStringValue(mAppContext, Utils.KEY_TRACKED_DEVICE_APP_UID).equals(mDefaultDeviceAppUID)) {
            try {
                final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

                String tmDevice, tmSerial, androidId, deviceAppUID;

                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                tmDevice = "" + tm.getDeviceId();
                tmSerial = "" + tm.getSimSerialNumber();
                androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

                deviceAppUID = androidId + tmDevice + tmSerial;

                setSPStringValue(mAppContext, Utils.KEY_TRACKED_DEVICE_APP_UID, deviceAppUID);

            } catch (Exception exc) {
                Log.i(TAG, exc.toString());
            }
        }

    }

    public static void setSPStringValue(Context context, String key,String value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(key, value)
                .apply();
    }

    public static String getSPStringValue(Context context, String key) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(key, mDefaultDeviceAppUID);
    }

    public static void setSPBooleanValue(Context context, String key, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(key, value)
                .apply();
    }

    public static boolean getSPBooleanValue(Context context, String key) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(key, false);
    }

    public static void setSPLongValue(Context context, String key, long value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putLong(key, value)
                .apply();
    }

    public static long getSPLongValue(Context context, String key) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(key, -1);
    }




    public static boolean isNetwork(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }



    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    public static void sendNotification(Context context, String notificationDetails) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(context, MainActivity.class);

        notificationIntent.putExtra("from_notification", true);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        // Define the notification settings.
        builder.setSmallIcon(R.mipmap.ic_launcher)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.mipmap.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle("Location update")
                .setContentText(notificationDetails)
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);

            // Channel ID
            builder.setChannelId(CHANNEL_ID);
        }

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }


    /**
     * Returns the title for reporting about a list of {@link Location} objects.
     *
     * @param context The {@link Context}.
     */
    public static String getLocationResultTitle(Context context, List<Location> locations) {
        String numLocationsReported = context.getResources().getQuantityString(
                R.plurals.num_locations_reported, locations.size(), locations.size());
        return numLocationsReported + ": " + DateFormat.getDateTimeInstance().format(new Date());
    }

    /**
     * Returns the text for reporting about a list of  {@link Location} objects.
     *
     * @param locations List of {@link Location}s.
     */
    private static String getLocationResultText(Context context, List<Location> locations) {
        if (locations.isEmpty()) {
            return context.getString(R.string.unknown_location);
        }
        StringBuilder sb = new StringBuilder();
        for (Location location : locations) {
            sb.append("(");
            sb.append(location.getLatitude());
            sb.append(", ");
            sb.append(location.getLongitude());
            sb.append(", ");
            sb.append(location.getAltitude());
            sb.append(", ");
            sb.append(location.hasSpeed());
            sb.append(", ");
            sb.append(location.getSpeed());
            sb.append(", ");
            Date d = new Date(location.getTime());
            sb.append(DateFormat.getDateTimeInstance().format(d));
            sb.append(")");
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Returns a json string  for reporting about a list of  {@link Location} objects.
     *
     * @param location List of {@link Location}s.
     */
    public static String getLocationResultJson(Context context, Location location) throws JSONException {

        JSONObject json = new JSONObject();
        json.put("Lat", location.getLatitude());
        json.put("Lng", location.getLongitude());
        json.put("Alt", location.getAltitude());
        json.put("hasSpeed", location.hasSpeed());
        json.put("Speed", location.getSpeed());
        Date d = new Date(location.getTime());
        json.put("Time",DateFormat.getDateTimeInstance().format(d));

        return json.toString();
    }

    public static void storeSPLocationUpdatesResult(Context context, List<Location> locations) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(KEY_LOCATION_UPDATES_RESULT, getLocationResultTitle(context, locations)
                        + "\n" + getLocationResultText(context, locations))
                .apply();
    }

    public static String getSPLocationUpdatesResult(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_LOCATION_UPDATES_RESULT, "");
    }

    /**
     * Returns the latest settings from the remote repository.
     * Settings are in json format
     */
    public static void getSettingsUpdate() {

        new SettingsGrabber().execute(); //execute the asynctask

    }

    /**
     * Post data to server
     */
    public static void postDataToServer(Context ctx, String url) {

        try {
            // Instantiate the RequestQueue.
            RequestQueue queue = VolleyHttpRequestQueueSingleton.getInstance(ctx).
                    getRequestQueue();

            // Request a string response from the provided URL.
            if (Utils.isNetwork(ctx)) {
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.i(TAG, response);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i(TAG, error.toString());
                    }
                });

                // Add the request to the RequestQueue.
                queue.add(stringRequest);
                //queue.start();
            } else {
                Log.i(TAG, "No network");
            }
        }catch (Exception exc){
            exc.printStackTrace();
        }
    }

    /**
     *  requests start of location updates with a Worker (minimum @ every 15 minutes).
     */
    public static void requestLocationUpdates() {

        // PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        //  wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
        //         "GoSpyTracker:WakelockForLU");
        // wl.acquire();

        //if (wl.isHeld()) {
           /* WorkRequest uploadWorkRequest =
                new OneTimeWorkRequest.Builder(MainWorker.class)
                        .build();*/

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest luRequest =
                new PeriodicWorkRequest.Builder(MainWorker.class, 2, TimeUnit.MINUTES) // can not be less than 15 minuts. this 2 minutes setting is worthless
                        // Constraints
                        .setConstraints(constraints)
                        // tag fro removing the work if needed
                        .addTag(mDefaultTAGLuWorker)
                        .build();

        WorkManager.getInstance(mAppContext).enqueue(luRequest);

        // minimum repeating interval for worker is 15 minutes.
        // So, we start an alarm for every (X) minutes to get more LUs
        // this is also helpfull since after a reboot alarms are not triggered, if we were to use only alarms

        Utils.setAlarmForPeriodicLU(1000 * 60 * 2); // every 2 minutes

        Utils.setSPBooleanValue(mAppContext, Utils.IS_KEY_LOCATION_UPDATES_REQUESTED, true);
        // }



    }

    public static void setAlarmForPeriodicLU(int milliseconds){

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MINUTE, 1);

        Intent intent = new Intent(mAppContext, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_PROCESS_ALARM);
        mAlarmPendingIntent = PendingIntent.getBroadcast(mAppContext, ALARM_NOTIFICATION_ID,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //alarm manager can be retrieved from static content
        if (mAlarmManager == null) {
            mAlarmManager = (AlarmManager) mAppContext.getSystemService(mAppContext.ALARM_SERVICE);
        }
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), milliseconds, mAlarmPendingIntent);


    }

    /**
     *  requests removal of location updates.
     */
    public static void removeLocationUpdates() {

        /*if (wl != null) {
            if (wl.isHeld()){
                wl.release();
            }
        }*/
        // cancel LU Worker
        WorkManager.getInstance(mAppContext).cancelAllWorkByTag(mDefaultTAGLuWorker);

        // cancel the alarm as well
        Intent intent = new Intent(mAppContext, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_PROCESS_ALARM);
        mAlarmPendingIntent = PendingIntent.getBroadcast(mAppContext, ALARM_NOTIFICATION_ID,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //alarm manager can be retrieved from static content
        if (mAlarmManager == null) {
            mAlarmManager = (AlarmManager) mAppContext.getSystemService(mAppContext.ALARM_SERVICE);
        }
        mAlarmManager.cancel(mAlarmPendingIntent);

        // cancel any fused location updates already working

        LocationUpdateProvider.getSingletonLocationUpdateProvider(mAppContext).removeLocationUpdates();

        Utils.setSPBooleanValue(mAppContext, Utils.IS_KEY_LOCATION_UPDATES_REQUESTED, false);
    }

    /**
     * Persist token to third-party servers.
     * <p>
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    public static void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
        try {
            String urlString = "http://" + Utils.getSPStringValue(mAppContext, Utils.KEY_SERVER_IP) +
                    ":8080/api/v1/user/" +
                    URLEncoder.encode(Utils.getSPStringValue(
                            mAppContext, Utils.KEY_TRACKED_DEVICE_APP_UID), "UTF-8") +
                    "/token/" + token;

            Utils.postDataToServer(mAppContext, urlString);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}

