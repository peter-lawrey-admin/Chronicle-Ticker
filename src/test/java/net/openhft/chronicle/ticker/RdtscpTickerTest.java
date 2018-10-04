package net.openhft.chronicle.ticker;

import org.junit.Test;

public class RdtscpTickerTest implements BaseTickerTest {

    @Test
    public void perfTest() {
        driver(RdtscpTicker.INSTANCE);
    }

}
