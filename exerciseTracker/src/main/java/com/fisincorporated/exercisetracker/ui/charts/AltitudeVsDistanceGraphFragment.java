package com.fisincorporated.exercisetracker.ui.charts;

import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.database.TrackerDatabase;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.GPSLog;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.LocationExercise;
import com.fisincorporated.exercisetracker.ui.master.ExerciseMasterFragment;
import com.fisincorporated.exercisetracker.ui.utils.DisplayUnits;
import com.fisincorporated.exercisetracker.utility.Utility;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.LargeValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class AltitudeVsDistanceGraphFragment extends ExerciseMasterFragment {
    private long locationExerciseId;
    private String title;
    private String description;

    private String chartTitle;
    private LineChart graphView;
    private TextView graphTitle;
    private Resources resources;
    private View graphLayoutView;
    private View progressBar;
    private LineData lineData;

    public static AltitudeVsDistanceGraphFragment newInstance(Bundle bundle) {
        AltitudeVsDistanceGraphFragment fragment = new AltitudeVsDistanceGraphFragment();
        fragment.setArguments(bundle);
        return fragment;
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chart_with_title, container, false);
        getReferencedViews(view);
        resources = getResources();

        if (getArguments() != null) {
            locationExerciseId = getArguments().getLong(LocationExercise._ID);
            title = getArguments().getString(GlobalValues.TITLE);
            description = getArguments().getString(LocationExercise.DESCRIPTION);
        }
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
        setUpChart();
        getAltVsDistSeries();
    }

    private void showGraph(boolean showGraph) {
        progressBar.setVisibility(showGraph ? View.GONE : View.VISIBLE);
        graphLayoutView.setVisibility((showGraph ? View.VISIBLE : View.GONE));
    }


    private void setUpChart() {
        graphView.clear();
        chartTitle = resources.getString(R.string.chart_title_elevation_vs_distance, DisplayUnits.getFeetMeters()
                , DisplayUnits.getMilesKm(), title, description);
    }

    public void getAltVsDistSeries() {
        // TODO convert to CursorLoader (and remove csr.close() below)
        Cursor csr;

        lineData = new LineData();
        List<Entry> entries = new ArrayList<>();

        float elevation;
        float startElecation = -1000;
        float priorElevation = startElecation;
        float avgElevation = 0;
        float totalDistance = 0;

        csr = database.query(GPSLog.GPSLOG_TABLE, new String[]{
                        GPSLog.DISTANCE_FROM_LAST_POINT, GPSLog.ELEVATION},
                GPSLog.LOCATION_EXERCISE_ID + " = ?",
                new String[]{locationExerciseId + ""}, null, null,
                TrackerDatabase.GPSLog.TIMESTAMP);

        if (csr.getCount() == 0 || csr.getCount() == 1) {
            Toast.makeText(getActivity(),
                    "There is no GPS log data available to plot", Toast.LENGTH_LONG)
                    .show();
            return;
        } else {
            csr.moveToFirst();
            while (!csr.isAfterLast()) {
                totalDistance += (double) (DisplayUnits.isImperialDisplay() ? Utility
                        .metersToMiles((float) csr.getDouble(0)) : ((float) csr
                        .getDouble(0)) / 1000);
                elevation =  (DisplayUnits.isImperialDisplay() ? Utility
                        .metersToFeet((float) csr.getDouble(1)) : ((float) csr
                        .getDouble(1)) / 1000);
                // elevation can be very noisy so just trying to smooth it out
                if (priorElevation != startElecation) {
                    avgElevation = (priorElevation + elevation) / 2f;
                    priorElevation = elevation;
                } else {
                    avgElevation = elevation;
                    priorElevation = elevation;
                }
                entries.add(new Entry((float) totalDistance, avgElevation));
                csr.moveToNext();
            }
        }
        csr.close();

        LineDataSet dataSet = new LineDataSet(entries, "altitude");
        dataSet.setValueTextSize(12f);
        dataSet.setColor(Color.BLACK);
        dataSet.setValueTextColor(Color.BLACK);
        lineData.addDataSet(dataSet);

        lineData.setDrawValues(true);
        graphView.setData(lineData);

        // Set X axis (time) formatting info
        XAxis xAxis = graphView.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        // Set Y axis (mileage) formatting
        YAxis leftAxis = graphView.getAxisLeft();
        leftAxis.setValueFormatter(new LargeValueFormatter());
        leftAxis.setDrawGridLines(true);
        leftAxis.setSpaceTop(35f);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        graphView.getAxisRight().setEnabled(false);  // TODO revisit later

        graphView.invalidate();

        graphTitle.setText(chartTitle);
        graphView.getDescription().setText("");
        showGraph(true);

    }
}
