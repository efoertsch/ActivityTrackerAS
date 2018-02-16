package com.fisincorporated.exercisetracker.dagger;

import com.fisincorporated.exercisetracker.ui.maps.MapRoute;

import dagger.Subcomponent;



@UserScope
@Subcomponent(modules = {MapRouteModule.class})
public interface MapRouteSubComponent {

    // allow to inject into our MapRoute class
    void inject(MapRoute mapRoute);


    @Subcomponent.Builder
    interface Builder extends SubComponentBuilder<MapRouteSubComponent> {
        MapRouteSubComponent.Builder mapRouteModule(MapRouteModule mapRouteModule);
    }
}
