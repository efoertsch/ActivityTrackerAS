package com.fisincorporated.exercisetracker.ui.photos.slideshow;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;

public class ScreenSlidePageFragment extends Fragment {
    private String uri;
    //ChrisBanes Photoview
    private PhotoView photoView;


    public static ScreenSlidePageFragment getInstance(String uri) {
        ScreenSlidePageFragment screenSlidePageFragment = new ScreenSlidePageFragment();
        screenSlidePageFragment.uri = uri;
        return  screenSlidePageFragment;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(GlobalValues.PHOTO_URI, uri);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.photo_view, container, false);

        photoView = (PhotoView) rootView.findViewById(R.id.photo_view_imageview);
        if (uri == null && savedInstanceState != null) {
            uri = savedInstanceState.getString(GlobalValues.PHOTO_URI);
        }
        displayPhoto(uri);
        return rootView;
    }

    private void displayPhoto(String uri) {
        Glide.with(this)
                .load(Uri.fromFile(new File(uri)))
                .into(photoView);
    }
}
