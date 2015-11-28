package com.fisincorporated.ExerciseTracker;
 
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.fisincorporated.interfaces.IHandleSelectedAction;

public abstract class ExerciseMasterFragmentActivity extends ExerciseMasterActivity implements IHandleSelectedAction {
		
	protected abstract Fragment createFragment();
	
//	// added for master/detail fragments as on tablet
//	protected int getLayoutResId(){
//		return R.layout.activity_fragment;
//	}
	
// added for tablet

	protected int getLayoutResId() {
		return R.layout.activity_masterdetail;
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// added for master/detail fragments as on tablet
		//setContentView(R.layout.activity_fragment);
		setContentView(getLayoutResId());
		FragmentManager fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
		if (fragment == null){
			fragment = createFragment();			
			fm.beginTransaction().add(R.id.fragmentContainer, fragment)
			.commit();
		}
		 
	}

	
	
 
	
}
