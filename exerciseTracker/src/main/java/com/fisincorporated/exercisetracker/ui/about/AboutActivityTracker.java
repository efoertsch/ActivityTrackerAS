package com.fisincorporated.exercisetracker.ui.about;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.ui.master.ExerciseMasterActivity;


public class AboutActivityTracker extends ExerciseMasterActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityTitle(R.string.activity_about);
    }

    protected   Fragment createFragment(){
        return new AboutFragment();
    }

}
