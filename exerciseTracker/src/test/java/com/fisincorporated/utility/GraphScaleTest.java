package com.fisincorporated.utility;

import com.fisincorporated.exercisetracker.utility.StatsUtil;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GraphScaleTest {

    float maxY;
    float maxGraphYValue;

    private StatsUtil statsUtil;

    @Before
    public void createStatsUtil(){
        statsUtil =   statsUtil.getInstance();
    }

    @Test
    public void yAxisValue1To10() throws Exception {

        maxY = 1f;
        maxGraphYValue = statsUtil.calcMaxYGraphValue(maxY);
        assertEquals("Calc of maxY graph scale value failed", 10f,
                maxGraphYValue, 0);
    }

    @Test
    public void yAxisValue6To10() throws Exception {

        maxY = 6f;
        maxGraphYValue = statsUtil.calcMaxYGraphValue(maxY);
        assertEquals("Calc of maxY graph scale value failed", 10f,
                maxGraphYValue, 0);
    }

    @Test
    public void yAxisValue12To15() throws Exception {
        maxY = 12f;
        maxGraphYValue = statsUtil.calcMaxYGraphValue(maxY);
        assertEquals("Calc of maxY graph scale value failed", 15f,
                maxGraphYValue, 0);

    }

    @Test
    public void yAxisValue16To20() throws Exception {
        maxY = 16f;
        maxGraphYValue = statsUtil.calcMaxYGraphValue(maxY);
        assertEquals("Calc of maxY graph scale value failed", 20f,
                maxGraphYValue, 0);

    }

    @Test
    public void yAxisValue110To150() throws Exception {

        maxY = 110f;
        maxGraphYValue = statsUtil.calcMaxYGraphValue(maxY);
        assertEquals("Calc of maxY graph scale value failed", 150f,
                maxGraphYValue, 0);

    }

    @Test
    public void yAxisValue805To900() throws Exception {
        maxY = 805f;
        maxGraphYValue = statsUtil.calcMaxYGraphValue(maxY);
        assertEquals("Calc of maxY graph scale value failed", 900f,
                maxGraphYValue, 0);

    }

    @Test
    public void yAxisValue1150To1500() throws Exception {
        maxY = 1150f;
        maxGraphYValue = statsUtil.calcMaxYGraphValue(maxY);
        assertEquals("Calc of maxY graph scale value failed", 1500f,
                maxGraphYValue, 0);

    }

    @Test
    public void yAxisValue8050To9000() throws Exception {
        maxY = 8050f;
        maxGraphYValue = statsUtil.calcMaxYGraphValue(maxY);
        assertEquals("Calc of maxY graph scale value failed", 9000f,
                maxGraphYValue, 0);
    }


}
