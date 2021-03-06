package com.fisincorporated.exercisetracker.ui.maps;

import android.content.Context;
import android.content.Intent;
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
import com.fisincorporated.exercisetracker.ui.media.MediaDetail;
import com.fisincorporated.exercisetracker.ui.media.MediaPoint;
import com.fisincorporated.exercisetracker.ui.media.mediagrid.MediaGridPagerActivity;
import com.fisincorporated.exercisetracker.ui.utils.BitmapUtils;
import com.fisincorporated.exercisetracker.ui.utils.DisplayUnits;
import com.fisincorporated.exercisetracker.utility.PhotoUtils;
import com.fisincorporated.exercisetracker.utility.StatsUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;


//TODO remove callbacks and use RXJava or Bus
public class MapRoute implements GoogleMap.OnMarkerClickListener {

    private static final String TAG = MapRoute.class.getSimpleName();
    private static final int NON_PHOTO_PIN = -1;

    private DisplayUnits displayUnits;
    private PhotoUtils photoUtils;
    private StatsUtil statsUtil;
    private TrackerDatabaseHelper trackerDatabaseHelper;

    private SupportMapFragment supportMapFragment;
    private Context context;
    private GoogleMap googleMap;
    private LocationExerciseRecord ler;
    private boolean useCurrentLocationLabel;
    private Cursor cursor;
    private int mapType;

    private ExerciseRecord er;

    /**
     * Use to center GPS trace in googleMap frame
     */
    private LatLng southwest = null;
    private double swLat = 0;
    private double swLong = 0;
    private double neLat = 0;
    private double neLong = 0;

    private float distance = 0;
    private int pinEveryX;
    private float currentDistance = 0;
    private float[] distanceBetweenPoints = {0f};
    private String distanceUnits = "";

    // TODO set groupTime per exercise - currently 3 minutes
    private int groupTime = 3 * 60 * 1000;
    private ArrayList<MediaDetail> mediaDetailList = new ArrayList<>();
    private ArrayList<MediaPoint> mediaPoints = new ArrayList<>();
    private String title;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private boolean displayPhotoPoints;

    @Inject
    public MapRoute(DisplayUnits displayUnits, PhotoUtils photoUtils, StatsUtil statsUtil, TrackerDatabaseHelper trackerDatabaseHelper) {
        this.displayUnits = displayUnits;
        this.photoUtils = photoUtils;
        this.statsUtil = statsUtil;
        this.trackerDatabaseHelper = trackerDatabaseHelper;
    }

    public void displayPhotoPoints(boolean display){
        this.displayPhotoPoints =  display;
    }

    public MapRoute setSupportMapFragment(SupportMapFragment supportMapFragment) {
        this.supportMapFragment = supportMapFragment;
        this.context = supportMapFragment.getContext();
        return this;
    }

    public MapRoute setLocationExerciseRecord(LocationExerciseRecord ler) {
        this.ler = ler;
        return this;
    }

    public MapRoute setUseCurrentLocationLabel(boolean useCurrentLocationLabel) {
        this.useCurrentLocationLabel = useCurrentLocationLabel;
        return this;
    }

    public MapRoute setCursor(Cursor cursor) {
        this.cursor = cursor;
        return this;
    }

    public MapRoute setTitle(String title) {
        this.title = title;
        return this;
    }


    public void plotGpsRoute() {
        getMapReady();
    }

    private void setupMapPinInfo() {
        currentDistance = 0;
        distance = 0;
        distanceUnits = (displayUnits.isImperialDisplay() ?
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
        Single<ExerciseRecord> observable = trackerDatabaseHelper.getErSingleObservable(ler.getExerciseId());
        compositeDisposable.add(observable.subscribe(exerciseRecord -> {
                    er = exerciseRecord;
                    getPhotosTakenFromStartToFinish();
                    //getPhotosTakenFromMidnightToEoD();
                },
                throwable -> {
                    Toast.makeText(context, R.string.error_reading_exercise_record, Toast.LENGTH_LONG).show();
                    Log.e(TAG, throwable.toString());
                }));
    }


    // TODO get intial value from preferences, store any change to map type to preference
    public MapRoute setMapType(int mapType) {
        if (googleMap != null) {
            googleMap.setMapType(mapType);
        }
        this.mapType = mapType;
        return this;
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
        Marker marker;
        try {
            DateFormat sdf = DateFormat.getDateTimeInstance();
            if (googleMap != null) {
                // mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                marker = googleMap.addMarker(new MarkerOptions()
                        .title("Start")
                        .snippet(sdf.format(ler.getStartTimestamp()))
                        .position(
                                new LatLng((double) ler.getStartLatitude(), (double) ler
                                        .getStartLongitude())));
                marker.setTag(NON_PHOTO_PIN);

                marker = googleMap.addMarker(new MarkerOptions()
                        .title(useCurrentLocationLabel ? context.getString(R.string.current_location) : context.getString(R.string.end))
                        .snippet(sdf.format(ler.getEndTimestamp()))
                        .position(
                                new LatLng((double) ler.getEndLatitude(), (double) ler
                                        .getEndLongitude())));
                marker.setTag(NON_PHOTO_PIN);
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
        long fromTime;
        long toTime;
        int elevationIndex;
        int timestampIndex;
        int photoStartIndex = 0;
        mediaPoints.clear();

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
            fromTime = Timestamp.valueOf(csr.getString(timestampIndex)).getTime();
            Log.d(TAG, "fromTime:" + csr.getString(timestampIndex));
            csr.moveToNext();
            while (!csr.isAfterLast()) {
                toLatLng = new LatLng(csr.getDouble(latIndex), csr.getDouble(longIndex));
                toTime = Timestamp.valueOf(csr.getString(timestampIndex)).getTime();
                googleMap.addPolyline(new PolylineOptions().add(fromLatLng, toLatLng)
                        .width(5).color(Color.RED));
                // determine if you need to display mileage pin
                calcDistanceToPlacePin(fromLatLng, toLatLng, googleMap);
                updateMapLatLongCorners(toLatLng);
                Log.d(TAG, "toTime:" + csr.getString(timestampIndex));
                if (displayPhotoPoints) {
                    photoStartIndex = setPhotoMarkers(fromLatLng, fromTime, toTime, groupTime, photoStartIndex);
                }
                fromLatLng = toLatLng;
                fromTime = toTime;
                csr.moveToNext();
            }
            LatLng southwest = new LatLng(swLat, swLong);
            LatLng northeast = new LatLng(neLat, neLong);

            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(
                    southwest, northeast), 500, 500, 0));

            if (displayPhotoPoints) {
                placePhotoPoints();
            }

        }
    }

    private int setPhotoMarkers(LatLng atLatLng, long fromTime, long toTime, int groupTime, int photoStartIndex) {
        Log.d(TAG, "Starting at photo " + photoStartIndex + " latLng:" + atLatLng.toString());
        int photoPointsSize = 0;
        long photoDate;
        if (mediaDetailList.size() == 0 || photoStartIndex > mediaDetailList.size() - 1) {
            return photoStartIndex;
        }

        for (int i = photoStartIndex; i < mediaDetailList.size(); i++) {
            photoPointsSize = mediaPoints.size();
            photoDate = mediaDetailList.get(i).getDateTaken();
            Log.d(TAG, " Photo timestamp:" + new Timestamp(photoDate));
            // Photo taken within groupTime and first photo that meets that criteria
            if ((photoPointsSize == 0 && photoDate >= fromTime && photoDate <= (fromTime + groupTime))
                    // or photo taken within the groupTime period
                    || (photoPointsSize > 0 && photoDate >= mediaPoints.get(photoPointsSize - 1).getTime() &&
                    photoDate <= mediaPoints.get(photoPointsSize - 1).getTime() + groupTime)
                    // or photo first in new group
                    || (photoDate >= fromTime && photoDate <= (fromTime + groupTime))
                    // or GPS lost signal or stopped tracking and toTime is greater than fromTime + groupTime so add to existing group
                    || (photoDate >= fromTime && photoDate <= toTime)) {
                addPhotoDetailToPhotoPoint(mediaDetailList.get(i), atLatLng, fromTime, toTime, groupTime);
                Log.d(TAG, "Adding photo " + i + " photoTime:"
                        + new Timestamp(mediaDetailList.get(i).getDateTaken())
                        + " group:" + mediaPoints.size());
                ++photoStartIndex;
            }
        }
        return photoStartIndex;
    }

    private void addPhotoDetailToPhotoPoint(MediaDetail mediaDetail, LatLng latLng, long fromTime, long toTime, int groupTime) {
        if (mediaPoints.size() == 0) {
            addNewPhotoPoint(mediaDetail, latLng, fromTime);
        } else {
            MediaPoint mediaPoint = mediaPoints.get(mediaPoints.size() - 1);
            // see if photo can go into current group or start new group
            if ((mediaDetail.getDateTaken() <= mediaPoint.getTime() + groupTime)
                    || mediaDetail.getDateTaken() >= fromTime && mediaDetail.getDateTaken() <= toTime) {
                mediaPoint.addPhotoDetail(mediaDetail);
            } else {
                // add photo to new group
                addNewPhotoPoint(mediaDetail, latLng, fromTime);
            }
        }
    }

    private void addNewPhotoPoint(MediaDetail mediaDetail, LatLng latLng, long fromTime) {
        MediaPoint mediaPoint = MediaPoint.getInstance(fromTime, latLng);
        mediaPoint.addPhotoDetail(mediaDetail);
        mediaPoints.add(mediaPoint);
    }

    private void placePhotoPoints() {
        if (mediaPoints.size() > 0) {
            googleMap.setOnMarkerClickListener(this);
        }
        for (int i = 0; i < mediaPoints.size(); ++i) {
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(mediaPoints.get(i).getLatlng())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.photo_map_pin)));
           // .icon(BitmapDescriptorFactory.fromBitmap(photoBitmap)));
            //.title(i + ""));
            marker.setTag(i);

        }
    }

    /**
     * Called when the user clicks a marker.
     */
    @Override
    public boolean onMarkerClick(final Marker marker) {
        Intent intent;
        Integer photoPointPosition = (Integer) marker.getTag();
        // Seems like Maps returns click on a milage marker that is very close to photo pin
        if (photoPointPosition != null && photoPointPosition != NON_PHOTO_PIN) {
            MediaPoint mediaPoint = mediaPoints.get(photoPointPosition);
            intent = MediaGridPagerActivity.IntentBuilder.getBuilder(context)
                    .setPhotoPoints(mediaPoints)
                    .setPhotoPointPosition(photoPointPosition)
                    .setTitle(title).build();
            context.startActivity(intent);
            return true;
        }
        return false;
    }

    private void assignDistancePerPin() {
        pinEveryX = (er != null ? er.getPinEveryXMiles() : GlobalValues.DEFAULT_NO_PINS);
    }

    private void calcDistanceToPlacePin(LatLng fromLatLng, LatLng toLatLng, GoogleMap map) {
        if (pinEveryX <= 0) {
            return;
        }
        boolean isImperialDisplay = displayUnits.isImperialDisplay();
        Location.distanceBetween(fromLatLng.latitude, fromLatLng.longitude, toLatLng.latitude, toLatLng.longitude, distanceBetweenPoints);
        distance += distanceBetweenPoints[0];
        if (statsUtil.coveredDistanceForMarker(distance, isImperialDisplay, pinEveryX)) {
            currentDistance += distance;
            int displayDistance = statsUtil.calcDisplayDistance(currentDistance, isImperialDisplay);
            distance = 0;
            Marker marker = map.addMarker(new MarkerOptions()
                    .position(toLatLng)
                    .icon(BitmapDescriptorFactory.fromBitmap(setMarkerDrawable(displayDistance + distanceUnits)))
                    // SET ANCHOR POINT TO MARKER CENTER
                    .anchor(0.5f, 0.5f));
            marker.setTag(NON_PHOTO_PIN);
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
    private void updateMapLatLongCorners(LatLng gpsLatLng) {
        if (southwest == null) {
            southwest = gpsLatLng;
            swLat = gpsLatLng.latitude;
            swLong = gpsLatLng.longitude;

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


    public void getPhotosTakenFromStartToFinish() {
        long startTimeLong;
        long endTimeLong;
        startTimeLong = ler.getStartTimestamp().getTime();
        endTimeLong = ler.getEndTimestamp().getTime();
        if (displayPhotoPoints) {
            getPhotosTaken(context, startTimeLong, endTimeLong);
        } else {
            startPlotting();
        }
    }


    public void getPhotosTakenFromMidnightToEoD() {
        // Find midnight at start of activity
        long startTimeLong = ler.getStartTimestamp().getTime();
        long startMidnight = startTimeLong - (startTimeLong % (24 * 60 * 60 * 1000));
        // find 1 sec prior to midnight at end of activity (might be next day or later);
        long endTimeLong = ler.getEndTimestamp().getTime();
        long endOfDay = endTimeLong - (endTimeLong % (24 * 60 * 60 * 1000)) + (1000 * (59 + (59 * 60) + (11 * 60 * 60)));
        if (displayPhotoPoints) {
            getPhotosTaken(context, startMidnight, endOfDay);
        } else {
            startPlotting();
        }
    }

    public void getPhotosTaken(Context context, long startTime, long endTime) {
        compositeDisposable.add(photoUtils.getMediaListObservable(context, startTime, endTime)
                .onErrorReturn(throwable -> {
                            Toast.makeText(context, R.string.error_get_photos_for_activity, Toast.LENGTH_LONG).show();
                            return new ArrayList<>();
                        }
                )
                .subscribe(medialDetailList -> {
                            MapRoute.this.mediaDetailList = medialDetailList;
                            startPlotting();
                        },
                        throwable -> {
                            Toast.makeText(context, R.string.error_get_photos_for_activity, Toast.LENGTH_LONG).show();
                        }));
    }

    public void onTerminate() {
        compositeDisposable.dispose();
    }
}
