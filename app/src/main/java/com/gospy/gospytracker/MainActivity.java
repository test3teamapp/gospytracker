

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

package com.gospy.gospytracker;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.gospy.gospytracker.utils.Utils;

import java.util.concurrent.TimeUnit;


/**
 * The only activity in this sample. Displays UI widgets for requesting and removing location
 * updates, and for the batched location updates that are reported.
 * <p>
 * "Q" supports three user choices for location:
 * <ul>
 *     <li>Allow all the time</li>
 *     <li>Allow while app is in use, i.e., while app is in foreground</li>
 *     <li>Not allow location</li>
 * </ul>
 * <p>
 * Because this app requires location updates while the app isn't in use to work, i.e., not in the
 * foreground, the app requires the users to approve "all the time" for location access.
 * <p>
 * However, best practice is to handle "all the time" and "while in use" permissions via a
 * foreground service + Notification. This use case is shown in the
 * LocationUpdatesForegroundService sample in this same repo.
 * <p>
 * We still wanted to show an example where the app needs location access "all the time" for its
 * location features to be enabled (this sample).
 * <p>
 * Location updates requested through this activity continue even when the activity is not in the
 * foreground. Note: apps running on "O" devices (regardless of targetSdkVersion) may receive
 * updates less frequently than the interval specified in the {@link LocationRequest} when the app
 * is no longer in the foreground.
 */
public class MainActivity extends FragmentActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;


    // UI Widgets.
    private Button mRequestUpdatesButton;
    private Button mRemoveUpdatesButton;
    private Button mSetDeviceIdButton;
    private TextView mCurrentDeviceIdView;
    private EditText mUpdateDeviceIdView;

    private WorkManager mWorkManager;
    private PowerManager.WakeLock wl;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Utils.setAppContext(this);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mRequestUpdatesButton = (Button) findViewById(R.id.request_updates_button);
        mRemoveUpdatesButton = (Button) findViewById(R.id.remove_updates_button);
        mSetDeviceIdButton = (Button) findViewById(R.id.set_device_id_button);
        ;
        mCurrentDeviceIdView = (TextView) findViewById(R.id.main_activity_current_device_id_txt);
        mUpdateDeviceIdView = (EditText) findViewById(R.id.main_activity_input_device_id_txt);

        // Check if the user revoked runtime permissions.
        if (!checkPermissions()) {
            requestPermissions();
        }

        if (Utils.isNetwork(this)) {
            Utils.getSettingsUpdate();
        }
        if (Utils.getSPStringValue(this, Utils.KEY_TRACKED_DEVICE_APP_UID).equals(Utils.defaultDeviceAppUID)) {
            Utils.generateDeviceAppUID(this);
        }
        // update the text views for the device id
        mCurrentDeviceIdView.setText(this.getString(R.string.current_device_id) +
                " : " + Utils.getSPStringValue(this, Utils.KEY_TRACKED_DEVICE_APP_UID));


        // Get token
        // [START retrieve_current_token]
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.i(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        // Log and toast
                        String msg = getString(R.string.msg_token_fmt, token);
                        Log.i(TAG, msg);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
        // [END retrieve_current_token]

        // firebase notification channel -- Οβσολετε. We handle the full message in the FBMessageService class

       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }*/

        // If a notification message is tapped, any data accompanying the notification
        // message is available in the intent extras. In this sample the launcher
        // intent is fired when the notification is tapped, so any accompanying data would
        // be handled here. If you want a different intent fired, set the click_action
        // field of the notification message to the desired intent. The launcher intent
        // is used when no click_action is specified.
        //
        // Handle possible data accompanying notification message.
        // [START handle_data_extras]
       /* if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.i(TAG, "Key: " + key + " Value: " + value);
            }
        }*/
        // [END handle_data_extras]

    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        Utils.setAppContext(this);
        updateButtonsState(Utils.getSPBooleanValue(this, Utils.IS_KEY_LOCATION_UPDATES_REQUESTED));
        // update the text views for the device id
        mCurrentDeviceIdView.setText(this.getString(R.string.current_device_id) +
                " : " + Utils.getSPStringValue(this, Utils.KEY_TRACKED_DEVICE_APP_UID));
        if (Utils.isNetwork(this)) {
            Utils.getSettingsUpdate();
        }

    }

    @Override
    protected void onStop() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    public void setDeviceId(View view) {
        if (this.mUpdateDeviceIdView.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please provide an identifier for the device", Toast.LENGTH_SHORT).show();
        } else {
            Utils.setSPStringValue(this, Utils.KEY_TRACKED_DEVICE_APP_UID, this.mUpdateDeviceIdView.getText().toString());
        }
        // update the text views for the device id
        mCurrentDeviceIdView.setText(this.getString(R.string.current_device_id) +
                " : " + Utils.getSPStringValue(this, Utils.KEY_TRACKED_DEVICE_APP_UID));

        // sent an event to google analytics
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, this.getString(R.string.current_device_id));
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, Utils.getSPStringValue(this, Utils.KEY_TRACKED_DEVICE_APP_UID));
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "string");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    /**
     * Handles the Request Updates button and requests start of location updates.
     */
    public void requestLocationUpdates(View view) {

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
                new PeriodicWorkRequest.Builder(MainWorker.class, 2, TimeUnit.MINUTES)
                        // Constraints
                        .setConstraints(constraints)
                        // tag fro removing the work if needed
                        .addTag("periodicLuWorkRequest")
                        .build();

        WorkManager.getInstance(this).enqueue(luRequest);
        Utils.setSPBooleanValue(this, Utils.IS_KEY_LOCATION_UPDATES_REQUESTED, true);
        // }


    }

    /**
     * Handles the Remove Updates button, and requests removal of location updates.
     */
    public void removeLocationUpdates(View view) {

        /*if (wl != null) {
            if (wl.isHeld()){
                wl.release();
            }
        }*/
        WorkManager.getInstance(this).cancelAllWorkByTag("periodicLuWorkRequest");
        Utils.setSPBooleanValue(this, Utils.IS_KEY_LOCATION_UPDATES_REQUESTED, false);
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int fineLocationPermissionState = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION);

        int backgroundLocationPermissionState = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_BACKGROUND_LOCATION);

        int coarseLocationPermissionState = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION);

        int internetPermissionState = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.INTERNET);

        int phonestatePermissionState = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.READ_PHONE_STATE);

        int networkstatePermissionState = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_NETWORK_STATE);

        int wakelockPermissionState = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.WAKE_LOCK);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return false;
        }

        return (fineLocationPermissionState == PackageManager.PERMISSION_GRANTED) &&
                (backgroundLocationPermissionState == PackageManager.PERMISSION_GRANTED) &&
                (coarseLocationPermissionState == PackageManager.PERMISSION_GRANTED) &&
                (internetPermissionState == PackageManager.PERMISSION_GRANTED) &&
                (phonestatePermissionState == PackageManager.PERMISSION_GRANTED) &&
                (networkstatePermissionState == PackageManager.PERMISSION_GRANTED) &&
                (wakelockPermissionState == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermissions() {

        boolean permissionAccessFineLocationApproved =
                ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;

        boolean backgroundLocationPermissionApproved =
                ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;

        boolean coarseLocationPermissionApproved =
                ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;

        boolean internetPermissionApproved =
                ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.INTERNET)
                        == PackageManager.PERMISSION_GRANTED;

        boolean phonestatePermissionApproved =
                ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.READ_PHONE_STATE)
                        == PackageManager.PERMISSION_GRANTED;

        boolean networkstatePermissionApproved =
                ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_NETWORK_STATE)
                        == PackageManager.PERMISSION_GRANTED;

        boolean wakelockPermissionApproved =
                ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.WAKE_LOCK)
                        == PackageManager.PERMISSION_GRANTED;

        boolean shouldProvideRationale =
                permissionAccessFineLocationApproved && backgroundLocationPermissionApproved &&
                        coarseLocationPermissionApproved && internetPermissionApproved && networkstatePermissionApproved;

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    findViewById(R.id.activity_main),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION,
                                            Manifest.permission.INTERNET,
                                            Manifest.permission.READ_PHONE_STATE,
                                            Manifest.permission.ACCESS_NETWORK_STATE,
                                            Manifest.permission.WAKE_LOCK},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.INTERNET,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.ACCESS_NETWORK_STATE,
                            Manifest.permission.WAKE_LOCK},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");

            } else if ((grantResults[0] == PackageManager.PERMISSION_GRANTED) &&
                    (grantResults[1] == PackageManager.PERMISSION_GRANTED) &&
                    (grantResults[2] == PackageManager.PERMISSION_GRANTED) &&
                    (grantResults[3] == PackageManager.PERMISSION_GRANTED) &&
                    (grantResults[4] == PackageManager.PERMISSION_GRANTED) &&
                    (grantResults[5] == PackageManager.PERMISSION_GRANTED) &&
                    (grantResults[6] == PackageManager.PERMISSION_GRANTED)
            ) {
                // Permission was granted.

            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                Snackbar.make(
                        findViewById(R.id.activity_main),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(Utils.KEY_LOCATION_UPDATES_RESULT)) {
            //mLocationUpdatesResultView.setText(Utils.getLocationUpdatesResult(this));
        } else if (s.equals(Utils.KEY_IS_LOCATION_UPDATES_REQUESTED)) {
            updateButtonsState(Utils.getSPBooleanValue(this, Utils.IS_KEY_LOCATION_UPDATES_REQUESTED));
        }
    }


    /**
     * Ensures that only one button is enabled at any time. The Start Updates button is enabled
     * if the user is not requesting location updates. The Stop Updates button is enabled if the
     * user is requesting location updates.
     */
    private void updateButtonsState(boolean requestingLocationUpdates) {
        if (requestingLocationUpdates) {
            mRequestUpdatesButton.setEnabled(false);
            mRemoveUpdatesButton.setEnabled(true);
        } else {
            mRequestUpdatesButton.setEnabled(true);
            mRemoveUpdatesButton.setEnabled(false);
        }
    }
}

