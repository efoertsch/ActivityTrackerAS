package com.fisincorporated.exercisetracker.dagger.ui;


import com.fisincorporated.exercisetracker.ui.history.ActivityFragmentHistory;
import com.fisincorporated.exercisetracker.ui.history.ActivityHistory;
import com.fisincorporated.exercisetracker.ui.maps.ActivityMap;
import com.fisincorporated.exercisetracker.ui.maps.ActivityMapFragment;
import com.fisincorporated.exercisetracker.ui.settings.SettingsFragment;
import com.fisincorporated.exercisetracker.ui.startup.ExerciseDrawerActivity;
import com.fisincorporated.exercisetracker.ui.startup.StartupPhotoFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module(subcomponents = { })
public abstract class UIBuildersModule {

    // TODO add all activities and fragments as extending DaggerActivity and DaggerFragment
    @ContributesAndroidInjector
    abstract ExerciseDrawerActivity bindExerciseDrawerActivity();

    @ContributesAndroidInjector(modules = {})
    abstract StartupPhotoFragment bindStartupPhotoFragment();

    @ContributesAndroidInjector(modules = { })
    abstract SettingsFragment bindSettingsFragment();

    @ContributesAndroidInjector
    abstract ActivityHistory bindActivityHistory();

    @ContributesAndroidInjector(modules = { })
    abstract ActivityFragmentHistory bindActivityFragmentHistory();

    @ContributesAndroidInjector(modules = { })
    abstract ActivityMap bindActivityMap();

    @ContributesAndroidInjector(modules = { })
    abstract ActivityMapFragment bindActivityMapFragment();

    // Add more bindings here for other sub components
    // Be sure not to provide any dependencies for the subcomponent here since this module will be included in the application component and could thereby have application scope.

}
