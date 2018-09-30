package net.openhft.chronicle.ticker;

import org.junit.Test;

public class NanoTickerTest {
    static long blackHole;

    @Test
    public void perfTest() {
        long start = System.nanoTime();
        long end = start + 2_000_000_000;
        int batch = 1000;
        long tests = 0;
        Ticker instance = NanoTicker.INSTANCE;
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