package com.fisincorporated.exercisetracker.ui.maps;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.database.ExerciseRecord;
import com.fisincorporated.exercisetracker.database.LocationExerciseRecord;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.GPSLog;
import com.fisincorporated.exercisetracker.database.TrackerDatabaseHelper;
import com.fisincorporated.exercisetracker.ui.utils.DisplayUnits;
import com.fisincorporated.exercisetracker.utility.Utility;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DateFormat;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;



public class MapRoute {

    private static String TAG = MapRoute.class.getSimpleName();

    private SupportMapFragment supportMapFragment;
    private Context context;
    private GoogleMap googleMap;
    private LocationExerciseRecord ler;
    private boolean useCurrentLocationLabel;
    private Cursor cursor;

    private ExerciseRecord er;

    /**
     * Use to center GPS trace in googleMap frame
     */
    private LatLng southwest = null;
    private LatLng northeast = null;
    private double swLat = 0;
    private double swLong = 0;
    private double neLat = 0;
    private double neLong = 0;

    private float distance = 0;
    private int pinEveryX;
    private float currentDistance = 0;
    private float[] distanceBetweenPoints = {0f};
    private String distanceUnits = "";

    // maptype must be GoogleMap.MAP_TYPE_HYBRID, _SATELLITE, ...
    private int mapType;

    CompositeDisposable compositeDisposable = new CompositeDisposable();

    public MapRoute() {
        this.supportMapFragment = supportMapFragment;
    }


    public static class Builder {

        SupportMapFragment supportMapFragment;
        LocationExerciseRecord ler;
        boolean useCurrentLocationLabel;
        int mapType;
        Cursor cursor;

        public Builder(SupportMapFragment supportMapFragment) {
            this.supportMapFragment = supportMapFragment;
        }

        public Builder setLocationExerciseRecord(LocationExerciseRecord ler) {
            this.ler = ler;
            return this;
        }

        public Builder setUseCurrentLocationLabel(boolean useCurrentLocationLabel) {
            this.useCurrentLocationLabel = useCurrentLocationLabel;
            return this;
        }

        public Builder setMapType(int mapType) {
            this.mapType = mapType;
            return this;
        }

        public void setCursor(Cursor cursor) {
            this.cursor = cursor;
        }

        public MapRoute build() {
            MapRoute mapRoute = new MapRoute();
            mapRoute.supportMapFragment = this.supportMapFragment;
            mapRoute.context = supportMapFragment.getContext();
            mapRoute.ler = ler;
            mapRoute.useCurrentLocationLabel = useCurrentLocationLabel;
            mapRoute.cursor = cursor;
            mapRoute.mapType = mapType;
            return mapRoute;
        }

    }

    public void plotGpsRoute() {
        getMapReady();
    }

    private void setupMapPinInfo() {
        currentDistance = 0;
        distance = 0;
        distanceUnits = (DisplayUnits.isImperialDisplay() ?
                context.getString(R.string.map_pin_miles) : context.getString(R.string.map_pin_kilometers));
    }

    public void getMapReady() {
        Observable<GoogleMap> observable = RxGoogleMap.getGoogleMapObservable(supportMapFragment);
        compositeDisposable.add(observable.subscribe(googleMap -> {
            if (googleMap != null) {
                this.googleMap = googleMap;
                getExerciseRecord();
            } else {
                Toast.makeText(context, context.getText(R.string.maps_not_available), Toast.LENGTH_LONG).show();
            }
        }));
    }

    public void getExerciseRecord() {
        Single<ExerciseRecord> observable = TrackerDatabaseHelper.getErSingleObservable(ler.getExerciseId());
        compositeDisposable.add(observable.subscribe(exerciseRecord -> {
                    er = exerciseRecord;
                    startPlotting();
                },
                throwable -> {
                    Toast.makeText(context, R.string.error_reading_exercise_record, Toast.LENGTH_LONG).show();
                    Log.e(TAG, throwable.toString());
                }));
    }


    public void setMapType(int mapType) {
        if (googleMap != null) {
            googleMap.setMapType(mapType);
        }
        this.mapType = mapType;
    }

    private void startPlotting() {
        setupMapPinInfo();
        plotStartEndGPSPoints();
        plotRouteOnMap(cursor);
    }


    /**
     * Place start and end markers on googleMap
     */
    private void plotStartEndGPSPoints() {
        // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            DateFormat sdf = DateFormat.getDateTimeInstance();
            if (googleMap != null) {
                // mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                googleMap.addMarker(new MarkerOptions()
                        .title("Start")
                        .snippet(sdf.format(ler.getStartTimestamp()))
                        .position(
                                new LatLng((double) ler.getStartLatitude(), (double) ler
                                        .getStartLongitude())));

                googleMap.addMarker(new MarkerOptions()
                        .title(useCurrentLocationLabel ? context.getString(R.string.current_location) : context.getString(R.string.end))
                        .snippet(sdf.format(ler.getEndTimestamp()))
                        .position(
                                new LatLng((double) ler.getEndLatitude(), (double) ler
                                        .getEndLongitude())));
            }
        } catch (Exception e) {
            Toast.makeText(context,
                    R.string.an_error_occured_in_mapping_gps_route,
                    Toast.LENGTH_SHORT).show();
            Log.e(GlobalValues.LOG_TAG, TAG + ":" + e.toString());
        }
    }

    private void plotRouteOnMap(Cursor csr) {
        LatLng fromLatLng;
        LatLng toLatLng;
        int latIndex;
        int longIndex;
        int elevationIndex;
        int timestampIndex;

        if (csr.getCount() == 0 || csr.getCount() == 1) {
            return;
        } else {
            latIndex = csr.getColumnIndex(GPSLog.LATITUDE);
            longIndex = csr.getColumnIndex(GPSLog.LONGITUDE);
            elevationIndex = csr.getColumnIndex(GPSLog.ELEVATION);
            timestampIndex = csr.getColumnIndex(GPSLog.TIMESTAMP);

            assignDistancePerPin();
            csr.moveToFirst();
            fromLatLng = new LatLng(csr.getDouble(latIndex), csr.getDouble(longIndex));
            csr.moveToNext();
            while (!csr.isAfterLast()) {
                toLatLng = new LatLng(csr.getDouble(latIndex), csr.getDouble(longIndex));
                googleMap.addPolyline(new PolylineOptions().add(fromLatLng, toLatLng)
                        .width(5).color(Color.RED));
                // determine if you need to display mileage pin
                calcDistanceToPlacePin(fromLatLng, toLatLng, googleMap);
                findGpsCorners(toLatLng);
                fromLatLng = toLatLng;
                csr.moveToNext();
            }
            LatLng southwest = new LatLng(swLat, swLong);
            LatLng northeast = new LatLng(neLat, neLong);

            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(
                    southwest, northeast), 500, 500, 0));

            // cursor closed by LoadManager
            // csr.close();
        }
    }

    private void assignDistancePerPin() {
        pinEveryX = (er != null ? er.getPinEveryXMiles() : GlobalValues.DEFAULT_NO_PINS);
    }

    private void calcDistanceToPlacePin(LatLng fromLatLng, LatLng toLatLng, GoogleMap map) {
        if (pinEveryX <= 0) {
            return;
        }
        boolean isImperialDisplay = DisplayUnits.isImperialDisplay();
        Location.distanceBetween(fromLatLng.latitude, fromLatLng.longitude, toLatLng.latitude, toLatLng.longitude, distanceBetweenPoints);
        distance += distanceBetweenPoints[0];
        if (Utility.coveredDistanceForMarker(distance, isImperialDisplay, pinEveryX)) {
            currentDistance += distance;
            int displayDistance = Utility.calcDisplayDistance(currentDistance, isImperialDisplay);
            distance = 0;
            map.addMarker(new MarkerOptions()
                    .position(toLatLng)
                    .icon(BitmapDescriptorFactory.fromBitmap(setMarkerDrawable(displayDistance + distanceUnits)))
                    // SET ANCHOR POINT TO MARKER CENTER
                    .anchor(0.5f, 0.5f));
        }
    }

    // cribbed code for custom marker from http://www.ugopiemontese.eu/2014/06/18/custom-markers-for-android-google-maps-api-v2/
    public Bitmap setMarkerDrawable(String pinText) {
        Bitmap icon = BitmapUtils.drawTextToBitmap(context, R.drawable.map_pin_circle, pinText);
        return icon;
    }


    /**
     * Find the most southwest and northeast GPS lat/long
     *
     * @param gpsLatLng
     */
    private void findGpsCorners(LatLng gpsLatLng) {
        if (southwest == null) {
            southwest = gpsLatLng;
            swLat = gpsLatLng.latitude;
            swLong = gpsLatLng.longitude;

            northeast = gpsLatLng;
            neLat = gpsLatLng.latitude;
            neLong = gpsLatLng.longitude;
        }
        if (gpsLatLng.latitude < swLat) {
            swLat = gpsLatLng.latitude;
        }
        if (gpsLatLng.longitude < swLong) {
            swLong = gpsLatLng.longitude;
        }
        if (gpsLatLng.latitude > neLat) {
            neLat = gpsLatLng.latitude;
        }
        if (gpsLatLng.longitude > neLong) {
            neLong = gpsLatLng.longitude;
        }

    }

    public void onTerminate() {
        compositeDisposable.dispose();
    }
}
