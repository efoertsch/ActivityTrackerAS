package com.fisincorporated.exercisetracker.application;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.database.TrackerDatabaseHelper;


public class ActivityTrackerApplication extends Application {

    private static final String GPS_LOGGING = "GPS_LOGGING";
    static private ActivityTrackerApplication activityTrackerApplication;
    private TrackerDatabaseHelper databaseHelper = null;
    private SQLiteDatabase database = null;


    @Override
    public void onCreate() {
        super.onCreate();
        getDatabaseSetup();
        activityTrackerApplication = this;
    }

    // TODO use Dagger injection
    public void getDatabaseSetup() {
        databaseHelper = TrackerDatabaseHelper
                .getTrackerDatabaseHelper(this);
        database = databaseHelper.getReadableDatabase();
        GlobalValues.DATABASE_PATH_AND_NAME = this.getDatabasePath(GlobalValues.DATABASE_NAME).getPath();
    }

    @Override
    public void onTerminate() {
        if (database != null) {
            database.close();
        }
        super.onTerminate();
    }

    public static ActivityTrackerApplication getActivityTrackerApplication() {
        return activityTrackerApplication;
    }

    public TrackerDatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

    public SQLiteDatabase getDatabase() {
        return database;
    }

}
