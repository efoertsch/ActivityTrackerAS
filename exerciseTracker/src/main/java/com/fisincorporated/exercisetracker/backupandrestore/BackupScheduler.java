package com.fisincorporated.exercisetracker.backupandrestore;

import android.content.Context;
import android.os.Bundle;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.fisincorporated.exercisetracker.GlobalValues;



public class BackupScheduler {

    private static final String TAG = BackupScheduler.class.getSimpleName();

    // schedule the job.
    public static void scheduleBackupJob(Context context, int backupLocation) {

        // Can't store data as parcelable for job params, need to convert to Json and store that
        Bundle bundle = new Bundle();
        bundle.putInt(GlobalValues.BACKUP_TYPE, backupLocation);

        // Create a new dispatcher using the Google Play driver.
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        Job backupJob = dispatcher.newJobBuilder()
                .setExtras(bundle)
                .setService(BackupJobService.class) // the JobService that will be called
                .setTag("ActivityTracker backup" + backupLocation)        // uniquely identifies the job
                .setTrigger(Trigger.executionWindow(0, 0)) // trigger immediately
                .setRecurring(false)         // one-off job
                .setReplaceCurrent(true) // overwrite an existing job with the same tag
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                // drive backup require wifi access
                .setConstraints(((backupLocation == GlobalValues.BACKUP_TO_DRIVE) ? Constraint.ON_UNMETERED_NETWORK : Constraint.ON_ANY_NETWORK)
                        // only run when the device is charging
                        , Constraint.DEVICE_CHARGING)
                .build();
        dispatcher.mustSchedule(backupJob);
    }
}
