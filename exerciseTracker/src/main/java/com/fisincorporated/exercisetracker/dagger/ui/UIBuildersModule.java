package com.fisincorporated.exercisetracker.dagger.ui;


import com.fisincorporated.exercisetracker.broadcastreceiver.LocationReceiver;
import com.fisincorporated.exercisetracker.ui.charts.AltitudeVsDistanceGraphFragment;
import com.fisincorporated.exercisetracker.ui.charts.DistancePerExerciseFragment;
import com.fisincorporated.exercisetracker.ui.filters.ExerciseFilterDialog;
import com.fisincorporated.exercisetracker.ui.filters.LocationFilterDialog;
import com.fisincorporated.exercisetracker.ui.history.ActivityFragmentHistory;
import com.fisincorporated.exercisetracker.ui.logger.ActivityLoggerFragment;
import com.fisincorporated.exercisetracker.ui.maintenance.ExerciseMaintenanceDetailFragment;
import com.fisincorporated.exercisetracker.ui.maintenance.ExerciseMaintenanceListActivity;
import com.fisincorporated.exercisetracker.ui.maintenance.ExerciseMaintenanceListFragment;
import com.fisincorporated.exercisetracker.ui.maps.ActivityMap;
import com.fisincorporated.exercisetracker.ui.maps.ActivityMapFragment;
import com.fisincorporated.exercisetracker.ui.settings.SettingsFragment;
import com.fisincorporated.exercisetracker.ui.startup.ExerciseDrawerActivity;
import com.fisincorporated.exercisetracker.ui.startup.StartupPhotoFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module(subcomponents = { })
public abstract class UIBuildersModule {

    // TODO where possible add all activities and fragments as extending DaggerActivity and DaggerFragment


    @ContributesAndroidInjector(modules = {})
    abstract DistancePerExerciseFragment bindDistancePerExerciseFragment();

    @ContributesAndroidInjector(modules = {})
    abstract AltitudeVsDistanceGraphFragment bindAltitudeVsDistanceGraphFragment();

    @ContributesAndroidInjector(modules = {})
    abstract LocationFilterDialog bindLocationFilterDialog();

    @ContributesAndroidInjector(modules = {})
    abstract ExerciseFilterDialog bindExerciseFilterDialog();

    @ContributesAndroidInjector(modules = { })
    abstract ActivityFragmentHistory bindActivityFragmentHistory();

    @ContributesAndroidInjector(modules = { })
    abstract ActivityLoggerFragment bindActivityLoggerFragment();

    @ContributesAndroidInjector
    abstract ExerciseDrawerActivity bindExerciseDrawerActivity();

    @ContributesAndroidInjector(modules = {})
    abstract StartupPhotoFragment bindStartupPhotoFragment();

    @ContributesAndroidInjector(modules = { })
    abstract SettingsFragment bindSettingsFragment();

    @ContributesAndroidInjector(modules = { })
    abstract ExerciseMaintenanceListActivity bindExerciseMaintenanceListActivity();

    @ContributesAndroidInjector(modules = { })
    abstract ExerciseMaintenanceListFragment bindExerciseMaintenanceListFragment();

    @ContributesAndroidInjector(modules = { })
    abstract ExerciseMaintenanceDetailFragment  bindExerciseMaintenanceDetailFragment();

    @ContributesAndroidInjector(modules = { })
    abstract ActivityMap bindActivityMap();

    @ContributesAndroidInjector(modules = { })
    abstract ActivityMapFragment bindActivityMapFragment();

    @ContributesAndroidInjector(modules = {})
    abstract LocationReceiver bindLocationReceiver();



    // Add more bindings here for other sub components
    // Be sure not to provide any dependencies for the subcomponent here since this module will be included in the application component and could thereby have application scope.

}
