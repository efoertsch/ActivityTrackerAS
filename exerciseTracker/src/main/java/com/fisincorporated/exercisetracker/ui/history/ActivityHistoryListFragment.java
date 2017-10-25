package com.fisincorporated.exercisetracker.ui.history;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fisincorporated.exercisetracker.ActivityListFragment;
import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.database.LocationExerciseDAO;
import com.fisincorporated.exercisetracker.database.LocationExerciseRecord;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.Exercise;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.ExrcsLocation;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.LocationExercise;
import com.fisincorporated.utility.Utility;

import java.text.DateFormat;

public class ActivityHistoryListFragment extends ActivityListFragment {

	ButtonCursorAdapter buttonCursorAdapter = null;
  
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		restoreSortFilter(getArguments());
		View view = inflater.inflate(R.layout.prior_activity_list, container,
				false);
		activityListView = (ListView) view.findViewById(android.R.id.list);
		//activityListView.setEmptyView(view.findViewById(R.id.empty_activity_list));
//		registerForContextMenu((View) view
//				.findViewById(R.id.prior_activity_list_columns));
		return view;
	}
	
	 

	public void onListItemClick(ListView l, View v, int position, long id) {
		Bundle args = new Bundle();
		args.putLong(LocationExercise._ID, id);
		args.putString(GlobalValues.TITLE,
 				((TextView) v.findViewById(R.id.prior_activity_row_activity)).getText()
 				+ "@" + ((TextView) v.findViewById(R.id.prior_activity_row_location)).getText()
 				+ " " + 	((TextView) v.findViewById(R.id.prior_activity_row_date)).getText()); 
		args.putString(LocationExercise.DESCRIPTION, (String) ((TextView) v.findViewById(R.id.prior_activity_row_description)).getText()) ;
		args.putInt(GlobalValues.SORT_ORDER, sortOrder);
		args.putStringArrayList(GlobalValues.EXERCISE_FILTER_PHRASE, exerciseSelections);
		args.putStringArrayList(GlobalValues.LOCATION_FILTER_PHRASE, locationSelections);
		args.putInt(GlobalValues.CURSOR_POSITION, position);
		args.putInt(GlobalValues.DISPLAY_TARGET, GlobalValues.DISPLAY_STATS);
		callBacks.onSelectedAction(args);
	}



	public class ButtonCursorAdapter extends SimpleCursorAdapter {

		@SuppressWarnings("deprecation")
		public ButtonCursorAdapter(Context context, int layout, Cursor csr,
				String[] from, int[] to) {
			super(context, layout, csr, from, to,0);
		}

		public void bindView(View view, Context context, Cursor cursor) {
			ImageButton infoButton = (ImageButton) view
					.findViewById(R.id.activity_history_row_btnShowMap);
			long row_id = cursor.getLong(cursor.getColumnIndex(LocationExercise._ID));
			infoButton.setTag((Long) row_id);
			infoButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Toast.makeText(getActivity().getBaseContext(),
							getActivity().getResources().getString(R.string.displaying_the_map_may_take_a_moment), Toast.LENGTH_SHORT)
							.show();
					 Bundle args = new Bundle();
					 args.putLong(LocationExercise._ID, (Long) v.getTag());
					 addToBundle((Long)v.getTag(), args);
					 args.putInt(GlobalValues.DISPLAY_TARGET, GlobalValues.DISPLAY_MAP);
					 callBacks.onSelectedAction(args); 
					
				}
			});
			super.bindView(view, context, cursor);
		}

	}

	/**
	 * Get the title for the map This could be done in a better manner...
	 */
	private void addToBundle(long locationExerciseId, Bundle bundle) {
		String title = " ";
		LocationExerciseDAO leDAO = new LocationExerciseDAO(databaseHelper);
		LocationExerciseRecord ler = leDAO
				.loadLocationExerciseRecordById(locationExerciseId);
		if (ler.get_id() > 0) {
			title = leDAO.getExercise(ler.getExerciseId()) + "@"
					+ leDAO.getLocation(ler.getLocationId()) + " "
					+ DateFormat.getDateInstance().format(ler.getStartTimestamp());
				bundle.putString(GlobalValues.TITLE,title);
				bundle.putString(LocationExercise.DESCRIPTION, ler.getDescription());
				
		} else {
			title = getActivity().getString(R.string.error_getting_activity_info);
			bundle.putString(GlobalValues.TITLE,title);
			bundle.putString(LocationExercise.DESCRIPTION, "");
		}

	}
	
	@Override
	public void onResume(){
		super.onResume();
		if (doRefresh){
			getLoaderManager().restartLoader(0, null, this);
			doRefresh = false;
		}
			
		
	}

	// LoaderCallBacks interface methods
	// #1
	// @Override
	// public Loader<Cursor> onCreateLoader(int id, Bundle args) {
	//  we only use one cursor so int id can = 0 
	//  getDatabaseSetup();
	// return new ListCursorLoader(getActivity(), this);
	// }

	// #2
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		// create an adapter to point at this cursor
		// SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
		buttonCursorAdapter = new ButtonCursorAdapter(getActivity(),
				R.layout.prior_activity_row, cursor, new String[] {
						Exercise.EXERCISE, ExrcsLocation.LOCATION, GlobalValues.START_DATE,
						LocationExercise.DESCRIPTION }, new int[] {
						R.id.prior_activity_row_activity,
						R.id.prior_activity_row_location,
						R.id.prior_activity_row_date,
						R.id.prior_activity_row_description } );
		
		buttonCursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				// format the date to local date
				if (cursor.getColumnName(columnIndex).equalsIgnoreCase(GlobalValues.START_DATE)){
					String activityDate = cursor.getString(cursor.getColumnIndex(GlobalValues.START_DATE));
					((TextView) view).setText(Utility.formatDate(dateFormat, activityDate));
					return true;
				}
				return false;
			};
		});

		setListAdapter(buttonCursorAdapter);
	}

	// #3
	 @Override
	 public void onLoaderReset(Loader<Cursor> loader) {
	// stop using the cursor (via the adapter)
	// setting to null gives error stating fragment is not in the fragment
	// manager
	// this methond is being called on exit from ActivityPager
	// setListAdapter(null);
		((ButtonCursorAdapter)this.getListAdapter()).changeCursor(null);
	  }

	 

}
