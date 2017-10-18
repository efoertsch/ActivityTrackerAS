package com.fisincorporated.exercisetracker.ui.stats;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.widget.FrameLayout;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.database.SQLiteCursorLoader;
import com.fisincorporated.exercisetracker.ui.master.ExerciseMasterFragmentActivity;
import com.fisincorporated.exercisetracker.ui.utils.DepthPageTransformer;

import java.util.ArrayList;

public class ActivityPager extends ExerciseMasterFragmentActivity implements
		LoaderCallbacks<Cursor>   {
	private ViewPager viewPager;
	 
	private int defaultSortOrder = 6;
	protected int sortOrder = defaultSortOrder;
	private int cursorPosition = 0;
	protected ArrayList<String> exerciseSelections = new ArrayList<String>();
	protected ArrayList<String> locationSelections = new ArrayList<String>();


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		viewPager = new ViewPager(this);
		viewPager.setId(R.id.viewPager);
		viewPager.setPageTransformer(true, new DepthPageTransformer());

		viewPager.addOnPageChangeListener(new OnPageChangeListener(){
			@Override
			public void onPageScrollStateChanged(int state) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onPageSelected(int position) {
				cursorPosition = position; 
				Log.i(GlobalValues.LOG_TAG, "ActivityPager. position:" + position);
			}
	 
		});
		viewPager.setOffscreenPageLimit(1);
		FrameLayout frmLayout = (FrameLayout)findViewById(R.id.fragmentContainer);
		frmLayout.addView(viewPager);

		setActivityTitle(R.string.activity_stats);
		getFilterAndSort();

	}
	
	public void onResume(){
		super.onResume();
		getDatabaseSetup();
		getSupportLoaderManager().restartLoader(GlobalValues.ACTIVITY_PAGER_LOADER, null, this);
	}

	@Override
	protected Fragment createFragment() {
		return null;
	}

	private void getFilterAndSort() {
		Intent intent = getIntent();
		// So paging displays same activities and in same order as the list
		sortOrder = intent.getIntExtra(GlobalValues.SORT_ORDER, defaultSortOrder);
		exerciseSelections = intent
				.getStringArrayListExtra(GlobalValues.EXERCISE_FILTER_PHRASE);
		locationSelections = intent
				.getStringArrayListExtra(GlobalValues.LOCATION_FILTER_PHRASE);
		cursorPosition = intent.getIntExtra(GlobalValues.CURSOR_POSITION, 0);

	}

	private static class ListCursorLoader extends SQLiteCursorLoader {
		ActivityPager ap;

		// public ListCursorLoader(Context context,DeletePriorActivitiesFragment
		// palf) {
		public ListCursorLoader(Context context, ActivityPager ap) {
			super(context);
			this.ap = ap;
		}

		@Override
		protected Cursor loadCursor() {
			return ap.getActivitiesListCursor();
		}
	}

	protected Cursor getActivitiesListCursor() {
		StringBuffer query = new StringBuffer();
		databaseHelper.createActivitySQL(query, exerciseSelections,
				locationSelections, sortOrder);
		csrUtility = database.rawQuery(query.toString(), null);
		if (csrUtility.getCount() == 0)
			return null;
		return csrUtility;
	}

	// LoaderCallBacks interface methods
	// #1
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// we only ever load the runs, so assume this is the case
		return new ListCursorLoader(this, this);
	}

	// #2
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		viewPager.setAdapter(new ActivityPagerAdapter<ActivityDetailFragment>(
				getSupportFragmentManager(), ActivityDetailFragment.class,
				cursor));
		viewPager.setCurrentItem(cursorPosition);
		}

	// #3
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// stop using the cursor (via the adapter)
		// setting to null gives error stating fragment is not in the fragment manager
		// this methond is being called on exit from ActivityPager
		//viewPager.setAdapter(null);
		((ActivityPagerAdapter)viewPager.getAdapter()).swapCursor(null);
	}

}
