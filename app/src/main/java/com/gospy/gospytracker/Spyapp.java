package com.gospy.gospytracker;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class Spyapp extends Application {
    private static Spyapp instance;
    private static Context mContext;
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
    }
}
