package com.fisincorporated.exercisetracker.ui.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;

import javax.inject.Inject;

public class DisplayUnits {

    private DisplayUnits displayUnits;
    private Context context;
    private String imperialMetric;
    private String imperial;
    private String feetMeters;
    private String milesKm;
    private String mphKph;

    @Inject
    public DisplayUnits(Context context) {
        this.context = context;
        initialize(context);
    }

    /**
     * Initialize at app startup
     * @param context
     */
    private void initialize(Context context) {
        if (displayUnits == null) {
            displayUnits = new DisplayUnits(context);
            imperial = context.getString(R.string.imperial);
            displayUnits.getUnits();
        }
    }

    SharedPreferences.OnSharedPreferenceChangeListener prefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            if(key.equals(GlobalValues.DISPLAY_UNITS_PREFERENCE_KEY)) {
                getUnits();
            }
        }
    };

    private void getUnits() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        imperialMetric = sharedPref.getString(context.getString(R.string.display_units), context.getString(R.string.imperial));
        assignUnits();
        sharedPref.registerOnSharedPreferenceChangeListener(prefsListener);
    }

    private void assignUnits() {
        if (imperialMetric.equalsIgnoreCase(context.getString(R.string.imperial))) {
            feetMeters = context.getString(R.string.feet_abbrev);
            milesKm = context.getString(R.string.miles);
            mphKph = context.getString(R.string.miles_per_hour_abbrev);
        } else {
            feetMeters = context.getString(R.string.meters_abbrev);
            milesKm = context.getString(R.string.kilometers_abbrev);
            mphKph = context.getString(R.string.kilometers_per_hours_abbrev);
        }
    }

    public  String getImperial() {
        return imperial;
    }

    public  String getImperialMetric() {
        return imperialMetric;
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
        return (imperialMetric.equalsIgnoreCase(context.getString(R.string.imperial)));
    }
}
