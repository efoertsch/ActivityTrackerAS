package com.fisincorporated.exercisetracker.ui.drive;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.backupandrestore.GoogleDriveUtil;
import com.fisincorporated.exercisetracker.backupandrestore.LocalBackupUtils;
import com.fisincorporated.exercisetracker.ui.startup.ExerciseDrawerActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import io.reactivex.Completable;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Use a test activity to test backup and restore
 */
public class DriveActivity extends AppCompatActivity {

    private static final String TAG = DriveActivity.class.getSimpleName();
    private static final int layoutView = R.layout.activity_drive;
    private static final int BACKUP_TO_DRIVE = 100;
    private static final int RESTORE_FROM_DRIVE = 200;
    private static final int GOOGLE_SIGNON = 2;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private GoogleSignInAccount signInAccount;
    private int action;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutView);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button driveBackupButton = (Button) findViewById(R.id.driveBackupButton);
        driveBackupButton.setOnClickListener(v -> {
            action = BACKUP_TO_DRIVE;
            backupDbToDrive();
        });

        Button driveRestoreButton = (Button) findViewById(R.id.driveRestoreButton);
        driveRestoreButton.setOnClickListener(v -> {
            restoreDbFromDrive();
            action = RESTORE_FROM_DRIVE;
        });

        Button localBackupButton = (Button) findViewById(R.id.localBackupButton);
        localBackupButton.setOnClickListener(v -> backupDbToLocal());

        Button localRestoreButton = (Button) findViewById(R.id.localRestoreButton);
        localRestoreButton.setOnClickListener(v -> restoreDbFromLocal());

    }

    @Override
    public void onStop() {
        super.onStop();
        compositeDisposable.clear();
    }

    private void backupDbToDrive() {
        if (haveDriveAccess()) {
            Completable observable = GoogleDriveUtil.getBackupToDriveCompletable(getApplicationContext(), signInAccount);
            subscribeToBackupRestoreObservable(observable, R.string.drive_backup_success, R.string.backup_error);
        }
    }

    private void restoreDbFromDrive() {
        if (haveDriveAccess()) {
            Completable observable = GoogleDriveUtil.getRestoreFromDriveCompletable(getApplicationContext(), signInAccount);
            subscribeToBackupRestoreObservable(observable, R.string.drive_restore_success, R.string.restore_error);
        }
    }

    private void backupDbToLocal() {
        Completable observable = LocalBackupUtils.getLocalBackupCompletable(getApplicationContext(), GlobalValues.DATABASE_NAME);
        subscribeToBackupRestoreObservable(observable, R.string.local_backup_success, R.string.backup_error);
    }

    private void restoreDbFromLocal() {
        Completable observable = LocalBackupUtils.getRestoreLocalCompletable(getApplicationContext());
        subscribeToBackupRestoreObservable(observable, R.string.local_restore_success, R.string.restore_error);
    }

    private void subscribeToBackupRestoreObservable(Completable observable, final @StringRes int successMsg, final @StringRes int failureMessage) {
        observable.subscribe(() -> {
                    postNotification(getApplicationContext().getString(successMsg));
                    displaySuccessfulMessage(getString(successMsg));
                    postNotification(getApplicationContext().getString(successMsg));
                }
                , throwable -> {
                    postNotification(getApplicationContext().getString(failureMessage));
                    displayError(throwable);
                    postNotification(getApplicationContext().getString(failureMessage));
                });
    }

    private boolean haveDriveAccess() {
        signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (signInAccount == null) {
            Intent intent = new Intent(this, DriveSignOnActivity.class);
            intent.putExtra("action", action);
            startActivityForResult(intent, GOOGLE_SIGNON);
            return false;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK || requestCode != GOOGLE_SIGNON) {
            return;
        }
        action = intent.getIntExtra("action", -1);
        switch (action) {
            case BACKUP_TO_DRIVE:
                backupDbToDrive();
                break;
            case RESTORE_FROM_DRIVE:
                restoreDbFromDrive();
                break;
        }
    }

    private void displaySuccessfulMessage(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }


    private void displayError(Throwable throwable) {
        log(throwable);
        Toast.makeText(getApplicationContext(), throwable.toString(), Toast.LENGTH_LONG).show();
    }

    public void postNotification(String notificationContent) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_backup_status_bar)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(notificationContent);
        // Creates an explicit intent for an Activity in your app
        Intent intent = new Intent(this, ExerciseDrawerActivity.class);

        // The stack builder object can contain an artificial back stack for the started Activity.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        notificationManager.notify(0, builder.build());
    }

    private static void log(Throwable throwable) {
        Log.e(TAG, throwable.toString() + " " + throwable.getCause().toString());

    }

}
