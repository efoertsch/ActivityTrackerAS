package com.fisincorporated.exercisetracker.utility;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.inject.Inject;


/**
 * Get timezone (eg. 'EST'), and GMT hour and minute offset
 * Note this these are determined first time this class is instantiated
 */
public class TimeZoneUtils {
    TimeZone timeZone;
    Calendar calendar;
    int offsetInMillis;

    @Inject
    public TimeZoneUtils() {
        timeZone = TimeZone.getDefault();
        calendar = GregorianCalendar.getInstance(timeZone);
        offsetInMillis = timeZone.getOffset(calendar.getTimeInMillis());
    }

    public String getDeviceTimeZone() {
        return (TimeZone.getTimeZone(timeZone.getID()).getDisplayName(
                false, TimeZone.SHORT));
    }

    public int getGmtHourOffset() {
        return  offsetInMillis / 3600000;
    }

    public int getGmtMinuteOffest() {
        return (offsetInMillis / 60000) % 60;
    }
}
