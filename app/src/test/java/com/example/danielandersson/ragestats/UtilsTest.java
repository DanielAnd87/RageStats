package com.example.danielandersson.ragestats;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

import static junit.framework.Assert.assertEquals;

/**
 *
 * Created by danielandersson on 2017-09-13.
 */
public class UtilsTest {

    private long mTodayTimestamp;

    @Before
    public void setUp() throws Exception {
        mTodayTimestamp = 1505301615;
    }

    @Test
    public void getDayStartTimestamp() throws Exception {
        assertEquals(mTodayTimestamp < Utils.getCurrentTimestamp(), true);
    }


    @Test
    public void mockIsAfterYesterday() throws Exception {
        assertEquals(Utils.getDayStartTimestamp(Calendar.getInstance()) < Utils.getCurrentTimestamp(), true);
        assertEquals(Utils.getDayStartTimestamp(Calendar.getInstance()) < mTodayTimestamp, true);
    }
    @Test
    public void mockIsBeforeTomorrow() throws Exception {
        assertEquals(Utils.getDayEndTimestamp(Calendar.getInstance()) > Utils.getCurrentTimestamp(), true);
        assertEquals(Utils.getDayEndTimestamp(Calendar.getInstance()) > mTodayTimestamp, true);
    }

    @Test
    public void tomorrowIsAfterToday() throws Exception {
        final Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.
                DATE, 1);

        assertEquals(Utils.getCurrentTimestamp() < Utils.getDayStartTimestamp(tomorrow), true);
    }



}