package com.fisincorporated.exercisetracker.dagger.ui;


import com.fisincorporated.exercisetracker.dagger.application.ApplicationModule;
import com.fisincorporated.exercisetracker.ui.maps.MapRoute;

import dagger.Subcomponent;

@Subcomponent(modules = {MapRouteModule.class, ApplicationModule.class})
public interface MapRouteComponent {

    // allow to inject into our MapRoute class
    void inject(MapRoute mapRoute);

}
