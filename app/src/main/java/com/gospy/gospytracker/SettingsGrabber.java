package com.gospy.gospytracker;


import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

public class SettingsGrabber extends AsyncTask<Void, Void, Void> {
    //New class for the Asynctask, where the data will be fetched in the background
    private static Document doc = null;
    private static final String TAG = MainActivity.class.getSimpleName();
    @Override
    protected Void doInBackground(Void... params) {
        // NO CHANGES TO UI TO BE DONE HERE

        try {
            doc = Jsoup.connect("https://gospyapp.wordpress.com/").get();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        //This is where we update the UI with the acquired data
        if (doc != null) {
            try {
                Element settingsText = doc.select("div.entry-content").first();
                String settings = settingsText.text();
                System.out.println(settings);
            }catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("FAILURE");
        }
    }
}

