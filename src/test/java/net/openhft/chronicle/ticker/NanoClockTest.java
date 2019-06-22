package net.openhft.chronicle.ticker;

import org.junit.Assert;
import org.junit.Test;

public class NanoClockTest {

    @Test
    public void perfTest()
    {
        long start = System.nanoTime();
        long end = start + 2_000_000_000;
        int batch = 1000;
        long tests = 0;
        long blackHole = 0;
        do {
            for (int i = 0; i < batch; i++)
                blackHole = Ticker.nanoClock();
            tests += batch;
        } while (System.nanoTime() < end);
        long time = System.nanoTime() - start;
        double avgTime = (double) time / tests;
        System.out.printf("NanoClockTest: Average time to call nanoClock() %.1f ns%n", avgTime);
    }

    @Test
    public void epochTimeTest() {
        long nowMillis = Ticker.nanoClock() / 1000000;
        Assert.assertTrue(Math.abs(nowMillis - System.currentTimeMillis()) < 1000);
    }
}
