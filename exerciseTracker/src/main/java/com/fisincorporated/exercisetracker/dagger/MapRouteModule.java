package com.fisincorporated.exercisetracker.dagger;

import android.content.SharedPreferences;

import dagger.Module;
import dagger.Provides;


@Module
public class MapRouteModule {

    @Provides
    @UserScope
    SharedPreferences.Editor provideSharedPreferencesEditor(SharedPreferences preferences)
    {
        return preferences.edit();
    }

}
