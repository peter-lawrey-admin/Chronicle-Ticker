package net.openhft.chronicle.ticker;

public class MicroTicker implements Ticker {
    @Override
    public long count() {
        return NanoTicker.INSTANCE.count() / 1000;
    }

    @Override
    public long countPerSecond() {
        return 1_000_000;
    }

    @Override
    public long countFromEpoch() {
        return NanoTicker.INSTANCE.countFromEpoch() / 1000;
    }
}
