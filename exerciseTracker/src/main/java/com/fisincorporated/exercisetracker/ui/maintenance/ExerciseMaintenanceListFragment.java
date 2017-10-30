package com.fisincorporated.exercisetracker.ui.maintenance;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.database.SQLiteCursorLoader;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.Exercise;
import com.fisincorporated.exercisetracker.ui.master.ExerciseMasterFragment;

import java.lang.ref.WeakReference;

public class ExerciseMaintenanceListFragment extends ExerciseMasterFragment implements LoaderManager.LoaderCallbacks<Cursor>, IExerciseCallbacks{

    protected RecyclerView recyclerView;

    protected IExerciseCallbacks callBacks;

    private ExerciseListRecyclerAdapter recyclerAdapter;

    @Override
    public void onExerciseSelected(int exerciseId, int position) {
        callBacks.onExerciseSelected(exerciseId, position);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callBacks = (IExerciseCallbacks) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.exercise_maintenance_list_view, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.exercise_maintenance_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        TextView addNewExervice  = (TextView) view.findViewById(R.id.exercise_maintenance_list_add_activity);
        addNewExervice.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Go to the exercise maintenance activity to add new exercise
				// mod for tablets, return selected (new) exercise to acivity and it will determine
				// if for detail fragment or call activity to display exercise fragment
				 callBacks.onExerciseSelected(-1, 0);
			}
		});

        return view;

    }

    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, null, this);
    }

    private static class ListCursorLoader extends SQLiteCursorLoader {
        ExerciseMaintenanceListFragment el;

        public ListCursorLoader(Context context, ExerciseMaintenanceListFragment el) {
            super(context);
            this.el = el;
        }

        @Override
        protected Cursor loadCursor() {
            return el.showExerciseList();
        }
    }

    private Cursor showExerciseList() {
        Cursor cursor;
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

    // LoaderCallBacks interface methods
    // #1
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // we only ever load the runs, so assume this is the case
        return new ListCursorLoader(getContext(), this);
    }

    // #2
    @SuppressWarnings("deprecation")
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (recyclerAdapter == null) {
            recyclerAdapter = new ExerciseListRecyclerAdapter(getContext(),new WeakReference<IExerciseCallbacks>(this));
            recyclerView.setAdapter(recyclerAdapter);
        }
        recyclerAdapter.swapCursor(cursor);
    }

    // #3
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // stop using the cursor (via the adapter)
        recyclerAdapter.swapCursor(null);
    }

    public void finalize(){
        Log.e(GlobalValues.LOG_TAG,"ExerciseMaintenanceDetailActivity.finalize. Calling super.finalize to close db");
        super.finalize();
    }


}
