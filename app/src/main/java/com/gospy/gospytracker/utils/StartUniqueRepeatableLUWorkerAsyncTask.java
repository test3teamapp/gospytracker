package com.gospy.gospytracker.utils;

import android.os.AsyncTask;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.gospy.gospytracker.Spyapp;

import java.util.concurrent.TimeUnit;

public class StartUniqueRepeatableLUWorkerAsyncTask extends AsyncTask<Void, Void, Void> {
//New class for the Asynctask, where the data will be fetched in the background

    @Override
    protected Void doInBackground(Void... params) {
        // NO CHANGES TO UI TO BE DONE HERE
        // [START dispatch_job]
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(UniqueRepeatableLUWorker.class)
                // tag for removing the work if needed
                .addTag(Utils.mDefaultTAGLuWorkerRepeatable)
                .setInitialDelay(2, TimeUnit.MINUTES)
                .build();
        WorkManager.getInstance(Spyapp.getContext()).enqueueUniqueWork(Utils.mDefaultTAGLuWorkerRepeatable, ExistingWorkPolicy.REPLACE, work);
        // [END dispatch_job]

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        //This is where we update the UI with the acquired data
    }

}
