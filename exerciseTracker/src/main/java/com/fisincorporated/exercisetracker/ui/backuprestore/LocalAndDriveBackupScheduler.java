package com.fisincorporated.exercisetracker.ui.backuprestore;


import android.content.Context;
import android.os.Bundle;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.fisincorporated.exercisetracker.GlobalValues;

// schedule backup via something like
// LocalAndDriveBackupScheduler.scheduleBackupJob(applicationContext, GlobalValues.BACKUP_TO_LOCAL || BACKUP_TO_DRIVE);
public class LocalAndDriveBackupScheduler {
    private static final String TAG = LocalAndDriveBackupScheduler.class.getSimpleName();

    // schedule the job.
    public static void scheduleBackupJob(Context context, int backupLocation) {

        // Can't store data as parcelable for job params, need to convert to Json and store that
        Bundle bundle = new Bundle();
        bundle.putInt(GlobalValues.BACKUP_TYPE, backupLocation);

        // Create a new dispatcher using the Google Play driver.
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        Job backupJob = dispatcher.newJobBuilder()
                .setService(BackupJobService.class) // the JobService that will be called
                .setTag("ActivityTracker backup" + backupLocation)        // uniquely identifies the job
                .setRecurring(false)         // one-off job
                .setReplaceCurrent(false) // don't overwrite an existing job with the same tag
                .build();
        dispatcher.mustSchedule(backupJob);
    }
}


