/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.util.CollectionUtils;

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
