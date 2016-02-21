/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.test;

import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class PerformanceTestUtils {

    private PerformanceTestUtils() {
    }

    public static <E> Collection<E> getUniqueRandomElements(Collection<E> candidateElements, int numRandomElements) {
        return getUniqueRandomElements(candidateElements, numRandomElements, null);
    }

    public static <E> Collection<E> getUniqueRandomElements(Collection<E> candidateElements, int numRandomElements, ElementRejector<E> rejector) {

        if (!(candidateElements instanceof Set)) {
            candidateElements = new LinkedHashSet<E>(candidateElements);
        }

        Collection<E> result = new ArrayList<E>(numRandomElements);
        candidateElements = new ArrayList<E>(candidateElements);
        Random random = ThreadLocalRandom.current();

        for (; result.size() < numRandomElements; ) {
            E element = ((List<E>) candidateElements).get(random.nextInt(candidateElements.size()));
            if (result.contains(element)) {
                continue;
            }
            if (rejector != null && rejector.isToBeRejected(element)) {
                continue;
            }
            result.add(element);
        }

        return result;
    }

    public interface ElementRejector<E> {

        boolean isToBeRejected(E element);
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
