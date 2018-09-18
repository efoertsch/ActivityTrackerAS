package com.fisincorporated.exercisetracker.dagger.application;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.fisincorporated.exercisetracker.application.ActivityTrackerApplication;
import com.fisincorporated.exercisetracker.database.TrackerDatabaseHelper;
import com.fisincorporated.exercisetracker.ui.logger.GPSLocationManager;
import com.fisincorporated.exercisetracker.ui.utils.DisplayUnits;
import com.fisincorporated.exercisetracker.utility.PhotoUtils;
import com.fisincorporated.exercisetracker.utility.StatsUtil;
import com.fisincorporated.exercisetracker.utility.TimeZoneUtils;
import com.jakewharton.rxrelay2.PublishRelay;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {

    @Provides
    @Singleton
    Context provideContext(ActivityTrackerApplication application) {
        return application.getApplicationContext();
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

    @Provides
    @Singleton
    GPSLocationManager provideGpsLocationManager(Context context, StatsUtil statsUtil
            , PublishRelay<Object> publishRelay
            , TrackerDatabaseHelper trackerDatabaseHelper, TimeZoneUtils timeZoneUtils) {
        return new GPSLocationManager(context, statsUtil, publishRelay, trackerDatabaseHelper, timeZoneUtils);
    }

    // RxJava Bus to replace callback interfaces
    @Provides
    @Singleton
    PublishRelay<Object> providePublishRelay() {
        return PublishRelay.create();
    }


    //TODO combine into AppPreferences or Repository
    @Provides
    @Singleton
    SharedPreferences providesSharedPreferences(Context appContext) {
        return PreferenceManager.getDefaultSharedPreferences(appContext);
    }

}