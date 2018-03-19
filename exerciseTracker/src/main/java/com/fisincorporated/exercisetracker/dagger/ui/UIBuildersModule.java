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
import com.fisincorporated.exercisetracker.ui.maintenance.ExerciseMaintenancePagerActivity;
import com.fisincorporated.exercisetracker.ui.maps.ActivityMap;
import com.fisincorporated.exercisetracker.ui.maps.ActivityMapFragment;
import com.fisincorporated.exercisetracker.ui.settings.ChangeStartupPhotoFragment;
import com.fisincorporated.exercisetracker.ui.settings.SettingsFragment;
import com.fisincorporated.exercisetracker.ui.startactivity.StartExerciseActivity;
import com.fisincorporated.exercisetracker.ui.startactivity.StartExerciseFragment;
import com.fisincorporated.exercisetracker.ui.startup.StartupPhotoFragment;
import com.fisincorporated.exercisetracker.ui.stats.ActivityDetailFragment;
import com.fisincorporated.exercisetracker.ui.stats.ActivityPagerActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module(subcomponents = {})
public abstract class UIBuildersModule {

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

    @ContributesAndroidInjector(modules = { })
    abstract SettingsFragment bindSettingsFragment();

    @ContributesAndroidInjector(modules = { })
    abstract StartExerciseActivity bindStartExerciseActivity();

    @ContributesAndroidInjector(modules = { })
    abstract StartExerciseFragment bindStartExerciseFragment();

    @ContributesAndroidInjector(modules = {})
    abstract StartupPhotoFragment bindStartupPhotoFragment();

    @ContributesAndroidInjector(modules = {})
    abstract ActivityPagerActivity bindActivityPagerActivity();

    @ContributesAndroidInjector(modules = {})
    abstract ActivityDetailFragment bindActivityDetailFragment();

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

    @ContributesAndroidInjector(modules = {})
    abstract ExerciseMaintenancePagerActivity bindExerciseMaintenancePagerActivity();

    @ContributesAndroidInjector(modules = {})
    abstract ChangeStartupPhotoFragment bindChangeStartupPhotoFragment();


    // Add more bindings here for other sub components
    // Be sure not to provide any dependencies for the subcomponent here since this module will be included in the application component and could thereby have application scope.

}
