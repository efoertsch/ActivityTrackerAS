package com.fisincorporated.exercisetracker.backupandrestore;

public class GoogleDriveException extends Exception {


    public GoogleDriveException(String message) {
        super(message, null);
    }
    public GoogleDriveException(String message, Exception e) {
        super(message, e);
    }
}
