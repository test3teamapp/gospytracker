package com.gospy.gospytracker.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;


public class PostToServerWorker extends Worker {

    private static final String TAG = PostToServerWorker.class.getSimpleName();

    public PostToServerWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {

        String serverUriInput = getInputData().getString("SERVER_URI");
        if(serverUriInput == null) {
            return Result.failure();
        }

        // Do the work here--
        // try bringing up network

        final boolean[] avail = {false};

        try {

            ConnectivityManager cm = (ConnectivityManager) super.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            assert cm != null;
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                avail[0] = true;
            } else {
                Log.i(TAG, "No network. Trying to bring wifi up");

                try {
                    WifiManager wifiMgr = (WifiManager) super.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    boolean res = wifiMgr.setWifiEnabled(true);
                    if (res) {
                        Log.d(TAG, "brought up wifi");
                        avail[0] = true;
                    } else {
                        Log.d(TAG, "bringing up wifi failed");
                    }
                    //fOR aNDROID 10 : res value is set to false above because setWifiEnabled returns false on Android 10
                } catch (Exception exc) {
                    Log.d(TAG, "Exception when bringing up wifi");
                    exc.printStackTrace();
                }

                Log.d(TAG, "bringing up cellular");
                // try requesting network. (we have issues of not accessing the network while in the background

                NetworkRequest.Builder request = new NetworkRequest.Builder();
                request.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
                request.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    cm.requestNetwork(request.build(), new ConnectivityManager.NetworkCallback() {
                        @Override
                        public void onAvailable(Network network) {
                            Log.d(TAG, "requestNetwork onAvailable()");
                            avail[0] = true;
                        }

                        @Override
                        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                            Log.d(TAG, "requestNetwork onCapabilitiesChanged()");
                        }

                        @Override
                        public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
                            Log.d(TAG, "requestNetwork onLinkPropertiesChanged()");
                        }

                        @Override
                        public void onLosing(Network network, int maxMsToLive) {
                            Log.d(TAG, "requestNetwork onLosing()");
                        }

                        @Override
                        public void onLost(Network network) {
                            Log.d(TAG, "requestNetwork onLost()");
                        }
                    }, 60 * 1000);
                }
            }
        } catch (Exception exc) {
            Log.d(TAG, "Exception checking network");
            exc.printStackTrace();
        }

        // delay for allowing network to come up before return
        //long currentTime = System.currentTimeMillis();
       // while (System.currentTimeMillis() < currentTime + 3000) {
            // Log.i(TAG,"tik tok");
       // }

        Log.i(TAG,"Trying to post data. Network is : " + avail[0]);
        // Volley cashes the requests. Hopefully, if the network is brought up - if not already up -
        // it will send them then.

        try {
            // Instantiate the RequestQueue for volley.
            final RequestQueue queue = VolleyHttpRequestQueueSingleton.getInstance(super.getApplicationContext()).getRequestQueue();
            // Request a string response from the provided URL.

           // if (Utils.isNetwork()) {

                StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUriInput,
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
                queue.start();

            //}
        } catch (Exception exc) {
            exc.printStackTrace();
            return Result.failure();
        }

        // Indicate whether the work finished successfully with the Result
        return Result.success();
    }
}
