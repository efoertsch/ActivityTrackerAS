package com.fisincorporated.exercisetracker.dagger.ui;


import android.support.v4.app.Fragment;

import com.fisincorporated.exercisetracker.ui.settings.SettingsFragment;
import com.fisincorporated.exercisetracker.ui.startup.StartupPhotoFragment;

import dagger.Binds;
import dagger.Module;
import dagger.android.AndroidInjector;
import dagger.android.ContributesAndroidInjector;
import dagger.android.support.FragmentKey;
import dagger.multibindings.IntoMap;

@Module(subcomponents = {SettingsFragmentSubComponent.class, StartupPhotoFragmentSubComponent.class })
public abstract class UIBuildersModule {


    @Binds
    @IntoMap
    @FragmentKey(SettingsFragment.class)
    abstract AndroidInjector.Factory<? extends Fragment> bindSettingsFragmentInjectorFactory(SettingsFragmentSubComponent.Builder builder);

//    @ContributesAndroidInjector
//    abstract SettingsFragment bindSettingsFragment();


    @ContributesAndroidInjector
    abstract StartupPhotoFragment bindStartupPhotoFragment();


    // Add more bindings here for other sub components
    // Be sure not to provide any dependencies for the subcomponent here since this module will be included in the application component and could thereby have application scope.

}
