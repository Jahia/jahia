/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.cache.ehcache;

import net.sf.ehcache.statistics.StatisticsGateway;

/**
 * A bean wrapper around the new EHCache 2.7 statistics gateway to make it easy to use in JSPs or other
 * classes
 */
public class EhCacheStatisticsWrapper {

    private StatisticsGateway statisticsGateway;

    public EhCacheStatisticsWrapper(StatisticsGateway statisticsGateway) {
        this.statisticsGateway = statisticsGateway;
    }

    public long getLocalHeapSizeInBytes() {
        return statisticsGateway.getLocalHeapSizeInBytes();
    }

    public long getLocalHeapSize() {
        return statisticsGateway.getLocalHeapSize();
    }

    public long getWriterQueueLength() {
        return statisticsGateway.getWriterQueueLength();
    }

    public long getLocalDiskSize() {
        return statisticsGateway.getLocalDiskSize();
    }

    public long getLocalOffHeapSize() {
        return statisticsGateway.getLocalOffHeapSize();
    }

    public long getLocalDiskSizeInBytes() {
        return statisticsGateway.getLocalDiskSizeInBytes();
    }

    public long getLocalOffHeapSizeInBytes() {
        return statisticsGateway.getLocalOffHeapSizeInBytes();
    }

    public long getRemoteSize() {
        return statisticsGateway.getRemoteSize();
    }

    public long getSize() {
        return statisticsGateway.getSize();
    }


    public long getCacheHitCount() {
        return statisticsGateway.cacheHitCount();
    }

    public long getCacheMissExpiredCount() {
        return statisticsGateway.cacheMissExpiredCount();
    }

    public long getCacheMissNotFoundCount() {
        return statisticsGateway.cacheMissNotFoundCount();
    }

    public long getCacheMissCount() {
        return statisticsGateway.cacheMissCount();
    }

    public long getCachePutAddedCount() {
        return statisticsGateway.cachePutAddedCount();
    }

    public long getCachePutUpdatedCount() {
        return statisticsGateway.cachePutUpdatedCount();
    }

    public long getCachePutCount() {
        return statisticsGateway.cachePutCount();
    }

    public long getCacheRemoveCount() {
        return statisticsGateway.cacheRemoveCount();
    }

    public long getLocalHeapHitCount() {
        return statisticsGateway.localHeapHitCount();
    }

    public long getLocalHeapMissCount() {
        return statisticsGateway.localHeapMissCount();
    }

    public long getLocalHeapPutAddedCount() {
        return statisticsGateway.localHeapPutAddedCount();
    }

    public long getLocalHeapPutUpdatedCount() {
        return statisticsGateway.localHeapPutUpdatedCount();
    }

    public long getLocalHeapPutCount() {
        return statisticsGateway.localHeapPutCount();
    }

    public long getLocalHeapRemoveCount() {
        return statisticsGateway.localHeapRemoveCount();
    }

    public long getLocalOffHeapHitCount() {
        return statisticsGateway.localOffHeapHitCount();
    }

    public long getLocalOffHeapMissCount() {
        return statisticsGateway.localOffHeapMissCount();
    }

    public long getLocalOffHeapPutAddedCount() {
        return statisticsGateway.localOffHeapPutAddedCount();
    }

    public long getLocalOffHeapPutUpdatedCount() {
        return statisticsGateway.localOffHeapPutUpdatedCount();
    }

    public long getLocalOffHeapPutCount() {
        return statisticsGateway.localOffHeapPutCount();
    }

    public long getLocalOffHeapRemoveCount() {
        return statisticsGateway.localOffHeapRemoveCount();
    }

    public long getLocalDiskHitCount() {
        return statisticsGateway.localDiskHitCount();
    }

    public long getLocalDiskMissCount() {
        return statisticsGateway.localDiskMissCount();
    }

    public long getLocalDiskPutAddedCount() {
        return statisticsGateway.localDiskPutAddedCount();
    }

    public long getLocalDiskPutUpdatedCount() {
        return statisticsGateway.localDiskPutUpdatedCount();
    }

    public long getLocalDiskPutCount() {
        return statisticsGateway.localDiskPutCount();
    }

    public long getLocalDiskRemoveCount() {
        return statisticsGateway.localDiskRemoveCount();
    }

    public long getXaCommitReadOnlyCount() {
        return statisticsGateway.xaCommitReadOnlyCount();
    }

    public long getXaCommitExceptionCount() {
        return statisticsGateway.xaCommitExceptionCount();
    }

    public long getXaCommitCommittedCount() {
        return statisticsGateway.xaCommitCommittedCount();
    }

    public long getXaCommitCount() {
        return statisticsGateway.xaCommitCount();
    }

    public long getXaRecoveryNothingCount() {
        return statisticsGateway.xaRecoveryNothingCount();
    }

    public long getXaRecoveryRecoveredCount() {
        return statisticsGateway.xaRecoveryRecoveredCount();
    }

    public long getXaRecoveryCount() {
        return statisticsGateway.xaRecoveryCount();
    }

    public long getXaRollbackExceptionCount() {
        return statisticsGateway.xaRollbackExceptionCount();
    }

    public long getXaRollbackSuccessCount() {
        return statisticsGateway.xaRollbackSuccessCount();
    }

    public long getXaRollbackCount() {
        return statisticsGateway.xaRollbackCount();
    }

    public long getCacheExpiredCount() {
        return statisticsGateway.cacheExpiredCount();
    }

    public long getCacheEvictedCount() {
        return statisticsGateway.cacheEvictedCount();
    }

    public double getCacheHitRatio() {
        return statisticsGateway.cacheHitRatio();
    }

}
