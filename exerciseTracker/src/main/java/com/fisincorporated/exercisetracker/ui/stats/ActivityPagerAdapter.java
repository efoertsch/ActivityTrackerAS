package com.fisincorporated.exercisetracker.ui.stats;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.Exercise;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.ExrcsLocation;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.LocationExercise;
import com.fisincorporated.exercisetracker.utility.Utility;

import java.text.SimpleDateFormat;

import javax.inject.Inject;

// From http://tumble.mlcastle.net/post/25875136857/bridging-cursorloaders-and-viewpagers-on-android
// note the original example used a more generic method of getting the args values from the cursor and
// passing them to the  fragment newInstance call. Not using here as fragment and other code already set
//  but next time plass all args as strings
public class ActivityPagerAdapter<F extends Fragment> extends FragmentStatePagerAdapter {
    protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private final Class<F> fragmentClass;
    private Cursor cursor;

    @Inject
    public Utility utility;

    public ActivityPagerAdapter(FragmentManager fm, Class<F> fragmentClass, Cursor cursor) {
        super(fm);
        this.fragmentClass = fragmentClass;
        this.cursor = cursor;
    }

    @Override
    public F getItem(int position) {
        if (cursor == null) // shouldn't happen
            return null;

        cursor.moveToPosition(position);
        F frag;
        try {
            Bundle args = new Bundle();
            args.putLong(LocationExercise._ID, cursor.getLong(cursor.getColumnIndex(LocationExercise._ID)));
            args.putString(GlobalValues.TITLE,
                    cursor.getString(cursor.getColumnIndex(Exercise.EXERCISE))
                            + "@" + cursor.getString(cursor.getColumnIndex(ExrcsLocation.LOCATION))
                            + " " + utility.formatDate(dateFormat, cursor.getString(cursor.getColumnIndex(GlobalValues.START_DATE))));
            args.putString(LocationExercise.DESCRIPTION, cursor.getString(cursor.getColumnIndex(LocationExercise.DESCRIPTION)));
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
        // not part of original code. Added to debug attempt but via debugger this method
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
