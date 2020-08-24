package com.gospy.gospytracker;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.gospy.gospytracker.receivers.AlarmReceiver;
import com.gospy.gospytracker.receivers.LocationUpdatesBroadcastReceiver;

import java.util.Calendar;

public class MainWorker extends Worker {

    // location updates interval - 10sec
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    // fastest updates interval - 5 sec
    // location updates will be received if another app is requesting the locations
    // than your app can handle
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5000;

    private static final int REQUEST_CHECK_SETTINGS = 100;
    private static final int ALARM_NOTIFICATION_ID = 101;
    private static final String TAG = "MainWorker";
    private Context appContext = null;
    private AlarmManager mAlarmManager;
    private PendingIntent mAlarmPendingIntent;

    public MainWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        appContext = context;
    }

    @SuppressLint("MissingPermission")
    @Override
    public Result doWork() {

        // Do the work here--in this case, upload the images.

        LocationUpdateProvider.getSingletonLocationUpdateProvider(super.getApplicationContext()).requestLocationUpdates();
        // minimum repeating interval for worker is 15 minutes.
        // So, we start an alarm for every (X) minutes to get more LUs
        // this is also helpfull since after a reboot alarms are not triggered, if we were to use only alarms
       /* boolean alarmUp = (PendingIntent.getBroadcast(appContext, ALARM_NOTIFICATION_ID,
                new Intent(AlarmReceiver.ACTION_PROCESS_ALARM),
                PendingIntent.FLAG_NO_CREATE) != null);

        if (alarmUp)
        {
            Log.i(TAG, "Alarm is already active");
        }else {*/
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.MINUTE, 1);

            Intent intent = new Intent(appContext,AlarmReceiver.class);
            intent.setAction(AlarmReceiver.ACTION_PROCESS_ALARM);
            mAlarmPendingIntent = PendingIntent.getBroadcast(appContext, ALARM_NOTIFICATION_ID,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
            //alarm manager can be retrieved from static content
            mAlarmManager = (AlarmManager) appContext.getSystemService(appContext.ALARM_SERVICE);

            mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 3, mAlarmPendingIntent);
       //}

        // Indicate whether the work finished successfully with the Result
        return Result.success();
    }
}
