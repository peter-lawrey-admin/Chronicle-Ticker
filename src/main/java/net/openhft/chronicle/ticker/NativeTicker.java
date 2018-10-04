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

public interface NativeTicker extends Ticker {

    @Override
    default public long countPerSecond()
    {
        return (long)(1000000000.0 * NativeTime.ticksPerNanosecond());
    }

    @Override
    default public long countFromEpoch()
    {
        long arbitraryTicks = count();
        long realTicks = (long)((double)NativeTime.clocknanos() * NativeTime.ticksPerNanosecond());

        return realTicks - arbitraryTicks;
    }
}