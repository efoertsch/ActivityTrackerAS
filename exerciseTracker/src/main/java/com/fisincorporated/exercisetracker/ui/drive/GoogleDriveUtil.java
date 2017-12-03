package com.fisincorporated.exercisetracker.ui.drive;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.ui.utils.IoUtils;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.drive.CreateFileActivityOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;


public class GoogleDriveUtil {

    private static final String TAG = GoogleDriveUtil.class.getSimpleName();

    private static final int REQUEST_CODE_SIGN_IN = 0;
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
    private static final int REQUEST_CODE_CREATOR = 2;

    //TODO change field naming
    private GoogleSignInClient mGoogleSignInClient;
    private DriveClient mDriveClient;
    private DriveResourceClient mDriveResourceClient;
    private Context context;

    private DriveClientReady driveClientReady;

    interface DriveClientReady {
        void onDriveClientError(String error);

        void onDriveClientReady();
    }

    private GoogleDriveUtil() {
    }

    public GoogleDriveUtil getInstance(Context context, DriveClientReady driveClientReady) {
        GoogleDriveUtil driveOps = new GoogleDriveUtil();
        driveOps.context = context;
        driveOps.driveClientReady = driveClientReady;
        return driveOps;
    }

    private void initializeDriveClient(GoogleSignInAccount signInAccount) {
        mDriveClient = Drive.getDriveClient(context, signInAccount);
        mDriveResourceClient = Drive.getDriveResourceClient(context, signInAccount);
        if (driveClientReady != null) {
            driveClientReady.onDriveClientReady();
        }
    }

    protected DriveClient getDriveClient() {
        return mDriveClient;
    }

    protected DriveResourceClient getDriveResourceClient() {
        return mDriveResourceClient;
    }

    public Task<DriveFolder> getRootFolderTask(DriveResourceClient driveResourceClient) {
        return driveResourceClient.getRootFolder();
    }

    public Task<DriveContents> getCreateContentsTask(DriveResourceClient driveResourceClient) {
        return getDriveResourceClient().createContents();
    }

    public void saveFileToDrive (final Task<DriveFolder> rootFolderTask, final Task<DriveContents> createContentsTask ) {
        Tasks.whenAll(rootFolderTask, createContentsTask)
                .continueWithTask(new Continuation<Void, Task<DriveFile>>() {
                    @Override
                    public Task<DriveFile> then(@NonNull Task<Void> task) throws Exception {
                        DriveFolder parent = rootFolderTask.getResult();
                        DriveContents contents = createContentsTask.getResult();
                        OutputStream outputStream = contents.getOutputStream();
                        try (Writer writer = new OutputStreamWriter(outputStream)) {
                            writer.write("Hello World!");
                        }

                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle("HelloWorld.txt")
                                .setMimeType("text/plain")
                                .setStarred(true)
                                .build();

                        return getDriveResourceClient().createFile(parent, changeSet, contents);
                    }
                })
                .addOnSuccessListener(this,
                        new OnSuccessListener<DriveFile>() {
                            @Override
                            public void onSuccess(DriveFile driveFile) {
                                showMessage(getString(R.string.file_created,
                                        driveFile.getDriveId().encodeToString()));
                                finish();
                            }
                        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Unable to create file", e);
                        showMessage(getString(R.string.file_create_error));
                        finish();
                    }
                });
    }
    private void retrieveMetadata(final DriveFile file) {
        // [START retrieve_metadata]
        Task<Metadata> getMetadataTask = getDriveResourceClient().getMetadata(file);
        getMetadataTask
                .addOnSuccessListener(this,
                        new OnSuccessListener<Metadata>() {
                            @Override
                            public void onSuccess(Metadata metadata) {
                                showMessage(getString(
                                        R.string.metadata_retrieved, metadata.getTitle()));
                                finish();
                            }
                        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Unable to retrieve metadata", e);
                        showMessage(getString(R.string.read_failed));
                        finish();
                    }
                });
        // [END retrieve_metadata]
    }

    public MetadataChangeSet getMetadataChangeSet(String fileName, String mimeType) {
        // Create the initial metadata - MIME type and title.
        // Note that the user will be able to change the title later.
       return new MetadataChangeSet.Builder()
                        .setMimeType(mimeType)
                        .setTitle(fileName)
                        .build();
    }

    /**
     * Creates an {@link IntentSender} to start a dialog activity with configured {@link
     * CreateFileActivityOptions} for user to create a new file in Drive.
     * Saves file in root of users Drive
     */
    private Task<Void> createFileIntentSender(final Activity activity,  DriveContents driveContents, File file, String mimeType) {
        Log.i(TAG, "New contents created.");
        OutputStream outputStream;
        InputStream inputStream;
        // Get an output stream for the contents.
        outputStream = driveContents.getOutputStream();

        try {
            inputStream = new BufferedInputStream(new FileInputStream(file));
            IoUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            // TODO - set up snackbar?
            //Snackbar.make(findViewById(R.id.content_drive_view), "Unable to write file contents.", Snackbar.LENGTH_INDEFINITE);
            Toast.makeText(activity,"Unable to write file contents.", Toast.LENGTH_LONG );
        }

        // Create the initial metadata - MIME type and title.
        // Note that the user will be able to change the title later.
        MetadataChangeSet metadataChangeSet = getMetadataChangeSet(file.getName(), mimeType);

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
                                activity.startIntentSenderForResult(task.getResult(), REQUEST_CODE_CREATOR, null, 0, 0, 0);
                                return null;
                            }
                        });
    }

    public void postNotification(String notificationContent, Class activityClass) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_backup_status_bar)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(notificationContent);
        // Creates an explicit intent for an Activity in your app
        Intent intent = new Intent(context, activityClass);

        // The stack builder object can contain an artificial back stack for the started Activity.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        mNotificationManager.notify(0, mBuilder.build());
    }

}
