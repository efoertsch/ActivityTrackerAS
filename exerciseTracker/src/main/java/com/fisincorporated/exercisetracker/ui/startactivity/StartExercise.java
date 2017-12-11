package com.fisincorporated.exercisetracker.ui.startactivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.database.ExerciseDAO;
import com.fisincorporated.exercisetracker.database.ExerciseRecord;
import com.fisincorporated.exercisetracker.database.LocationExerciseDAO;
import com.fisincorporated.exercisetracker.database.LocationExerciseRecord;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.Exercise;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.ExrcsLocation;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.LocationExercise;
import com.fisincorporated.exercisetracker.ui.logger.ActivityLogger;
import com.fisincorporated.exercisetracker.ui.logger.GPSLocationManager;
import com.fisincorporated.exercisetracker.ui.master.ExerciseMasterFragmentActivity;

public class StartExercise extends ExerciseMasterFragmentActivity {
	private static final String TAG = "StartExercise";
	private LocationExerciseDAO leDAO = null;
	private LocationExerciseRecord ler = null;
	private ExerciseDAO eDAO = null;
	private ExerciseRecord er = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		long id = -1;
		super.onCreate(savedInstanceState);
        setActivityTitle(R.string.start_exercise);
        if ((id = GPSLocationManager.checkActivityId(this)) != -1) {
            directToActivityLogger(id);
        }
	}

	protected Fragment createFragment() {
		return new StartExerciseFragment();
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

}
