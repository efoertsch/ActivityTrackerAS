package com.fisincorporated.exercisetracker.ui.history;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.fisincorporated.exercisetracker.ActivityChart;
import com.fisincorporated.exercisetracker.ActivityDistanceChartFragment;
import com.fisincorporated.exercisetracker.ActivityListFragment;
import com.fisincorporated.exercisetracker.DeletePriorActivitiesFragment;
import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.ui.master.ExerciseMasterFragmentActivity;

public class ActivityList extends ExerciseMasterFragmentActivity {
	ActivityListFragment alf = null;
	Menu myMenu = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActivityTitle(R.string.activity_history);
	}
	@Override
	protected Fragment createFragment() {
		// backstack listener from
		// http://stackoverflow.com/questions/6503189/fragments-onresume-from-back-stack
		getSupportFragmentManager().addOnBackStackChangedListener(getListener());
		return alf = new ActivityHistoryListFragment();
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
		case R.id.activities_list_options_delete:
			switchToDeleteFragment();
			return true;
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
		alf = (ActivityListFragment) fm.findFragmentById(R.id.fragmentContainer);
	}

	public void switchToDeleteFragment() {
		FragmentManager fm = getSupportFragmentManager();
		Bundle sortFilter = ((ActivityListFragment) fm
				.findFragmentById(R.id.fragmentContainer)).getSortFilterValues();
		FragmentTransaction ft = fm.beginTransaction();
		alf = new DeletePriorActivitiesFragment();
		alf.setArguments(sortFilter);
		ft.replace(R.id.fragmentContainer, alf, "DeleteActivities");
		ft.addToBackStack(null);
		ft.commit();

	}

	public void onBackPressed() {
		checkToEnableDelete();
		super.onBackPressed();
	}

	// Note: this is not called when on prioractivities list and back pressed to go back to main menu
	private OnBackStackChangedListener getListener() {
		OnBackStackChangedListener result = new OnBackStackChangedListener() {
			public void onBackStackChanged() {
				FragmentManager manager = getSupportFragmentManager();
				if (manager != null) {
					checkToEnableDelete();
				}
			}
		};
		return result;
	}

	public void checkToEnableDelete() {
		FragmentManager fm = getSupportFragmentManager();
		if (myMenu == null) {
			return;
		}
		if (null == fm.findFragmentByTag("DeleteActivities"))
			myMenu.findItem(R.id.activities_list_options_delete).setEnabled(true)
					.setVisible(true);
		else
			myMenu.findItem(R.id.activities_list_options_delete).setEnabled(false)
					.setVisible(false);
	}

	public void showChart(int chartType) {
		Bundle bundle = alf.getSortFilterValues();
		bundle.putInt(GlobalValues.BAR_CHART_TYPE, chartType);
		Fragment newDetail = null;
		if (findViewById(R.id.detailFragmentContainer) == null) {
			Intent intent = new Intent(this, ActivityChart.class);
			xferBundleToIntent(intent, bundle);
			startActivity(intent);
		}

		else {
			// display the activity info in the detailfragment container
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			Fragment oldDetail = fm.findFragmentById(R.id.detailFragmentContainer);
			newDetail = ActivityDistanceChartFragment.newInstance(bundle);

			if (oldDetail != null) {
				ft.remove(oldDetail);
			}
			if (newDetail != null) {
				ft.add(R.id.detailFragmentContainer, newDetail);
			}
			ft.commit();
		}
	}
}
