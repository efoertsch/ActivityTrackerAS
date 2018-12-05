package com.fisincorporated.exercisetracker.application;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class AppPreferences {

    private static final String PREF_CURRENT_LER_ID = "PREF_CURRENT_LER_ID";
    private SharedPreferences sharedPreferences;
    private Context context;

    public AppPreferences(Context context, String activityTrackerPrefs) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(activityTrackerPrefs, MODE_PRIVATE);
    }


    public long getActivityId(){
        return sharedPreferences.getLong(PREF_CURRENT_LER_ID, -1);
    }

    public void setActivityId(long lerId) {
        sharedPreferences.edit().putLong(PREF_CURRENT_LER_ID, lerId).apply();
    }

    public void deleteActivityId(){
        sharedPreferences.edit().remove(PREF_CURRENT_LER_ID).apply();
    }
}
