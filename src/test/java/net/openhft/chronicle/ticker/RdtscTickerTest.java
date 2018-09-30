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
}