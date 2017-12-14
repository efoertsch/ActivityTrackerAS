package com.fisincorporated.exercisetracker.ui.maps;


public class PhotoDetail {

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


}
