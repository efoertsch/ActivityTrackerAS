package com.fisincorporated.exercisetracker.ui.photos;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.ui.maps.PhotoPoint;

import java.util.ArrayList;

public class PhotoGridPagerAdapter <F extends Fragment> extends FragmentStatePagerAdapter {

    private final Class<F> fragmentClass;
    private ArrayList<PhotoPoint> photoPoints;


    public PhotoGridPagerAdapter(FragmentManager fm, Class<F> fragmentClass, ArrayList<PhotoPoint> photoPoints) {
        super(fm);
        this.fragmentClass = fragmentClass;
        this.photoPoints = photoPoints;

    }

    @Override
    public F getItem(int position) {
        F frag;
        try {
            Bundle args = new Bundle();
            args.putParcelableArrayList(GlobalValues.PHOTO_POINTS, photoPoints.get(position).getPhotoDetails());
            frag = fragmentClass.newInstance();
            frag.setArguments(args);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        return frag;
    }

    @Override
    public int getCount() {
       return photoPoints.size();
    }

}
