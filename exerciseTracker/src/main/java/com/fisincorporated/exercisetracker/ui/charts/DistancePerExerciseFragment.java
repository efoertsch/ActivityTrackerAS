package com.fisincorporated.exercisetracker.ui.charts;

import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.Exercise;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.LocationExercise;
import com.fisincorporated.exercisetracker.database.TrackerDatabaseHelper;
import com.fisincorporated.exercisetracker.ui.master.ExerciseDaggerFragment;
import com.fisincorporated.exercisetracker.ui.utils.DisplayUnits;
import com.fisincorporated.exercisetracker.utility.StatsUtil;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

public class DistancePerExerciseFragment extends ExerciseDaggerFragment {
    private ArrayList<String> exerciseSelections = new ArrayList<>();
    private ArrayList<String> locationSelections = new ArrayList<>();
    private int chartType = GlobalValues.BAR_CHART_LAST_MONTH;
    private Date minDate = new Date();
    private String startTimeStamp;
    // numberOfTimeUnits can be # days, # weeks, # months, # years
    private int numberOfTimeUnits = 0;
    private ArrayList<String> exercises = new ArrayList<>();
    private Date[] activityDates;
    //labels for xAxis;
    private String[] xLabels;
    private double[] totalDistanceValues;
    private double maxDistanceInDay = 0;
    private ArrayList<double[]> values = new ArrayList<>();
    // Color.Green bad contrast on white background
    private int[] colorList = new int[]{Color.RED, Color.BLUE, Color.parseColor("#ff4CAF50"),
            Color.MAGENTA, Color.YELLOW, Color.CYAN};
    private int[] exerciseGraphColors;

    private Calendar calendar = Calendar.getInstance();

    private String chartTitle;

    private float dataMaxY;

    private LineChart graphView;
    private TextView graphTitle;
    private Resources resources;
    private View graphLayoutView;
    private View progressBar;

    @Inject
    DisplayUnits displayUnits;

    @Inject
    StatsUtil statsUtil;

    public static DistancePerExerciseFragment newInstance(Bundle bundle) {
        // is this easier/better than copying values?
        Bundle args = (Bundle) bundle.clone();
        DistancePerExerciseFragment fragment = new DistancePerExerciseFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chart_with_title, container, false);
        getReferencedViews(view);
        resources = getResources();
        return view;
    }

    private void getReferencedViews(View view) {
        graphLayoutView = view.findViewById(R.id.chart_graph_layout);
        progressBar = view.findViewById(R.id.chart_progress_bar);
        graphView = (LineChart) view.findViewById(R.id.chart_graph);
        graphTitle = (TextView) view.findViewById(R.id.chart_title);
    }

    public void onResume() {
        super.onResume();
        showGraph(false);
        getChartAndFilterArgs();
        createGraph();
    }

    private void showGraph(boolean showGraph) {
        progressBar.setVisibility(showGraph ? View.GONE : View.VISIBLE);
        graphLayoutView.setVisibility((showGraph ? View.VISIBLE : View.GONE));
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

    private void createGraph() {
        displayPleaseWait();
        new CreateChartAsync().execute(chartType);
    }

    private void displayPleaseWait() {
        Toast.makeText(getActivity(), getResources().getText(R.string.chart_being_created_one_moment_please), Toast.LENGTH_SHORT).show();
    }

    private class CreateChartAsync extends AsyncTask<Integer, Void, Boolean> {
        // there should be only one parm.
        protected Boolean doInBackground(Integer... params) {
            boolean isSuccessful = false;
            for (Integer chartType : params) {
                switch (chartType) {
                    case GlobalValues.BAR_CHART_LAST_MONTH:
                        isSuccessful = getLastMonthBarData();
                     	break;
                    case GlobalValues.BAR_CHART_DISTANCE_WEEKLY:
                        isSuccessful = getWeeklyBarData();
                     	break;
                    case GlobalValues.BAR_CHART_DISTANCE_MONTHLY:
                        isSuccessful = getMonthlyBarData();
                     	break;
                    case GlobalValues.BAR_CHART_DISTANCE_YEARLY:
                        isSuccessful = getYearlyBarData();
                     	break;
                }
            }

            if (isSuccessful) {
                loadColors();
                determineChartTitle();
            }
            return isSuccessful;
        }

        // remember to include (in this case) Void parm to match the AsyncTask definition
        protected void onPostExecute(Boolean result) {
            if (!result) return;
            showGraph(true);
            composeGraph();
        }
    }

    private void determineChartTitle() {
        switch (chartType) {
            case GlobalValues.BAR_CHART_LAST_MONTH:
                getChartTitle(R.string.chart_distance_in_last_month);
                break;
            case GlobalValues.BAR_CHART_DISTANCE_WEEKLY:
                getChartTitle(R.string.chart_distance_per_week);
                break;
            case GlobalValues.BAR_CHART_DISTANCE_MONTHLY:
                getChartTitle(R.string.chart_distance_per_month);
                break;
            case GlobalValues.BAR_CHART_DISTANCE_YEARLY:
                getChartTitle(R.string.chart_distance_per_year);
                break;
        }
    }

    private void getChartTitle(@StringRes int stringRes) {
        chartTitle = resources.getString(stringRes, displayUnits.isImperialDisplay() ? resources.getString(R.string.chart_miles) : resources.getString(R.string.chart_kilometers));
    }


    // show distances daily for last month
    private boolean getLastMonthBarData() {
        // 1. Find min activity based on filter parms
        StringBuffer query = new StringBuffer();
        TrackerDatabaseHelper.getMonthAgoDateSQL(query, exerciseSelections,
                locationSelections);
        if (0 == getMinDateAndNumberTimeUnits(query.toString())) {
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
        TrackerDatabaseHelper.getDailyActivityDistancesSQL(query, exerciseSelections,
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
        TrackerDatabaseHelper.getMinWeekAndNumberOfWeeksSQL(query, exerciseSelections,
                locationSelections);
        if (0 == getMinDateAndNumberTimeUnits(query.toString())) {
            return false;
        }
        // check this
        // ++numberOfTimeUnits ;
        // 2. Create date array sized on number of weeks from the min date found
        // set dates to week end date not week beging
        activityDates = constructConsecutiveDates(statsUtil.addDays(minDate, 6),
                numberOfTimeUnits, Calendar.DATE, 7);
        totalDistanceValues = createZeroArrary(numberOfTimeUnits);
        // 3.Get the exercises, days, distances
        query.setLength(0);
        TrackerDatabaseHelper.getWeeklyActivityDistancesSQL(query, exerciseSelections,
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
        TrackerDatabaseHelper.getMinDateAndNumMonthsSQL(query, exerciseSelections,
                locationSelections);
        if (0 == getMinDateAndNumberTimeUnits(query.toString())) {
            return false;
        }
        // TODO: Need to fix sql as it is calculating number of time units 1 less that what is needed
        // eg. First exercise 10-14-2014, current date 12-13-2015 it returns 14 but it should be 15.
        ++numberOfTimeUnits;
        // 2. Create date array sized on number of months from the min date (month) found
        activityDates = constructConsecutiveDates(minDate, numberOfTimeUnits,
                Calendar.MONTH, 1);
        totalDistanceValues = createZeroArrary(numberOfTimeUnits);
        // 3.Get the exercises, days, distances
        query.setLength(0);
        TrackerDatabaseHelper.getMonthlyActivityDistancesSQL(query, exerciseSelections,
                locationSelections, startTimeStamp);
        getActivityDistances(query.toString());
        // 4. get labels in mm/dd or dd format for x axis
        createMonthlyXLabels();
        return true;
    }

    // show distances by year
    private boolean getYearlyBarData() {
        StringBuffer query = new StringBuffer();
        TrackerDatabaseHelper.getMinYearAndNumYearsSQL(query, exerciseSelections,
                locationSelections);
        if (0 == getMinDateAndNumberTimeUnits(query.toString())) {
            return false;
        }
        // 2. Create date array sized on number of years from the min date (year) found
        activityDates = constructConsecutiveDates(minDate, numberOfTimeUnits,
                Calendar.YEAR, 1);
        totalDistanceValues = createZeroArrary(numberOfTimeUnits);
        // 3.Get the exercises, years, distances
        query.setLength(0);
        TrackerDatabaseHelper.getYearlyActivityDistancesSQL(query, exerciseSelections,
                locationSelections, startTimeStamp);
        getActivityDistances(query.toString());
        // 4. get labels in mm/dd or dd format for x axis
        createYearlyXLabels();
        return true;
    }

    private int getMinDateAndNumberTimeUnits(String query) {
        numberOfTimeUnits = 0;
        csrUtility = database.rawQuery(query, null);
        if (csrUtility.getCount() == 0) {
            displayToastOnUIThread(R.string.no_activities_to_chart);
            getFragmentManager().popBackStack();
            return 0;
        }
        csrUtility.moveToFirst();
        startTimeStamp = csrUtility.getString(0);
        // if not activities within timeframe selected return 0.
        if (startTimeStamp == null) {
            displayToastOnUIThread(R.string.no_activities_found_for_time_period_selected);
            return 0;
        }
        try {
            minDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .parse(csrUtility.getString(0));
        } catch (ParseException e) {
            displayToastOnUIThread(R.string.error_getting_activity_date);
            getFragmentManager().popBackStack();
            return 0;
        }
        numberOfTimeUnits = csrUtility.getInt(1);
        csrUtility.close();
        return numberOfTimeUnits;

    }

    private void displayToastOnUIThread(final int stringRes) {
        getActivity().runOnUiThread(() -> Toast.makeText(getActivity(),
                getResources().getString(stringRes),
                Toast.LENGTH_LONG).show());
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

        dataMaxY = 0;

        csrUtility = database.rawQuery(query.toString(), null);
        if (csrUtility.getCount() == 0) {
            displayToastOnUIThread(R.string.no_activities_to_chart);
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
            if (displayUnits.isImperialDisplay()) {
                distance = statsUtil.metersToMiles((float) origDistance);
            } else {
                distance = origDistance / 1000d;
            }
            distance = (double) Math.round(distance * 10) / 10;

            if (distance > dataMaxY) {
                dataMaxY = (float) distance;
            }

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
        exerciseGraphColors = new int[values.size()];
        for (int i = 0; i < values.size() && i < colorList.length; ++i) {
            exerciseGraphColors[i] = colorList[i];
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
            xLabels[i] = calendar.get(Calendar.YEAR) + "";
        }
    }


    private void composeGraph() {
        graphView.clear();
        // Create graphing datasets
        LineData lineData = new LineData();
        for (int i = 0; i < exercises.size(); ++i) {
            List<Entry> entries = new ArrayList<>();
            for (int j = 0; j < activityDates.length; ++j) {
                entries.add(new Entry((float) j, (float) values.get(i)[j]));
            }

            LineDataSet dataSet = new LineDataSet(entries, exercises.get(i));
            dataSet.setValueTextSize(12f);
            dataSet.setLabel(exercises.get(i));
            dataSet.setColor(exerciseGraphColors[i]);
            dataSet.setValueTextColor(exerciseGraphColors[i]);
            lineData.addDataSet(dataSet);
        }

        lineData.setDrawValues(true);
        graphView.setData(lineData);

        // Set X axis (time) formatting info
        XAxis xAxis = graphView.getXAxis();
        xAxis.setGranularity(1f);
        //xAxis.setCenterAxisLabels(true);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                // show years, months or days
                // hack - not sure why this is just yet
                // if only 1 time period may get value of -1
                if (value < 0 || value > xLabels.length -1) {
                    return "";
                }
                return xLabels[(int)value];
            }
        });
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        // Set Y axis (mileage) formatting
        YAxis leftAxis = graphView.getAxisLeft();
        leftAxis.setValueFormatter(new LargeValueFormatter());
        leftAxis.setDrawGridLines(true);
        leftAxis.setSpaceTop(35f);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        leftAxis.setAxisMaximum(statsUtil.calcMaxYGraphValue(dataMaxY));
        graphView.getAxisRight().setEnabled(false);  // TODO revisit later

        // Set graph title - note separate TextView that is not part of graph
        graphTitle.setText(chartTitle);
        graphView.getDescription().setText("");

        graphView.invalidate();
    }
}
