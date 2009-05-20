/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.operations;

import org.jahia.services.cache.GroupCacheKey;
import org.jahia.settings.SettingsBean;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

/**
 * PageGeneratorQueue
 */
public class PageGeneratorQueue {
    private Map<GroupCacheKey,GroupCacheKey> notCacheablePage = new ConcurrentHashMap<GroupCacheKey, GroupCacheKey>(2503);
    private Map<GroupCacheKey, CountDownLatch> generatingPages;
    private int maxPagesToGenerateInParallel;
    private long pageGenerationWaitTime ;
    private long pageGenerationWaitTimeOnStartup ;    
    private Semaphore availableProcessings = null;
    private CountDownLatch firstRequestLatch = null;

    public PageGeneratorQueue(SettingsBean settingsBean) {
        maxPagesToGenerateInParallel = settingsBean.getMaxParallelProcessings();
        pageGenerationWaitTime = settingsBean.getPageGenerationWaitTime();
        pageGenerationWaitTimeOnStartup = settingsBean.getPageGenerationWaitTimeOnStartup();
        generatingPages = new HashMap<GroupCacheKey, CountDownLatch>(maxPagesToGenerateInParallel);
    }
    
    public Map<GroupCacheKey, GroupCacheKey> getNotCacheablePage() {
        return notCacheablePage;
    }

    public Map<GroupCacheKey, CountDownLatch> getGeneratingPages() {
        return generatingPages;
    }
    
    public CountDownLatch getFirstRequestLatch() {
        return firstRequestLatch;
    }

    public Semaphore getAvailableProcessings() {
        if (availableProcessings != null)
            return availableProcessings;
        synchronized (this) {
            if (availableProcessings != null)
                return availableProcessings;            
            availableProcessings = new Semaphore(getMaxPagesToGenerateInParallel(), true);            
        }
        return availableProcessings;
    }
    
    public int getMaxPagesToGenerateInParallel() {
        return maxPagesToGenerateInParallel;
    }

    public long getPageGenerationWaitTime() {
        return pageGenerationWaitTime;
    }
    
    public long getPageGenerationWaitTimeOnStartup() {
        return pageGenerationWaitTimeOnStartup;
    }

    public void setFirstRequestLatch(CountDownLatch firstRequestLatch) {
        this.firstRequestLatch = firstRequestLatch;
    }    
}
