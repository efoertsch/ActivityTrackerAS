package com.fisincorporated.exercisetracker.ui.master;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.fisincorporated.exercisetracker.ui.maps.ActivityMap;
import com.fisincorporated.exercisetracker.ui.maps.ActivityMapFragment;
import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.LocationExercise;
import com.fisincorporated.exercisetracker.database.TrackerDatabaseHelper;
import com.fisincorporated.exercisetracker.ui.charts.AltitudeVsDistanceGraphFragment;
import com.fisincorporated.exercisetracker.ui.charts.GraphActivity;
import com.fisincorporated.exercisetracker.ui.stats.ActivityDetailFragment;
import com.fisincorporated.exercisetracker.ui.stats.ActivityPager;
import com.fisincorporated.facebook.FacebookPostStatsActivity;
import com.fisincorporated.facebook.FacebookPostStatsFragment;

abstract public class ExerciseMasterActivity extends AppCompatActivity implements IHandleSelectedAction {
	protected TrackerDatabaseHelper databaseHelper = null;
	protected SQLiteDatabase database = null;
	protected Cursor csrUtility;

	protected static String imperialMetric;
	protected static String imperial;
	protected static String feetMeters;
	protected static String milesKm;
	protected static String mphKph;

	protected ActionBar actionBar;
	protected Toolbar toolbar;


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public void getDatabaseSetup() {
		if (databaseHelper == null)
			databaseHelper = TrackerDatabaseHelper.getTrackerDatabaseHelper(this);
		if (database == null)
			database = databaseHelper.getWritableDatabase();
		if (!database.isOpen())
			database = databaseHelper.getWritableDatabase();
	}

	public TrackerDatabaseHelper getTrackerDataseHelper() {
		return databaseHelper;
	}

	public SQLiteDatabase getSQLiteDatabase() {
		return database;
	}

	@SuppressLint("NewApi")
	public void deleteDatabase() {
		databaseHelper.getDatabaseName();
	}

	public void onDestroy() {
		if (database != null) {
			if (database.isOpen()){
				database.close();
			}
			database = null;
		}
		super.onDestroy();
	}

	@Override
	public void finalize() {
		if (database != null) {
			if (database.isOpen()){
				database.close();
			}
			database = null;
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
