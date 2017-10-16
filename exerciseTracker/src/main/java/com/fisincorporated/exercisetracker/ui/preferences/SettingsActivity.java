package com.fisincorporated.exercisetracker.ui.preferences;

import android.support.v4.app.Fragment;

import com.fisincorporated.exercisetracker.ExerciseMasterFragmentActivity;

public class SettingsActivity extends ExerciseMasterFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new SettingsFragment();
    }

}