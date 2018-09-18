package com.fisincorporated.exercisetracker.ui.logger;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.database.ExerciseDAO;
import com.fisincorporated.exercisetracker.database.ExerciseRecord;
import com.fisincorporated.exercisetracker.database.GPSLogDAO;
import com.fisincorporated.exercisetracker.database.GPSLogRecord;
import com.fisincorporated.exercisetracker.database.LocationExerciseDAO;
import com.fisincorporated.exercisetracker.database.LocationExerciseRecord;
import com.fisincorporated.exercisetracker.database.TrackerDatabase;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.Exercise;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.LocationExercise;
import com.fisincorporated.exercisetracker.database.TrackerDatabaseHelper;
import com.fisincorporated.exercisetracker.utility.StatsUtil;
import com.fisincorporated.exercisetracker.utility.TimeZoneUtils;
import com.jakewharton.rxrelay2.PublishRelay;

import java.sql.Timestamp;
import java.util.ArrayList;

// Adapted from Android Programming - The Big Nerd Ranch Guide. Modified to retrofit into existing system
// Updates come in via LocationReciever

// TODO - refactor sqlite logic
public class GPSLocationManager {
    private static final String TAG = "GPSLocationManager";
    // ACTION_LOCATION string must match to manifest receiver
    private static final String ACTION_LOCATION = "com.fisincorporated.ExerciseTracker.ACTION_LOCATION";
    private static final String PREFS_FILE = "activity";
    private static final String PREF_CURRENT_LER_ID = "GPSLocationManager.currentLerId";
    private static final String UPDATE_RATE = "GPSLocationManager.UPDATE_RATE";
    private static final String MIN_DISTANCE_TO_LOG = "GPSLocationManager.MIN_DISTANCE_TO_LOG";
    private static final String ELEVATION_IN_DIST_CALCS = "GPSLocationManager.ELEVATION_IN_DIST_CALCS";
    private static SharedPreferences sPrefs;
    private static long sCurrentLerId;

    // private static final String TEST_PROVIDER = "TEST_PROVIDER";

    private static Context sAppContext;
    private static LocationManager sLocationManager;
    private static float sMinDistanceToLog = 20;
    private static int sUpdateRate = 60000;
    private static int sElevationInDistcalcs = 0;
    private static LocationExerciseRecord sLer = null;
    private static long sLastNotificationTime = 0;
    private static long sNotificationInterval = 60 * 1000;

    /**
     * Used for notification
     */
    private static FragmentActivity sActivity;

    private static LocationExerciseDAO sLeDAO = null;
    private static GPSLogDAO sGpslrDAO = null;
    private static ExerciseDAO sEDao = null;
    private static ExerciseRecord sEr = null;
    //protected static SQLiteDatabase sDatabase = null;
    private static String sExercise;
    private static String sExrcsLocation;
    private static ArrayList<String[]> sStats = new ArrayList<String[]>();
    private static NotificationManager sNotificationManager;

    private StatsUtil statsUtil;
    private PublishRelay<Object> publishRelay;
    private TimeZoneUtils timeZoneUtils;

    public GPSLocationManager(Context appContext, StatsUtil statsUtil
            , PublishRelay<Object> publishRelay, TrackerDatabaseHelper trackerDatabaseHelper
            , TimeZoneUtils timeZoneUtils) {
        sAppContext = appContext;
        this.statsUtil = statsUtil;
        this.publishRelay = publishRelay;
        this.timeZoneUtils = timeZoneUtils;
        sLocationManager = (LocationManager) sAppContext
                .getSystemService(Context.LOCATION_SERVICE);
        sPrefs = sAppContext.getSharedPreferences(PREFS_FILE,
                Context.MODE_PRIVATE);
        sCurrentLerId = sPrefs.getLong(PREF_CURRENT_LER_ID, -1);
        if (sCurrentLerId != -1) {
            sUpdateRate = sPrefs.getInt(UPDATE_RATE, 60000);
            sMinDistanceToLog = sPrefs.getFloat(UPDATE_RATE, 10);
            sElevationInDistcalcs = sPrefs.getInt(ELEVATION_IN_DIST_CALCS, 0);

        }
        if (sLeDAO == null)
            sLeDAO = trackerDatabaseHelper.getLocationExerciseDAO();
        if (sGpslrDAO == null)
            sGpslrDAO = trackerDatabaseHelper.getGPSLogDAO();
        if (sEDao == null) {
            sEDao = trackerDatabaseHelper.getExerciseDAO();
        }
    }

    public static long checkActivityId(Context appContext) {
        sPrefs = appContext
                .getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        return sPrefs.getLong(PREF_CURRENT_LER_ID, -1);
    }

    // This should only called by class responsible to start/continue logging an
    // activity
    public static void setActivityDetails(Bundle bundle) {
        if (bundle != null) {
            sLer = bundle.getParcelable(LocationExercise.LOCATION_EXERCISE_TABLE);
            if (sLer.get_id() != sCurrentLerId) {
                sCurrentLerId = sLer.get_id();
                sPrefs.edit().putLong(PREF_CURRENT_LER_ID, sCurrentLerId).apply();
            }

            sUpdateRate = sLer.getLogInterval() * 1000;
            sMinDistanceToLog = bundle.getFloat(Exercise.MIN_DISTANCE_TO_LOG, 10);
            sElevationInDistcalcs = bundle.getInt(
                    Exercise.ELEVATION_IN_DIST_CALCS, 0);

            sPrefs.edit().putInt(UPDATE_RATE, sUpdateRate);
            sPrefs.edit().putFloat(MIN_DISTANCE_TO_LOG, sMinDistanceToLog);
            sPrefs.edit().putInt(ELEVATION_IN_DIST_CALCS, sElevationInDistcalcs);
        }
    }

    private PendingIntent getLocationPendingIntent(boolean shouldCreate) {
        Intent broadcast = new Intent(ACTION_LOCATION);
        int flags = shouldCreate ? 0 : PendingIntent.FLAG_NO_CREATE;
        return PendingIntent.getBroadcast(sAppContext, 0, broadcast, flags);
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        String provider = LocationManager.GPS_PROVIDER;
        // // if we have the test provider and it's enabled, use it
        // if (sLocationManager.getProvider(TEST_PROVIDER) != null
        // && sLocationManager.isProviderEnabled(TEST_PROVIDER)) {
        // provider = TEST_PROVIDER;
        // }
        Log.d(TAG, "Using provider " + provider);

        if (!isLocationPermissionGranted()) {
            publishRelay.accept(new NeedLocationPermission());
            return;
        }
        Location lastKnown = sLocationManager.getLastKnownLocation(provider);
        if (lastKnown != null) {
            // reset the time to now
            lastKnown.setTime(System.currentTimeMillis());
            broadcastLerUpdate(sLer, lastKnown);
        }
        // start updates from the location manager
        PendingIntent pi = getLocationPendingIntent(true);
        sLocationManager.requestLocationUpdates(provider, sUpdateRate, sMinDistanceToLog, pi);
        Log.d(TAG, "Starting GPS");
    }

    //TODO Convert to using EasyPermissions
    private boolean isLocationPermissionGranted() {
        return ActivityCompat.checkSelfPermission(sAppContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isTrackingLer() {
        return getLocationPendingIntent(false) != null;
    }

    private void broadcastLerUpdate(LocationExerciseRecord ler, Location location) {
        publishRelay.accept(ler);
        // Update notification
        generateNotification();
    }

    void startNewLer(LocationExerciseRecord ler) {
        // insert the activity into the db
        sLer = ler;
        // start tracking the run
        startTrackingLer(sLer);
    }

    void startTrackingLer(LocationExerciseRecord ler) {
        // keep the ID
        sCurrentLerId = ler.get_id();
        // store it in shared preferences
        sPrefs.edit().putLong(PREF_CURRENT_LER_ID, sCurrentLerId).apply();
        // start location updates
        startLocationUpdates();
    }

    public void stopTrackingLer() {
        stopLocationUpdates();
        sCurrentLerId = -1;
        sPrefs.edit().remove(PREF_CURRENT_LER_ID).apply();
        cancelNotifications();

    }

    public void stopLocationUpdates() {
        PendingIntent pi = getLocationPendingIntent(false);
        if (pi != null) {
            sLocationManager.removeUpdates(pi);
            pi.cancel();
            Log.d(TAG, "Stopping GPS");
        }

    }

    private LocationExerciseRecord getLer() {
        return sLer = sLeDAO.loadLocationExerciseRecordById(sCurrentLerId);
    }

    public void updateLer(Location location) {
        LocationExerciseRecord ler = getLer();
        // on the off chance the trace was canceled prior to this update occurring
        // sCurrentLerId will be -1 resulting in the ler object having id -1 so
        // just return.
        if (ler.get_id() == -1)
            return;
        updateLer(ler, location);
    }

    protected void updateLer(LocationExerciseRecord ler, Location location) {
        float results[] = {0};
        int distance = 0;
        float altitudeLost = 0;
        float altitudeGained = 0;
        int totalDistance = 0;
        float averageSpeed = 0;
        float maxSpeedToPoint = 0;
        double altitude  = 0;

        // if startTime null then first time in.
        if (ler.getStartTimestamp() == null) {
            ler.setStartTimestamp(new Timestamp(location.getTime()));
            ler.setEndTimestamp(new Timestamp(location.getTime()));
            ler.setStartLatitude((float) location.getLatitude());
            ler.setEndLatitude((float) location.getLatitude());
            ler.setStartLongitude((float) (location.getLongitude()));
            ler.setEndLongitude((float) (location.getLongitude()));
            ler.setStartAltitude((float) (location.getAltitude()));
            ler.setEndAltitude((float) (location.getAltitude()));
            ler.setDistance(0);
            ler.setAltitudeGained(0f);
            ler.setAltitudeLost(0f);
            ler.setAverageSpeed(0f);
            ler.setMaxSpeedToPoint(0f);

            altitude = location.getAltitude();
            ler.setCurrentAltitude((float) altitude );
            ler.setMinAltitude((float) altitude);
            ler.setMaxAltitude((float) altitude);
            ler.setTimezone(timeZoneUtils.getDeviceTimeZone());
            ler.setGmtHourOffset(timeZoneUtils.getGmtHourOffset());
            ler.setGmtMinuteOffset(timeZoneUtils.getGmtMinuteOffest());


        } else {
            // 2nd or subsequent time or restarting so calc distance from where you
            // stopped to where you are
            Location.distanceBetween(location.getLatitude(),
                    location.getLongitude(), ler.getEndLatitude(),
                    ler.getEndLongitude(), results);
            // see if elevation difference should be taken into account for
            // calc'ing distance from last point
            if (sElevationInDistcalcs == 0) {
                distance =  Math.round(results[0]);
            } else {
                distance = (int) Math
                        .round(Math.hypot(
                                Math.abs((int) location.getAltitude()
                                        - ler.getEndAltitude()), (double) results[0]));
            }
            totalDistance = ler.getDistance() + distance;
            ler.setDistance(totalDistance);
            altitudeGained = ler.getAltitudeGained();
            altitudeLost = ler.getAltitudeLost();

            altitude = location.getAltitude();

            if (altitude > ler.getEndAltitude()) {
                altitudeGained += altitude - ler.getEndAltitude();
            } else {
                altitudeLost += ler.getEndAltitude() - altitude;
            }
            ler.setAltitudeGained(altitudeGained);
            ler.setAltitudeLost(altitudeLost);
            // averageSpeed in kph
            averageSpeed = (totalDistance / 1000f)
                    / ((location.getTime() - ler.getStartTimestamp().getTime()) / GlobalValues.TIME_TO_FRACTION_HOURS);
            ler.setAverageSpeed(averageSpeed);
            // max speed in kph
            maxSpeedToPoint = (distance / 1000f)
                    / ((location.getTime() - ler.getEndTimestamp().getTime()) / GlobalValues.TIME_TO_FRACTION_HOURS);
            if ((ler.getMaxSpeedToPoint() != null)
                    && maxSpeedToPoint > ler.getMaxSpeedToPoint()) {
                ler.setMaxSpeedToPoint(maxSpeedToPoint);
            }
            // these are set whether first time through or on restart
            ler.setEndTimestamp(new Timestamp(location.getTime()));
            ler.setEndLatitude((float) location.getLatitude());
            ler.setEndLongitude((float) (location.getLongitude()));
            ler.setEndAltitude((float)altitude);

            ler.setCurrentAltitude( (float) altitude);
            if (altitude < ler.getMinAltitude()) {
                ler.setMinAltitude( (float)altitude);
            }
            if (altitude > ler.getMaxAltitude()) {
                ler.setMaxAltitude( (float) altitude);
            }
        }
        sLeDAO.updateLocationExercise(ler);
        // distance may be 0 if first time in or >0 if restarting
        if (ler.getLogDetail() == 1) {
            insertGPSLogRecord(ler.get_id(), location, distance);
        }
        broadcastLerUpdate(ler, location);

    }

    private void insertGPSLogRecord(long lerId, Location location,
                                    int distanceFromLastPoint) {
        GPSLogRecord gpslr = new GPSLogRecord();
        gpslr.setLocationExerciseId(lerId);
        gpslr.setElevation((int) location.getAltitude());
        gpslr.setLatitude((float) location.getLatitude());
        gpslr.setLongitude((float) location.getLongitude());
        gpslr.setDistanceFromLastPoint(distanceFromLastPoint);
        gpslr.setTimestamp(new java.sql.Timestamp(location.getTime()).toString());
        sGpslrDAO.createGPSLogRecord(gpslr);

    }


    /**
     * Generate notification and update as you go
     */
    private void generateNotification() {
        if (sLer == null || sLer.getDistance() == null) return;
        // Only update notification every so often
        if (System.currentTimeMillis() - sLastNotificationTime < sNotificationInterval) {
            return;
        }
        sLastNotificationTime = System.currentTimeMillis();
        statsUtil.formatActivityStats(sStats, sLer, true);
        String notificationText = sExercise + "@" + sExrcsLocation + '\n';

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(sActivity)
                        .setSmallIcon(R.drawable.ic_stat_device_gps_fixed)
                        .setContentTitle(sActivity.getResources().getString(R.string.app_name))
                        .setContentText(notificationText);
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle("Activity Statistics");

        for (int i = 0; i < sStats.size(); i++) {
            inboxStyle.addLine(sStats.get(i)[0] + ": " + sStats.get(i)[1]);
        }
        builder.setStyle(inboxStyle);
// Creates an explicit intent for an Activity in your app
        Intent resultIntent = createNotificationIntent();

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(sActivity);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(sActivity.getClass());
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        builder.setContentIntent(resultPendingIntent);
        sNotificationManager =
                (NotificationManager) sActivity.getSystemService(Context.NOTIFICATION_SERVICE);
// GlobalValues.NOTIFICATION_LOGGER allows you to update the notification later on.
        sNotificationManager.notify(GlobalValues.NOTIFICATION_LOGGER, builder.build());

    }

    private Intent createNotificationIntent() {
        Intent intent = new Intent(sActivity, sActivity.getClass());
        intent.putExtra(LocationExercise.LOCATION_EXERCISE_TABLE, sLer);
        intent.putExtra(Exercise.EXERCISE, sLeDAO.getExercise(sLer.getExerciseId()));
        intent.putExtra(TrackerDatabase.ExrcsLocation.LOCATION,
                sLeDAO.getLocation(sLer.getLocationId()));
        intent.putExtra(LocationExercise.DESCRIPTION,
                sLer.getDescription() == null ? "" : sLer.getDescription());
        sEr = sEDao.loadExerciseRecordById(sLer.getExerciseId());
        intent.putExtra(Exercise.MIN_DISTANCE_TO_LOG, sEr.getMinDistanceToLog());
        intent.putExtra(Exercise.ELEVATION_IN_DIST_CALCS,
                sEr.getElevationInDistCalcs());
        return intent;
    }

    private void cancelNotifications() {
        if (sNotificationManager != null) {
            sNotificationManager.cancel(GlobalValues.NOTIFICATION_LOGGER);
        }
    }

    public void setNotification(FragmentActivity activity, String exercise, String exrcsLocation) {
        sActivity = activity;
        sExercise = exercise;
        sExrcsLocation = exrcsLocation;
    }

    public long getCurrentLer() {
        if (sLer != null) {
            return sLer.get_id();
        } else return -1l;
    }

}
