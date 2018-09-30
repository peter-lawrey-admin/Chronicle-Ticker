package net.openhft.chronicle.ticker;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class RdtscTickerTest {
    @Test
    public void count() {
        assumeTrue(RdtscTicker.LOADED);
        Ticker instance = RdtscTicker.INSTANCE;
        long a = instance.count();
        for (int i = 0; i < 1000; i++) {
            long b = instance.count();
            assertTrue(b > a);
            a = b;
        }
    }

    @Test
    public void countPerSecond() {
        assumeTrue(RdtscTicker.LOADED);
        Ticker instance = RdtscTicker.INSTANCE;
        assertTrue(instance.countPerSecond() >= 1_000_000_000L);
        assertTrue(instance.countPerSecond() < 10_000_000_000L);

    }

    @Test
    public void countFromEpoch() {
        assumeTrue(RdtscTicker.LOADED);
        assumeTrue(System.getProperty("os.name").endsWith("nux"));
    }

    static long blackHole;

    @Test
    public void perfTest() {
        long start = System.nanoTime();
        long end = start + 2_000_000_000;
        int batch = 1000;
        long tests = 0;
        Ticker instance = RdtscTicker.INSTANCE;
        do {
            for (int i = 0; i < batch; i++)
                blackHole = instance.count();
            tests += batch;
        } while (System.nanoTime() < end);
        long time = System.nanoTime() - start;
        double avgTime = (double) time / tests;
        System.out.printf(instance.getClass().getSimpleName()
                + ": Average time to call count() %.1f ns%n", avgTime);
    }
}