package com.fisincorporated.exercisetracker;

import com.fisincorporated.exercisetracker.database.TrackerDatabase.LocationExercise;
import com.fisincorporated.exercisetracker.ui.master.ExerciseMasterFragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class ActivityChart extends ExerciseMasterFragmentActivity {
	 
	
	public ActivityChart() {
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	}
	 
	@Override
	protected Fragment createFragment() {
		Intent intent = getIntent();
		Bundle bundle = new Bundle();
		int chartType = intent.getIntExtra(GlobalValues.BAR_CHART_TYPE, GlobalValues.BAR_CHART_LAST_MONTH);
		switch (chartType) {
			case GlobalValues.DISTANCE_VS_ELEVATION:
					bundle.putLong(LocationExercise._ID, intent.getLongExtra(LocationExercise._ID, -1));
					bundle.putString(GlobalValues.TITLE, intent.getStringExtra(GlobalValues.TITLE));
					bundle.putString(LocationExercise.DESCRIPTION, intent.getStringExtra(LocationExercise.DESCRIPTION));
					// only have one chart for now
					return   ElevationVsDistanceFragment.newInstance(bundle);
			default:
				bundle.putStringArrayList(GlobalValues.EXERCISE_FILTER_PHRASE,
						intent.getStringArrayListExtra(GlobalValues.EXERCISE_FILTER_PHRASE));
				bundle.putStringArrayList(GlobalValues.LOCATION_FILTER_PHRASE,
						intent.getStringArrayListExtra(GlobalValues.LOCATION_FILTER_PHRASE));
				bundle.putInt(GlobalValues.BAR_CHART_TYPE, intent.getIntExtra(GlobalValues.BAR_CHART_TYPE, GlobalValues.BAR_CHART_LAST_MONTH));
				return ActivityDistanceChartFragment.newInstance(bundle);
		}

	}
}
