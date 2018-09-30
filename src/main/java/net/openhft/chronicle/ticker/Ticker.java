package net.openhft.chronicle.ticker;

public interface Ticker {
    /**
     * @return the number of ticks
     */
    long count();

    /**
     * @return the number of ticks in a second.
     */
    long countPerSecond();

    /**
     * The number of ticks to add to get epoch time, or an approximation of wall time.
     *
     * @return the number ticks offset from epoch
     */
    long countFromEpoch();
}
