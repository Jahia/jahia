/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.render.filter.cache;

import org.apache.log4j.Logger;
import org.jahia.services.cache.GroupCacheKey;
import org.jahia.settings.SettingsBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 12 oct. 2010
 */
public class PageGeneratorQueue implements InitializingBean {
    private transient static Logger logger = Logger.getLogger(PageGeneratorQueue.class);
    private Map<String,String> notCacheablePage = new ConcurrentHashMap<String, String>(2503);
    private Map<String, CountDownLatch> generatingPages;
    private int maxPagesToGenerateInParallel;
    private long pageGenerationWaitTime ;
    private long pageGenerationWaitTimeOnStartup ;
    private Semaphore availableProcessings = null;
    private CountDownLatch firstRequestLatch = null;

    public Map<String, String> getNotCacheablePage() {
        return notCacheablePage;
    }

    public Map<String, CountDownLatch> getGeneratingPages() {
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

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     *                   as failure to set an essential property) or if initialization fails.
     */
    public void afterPropertiesSet() throws Exception {
        generatingPages = new HashMap<String, CountDownLatch>(maxPagesToGenerateInParallel);
    }

    public void setMaxPagesToGenerateInParallel(int maxPagesToGenerateInParallel) {
        this.maxPagesToGenerateInParallel = maxPagesToGenerateInParallel;
    }

    public void setPageGenerationWaitTime(long pageGenerationWaitTime) {
        this.pageGenerationWaitTime = pageGenerationWaitTime;
    }

    public void setPageGenerationWaitTimeOnStartup(long pageGenerationWaitTimeOnStartup) {
        this.pageGenerationWaitTimeOnStartup = pageGenerationWaitTimeOnStartup;
    }
}
