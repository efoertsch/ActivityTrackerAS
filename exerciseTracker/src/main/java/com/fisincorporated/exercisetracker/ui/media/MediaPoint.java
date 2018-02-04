package com.fisincorporated.exercisetracker.ui.media;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MediaPoint implements Parcelable {
    private ArrayList<MediaDetail> mediaDetails = new ArrayList<>();
    private LatLng latlng;
    private long time;

    private MediaPoint(){}

    public static MediaPoint getInstance(long time, LatLng latLng){
        MediaPoint mediaPoint = new MediaPoint();
        mediaPoint.setTime(time);
        mediaPoint.setLatlng(latLng);
        return mediaPoint;
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

    protected MediaPoint(Parcel in) {
        this.mediaDetails = in.createTypedArrayList(MediaDetail.CREATOR);
        this.latlng = in.readParcelable(LatLng.class.getClassLoader());
        this.time = in.readLong();
    }

    public static final Parcelable.Creator<MediaPoint> CREATOR = new Parcelable.Creator<MediaPoint>() {
        @Override
        public MediaPoint createFromParcel(Parcel source) {
            return new MediaPoint(source);
        }

        @Override
        public MediaPoint[] newArray(int size) {
            return new MediaPoint[size];
        }
    };
}
