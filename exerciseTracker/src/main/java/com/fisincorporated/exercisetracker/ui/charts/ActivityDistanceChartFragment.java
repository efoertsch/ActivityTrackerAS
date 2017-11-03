package com.fisincorporated.exercisetracker.ui.charts;

import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.Exercise;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.LocationExercise;
import com.fisincorporated.exercisetracker.ui.master.ExerciseMasterFragment;
import com.fisincorporated.utility.Chart;
import com.fisincorporated.utility.Utility;

import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ActivityDistanceChartFragment extends ExerciseMasterFragment {
	private LinearLayout chartLayout = null;
	private ArrayList<String> exerciseSelections = new ArrayList<String>();
	private ArrayList<String> locationSelections = new ArrayList<String>();
	private int chartType = GlobalValues.BAR_CHART_LAST_MONTH;
	private Date minDate = new Date();
	private String startTimeStamp;
	// numberOfTimeUnits can be # days, # weeks, # months, # years
	private int numberOfTimeUnits = 0;
	private ArrayList<String> exercises = new ArrayList<String>();
	private Date[] activityDates;
	private String[] xLabels;
	private double[] totalDistanceValues;
	private double maxDistanceInDay = 0;
	private ArrayList<double[]> values = new ArrayList<double[]>();
	private int[] colorList = new int[] {Color.RED,  Color.BLUE , Color.GREEN,
			Color.MAGENTA, Color.YELLOW, Color.CYAN };
	private int[] colors;
	private Chart chart = new Chart();
	private GraphicalView mChartView;
	Calendar calendar = Calendar.getInstance();

	XYMultipleSeriesDataset mDataset;
	XYMultipleSeriesRenderer mRenderer;

	// eventually replace by string resources
	private String chartTitle;
	private String xAxisTitle;
	private String yAxisTitle;

	public ActivityDistanceChartFragment() {
	}

	public static ActivityDistanceChartFragment newInstance(Bundle bundle) {
		Bundle args = new Bundle();
		// is this easier/better than copying values?
		args = (Bundle) bundle.clone();
		ActivityDistanceChartFragment fragment = new ActivityDistanceChartFragment();
		fragment.setArguments(args);
		return fragment;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.chart, container, false);
		getReferencedViews(view);
		return view;
	}

	private void getReferencedViews(View view) {
		chartLayout = (LinearLayout) view.findViewById(R.id.chart);
		((TextView) view.findViewById(R.id.chart_title)).setVisibility(View.GONE);

	}

	public void onResume() {
		super.onResume();
		findDisplayUnits();
		getChartAndFilterArgs();
		createChart();
	}

	private void getChartAndFilterArgs() {
		Bundle args;
		if ((args = getArguments()) != null) {
			if (args.containsKey(GlobalValues.LOCATION_FILTER_PHRASE))
				exerciseSelections = args
						.getStringArrayList(GlobalValues.EXERCISE_FILTER_PHRASE);
			if (args.containsKey(GlobalValues.LOCATION_FILTER_PHRASE))
				locationSelections = args
						.getStringArrayList(GlobalValues.LOCATION_FILTER_PHRASE);
			chartType = args.getInt(GlobalValues.BAR_CHART_TYPE,
					GlobalValues.BAR_CHART_LAST_MONTH);
		}
	}
	
	

	private void createChart() {
		switch (chartType) {
		case GlobalValues.BAR_CHART_LAST_MONTH:
			displayPleaseWait();
			getDistanceInLastMonthChartTitles();
			new CreateChartAsync().execute(chartType);
			break;
		case GlobalValues.BAR_CHART_DISTANCE_WEEKLY:
			displayPleaseWait();
			getWeeklyDistanceChartTitles();
			new CreateChartAsync().execute(chartType);
			break;
		case GlobalValues.BAR_CHART_DISTANCE_MONTHLY:
			displayPleaseWait();
			getMonthlyDistanceChartTitles();
			new CreateChartAsync().execute(chartType);
			break;
		case GlobalValues.BAR_CHART_DISTANCE_YEARLY:
			displayPleaseWait();
			getYearlyDistanceChartTitles();
			new CreateChartAsync().execute(chartType);
			break;
		}
	}
	
	private void displayPleaseWait(){
		Toast.makeText(getActivity(), getResources().getText(R.string.chart_being_created_one_moment_please), Toast.LENGTH_SHORT).show();
	}

	private class CreateChartAsync extends AsyncTask<Integer, Void, Boolean> {
		protected Boolean doInBackground(Integer... params) {
			for (Integer chartType : params) {
				switch (chartType) {
				case GlobalValues.BAR_CHART_LAST_MONTH:
					return getLastMonthBarData();
				//	break;
				case GlobalValues.BAR_CHART_DISTANCE_WEEKLY:
					return getWeeklyBarData();
				//	break;
				case GlobalValues.BAR_CHART_DISTANCE_MONTHLY:
					return getMonthlyBarData();
				//	break;
				case GlobalValues.BAR_CHART_DISTANCE_YEARLY:
					return getYearlyBarData();
				//	break;
				}

			}
			return false;
		}

		

		// remember to include (in this case) Void parm to match the AsyncTask definition
		protected void onPostExecute(Boolean result) {
			if (!result) return;
			loadColors();
			mChartView = createBarChart();
			chartLayout.addView(mChartView, new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		}
	}

	private void getDistanceInLastMonthChartTitles() {
		chartTitle = getResources().getString(R.string.distance_in_last_month);
		xAxisTitle = getResources().getString(R.string.date);
		yAxisTitle = getResources().getString(R.string.distance_in) + " "
				+ milesKm;
	}

	private void getWeeklyDistanceChartTitles() {
		chartTitle = getResources().getString(R.string.distance_per_week);
		xAxisTitle = getResources().getString(R.string.week_ending);
		yAxisTitle = getResources().getString(R.string.distance_in) + " "
				+ milesKm;
	}

	private void getMonthlyDistanceChartTitles() {
		chartTitle = getResources().getString(R.string.distance_per_month);
		xAxisTitle = getResources().getString(R.string.year_month);
		yAxisTitle = getResources().getString(R.string.distance_in) + " "
				+ milesKm;
	}
	
	private void getYearlyDistanceChartTitles() {
		chartTitle = getResources().getString(R.string.distance_per_year); 
		xAxisTitle = getResources().getString(R.string.year);
		yAxisTitle = getResources().getString(R.string.distance_in) + " "
				+ milesKm;
	}

	// show distances daily for last month
	private boolean getLastMonthBarData() {
		// 1. Find min activity based on filter parms
		StringBuffer query = new StringBuffer();
		databaseHelper.getMonthAgoDateSQL(query, exerciseSelections,
				locationSelections);
		if (0 == getMinDateAndNumberTimeUnits(query.toString())){
			return false;
		}
		// numberOfTimeUnits is inclusive of today so adding 1
		++numberOfTimeUnits;
		// 2. Create date array sized on number of days from today - min date
		activityDates = constructConsecutiveDates(minDate, numberOfTimeUnits,
				Calendar.DATE, 1);
		totalDistanceValues = createZeroArrary(numberOfTimeUnits);
		// 3.Get the exercises, days, distances
		query.setLength(0);
		databaseHelper.getDailyActivityDistancesSQL(query, exerciseSelections,
				locationSelections, startTimeStamp);
		getActivityDistances(query.toString());
		// 4. get labels in mm/dd or dd format for x axis
		createDailyXLabels();
		return true;

	}

	// show distances by week
	private boolean getWeeklyBarData() {
		// 1. Find min activity based on filter parms
		StringBuffer query = new StringBuffer();
		databaseHelper.getMinWeekAndNumberOfWeeksSQL(query, exerciseSelections,
				locationSelections);
		if (0 == getMinDateAndNumberTimeUnits(query.toString())){
			return false;
		}
		// check this
		// ++numberOfTimeUnits ;
		// 2. Create date array sized on number of weeks from the min date found
		// set dates to week end date not week beging
		activityDates = constructConsecutiveDates(Utility.addDays(minDate, 6),
				numberOfTimeUnits, Calendar.DATE, 7);
		totalDistanceValues = createZeroArrary(numberOfTimeUnits);
		// 3.Get the exercises, days, distances
		query.setLength(0);
		databaseHelper.getWeeklyActivityDistancesSQL(query, exerciseSelections,
				locationSelections, startTimeStamp);
		getActivityDistances(query.toString());
		// 4. get labels in mm/dd or dd format for x axis
		createWeeklyXLabels();
		return true;
	}

	// show distances by month
	private boolean getMonthlyBarData() {
		// 1. Find min activity based on filter parms
		StringBuffer query = new StringBuffer();
		databaseHelper.getMinDateAndNumMonthsSQL(query, exerciseSelections,
				locationSelections);
		if (0 == getMinDateAndNumberTimeUnits(query.toString())){
			return false;
		}
		// TODO: Need to fix sql as it is calculating number of time units 1 less that what is needed
		// eg. First exercise 10-14-2014, current date 12-13-2015 it returns 14 but it should be 15.
		++numberOfTimeUnits ;
		// 2. Create date array sized on number of months from the min date (month) found
		activityDates = constructConsecutiveDates(minDate, numberOfTimeUnits,
				Calendar.MONTH, 1);
		totalDistanceValues = createZeroArrary(numberOfTimeUnits);
		// 3.Get the exercises, days, distances
		query.setLength(0);
		databaseHelper.getMonthlyActivityDistancesSQL(query, exerciseSelections,
				locationSelections, startTimeStamp);
		getActivityDistances(query.toString());
		// 4. get labels in mm/dd or dd format for x axis
		createMonthlyXLabels();
		return true;
	}
	
	// show distances by year
	private boolean getYearlyBarData() {
		StringBuffer query = new StringBuffer();
		databaseHelper.getMinYearAndNumYearsSQL(query, exerciseSelections,
				locationSelections);
		if (0 == getMinDateAndNumberTimeUnits(query.toString())){
			return false ;
		}
		// 2. Create date array sized on number of years from the min date (year) found
		activityDates = constructConsecutiveDates(minDate, numberOfTimeUnits,
				Calendar.YEAR, 1);
		totalDistanceValues = createZeroArrary(numberOfTimeUnits);
		// 3.Get the exercises, years, distances
		query.setLength(0);
		databaseHelper.getYearlyActivityDistancesSQL(query, exerciseSelections,
				locationSelections, startTimeStamp);
		getActivityDistances(query.toString());
		// 4. get labels in mm/dd or dd format for x axis
		createYearlyXLabels();
		return true;
	}

	private int getMinDateAndNumberTimeUnits(String query) {
		numberOfTimeUnits = 0;
		csrUtility = database.rawQuery(query.toString(), null);
		if (csrUtility.getCount() == 0) {
			displayToastOnUIThread(R.string.no_activities_to_chart);
//			Toast.makeText(getActivity(),
//					getResources().getString(R.string.no_activities_to_chart),
//					Toast.LENGTH_LONG).show();
			getFragmentManager().popBackStack();
			return 0;
		}
		csrUtility.moveToFirst();
		startTimeStamp = csrUtility.getString(0);
		// if not activities within timeframe selected return 0.
		if (startTimeStamp == null) {
			displayToastOnUIThread(R.string.no_activities_found_for_time_period_selected);
//			Toast.makeText(getActivity(),
//					getResources().getString(R.string.no_activities_found_for_time_period_selected),
//					Toast.LENGTH_LONG).show();
			return 0;
		}
		try {
			minDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
					.parse(csrUtility.getString(0));
		} catch (ParseException e) {
			displayToastOnUIThread(R.string.error_getting_activity_date);
//				Toast.makeText(getActivity(),
//					getResources().getString(R.string.error_getting_activity_date),
//					Toast.LENGTH_LONG).show();
			getFragmentManager().popBackStack();
			return 0;
		}
		numberOfTimeUnits = csrUtility.getInt(1);
		csrUtility.close();
		return numberOfTimeUnits;

	}
	
	private void displayToastOnUIThread(final int stringRes){
		getActivity().runOnUiThread(new Runnable(){
         @Override
         public void run(){
         	Toast.makeText(getActivity(),
   					getResources().getString(stringRes),
   					Toast.LENGTH_LONG).show();
         }
		});
	}

	// dates will be used for x axis labels
	private Date[] constructConsecutiveDates(Date minDate,
			int numberOfTimeUnits, int typeIncrement, int unitIncrement) {
		calendar.setTime(minDate);
		Date[] dates = new Date[numberOfTimeUnits];
		dates[0] = calendar.getTime();
		for (int i = 1; i < numberOfTimeUnits; ++i) {
			calendar.add(typeIncrement, unitIncrement);
			dates[i] = calendar.getTime();
		}
		return dates;
	}

	private void getActivityDistances(String query) {
		int timeIndex = 0;
		double[] exerciseValues;
		int origDistance;
		double distance;

		csrUtility = database.rawQuery(query.toString(), null);
		if (csrUtility.getCount() == 0) {
			displayToastOnUIThread(R.string.no_activities_to_chart);
//			Toast.makeText(getActivity(),
//					getResources().getString(R.string.no_activities_to_chart),
//					Toast.LENGTH_LONG).show();
			getFragmentManager().popBackStack();
		}
		csrUtility.moveToFirst();
		while (!csrUtility.isAfterLast()) {
			int exerciseIndex = getExeciseIndex(csrUtility);
			// the number of days/weeks/months/years since the min date
			timeIndex = csrUtility.getInt(csrUtility.getColumnIndex("time_index"));
			// the distances for the specific exercise
			exerciseValues = values.get(exerciseIndex);

			// going from int to double, and distances in km or miles
			// round to 1 decimal place
			origDistance = csrUtility.getInt(csrUtility
					.getColumnIndex(LocationExercise.DISTANCE));
			if (imperialMetric.equals(imperial)) {
				distance = Utility.metersToMiles((float) origDistance);
			} else {
				distance = origDistance / 1000d;
			}
			distance = (double) Math.round(distance * 10) / 10;

			exerciseValues[timeIndex] = distance;

			totalDistanceValues[timeIndex] += distance;
			if (maxDistanceInDay < totalDistanceValues[timeIndex]) {
				maxDistanceInDay = totalDistanceValues[timeIndex];
			}
			csrUtility.moveToNext();
		}
		csrUtility.close();

	}

	/**
	 * Add new exercise and value array if needed
	 * 
	 * @param csrUtility
	 * @return the index of the exercise
	 */

	private int getExeciseIndex(Cursor csrUtility) {
		int columnIndex = csrUtility.getColumnIndex(Exercise.EXERCISE);
		int exerciseIndex = exercises.indexOf(csrUtility.getString(columnIndex));
		if (exerciseIndex == -1) {
			exercises.add(csrUtility.getString(columnIndex));
			exerciseIndex = exercises.size() - 1;
			values.add(createZeroArrary(numberOfTimeUnits));
		}
		return exerciseIndex;
	}

	private double[] createZeroArrary(int size) {
		double[] zeroArray = new double[size];
		for (int i = 0; i < size; ++i) {
			zeroArray[i] = 0;
		}
		return zeroArray;
	}

	private void loadColors() {
		colors = new int[values.size()];
		for (int i = 0; i < values.size() && i < colorList.length; ++i) {
			colors[i] = colorList[i];
		}
	}

	private void createDailyXLabels() {
		xLabels = new String[activityDates.length];
		// int currentMonth = 0 ;
		int x = 7;
		for (int i = activityDates.length - 1; i >= 0; --i) {
			calendar.setTime(activityDates[i]);
			// put in mm/dd on first and last days and every 7 days from last day
			if (x == 7 || i == 0) {
				xLabels[i] = (calendar.get(Calendar.MONTH) + 1) + "/"
						+ calendar.get(Calendar.DAY_OF_MONTH);
			} else {
				xLabels[i] = calendar.get(Calendar.DAY_OF_MONTH) + "";
			}
			--x;
			if (x == 0)
				x = 7;
		}
	}

	private void createWeeklyXLabels() {
		xLabels = new String[activityDates.length];
		for (int i = 0; i < activityDates.length; ++i) {
			calendar.setTime(activityDates[i]);
			xLabels[i] = (calendar.get(Calendar.MONTH) + 1) + "/"
					+ calendar.get(Calendar.DAY_OF_MONTH);
		}
	}

	private void createMonthlyXLabels() {
		xLabels = new String[activityDates.length];
		for (int i = 0; i < activityDates.length; ++i) {
			calendar.setTime(activityDates[i]);
			xLabels[i] = calendar.get(Calendar.YEAR) + "/"
					+ (calendar.get(Calendar.MONTH) + 1);
		}
	}
	
	private void createYearlyXLabels() {
		xLabels = new String[activityDates.length];
		for (int i = 0; i < activityDates.length; ++i) {
			calendar.setTime(activityDates[i]);
			xLabels[i] = calendar.get(Calendar.YEAR) + "" ;
		}
	}

	private GraphicalView createBarChart() {
		return chart.createBarChart(getActivity(), chartTitle, xAxisTitle,
				yAxisTitle, exercises.toArray(new String[] { "" }), 0d,
				(double) numberOfTimeUnits - 1, 0d, maxDistanceInDay, values,
				colors, xLabels, Type.DEFAULT);

	}

}
