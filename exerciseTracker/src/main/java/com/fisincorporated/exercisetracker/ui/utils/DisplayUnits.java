package com.fisincorporated.exercisetracker.ui.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;

public class DisplayUnits {

    private static DisplayUnits displayUnits;
    private static Context context;
    private static String imperialMetric;
    private static String imperial;
    private static String feetMeters;
    private static String milesKm;
    private static String mphKph;
     
    private DisplayUnits() {
    }

    private DisplayUnits(Context context) {
        DisplayUnits.context = context;
    }

    /**
     * Initialize at app startup
     * @param context
     */
    public static void initialize(Context context) {
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

    public static String getImperial() {
        return imperial;
    }

    public static String getImperialMetric() {
        return imperialMetric;
    }

    public static String getFeetMeters() {
        return feetMeters;
    }

    public static String getMilesKm() {
        return milesKm;
    }

    public static String getMphKph() {
        return mphKph;
    }

    public static boolean isImperialDisplay(){
        return (imperialMetric.equalsIgnoreCase(context.getString(R.string.imperial)));
    }
}
