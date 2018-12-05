package com.fisincorporated.exercisetracker.application;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import com.bumptech.glide.request.target.ViewTarget;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.dagger.application.ApplicationComponent;
import com.fisincorporated.exercisetracker.dagger.application.DaggerApplicationComponent;
import com.fisincorporated.exercisetracker.database.TrackerDatabaseHelper;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;


public class ActivityTrackerApplication extends DaggerApplication {

    private SQLiteDatabase database = null;
    private static ApplicationComponent appComponent;

    @Inject
    @Named("CHANNEL_ID")
    String channelId;

    @Inject
    TrackerDatabaseHelper trackerDatabaseHelper;

    @Override
    public void onCreate() {
        // Note call to super will end up calling AndroidInjector
        super.onCreate();
        opendDatabase();
        // To be able to use setTag with Glide
        ViewTarget.setTagId(R.id.glide_tag);
        createNotificationChannel();
    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        appComponent = DaggerApplicationComponent.builder().application(this).build();
        appComponent.inject(this);
        return appComponent;
    }

    public void opendDatabase() {
        database = trackerDatabaseHelper.getDatabase();
    }

    @Override
    public void onTerminate() {
        if (database != null) {
            database.close();
        }
        super.onTerminate();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getApplicationContext().getString(R.string.channel_name);
            String description = getApplicationContext().getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static ApplicationComponent getDaggerApplicationComponent(){
        return appComponent;
    }

}
