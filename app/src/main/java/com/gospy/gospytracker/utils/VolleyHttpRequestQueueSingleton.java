package com.gospy.gospytracker.utils;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.gospy.gospytracker.MainActivity;

public class VolleyHttpRequestQueueSingleton {
    private static VolleyHttpRequestQueueSingleton mInstance;
    private RequestQueue mRequestQueue;
    private static Context mAppContext;

    private static final String TAG = MainActivity.class.getSimpleName();

    private VolleyHttpRequestQueueSingleton(Context context) {
        mAppContext = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized VolleyHttpRequestQueueSingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleyHttpRequestQueueSingleton(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mAppContext.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

}


