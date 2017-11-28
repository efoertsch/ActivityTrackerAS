package com.fisincorporated.exercisetracker.utility;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.database.LocationExerciseRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Utility {


    public static void formatActivityStats(FragmentActivity activity, ArrayList<String[]> stats,
                                           LocationExerciseRecord ler, String imperialMetric, String imperial, String feetMeters, String milesKm, String mphKph) {
        stats.clear();

        float distance = (imperialMetric.equals(imperial) ? Utility
                .metersToMiles((float) ler.getDistance()) : ((float) ler
                .getDistance()) / 1000f);
        stats.add(new String[]{
                activity.getResources().getString(R.string.distance_traveled),
                String.format("%.2f " + milesKm, distance)});

        long elapsed = ler.getEndTimestamp().getTime()
                - ler.getStartTimestamp().getTime();
        int diffHours = (int) (elapsed / GlobalValues.TIME_TO_FRACTION_HOURS);
        // find remaining minutes
        float diffMinutes = (elapsed - (diffHours * GlobalValues.TIME_TO_FRACTION_HOURS))
                / GlobalValues.TIME_TO_MINUTES;
        stats.add(new String[]{
                activity.getResources().getString(R.string.time_on_activity),
                String.format("%d Hrs  %.1f Mins", diffHours, diffMinutes)});

        // speeds in kph or mph per distance calc above
        float averageSpeed = 0;
        if (elapsed > 0) {
            averageSpeed = (distance)
                    / (elapsed / GlobalValues.TIME_TO_FRACTION_HOURS);
        }
        stats.add(new String[]{
                activity.getResources().getString(R.string.average_speed),
                String.format("%.2f " + mphKph, averageSpeed)});

        stats.add(new String[]{
                activity.getResources().getString(R.string.max_speed),
                String.format(
                        "%.2f " + mphKph,
                        imperialMetric.equals(imperial) ? Utility
                                .kilometersToMiles(ler.getMaxSpeedToPoint())
                                : ler.getMaxSpeedToPoint())});

        int altitude = (int) (imperialMetric.equals(imperial) ? (int) Utility
                .metersToFeet(ler.getAltitudeGained()) : ler.getAltitudeGained());
        stats.add(new String[]{
                activity.getResources().getString(R.string.altitude_gained),
                String.format("%d " + feetMeters, altitude)});

        altitude = (int) (imperialMetric.equals(imperial) ? (int) Utility
                .metersToFeet(ler.getAltitudeLost()) : ler.getAltitudeLost());
        stats.add(new String[]{
                activity.getResources().getString(R.string.altitude_lost),
                String.format("%d " + feetMeters, altitude)});

        altitude = (int) (imperialMetric.equals(imperial) ? (int) Utility
                .metersToFeet(ler.getAltitudeGained() - ler.getAltitudeLost())
                : ler.getAltitudeGained() - ler.getAltitudeLost());
        stats.add(new String[]{
                activity.getResources().getString(R.string.overall_altitude_change),
                String.format("%d " + feetMeters, altitude)});

    }

    public static void formatActivityStatsForFacebook(FragmentActivity activity, StringBuilder sb,
                                                      LocationExerciseRecord ler, String imperialMetric, String imperial, String feetMeters, String milesKm, String mphKph) {
        sb.setLength(0);
        String lineSeparator = System.getProperty("line.separator");

        float distance = (imperialMetric.equals(imperial) ? Utility
                .metersToMiles((float) ler.getDistance()) : ((float) ler
                .getDistance()) / 1000f);
        sb.append(activity.getResources().getString(R.string.distance_traveled) + " : " +
                String.format("%.2f " + milesKm, distance) + lineSeparator);

        long elapsed = ler.getEndTimestamp().getTime()
                - ler.getStartTimestamp().getTime();
        int diffHours = (int) (elapsed / GlobalValues.TIME_TO_FRACTION_HOURS);
        // find remaining minutes
        float diffMinutes = (elapsed - (diffHours * GlobalValues.TIME_TO_FRACTION_HOURS))
                / GlobalValues.TIME_TO_MINUTES;
        sb.append(activity.getResources().getString(R.string.time_on_activity) + " : " +
                String.format("%d Hrs  %.1f Mins", diffHours, diffMinutes) + lineSeparator);

        // speeds in kph or mph per distance calc above
        float averageSpeed = 0;
        if (elapsed > 0) {
            averageSpeed = (distance)
                    / (elapsed / GlobalValues.TIME_TO_FRACTION_HOURS);
        }
        sb.append(activity.getResources().getString(R.string.average_speed) + " : " +
                String.format("%.2f " + mphKph, averageSpeed) + lineSeparator);

        sb.append(activity.getResources().getString(R.string.max_speed) + " : " +
                String.format(
                        "%.2f " + mphKph,
                        imperialMetric.equals(imperial) ? Utility
                                .kilometersToMiles(ler.getMaxSpeedToPoint())
                                : ler.getMaxSpeedToPoint()) + lineSeparator);

        int altitude = (int) (imperialMetric.equals(imperial) ? (int) Utility
                .metersToFeet(ler.getAltitudeGained()) : ler.getAltitudeGained());
        sb.append(
                activity.getResources().getString(R.string.altitude_gained) + " : " +
                        String.format("%d " + feetMeters, altitude) + lineSeparator);

        altitude = (int) (imperialMetric.equals(imperial) ? (int) Utility
                .metersToFeet(ler.getAltitudeLost()) : ler.getAltitudeLost());
        sb.append(
                activity.getResources().getString(R.string.altitude_lost) + " : " +
                        String.format("%d " + feetMeters, altitude) + lineSeparator);

        altitude = (int) (imperialMetric.equals(imperial) ? (int) Utility
                .metersToFeet(ler.getAltitudeGained() - ler.getAltitudeLost())
                : ler.getAltitudeGained() - ler.getAltitudeLost());
        sb.append(activity.getResources().getString(R.string.overall_altitude_change) + " : " +
                String.format("%d " + feetMeters, altitude) + lineSeparator);

    }

    public static String makeFileNameReady(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9.@-]", "_");
    }

    public static String removeLtGt(String someString) {
        return someString.replaceAll("[<>]", "_");
    }

    public static float metersToFeet(float meters) {
        return meters * 3.2808f;
    }

    public static float feetToMeters(float feet) {
        return feet / 3.2808f;
    }

    public static float metersToMiles(float meters) {
        return meters * 0.00062137f;
    }

    public static float kilometersToMiles(float kilometers) {
        return kilometers * 0.62137f;
    }

    //someDate must be in yyyy-mm-dd format
    public static String formatDate(SimpleDateFormat dateFormat, String someDate) {
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
    public static String readAssetFile(Context context, String filename) {
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
    public static Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days); //minus number would decrement the days
        return cal.getTime();
    }


    public static float calcMaxYGraphValue(float maxY) {
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

    public static boolean coveredDistanceForMarker(float metersTraveled, boolean imperialDisplay, int distancePerMarker ){
        if (imperialDisplay) {
            // convert meters to miles then compare to pin marker distance
            return distancePerMarker <= calcDisplayDistance(metersTraveled,imperialDisplay);
        } else {
            // convert meters to kilometers then compare
            return distancePerMarker <= calcDisplayDistance(metersTraveled,imperialDisplay);
        }
    }

    public static int calcDisplayDistance(float distanceInMeters, boolean imperialDisplay) {
        if (imperialDisplay) {
            // convert meters to miles
            return (int) (distanceInMeters * 3.28084) / 5280;
        } else {
            // convert meters to kilometers
            return (int) distanceInMeters / 1000;
        }
    }
}
