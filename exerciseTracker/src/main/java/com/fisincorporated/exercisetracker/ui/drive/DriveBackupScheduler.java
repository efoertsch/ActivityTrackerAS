package com.fisincorporated.exercisetracker.ui.drive;

import android.content.Context;
import android.os.Bundle;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.fisincorporated.exercisetracker.GlobalValues;



public class DriveBackupScheduler {

    private static final String TAG = DriveBackupScheduler.class.getSimpleName();

    // schedule the job.
    public static void scheduleBackupJob(Context context, int backupLocation) {

        // Can't store data as parcelable for job params, need to convert to Json and store that
        Bundle bundle = new Bundle();
        bundle.putInt(GlobalValues.BACKUP_TYPE, backupLocation);

        // Create a new dispatcher using the Google Play driver.
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        Job backupJob = dispatcher.newJobBuilder()
                .setExtras(bundle)
                .setService(DriveBackupJobService.class) // the JobService that will be called
                .setTag("ActivityTracker backup" + backupLocation)        // uniquely identifies the job
                .setTrigger(Trigger.executionWindow(0, 0)) // trigger immediately
                .setRecurring(false)         // one-off job
                .setReplaceCurrent(false) // don't overwrite an existing job with the same tag
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .build();
        dispatcher.mustSchedule(backupJob);
    }
}
