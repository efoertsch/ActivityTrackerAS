package com.fisincorporated.exercisetracker.ui.startactivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.application.AppPreferences;
import com.fisincorporated.exercisetracker.database.ExerciseDAO;
import com.fisincorporated.exercisetracker.database.ExerciseRecord;
import com.fisincorporated.exercisetracker.database.LocationExerciseDAO;
import com.fisincorporated.exercisetracker.database.LocationExerciseRecord;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.Exercise;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.ExrcsLocation;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.LocationExercise;
import com.fisincorporated.exercisetracker.database.TrackerDatabaseHelper;
import com.fisincorporated.exercisetracker.ui.logger.ActivityLoggerActivity;
import com.fisincorporated.exercisetracker.ui.master.ExerciseDaggerActivity;

import javax.inject.Inject;

public class StartExerciseActivity extends ExerciseDaggerActivity {
	private static final String TAG = StartExerciseActivity.class.getName();
	private LocationExerciseDAO leDAO = null;
	private LocationExerciseRecord ler = null;
	private ExerciseDAO eDAO = null;
	private ExerciseRecord er = null;

	@Inject
	TrackerDatabaseHelper trackerDatabaseHelper;

	@Inject
	AppPreferences appPreferences;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		long id = -1;
		super.onCreate(savedInstanceState);
        setActivityTitle(R.string.start_exercise);
        if ((id = appPreferences.getActivityId()) != -1) {
            directToActivityLogger(id);
        }
	}

	protected Fragment createFragment() {
		return new StartExerciseFragment();
	}

	private void directToActivityLogger(long id) {
		// TODO Create ActivityLoggerActivity.Builder to pass parms
		Intent intent = new Intent(this, ActivityLoggerActivity.class);
		leDAO = trackerDatabaseHelper.getLocationExerciseDAO();
		eDAO = trackerDatabaseHelper.getExerciseDAO();
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
