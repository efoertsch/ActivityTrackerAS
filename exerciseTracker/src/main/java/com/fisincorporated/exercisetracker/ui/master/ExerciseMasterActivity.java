package com.fisincorporated.exercisetracker.ui.master;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.LocationExercise;
import com.fisincorporated.exercisetracker.database.TrackerDatabaseHelper;
import com.fisincorporated.exercisetracker.facebook.FacebookPostStatsActivity;
import com.fisincorporated.exercisetracker.facebook.FacebookPostStatsFragment;
import com.fisincorporated.exercisetracker.ui.charts.AltitudeVsDistanceGraphFragment;
import com.fisincorporated.exercisetracker.ui.charts.GraphActivity;
import com.fisincorporated.exercisetracker.ui.maps.ActivityMap;
import com.fisincorporated.exercisetracker.ui.maps.ActivityMapFragment;
import com.fisincorporated.exercisetracker.ui.stats.ActivityDetailFragment;
import com.fisincorporated.exercisetracker.ui.stats.ActivityPager;

import dagger.android.AndroidInjection;
import dagger.android.support.DaggerAppCompatActivity;

abstract public class ExerciseMasterActivity extends DaggerAppCompatActivity implements IHandleSelectedAction {
	protected TrackerDatabaseHelper databaseHelper = null;
	protected SQLiteDatabase database = null;
	protected Cursor csrUtility;

	protected ActionBar actionBar;
	protected Toolbar toolbar;


	public void onCreate(Bundle savedInstanceState) {
		AndroidInjection.inject(this);
		super.onCreate(savedInstanceState);
		getDatabaseSetup();
	}

	//TODO user Dagger injection
	public void getDatabaseSetup() {
		if (databaseHelper == null) {
			databaseHelper = TrackerDatabaseHelper.getTrackerDatabaseHelper();
		}
		if (database == null) {
			database = databaseHelper.getWritableDatabase();
		}

		if (!database.isOpen()) {
			database = databaseHelper.getWritableDatabase();
		}
	}
	
	public void onSelectedAction(Bundle args) {
		Fragment newDetail = null;
		if (findViewById(R.id.detailFragmentContainer) == null) {
			// start info from bundle to load to intent and start instance of
			// ActivityPager
			switch (args.getInt(GlobalValues.DISPLAY_TARGET)) {
			 case GlobalValues.DISPLAY_STATS: 
				startViewPager(args);
				break;
			 case GlobalValues.DISPLAY_MAP:
				startActivityMap(args);
				break;
			 case  GlobalValues.DISPLAY_CHART:
				startCharts(args);
				break;
			 case  GlobalValues.DISPLAY_FACEBOOK_TO_POST:
				 startFacebookToPost(args);
				break;
			}
		} else {
			// display the activity info in the detailfragment container
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			Fragment oldDetail = fm.findFragmentById(R.id.detailFragmentContainer);
			Bundle bundle = new Bundle(args);
			switch (args.getInt(GlobalValues.DISPLAY_TARGET)) {
			case GlobalValues.DISPLAY_STATS: 
				newDetail = ActivityDetailFragment.newInstance(bundle);
				break;
			case GlobalValues.DISPLAY_MAP:
				newDetail = ActivityMapFragment.newInstance(bundle);
				break;
			case  GlobalValues.DISPLAY_CHART:
				newDetail = AltitudeVsDistanceGraphFragment.newInstance(bundle);
				break;
			case  GlobalValues.DISPLAY_FACEBOOK_TO_POST:
				newDetail = FacebookPostStatsFragment.newInstance(bundle);
			}
				
			if (oldDetail != null) {
				ft.remove(oldDetail);
			}
			if (newDetail != null){
				ft.add(R.id.detailFragmentContainer, newDetail);
			}
			ft.commit();
		}
	}

	private void startCharts(Bundle args) {
		Intent intent = new Intent(this, GraphActivity.class);
		xferBundleToIntent(intent, args);
		startActivity(intent);
	
	}

	private void startActivityMap(Bundle args) {
		Intent intent = new Intent(this, ActivityMap.class);
		xferBundleToIntent(intent, args);
		startActivity(intent);
	}
	
	private void startFacebookToPost(Bundle args) {
		Intent intent = new Intent(this, FacebookPostStatsActivity.class);
		xferBundleToIntent(intent, args);
		startActivity(intent);
	}

	private void startViewPager(Bundle args) {
		Intent intent = new Intent(this, ActivityPager.class);
		xferBundleToIntent(intent, args);
		startActivity(intent);
	}
	
	protected void xferBundleToIntent(Intent intent, Bundle args){
		intent.putExtra(GlobalValues.BAR_CHART_TYPE, args.getInt(GlobalValues.BAR_CHART_TYPE, GlobalValues.BAR_CHART_LAST_MONTH));
		intent.putExtra(LocationExercise._ID, args.getLong(LocationExercise._ID));
		intent.putExtra(GlobalValues.TITLE, args.getString(GlobalValues.TITLE));
		intent.putExtra(LocationExercise.DESCRIPTION, args.getString(LocationExercise.DESCRIPTION));
		intent.putExtra(GlobalValues.SORT_ORDER,
				args.getInt(GlobalValues.SORT_ORDER));
		intent.putStringArrayListExtra(GlobalValues.EXERCISE_FILTER_PHRASE,
				args.getStringArrayList(GlobalValues.EXERCISE_FILTER_PHRASE));
		intent.putStringArrayListExtra(GlobalValues.LOCATION_FILTER_PHRASE,
				args.getStringArrayList(GlobalValues.LOCATION_FILTER_PHRASE));
		intent.putExtra(GlobalValues.CURSOR_POSITION,
				args.getInt(GlobalValues.CURSOR_POSITION));
		
		// for Facebook
		intent.putExtra(GlobalValues.ACTIVITY_STATS,args.getString(GlobalValues.ACTIVITY_STATS));

	}
}
