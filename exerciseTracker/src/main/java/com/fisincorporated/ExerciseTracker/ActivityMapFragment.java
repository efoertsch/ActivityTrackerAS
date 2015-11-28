package com.fisincorporated.ExerciseTracker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.fisincorporated.database.ExerciseDAO;
import com.fisincorporated.database.ExerciseRecord;
import com.fisincorporated.database.ExrcsLocationDAO;
import com.fisincorporated.database.ExrcsLocationRecord;
import com.fisincorporated.database.GPSLogDAO;
import com.fisincorporated.database.LocationExerciseDAO;
import com.fisincorporated.database.LocationExerciseRecord;
import com.fisincorporated.database.SQLiteCursorLoader;
import com.fisincorporated.database.TrackerDatabase.GPSLog;
import com.fisincorporated.database.TrackerDatabase.LocationExercise;
import com.fisincorporated.utility.Utility;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class ActivityMapFragment extends ExerciseMasterFragment implements
		LoaderCallbacks<Cursor> {
	public static final String USE_CURRENT_LOCATION_LABEL = "ActivityMapFragment.CURRENT_LOCATION_LABEL";
	private static final int DELETE_REQUESTCODE = 1;
	private LocationExerciseRecord ler = null;
	private LocationExerciseDAO leDAO = null;
	private ExerciseRecord er = null;
	private ExerciseDAO eDAO = null;
	private ExrcsLocationRecord elr = null;
	private ExrcsLocationDAO elDAO = null;
	private long locationExerciseId;
	private String activityTitle = GlobalValues.UNDEFINED;
	private String title = "";
	private String description = "";
	private String kmlFileName = null;
	private File kmlPath = null;
	private File kmlFile = null;
	private String eol = System.getProperty("line.separator");
	private boolean useCurrentLocationLabel = false;

	private int deleteDetailType;
	private static final int FOR_MAP_PLOT = 1;
	private static final int FOR_KML_FILE = 2;
	private int logicPath;

	private TextView tvInfo;

	 
	private GoogleMap map;
	//private MapFragment mapFragment = null;
	private SupportMapFragment supportMapFragment = null;
	
	String newline = System.getProperty("line.separator");
	

	public static ActivityMapFragment newInstance(Bundle bundle) {
//		Bundle args = new Bundle();
//		args.putLong(LocationExercise._ID,
//				bundle.getLong(LocationExercise._ID, -1));
//		args.putString(GlobalValues.TITLE, bundle.getString(GlobalValues.TITLE));
//		args.putString(LocationExercise.DESCRIPTION, bundle.getString(LocationExercise.DESCRIPTION));
		ActivityMapFragment fragment = new ActivityMapFragment();
		//fragment.setArguments(args);
		fragment.setArguments(bundle);
		return fragment;
	}

	public void onCreate(Bundle savedInstanceState) {
		setRetainInstance(true);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		// View view = inflater.inflate(R.layout.map_layout, container, false);
		View view = inflater.inflate(R.layout.map_layout_wo_map_fragment,
				container, false);
		// Can only have one instance of SupportMapFragment so make sure
		// singletion (which newInstance is doing)
		supportMapFragment = SupportMapFragment.newInstance();
		FragmentTransaction fragmentTransaction = getChildFragmentManager()
				.beginTransaction();
		fragmentTransaction.add(R.id.mapFragmentContainer, supportMapFragment);
		fragmentTransaction.commit();
		setHasOptionsMenu(true);
		getReferencedViews(view);

		return view;

	}

	private void getArgumentBundle() {
		Bundle bundle = getArguments();
		if (bundle != null) {
			locationExerciseId = bundle.getLong(LocationExercise._ID, -1);
			title = bundle.getString(GlobalValues.TITLE);
			description = bundle.getString(LocationExercise.DESCRIPTION);
			activityTitle = title + "  " + description ;
			useCurrentLocationLabel = bundle.getBoolean(USE_CURRENT_LOCATION_LABEL, false );
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		checkForGooglePlayServices();
		getArgumentBundle();
		tvInfo.setText(activityTitle);
		loadExerciseInfo();
		map = supportMapFragment.getMap();
		if (map != null){
		plotGPSPoints();
		getLoaderManager().initLoader(GlobalValues.MAP_LOADER, null, this);
		}
		else {
			Toast.makeText(getActivity(), getResources().getText(R.string.maps_not_available), Toast.LENGTH_LONG).show();
		}
			

	}

	private void checkForGooglePlayServices() {
		int GooglePlayAvailableCode;
		GooglePlayAvailableCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getActivity());
		if (ConnectionResult.SUCCESS != GooglePlayAvailableCode) {
			Toast.makeText(getActivity(), "GooglePlayServices not available",
					Toast.LENGTH_LONG).show();
			getFragmentManager().popBackStack();
		}
	}

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.activity_map_menu, menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.activity_map_type_satellite_plus_streets:
			if (map != null)
				map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			return true;
		case R.id.activity_map_type_satellite:
			if (map != null)
				map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			return true;
		case R.id.activity_map_type_normal:
			if (map != null)
				map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			return true;
		case R.id.activity_map_type_terrain:
			if (map != null)
				map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
			return true;
//		case R.id.activity_map_show_stats:
//			args.putInt(GlobalValues.DISPLAY_TARGET, GlobalValues.DISPLAY_STATS);
//			loadArgsAndCallback(args);
//			return true;
//		case R.id.activity_map_show_chart:
//			args.putInt(GlobalValues.DISPLAY_TARGET, GlobalValues.DISPLAY_CHART);
//			loadArgsAndCallback(args);
//			return true;
		case R.id.activity_map_email:
			Toast.makeText(getActivity(),
					getResources().getString(R.string.email_being_created),
					Toast.LENGTH_SHORT).show();
			logicPath = FOR_KML_FILE;
			getLoaderManager().restartLoader(GlobalValues.MAP_LOADER, null, this);
			return true;
//		case R.id.activity_map_delete:
//			deleteDetailType = 2;
//			ActivityDialogFragment dialog = ActivityDialogFragment.newInstance(-1,
//					R.string.delete_detail_confirmation, R.string.yes, R.string.no,
//					-1);
//			dialog.setTargetFragment(ActivityMapFragment.this, DELETE_REQUESTCODE);
//			dialog.show(getActivity().getSupportFragmentManager(), "confirmDialog");
//			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

//	private void loadArgsAndCallback(Bundle args) {
//		args.putLong(LocationExercise._ID,   locationExerciseId);
//		args.putString(GlobalValues.TITLE,title);
//		args.putString(LocationExercise.DESCRIPTION, description) ;
//		callBacks.onSelectedAction(args);
//	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (resultCode != Activity.RESULT_OK)
			return;
		if (requestCode == DELETE_REQUESTCODE) {
			int buttonPressed = intent.getIntExtra(
					ActivityDialogFragment.DIALOG_RESPONSE, -1);
			if (buttonPressed == DialogInterface.BUTTON_POSITIVE) {
				doPositiveCancelClick();
			} else if (buttonPressed == DialogInterface.BUTTON_NEGATIVE) {
				doNegativeCancelClick();
			}
		}
	}

	public void doPositiveCancelClick() {
		GPSLogDAO gpslogDAO = new GPSLogDAO(databaseHelper);
		gpslogDAO.deleteGPSLogbyLerRowId(ler.get_id());
		Toast.makeText(getActivity(), "GPS log detail deleted.",
				Toast.LENGTH_SHORT).show();
		if (deleteDetailType == 1) {
			leDAO = new LocationExerciseDAO(databaseHelper);
			leDAO.deleteLocationExercise(ler);
			Toast.makeText(getActivity(), "Activity deleted.", Toast.LENGTH_SHORT)
					.show();
			getFragmentManager().popBackStack();
		}
	}

	// continue on as before
	public void doNegativeCancelClick() {
		return;
	}

	private void loadExerciseInfo() {

		leDAO = new LocationExerciseDAO(databaseHelper);
		ler = leDAO.loadLocationExerciseRecordById(locationExerciseId);
		if (ler.get_id() > 0) {
			eDAO = new ExerciseDAO(databaseHelper);
			er = eDAO.loadExerciseRecordById(ler.getExerciseId());
			elDAO = new ExrcsLocationDAO(databaseHelper);
			elr = elDAO.loadExrcsLocationRecordById(ler.getLocationId());

		} else {
			Toast.makeText(
					getActivity(),
					"Valid activity record not found with id =" + locationExerciseId,
					Toast.LENGTH_LONG).show();
			getFragmentManager().popBackStack();
		}

		if (ler.getStartTimestamp() == null) {
			Toast.makeText(getActivity(),
					"Invalid activity record. Insufficient information to map.",
					Toast.LENGTH_LONG).show();
			getFragmentManager().popBackStack();
		}
	}

	private void getReferencedViews(View view) {
		tvInfo = (TextView) view.findViewById(R.id.map_activity_tvInfo);

	}

	private void plotGPSPoints() {
		// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		DateFormat sdf = DateFormat.getDateTimeInstance();
		if (map != null) {
			// mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
			map.addMarker(new MarkerOptions()
					.title("Start")
					.snippet(sdf.format(ler.getStartTimestamp()))
					.position(
							new LatLng((double) ler.getStartLatitude(), (double) ler
									.getStartLongitude())));

			map.addMarker(new MarkerOptions()
					.title( useCurrentLocationLabel ? "Current Location": "End")
					.snippet(sdf.format(ler.getEndTimestamp()))
					.position(
							new LatLng((double) ler.getEndLatitude(), (double) ler
									.getEndLongitude())));

			LatLng southwest = new LatLng(Math.min(ler.getStartLatitude(),
					ler.getEndLatitude()), Math.min(ler.getStartLongitude(),
					ler.getEndLongitude()));
			LatLng northeast = new LatLng(Math.max(ler.getStartLatitude(),
					ler.getEndLatitude()), Math.max(ler.getStartLongitude(),
					ler.getEndLongitude()));
			map.moveCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(
					southwest, northeast), 500, 500, 0));
		}

		try {
			logicPath = FOR_MAP_PLOT;
		} catch (Exception e) {
			Toast.makeText(getActivity(),
					"Sorry - an error occurred in mapping the GPS route.",
					Toast.LENGTH_SHORT).show();
			Log.e(GlobalValues.LOG_TAG, "ActivityMap.onResume()" + e.toString());

		}

	}

	private static class GPSPointsCursorLoader extends SQLiteCursorLoader {
		// private DeletePriorActivitiesFragment palf;
		ActivityMapFragment am;

		// public ListCursorLoader(Context context,DeletePriorActivitiesFragment
		// palf) {
		public GPSPointsCursorLoader(Context context, ActivityMapFragment am) {
			super(context);
			this.am = am;
		}

		@Override
		protected Cursor loadCursor() {
			return am.getGPSPointsCursor();
		}
	}

	protected Cursor getGPSPointsCursor() {
		if (!database.isOpen()) {
			Log.e(GlobalValues.LOG_TAG,
					"ActivityMapFragment.getGPSPointsCursor:com.fisincorporated.database is not open attempting reopen");
		}
		database = databaseHelper.getWritableDatabase();
		if (!database.isOpen()) {
			Log.e(GlobalValues.LOG_TAG,
					"ActivityMapFragment.getGPSPointsCursor:com.fisincorporated.database is not open. Big Trouble in Little China");
		}
		csrUtility = database.query(GPSLog.GPSLOG_TABLE, new String[] {
				GPSLog.LATITUDE, GPSLog.LONGITUDE, GPSLog.ELEVATION },
				GPSLog.LOCATION_EXERCISE_ID + " = ?",
				new String[] { locationExerciseId + "" }, null, null,
				GPSLog.TIMESTAMP);
		return csrUtility;
	}

	private void plotRoute(Cursor csr) throws IOException {

		switch (logicPath) {
		case FOR_MAP_PLOT:
			plotRouteForMap(csr);
			break;
		case FOR_KML_FILE:
			if (createKMLFile()) {
				writeToKMLFile(csr);
				emailKMLFile();
			}
			break;
		}

	}

	private void plotRouteForMap(Cursor csr) {
		LatLng fromLatLng;
		LatLng toLatLng;
		if (csr.getCount() == 0 || csr.getCount() == 1) {
			return;
		} else {
			csr.moveToFirst();
			fromLatLng = new LatLng(csr.getDouble(0), csr.getDouble(1));
			csr.moveToNext();
			while (!csr.isAfterLast()) {
				toLatLng = new LatLng(csr.getDouble(0), csr.getDouble(1));
				map.addPolyline(new PolylineOptions().add(fromLatLng, toLatLng)
						.width(5).color(Color.RED));
				fromLatLng = toLatLng;
				csr.moveToNext();
			}
			// cursor closed by LoadManager
			// csr.close();
		}
	}

	private boolean createKMLFile() {
		boolean success = true;
		String appName = getResources().getString(R.string.app_name);
		kmlFileCleanup();
		// Any changes to file format requires change to kmlFileCleanup
		kmlFileName = Utility.makeFileNameReady(appName
				+ "."
				+ er.getExercise()
				+ "@"
				+ elr.getLocation()
				+ "_"
				+ new SimpleDateFormat("yyyy-MM-dd HH:mm").format(ler
						.getStartTimestamp()) + ".kml");
		kmlPath = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		kmlFile = new File(kmlPath, kmlFileName);
		kmlPath.mkdirs();
		if (!kmlPath.isDirectory()) {
			Toast.makeText(
					getActivity(),
					"Sorry. The GPS attachment file could not be created at "
							+ kmlPath.getAbsolutePath(), Toast.LENGTH_LONG).show();
			return false;
		}

		return success;
	}

	// Note that coordinates for kml file are longitude/latitude
	private void writeToKMLFile(Cursor csr) throws IOException {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(kmlFile.getAbsoluteFile())));
			writer.write(GlobalValues.KML_ROUTE_HEADER + eol);
			writer.write(" <Placemark><name>" + Utility.removeLtGt(activityTitle)
					+ "</name>" + eol);
			writer.write("<description>"
					+ Utility.removeLtGt(ler.getDescription()) + "</description>"
					+ eol);
			writer.write("<LineString><coordinates>" + eol);
			if (csr.getCount() > 0) {
				csr.moveToFirst();
				while (!csr.isAfterLast()) {
					writer.write(csr.getDouble(1) + "," + csr.getDouble(0) + ","
							+ csr.getInt(2) + eol);
					csr.moveToNext();
				}
			}
			writer.write(GlobalValues.KML_ROUTE_TRAILER + eol);
		} catch (Exception e) {
			Log.e(GlobalValues.LOG_TAG,
					"ActivityMapFragment.writeToKMLFile error " + e.toString());
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (Exception e) {
					;
				}
			}
			// cursor closed by LoadManager
			// if (csr != null && !csr.isClosed()) {
			// try {
			// csr.close();
			// } catch (Exception e) {
			// ;
			// }
			// }
		}
	};

	/**
	 * Note this depends on having successfully written KML file and the
	 * path/filename can be had from kmlPath and kmlFileName
	 */
	private void emailKMLFile() {
		String summaryStats = "";
		if (!kmlFile.exists() || !kmlFile.canRead()) {
			Toast.makeText(getActivity(),
					"Can't find or read the created logfile: " + kmlFile.getName(),
					Toast.LENGTH_SHORT).show();
			return;
		}
		Toast.makeText(getActivity(), "KML attachment at " + kmlFile.getName(),
				Toast.LENGTH_SHORT).show();
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.setType("text/xml");

		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "My "
				+ activityTitle);

		summaryStats = getStatsForEmail();

		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, summaryStats + getResources()
				.getString(R.string.kml_email_explain) + newline + newline);

		Uri uri = Uri.parse("file://" + kmlFile.getAbsolutePath());
		emailIntent.putExtra(Intent.EXTRA_STREAM, uri);

		startActivity(Intent.createChooser(emailIntent, "Send mail..."));
	}
	
	private String getStatsForEmail(){
		ArrayList<String[]> stats = new ArrayList<String[]>();
		
		StringBuilder sb = new StringBuilder();
		sb.append(getResources().getString(R.string.my_activity_statistics, title ) + newline + newline);
		findDisplayUnits();
		Utility.formatActivityStats(getActivity(), stats, ler,
				imperialMetric, imperial, feetMeters, milesKm, mphKph);
		for (int i = 0;i < stats.size(); ++i ){
			sb.append(stats.get(i)[0] + ":\t" + stats.get(i)[1] + newline );
		} 
		sb.append(newline);
		return sb.toString();
	}

	private void kmlFileCleanup() {
		final String appName = getResources().getString(R.string.app_name);
		File folder = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		File[] files = folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(final File dir, final String name) {
				return name.matches(appName + ".*.@.*_*.kml");
			}
		});
		for (final File file : files) {
			if (!file.delete()) {
				Log.e(GlobalValues.LOG_TAG,
						"Can't remove " + file.getAbsolutePath());
			}
		}
	}

	// LoaderCallBacks interface methods
	// #1
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// we only ever load the runs, so assume this is the case
		return new GPSPointsCursorLoader(getActivity(), this);
	}

	// #2
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

		try {
			plotRoute(cursor);
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	// #3
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// not sure what is needed here
		// cursor should be handled/closed by LoadManager

		;
	}

}