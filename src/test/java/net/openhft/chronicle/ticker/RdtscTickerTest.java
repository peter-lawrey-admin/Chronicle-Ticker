package net.openhft.chronicle.ticker;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class RdtscTickerTest implements BaseTickerTest {

    @Test
    public void count() {
        assumeTrue(NativeTime.LOADED);
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
        assumeTrue(NativeTime.LOADED);
        Ticker instance = RdtscTicker.INSTANCE;
        assertTrue(instance.countPerSecond() >= 1_000_000_000L);
        assertTrue(instance.countPerSecond() < 10_000_000_000L);
    }

    @Test
    public void countFromEpoch() {
        assumeTrue(NativeTime.LOADED);
        assumeTrue(System.getProperty("os.name").endsWith("nux"));
    }

    @Test
    public void perfTest() {
        driver(RdtscTicker.INSTANCE);
    }
}