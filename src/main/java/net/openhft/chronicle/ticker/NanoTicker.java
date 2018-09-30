package net.openhft.chronicle.ticker;


public class NanoTicker implements Ticker {
    public static final NanoTicker INSTANCE = new NanoTicker();
    private long delta = 0;

    @Override
    public long count() {
        return System.nanoTime();
    }

    @Override
    public long countPerSecond() {
        return 1_000_000_000L;
    }

    @Override
    public long countFromEpoch() {
        long nowNS = System.nanoTime();
        long nowMS = System.currentTimeMillis() * 1000000;
        long estimate = nowNS + delta;

        if (estimate < nowMS) {
            delta = nowMS - nowNS;
            return nowMS;

        } else if (estimate > nowMS + 1000000) {
            nowMS += 1000000;
            delta = nowMS - nowNS;
            return nowMS;
        }
        return estimate;
    }
}
