package com.fisincorporated.exercisetracker.rxdrive;

// From https://github.com/francescocervone/RxDrive/blob/develop/rxdrive/src/main/java/com/francescocervone/rxdrive
public class Progress {
    private long mBytesDownloaded;
    private long mBytesExpected;

    Progress(long bytesDownloaded, long bytesExpected) {
        mBytesDownloaded = bytesDownloaded;
        mBytesExpected = bytesExpected;
    }

    public long getBytesDownloaded() {
        return mBytesDownloaded;
    }

    public long getBytesExpected() {
        return mBytesExpected;
    }

    public double getPercentage() {
        if (mBytesExpected == -1) {
            return 100;
        }
        return (mBytesDownloaded * 100d) / mBytesExpected;
    }

    public boolean isCompleted() {
        return mBytesExpected == mBytesDownloaded;
    }
}