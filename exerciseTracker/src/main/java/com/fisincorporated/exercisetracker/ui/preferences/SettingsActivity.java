package com.fisincorporated.exercisetracker.ui.preferences;

import android.os.Bundle;
import android.support.v4.app.SupportActivity;

public class SettingsActivity extends SupportActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}