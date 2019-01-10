package com.fisincorporated.exercisetracker.dagger.application;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.fisincorporated.exercisetracker.application.ActivityTrackerApplication;
import com.fisincorporated.exercisetracker.application.AppPreferences;
import com.fisincorporated.exercisetracker.database.TrackerDatabaseHelper;
import com.fisincorporated.exercisetracker.ui.utils.DisplayUnits;
import com.fisincorporated.exercisetracker.utility.PhotoUtils;
import com.fisincorporated.exercisetracker.utility.StatsUtil;
import com.fisincorporated.exercisetracker.utility.TimeZoneUtils;
import com.jakewharton.rxrelay2.PublishRelay;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {

    private static final String ACTIVITYTRACKER_PREFS = "ACTIVITYTRACKER_PREFS";

    @Provides
    @Singleton
    Context provideContext(ActivityTrackerApplication application) {
        return application.getApplicationContext();
    }


    @Provides
    @Named(ACTIVITYTRACKER_PREFS)
    public String providesAppSharedPreferencesName() {
        return ACTIVITYTRACKER_PREFS;
    }

    @Provides
    @Singleton
    public AppPreferences provideAppPreferences(Context appContext) {
        return new AppPreferences(appContext, ACTIVITYTRACKER_PREFS);
    }

    //TODO replace with above AppPreferences
    @Provides
    @Singleton
    SharedPreferences providesSharedPreferences(Context appContext) {
        return PreferenceManager.getDefaultSharedPreferences(appContext);
    }

    @Provides
    @Singleton
    TrackerDatabaseHelper provideTrackerDatabaseHelper(ActivityTrackerApplication application) {
        return TrackerDatabaseHelper.getTrackerDatabaseHelper(application);
    }

    @Provides
    PhotoUtils providePhotoUtils(Context context) {
        return new PhotoUtils(context);
    }

    //TODO combine into AppPreferences or Repository
    @Provides
    @Singleton
    DisplayUnits provideDisplayUnits(SharedPreferences sharedPreferences, Context context) {
        return new DisplayUnits(sharedPreferences, context);
    }

    @Provides
    @Singleton
    StatsUtil provideStatsUtil(DisplayUnits displayUnits, Context context) {
        return new StatsUtil(displayUnits, context);
    }

    @Provides
    @Singleton
    TimeZoneUtils provideTimeZoneUtils() {
        return new TimeZoneUtils();
    }

    // RxJava Bus to replace callback interfaces
    @Provides
    @Singleton
    PublishRelay<Object> providePublishRelay() {
        return PublishRelay.create();
    }




}