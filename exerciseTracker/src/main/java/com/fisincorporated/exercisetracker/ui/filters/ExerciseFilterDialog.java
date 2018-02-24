package com.fisincorporated.exercisetracker.ui.filters;

import android.os.Bundle;

import java.util.ArrayList;

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
		return databaseHelper.loadExerciseSelectionArray();
	}
}
