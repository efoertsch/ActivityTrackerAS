package com.fisincorporated.exercisetracker.ui.maps;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.LocationExercise;
import com.fisincorporated.exercisetracker.ui.master.ExerciseMasterFragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class ActivityMap extends ExerciseMasterFragmentActivity {
	

	
	public ActivityMap() {
		// TODO Auto-generated constructor stub
	}
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	}
	 
	@Override
	protected Fragment createFragment() {
		Intent intent = getIntent();
		Bundle bundle = new Bundle();
 		bundle.putLong(LocationExercise._ID, intent.getLongExtra(LocationExercise._ID, -1));
		bundle.putString(GlobalValues.TITLE, intent.getStringExtra(GlobalValues.TITLE));
		bundle.putString(LocationExercise.DESCRIPTION, intent.getStringExtra(LocationExercise.DESCRIPTION));
		bundle.putBoolean(ActivityMapFragment.USE_CURRENT_LOCATION_LABEL,intent.getBooleanExtra(ActivityMapFragment.USE_CURRENT_LOCATION_LABEL, false));
		return   ActivityMapFragment.newInstance(bundle);

	}
}
