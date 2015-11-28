package com.fisincorporated.ExerciseTracker;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;

import com.fisincorporated.database.SQLiteCursorLoader;
import com.fisincorporated.database.TrackerDatabase.Exercise;

public class ExercisePager extends ExerciseMasterActivity implements
		LoaderCallbacks<Cursor>, ExerciseListFragment.Callbacks {

	private int cursorPosition = 0;
	private ViewPager viewPager;

	// This is an unused empty method but is needed for compatibility
	// with ExerciseListFragment interface requirements
	@Override
	public void onExerciseSelected(Bundle args) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		viewPager = new ViewPager(this);
		viewPager.setId(R.id.viewPager);
		viewPager.setOffscreenPageLimit(1);

		setContentView(viewPager);
		Intent intent = getIntent();
		cursorPosition = intent.getIntExtra(GlobalValues.CURSOR_POSITION, 0);

	}

	public void onResume() {
		super.onResume();
		getDatabaseSetup();
		getSupportLoaderManager().restartLoader(GlobalValues.EXERICSE_PAGER_LOADER, null, this);
	}

	private static class ListCursorLoader extends SQLiteCursorLoader {
		ExercisePager ap;

		public ListCursorLoader(Context context, ExercisePager ap) {
			super(context);
			this.ap = ap;
		}

		@Override
		protected Cursor loadCursor() {
			return ap.getExerciseListCursor();
		}
	}

	protected Cursor getExerciseListCursor() {
		csrUtility = database.query(Exercise.EXERCISE_TABLE,
				Exercise.exerciseColumnNames, null, null, null, null,
				Exercise.DEFAULT_SORT_ORDER);
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
		viewPager
				.setAdapter(new ExercisePagerAdapter<ExerciseMaintenanceFragment>(
						getSupportFragmentManager(),
						ExerciseMaintenanceFragment.class, cursor));
		viewPager.setCurrentItem(cursorPosition);
	}

	// #3
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// stop using the cursor (via the adapter)
		// setting to null gives error stating fragment is not in the fragment
		// manager
		// this methond is being called on exit from ActivityPager
		// viewPager.setAdapter(null);
		((ExercisePagerAdapter<?>  )viewPager.getAdapter() ).swapCursor(null);
	}

	 

}
