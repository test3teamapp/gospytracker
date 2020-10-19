package com.gospy.gospytracker.utils;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class PostToServer extends AsyncTask<String, Void, Void> {
//New class for the Asynctask, where the data will be fetched in the background

    private static final String TAG = PostToServer.class.getSimpleName();

    @Override
    protected Void doInBackground(String... params) {
        // NO CHANGES TO UI TO BE DONE HERE

        try {

            URL url = new URL(params[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Accept", "*/*");

            connection.setDoOutput(true);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            // writer.write(params[1]);
            writer.close();

            connection.connect();

            // Response: 400
            Log.i(TAG, connection.getResponseMessage() + "");


        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        //This is where we update the UI with the acquired data
    }

}
