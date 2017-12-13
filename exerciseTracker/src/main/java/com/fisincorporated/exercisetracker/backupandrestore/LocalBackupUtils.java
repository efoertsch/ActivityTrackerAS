package com.fisincorporated.exercisetracker.backupandrestore;

import android.content.Context;
import android.os.Environment;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.schedulers.Schedulers;

public class LocalBackupUtils {

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
            File downloadDir = getExternalStorageBackupDirectory();
            File dataDir = Environment.getDataDirectory();
            String  currentDBPath= "//data//" + GlobalValues.PACKAGE_NAME
                    + "//databases//" + backupFileName;
            if (downloadDir.canWrite()) {
                File appDb = new File(downloadDir, GlobalValues.DATABASE_PATH_AND_NAME);
                File backupFile = new File(downloadDir, backupFileName);
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

}
