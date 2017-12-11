package com.fisincorporated.exercisetracker.ui.drive;

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
import com.fisincorporated.exercisetracker.rxdrive.RxDrive;
import com.fisincorporated.exercisetracker.ui.backuprestore.BackupUtils;
import com.fisincorporated.exercisetracker.ui.startup.ExerciseDrawerActivity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DriveActivity extends AppCompatActivity {

    private static final String TAG = DriveActivity.class.getSimpleName();

    private RxDrive rxDrive;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final static int layoutView = R.layout.activity_drive;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutView);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button driveBackupButton = (Button) findViewById(R.id.driveBackupButton);
        driveBackupButton.setOnClickListener(v -> backupDbToDrive());

        Button driveRestoreButton = (Button) findViewById(R.id.driveRestoreButton);
        driveRestoreButton.setOnClickListener(v -> restoreDbFromDrive());

        Button localBackupButton = (Button) findViewById(R.id.localBackupButton);
        localBackupButton.setOnClickListener(v -> backupDbToLocal());

        Button localRestoreButton = (Button) findViewById(R.id.localRestoreButton);
        localRestoreButton.setOnClickListener(v -> restoreDbFromLocal());

        rxDrive = new RxDrive(new GoogleApiClient.Builder(this)
                .addScope(Drive.SCOPE_FILE));

    }

    @Override
    protected void onStart() {
        super.onStart();
        setupGoogleApiClientObservable();
        rxDrive.connect();
    }

    /**
     * Start sign in activity.
     */
    private void setupGoogleApiClientObservable() {
        Disposable disposable = rxDrive.connectionObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(connectionState -> {
                    switch (connectionState.getState()) {
                        case CONNECTED:
                            log("Connected");
                            //backupDbToDrive();
                            //restoreDbFromDrive();
                            break;
                        case SUSPENDED:
                            log("SUSPENDED");
                            break;
                        case FAILED:
                            log(connectionState.getConnectionResult().getErrorMessage());
                            rxDrive.resolveConnection(DriveActivity.this, connectionState.getConnectionResult());
                            break;
                        case UNABLE_TO_RESOLVE:
                            log("Unable to resolve. " + connectionState.getConnectionResult());
                            finish();
                            break;
                    }
                }, this::log);
        compositeDisposable.add(disposable);

    }

    @Override
    public void onStop() {
        super.onStop();
        rxDrive.disconnect();
        compositeDisposable.clear();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        rxDrive.onActivityResult(requestCode, resultCode, data);
    }

    private void log(Object object) {
        if (object != null) {
            Log.d(TAG, "log: " + object);
            Toast.makeText(this, object.toString(), Toast.LENGTH_SHORT).show();
            if (object instanceof Throwable) {
                ((Throwable) object).printStackTrace();
            }
        }
    }

    private void backupDbToDrive() {
        Completable observable = BackupUtils.getDriveBackupCompletable(getApplicationContext(), rxDrive);
        subscribeToBackupRestoreObservable(observable, R.string.drive_backup_success, R.string.backup_error);
    }

    private void restoreDbFromDrive() {
        Completable observable = BackupUtils.getRestoreDriveCompletable(getApplicationContext(), rxDrive);
        subscribeToBackupRestoreObservable(observable, R.string.drive_restore_success, R.string.restore_error);
    }

    private void backupDbToLocal() {
        Completable observable = BackupUtils.getLocalBackupCompletable(getApplicationContext(), GlobalValues.DATABASE_NAME);
        subscribeToBackupRestoreObservable(observable, R.string.local_backup_success, R.string.backup_error);
    }

    private void restoreDbFromLocal() {
        Completable observable = BackupUtils.getRestoreLocalCompletable(getApplicationContext());
        subscribeToBackupRestoreObservable(observable, R.string.local_restore_success, R.string.restore_error);
    }

    private void subscribeToBackupRestoreObservable(Completable observable, final @StringRes int successMsg, final @StringRes int failureMessage) {
        observable.subscribe(() -> {
                    postNotification(getApplicationContext().getString(successMsg));
                    displaySuccessfulRestore();
                    postNotification(getApplicationContext().getString(successMsg));
                }
                , throwable -> {
                    postNotification(getApplicationContext().getString(failureMessage, throwable.toString()));
                    displayError(throwable);
                    postNotification(getApplicationContext().getString(failureMessage, throwable.toString()));
                });
    }


    private void displaySuccessfulRestore() {
        Toast.makeText(getApplicationContext(), getString(R.string.activitytracker_database_restored), Toast.LENGTH_LONG).show();
    }


    private void displayError(Throwable throwable) {
        Toast.makeText(getApplicationContext(), throwable.toString(), Toast.LENGTH_LONG).show();
    }

    public void postNotification(String notificationContent) {
        NotificationCompat.Builder mBuilder =
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
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        mNotificationManager.notify(0, mBuilder.build());
    }

}
