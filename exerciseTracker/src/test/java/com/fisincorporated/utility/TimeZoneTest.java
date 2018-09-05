package com.fisincorporated.utility;

import com.fisincorporated.exercisetracker.utility.TimeZoneUtils;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class TimeZoneTest {

    TimeZoneUtils timeZoneUtils;

    @Before
    public void setup() {
        timeZoneUtils = new TimeZoneUtils();
    }

    @Test
    public void testTimeZoneName() throws Exception {
        String timezone = timeZoneUtils.getDeviceTimeZone();
        System.out.println(timezone);
        assertEquals("Not in EST, expected", "EST", timezone);
    }

    @Test
    public void gmtHourOffsetShouldBeMinus4() {
        int hourOffset = timeZoneUtils.getGmtHourOffset();
        System.out.println("Hour offset:" + hourOffset);
        assertEquals("DST Hour offset incorrect", -4, hourOffset);

    }

    @Test
    public void gmtMinuteOffsetShouldBe0() {
        int minuteOffset = timeZoneUtils.getGmtMinuteOffest();
        System.out.println("Minute offset:" + minuteOffset);
        assertEquals("DST minute offset incorrect", 0, minuteOffset);
    }
}
