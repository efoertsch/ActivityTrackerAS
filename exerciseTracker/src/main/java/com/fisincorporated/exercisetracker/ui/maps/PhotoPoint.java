package com.fisincorporated.exercisetracker.ui.maps;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class PhotoPoint implements Parcelable {
    private ArrayList<PhotoDetail> photoDetails = new ArrayList<>();
    private LatLng latlng;
    private long time;

    private PhotoPoint(){}

    public static PhotoPoint getInstance(long time, LatLng latLng){
        PhotoPoint photoPoint = new PhotoPoint();
        photoPoint.setTime(time);
        photoPoint.setLatlng(latLng);
        return photoPoint;
    }

    public ArrayList<PhotoDetail> getPhotoDetails() {
        return photoDetails;
    }

    public void setPhotoDetails(ArrayList<PhotoDetail> photoDetails) {
        this.photoDetails = photoDetails;
    }

    public LatLng getLatlng() {
        return latlng;
    }

    public void setLatlng(LatLng latlng) {
        this.latlng = latlng;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void addPhotoDetail(PhotoDetail photoDetail){
        photoDetails.add(photoDetail);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.photoDetails);
        dest.writeParcelable(this.latlng, flags);
        dest.writeLong(this.time);
    }

    protected PhotoPoint(Parcel in) {
        this.photoDetails = in.createTypedArrayList(PhotoDetail.CREATOR);
        this.latlng = in.readParcelable(LatLng.class.getClassLoader());
        this.time = in.readLong();
    }

    public static final Parcelable.Creator<PhotoPoint> CREATOR = new Parcelable.Creator<PhotoPoint>() {
        @Override
        public PhotoPoint createFromParcel(Parcel source) {
            return new PhotoPoint(source);
        }

        @Override
        public PhotoPoint[] newArray(int size) {
            return new PhotoPoint[size];
        }
    };
}
