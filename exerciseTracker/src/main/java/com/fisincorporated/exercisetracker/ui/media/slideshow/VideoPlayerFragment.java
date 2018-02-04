package com.fisincorporated.exercisetracker.ui.media.slideshow;

import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.fisincorporated.exercisetracker.R;

import java.io.File;

public class VideoPlayerFragment extends Fragment {

    private static final String TAG = VideoPlayerFragment.class.getSimpleName();
    private static final String MEDIA_FILE = "MediaFile";

    private Resources res;
    private String mediaFile = "";

    private MediaController mediaController = null;
    private VideoView videoView;
    private View photoViewLayout;
    private ImageView photoView;


    public static VideoPlayerFragment getInstance(String mediaPath) {
        VideoPlayerFragment videoPlayerFragment = new VideoPlayerFragment();
        videoPlayerFragment.createVideoBundle(mediaPath);
        return videoPlayerFragment;
    }

    public void createVideoBundle(String mediaFile) {
        Bundle bundle = new Bundle();
        bundle.putString(MEDIA_FILE, mediaFile);
        setArguments(bundle);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        lookForArguments(savedInstanceState);
        res = getResources();
    }

    // Need to be careful as logic below may need modification per the
    // circumstances
    private void lookForArguments(Bundle savedInstanceState) {
        Bundle bundle;
        if (getArguments() != null) {
            bundle = getArguments();
            mediaFile = bundle.getString(MEDIA_FILE);
        }
    }

    private String getDataSource() {
        return "file:///" + mediaFile;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.media_player_video, parent, false);
        videoView = (VideoView) v.findViewById(R.id.media_player_video_view);
        photoView = (ImageView) v.findViewById(R.id.media_play_photo_image);
        photoViewLayout = v.findViewById(R.id.media_player_photo_layout);
        photoViewLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playVideo();
            }
        });
        displayPhotoImage();
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    // Runs prior to onStart and onResume
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        stopVideoIfPlaying();
        displayPhotoImage();
    }

    private void stopVideoIfPlaying() {
        if (videoView != null) {
            videoView.stopPlayback();
            videoView.setVisibility(View.INVISIBLE);
        }
    }

    private void playVideo() {
        mediaController = new MediaController(getContext());
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);
        videoView.setVideoURI(Uri.parse(getDataSource()));
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoView.bringToFront();

            }
        });
        videoView.setVisibility(View.VISIBLE);
        videoView.start();
    }

    private void displayPhotoImage() {
        if (photoViewLayout != null) {
            if (photoView != null && photoView.getDrawable() == null) {
                Glide.with(this)
                        .load(Uri.fromFile(new File(mediaFile)))
                        .into(photoView);
            }
            photoViewLayout.bringToFront();

        }
    }

}
