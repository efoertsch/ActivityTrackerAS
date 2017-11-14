package com.fisincorporated.utility;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GraphScaleTest {

    float maxY;
    float maxGraphYValue;

    @Test
    public void yAxisValue1To10() throws Exception {

        maxY = 1f;
        maxGraphYValue = Utility.calcMaxYGraphValue(maxY);
        assertEquals("Calc of maxY graph scale value failed", 10f,
                maxGraphYValue, 0);
    }

    @Test
    public void yAxisValue6To10() throws Exception {

        maxY = 6f;
        maxGraphYValue = Utility.calcMaxYGraphValue(maxY);
        assertEquals("Calc of maxY graph scale value failed", 10f,
                maxGraphYValue, 0);
    }

    @Test
    public void yAxisValue12To15() throws Exception {
        maxY = 12f;
        maxGraphYValue = Utility.calcMaxYGraphValue(maxY);
        assertEquals("Calc of maxY graph scale value failed", 15f,
                maxGraphYValue, 0);

    }

    @Test
    public void yAxisValue16To20() throws Exception {
        maxY = 16f;
        maxGraphYValue = Utility.calcMaxYGraphValue(maxY);
        assertEquals("Calc of maxY graph scale value failed", 20f,
                maxGraphYValue, 0);

    }

    @Test
    public void yAxisValue110To150() throws Exception {

        maxY = 110f;
        maxGraphYValue = Utility.calcMaxYGraphValue(maxY);
        assertEquals("Calc of maxY graph scale value failed", 150f,
                maxGraphYValue, 0);

    }

    @Test
    public void yAxisValue805To900() throws Exception {
        maxY = 805f;
        maxGraphYValue = Utility.calcMaxYGraphValue(maxY);
        assertEquals("Calc of maxY graph scale value failed", 900f,
                maxGraphYValue, 0);

    }

    @Test
    public void yAxisValue1150To1500() throws Exception {
        maxY = 1150f;
        maxGraphYValue = Utility.calcMaxYGraphValue(maxY);
        assertEquals("Calc of maxY graph scale value failed", 1500f,
                maxGraphYValue, 0);

    }

    @Test
    public void yAxisValue8050To9000() throws Exception {
        maxY = 8050f;
        maxGraphYValue = Utility.calcMaxYGraphValue(maxY);
        assertEquals("Calc of maxY graph scale value failed", 9000f,
                maxGraphYValue, 0);
    }


}
