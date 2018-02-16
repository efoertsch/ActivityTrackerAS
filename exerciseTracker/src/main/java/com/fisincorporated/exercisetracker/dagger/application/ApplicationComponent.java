package com.fisincorporated.exercisetracker.dagger.application;

import com.fisincorporated.exercisetracker.application.ActivityTrackerApplication;
import com.fisincorporated.exercisetracker.broadcastreceiver.LocationReceiver;
import com.fisincorporated.exercisetracker.dagger.SubComponentBinder;
import com.fisincorporated.exercisetracker.dagger.SubComponentBuilder;
import com.fisincorporated.exercisetracker.dagger.ui.UIBuildersModule;
import com.fisincorporated.exercisetracker.ui.history.ActivityFragmentHistory;
import com.fisincorporated.exercisetracker.ui.logger.ActivityLoggerFragment;
import com.fisincorporated.exercisetracker.ui.settings.SettingsFragment;
import com.fisincorporated.exercisetracker.ui.startup.StartupPhotoFragment;

import java.util.Map;

import javax.inject.Provider;
import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;


@Singleton
@Component(modules = {UIBuildersModule.class, AndroidSupportInjectionModule.class, ApplicationModule.class,
        SubComponentBinder.class})
public interface ApplicationComponent extends
        AndroidInjector<ActivityTrackerApplication> {

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(ActivityTrackerApplication application);

        ApplicationComponent build();
    }

    Map<Class<?>,Provider<SubComponentBuilder>> subcomponentBuilders();

    void inject(ActivityTrackerApplication activityTrackerApplication);

    void inject(StartupPhotoFragment startupPhotoFragment);

    void inject(SettingsFragment settingsFragment);

    void inject(ActivityLoggerFragment activityLoggerFragment);

    void inject(LocationReceiver locationReceiver);

    void inject(ActivityFragmentHistory activityFragmentHistory);



}

