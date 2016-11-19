package pl.edu.agh.pp.utils;

import org.junit.Test;

import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Krzysztof Węgrzyński on 2016-11-12.
 */
public class TimerTest {
    private final long MAX_DAY_DELAY = 250_000 + 18_000;
    private final long MAX_NIGHT_DELAY = 600_000 + 30_000;

    @Test
    public void getInstance() throws Exception {
        assertNotNull(Timer.getInstance());
    }

    public double mean(List<Double> m) {
        double sum = 0;
        for (int i = 0; i < m.size(); i++) {
            sum += m.get(i);
        }
        return sum / m.size();
    }

    @Test
    public void getWaitingTime() throws Exception {
        Timer timer = Timer.getInstance();
        ConsoleOutputCapturer consoleOutputCapturer = new ConsoleOutputCapturer();
        Calendar testCalendar = Calendar.getInstance();

        testCalendar.set(2016, 1, 2, 15, 1);
        consoleOutputCapturer.start();
        timer.getWaitingTime(testCalendar);
        assertEquals("DAY SHIFT-----------------------------------------------\r\n", consoleOutputCapturer.stop());


        testCalendar.set(2016, 1, 2, 22, 59, 59);
        consoleOutputCapturer.start();
        timer.getWaitingTime(testCalendar);
        assertEquals("DAY SHIFT-----------------------------------------------\r\n", consoleOutputCapturer.stop());


        testCalendar.set(2016, 1, 2, 23, 0, 0);
        consoleOutputCapturer.start();
        timer.getWaitingTime(testCalendar);
        assertEquals("NIGHT SHIFT----------------------------------------------\r\n", consoleOutputCapturer.stop());


        testCalendar.set(2016, 1, 3, 2, 0, 0);
        consoleOutputCapturer.start();
        timer.getWaitingTime(testCalendar);
        assertEquals("NIGHT SHIFT----------------------------------------------\r\n", consoleOutputCapturer.stop());


        testCalendar.set(2016, 1, 3, 2, 22, 48);
        consoleOutputCapturer.start();
        timer.getWaitingTime(testCalendar);
        assertEquals("NIGHT SHIFT----------------------------------------------\r\n", consoleOutputCapturer.stop());

        testCalendar.set(2016, 1, 3, 4, 59);
        consoleOutputCapturer.start();
        timer.getWaitingTime(testCalendar);
        assertEquals("NIGHT SHIFT----------------------------------------------\r\n", consoleOutputCapturer.stop());

        testCalendar.set(2016, 1, 3, 5, 00, 00);
        consoleOutputCapturer.start();
        timer.getWaitingTime(testCalendar);
        assertEquals("NIGHT SHIFT----------------------------------------------\r\n", consoleOutputCapturer.stop());

        testCalendar.set(2016, 1, 3, 5, 00, 01);
        consoleOutputCapturer.start();
        timer.getWaitingTime(testCalendar);
        assertEquals("DAY SHIFT-----------------------------------------------\r\n", consoleOutputCapturer.stop());
    }

}