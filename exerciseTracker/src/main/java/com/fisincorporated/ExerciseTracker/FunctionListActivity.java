package com.fisincorporated.ExerciseTracker;


import android.os.Bundle;
import android.support.v4.app.Fragment;

public class FunctionListActivity extends ExerciseMasterFragmentActivity {


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		actionBar.setDisplayHomeAsUpEnabled(false);
	}

	@Override
	protected Fragment createFragment() {
		// TODO Auto-generated method stub
		return new Functions();
	}

}
