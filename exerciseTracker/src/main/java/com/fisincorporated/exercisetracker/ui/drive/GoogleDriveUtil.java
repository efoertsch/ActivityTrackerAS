package com.fisincorporated.exercisetracker.ui.drive;

import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.utility.IoUtils;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.drive.query.SortOrder;
import com.google.android.gms.drive.query.SortableField;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

//TODO Make more generic
public class GoogleDriveUtil {

    private static final String TAG = GoogleDriveUtil.class.getSimpleName();

    public static Completable uploadFileToDrive(GoogleDriveFile googleDriveFile) {
        return getRootFolderCompletable(googleDriveFile)
                .andThen(getCreateFolderCompletable(googleDriveFile))
                .andThen(getDriveContentsCompletable(googleDriveFile))
                .andThen(getWriteToDriveFileCompletable(googleDriveFile))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static Completable getRootFolderCompletable(GoogleDriveFile googleDriveFile) {
        Completable completable = Completable.fromAction(() -> {
            try {
                googleDriveFile.setParentDriveFolder(Tasks.await(googleDriveFile.getDriveResourceClient().getRootFolder()));
            } catch (Exception e) {
                throw new GoogleDriveException(googleDriveFile.getContext().getString(R.string.drive_root_folder_not_found), e);
            }
        });
        return completable;
    }

    /**
     * Create folder if it doesn't already exist, or if it does just get its DriveFolder info and
     * update GoogleDriveFile
     *
     * @param googleDriveFile
     * @return
     */
    public static Completable getCreateFolderCompletable(GoogleDriveFile googleDriveFile) {
        Completable completable = Completable.fromAction(() -> {
            DriveFolder driveFolder;
            try {
                DriveId driveId = getDriveIdForFileOrFolder(googleDriveFile.getDriveResourceClient()
                        , googleDriveFile.getParentDriveFolder(),
                        DriveFolder.MIME_TYPE, googleDriveFile.getFolderName(), false);
                if (driveId != null) {
                    driveFolder = driveId.asDriveFolder();
                } else {
                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle(googleDriveFile.getFolderName())
                            .setMimeType(DriveFolder.MIME_TYPE)
                            .build();
                    driveFolder = Tasks.await(googleDriveFile.getDriveResourceClient().createFolder(googleDriveFile.getParentDriveFolder(), changeSet));
                }
                googleDriveFile.setDriveFileFolder(driveFolder);
            } catch (Exception e) {
                throw new GoogleDriveException(googleDriveFile.getContext().getString(R.string.unable_to_create_new_folder
                        , googleDriveFile.getFolderName(), googleDriveFile.getParentDriveFolder().getDriveId().toString()), e);
            }
        });
        return completable;
    }

    public static Completable getDriveContentsCompletable(GoogleDriveFile googleDriveFile) {
        Completable completable = Completable.fromAction(() -> {
            DriveContents driveContents;
            try {
                DriveId driveId = getDriveIdForFileOrFolder(googleDriveFile.getDriveResourceClient()
                        , googleDriveFile.getDriveFileFolder()
                        , googleDriveFile.getMimeType(), googleDriveFile.getDriveFileName()
                        , false);
                if (driveId != null) {
                    DriveFile driveFile = driveId.asDriveFile();
                    driveContents = Tasks.await(googleDriveFile.getDriveResourceClient().openFile(driveFile, DriveFile.MODE_WRITE_ONLY));
                    googleDriveFile.setDriveContents(driveContents);
                } else {
                    driveContents = Tasks.await(googleDriveFile.getDriveResourceClient().createContents());
                }
                googleDriveFile.setDriveContents(driveContents);
            } catch (Exception e) {
                throw new GoogleDriveException(googleDriveFile.getContext().getString(R.string.unable_to_create_drivecontents), e);
            }
        });
        return completable;
    }

    public static Completable getWriteToDriveFileCompletable(GoogleDriveFile googleDriveFile) {
        Completable completable = Completable.fromAction(() -> {
            try {
                OutputStream outputStream = googleDriveFile.getDriveContents().getOutputStream();
                InputStream inputStream = new FileInputStream(googleDriveFile.getLocalFile());
                IoUtils.copyWithNoClose(inputStream, outputStream);
                if (googleDriveFile.getDriveFile() == null) {
                    // First time writing file to Drive
                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle(googleDriveFile.getLocalFile().getName())
                            .setMimeType(googleDriveFile.getMimeType())
                            .setStarred(googleDriveFile.isStarred())
                            .build();
                    googleDriveFile.setDriveFile(Tasks.await(googleDriveFile.getDriveResourceClient()
                            .createFile(googleDriveFile.getDriveFileFolder(), changeSet, googleDriveFile.getDriveContents())));
                    // Seems a little wonky but closing the outputstream prior to commit causes the commitContents to throw and exception
                    // So copy file but don't close before commit.
                    Tasks.await(googleDriveFile.getDriveResourceClient().commitContents(googleDriveFile.getDriveContents(), null));
                }
                IoUtils.closeStream(inputStream);
                // And just in case
                IoUtils.closeStream(outputStream);
            } catch (Exception e) {
                throw new GoogleDriveException((googleDriveFile.getContext().getString(R.string.unable_to_write_to_drive_file, googleDriveFile.getLocalFile().getName())), e);
            }
        });
        return completable;
    }

    public static Completable downloadFileFromDrive(GoogleDriveFile googleDriveFile) {
        return getRootFolderCompletable(googleDriveFile)
                .andThen(checkForFolderCompletable(googleDriveFile))
                .andThen(checkForFileCompletable(googleDriveFile))
                .andThen(restoreDriveFileToLocalCompletable(googleDriveFile))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private static Completable checkForFolderCompletable(GoogleDriveFile googleDriveFile) {
        Completable completable = Completable.fromAction(() -> {
            try {
                DriveId driveId = getDriveIdForFileOrFolder(googleDriveFile.getDriveResourceClient()
                        , googleDriveFile.getParentDriveFolder()
                        , DriveFolder.MIME_TYPE, googleDriveFile.getFolderName()
                        , false);
                if (driveId != null) {
                    googleDriveFile.setDriveFileFolder(driveId.asDriveFolder());
                } else {
                    throw new GoogleDriveException(googleDriveFile.getContext().getString(R.string.no_drive_backup_available));
                }
            } catch (Exception e) {
                throw new GoogleDriveException(googleDriveFile.getContext().getString(R.string.no_drive_backup_available), e);
            }
        });
        return completable;
    }

    private static CompletableSource checkForFileCompletable(GoogleDriveFile googleDriveFile) {
        Completable completable = Completable.fromAction(() -> {
            try {
                DriveId driveId = getDriveIdForFileOrFolder(googleDriveFile.getDriveResourceClient()
                        , googleDriveFile.getDriveFileFolder()
                        , googleDriveFile.getMimeType(), googleDriveFile.getDriveFileName()
                        , false);
                if (driveId != null) {
                    DriveFile driveFile = driveId.asDriveFile();
                    DriveContents driveContents = Tasks.await(googleDriveFile.getDriveResourceClient().openFile(driveFile, DriveFile.MODE_READ_ONLY));
                    googleDriveFile.setDriveContents(driveContents);
                } else {
                    throw new GoogleDriveException(googleDriveFile.getContext().getString(R.string.no_drive_backup_available));
                }
            } catch (Exception e) {
                throw new GoogleDriveException(googleDriveFile.getContext().getString(R.string.no_drive_backup_available), e);
            }
        });
        return completable;
    }


    private static CompletableSource restoreDriveFileToLocalCompletable(GoogleDriveFile googleDriveFile) {
        Completable completable = Completable.fromAction(() -> {
            try {
                OutputStream outputStream = new FileOutputStream(googleDriveFile.getLocalFile());
                InputStream inputStream = googleDriveFile.getDriveContents().getInputStream();
                IoUtils.copyWithNoClose(inputStream, outputStream);
                googleDriveFile.getDriveResourceClient().discardContents(googleDriveFile.getDriveContents());
                IoUtils.closeStream(outputStream);
            } catch (Exception e) {
                throw new GoogleDriveException((googleDriveFile.getContext().getString(R.string.an_error_occurred_reading_file_from_drive, googleDriveFile.getLocalFile().getName())), e);
            }
        });
        return completable;
    }

    private static DriveId getDriveIdForFileOrFolder(DriveResourceClient driveResourceClient, DriveFolder parentFolder
            , String mimeType, String fileOrFolderName, boolean trashed) throws ExecutionException, InterruptedException {
        DriveId driveId = null;
        Query query = getDoesFileOrFolderExistQuery(mimeType, fileOrFolderName, trashed);
        MetadataBuffer metadataBuffer = Tasks.await(driveResourceClient.queryChildren(parentFolder, query));
        if (metadataBuffer.getCount() > 0) {
            Metadata metaData = metadataBuffer.get(0);
            driveId = metaData.getDriveId();
            metadataBuffer.release();
        }
        return driveId;
    }

    public static Query getDoesFileOrFolderExistQuery(String mimeType, String title, boolean trashed) {
        return new Query.Builder()
                .addFilter(Filters.and(Filters.eq(SearchableField.MIME_TYPE, mimeType),
                        Filters.eq(SearchableField.TITLE, title)
                        , Filters.eq(SearchableField.TRASHED, trashed)))
                .setSortOrder(new SortOrder.Builder().addSortDescending(SortableField.MODIFIED_DATE).build())
                .build();
    }

//    public GoogleDriveUtil getInstance(Context context, DriveClientReady driveClientReady) {
//        GoogleDriveUtil driveOps = new GoogleDriveUtil();
//        driveOps.context = context;
//        driveOps.driveClientReady = driveClientReady;
//        return driveOps;
//    }
//
//
//
//    private void initializeDriveClient(GoogleSignInAccount signInAccount) {
//        mDriveClient = Drive.getDriveClient(context, signInAccount);
//        mDriveResourceClient = Drive.getDriveResourceClient(context, signInAccount);
//        if (driveClientReady != null) {
//            driveClientReady.onDriveClientReady();
//        }
//    }

//    protected DriveClient getDriveClient() {
//        return mDriveClient;
//    }
//
//    protected DriveResourceClient getDriveResourceClient() {
//        return mDriveResourceClient;
//    }

    public Task<DriveFolder> getRootFolderTask(DriveResourceClient driveResourceClient) {
        return driveResourceClient.getRootFolder();
    }

//    public Task<DriveContents> getCreateContentsTask(DriveResourceClient driveResourceClient) {
//        return getDriveResourceClient().createContents();
//    }

//    public void saveFileToDrive(final Task<DriveFolder> rootFolderTask, final Task<DriveContents> createContentsTask) {
//        Tasks.whenAll(rootFolderTask, createContentsTask)
//                .continueWithTask(new Continuation<Void, Task<DriveFile>>() {
//                    @Override
//                    public Task<DriveFile> then(@NonNull Task<Void> task) throws Exception {
//                        DriveFolder parent = rootFolderTask.getResult();
//                        DriveContents contents = createContentsTask.getResult();
//                        OutputStream outputStream = contents.getOutputStream();
//                        try (Writer writer = new OutputStreamWriter(outputStream)) {
//                            writer.write("Hello World!");
//                        }
//
//                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
//                                .setTitle("HelloWorld.txt")
//                                .setMimeType("text/plain")
//                                .setStarred(true)
//                                .build();
//
//                        return getDriveResourceClient().createFile(parent, changeSet, contents);
//                    }
//                })
//                .addOnSuccessListener(
//                        new OnSuccessListener<DriveFile>() {
//                            @Override
//                            public void onSuccess(DriveFile driveFile) {
//                                Log.d(TAG, context.getString(R.string.drive_backup_success));
//
//                            }
//                        })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.e(TAG, "Unable to create file", e);
//                    }
//                });
//    }
//
//    private void retrieveMetadata(final DriveFile file) {
//        // [START retrieve_metadata]
//        Task<Metadata> getMetadataTask = getDriveResourceClient().getMetadata(file);
//        getMetadataTask
//                .addOnSuccessListener(
//                        new OnSuccessListener<Metadata>() {
//                            @Override
//                            public void onSuccess(Metadata metadata) {
//                                Log.d(TAG, "metadata retrieved:" + metadata.getTitle());
//
//                            }
//                        })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.e(TAG, "Unable to retrieve metadata", e);
//                    }
//                });
//        // [END retrieve_metadata]
//    }
//
//    public MetadataChangeSet getMetadataChangeSet(String fileName, String mimeType) {
//        // Create the initial metadata - MIME type and title.
//        // Note that the user will be able to change the title later.
//        return new MetadataChangeSet.Builder()
//                .setMimeType(mimeType)
//                .setTitle(fileName)
//                .build();
//    }
//
//    /**
//     * Creates an {@link IntentSender} to start a dialog activity with configured {@link
//     * CreateFileActivityOptions} for user to create a new file in Drive.
//     * Saves file in root of users Drive
//     */
//    private Task<Void> createFileIntentSender(final Activity activity, DriveContents driveContents, File file, String mimeType) {
//        Log.i(TAG, "New contents created.");
//        OutputStream outputStream;
//        InputStream inputStream;
//        // Get an output stream for the contents.
//        outputStream = driveContents.getOutputStream();
//
//        try {
//            inputStream = new BufferedInputStream(new FileInputStream(file));
//            IoUtils.copy(inputStream, outputStream);
//        } catch (Exception e) {
//            // TODO - set up snackbar?
//            //Snackbar.make(findViewById(R.id.content_drive_view), "Unable to write file contents.", Snackbar.LENGTH_INDEFINITE);
//            Toast.makeText(activity, "Unable to write file contents.", Toast.LENGTH_LONG);
//        }
//
//        // Create the initial metadata - MIME type and title.
//        // Note that the user will be able to change the title later.
//        MetadataChangeSet metadataChangeSet = getMetadataChangeSet(file.getName(), mimeType);
//
//        // Set up options to configure and display the create file activity.
//        CreateFileActivityOptions createFileActivityOptions =
//                new CreateFileActivityOptions.Builder()
//                        .setInitialMetadata(metadataChangeSet)
//                        .setInitialDriveContents(driveContents)
//                        .build();
//
//        return mDriveClient
//                .newCreateFileActivityIntentSender(createFileActivityOptions)
//                .continueWith(
//                        new Continuation<IntentSender, Void>() {
//                            @Override
//                            public Void then(@NonNull Task<IntentSender> task) throws Exception {
//                                activity.startIntentSenderForResult(task.getResult(), REQUEST_CODE_CREATOR, null, 0, 0, 0);
//                                return null;
//                            }
//                        });
//    }
//
//    public void postNotification(String notificationContent, Class activityClass) {
//        NotificationCompat.Builder mBuilder =
//                new NotificationCompat.Builder(context)
//                        .setSmallIcon(R.drawable.ic_backup_status_bar)
//                        .setContentTitle(context.getString(R.string.app_name))
//                        .setContentText(notificationContent);
//        // Creates an explicit intent for an Activity in your app
//        Intent intent = new Intent(context, activityClass);
//
//        // The stack builder object can contain an artificial back stack for the started Activity.
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
//        // Adds the Intent that starts the Activity to the top of the stack
//        stackBuilder.addNextIntent(intent);
//        PendingIntent resultPendingIntent =
//                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT
//                );
//        mBuilder.setContentIntent(resultPendingIntent);
//        NotificationManager mNotificationManager =
//                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//
//        // mNotificationId is a unique integer your app uses to identify the
//        // notification. For example, to cancel the notification, you can pass its ID
//        // number to NotificationManager.cancel().
//        mNotificationManager.notify(0, mBuilder.build());
//    }

}
