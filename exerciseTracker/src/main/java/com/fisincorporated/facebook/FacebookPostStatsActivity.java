package com.fisincorporated.facebook;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.fisincorporated.ExerciseTracker.ExerciseMasterFragmentActivity;
import com.fisincorporated.ExerciseTracker.GlobalValues;
import com.fisincorporated.database.TrackerDatabase.LocationExercise;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

//http://stackoverflow.com/questions/21634138/working-example-of-latest-android-facebook-sdk-login
public class FacebookPostStatsActivity extends ExerciseMasterFragmentActivity {
	 
	private static final String TAG = "FacebookPostStatsActivity";
	
	CallbackManager callbackManager;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        
    }
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    callbackManager.onActivityResult(requestCode, resultCode, data);
	}
		@Override
		protected Fragment createFragment() {
			Intent intent = getIntent();
			Bundle bundle = new Bundle();
	 		 
			bundle.putString(GlobalValues.TITLE, intent.getStringExtra(GlobalValues.TITLE));
			bundle.putString(LocationExercise.DESCRIPTION, intent.getStringExtra(LocationExercise.DESCRIPTION));
			bundle.putString(GlobalValues.ACTIVITY_STATS,intent.getStringExtra(GlobalValues.ACTIVITY_STATS));
			return   FacebookPostStatsFragment.newInstance(bundle);

		}
		

}