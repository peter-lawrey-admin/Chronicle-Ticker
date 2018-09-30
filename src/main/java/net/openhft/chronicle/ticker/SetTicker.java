package net.openhft.chronicle.ticker;

public class SetTicker implements Ticker {
    private long count = 0;
    private long countPerSecond = 1000;
    private long countFromEpoch = 0;

    @Override
    public long count() {
        return count;
    }

    @Override
    public long countPerSecond() {
        return countPerSecond;
    }

    @Override
    public long countFromEpoch() {
        return countFromEpoch;
    }

    public SetTicker count(long count) {
        this.count = count;
        return this;
    }

    public SetTicker countPerSecond(long countPerSecond) {
        this.countPerSecond = countPerSecond;
        return this;
    }

    public SetTicker countFromEpoch(long countFromEpoch) {
        this.countFromEpoch = countFromEpoch;
        return this;
    }
}
