package com.fisincorporated.ExerciseTracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.fisincorporated.database.ExerciseDAO;
import com.fisincorporated.database.ExerciseRecord;
import com.fisincorporated.database.LocationExerciseDAO;
import com.fisincorporated.database.LocationExerciseRecord;
import com.fisincorporated.database.TrackerDatabase.Exercise;
import com.fisincorporated.database.TrackerDatabase.ExrcsLocation;
import com.fisincorporated.database.TrackerDatabase.LocationExercise;

public class StartExercise extends ExerciseMasterActivity {
	private static final String TAG = "StartExercise";
	private LocationExerciseDAO leDAO = null;
	private LocationExerciseRecord ler = null;
	private ExerciseDAO eDAO = null;
	private ExerciseRecord er = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		long id = -1;
		super.onCreate(savedInstanceState);
		// added for master/detail fragments as on tablet
		// setContentView(R.layout.activity_fragment);
		if ((id = GPSLocationManager.checkActivityId(this)) != -1) {
			directToActivityLogger(id);
		} else {
			setContentView(getLayoutResId());
			FragmentManager fm = getSupportFragmentManager();
			Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
			if (fragment == null) {
				fragment = createFragment();
				fm.beginTransaction().add(R.id.fragmentContainer, fragment)
						.commit();
			}
		}
	}

	protected Fragment createFragment() {
		return new StartExerciseFragment();
	}

	// added for tablet
	protected int getLayoutResId() {
		return R.layout.activity_masterdetail;
	}

	private void directToActivityLogger(long id) {
		getDatabaseSetup();
		Intent intent = new Intent(this, ActivityLogger.class);
		leDAO = new LocationExerciseDAO(databaseHelper);
		eDAO = new ExerciseDAO(databaseHelper);
		ler = leDAO.loadLocationExerciseRecordById(id);
		intent.putExtra(LocationExercise.LOCATION_EXERCISE_TABLE, ler);
		intent.putExtra(Exercise.EXERCISE, leDAO.getExercise(ler.getExerciseId()));
		intent.putExtra(ExrcsLocation.LOCATION,
				leDAO.getLocation(ler.getLocationId()));
		intent.putExtra(LocationExercise.DESCRIPTION, ler.getDescription() == null ? "" : ler.getDescription());
		er = eDAO.loadExerciseRecordById(ler.getExerciseId());
		intent.putExtra(Exercise.MIN_DISTANCE_TO_LOG, er.getMinDistanceToLog());
		intent.putExtra(Exercise.ELEVATION_IN_DIST_CALCS,
				er.getElevationInDistCalcs());
		startActivity(intent);
		this.finish();
	}
	
	// Fix for at least 4.4.2 (may occur prior to at anything above 2.3.4)
	public void onBackPressed(){
		Log.i(TAG, "onBackPressed");
		FragmentManager fm = getSupportFragmentManager();
		// this must be true or big trouble
		StartExerciseFragment fragment = (StartExerciseFragment)fm.findFragmentById(R.id.fragmentContainer);
		fragment.removeListeners();
		super.onBackPressed();
		Log.i(TAG, "completed onBackPressed");
		
		
	}

}
