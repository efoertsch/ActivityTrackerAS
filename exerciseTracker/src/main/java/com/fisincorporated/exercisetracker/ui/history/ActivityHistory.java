package com.fisincorporated.exercisetracker.ui.history;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.ui.charts.DistancePerExerciseFragment;
import com.fisincorporated.exercisetracker.ui.charts.GraphActivity;
import com.fisincorporated.exercisetracker.ui.master.ExerciseMasterFragmentActivity;
import com.fisincorporated.exercisetracker.ui.master.IChangeToolbar;

public class ActivityHistory extends ExerciseMasterFragmentActivity implements IChangeToolbar {
    ActivityFragmentHistory alf = null;
    Menu myMenu = null;
    Drawable actionBarDrawable = null;
    String originalTitle = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityTitle(R.string.activity_history);
        originalTitle = getString(R.string.activity_history);
    }

    @Override
    protected Fragment createFragment() {
        return alf = new ActivityFragmentHistory();
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
        Fragment newDetail = null;
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

    @Override
    public void setToolbarColor(int color) {
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(color));
        }

    }

    @Override
    public void resetToolbarColorToDefault() {
        setActionBarBackgroundColorToDefault();

    }

    @Override
    public void setToolbarTitle(String title) {
        setActivityTitle(title);
    }

    @Override
    public void resetToolbarTitleToDefault() {
        setActivityTitle(originalTitle);

    }
}
