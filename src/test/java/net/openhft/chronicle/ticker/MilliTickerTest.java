package net.openhft.chronicle.ticker;

import org.junit.Test;

public class MilliTickerTest implements BaseTickerTest {

    @Test
    public void perfTest() {
        driver(MilliTicker.INSTANCE);
    }

}