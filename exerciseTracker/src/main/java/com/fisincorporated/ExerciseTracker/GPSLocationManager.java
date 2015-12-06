package com.fisincorporated.ExerciseTracker;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.fisincorporated.database.GPSLogDAO;
import com.fisincorporated.database.GPSLogRecord;
import com.fisincorporated.database.LocationExerciseDAO;
import com.fisincorporated.database.LocationExerciseRecord;
import com.fisincorporated.database.TrackerDatabase.Exercise;
import com.fisincorporated.database.TrackerDatabase.LocationExercise;
import com.fisincorporated.database.TrackerDatabaseHelper;

import java.sql.Timestamp;

// Adapted from Android Programming - The Big Nerd Ranch Guide. Modified to retrofit into existing system
public class GPSLocationManager {
	private static final String TAG = "GPSLocationManager";
	public static final String LER_UPDATE = "com.fisincorporated.ExerciseTracker.LER_UPDATE";
	public static final String ACTION_LOCATION = "com.fisincorporated.ExerciseTracker.ACTION_LOCATION";
	private static final String PREFS_FILE = "activity";
	private static final String PREF_CURRENT_LER_ID = "GPSLocationManager.currentLerId";
	private static final String UPDATE_RATE = "GPSLocationManager.UPDATE_RATE";
	private static final String MIN_DISTANCE_TO_LOG = "GPSLocationManager.MIN_DISTANCE_TO_LOG";
	private static final String ELEVATION_IN_DIST_CALCS = "GPSLocationManager.ELEVATION_IN_DIST_CALCS";
	private static SharedPreferences sPrefs;
	private static long sCurrentLerId;

	// private static final String TEST_PROVIDER = "TEST_PROVIDER";
	private static GPSLocationManager sGPSLocationManager;
	private Context mAppContext;
	private LocationManager mLocationManager;
	private static float sMinDistanceToLog = 20;
	private static int sUpdateRate = 60000;
	private static int sElevationInDistcalcs = 0;
	private static LocationExerciseRecord sLer = null;

	private LocationExerciseDAO mLeDAO = null;
	private GPSLogDAO mGpslrDAO = null;
	protected TrackerDatabaseHelper mHelper = null;
	protected SQLiteDatabase database = null;

	// referenced in manifest
//	private BroadcastReceiver LocationReceiver = new LocationReceiver() {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			Location location = (Location) intent
//					.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED);
//			LocationExerciseRecord ler = getLer();
//			updateLer(ler, location);
//		}
//	};

	private GPSLocationManager(Context appContext) {
		mAppContext = appContext;
		mLocationManager = (LocationManager) mAppContext
				.getSystemService(Context.LOCATION_SERVICE);
		mHelper = TrackerDatabaseHelper.getTrackerDatabaseHelper(mAppContext);
		sPrefs = mAppContext.getSharedPreferences(PREFS_FILE,
				Context.MODE_PRIVATE);
		sCurrentLerId = sPrefs.getLong(PREF_CURRENT_LER_ID, -1);
		if (sCurrentLerId != -1) {
			sUpdateRate = sPrefs.getInt(UPDATE_RATE, 60000);
			sMinDistanceToLog = sPrefs.getFloat(UPDATE_RATE, 10);
			sElevationInDistcalcs = sPrefs.getInt(ELEVATION_IN_DIST_CALCS, 0);

		}
		if (mLeDAO == null)
			mLeDAO = new LocationExerciseDAO(mHelper);
		if (mGpslrDAO == null)
			mGpslrDAO = new GPSLogDAO(mHelper);
	}

	public static GPSLocationManager get(Context c) {
		if (sGPSLocationManager == null) {
			// we use the application context to avoid leaking activities
			sGPSLocationManager = new GPSLocationManager(c.getApplicationContext());
		}
		return sGPSLocationManager;
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
			sLer = (LocationExerciseRecord) bundle
					.getParcelable(LocationExercise.LOCATION_EXERCISE_TABLE);
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
		return PendingIntent.getBroadcast(mAppContext, 0, broadcast, flags);
	}

	private void startLocationUpdates() {
		String provider = LocationManager.GPS_PROVIDER;
		// // if we have the test provider and it's enabled, use it
		// if (mLocationManager.getProvider(TEST_PROVIDER) != null
		// && mLocationManager.isProviderEnabled(TEST_PROVIDER)) {
		// provider = TEST_PROVIDER;
		// }
		Log.d(TAG, "Using provider " + provider);

		// get the last known location and broadcast it if we have one
		Location lastKnown = mLocationManager.getLastKnownLocation(provider);
		if (lastKnown != null) {
			// reset the time to now
			lastKnown.setTime(System.currentTimeMillis());
			broadcastLerUpdate(sLer, lastKnown);
		}
		// start updates from the location manager
		PendingIntent pi = getLocationPendingIntent(true);
		mLocationManager.requestLocationUpdates(provider, sUpdateRate,
				sMinDistanceToLog, pi);
		Log.d(TAG, "Starting GPS");
	}

	public boolean isTrackingLer() {
		return getLocationPendingIntent(false) != null;
	}

	// public boolean isTrackingLer(LocationExerciseRecord ler) {
	// return ler != null && ler.get_id() == sCurrentLerId;
	// }

	private void broadcastLerUpdate(LocationExerciseRecord ler, Location location) {
		Intent broadcast = new Intent(LER_UPDATE);
		broadcast.putExtra(LER_UPDATE, ler);
		broadcast.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);
		mAppContext.sendBroadcast(broadcast);
	}

	public LocationExerciseRecord startNewLer(LocationExerciseRecord ler) {
		// insert the activity into the db
		sLer = ler;
		// start tracking the run
		startTrackingLer(sLer);
		return sLer;
	}

	public void startTrackingLer(LocationExerciseRecord ler) {
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
	}

	public void stopLocationUpdates() {
		PendingIntent pi = getLocationPendingIntent(false);
		if (pi != null) {
			mLocationManager.removeUpdates(pi);
			pi.cancel();
			Log.d(TAG, "Stopping GPS");
		}

	}

	public LocationExerciseRecord getLer(long id) {
		LocationExerciseRecord ler = mLeDAO.loadLocationExerciseRecordById(id);
		return ler;
	}

	// public void insertLocation(Location loc) {
	// if (sCurrentLerId != -1) {
	// updateLer(sLer, loc);
	// } else {
	// Log.e(TAG, "Location received with no tracking id; ignoring.");
	// }
	// }

	private LocationExerciseRecord getLer() {
		return mLeDAO.loadLocationExerciseRecordById(sCurrentLerId);
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
		float results[] = { 0 };
		int distance = 0;
		float altitudeLost = 0;
		float altitudeGained = 0;
		int totalDistance = 0;
		float averageSpeed = 0;
		float maxSpeedToPoint = 0;

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
		} else {
			// 2nd or subsequent time or restarting so calc distance from where you
			// stopped to where you are
			Location.distanceBetween(location.getLatitude(),
					location.getLongitude(), ler.getEndLatitude(),
					ler.getEndLongitude(), results);
			// see if elevation difference should be taken into account for
			// calc'ing distance from last point
			if (sElevationInDistcalcs == 0) {
				distance = (int) Math.round(results[0]);
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
			if (location.getAltitude() > ler.getEndAltitude()) {
				altitudeGained += location.getAltitude() - ler.getEndAltitude();
			} else {
				altitudeLost += ler.getEndAltitude() - location.getAltitude();
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
			ler.setEndAltitude((float) (location.getAltitude()));

		}
		mLeDAO.updateLocationExercise(ler);
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
		mGpslrDAO.createGPSLogRecord(gpslr);

	}

}
