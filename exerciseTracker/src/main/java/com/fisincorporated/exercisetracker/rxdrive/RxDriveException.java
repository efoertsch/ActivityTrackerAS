package com.fisincorporated.exercisetracker.rxdrive;

import com.google.android.gms.common.api.Status;

// From https://github.com/francescocervone/RxDrive/blob/develop/rxdrive/src/main/java/com/francescocervone/rxdrive
public class RxDriveException extends RuntimeException {

    private Status mStatus;

    public RxDriveException(Status status) {

        mStatus = status;
    }

    public Status getStatus() {
        return mStatus;
    }
}