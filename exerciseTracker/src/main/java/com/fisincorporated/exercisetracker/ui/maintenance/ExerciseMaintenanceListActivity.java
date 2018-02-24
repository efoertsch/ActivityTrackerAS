package com.fisincorporated.exercisetracker.ui.maintenance;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.database.ExerciseDAO;
import com.fisincorporated.exercisetracker.database.ExerciseRecord;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.Exercise;
import com.fisincorporated.exercisetracker.database.TrackerDatabaseHelper;
import com.fisincorporated.exercisetracker.ui.master.ExerciseDaggerActivity;
import com.jakewharton.rxrelay2.PublishRelay;

import javax.inject.Inject;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class ExerciseMaintenanceListActivity extends ExerciseDaggerActivity  {

    private static final String TAG = ExerciseMaintenanceListActivity.class.getSimpleName();

    private Disposable publishRelayDisposable;

    @Inject
    PublishRelay<Object> publishRelay;

    @Inject
    TrackerDatabaseHelper trackerDatabaseHelper;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityTitle(R.string.exercise_list);
    }

    private Observer<Object> publishRelayObserver = new Observer<Object>() {
        @Override
        public void onSubscribe(Disposable disposable) {
            publishRelayDisposable = disposable;
        }

        @Override
        public void onNext(Object o) {
            if (o instanceof ExerciseSelectedEvent) {
                ExerciseSelectedEvent exerciseSelectedEvent = (ExerciseSelectedEvent) o;
                onExerciseSelected(exerciseSelectedEvent.getExerciseId(), exerciseSelectedEvent.getPosition());

            }
        }
        @Override
        public void onError(Throwable e) {
            // Big Trouble - PublishRelay should never throw
            Log.e(TAG, "PublishRelay throwing error:" + e.toString());
            // TODO Do something more
        }

        @Override
        public void onComplete() {
            // Big Trouble - PublishRelay should never call
            Log.e(TAG, "PublishRelay onComplete Thrown");
            // TODO Do something more
        }
    };


    @Override
    public void onStart() {
        super.onStart();
        publishRelay.subscribe(publishRelayObserver);
    }

    @Override
    public void onStop() {
        if (publishRelayDisposable != null) {
            publishRelayDisposable.dispose();
        }
        publishRelayDisposable = null;
        super.onStop();
    }

    @Override
    protected Fragment createFragment() {
        return new ExerciseMaintenanceListFragment();
    }

    // for tablet implementation
    public void onExerciseSelected(int exerciseId, int position) {
        Fragment newDetail;
        if (findViewById(R.id.detailFragmentContainer) == null) {
            // start info from bundle to load to intent and start instance of
            // ExerciseMaintenancePagerActivity
            if (exerciseId == -1) {
                // this is for an ADD of new exercise
                Intent intent = new Intent(this, ExerciseMaintenanceDetailActivity.class);
                ExerciseRecord exerciseRecord = new ExerciseRecord();
                intent.putExtra(Exercise.EXERCISE_TABLE, exerciseRecord);
                startActivity(intent);
            } else {
                // this is for update/delete of existing exercise, use view pager
                Intent intent = new Intent(this, ExerciseMaintenancePagerActivity.class);
                intent.putExtra(GlobalValues.CURSOR_POSITION, position);
                startActivity(intent);
            }
        } else {
            // tablet version
            // display the activity info in the detailfragment container
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            Fragment oldDetail = fm.findFragmentById(R.id.detailFragmentContainer);
            Bundle args = new Bundle();
            args.putParcelable(Exercise.EXERCISE_TABLE,
                    trackerDatabaseHelper.getExerciseDAO().loadExerciseRecordById(exerciseId));
            args.putInt(GlobalValues.CURSOR_POSITION, position);
            newDetail = ExerciseMaintenanceDetailFragment.newInstance(args);
            if (oldDetail != null) {
                ft.remove(oldDetail);
            }
            ft.add(R.id.detailFragmentContainer, newDetail);
            ft.addToBackStack(null);
            ft.commit();
        }
    }

}
