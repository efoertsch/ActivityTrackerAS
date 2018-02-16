package com.fisincorporated.exercisetracker.dagger;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;


//https://github.com/Zorail/SubComponent/blob/master/app/src/main/java/zorail/rohan/com/subcomponent/Binder/ApplicationBinder.java
@Module(subcomponents = {MapRouteSubComponent.class})
public abstract class SubComponentBinder {
    @Binds
    @IntoMap
    @SubComponentKey(MapRouteSubComponent.Builder.class)
    public abstract SubComponentBuilder myBuilder(MapRouteSubComponent.Builder impl);


}