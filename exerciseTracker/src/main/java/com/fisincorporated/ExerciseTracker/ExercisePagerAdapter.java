package com.fisincorporated.ExerciseTracker;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.fisincorporated.database.ExerciseDAO;
import com.fisincorporated.database.ExerciseRecord;
import com.fisincorporated.database.TrackerDatabase.Exercise;

public class ExercisePagerAdapter<F extends Fragment> extends
		FragmentStatePagerAdapter {

	private final Class<F> fragmentClass;
	private Cursor cursor;

	public ExercisePagerAdapter(FragmentManager fm, Class<F> fragmentClass,
			Cursor cursor) {
		super(fm);
		this.fragmentClass = fragmentClass;
		this.cursor = cursor;

	}

	@Override
	public F getItem(int position) {
		if (cursor == null) // shouldn't happen
			return null;

		cursor.moveToPosition(position);
		ExerciseRecord er = new ExerciseRecord();
		ExerciseDAO.loadExerciseRecord(cursor, er);
		F frag;
		try {
			Bundle args = new Bundle();
			args.putParcelable(Exercise.EXERCISE_TABLE, er);
			frag = fragmentClass.newInstance();
			frag.setArguments(args);

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}

		return frag;
	}

	@Override
	public int getCount() {
		if (cursor == null)
			return 0;
		else
			return cursor.getCount();
	}

	public void swapCursor(Cursor c) {
		if (cursor == c)
			return;
		// not part of original code. Added to debug attempt but via debugger this
		// method
		// is not called at all prior to close error
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}

		this.cursor = c;
		notifyDataSetChanged();
	}

	public Cursor getCursor() {
		return cursor;
	}
}
