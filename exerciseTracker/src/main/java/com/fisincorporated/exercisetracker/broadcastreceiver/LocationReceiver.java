package com.fisincorporated.exercisetracker.broadcastreceiver;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.fisincorporated.exercisetracker.ui.logger.GPSLocationManager;

import javax.inject.Inject;

import dagger.android.DaggerBroadcastReceiver;

public class LocationReceiver extends DaggerBroadcastReceiver {

    private static final String TAG = "LocationReceiver";

    @Inject
    GPSLocationManager gpsLocationManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Location location = intent.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED);
        if (location != null) {
            onLocationReceived(context, location);
            return;
        }
        // if we get here, something else has happened
        if (intent.hasExtra(LocationManager.KEY_PROVIDER_ENABLED)) {
            boolean enabled = intent.getBooleanExtra(LocationManager.KEY_PROVIDER_ENABLED, false);
            onProviderEnabledChanged(enabled);
        }
    }

    protected void onLocationReceived(Context context, Location location) {
        Log.d(TAG, this + " Got location from " + location.getProvider() + ": " + location.getLatitude() + ", " + location.getLongitude());
        gpsLocationManager.updateLer(location);
    }

    protected void onProviderEnabledChanged(boolean enabled) {
        Log.d(TAG, "Provider " + (enabled ? "enabled" : "disabled"));
    }

}
