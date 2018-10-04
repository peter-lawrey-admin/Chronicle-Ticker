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

public class MicroTicker implements Ticker {
    public static final MicroTicker INSTANCE = new MicroTicker();

    @Override
    public long count() {
        return NanoTicker.INSTANCE.count() / 1000;
    }

    @Override
    public long countPerSecond() {
        return 1_000_000;
    }

    @Override
    public long countFromEpoch() {
        return NanoTicker.INSTANCE.countFromEpoch() / 1000;
    }
}
