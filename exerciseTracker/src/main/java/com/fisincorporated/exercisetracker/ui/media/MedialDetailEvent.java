package com.fisincorporated.exercisetracker.ui.media;



import java.util.ArrayList;

public class MedialDetailEvent {
    private ArrayList<MediaDetail> mediaDetails;

    public MedialDetailEvent(ArrayList<MediaDetail> mediaDetails) {
        this.mediaDetails = mediaDetails;
    }

    public ArrayList<MediaDetail> getMediaDetails() {
        return mediaDetails;
    }
}
