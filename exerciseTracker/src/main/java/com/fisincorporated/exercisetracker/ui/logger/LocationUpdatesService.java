package com.fisincorporated.exercisetracker.ui.logger;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.application.AppPreferences;
import com.fisincorporated.exercisetracker.database.ExerciseDAO;
import com.fisincorporated.exercisetracker.database.ExerciseRecord;
import com.fisincorporated.exercisetracker.database.GPSLogDAO;
import com.fisincorporated.exercisetracker.database.GPSLogRecord;
import com.fisincorporated.exercisetracker.database.LocationExerciseDAO;
import com.fisincorporated.exercisetracker.database.LocationExerciseRecord;
import com.fisincorporated.exercisetracker.database.TrackerDatabase;
import com.fisincorporated.exercisetracker.database.TrackerDatabaseHelper;
import com.fisincorporated.exercisetracker.utility.StatsUtil;
import com.fisincorporated.exercisetracker.utility.TimeZoneUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.sql.Timestamp;
import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.android.DaggerService;

// Copied from https://github.com/googlesamples/android-play-location/LocationUpdatesForegroundService/
// to handle  Android O location services changes and modified as needed
public class LocationUpdatesService extends DaggerService {

    private static final String PACKAGE_NAME = "com.fisincorporated.exercisetracker.ui.logger";
    private static final String TAG = LocationUpdatesService.class.getSimpleName();

    /**
     * The name of the channel for notifications.
     */
    static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";
    static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME +
            ".started_from_notification";
    private final IBinder mBinder = new LocalBinder();

    /**
     * The identifier for the notification displayed for the foreground service.
     */
    private static final int NOTIFICATION_ID = 12345678;

    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */
    private boolean changingConfiguration = false;
    private NotificationManager notificationManager;

    /**
     * Contains parameters used by {@link com.google.android.gms.location.FusedLocationProviderApi}.
     */
    private LocationRequest locationRequest;

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient fusedLocationClient;

    /**
     * Callback for changes in location.
     */
    private LocationCallback locationCallback;

    private Handler serviceHandler;

    /**
     * The current location.
     */
    private Location location;

    // ActivityTracker custom code
    @Inject
    StatsUtil statsUtil;

    @Inject
    TrackerDatabaseHelper trackerDatabaseHelper;

    @Inject
    TimeZoneUtils timeZoneUtils;

    @Inject
    AppPreferences appPreferences;

    @Inject
    @Named("CHANNEL_ID")
    String channelId;

    private String exrcsLocation;
    private String exercise;
    private String description;
    private ArrayList<String[]> stats = new ArrayList<>();

    private LocationExerciseRecord ler;

    private LocationExerciseDAO sLeDAO = null;
    private GPSLogDAO sGpslrDAO = null;
    private ExerciseDAO sEDao = null;
    private ExerciseRecord sEr = null;

    private float minDistanceToLog = 20;
    private int updateRate = 60000;
    private int elevationInDistcalcs = 0;
    private long lastNotificationTime = 0;
    private long notificationInterval = 60 * 1000;
    private boolean running = false;
    private Bundle bundle;

    public LocationUpdatesService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        serviceHandler = new Handler(handlerThread.getLooper());
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            notificationManager.createNotificationChannel(mChannel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service started");
        getDAOs();
        lookForArguments(intent);
        getLastLocation();

        boolean startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION,
                false);

        // We got here because the user decided to remove location updates from the notification.
        if (startedFromNotification) {
            removeLocationUpdates();
            stopSelf();
        }
        // Tells the system to not try to recreate the service after it has been killed.
        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        changingConfiguration = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) comes to the foreground
        // and binds with this service. The service should cease to be a foreground service
        // when that happens.
        Log.i(TAG, "in onBind()");
        lookForArguments(intent);
        stopForeground(true);
        changingConfiguration = false;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        // Called when a client (Activity/Fragment) returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        Log.i(TAG, "in onRebind()");
        lookForArguments(intent);
        stopForeground(true);
        changingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Last client unbound from service");

        // Called when the last client (Activity/Fragment) unbinds from this
        // service. If this method is called due to a configuration change, we
        // do nothing. Otherwise, we make this service a foreground service.
        if (!changingConfiguration) {
            Log.i(TAG, "Starting foreground service");
            Notification notification = getNotification();
            // If targeting O, use the following code.
//            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
//                notificationManager.startServiceInForeground(new Intent(this,
//                        LocationUpdatesService.class), NOTIFICATION_ID, getNotification());
//            } else {
//                startForeground(NOTIFICATION_ID, getNotification());
//            }

            if (notification != null) {
                startForeground(NOTIFICATION_ID, notification);
            }
        }
        return true; // Ensures onRebind() is called when a client re-binds.
    }

    @Override
    public void onDestroy() {
        serviceHandler.removeCallbacksAndMessages(null);
    }

    //TODO replace bundle with ?

    /**
     * Makes a request for location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    public void requestLocationUpdates() {
        Log.i(TAG, "Requesting location updates");
        running = true;
        Intent intent = new Intent(getApplicationContext(), LocationUpdatesService.class);
        intent.putExtra(GlobalValues.BUNDLE, bundle);
        startService(intent);
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback, Looper.myLooper());
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
        }
    }

    /**
     * Removes location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    public void removeLocationUpdates() {
        Log.i(TAG, "Removing location updates");
        running = false;
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
        stopTrackingLer();
    }

    private void getLastLocation() {
        try {
            fusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                location = task.getResult();
                                onNewLocation(location);
                            } else {
                                Log.w(TAG, "Failed to get location.");
                            }
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
        }
    }

    private void onNewLocation(Location location) {
        // Log.i(TAG, "New location: " + location);

        this.location = location;
        updateLer(location);

        // Notify anyone listening for broadcasts about the new location.
//        Intent intent = new Intent(ACTION_BROADCAST);
//        intent.putExtra(EXTRA_LOCATION, location);
//        intent.putExtra(TrackerDatabase.LocationExercise.LOCATION_EXERCISE_TABLE, ler);
//        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

        // Update notification content if running as a foreground service.
        if (serviceIsRunningInForeground(this)) {
            Notification notification = getNotification();
            if (notification != null) {
                notificationManager.notify(NOTIFICATION_ID, notification);
            }
        }
    }

    public boolean isRunning() {
        return running;
    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        LocationUpdatesService getService() {
            return LocationUpdatesService.this;
        }
    }

    /**
     * Returns true if this is a foreground service.
     *
     * @param context The {@link Context}.
     */
    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }

    // TODO only send in LER and then get all values based on that
    private void lookForArguments(Intent intent) {
        bundle = intent.getBundleExtra(GlobalValues.BUNDLE);
        if (bundle != null) {
            ler = bundle.getParcelable(TrackerDatabase.LocationExercise.LOCATION_EXERCISE_TABLE);
            updateRate = ler.getLogInterval() * 1000;
            exercise = bundle.getString(TrackerDatabase.Exercise.EXERCISE);
            minDistanceToLog = bundle.getFloat(TrackerDatabase.Exercise.MIN_DISTANCE_TO_LOG, 10);
            elevationInDistcalcs = bundle.getInt(TrackerDatabase.Exercise.ELEVATION_IN_DIST_CALCS, 0);
            exrcsLocation = bundle.getString(TrackerDatabase.ExrcsLocation.LOCATION);
            description = bundle.getString(TrackerDatabase.LocationExercise.DESCRIPTION);
            if (description == null) {
                description = "";
            }
            startTrackingLer(ler);
            createLocationRequest();
        } else {
            stopSelf();
        }
    }

    /**
     * Sets the location request parameters.
     */
    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(updateRate);
        locationRequest.setFastestInterval(updateRate);
        locationRequest.setSmallestDisplacement(minDistanceToLog);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    // TODO move all this to a repository
    public void getDAOs() {
        if (sLeDAO == null)
            sLeDAO = trackerDatabaseHelper.getLocationExerciseDAO();
        if (sGpslrDAO == null)
            sGpslrDAO = trackerDatabaseHelper.getGPSLogDAO();
        if (sEDao == null) {
            sEDao = trackerDatabaseHelper.getExerciseDAO();
        }
    }

    void startTrackingLer(LocationExerciseRecord ler) {
        appPreferences.setActivityId(ler.get_id());
    }

    public void stopTrackingLer() {
        appPreferences.deleteActivityId();
        cancelNotifications();
        stopSelf();
    }

    //If stopped tracker (and service stopped) then continued get up to date record.
    private LocationExerciseRecord getLer() {
        return ler = sLeDAO.loadLocationExerciseRecordById(ler.get_id());
    }

    public void updateLer(Location location) {
        getLer();
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
        double altitude = 0;

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
            ler.setCurrentAltitude((float) altitude);
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
            if (elevationInDistcalcs == 0) {
                distance = Math.round(results[0]);
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
            ler.setEndAltitude((float) altitude);

            ler.setCurrentAltitude((float) altitude);
            if (altitude < ler.getMinAltitude()) {
                ler.setMinAltitude((float) altitude);
            }
            if (altitude > ler.getMaxAltitude()) {
                ler.setMaxAltitude((float) altitude);
            }
        }
        sLeDAO.updateLocationExercise(ler);
        // distance may be 0 if first time in or >0 if restarting
        if (ler.getLogDetail() == 1) {
            insertGPSLogRecord(ler.get_id(), location, distance);
        }

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
    private Notification getNotification() {
        if (ler == null || ler.getDistance() == null) {
            return null;
        }

        // Only update notification every so often
        if (System.currentTimeMillis() - lastNotificationTime < notificationInterval) {
            return null;
        }
        lastNotificationTime = System.currentTimeMillis();
        statsUtil.formatActivityStats(stats, ler, true, true);
        String notificationText = exercise + "@" + exrcsLocation + '\n';

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_device_gps_fixed)
                        .setContentTitle(getResources().getString(R.string.app_name))
                        .setContentText(notificationText);
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle("Activity Statistics");

        for (int i = 0; i < stats.size(); i++) {
            inboxStyle.addLine(stats.get(i)[0] + ": " + stats.get(i)[1]);
        }
        builder.setStyle(inboxStyle);

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(channelId); // Channel ID
        }

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = createNotificationIntent();

        // The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ActivityLoggerActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        builder.setContentIntent(resultPendingIntent);
        return builder.build();
//        sNotificationManager =
//                (NotificationManager) sAppContext.getSystemService(Context.NOTIFICATION_SERVICE);
//// GlobalValues.NOTIFICATION_LOGGER allows you to update the notification later on.
//        sNotificationManager.notify(GlobalValues.NOTIFICATION_LOGGER, builder.build());

    }

    private Intent createNotificationIntent() {
        Intent intent = new Intent(getApplicationContext(), ActivityLoggerActivity.class);
        intent.putExtra(TrackerDatabase.LocationExercise.LOCATION_EXERCISE_TABLE, ler);
        intent.putExtra(TrackerDatabase.Exercise.EXERCISE, sLeDAO.getExercise(ler.getExerciseId()));
        intent.putExtra(TrackerDatabase.ExrcsLocation.LOCATION,
                sLeDAO.getLocation(ler.getLocationId()));
        intent.putExtra(TrackerDatabase.LocationExercise.DESCRIPTION,
                ler.getDescription() == null ? "" : ler.getDescription());
        sEr = sEDao.loadExerciseRecordById(ler.getExerciseId());
        intent.putExtra(TrackerDatabase.Exercise.MIN_DISTANCE_TO_LOG, sEr.getMinDistanceToLog());
        intent.putExtra(TrackerDatabase.Exercise.ELEVATION_IN_DIST_CALCS,
                sEr.getElevationInDistCalcs());
        return intent;
    }

    private void cancelNotifications() {
        if (notificationManager != null) {
            notificationManager.cancel(GlobalValues.NOTIFICATION_LOGGER);
        }
    }

}