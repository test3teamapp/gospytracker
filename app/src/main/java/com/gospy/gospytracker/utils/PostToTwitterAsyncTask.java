package com.gospy.gospytracker.utils;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;

public class PostToTwitterAsyncTask extends AsyncTask<String, Void, Void> {

    private static final String TAG = PostToTwitterAsyncTask.class.getSimpleName();

    @Override
    protected Void doInBackground(String... params) {
        // NO CHANGES TO UI TO BE DONE HERE

        try {

            Twitter twitter = TwitterFactory.getSingleton();
            try {
                twitter4j.Status status = twitter.updateStatus(Utils.getSPStringValue(Utils.KEY_TRACKED_DEVICE_APP_UID) + " : " + params[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //System.out.println("Successfully updated the status to [" + status.getText() + "].");


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
