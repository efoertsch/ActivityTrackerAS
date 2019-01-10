package com.fisincorporated.exercisetracker.utility;

import android.content.Context;
import android.content.res.Resources;
import android.widget.Toast;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.database.LocationExerciseRecord;
import com.fisincorporated.exercisetracker.ui.utils.DisplayUnits;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class StatsUtil {

    private DisplayUnits displayUnits;
    private Resources res;

    /**
     * For testing
     *
     * @return
     */
    public static StatsUtil getInstance() {
        return new StatsUtil();
    }

    private StatsUtil() {
    }

    public StatsUtil(DisplayUnits displayUnits, Context context) {
        this.displayUnits = displayUnits;
        res = context.getResources();
    }

    public void formatActivityStats(ArrayList<String[]> stats,
                                    LocationExerciseRecord ler, boolean currentlyActive, boolean forNotification) {
        boolean isImperialDisplay = displayUnits.isImperialDisplay();
        String feetMeters = displayUnits.getFeetMeters();
        String milesKm = displayUnits.getMilesKm();
        String mphKph = displayUnits.getMphKph();

        stats.clear();

        float distance;
        if (ler.getDistance() == null) {
            distance = 0;
        } else {
            distance = (isImperialDisplay ? metersToMiles((float) ler.getDistance()) : ((float) ler
                    .getDistance()) / 1000f);
        }
        stats.add(new String[]{
                res.getString(R.string.distance_traveled),
                String.format("%.1f " + milesKm, distance)});

        long elapsed;
        if (ler.getEndTimestamp() == null) {
            elapsed = 0;
        } else {
            elapsed = ler.getEndTimestamp().getTime()
                    - ler.getStartTimestamp().getTime();
        }
        int diffHours = (int) (elapsed / GlobalValues.TIME_TO_FRACTION_HOURS);
        // find remaining minutes
        int diffMinutes = Math.round((elapsed - (diffHours * GlobalValues.TIME_TO_FRACTION_HOURS))
                / GlobalValues.TIME_TO_MINUTES);
        stats.add(new String[]{
                res.getString(R.string.time_on_activity),
                res.getString(R.string.hours_and_minutes, diffHours, res.getString(R.string.hrs), diffMinutes, res.getString(R.string.mins))});

        // speeds in kph or mph per distance calc above
        float averageSpeed = 0;
        if (elapsed > 0) {
            averageSpeed = (distance)
                    / (elapsed / GlobalValues.TIME_TO_FRACTION_HOURS);
        }
        stats.add(new String[]{
                res.getString(R.string.average_speed),
                String.format("%.2f " + mphKph, averageSpeed)});

        if (!forNotification) {
            stats.add(new String[]{
                    res.getString(R.string.max_speed),
                    String.format(
                            "%.2f " + mphKph,
                            isImperialDisplay ? kilometersToMiles(ler.getMaxSpeedToPoint())
                                    : ler.getMaxSpeedToPoint())});
        }

        int altitude;
        if (!forNotification) {
            if (ler.getStartAltitude() == null) {
                altitude = 0;
            } else {
                altitude = Math.round(isImperialDisplay ? metersToFeet(ler.getStartAltitude()) : ler.getStartAltitude());
            }
            stats.add(new String[]{res.getString(R.string.start_altitude), String.format("%d %s", altitude, feetMeters)});
        }

        if (ler.getCurrentAltitude() == null) {
            altitude = 0;
        } else {
            altitude = Math.round(isImperialDisplay ? metersToFeet(ler.getCurrentAltitude()) : ler.getCurrentAltitude());
            if (currentlyActive) {
                stats.add(new String[]{res.getString(R.string.current_altitude), String.format("%d %s", altitude, feetMeters)});
            } else {
                stats.add(new String[]{res.getString(R.string.end_altitude), String.format("%d %s", altitude, feetMeters)});
            }
        }

        if (!forNotification) {
            if (ler.getMinAltitude() != null) {
                altitude = Math.round(isImperialDisplay ? metersToFeet(ler.getMinAltitude()) : ler.getMinAltitude());
                stats.add(new String[]{res.getString(R.string.min_altitude), String.format("%d %s", altitude, feetMeters)});

                altitude = Math.round(isImperialDisplay ? (int) metersToFeet(ler.getMaxAltitude()) : ler.getMaxAltitude());
                stats.add(new String[]{res.getString(R.string.max_altitude), String.format("%d %s", altitude, feetMeters)});

                altitude = (int) (isImperialDisplay ? (int) metersToFeet(ler.getAltitudeGained()) : ler.getAltitudeGained());
                stats.add(new String[]{
                        res.getString(R.string.altitude_gained),
                        String.format("%d " + feetMeters, altitude)});

                altitude = (int) (isImperialDisplay ? (int) metersToFeet(ler.getAltitudeLost()) : ler.getAltitudeLost());
                stats.add(new String[]{
                        res.getString(R.string.altitude_lost),
                        String.format("%d " + feetMeters, altitude)});

                altitude = (int) (isImperialDisplay ? (int) metersToFeet(ler.getAltitudeGained() - ler.getAltitudeLost())
                        : ler.getAltitudeGained() - ler.getAltitudeLost());
                stats.add(new String[]{
                        res.getString(R.string.overall_altitude_change),
                        String.format("%d " + feetMeters, altitude)});
            }
        }
    }


    public String makeFileNameReady(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9.@-]", "_");
    }

    public String removeLtGt(String someString) {
        return someString.replaceAll("[<>]", "_");
    }

    public float metersToFeet(float meters) {
        return meters * 3.2808f;
    }

    public float feetToMeters(float feet) {
        return feet / 3.2808f;
    }

    public float metersToMiles(float meters) {
        return meters * 0.00062137f;
    }

    public float kilometersToMiles(float kilometers) {
        return kilometers * 0.62137f;
    }

    //someDate must be in yyyy-mm-dd format
    public String formatDate(SimpleDateFormat dateFormat, String someDate) {
        if (someDate.equalsIgnoreCase(GlobalValues.NO_DATE)) {
            return someDate;
        }
        try {
            return DateFormat.getDateInstance().format(dateFormat.parse(someDate));
        } catch (ParseException e) {
            return someDate;
        }
    }

    // cribbed code from http://stackoverflow.com/questions/9544737/read-file-from-assets
    public String readAssetFile(Context context, String filename) {
        StringBuffer sb = new StringBuffer();
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open(filename)));
            // do reading, usually loop until end of file reading
            String mLine = reader.readLine();
            while (mLine != null) {
                sb.append(mLine);
                mLine = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            Toast.makeText(context, "Error occurred in reading file:" + filename, Toast.LENGTH_LONG).show();
        }
        return sb.toString();
    }


    // from http://stackoverflow.com/questions/428918/how-can-i-increment-a-date-by-one-day-in-java
    public Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days); //minus number would decrement the days
        return cal.getTime();
    }


    public float calcMaxYGraphValue(float maxY) {
        float maxGraphYValue = 0;
        float maxYPlus10pct;
        int maxLeftNumber;
        int rightNumber;

        maxYPlus10pct = maxY * 1.1f;

        if (maxYPlus10pct <= 10) {
            maxGraphYValue = 10;

        } else if (maxYPlus10pct <= 100) {
            maxLeftNumber = (int) (maxYPlus10pct / 10f);
            rightNumber = (int) (maxYPlus10pct - ((int) (maxYPlus10pct / 10f)) * 10f);
            if (rightNumber > 5) {
                maxGraphYValue = (maxLeftNumber + 1) * 10f;
            } else {
                maxGraphYValue = (maxLeftNumber * 10f) + 5;
            }

        } else if (maxYPlus10pct <= 1000) {
            // maxYPlus10pct  eg. 125   and   870
            // maxLeftNumber = 1  and 8
            maxLeftNumber = (int) (maxYPlus10pct / 100f);
            // rightNumber = 25  and 70
            rightNumber = (int) (maxYPlus10pct - ((int) (maxYPlus10pct / 100f)) * 100f);
            if (rightNumber > 50) {
                //maxGraphYValue =  900
                maxGraphYValue = (maxLeftNumber + 1) * 100f;
            } else {
                // maxGraphYValue = 150
                maxGraphYValue = (maxLeftNumber * 100f) + 50;
            }

        } else if (maxYPlus10pct <= 10000) {
            // maxYPlus10pct  eg. 1250   and   8700
            // maxLeftNumber = 1  and 8
            maxLeftNumber = (int) (maxYPlus10pct / 1000f);
            // rightNumber = 250   and 700
            rightNumber = (int) (maxYPlus10pct - ((int) (maxYPlus10pct / 1000f)) * 1000f);
            if (rightNumber > 500) {
                //maxGraphYValue =  9000
                maxGraphYValue = (maxLeftNumber + 1) * 1000f;
            } else {
                // maxGraphYValue = 1500
                maxGraphYValue = (maxLeftNumber * 1000f) + 500;
            }

        }
        return maxGraphYValue;
    }

    /**
     * @param metersTraveled    meters traveled between two points
     * @param imperialDisplay   true convert metersTraveled to miles and compare to distancePerMarker,
     *                          false leave metersTraveled as is
     * @param distancePerMarker distance to cover for placing a marker
     * @return true if distance covered >= distancePerMarker else false
     */
    public boolean coveredDistanceForMarker(float metersTraveled, boolean imperialDisplay, int distancePerMarker) {
        return distancePerMarker <= calcDisplayDistance(metersTraveled, imperialDisplay);
    }

    public int calcDisplayDistance(float distanceInMeters, boolean imperialDisplay) {
        if (imperialDisplay) {
            // convert meters to miles
            return (int) (distanceInMeters * 3.28084) / 5280;
        } else {
            // convert meters to kilometers
            return (int) distanceInMeters / 1000;
        }
    }
}
