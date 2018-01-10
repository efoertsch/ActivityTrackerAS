package com.fisincorporated.exercisetracker.application;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.bumptech.glide.request.target.ViewTarget;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.database.TrackerDatabaseHelper;
import com.fisincorporated.exercisetracker.ui.utils.DisplayUnits;


public class ActivityTrackerApplication extends Application {

    private static final String GPS_LOGGING = "GPS_LOGGING";
    static private ActivityTrackerApplication activityTrackerApplication;
    private TrackerDatabaseHelper databaseHelper = null;
    private SQLiteDatabase database = null;


    @Override
    public void onCreate() {
        super.onCreate();
        getDatabaseSetup();
        setupDisplayUnits();
        activityTrackerApplication = this;

        // To be able to use setTag with Glide
        ViewTarget.setTagId(R.id.glide_tag);
    }

    private void setupDisplayUnits() {
        DisplayUnits.initialize(this);
    }

    // TODO use Dagger injection
    public void getDatabaseSetup() {
        databaseHelper = TrackerDatabaseHelper
                .getTrackerDatabaseHelper(this);
        // do this to fire any table changes
        database = databaseHelper.getReadableDatabase();
    }

    @Override
    public void onTerminate() {
        if (database != null) {
            database.close();
        }
        super.onTerminate();
    }

}
