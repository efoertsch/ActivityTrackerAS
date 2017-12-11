package com.fisincorporated.exercisetracker.ui.backuprestore;

import android.content.Context;
import android.os.Environment;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.rxdrive.IOUtils;
import com.fisincorporated.exercisetracker.rxdrive.RxDrive;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.drive.query.SortOrder;
import com.google.android.gms.drive.query.SortableField;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.schedulers.Schedulers;

public class BackupUtils {

    public static Completable getLocalBackupCompletable(Context context, String backupFileName) {
        Completable completable = Completable.fromAction(() -> {
            try {
                createDbBackupOnLocal(context, backupFileName);
            } catch (Throwable throwable) {
                throw Exceptions.propagate(throwable);
            }
        });
        return completable;
    }

    public static void createDbBackupOnLocal(Context context, String backupFileName) throws
            Throwable {
        FileChannel src = null;
        FileChannel dst = null;
        try {
            File sd = getExternalStorageBackupDirectory();
            if (sd.canWrite()) {
                File appDb = new File(GlobalValues.DATABASE_PATH_AND_NAME);
                File backupFile = new File(sd, backupFileName);
                src = new FileInputStream(appDb).getChannel();
                dst = new FileOutputStream(backupFile).getChannel();
                dst.transferFrom(src, 0, src.size());
            } else {
                throw new Throwable(context.getString(R.string.local_backup_directory_not_writeable, getExternalStorageBackupDirectory().getName()));
            }
        } finally {
            try {
                if (src != null) src.close();
            } catch (Exception e) {
            }
            try {
                if (dst != null) dst.close();
            } catch (Exception e) {
            }

        }
    }

    // RxDrive passed in must be in connected state with Scope Drive.SCOPE_FILE
    public static Completable getDriveBackupCompletable(Context context, RxDrive rxDrive) {
        File dbFile = getDatabaseFile(context);
        return rxDrive.createFolder(rxDrive.getRootFolder(), context.getString(R.string.app_name))
                .flatMap(driveFolder ->
                        rxDrive.createFile(driveFolder, dbFile
                                , GlobalValues.DATABASE_NAME, GlobalValues.SQLITE_MIME_TYPE))
                .flatMap(driveId -> rxDrive.getMetadata(driveId.asDriveResource()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).toCompletable();

    }

    // RxDrive passed in must be in connected state with Scope Drive.SCOPE_FILE
    public static Completable getRestoreDriveCompletable(Context context, RxDrive rxDrive) {
        Query query = new Query.Builder()
                .addFilter(Filters.and(Filters.eq(SearchableField.MIME_TYPE, GlobalValues.SQLITE_MIME_TYPE),
                        Filters.eq(SearchableField.TITLE, GlobalValues.DATABASE_NAME)
                        , Filters.eq(SearchableField.TRASHED, false)))
                .setSortOrder(new SortOrder.Builder().addSortDescending(SortableField.MODIFIED_DATE).build())
                .build();

        return rxDrive.query(query)
                .flatMapObservable(Observable::fromIterable)
                .flatMapSingle(driveId -> {
                    if (driveId != null) {
                        return rxDrive.getMetadata(driveId.asDriveResource());
                    } else {
                        throw new FileNotFoundException();
                    }
                })
                .toList()
                .filter(list -> !list.isEmpty())
                .map(list -> {
                    if (list.size() > 0) {
                        return list.get(0);
                    } else {
                        throw new FileNotFoundException();
                    }
                })
                .flatMapSingle(metadata -> rxDrive.open(metadata.getDriveId()))
                .map(inputStream -> {
                    if (inputStream != null) {
                        return BackupUtils.restoreDbFromDriveBackup(context, inputStream);
                    } else {
                        throw new FileNotFoundException();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).toCompletable();
    }

    public static String restoreDbFromDriveBackup(Context context, InputStream inputStream) throws IOException {
        FileOutputStream fileOutputStream = null;
        try {
            File appDB = new File(GlobalValues.DATABASE_PATH_AND_NAME);
            fileOutputStream = new FileOutputStream(appDB);
            IOUtils.copy(inputStream, fileOutputStream);
            return context.getString(R.string.activitytracker_database_restored);
        } finally {
            try {
                if (inputStream != null) inputStream.close();
            } catch (Exception e) {
            }
            try {
                if (fileOutputStream != null) fileOutputStream.close();
            } catch (Exception e) {
            }
        }
    }

    public static Completable getRestoreLocalCompletable(Context context) {
        Completable completable = Completable.fromAction(() -> {
            try {
                restoreDbFromLocalBackup(context);
            } catch (Throwable throwable) {
                throw Exceptions.propagate(throwable);
            }
        });
        completable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        return completable;
    }

    public static void restoreDbFromLocalBackup(Context context) throws Throwable {
        FileChannel src = null;
        FileChannel dst = null;
        String backupFileName = GlobalValues.DATABASE_NAME;
        try {
            File sd = getExternalStorageBackupDirectory();
            if (sd.canRead()) {
                File appDB = new File(GlobalValues.DATABASE_PATH_AND_NAME);
                File backupFile = new File(sd, backupFileName);
                src = new FileInputStream(backupFile).getChannel();
                dst = new FileOutputStream(appDB).getChannel();
                dst.transferFrom(src, 0, src.size());
            } else {
                throw new Throwable(context.getString(R.string.local_backup_directory_not_readable, getExternalStorageBackupDirectory().getName()));
            }
        } finally {
            try {
                if (src != null) src.close();
            } catch (Exception e) {
            }
            try {
                if (dst != null) dst.close();
            } catch (Exception e) {
            }

        }
    }


    public static File getExternalStorageBackupDirectory() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }

    private static File getDatabaseFile(Context context) {
        String currentDBPath = context.getDatabasePath(GlobalValues.DATABASE_NAME).getAbsolutePath();
        File currentDB = new File(currentDBPath);
        return currentDB;
    }

}
