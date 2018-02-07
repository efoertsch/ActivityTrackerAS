package com.fisincorporated.exercisetracker.application;

import android.database.sqlite.SQLiteDatabase;

import com.bumptech.glide.request.target.ViewTarget;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.dagger.application.ApplicationComponent;
import com.fisincorporated.exercisetracker.dagger.application.DaggerApplicationComponent;
import com.fisincorporated.exercisetracker.database.TrackerDatabaseHelper;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;


public class ActivityTrackerApplication extends DaggerApplication {

    private SQLiteDatabase database = null;

    @Inject
    TrackerDatabaseHelper trackerDatabaseHelper;

    @Override
    public void onCreate() {
        super.onCreate();
      //  createDaggerInjections();
        opendDatabase();

        // To be able to use setTag with Glide
        ViewTarget.setTagId(R.id.glide_tag);
    }
//
//    protected void createDaggerInjections() {
//        DaggerApplicationComponent
//                .builder()
//                .application(this)
//                .build()
//                .inject(this);
//    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        ApplicationComponent appComponent = DaggerApplicationComponent.builder().application(this).build();
        appComponent.inject(this);
        return appComponent;
    }

    public void opendDatabase() {
        database = trackerDatabaseHelper.getReadableDatabase();
    }

    @Override
    public void onTerminate() {
        if (database != null) {
            database.close();
        }
        super.onTerminate();
    }

}
