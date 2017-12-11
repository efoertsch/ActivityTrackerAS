package com.fisincorporated.exercisetracker.ui.drive;



import android.content.Context;

import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.File;

public class GoogleDriveFile {

    private DriveResourceClient driveResourceClient;
    private DriveFolder parentDriveFolder;
    private String folderName;
    private DriveFolder driveFileFolder;
    private String driveFileName;
    private DriveFile driveFile;
    private MetadataChangeSet metaDataChangeSet;
    private String mimeType;
    private DriveContents driveContents;
    private File localFile;
    private boolean starred;
    private Context context;

    private GoogleDriveFile() {
    }

    public static GoogleDriveFile getInstance() {
        return new GoogleDriveFile();
    }

    public DriveResourceClient getDriveResourceClient() {
        return driveResourceClient;
    }

    public GoogleDriveFile setDriveResourceClient(DriveResourceClient driveResourceClient) {
        this.driveResourceClient = driveResourceClient;
        return this;
    }

    public DriveFolder getParentDriveFolder() {
        return parentDriveFolder;
    }

    public GoogleDriveFile setParentDriveFolder(DriveFolder parentDriveFolder) {
        this.parentDriveFolder = parentDriveFolder;
        return this;
    }

    public String getFolderName() {
        return folderName;
    }

    public GoogleDriveFile setFolderName(String folderName) {
        this.folderName = folderName;
        return this;
    }

    public DriveFolder getDriveFileFolder() {
        return driveFileFolder;
    }

    public GoogleDriveFile setDriveFileFolder(DriveFolder driveFileFolder) {
        this.driveFileFolder = driveFileFolder;
        return this;
    }

    public String getDriveFileName() {
        return driveFileName;
    }

    public GoogleDriveFile setDriveFileName(String driveFileName) {
        this.driveFileName = driveFileName;
        return this;
    }

    public DriveFile getDriveFile() {
        return driveFile;
    }

    public GoogleDriveFile setDriveFile(DriveFile driveFile) {
        this.driveFile = driveFile;
        return this;
    }

    public MetadataChangeSet getMetaDataChangeSet() {
        return metaDataChangeSet;
    }

    public GoogleDriveFile setMetaDataChangeSet(MetadataChangeSet metaDataChangeSet) {
        this.metaDataChangeSet = metaDataChangeSet;
        return this;
    }

    public String getMimeType() {
        return mimeType;
    }

    public GoogleDriveFile setMimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    public DriveContents getDriveContents() {
        return driveContents;
    }

    public GoogleDriveFile setDriveContents(DriveContents driveContents) {
        this.driveContents = driveContents;
        return this;
    }

    public File getLocalFile() {
        return localFile;
    }

    public GoogleDriveFile setLocalFile(File localFile) {
        this.localFile = localFile;
        return this;
    }


    public boolean isStarred() {
        return starred;
    }

    public GoogleDriveFile setStarred(boolean starred) {
        this.starred = starred;
        return this;
    }

    public Context getContext() {
        return context;
    }

    public GoogleDriveFile setContext(Context context) {
        this.context = context;
        return this;
    }
}
