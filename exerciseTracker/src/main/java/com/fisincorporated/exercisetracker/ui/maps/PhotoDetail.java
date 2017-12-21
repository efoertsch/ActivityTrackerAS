package com.fisincorporated.exercisetracker.ui.maps;


import android.os.Parcel;
import android.os.Parcelable;

public class PhotoDetail implements Parcelable {

    private String photoPath;
    private long dateTaken;
    private String latitude;
    private String longitude;

    public String getPhotoPath() {
        return photoPath;
    }

    public PhotoDetail setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
        return this;
    }

    public long getDateTaken() {
        return dateTaken;
    }

    public PhotoDetail  setDateTaken(long dateTaken) {
        this.dateTaken = dateTaken;
        return this;
    }

    public String getLatitude() {
        return latitude;
    }

    public PhotoDetail setLatitude(String latitude) {
        this.latitude = latitude;
        return this;
    }

    public PhotoDetail setLongitude(String longitude) {
        this.longitude = longitude;
        return this;
    }

    public String getLongitude() {
        return longitude;
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.photoPath);
        dest.writeLong(this.dateTaken);
        dest.writeString(this.latitude);
        dest.writeString(this.longitude);
    }

    public PhotoDetail() {
    }

    protected PhotoDetail(Parcel in) {
        this.photoPath = in.readString();
        this.dateTaken = in.readLong();
        this.latitude = in.readString();
        this.longitude = in.readString();
    }

    public static final Parcelable.Creator<PhotoDetail> CREATOR = new Parcelable.Creator<PhotoDetail>() {
        @Override
        public PhotoDetail createFromParcel(Parcel source) {
            return new PhotoDetail(source);
        }

        @Override
        public PhotoDetail[] newArray(int size) {
            return new PhotoDetail[size];
        }
    };
}
