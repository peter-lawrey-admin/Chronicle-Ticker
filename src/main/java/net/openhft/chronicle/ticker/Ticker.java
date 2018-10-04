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

    /**
     * The current wall-clock nanoseconds since epoch - named analogously to System.nanoTime()
     * @return the number of nanos since the epoch
     */
    static long nanoClock()
    {
        return NativeTime.clocknanos();
    }

    static SetTicker forTesting()
    {
        return new SetTicker();
    }

    static Ticker milli() { return MilliTicker.INSTANCE; }

    static Ticker micro()
    {
        return MicroTicker.INSTANCE;
    }

    static Ticker nano()
    {
        return NanoTicker.INSTANCE;
    }

    static Ticker highResolution()
    {
        return NativeTime.LOADED ? RdtscTicker.INSTANCE : NanoTicker.INSTANCE;
    }
}
