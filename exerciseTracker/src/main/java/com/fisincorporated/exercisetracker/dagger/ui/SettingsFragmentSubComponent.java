package com.fisincorporated.exercisetracker.dagger.ui;


import com.fisincorporated.exercisetracker.ui.settings.SettingsFragment;

import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@Subcomponent(modules = {SettingsFragmentModule.class})
public interface SettingsFragmentSubComponent extends AndroidInjector<SettingsFragment> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<SettingsFragment> {
    }

}
