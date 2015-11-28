package com.fisincorporated.ExerciseTracker;


import android.support.v4.app.Fragment;

public class FunctionListActivity extends ExerciseMasterFragmentActivity {


	@Override
	protected Fragment createFragment() {
		// TODO Auto-generated method stub
		return new Functions();
	}

}
