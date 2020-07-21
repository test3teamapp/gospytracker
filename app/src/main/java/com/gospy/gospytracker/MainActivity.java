

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
import android.app.AlarmManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.LocationRequest;
import com.google.android.material.snackbar.Snackbar;


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
    private TextView mLocationUpdatesResultView;

    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor;
    private Sensor mSigMotion;
    private Sensor mTiltSensor;
    private Sensor mLightSensor;
    private Sensor mLinearAccelarationSensor;
    private Sensor mMotionSensor;
    private Sensor mProximitySensor;
    private Sensor mMagneticFieldSensor;
    private SensorListener mSensorListener;

    private AlarmManager mAlarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRequestUpdatesButton = (Button) findViewById(R.id.request_updates_button);
        mRemoveUpdatesButton = (Button) findViewById(R.id.remove_updates_button);
       // mLocationUpdatesResultView = (TextView) findViewById(R.id.location_updates_result);

        // Check if the user revoked runtime permissions.
        if (!checkPermissions()) {
            requestPermissions();
        }

        if (Utils.isNetwork(this)) {
            Utils.getSettingsUpdate();
        }
        Utils.generateDeviceAppUID(this);

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
        updateButtonsState(Utils.getRequestingLocationUpdates(this));
        if (Utils.isNetwork(this)) {
            Utils.getSettingsUpdate();
        }
        //mLocationUpdatesResultView.setText(Utils.getLocationUpdatesResult(this));

        sensorSetup();
    }

    @Override
    protected void onStop() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
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
                (networkstatePermissionState == PackageManager.PERMISSION_GRANTED);
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
                                            Manifest.permission.ACCESS_NETWORK_STATE},
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
                            Manifest.permission.ACCESS_NETWORK_STATE},
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
                    (grantResults[5] == PackageManager.PERMISSION_GRANTED)
            ) {
                // Permission was granted.
                requestLocationUpdates(null);

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
        } else if (s.equals(Utils.KEY_LOCATION_UPDATES_REQUESTED)) {
            updateButtonsState(Utils.getRequestingLocationUpdates(this));
        }
    }

    public void sensorSetup() {
        //sensor set up
        // Retrieve a PendingIntent that will perform a broadcast
        //alarm manager can be retrieved from static content
        mAlarmManager = (AlarmManager) getSystemService(this.ALARM_SERVICE);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //mSensorListener = SensorListener.getSingletonSensorListener(this, mAlarmManager, mSensorManager);

        assert mSensorManager != null;
        // mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSigMotion = mSensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION,true);
        //mTiltSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        //mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        //mLinearAccelarationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        //mMotionSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MOTION_DETECT);
        //mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        //mMagneticFieldSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        //assert mAccelerometerSensor != null;
        //mSensorManager.registerListener(mSensorListener, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        assert mSigMotion != null;
       // mSensorManager.requestTriggerSensor(mSensorListener, mSigMotion);
        // mSensorManager.registerListener(mSensorListener, mTiltSensor, SensorManager.SENSOR_DELAY_NORMAL);
        // mSensorManager.registerListener(mSensorListener, mLightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        // mSensorManager.registerListener(mSensorListener, mLinearAccelarationSensor, SensorManager.SENSOR_DELAY_NORMAL);
        //mSensorManager.registerListener(mSensorListener, mMotionSensor, SensorManager.SENSOR_DELAY_NORMAL);
        // mSensorManager.registerListener(mSensorListener, mProximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        // mSensorManager.registerListener(mSensorListener, mMagneticFieldSensor, SensorManager.SENSOR_DELAY_NORMAL);


    }

    /**
     * Handles the Request Updates button and requests start of location updates.
     */
    public void requestLocationUpdates(View view) {

        if (mSensorManager == null) {
            sensorSetup();
        }

        if (mAlarmManager == null) {
            mAlarmManager = (AlarmManager) getSystemService(this.ALARM_SERVICE);
           // mSensorListener.setAlarmManager(mAlarmManager);
        }

    }

    /**
     * Handles the Remove Updates button, and requests removal of location updates.
     */
    public void removeLocationUpdates(View view) {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(mSensorListener);
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

