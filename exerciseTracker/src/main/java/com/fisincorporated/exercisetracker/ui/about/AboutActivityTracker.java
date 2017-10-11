package com.fisincorporated.exercisetracker.ui.about;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.fisincorporated.exercisetracker.ExerciseMasterFragmentActivity;


public class AboutActivityTracker extends ExerciseMasterFragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected   Fragment createFragment(){
        return new AboutFragment();
    }

}
