package net.openhft.chronicle.ticker;

import org.junit.Test;

public class CpuidRdtscTickerTest implements BaseTickerTest {

    @Test
    public void perfTest() {
        driver(CpuidRdtscTicker.INSTANCE);
    }

}