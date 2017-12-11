package com.fisincorporated.exercisetracker.ui.drive;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.ui.backuprestore.BackupUtils;
import com.fisincorporated.exercisetracker.ui.startup.ExerciseDrawerActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveResourceClient;

import java.io.File;

import io.reactivex.Completable;
import io.reactivex.disposables.CompositeDisposable;

public class DriveBackupJobService extends JobService {
    private static final String TAG = DriveBackupJobService.class.getSimpleName();

    private final CompositeDisposable disposables = new CompositeDisposable();

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "onStartJob called");
        return startBackup(params);
    }

    /**
     * Called by system to cancel pending tasks when a cancel request is received.
     * Only called if onStartJob returns false
     *
     * @param params
     * @return true if to reschedule job based on previous scheduling criteria
     * false if to drop the job
     */
    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "onStopJob called");
        return false;
    }

    public boolean startBackup(JobParameters params) {
        int backupType = getBackupType(params);
        if (GlobalValues.BACKUP_TO_LOCAL == backupType) {
            return doLocalBackup(params);
        } else {
            return doBackupOnDrive(params);
        }
    }

    private boolean doLocalBackup(JobParameters params) {
        Completable observable = BackupUtils.getLocalBackupCompletable(getApplicationContext(), GlobalValues.DATABASE_NAME);
        subscribeToBackupObservable(params, observable);
        return true;
    }

    private boolean doBackupOnDrive(JobParameters params) {
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (signInAccount == null) {
            postSignInNotification(getString(R.string.sign_in_to_drive_before_backup));
            return false;
        }
        DriveResourceClient driveResourceClient = Drive.getDriveResourceClient(getApplicationContext(), signInAccount);
        GoogleDriveFile googleDriveFile = GoogleDriveFile.getInstance();
        googleDriveFile.setDriveResourceClient(driveResourceClient)
                .setContext(this.getApplicationContext())
                .setFolderName(getString(R.string.app_name))
                .setDriveFileName(GlobalValues.DATABASE_NAME)
                .setMimeType(GlobalValues.SQLITE_MIME_TYPE)
                .setLocalFile(getDatabaseFile(this));

        //Completable observable = BackupUtils.getDriveBackupCompletable(getApplicationContext(), null);
        subscribeToBackupObservable(params, GoogleDriveUtil.uploadFileToDrive(googleDriveFile));
        return true;
    }

    private void subscribeToBackupObservable(JobParameters params, Completable observable) {
        observable.subscribe(() -> {
            postNotification(getApplicationContext().getString(R.string.drive_backup_success));
            jobFinished(params, false);
        }, throwable -> {
            postErrorNotification(throwable);
            jobFinished(params, false);
        });
    }

    private int getBackupType(JobParameters params) {
        Bundle bundle = params.getExtras();
        return bundle.getInt(GlobalValues.BACKUP_TYPE, GlobalValues.BACKUP_TO_LOCAL);
    }

    private void postErrorNotification(Throwable e) {
        Log.e(TAG, e.toString());
        postNotification(getApplicationContext().getString(R.string.backup_error, e.toString()));
    }

    public void postSignInNotification(String notificationContent) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this);
        Notification notification = builder
                        .setSmallIcon(R.drawable.ic_notification_info)
                        .setContentTitle(getString(R.string.app_name))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationContent)).build();
        // Creates an explicit intent for an Activity in your app
        Intent intent = new Intent(this, DriveSignOnActivity.class);

        // The stack builder object will contain an artificial back stack for the started Activity.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(DriveSignOnActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        mNotificationManager.notify(0,notification);
    }

    public void postNotification(String notificationContent) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_notification_info)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(notificationContent);
        // Creates an explicit intent for an Activity in your app
        Intent intent = new Intent(this, ExerciseDrawerActivity.class);

        // The stack builder object will contain an artificial back stack for the started Activity.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ExerciseDrawerActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        mNotificationManager.notify(0, builder.build());
    }

    private static File getDatabaseFile(Context context) {
        String currentDBPath = context.getDatabasePath(GlobalValues.DATABASE_NAME).getAbsolutePath();
        File currentDB = new File(currentDBPath);
        return currentDB;
    }
}