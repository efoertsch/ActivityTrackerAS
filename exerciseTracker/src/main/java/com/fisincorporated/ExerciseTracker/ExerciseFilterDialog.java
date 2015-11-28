package com.fisincorporated.ExerciseTracker;

import java.util.ArrayList;

import com.fisincorporated.database.TrackerDatabase.Exercise;

import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;

public class ExerciseFilterDialog extends FilterDialogFragment {

	public ExerciseFilterDialog() {
		// TODO Auto-generated constructor stub
	}

	public static ExerciseFilterDialog newInstance(
			ArrayList<String> originalSelections, String title) {
		Bundle args = new Bundle();
		args.putStringArrayList(EXTRA_SELECTIONS, originalSelections);
		args.putString(EXTRA_TITLE, title);
		ExerciseFilterDialog fragment = new ExerciseFilterDialog();
		fragment.setArguments(args);
		return fragment;
	}

	protected ArrayList<String> loadSelectionArray() {
		Cursor csr = null;
		ArrayList<String> selections = new ArrayList<String>();
		try {
			csr = database.query(Exercise.EXERCISE_TABLE,
					new String[] { Exercise.EXERCISE }, null, null, null, null,
					Exercise.DEFAULT_SORT_ORDER);
			if (csr.getCount() != 0) {
				csr.moveToFirst();
				while (!csr.isAfterLast()) {
					selections.add(csr.getString(csr
							.getColumnIndex(Exercise.EXERCISE)));
					csr.moveToNext();
				}
			}
		} catch (SQLException sqle) {
			Log.i(GlobalValues.LOG_TAG,"ExerciseFilterDialog.loadSelectionArray sqlexception : " + sqle.toString());
		} finally {
			if (csr != null && !csr.isClosed()) {
				Log.i(GlobalValues.LOG_TAG,
						"ExerciseFilterDialog.loadSelectionArray closing cursor");
				csr.close();
			}
		}
		return selections;
	}
}
