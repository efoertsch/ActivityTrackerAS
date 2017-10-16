package com.fisincorporated.exercisetracker;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.fisincorporated.exercisetracker.database.ExerciseDAO;
import com.fisincorporated.exercisetracker.database.SQLiteCursorLoader;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.Exercise;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.ExrcsLocation;

public class ExerciseListFragment extends AbstractListFragment implements
		LoaderCallbacks<Cursor> {
	ListView exerciseListView = null;

	protected Callbacks callBacks;

	public interface Callbacks {
		void onExerciseSelected(Bundle args);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		callBacks = (Callbacks) activity;
	}

	 

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.exerciselist, container, false);
		exerciseListView = (ListView) view.findViewById(android.R.id.list);
		final Button btnAddNewActivity = (Button) view
				.findViewById(R.id.exerciselist_btnAddNewActivity);

		View footerView = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.exerciselist_footer, null, false);
		exerciseListView.addFooterView(footerView);
		// Handle Add new Button
//		btnAddNewActivity.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				// Go to the exercise maintenance activity to add new exercise
//				// mod for tablets, return selected (new) exercise to acivity and it will determine
//				// if for detail fragment or call activity to display exercise fragment
//				 Bundle args = new Bundle();
//				 args.putParcelable(Exercise.EXERCISE_TABLE, new ExerciseRecord());
//				 callBacks.onExerciseSelected(args);
//			}
//		});

		return view;

	}

	public void onListItemClick(ListView l, View v, int position, long id) {
		// Toast.makeText(getActivity(),
		// "Clicked on position:" + position + "  Row id:" + id,
		// Toast.LENGTH_LONG).show();
		Bundle args = new Bundle();
		args.putParcelable(Exercise.EXERCISE_TABLE,
				new ExerciseDAO(databaseHelper).loadExerciseRecordById(id));
		args.putInt(GlobalValues.CURSOR_POSITION, position);
		callBacks.onExerciseSelected(args);
	}

	public void onResume() {
		super.onResume();
		getLoaderManager().restartLoader(0, null, this);
	}

	private static class ListCursorLoader extends SQLiteCursorLoader {
		ExerciseListFragment el;

		public ListCursorLoader(Context context, ExerciseListFragment el) {
			super(context);
			this.el = el;
		}

		@Override
		protected Cursor loadCursor() {
			return el.showExerciseList();
		}
	}

	private Cursor showExerciseList() {
		Cursor cursor = null;
		StringBuffer sb = new StringBuffer();
		sb.append("select " + Exercise._ID + "," + Exercise.EXERCISE + " from "
				+ Exercise.EXERCISE_TABLE + " order by "
				+ Exercise.DEFAULT_SORT_ORDER);
		getDatabaseSetup();
		cursor = database.rawQuery(sb.toString(), null);

		if (cursor.getCount() == 0)
			return null;
		return cursor;

	}

	// This controls what column to place in the edittext when selected. The
	// default textview.tostring, not helpful
	class MyCursorToStringConverter implements
			SimpleCursorAdapter.CursorToStringConverter {

		public CharSequence convertToString(Cursor cursor) {
			return cursor.getString(cursor.getColumnIndex(ExrcsLocation.LOCATION));
		}
	}

	// LoaderCallBacks interface methods
	// #1
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// we only ever load the runs, so assume this is the case
		return new ListCursorLoader(getActivity(), this);
	}

	// #2
	@SuppressWarnings("deprecation")
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),
				R.layout.exerciserow, cursor, new String[] { Exercise.EXERCISE },
				new int[] { R.id.exerciserow_exercise },0);
		setListAdapter(adapter);
	}

	// #3
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// stop using the cursor (via the adapter)
		setListAdapter(null);
	}
	
	public void finalize(){
		Log.e(GlobalValues.LOG_TAG,"ExerciseMaintenance.finalize. Calling super.finalize to close db");
		super.finalize();
	}

}