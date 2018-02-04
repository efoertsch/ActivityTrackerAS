package com.fisincorporated.exercisetracker.ui.photos;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class PhotoPoint implements Parcelable {
    private ArrayList<MediaDetail> mediaDetails = new ArrayList<>();
    private LatLng latlng;
    private long time;

    private PhotoPoint(){}

    public static PhotoPoint getInstance(long time, LatLng latLng){
        PhotoPoint photoPoint = new PhotoPoint();
        photoPoint.setTime(time);
        photoPoint.setLatlng(latLng);
        return photoPoint;
    }

    public ArrayList<MediaDetail> getMediaDetails() {
        return mediaDetails;
    }

    public void setMediaDetails(ArrayList<MediaDetail> mediaDetails) {
        this.mediaDetails = mediaDetails;
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

    public void addPhotoDetail(MediaDetail mediaDetail){
        mediaDetails.add(mediaDetail);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.mediaDetails);
        dest.writeParcelable(this.latlng, flags);
        dest.writeLong(this.time);
    }

    protected PhotoPoint(Parcel in) {
        this.mediaDetails = in.createTypedArrayList(MediaDetail.CREATOR);
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
