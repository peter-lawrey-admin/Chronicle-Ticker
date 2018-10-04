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
#include <time.h>
#include <cstdint>

#if defined(__x86_64__)
    #if defined( __GNUC__)
        #include <x86intrin.h>
    #elif defined(_MSC_VER)
        #include <intrin.h>
    #endif
#endif

namespace detail
{

/**
 * rdtsc - read cpu timestamp counter - implementation for several platforms
 * if using rdtsc, make sure platform supports constant_tsc and nonstop_tsc (eg grep tsc /proc/cpuinfo on linux)
 * this ensures the counter is synchronised across cores and not affected by power management/CPU-clock respectively
 * NB: rdtsc calls can be reordered around other instructions by the CPU, so timings should be used with care if profiling code
 */
#if defined(__i386__)
    uint64_t rdtsc()
    {
        uint64_t x;
        __asm__ volatile (".byte 0x0f, 0x31" : "=A" (x));
        return x;
    }
#elif defined(__x86_64__)
    uint64_t rdtsc()
    {
        uint32_t lo, hi;
        asm volatile (
         "rdtsc"
        : "=a"(lo), "=d"(hi)   // output
        : "a"(0)               // input
        : "%ebx", "%ecx");     // clobbers
        uint64_t x = ((uint64_t)lo) | (((uint64_t)hi) << 32);
        return x;
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
    struct timespec tv{};
    clock_gettime( CLOCK_REALTIME, &tv );
    return ((uint64_t)1000000000*tv.tv_sec + tv.tv_nsec);
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