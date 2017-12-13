package com.fisincorporated.exercisetracker.ui.logger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.fisincorporated.exercisetracker.database.TrackerDatabase.Exercise;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.ExrcsLocation;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.LocationExercise;
import com.fisincorporated.exercisetracker.ui.master.ExerciseMasterFragmentActivity;

public class ActivityLoggerActivity extends ExerciseMasterFragmentActivity {

	@Override
	protected Fragment createFragment() {
		Intent intent = getIntent();
	// put what StartExerciseFragment is sending into Bundle for fragment
		Bundle bundle = new Bundle();
		bundle.putParcelable(LocationExercise.LOCATION_EXERCISE_TABLE, intent.getParcelableExtra(LocationExercise.LOCATION_EXERCISE_TABLE));
		bundle.putString(Exercise.EXERCISE,intent.getStringExtra(Exercise.EXERCISE));
		bundle.putString(ExrcsLocation.LOCATION, intent.getStringExtra(ExrcsLocation.LOCATION));
		bundle.putFloat(Exercise.MIN_DISTANCE_TO_LOG, intent.getFloatExtra(Exercise.MIN_DISTANCE_TO_LOG, 10));
		bundle.putInt(Exercise.ELEVATION_IN_DIST_CALCS,intent.getIntExtra(Exercise.ELEVATION_IN_DIST_CALCS,0));
		bundle.putString(LocationExercise.DESCRIPTION, intent.getStringExtra(LocationExercise.DESCRIPTION));
		return  ActivityLoggerFragment.newInstance(bundle);
	
	}

}
