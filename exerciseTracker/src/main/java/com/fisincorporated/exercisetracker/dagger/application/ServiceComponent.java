package com.fisincorporated.exercisetracker.dagger.application;

import com.fisincorporated.exercisetracker.backupandrestore.BackupJobService;

import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;


@Component(modules = {ChannelIdModule.class
        , AndroidSupportInjectionModule.class
        })
public interface ServiceComponent extends
        AndroidInjector<BackupJobService> {

    @Component.Builder
    interface Builder {
        ServiceComponent build();
    }

    void inject(BackupJobService backupJobService);

}
