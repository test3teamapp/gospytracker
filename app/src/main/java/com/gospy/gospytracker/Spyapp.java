package com.gospy.gospytracker;

import android.app.Application;
import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

public class Spyapp extends Application {
    private static Spyapp instance;
    private static Context mContext;
    private static PowerManager.WakeLock mWakeLock = null;
    private static final String TAG = Spyapp.class.getSimpleName();

    public static Spyapp getInstance() {
        return instance;
    }

    public static Context getContext() {
        //  return instance.getApplicationContext();
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate()");
        instance = this;
        mContext = getApplicationContext();

        /*
        boolean needToAcquire = false;
        if (mWakeLock != null){
            if (mWakeLock.isHeld()) {
                Log.i(TAG, "WakeLock is already held. Skip acquiring new");
            } else {
                needToAcquire = true;
                Log.i(TAG, "WakeLock was not null, but is not held. Acquire new");
            }
        }else {
            needToAcquire = true;
        }

        if (needToAcquire) {
            PowerManager pm = (PowerManager) Spyapp.getContext().getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "GoSpyTracker:WakelockForLU");
            mWakeLock.acquire();

            if (mWakeLock.isHeld()) {
                Log.i(TAG, "WakeLock is held");
            }else {
                Log.i(TAG, "Failed acquiring wakelock");
            }
        }
        */
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.i(TAG, "On Terminate");
        if (mWakeLock.isHeld()) {
            Log.i(TAG, "WakeLock is held");
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.i(TAG, "On LowMemory");
        if (mWakeLock.isHeld()) {
            Log.i(TAG, "WakeLock is held");
        }
    }

}
