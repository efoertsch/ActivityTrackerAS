package com.fisincorporated.exercisetracker.ui.master;
 
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;

import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.interfaces.IHandleSelectedAction;

public abstract class ExerciseMasterFragmentActivity extends ExerciseMasterActivity implements IHandleSelectedAction {

    protected abstract Fragment createFragment();

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
            if (fragment != null) {
                fm.beginTransaction().add(R.id.fragmentContainer, fragment)
                        .commit();
            }
		}

		// implement this in superclass?
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		if (toolbar != null) {
			setSupportActionBar(toolbar);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
		actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		 
	}

	public void setActivityTitle(@StringRes int stringRes){
		if (actionBar != null) {
			actionBar.setTitle(stringRes);
		}

	}

	
	
 
	
}
