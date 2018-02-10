package com.fisincorporated.utility;

import com.fisincorporated.exercisetracker.utility.StatsUtil;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class StatsUtilTest {

//    private StatsUtil mUtility;
//    @Before
//    public void setUp() throws Exception {
//        mUtility = new StatsUtil();
//    }

    @Test
    public void testMetersToFeet() throws Exception {
        float actual = StatsUtil.metersToFeet(1);

        float expected = 3.3f;
        // use this method because float is not precise
        assertEquals("Conversion from meters to feet failed", expected,
                actual, 0.02);
    }


    @Test
    public void testFeetToMeters() throws Exception {
        float actual = StatsUtil.feetToMeters(33);

        float expected = 10.0f;
        // use this method because float is not precise
        assertEquals("Conversion from feet to meters failed", expected,
                actual, 0.06);
    }

    @Test
    public void testMetersToMiles() throws Exception {
        float actual = StatsUtil.metersToMiles(1000);
        float expected = .621f;
        // use this method because float is not precise
        assertEquals("Conversion from meters to miles failed", expected,
                actual, 0.0004);
    }

    @Test
    public void testKilometersToMiles() throws Exception {
        float actual = StatsUtil.kilometersToMiles(1.0f);
        float expected = .62f;
        // use this method because float is not precise
        assertEquals("Conversion from kilometers to miles failed", expected,
                actual, 0.01);
    }

    @Test
    public void testFormatDate() throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        assertEquals("formatDate failed ", StatsUtil.formatDate(dateFormat, "2015-12-13"), "Dec 13, 2015");

    }

    @Test
    public void testAddDays() throws Exception {
        assertEquals("testAddDays failed", StatsUtil.addDays(new Date(2015, 12, 14), 1), new Date(2015, 12, 15));

    }

    @Test
    public void testCalcMarkerLessThan1Mile() throws Exception {
        // 1609 meters  < 1 mile
        assertEquals("testCalcMarkerDistance failed. 1609 meters < 1 mi", StatsUtil.coveredDistanceForMarker(1609, true, 1), false);
    }

    @Test
    public void testCalcMarker1Mile() throws Exception {
        // 1610 meters just over 1 mile
        assertEquals("testCalcMarkerDistance failed. 1610 meters = 1 mi", StatsUtil.coveredDistanceForMarker(1610, true, 1), true);
    }

    @Test
    public void testCalcMarkerOver1Mile() throws Exception {
        // 1620 meters   over 1 mile
        assertEquals("testCalcMarkerDistance failed. 1610 meters = 1 mi", StatsUtil.coveredDistanceForMarker(1620, true, 1), true);

    }

    @Test
    public void testCalcMarkerUnder2Mile() throws Exception {
        // 3218 meters  < 2 mile
        assertEquals("testCalcMarkerDistance failed. 3218 meters < 2 mi", StatsUtil.coveredDistanceForMarker(3218, true, 2), false);

    }

    @Test
    public void testCalcMarkerOver2Mile() throws Exception {
        // 3240 meters  > 2 mile
        assertEquals("testCalcMarkerDistance failed. 3240 meters > 2 mi", StatsUtil.coveredDistanceForMarker(3240, true, 2), true);

    }

    @Test
    public void testCalcMarkerUnder1KmDistance() throws Exception {
        // 999 meters   under 1 kilometer
        assertEquals("testCalcMarkerDistance failed. 999 meters < 1 km", StatsUtil.coveredDistanceForMarker(999, false, 1), false);

    }

    @Test
    public void testCalcMarker1KmDistance() throws Exception {
        // 1000 meters     1 kilometer
        assertEquals("testCalcMarkerDistance failed. 1000 meters = 1 km", StatsUtil.coveredDistanceForMarker(1000, false, 1), true);
    }

    @Test
    public void testCalcMarkerOver1KmDistance() throws Exception {
        // 1001 meters  over 1 kilometer
        assertEquals("testCalcMarkerDistance failed. 1001 meters > 1 km", StatsUtil.coveredDistanceForMarker(1001, false, 1), true);
    }

    @Test
    public void testCalcMarkerOver2KmDistance() throws Exception {
        // 2001 meters  over 2 kilometer
        assertEquals("testCalcMarkerDistance failed. 2001 meters > 2 km", StatsUtil.coveredDistanceForMarker(2001, false, 2), true);

    }
}