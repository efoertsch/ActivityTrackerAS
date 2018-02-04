package com.fisincorporated.exercisetracker.ui.media.mediagrid;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.ui.media.MediaPoint;

import java.util.ArrayList;

public class MediaGridPagerAdapter<F extends Fragment> extends FragmentStatePagerAdapter {

    private final Class<F> fragmentClass;
    private ArrayList<MediaPoint> mediaPoints;


    public MediaGridPagerAdapter(FragmentManager fm, Class<F> fragmentClass, ArrayList<MediaPoint> mediaPoints) {
        super(fm);
        this.fragmentClass = fragmentClass;
        this.mediaPoints = mediaPoints;

    }

    @Override
    public F getItem(int position) {
        F frag;
        try {
            Bundle args = new Bundle();
            args.putParcelableArrayList(GlobalValues.PHOTO_POINTS, mediaPoints.get(position).getMediaDetails());
            frag = fragmentClass.newInstance();
            frag.setArguments(args);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        return frag;
    }

    @Override
    public int getCount() {
       return mediaPoints.size();
    }

}
