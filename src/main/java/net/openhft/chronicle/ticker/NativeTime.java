/*
 * Copyright 2016 higherfrequencytrading.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.openhft.chronicle.ticker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * JNI-based implementation, trying to use rdtsc() system call
 * to access the most precise timer available
 *
 * @author cheremin
 * @author plawrey
 * @author rogersimmons
 */

public class NativeTime {
    public static final boolean LOADED;
    private static final Logger LOGGER = LoggerFactory.getLogger(NativeTime.class);
    private static double ticks_per_nanosecond = 1.0;

    static {
        boolean loaded = false;
        try {
            try {
                URL url = NativeTime.class.getClassLoader().getResource("libnativetime.so");
                if (url != null) {
                    System.load(url.getFile());
                    loaded = true;
                }
            } catch(Exception e){
                // ignored.
            }
            if (!loaded)
                System.loadLibrary("nativetime");

            // initial calibration
            calibrate(10);

            // background thread for finer-grained calibration
            Thread t = new Thread( ()->{ calibrate(1000);});
            t.setDaemon(true);
            t.start();

            loaded = true;
        } catch (UnsatisfiedLinkError ule) {
            LOGGER.debug("Unable to find libticker in [" + System.getProperty("java.library.path") + "] " + ule);
            loaded = false;
        }

        LOADED=loaded;
    }

    /**
     * Spin for given number of ms to calibrate ticks per nanosecond
     * @param ms = time in milliseconds to spin
     */
    private static void calibrate( int ms )
    {
        long ticks1 = cpuid_rdtsc();
        long nanos1 = System.nanoTime();

        // spin for 10ms
        long end = nanos1 + ms*1000000L;
        while( System.nanoTime() < end );

        long ticks2 = cpuid_rdtsc();
        long nanos2 = System.nanoTime();

        ticks_per_nanosecond = ((double)(ticks2-ticks1))/(nanos2-nanos1);
    }

    public static double ticksPerNanosecond() { return ticks_per_nanosecond; }

    public native static long rdtsc();
    public native static long cpuid_rdtsc();
    public native static long rdtscp();
    public native static long clocknanos();

}
