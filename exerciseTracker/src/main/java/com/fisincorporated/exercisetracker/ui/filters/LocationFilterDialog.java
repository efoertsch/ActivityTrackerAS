package com.fisincorporated.exercisetracker.ui.filters;

import android.os.Bundle;

import java.util.ArrayList;

public class LocationFilterDialog extends FilterDialogFragment {

	public LocationFilterDialog() {}

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
		return databaseHelper.loadLocationSelectionArray();
	}

}
