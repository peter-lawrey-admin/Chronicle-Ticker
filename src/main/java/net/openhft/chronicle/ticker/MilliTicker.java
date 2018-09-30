package net.openhft.chronicle.ticker;

public class MilliTicker implements Ticker {
    public static final MilliTicker INSTANCE = new MilliTicker();

    @Override
    public long count() {
        return System.currentTimeMillis();
    }

    @Override
    public long countPerSecond() {
        return 1000L;
    }

    @Override
    public long countFromEpoch() {
        return 0L;
    }
}
