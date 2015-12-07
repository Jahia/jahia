/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
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
