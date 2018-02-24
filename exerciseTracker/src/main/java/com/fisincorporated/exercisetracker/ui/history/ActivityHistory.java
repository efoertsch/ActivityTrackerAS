package com.fisincorporated.exercisetracker.ui.history;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.ui.charts.DistancePerExerciseFragment;
import com.fisincorporated.exercisetracker.ui.charts.GraphActivity;
import com.fisincorporated.exercisetracker.ui.master.ChangeToolbarEvent;
import com.fisincorporated.exercisetracker.ui.master.ExerciseMasterActivity;
import com.jakewharton.rxrelay2.PublishRelay;

import javax.inject.Inject;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class ActivityHistory extends ExerciseMasterActivity {

    private static final String TAG = ActivityHistory.class.getSimpleName();

    @Inject
    PublishRelay<Object> publishRelay;
    private Disposable publishRelayDisposable;

    ActivityFragmentHistory alf = null;
    Menu myMenu = null;
    String originalTitle = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityTitle(R.string.activity_history);
        originalTitle = getString(R.string.activity_history);
    }

    @Override
    protected Fragment createFragment() {
        alf = new ActivityFragmentHistory();
        return alf;
    }

    @Override
    public void onResume() {
        super.onResume();
        alf.setHandleSelectedActionImpl(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        alf.setHandleSelectedActionImpl(null);

    }

    @Override
    public void onStart() {
        super.onStart();
        if (publishRelay != null) {
            publishRelay.subscribe(publishRelayObserver);
        }
    }

    @Override
    public void onStop() {
        if (publishRelayDisposable != null) {
            publishRelayDisposable.dispose();
        }
        publishRelayDisposable = null;
        super.onStop();
    }

    // added for tablet
    @Override
    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activities_list_options, menu);
        myMenu = menu;
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        getCurrentFragment();
        switch (item.getItemId()) {
            case R.id.activities_list_filter_by_exercise:
                alf.showExerciseFilter();
                return true;
            case R.id.activities_list_filter_by_location:
                alf.showLocationFilter();
                return true;
            case R.id.activities_list_filter_clear_all:
                alf.clearFilters();
                return true;
            case R.id.activities_list_options_sort:
                return false;
            case R.id.activities_list_filter:
                return false;
            case R.id.activities_list_distance_in_last_month:
                showChart(GlobalValues.BAR_CHART_LAST_MONTH);
                return true;
            case R.id.activities_list_distance_by_week:
                showChart(GlobalValues.BAR_CHART_DISTANCE_WEEKLY);
                return true;
            case R.id.activities_list_distance_by_month:
                showChart(GlobalValues.BAR_CHART_DISTANCE_MONTHLY);
                return true;
            case R.id.activities_list_distance_by_year:
                showChart(GlobalValues.BAR_CHART_DISTANCE_YEARLY);
                return true;
            default:
                return alf.setSortOrder(item);
        }
    }

    private void getCurrentFragment() {
        FragmentManager fm = getSupportFragmentManager();
        alf = (ActivityFragmentHistory) fm.findFragmentById(R.id.fragmentContainer);
    }


    public void showChart(int chartType) {
        Bundle bundle = alf.getSortFilterValues();
        bundle.putInt(GlobalValues.BAR_CHART_TYPE, chartType);
        Fragment newDetail;
        if (findViewById(R.id.detailFragmentContainer) == null) {
            Intent intent = new Intent(this, GraphActivity.class);
            xferBundleToIntent(intent, bundle);
            startActivity(intent);
        } else {
            // display the activity info in the detailfragment container
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            Fragment oldDetail = fm.findFragmentById(R.id.detailFragmentContainer);
            newDetail = DistancePerExerciseFragment.newInstance(bundle);

            if (oldDetail != null) {
                ft.remove(oldDetail);
            }
            if (newDetail != null) {
                ft.add(R.id.detailFragmentContainer, newDetail);
            }
            ft.commit();
        }
    }

    private Observer<Object> publishRelayObserver = new Observer<Object>() {
        @Override
        public void onSubscribe(Disposable disposable) {
            publishRelayDisposable = disposable;
        }

        @Override
        public void onNext(Object o) {
            if (o instanceof ChangeToolbarEvent) {
                ChangeToolbarEvent changeToolbarEvent = (ChangeToolbarEvent) o;
                switch (changeToolbarEvent.getEvent()) {
                    case SET_TOOLBAR_COLOR:
                        if (actionBar != null) {
                            actionBar.setBackgroundDrawable(new ColorDrawable(changeToolbarEvent.getColor()));
                        }
                        break;
                    case RESET_TOOLBAR_COLOR_TO_DEFAULT:
                        setActionBarBackgroundColorToDefault();
                        break;
                    case SET_TOOLBAR_TITLE:
                        if (actionBar != null) {
                            setActivityTitle(changeToolbarEvent.getTitle());
                        }
                        break;
                    case RESET_TOOLBAR_TITLE_TO_DEFAULT:
                        setActivityTitle(originalTitle);
                        break;

                }
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

}
