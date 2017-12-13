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

    public static Completable getLocalBackupCompletable(Context context, String packageName, String fileName) {
        Completable completable = Completable.fromAction(() -> {
            try {
                copyDdFileToDownloadDirectory(context, packageName, fileName);
            } catch (Throwable throwable) {
                throw Exceptions.propagate(throwable);
            }
        });
        return completable;
    }

    /**
     * @param context
     * @param packageName app package name
     * @param fileName    name of file to be written to Download directory
     * @throws Throwable
     */
    @SuppressWarnings("resource")
    private static void copyDdFileToDownloadDirectory(Context context, String packageName, String fileName) throws Throwable {
        File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File data = Environment.getDataDirectory();
        if (sd.canWrite()) {
            String localFilePathAndName = "//data//" + packageName + "//databases//" + GlobalValues.DATABASE_NAME;
            File appFile = new File(data, localFilePathAndName);
            File externalFile = new File(sd, fileName);
            copyFile(appFile, externalFile);
        } else {
            throw new Throwable(context.getString(R.string.local_backup_directory_not_writeable, sd.getName()));
        }
    }

    private static void copyFile(File sourceFile, File destinationFile) throws Throwable {
        FileChannel src = null;
        FileChannel dst = null;
        try {
            src = new FileInputStream(sourceFile).getChannel();
            dst = new FileOutputStream(destinationFile).getChannel();
            dst.transferFrom(src, 0, src.size());
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

    public static Completable getRestoreLocalCompletable(Context context, String packageName, String fileName) {
        Completable completable = Completable.fromAction(() -> {
            try {
                copyDownloadFileToDbDirectory(context, packageName, fileName);
            } catch (Throwable throwable) {
                throw Exceptions.propagate(throwable);
            }
        });
        completable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        return completable;
    }

    @SuppressWarnings("resource")
    private static void copyDownloadFileToDbDirectory(Context context, String packageName, String fileName) throws Throwable {
        File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File data = Environment.getDataDirectory();
        if (sd.canRead()) {
            String localFilePathAndName = "//data//" + packageName + "//databases//" + GlobalValues.DATABASE_NAME;
            File appFile = new File(data, localFilePathAndName);
            File externalFile = new File(sd, fileName);
            copyFile(externalFile, appFile);
        } else {
            throw new Throwable(context.getString(R.string.local_backup_directory_not_readable, sd.getName()));
        }

    }

}
