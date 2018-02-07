package com.fisincorporated.exercisetracker.dagger.ui;


import com.fisincorporated.exercisetracker.ui.startup.StartupPhotoFragment;

import dagger.Subcomponent;
import dagger.android.AndroidInjector;


@Subcomponent(modules = {StartupPhotoFragmentModule.class})
public interface StartupPhotoFragmentSubComponent extends AndroidInjector<StartupPhotoFragment> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<StartupPhotoFragment> {
    }

}

