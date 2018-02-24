package com.fisincorporated.exercisetracker.ui.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;

import javax.inject.Inject;

public class DisplayUnits {

    private SharedPreferences sharedPreferences;
    private Resources resources;
    private String imperialMetric;
    private String feetMeters;
    private String milesKm;
    private String mphKph;

    @Inject
    public DisplayUnits(SharedPreferences sharedPreferences, Context context) {
        this.sharedPreferences = sharedPreferences;
        resources = context.getResources();
        getUnits();
    }



    private SharedPreferences.OnSharedPreferenceChangeListener prefsListener = (prefs, key) -> {
        if(key.equals(GlobalValues.DISPLAY_UNITS_PREFERENCE_KEY)) {
            getUnits();
        }
    };

    private void getUnits() {
        imperialMetric = sharedPreferences.getString(resources.getString(R.string.display_units)
                , resources.getString(R.string.imperial));
        assignUnits();
        sharedPreferences.registerOnSharedPreferenceChangeListener(prefsListener);
    }

    private void assignUnits() {
        if (imperialMetric.equalsIgnoreCase(resources.getString(R.string.imperial))) {
            feetMeters = resources.getString(R.string.feet_abbrev);
            milesKm = resources.getString(R.string.miles);
            mphKph = resources.getString(R.string.miles_per_hour_abbrev);
        } else {
            feetMeters = resources.getString(R.string.meters_abbrev);
            milesKm = resources.getString(R.string.kilometers_abbrev);
            mphKph = resources.getString(R.string.kilometers_per_hours_abbrev);
        }
    }

    public  String getFeetMeters() {
        return feetMeters;
    }

    public  String getMilesKm() {
        return milesKm;
    }

    public  String getMphKph() {
        return mphKph;
    }

    public  boolean isImperialDisplay(){
        return (imperialMetric.equalsIgnoreCase(resources.getString(R.string.imperial)));
    }
}
