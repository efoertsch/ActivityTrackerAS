package com.fisincorporated.exercisetracker.ui.maintenance;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.widget.FrameLayout;

import com.fisincorporated.exercisetracker.ExerciseListFragment;
import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.database.SQLiteCursorLoader;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.Exercise;
import com.fisincorporated.exercisetracker.ui.master.ExerciseMasterFragmentActivity;
import com.fisincorporated.exercisetracker.ui.utils.DepthPageTransformer;

public class ExercisePagerActivity extends ExerciseMasterFragmentActivity implements
		LoaderCallbacks<Cursor>, ExerciseListFragment.Callbacks {

	private int cursorPosition = 0;
	private ViewPager viewPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		viewPager = new ViewPager(this);
		viewPager.setId(R.id.viewPager);
		viewPager.setOffscreenPageLimit(1);
		viewPager.setPageTransformer(true, new DepthPageTransformer());

		FrameLayout frmLayout = (FrameLayout)findViewById(R.id.fragmentContainer);
		frmLayout.addView(viewPager);
		Intent intent = getIntent();
		cursorPosition = intent.getIntExtra(GlobalValues.CURSOR_POSITION, 0);

        setActivityTitle(R.string.exercise_maintenance);
	}

	public void onResume() {
		super.onResume();
		getDatabaseSetup();
		getSupportLoaderManager().restartLoader(GlobalValues.EXERICSE_PAGER_LOADER, null, this);
	}

    @Override
    protected Fragment createFragment() {
        return null;
    }

    @Override
    public void onExerciseSelected(Bundle args) {}

    private static class ListCursorLoader extends SQLiteCursorLoader {
		ExercisePagerActivity ap;

		public ListCursorLoader(Context context, ExercisePagerActivity ap) {
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
		// this method is being called on exit from ActivityPager
		// viewPager.setAdapter(null);
		((ExercisePagerAdapter<?>  )viewPager.getAdapter() ).swapCursor(null);
	}

	 

}
