package com.fisincorporated.exercisetracker.backupandrestore;

import android.content.Context;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.utility.CustomException;
import com.fisincorporated.exercisetracker.utility.IoUtils;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.drive.Drive;
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
import com.google.android.gms.tasks.Tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

//TODO Extract out completables to create generic drive completables (or Single<>)
public class GoogleDriveUtil {

    private static final String TAG = GoogleDriveUtil.class.getSimpleName();

    public static Completable getRestoreFromDriveCompletable(Context context, GoogleSignInAccount signInAccount) {
        DriveResourceClient driveResourceClient = Drive.getDriveResourceClient(context, signInAccount);
        GoogleDriveFile googleDriveFile = GoogleDriveFile.getInstance();
        googleDriveFile.setDriveResourceClient(driveResourceClient)
                .setContext(context)
                .setFolderName(context.getString(R.string.app_name))
                .setDriveFileName(GlobalValues.DATABASE_NAME)
                .setMimeType(GlobalValues.SQLITE_MIME_TYPE)
                .setLocalFile(getDatabaseFile(context));

        return GoogleDriveUtil.downloadFileFromDrive(googleDriveFile);
    }

    public static Completable getBackupToDriveCompletable(Context context, GoogleSignInAccount signInAccount){
        DriveResourceClient driveResourceClient = Drive.getDriveResourceClient(context, signInAccount);
        GoogleDriveFile googleDriveFile = GoogleDriveFile.getInstance();
        googleDriveFile.setDriveResourceClient(driveResourceClient)
                .setContext(context)
                .setFolderName(context.getString(R.string.app_name))
                .setDriveFileName(GlobalValues.DATABASE_NAME)
                .setMimeType(GlobalValues.SQLITE_MIME_TYPE)
                .setLocalFile(getDatabaseFile(context));
        return GoogleDriveUtil.uploadFileToDrive(googleDriveFile);
    }

    private static File getDatabaseFile(Context context) {
        String currentDBPath = context.getDatabasePath(GlobalValues.DATABASE_NAME).getAbsolutePath();
        File currentDB = new File(currentDBPath);
        return currentDB;
    }

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
                throw new CustomException(googleDriveFile.getContext().getString(R.string.drive_root_folder_not_found), e);
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
                throw new CustomException(googleDriveFile.getContext().getString(R.string.unable_to_create_new_folder
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
                    googleDriveFile.setDriveFile(driveFile);
                } else {
                    driveContents = Tasks.await(googleDriveFile.getDriveResourceClient().createContents());
                }
                googleDriveFile.setDriveContents(driveContents);
            } catch (Exception e) {
                throw new CustomException(googleDriveFile.getContext().getString(R.string.unable_to_create_drivecontents), e);
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
                    // create file will close file once file written
                    googleDriveFile.setDriveFile(Tasks.await(googleDriveFile.getDriveResourceClient()
                            .createFile(googleDriveFile.getDriveFileFolder(), changeSet, googleDriveFile.getDriveContents())));
                } else {
                    // backup file previous existed so commit (which also closes file)
                    Tasks.await(googleDriveFile.getDriveResourceClient().commitContents(googleDriveFile.getDriveContents(), null));
                }
                IoUtils.closeStream(inputStream);

            } catch (Exception e) {
                throw new CustomException((googleDriveFile.getContext().getString(R.string.unable_to_write_to_drive_file, googleDriveFile.getLocalFile().getName())), e);
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
                    throw new CustomException(googleDriveFile.getContext().getString(R.string.no_drive_backup_available));
                }
            } catch (Exception e) {
                throw new CustomException(googleDriveFile.getContext().getString(R.string.no_drive_backup_available), e);
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
                    throw new CustomException(googleDriveFile.getContext().getString(R.string.no_drive_backup_available));
                }
            } catch (Exception e) {
                throw new CustomException(googleDriveFile.getContext().getString(R.string.no_drive_backup_available), e);
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
                throw new CustomException(googleDriveFile.getContext().getString(R.string.an_error_occurred_reading_file_from_drive, googleDriveFile.getLocalFile().getName()), e);
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

}
