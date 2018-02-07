package com.fisincorporated.exercisetracker.dagger.application;



import com.fisincorporated.exercisetracker.application.ActivityTrackerApplication;
import com.fisincorporated.exercisetracker.dagger.ui.UIBuildersModule;
import com.fisincorporated.exercisetracker.ui.settings.SettingsFragment;
import com.fisincorporated.exercisetracker.ui.startup.StartupPhotoFragment;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;


@Singleton
@Component(modules = {UIBuildersModule.class, AndroidSupportInjectionModule.class, ApplicationModule.class, })
public interface ApplicationComponent extends
        AndroidInjector<ActivityTrackerApplication> {

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(ActivityTrackerApplication application);
        ApplicationComponent build();
    }

    void inject(ActivityTrackerApplication activityTrackerApplication);

    void inject(StartupPhotoFragment startupPhotoFragment);

    void inject(SettingsFragment settingsFragment);


}

