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

import java.net.URL;
import java.util.logging.Logger;

/**
 * JNI-based implementation, trying to use rdtsc() system call
 * to access the most precise timer available
 *
 * @author cheremin
 * @author plawrey
 */
public enum RdtscTicker implements Ticker {
    INSTANCE;

    public static final boolean LOADED;
    private static final Logger LOGGER = Logger.getLogger(RdtscTicker.class.getName());
    private static long cpuFrequency = 1000;
    private static long epochOffset;

    static {
        boolean loaded = false;
        try {
            try {
                URL url = RdtscTicker.class.getClassLoader().getResource("libticker.so");
                if (url != null) {
                    System.load(url.getFile());
                    loaded = true;
                }
            } catch (Exception e) {
                // ignored.
            }
            if (!loaded)
                System.loadLibrary("ticker");

            estimateFrequency(25);
            estimateFrequency(100);
            Thread t = new Thread(() -> {
                estimateFrequency(1000);
                LOGGER.info("Estimated clock frequency was " + cpuFrequency + " Hz");
            });
            t.setDaemon(true);
            t.start();
            rdtsc0();
            loaded = true;
        } catch (UnsatisfiedLinkError ule) {
            LOGGER.fine("Unable to find libCEInternals in [" + System.getProperty("java.library.path") + "] " + ule);
            loaded = false;
        }
        LOADED = loaded;
    }

    private static void estimateFrequency(int factor) {
        final long start = System.nanoTime();
        long now;
        while ((now = System.nanoTime()) == start) {
        }

        long end = start + factor * 1000000L;
        final long start0 = rdtsc0();
        while ((now = System.nanoTime()) < end) {
        }
        long end0 = rdtsc0();
        end = now;

        cpuFrequency = (end0 - start0 + 1) * 1_000_000_000L / (end - start);
        epochOffset = System.currentTimeMillis() * cpuFrequency - rdtsc0();
    }

    native static long rdtsc0();

    @Override
    public long count() {
        return rdtsc0();
    }

    @Override
    public long countPerSecond() {
        return cpuFrequency;
    }

    @Override
    public long countFromEpoch() {
        long epochOffset0 = System.currentTimeMillis() * cpuFrequency - rdtsc0();
        epochOffset += (epochOffset0 - epochOffset) / 16;
        return epochOffset;
    }
}