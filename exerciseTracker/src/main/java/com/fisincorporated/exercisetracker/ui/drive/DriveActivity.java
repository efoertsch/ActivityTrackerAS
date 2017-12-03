package com.fisincorporated.exercisetracker.ui.drive;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.ui.startup.ExerciseDrawerActivity;
import com.fisincorporated.exercisetracker.ui.utils.IoUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.CreateFileActivityOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class DriveActivity extends AppCompatActivity {

    private static final String TAG = DriveActivity.class.getSimpleName();

    private static final int REQUEST_CODE_SIGN_IN = 0;
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
    private static final int REQUEST_CODE_CREATOR = 2;

    private GoogleSignInClient mGoogleSignInClient;
    private DriveClient mDriveClient;
    private DriveResourceClient mDriveResourceClient;

    private final static int layoutView = R.layout.activity_drive;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutView);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.activity_drive_backup_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

    }

    /**
     * Start sign in activity.
     */
    private void signIn() {
        Log.i(TAG, "Start sign in");
        mGoogleSignInClient = buildGoogleSignInClient();
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    /**
     * Build a Google SignIn client with request to Drive app folder
     */
    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_APPFOLDER)
                        .build();
        return GoogleSignIn.getClient(this, signInOptions);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                Log.i(TAG, "Sign in request code");
                // Called after user is signed in.
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "Signed in successfully.");
                    // Use the last signed in account here since it already have a Drive scope.
                    initializeDriveClient(GoogleSignIn.getLastSignedInAccount(this);
                    mDriveResourceClient = Drive.getDriveResourceClient(this, GoogleSignIn.getLastSignedInAccount(this));
                    // Get drive resource client and use to save file
                    saveFileToDrive(mDriveResourceClient, getDatabaseFile(), GlobalValues.SQLITE_MIME_TYPE);
                }
                break;
            case REQUEST_CODE_CREATOR:
                Log.i(TAG, "creator request code");
                // Called after a file is saved to Drive.
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "Image successfully saved.");
                    Snackbar.make(findViewById(R.id.content_drive_view), "Backedup activity database successfully", Snackbar.LENGTH_INDEFINITE);
                }
                break;
        }
    }

    /**
     * Continues the sign-in process, initializing the Drive clients with the current
     * user's account.
     */
    private void initializeDriveClient(GoogleSignInAccount signInAccount) {
        mDriveClient = Drive.getDriveClient(getApplicationContext(), signInAccount);
        mDriveResourceClient = Drive.getDriveResourceClient(getApplicationContext(), signInAccount);
        onDriveClientReady();
    }



    //TODO put this and code from BackupRestoreFragment into utility class
    private File getDatabaseFile() {
        File data = Environment.getDataDirectory();
        String currentDBPath = "//data//" + GlobalValues.PACKAGE_NAME
                + "//databases//" + GlobalValues.DATABASE_NAME;
        File currentDB = new File(data, currentDBPath);
        return currentDB;
    }

    /**
     * Creates an {@link IntentSender} to start a dialog activity with configured {@link
     * CreateFileActivityOptions} for user to create a new file in Drive.
     * Saves file in root of users Drive
     */
    private Task<Void> createFileIntentSender(DriveContents driveContents, File file, String mimeType) {
        Log.i(TAG, "New contents created.");
        OutputStream outputStream;
        InputStream inputStream;
        // Get an output stream for the contents.
        outputStream = driveContents.getOutputStream();

        try {
            inputStream = new BufferedInputStream(new FileInputStream(file));
            IoUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            // TODO - make generic as snackbar may not be available
            Snackbar.make(findViewById(R.id.content_drive_view), "Unable to write file contents.", Snackbar.LENGTH_INDEFINITE);
        }

        // Create the initial metadata - MIME type and title.
        // Note that the user will be able to change the title later.
        MetadataChangeSet metadataChangeSet =
                new MetadataChangeSet.Builder()
                        .setMimeType(mimeType)
                        .setTitle(file.getName())
                        .build();
        // Set up options to configure and display the create file activity.
        CreateFileActivityOptions createFileActivityOptions =
                new CreateFileActivityOptions.Builder()
                        .setInitialMetadata(metadataChangeSet)
                        .setInitialDriveContents(driveContents)
                        .build();

        return mDriveClient
                .newCreateFileActivityIntentSender(createFileActivityOptions)
                .continueWith(
                        new Continuation<IntentSender, Void>() {
                            @Override
                            public Void then(@NonNull Task<IntentSender> task) throws Exception {
                                startIntentSenderForResult(task.getResult(), REQUEST_CODE_CREATOR, null, 0, 0, 0);
                                return null;
                            }
                        });
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
