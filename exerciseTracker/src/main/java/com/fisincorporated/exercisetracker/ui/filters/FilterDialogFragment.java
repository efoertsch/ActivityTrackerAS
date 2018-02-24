package com.fisincorporated.exercisetracker.ui.filters;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.widget.ArrayAdapter;

import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.database.TrackerDatabaseHelper;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.AndroidSupportInjection;
import dagger.android.support.HasSupportFragmentInjector;

public abstract class FilterDialogFragment extends DialogFragment implements HasSupportFragmentInjector {

	public static final String EXTRA_SELECTIONS = "com.fisincorporated.ExerciseTracker.checkedSelections";
	public static final String EXTRA_TITLE = "com.fisincorporated.ExerciseTracker.title";

	private ArrayList<String> selections;
	private ArrayList<String> originalSelections;
	private ArrayList<String> checkedSelections;
	private boolean[] checkedItems;

    @Inject
    DispatchingAndroidInjector<Fragment> childFragmentInjector;

	@Inject
	TrackerDatabaseHelper databaseHelper;

	public FilterDialogFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        AndroidSupportInjection.inject(this);
    }

	@NonNull
    @SuppressWarnings("unchecked")
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		String title = getArguments().getString(EXTRA_TITLE);
		originalSelections = getArguments().getStringArrayList(EXTRA_SELECTIONS);
		checkedSelections = (ArrayList<String>) originalSelections.clone();
		selections = loadSelectionArray();
		setCheckedItems();
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				getActivity(), android.R.layout.simple_spinner_dropdown_item,
				selections);
		arrayAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		builder.setTitle(title);
		builder.setMultiChoiceItems(selections.toArray(new String[0]),
				checkedItems, (dialog, which, isChecked) -> {
                    // Log.i(GlobalValues.LOG_TAG, "Item " + which
                    // + (isChecked ? " is checked" : " is not checked"));
                    checkedItems[which] = isChecked;
                });

		// The OK button returns a (possibly) updated filter list)
		builder.setPositiveButton(R.string.ok,
				(dialog, id) -> {
                    createNewFilterList();
                    sendResult(Activity.RESULT_OK);
                });
		// Negative button is Cancel so return the original filter list
		builder.setNegativeButton(R.string.cancel,
				(dialog, id) -> {
                    // Log.i(GlobalValues.LOG_TAG, "NegativeButton Clicked");
                    checkedSelections = originalSelections;
                    sendResult(Activity.RESULT_OK);
                });
		// set neutral is to clear filter (show all)
		builder.setNeutralButton(R.string.clear_filter,
				(dialog, id) -> {
                    // Log.i(GlobalValues.LOG_TAG, "Neutral Clicked");
                    checkedSelections = new ArrayList<String>();
                    sendResult(Activity.RESULT_OK);
                });
		return builder.create();
	}

	// Load list of strings into selections ArrayList
	protected abstract ArrayList<String> loadSelectionArray();

	private void setCheckedItems() {
		checkedItems = new boolean[selections.size()];
		for (int i = 0; i < checkedSelections.size(); ++i) {
			for (int j = 0; j < selections.size(); ++j) {
				if (selections.get(j).equals(checkedSelections.get(i))) {
					checkedItems[j] = true;
					break;
				}
			}
		}
	}

	private void createNewFilterList() {
		checkedSelections = new ArrayList<>();
		for (int i = 0; i < selections.size(); ++i) {
			if (checkedItems[i])
				checkedSelections.add(selections.get(i));
		}
	}

	private void sendResult(int resultCode) {
		if (getTargetFragment() == null) {
			return;
		}
		Intent intent = new Intent();
		intent.putStringArrayListExtra(FilterDialogFragment.EXTRA_SELECTIONS,
				checkedSelections);
		getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode,
				intent);
	}

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return childFragmentInjector;
    }

}