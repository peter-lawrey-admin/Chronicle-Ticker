/* vim: syntax=cpp
 * Copyright 2015 Higher Frequency Trading
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef NET_OPENHFT_CHRONICLE_TICKER_NATIVETIME_H
#define NET_OPENHFT_CHRONICLE_TICKER_NATIVETIME_H

#include <jni.h>
#include "net_openhft_chronicle_ticker_NativeTime.h"
#include <cstdint>

#if defined( _MSC_VER )
    #include <Windows.h>
    #include <intrin.h>
    #include <mutex>
    #include <thread>
    #include <chrono>
    #include <atomic>
#elif defined(__GNUC__)
    #include <time.h>
    #include <x86intrin.h>
#endif

namespace detail
{

#if defined(_WIN32)
/**
 *  Windows has relatively poor precision on even its most precise wall clock (GetSystemTimeAsFileTime)
 *  Similarly, the QPC/performance counter only ticks once every 300ns or so (QueryPerformanceCounter)
 *  The below combines QPC, GeSystemTimeAsFileTime, and rdtsc to provide a synthetic nanosecond resolution clock
 *  Calibrates on program start, and updates calibration every 60s via a background thread
 *  Typical call overhead is ~20ns
 */
class spinlock
{
public:
    void lock()	{ while (lock_.test_and_set(std::memory_order_acquire)); }
    void unlock() { lock_.clear(std::memory_order_release); }
private:
    std::atomic_flag lock_ = ATOMIC_FLAG_INIT;
};

class WinNanoClock
{
public:
    WinNanoClock()
    : BASELINE_COUNT(0)
    , BASELINE_NANOS(0)
    , INV_TICKS_PER_NANO(1.0)
    {
        initial_warmup_();

        std::thread t(&WinNanoClock::background_, this);
        t.detach();
    }

private:
    void initial_warmup_()
    {
        warmup_(1000); // initial coarse estimate; takes about 1us on program startup (during static initialisation)
    }

    void background_()
    {
        // background thread which performs a fine-grained resync with wall clock once a minute
        // NB: this means there can be edge cases where the nano-clock time goes backwards between 2 calls
        for (;;)
        {
            warmup_(1);
            std::this_thread::sleep_for(std::chrono::milliseconds(60000)); // resync every minute
        }
    }

    void warmup_( uint64_t factor )
    {
        // get fixed counts per second of Windows high performance counter (typically around 3+million)
        LARGE_INTEGER COUNTS_PER_SEC;
        QueryPerformanceFrequency(&COUNTS_PER_SEC);

        // calibrate Windows HPC ticks with CPU ticks
        uint64_t ticks0 = ticks_();
        LARGE_INTEGER count0;
        QueryPerformanceCounter(&count0);

        LARGE_INTEGER count1;
        do { QueryPerformanceCounter(&count1); } while (factor * (count1.QuadPart - count0.QuadPart) < (uint64_t)COUNTS_PER_SEC.QuadPart);
        uint64_t ticks1 = ticks_();

        INV_TICKS_PER_NANO = 1000000000.0 / (double)(factor * (ticks1 - ticks0));

        // get current nanosecond time, to calibrate CPU ticks with wall time
        // this seems to be the best wall clock Windows supports, but is still relatively low precision
        auto get_precise_time = []()->uint64_t
        {
            constexpr uint64_t HNS_PER_SEC = 10000000ULL; // 100ns per second
            constexpr uint64_t NS_PER_HNS = 100ULL; // nanos per 100ns

            FILETIME ft;
            ULARGE_INTEGER hnsTime;

            GetSystemTimePreciseAsFileTime(&ft);

            hnsTime.LowPart = ft.dwLowDateTime;
            hnsTime.HighPart = ft.dwHighDateTime;

            // get POSIX Epoch as baseline (subtract the number of hns intervals from Jan 1, 1601 to Jan 1, 1970)
            hnsTime.QuadPart -= (11644473600ULL * HNS_PER_SEC);

            struct timespec { long tv_sec; long tv_nsec; };
            timespec ct;

            ct.tv_nsec = (long)((hnsTime.QuadPart % HNS_PER_SEC) * NS_PER_HNS);
            ct.tv_sec = (long)(hnsTime.QuadPart / HNS_PER_SEC);

            return ((uint64_t)1000000000 * ct.tv_sec + ct.tv_nsec);
        };

        const uint64_t t0 = get_precise_time();
        for (;;)
        {
            const uint64_t t1 = get_precise_time();
            if (t1 > t0)
            {
                std::lock_guard<spinlock> guard(lock_);
                BASELINE_NANOS = t1;
                BASELINE_COUNT = ticks_();

                break;
            }
        }
    }

    uint64_t ticks_()
    {
        return __rdtsc();
    }

public:
    uint64_t now()
    {
        uint64_t count_now = ticks_();

        std::lock_guard<spinlock> guard(lock_);
        uint64_t elapsed_count = count_now - BASELINE_COUNT;
        uint64_t elapsed_nanos = (uint64_t)(elapsed_count*INV_TICKS_PER_NANO);
        return BASELINE_NANOS + elapsed_nanos;
    }

private:
    uint64_t BASELINE_COUNT;    // CPU tick value corresponding to calibration wall time
    uint64_t BASELINE_NANOS;    // calibration wall time in nanos
    double  INV_TICKS_PER_NANO; // store as 1/ticks to avoid a division in the main lookup
    spinlock lock_;
};
static WinNanoClock winNanoClock; // static so created at program start. continually recalibrates every 60s after
#endif

/**
 * rdtsc - read cpu timestamp counter - implementation for several platforms
 * if using rdtsc, make sure platform supports constant_tsc and nonstop_tsc (eg grep tsc /proc/cpuinfo on linux)
 * this ensures the counter is synchronised across cores and not affected by power management/CPU-clock respectively
 * NB: rdtsc calls can be reordered around other instructions by the CPU, so timings should be used with care if profiling code
 */
#if defined(__i386__) || defined( __x86_64__ ) || defined(_MSC_VER)
    uint64_t rdtsc()
    {
        return __rdtsc();
    }
#elif defined(__MIPS_32__)
    #define rdtsc(dest) _ _asm_ _ _ _volatile_ _("mfc0 %0,$9; nop" : "=r" (dest))
#elif defined(__MIPS_SGI__)
    uint64_t rdtsc ()
    {
        struct timespec tp;
        clock_gettime (CLOCK_SGI_CYCLE, &tp);
        return (uint64_t)(tp.tv_sec * (uint64_t)1000000000) + (uint64_t)tp.tv_nsec;
    }
#endif

/**
 * rdtsc with explicit cpuid call to serialise and prevent reordering
 * NB: if ordering matters, rdtscp (below) should be used if the platform supports it
 */
#if defined(__x86_64__)
uint64_t cpuid_rdtsc()
{
    uint32_t lo, hi;
    asm volatile (
     "cpuid \n"
     "rdtsc"
    : "=a"(lo), "=d"(hi)   // output
    : "a"(0)               // input
    : "%ebx", "%ecx");     // clobbers
    uint64_t x = ((uint64_t)lo) | (((uint64_t)hi) << 32);
    return x;
}
#endif

/**
 * Similar to rdtsc, but guarantees correct serialisation/ordering around neighbouring instructions
 * (Marginally slower, but more reliable, compared to rdtsc)
 * Older platforms will not support the instruction. (runtime SIGILL will result if used there)
 * Prefer the instinsic call.
 * asm for reference or if needed for platforms without a compiler intrinsic:
       uint32_t lo, hi;
       asm volatile( "rdtscp" : "=a"(lo), "=d"(hi) : "a"(0) : "%ebx", "%ecx");
       uint64_t x = ((uint64_t)lo) | (((uint64_t)hi) << 32);
 */
uint64_t rdtscp()
{
    unsigned dummy;
    return __rdtscp(&dummy);
}

/**
 * Returns nanoseconds since the epoch
 * Resolution 1ns
 */
uint64_t clocknanos()
{
#if !defined(_WIN32)
    struct timespec tv{};
    clock_gettime( CLOCK_REALTIME, &tv );
    return ((uint64_t)1000000000*tv.tv_sec + tv.tv_nsec);
#else
    return winNanoClock.now();
#endif
}

} // detail

/**
 *  JNI interface
 */
JNIEXPORT jlong JNICALL Java_net_openhft_chronicle_ticker_NativeTime_rdtsc
   (JNIEnv *env, jclass c) {
   return (jlong) detail::rdtsc();
}

JNIEXPORT jlong JNICALL Java_net_openhft_chronicle_ticker_NativeTime_cpuid_1rdtsc
   (JNIEnv *env, jclass c) {
   return (jlong) detail::cpuid_rdtsc();
}

JNIEXPORT jlong JNICALL Java_net_openhft_chronicle_ticker_NativeTime_rdtscp
   (JNIEnv *env, jclass c) {
   return (jlong) detail::rdtscp();
}

JNIEXPORT jlong JNICALL Java_net_openhft_chronicle_ticker_NativeTime_clocknanos
  (JNIEnv *, jclass) {
  return (jlong) detail::clocknanos();
}

#endif
