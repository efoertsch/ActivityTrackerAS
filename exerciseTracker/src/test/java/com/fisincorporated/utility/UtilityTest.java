package com.fisincorporated.utility;

import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Created by ericfoertsch on 12/13/15.
 */
public class UtilityTest {

    private Utility mUtility;
    @Before
    public void setUp() throws Exception {
        mUtility = new Utility();
    }

    @Test
    public void testMetersToFeet() throws Exception {
        float actual = Utility.metersToFeet(1);

        float expected = 3.3f;
        // use this method because float is not precise
        assertEquals("Conversion from meters to feet failed", expected,
                actual,  0.02);
    }


    @Test
    public void testFeetToMeters() throws Exception {
        float actual = Utility.feetToMeters(33);

        float expected = 10.0f;
        // use this method because float is not precise
        assertEquals("Conversion from feet to meters failed", expected,
                actual,  0.06);
    }

    @Test
    public void testMetersToMiles() throws Exception {
        float actual = Utility.metersToMiles(1000);
        float expected = .621f;
        // use this method because float is not precise
        assertEquals("Conversion from meters to miles failed", expected,
                actual,  0.0004);
    }

    @Test
    public void testKilometersToMiles() throws Exception {
        float actual = Utility.kilometersToMiles(1.0f);
        float expected = .62f;
        // use this method because float is not precise
        assertEquals("Conversion from kilometers to miles failed", expected,
                actual,  0.01);
    }

    @Test
    public void testFormatDate() throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        assertEquals("formatDate failed ", Utility.formatDate(dateFormat, "2015-12-13"), "Dec 13, 2015");

    }

    @Test
    public void testAddDays() throws Exception {
        assertEquals("testAddDays failed",Utility.addDays(new Date(2015,12,14), 1 ), new Date(2015,12,15) );

    }
}