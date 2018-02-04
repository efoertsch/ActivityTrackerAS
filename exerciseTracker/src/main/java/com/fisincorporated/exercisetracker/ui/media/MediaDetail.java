package com.fisincorporated.exercisetracker.ui.media;


import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

public class MediaDetail implements Comparable<MediaDetail>, Parcelable {

    private String mediaPath;
    private long dateTaken;
    private String latitude;
    private String longitude;
    private int mediaType;
    private String mimeType;

    public MediaDetail() {
    }

    public String getMediaPath() {
        return mediaPath;
    }

    public MediaDetail setMediaPath(String mediaPath) {
        this.mediaPath = mediaPath;
        return this;
    }

    public long getDateTaken() {
        return dateTaken;
    }

    public MediaDetail setDateTaken(long dateTaken) {
        this.dateTaken = dateTaken;
        return this;
    }

    public String getLatitude() {
        return latitude;
    }

    public MediaDetail setLatitude(String latitude) {
        this.latitude = latitude;
        return this;
    }

    public MediaDetail setLongitude(String longitude) {
        this.longitude = longitude;
        return this;
    }

    public String getLongitude() {
        return longitude;
    }

    public int getMediaType() {
        return mediaType;
    }

    public MediaDetail setMediaType(int mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    public String getMimeType() {
        return mimeType;
    }

    public MediaDetail setMimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    public MediaDetail setAsVideo() {
        mediaType = MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
        return this;
    }

    public MediaDetail setAsImage() {
        mediaType = MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
        return this;
    }

    public boolean isVideo() {
        return mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
    }

    public boolean isImage() {
        return mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mediaPath);
        dest.writeLong(dateTaken);
        dest.writeString(latitude);
        dest.writeString(longitude);
        dest.writeInt(mediaType);
        dest.writeString(mimeType);
    }

    private MediaDetail(Parcel in) {
        mediaPath = in.readString();
        dateTaken = in.readLong();
        latitude = in.readString();
        longitude = in.readString();
        mediaType = in.readInt();
        mimeType = in.readString();
    }


    public static final Creator<MediaDetail> CREATOR = new Creator<MediaDetail>() {
        @Override
        public MediaDetail createFromParcel(Parcel source) {
            return new MediaDetail(source);
        }

        @Override
        public MediaDetail[] newArray(int size) {
            return new MediaDetail[size];
        }
    };


    @Override
    public int compareTo(MediaDetail rhs) {
        // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
        return  getDateTaken() > rhs.getDateTaken() ? 1 : ( getDateTaken() < rhs.getDateTaken()) ? -1 : 0;
    }
}
