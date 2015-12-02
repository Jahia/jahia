package org.jahia.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.springframework.util.CollectionUtils;

public class PerformanceTestUtils {

    private PerformanceTestUtils() {
    }

    public static TimingStatistics getTimingStatistics(Collection<Long> samples) {
        return new TimingStatisticsImpl(samples);
    }

    public interface TimingStatistics {

        int getNumSamples();
        long getMin();
        long getMax();
        long getAvg();
        long getPercentile(int percentile);
    }

    private static class TimingStatisticsImpl implements TimingStatistics {

        private ArrayList<Long> sortedSamples;
        private Long min;
        private Long max;
        private long avg;

        public TimingStatisticsImpl(Collection<Long> samples) {
            if (CollectionUtils.isEmpty(samples)) {
                throw new IllegalArgumentException();
            }
            sortedSamples = new ArrayList<Long>(samples);
            Collections.sort(sortedSamples);
            long total = 0;
            for (long sample : sortedSamples) {
                if (sample < 0 ) {
                    throw new IllegalArgumentException();
                }
                if (min == null || sample < min) {
                    min = sample;
                }
                if (max == null || sample > max) {
                    max = sample;
                }
                total += sample;
            }
            avg = Math.round(total / (double) getNumSamples());
        }

        @Override
        public int getNumSamples() {
            return sortedSamples.size();
        }

        @Override
        public long getMin() {
            return min;
        }

        @Override
        public long getMax() {
            return max;
        }

        @Override
        public long getAvg() {
            return avg;
        }

        @Override
        public long getPercentile(int percentile) {
            if (percentile <= 0 || percentile >= 100) {
                throw new IllegalArgumentException();
            }
            int index = (int) Math.ceil(getNumSamples() * (percentile / (float) 100)) - 1;
            return sortedSamples.get(index);
        }
    }
}
