package com.fisincorporated.exercisetracker.ui.settings;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.ui.master.ExerciseDaggerActivity;

public class SettingsActivity extends ExerciseDaggerActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityTitle(R.string.settings);
    }

    @Override
    protected Fragment createFragment() {
        SettingsFragment settingsFragment = SettingsFragment.newInstance(getIntent().getExtras());
        return settingsFragment;
    }


}