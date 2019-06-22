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

import org.junit.Assert;

public interface BaseTickerTest {
    /**
     * Test driver
     */
    void perfTest();

    default void driver( Ticker instance )
    {
        long start = System.nanoTime();
        long end = start + 2_000_000_000;
        int batch = 1000;
        long tests = 0;
        long blackHole = 0;
        do {
            for (int i = 0; i < batch; i++)
                blackHole = instance.count();
            tests += batch;
        } while (System.nanoTime() < end);
        long time = System.nanoTime() - start;
        double avgTime = (double) time / tests;
        System.out.printf(instance.getClass().getSimpleName()
                + ": Average time to call count() %.1f ns%n", avgTime);
        System.out.println( "count (now) = " + instance.count() );
        System.out.println( "countPerSecond = " + instance.countPerSecond() );
        System.out.println( "countFromEpoch = " + instance.countFromEpoch() );
        System.out.println( "now (count since epoch) = " + ((long)instance.count() + (long)instance.countFromEpoch()) );
        System.out.println( "Estimated CPU frequency = " + 1000*NativeTime.ticksPerNanosecond() + "MHz");
    }

    default void checkEpochTime(Ticker instance) {
        long epochOffset = instance.countFromEpoch();
        long epoch = instance.count() + epochOffset;
        long epochMillis = epoch / (instance.countPerSecond() / 1000);
        long diffMillis = epochMillis - System.currentTimeMillis();
        Assert.assertTrue(Math.abs(diffMillis) < 1000);
    }
}
