package com.fisincorporated.ExerciseTracker;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fisincorporated.database.TrackerDatabase.GPSLog;
import com.fisincorporated.database.TrackerDatabase.LocationExercise;
import com.fisincorporated.utility.Utility;

// Code from AChartEngine demo, 1st attempt at chart
// clean this up 
public class ElevationVsDistanceFragment extends ExerciseMasterFragment {
	private long locationExerciseId;
	private LinearLayout chartLayout = null;
	private String title;
	private String description;

	// for plotting elevation vs distance
	/** The main dataset that includes all the series that go into a chart. */
	private XYMultipleSeriesDataset mDataset;
	/** The main renderer that includes all the renderers customizing a chart. */
	private XYMultipleSeriesRenderer mRenderer;
	/** The most recently added series. */
	private XYSeries xySeries;
	/** The most recently created renderer, customizing the current series. */
	private XYSeriesRenderer renderer;
	/** The chart view that displays the data. */
	private GraphicalView mChartView;

	public static ElevationVsDistanceFragment newInstance(Bundle bundle) {
//		Bundle args = new Bundle();
//		args.putLong(LocationExercise._ID,
//				bundle.getLong(LocationExercise._ID, -1));
//		args.putString(GlobalValues.TITLE, bundle.getString(GlobalValues.TITLE));
		ElevationVsDistanceFragment fragment = new ElevationVsDistanceFragment();
//		fragment.setArguments(args);
		fragment.setArguments(bundle);
		return fragment;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.chart, container, false);
		if (getArguments() != null) {
			locationExerciseId = getArguments().getLong(LocationExercise._ID);
			title = getArguments().getString(GlobalValues.TITLE);
			description = getArguments().getString(LocationExercise.DESCRIPTION);
		}
		getReferencedViews(view);
		return view;
	}

	public void onResume() {
		super.onResume();
		findDisplayUnits();
		setUpChart();
		getAltVsDistSeries();
	}

	private void getReferencedViews(View view) {
		chartLayout = (LinearLayout) view.findViewById(R.id.chart);
		((TextView) view.findViewById(R.id.chart_title)).setText(title + "  " + description);

	}

	private void setUpChart() {
		
		// set some properties on the main renderer
		mRenderer = new XYMultipleSeriesRenderer();
		//mRenderer.setChartTitle(title + "  " + description);
		mRenderer.setXTitle(getResources().getString(R.string.distance) +  "(" + feetMeters + ")" );
		mRenderer.setYTitle(getResources().getString(R.string.elevation) + "(" + milesKm + ")");
		mRenderer.setXAxisMin(0);
		mRenderer.setAxesColor(Color.LTGRAY);
		mRenderer.setLabelsColor(Color.LTGRAY);

		mRenderer.setApplyBackgroundColor(true);
		mRenderer.setBackgroundColor(Color.argb(100, 50, 50, 50));
		mRenderer.setAxisTitleTextSize(16);
		mRenderer.setChartTitleTextSize(20);
		mRenderer.setLabelsTextSize(15);
		mRenderer.setLegendTextSize(15);
		mRenderer.setMargins(new int[] { 20, 30, 15, 0 });
		mRenderer.setZoomButtonsVisible(true);
		mRenderer.setPointSize(5);
		mRenderer.setInScroll(true);
		mRenderer.setPanEnabled(true, true);
		// mRenderer.setClickEnabled(false);
		// create and set some properties for the xyrenderer
		renderer = new XYSeriesRenderer();
		mRenderer.addSeriesRenderer(renderer);
		// set some renderer properties

		renderer.setPointStyle(PointStyle.CIRCLE);
		renderer.setFillPoints(true);
		renderer.setDisplayChartValues(false);
		renderer.setColor(Color.WHITE);

		// create empty xy data series
		xySeries = new XYSeries("Elevation vs Distance");
		// and add to the multiple dataset (but really we use/plot just one)
		mDataset = new XYMultipleSeriesDataset();
		mDataset.addSeries(xySeries);

		mChartView = ChartFactory.getLineChartView(getActivity(), mDataset,
				mRenderer);

		// enable the chart click events
		mRenderer.setClickEnabled(true);
		mRenderer.setSelectableBuffer(10);

		mChartView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// handle the click event on the chart
				SeriesSelection seriesSelection = mChartView
						.getCurrentSeriesAndPoint();
				if (seriesSelection == null) {
					// Toast.makeText(getApplicationContext(), "No chart element",
					// Toast.LENGTH_SHORT).show();
				} else {
					// display information of the clicked point
					Toast.makeText(
							getActivity(),
							" Closest point: "
									+ String.format("%d" + feetMeters,
											(int) seriesSelection.getValue())
									+ " at "
									+ String.format("%.2f " + milesKm,
											seriesSelection.getXValue()),
							Toast.LENGTH_LONG).show();
				}
			}
		});
//		chartLayout.addView(mChartView, new LayoutParams(
//				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		chartLayout.addView(mChartView, new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}

	public void getAltVsDistSeries() {
		// !!!! convert to CursorLoader (and remove csr.close() below)
		Cursor csr;

		double elevation;
		double totalDistance = 0;

		csr = database.query(GPSLog.GPSLOG_TABLE, new String[] {
				GPSLog.DISTANCE_FROM_LAST_POINT, GPSLog.ELEVATION },
				GPSLog.LOCATION_EXERCISE_ID + " = ?",
				new String[] { locationExerciseId + "" }, null, null,
				GPSLog.TIMESTAMP);

		if (csr.getCount() == 0 || csr.getCount() == 1) {
			Toast.makeText(getActivity(),
					"There is no GPS_Do_not_use log data available to plot", Toast.LENGTH_LONG)
					.show();
			return;
		} else {
			csr.moveToFirst();
			while (!csr.isAfterLast()) {
				totalDistance += (double) (imperialMetric.equals(imperial) ? Utility
						.metersToMiles((float) csr.getDouble(0)) : ((float) csr
						.getDouble(0)) / 1000);
				elevation = (double) (imperialMetric.equals(imperial) ? Utility
						.metersToFeet((float) csr.getDouble(1)) : ((float) csr
						.getDouble(1)) / 1000);
				xySeries.add(totalDistance, elevation);
				csr.moveToNext();
			}
		}
		csr.close();
		mChartView.repaint();
	}

}
