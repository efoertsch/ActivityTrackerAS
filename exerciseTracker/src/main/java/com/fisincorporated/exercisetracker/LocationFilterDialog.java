package com.fisincorporated.exercisetracker;

import java.util.ArrayList;

import com.fisincorporated.exercisetracker.database.TrackerDatabase.ExrcsLocation;

import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;

public class LocationFilterDialog extends FilterDialogFragment {

	public LocationFilterDialog() {
		// TODO Auto-generated constructor stub
	}

	public static LocationFilterDialog newInstance(
			ArrayList<String> locationSelections, String title) {
		Bundle args = new Bundle();
		args.putStringArrayList(EXTRA_SELECTIONS, locationSelections);
		args.putString(EXTRA_TITLE, title);
		LocationFilterDialog fragment = new LocationFilterDialog();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	protected ArrayList<String> loadSelectionArray() {
		Cursor csr = null;
		ArrayList<String> selections = new ArrayList<String>();
		try {
			csr = database.query(ExrcsLocation.LOCATION_TABLE,
					new String[] { ExrcsLocation.LOCATION }, null, null, null, null,
					ExrcsLocation.DEFAULT_SORT_ORDER);

			if (csr.getCount() != 0) {
				csr.moveToFirst();
				while (!csr.isAfterLast()) {
					selections.add(csr.getString(csr
							.getColumnIndex(ExrcsLocation.LOCATION)));
					csr.moveToNext();
				}
			}
		} catch (SQLException sqle) {
			Log.i(GlobalValues.LOG_TAG,"LocationFilterDialog.loadSelectionArray sqlexception : " + sqle.toString());
		} finally {
			if (csr != null && !csr.isClosed()) {
				Log.i(GlobalValues.LOG_TAG,
						"LocationFilterDialog.loadSelectionArray closing cursor");
				csr.close();
			}
		}
		return selections;
	}

}
