package com.fisincorporated.ExerciseTracker;
 
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.fisincorporated.interfaces.IHandleSelectedAction;

public abstract class ExerciseMasterFragmentActivity extends ExerciseMasterActivity implements IHandleSelectedAction {
    protected ActionBar mActionBar;
    protected Toolbar mToolbar;
    protected AppBarLayout mAppBarLayout;

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
		// implement this in subclasses?
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		if (mToolbar != null) {
			setSupportActionBar(mToolbar);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
		mActionBar = getSupportActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
        mAppBarLayout.setExpanded(false);

		FragmentManager fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
		if (fragment == null){
			fragment = createFragment();			
			fm.beginTransaction().add(R.id.fragmentContainer, fragment)
			.commit();
		}
		 
	}

	
	
 
	
}
