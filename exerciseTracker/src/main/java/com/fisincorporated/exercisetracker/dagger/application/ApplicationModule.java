package com.fisincorporated.exercisetracker.dagger.application;


import android.content.Context;

import com.fisincorporated.exercisetracker.application.ActivityTrackerApplication;
import com.fisincorporated.exercisetracker.database.TrackerDatabaseHelper;
import com.fisincorporated.exercisetracker.ui.logger.GPSLocationManager;
import com.fisincorporated.exercisetracker.ui.utils.DisplayUnits;
import com.fisincorporated.exercisetracker.utility.PhotoUtils;
import com.fisincorporated.exercisetracker.utility.StatsUtil;
import com.jakewharton.rxrelay2.PublishRelay;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {

    @Provides
    Context provideContext(ActivityTrackerApplication application) {
        return application.getApplicationContext();
    }

    @Provides
    @Singleton
    TrackerDatabaseHelper provideTrackerDatabaseHelper(ActivityTrackerApplication application){
        return TrackerDatabaseHelper.getTrackerDatabaseHelper(application);
    }

    @Provides
    PhotoUtils providePhotoUtils(Context context){
        return new PhotoUtils(context);
    }

    @Provides
    @Singleton
    DisplayUnits provideDisplayUnits(Context context){
        return new DisplayUnits(context);
    }

    @Provides
    @Singleton
    StatsUtil provideStatsUtil(DisplayUnits displayUnits){
        return new StatsUtil(displayUnits);
    }

    @Provides
    @Singleton
    GPSLocationManager provideGpsLocationManager(Context context, StatsUtil statsUtil, PublishRelay<Object> publishRelay){
        return new GPSLocationManager(context, statsUtil, publishRelay);
    }

    @Provides
    @Singleton
    PublishRelay<Object> providePublishRelay() {
        return PublishRelay.create();
    }


}