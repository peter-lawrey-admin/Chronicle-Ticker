package net.openhft.chronicle.ticker;

import org.junit.Test;

public class MicroTickerTest implements BaseTickerTest {

    @Test
    public void perfTest() {
        driver(MicroTicker.INSTANCE);
    }

}