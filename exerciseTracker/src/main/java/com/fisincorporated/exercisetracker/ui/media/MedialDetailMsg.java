package com.fisincorporated.exercisetracker.ui.media;



import java.util.ArrayList;

public class MedialDetailMsg {
    private ArrayList<MediaDetail> mediaDetails;

    public MedialDetailMsg(ArrayList<MediaDetail> mediaDetails) {
        this.mediaDetails = mediaDetails;
    }

    public ArrayList<MediaDetail> getMediaDetails() {
        return mediaDetails;
    }
}
