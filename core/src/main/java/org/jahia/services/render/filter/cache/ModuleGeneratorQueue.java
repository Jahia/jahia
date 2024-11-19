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
package org.jahia.services.render.filter.cache;

import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.exceptions.JahiaServiceUnavailableException;
import org.jahia.services.render.RenderContext;
import org.jahia.tools.jvm.ThreadMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author : rincevent
 * @since JAHIA 6.5
 * Created : 12 oct. 2010
 */
public class ModuleGeneratorQueue implements InitializingBean {

    public static final String HAS_PROCESSING_SEMAPHORE_PARAM = "moduleGeneratorQueue.hasProcessingSemaphore";

    private Map<String, String> notCacheableModule = new ConcurrentHashMap<String, String>(2503);
    private Map<String, CountDownLatch> generatingModules;
    private int maxModulesToGenerateInParallel = 50;
    private long moduleGenerationWaitTime = 5000;
    private volatile Semaphore availableProcessings = null;
    private long minimumIntervalAfterLastAutoThreadDump = 60000; // in milliseconds
    private boolean threadDumpToSystemOut = true;
    private boolean threadDumpToFile = true;
    private static long lastThreadDumpTime = 0L;
    private Byte[] threadDumpCheckLock = new Byte[0];

    private static ThreadLocal<Set<CountDownLatch>> processingLatches = new ThreadLocal<Set<CountDownLatch>>();
    private static ThreadLocal<String> generatingKeySemaphore = new ThreadLocal<String>();

    private static final Logger logger = LoggerFactory.getLogger(ModuleGeneratorQueue.class);

    public Map<String, String> getNotCacheableModule() {
        return notCacheableModule;
    }

    public Map<String, CountDownLatch> getGeneratingModules() {
        return generatingModules;
    }

    /**
     * Available processings is a Semaphore that allow multiple threads to generate fragments in parallel
     * the number of parallel processings is configurable using "maxModulesToGenerateInParallel" property
     * @return the Semaphore
     */
    public Semaphore getAvailableProcessings() {
        // Double-checked locking only works with volatile for Java 5+
        // result variable is used to avoid accessing the volatile field multiple times to increase performance per Effective Java 2nd Ed.
        Semaphore result = availableProcessings;
        if (result == null) {
            synchronized (this) {
                result = availableProcessings;
                if (result == null) {
                    availableProcessings = result = new Semaphore(maxModulesToGenerateInParallel, true);
                }
            }
        }
        return result;
    }

    /**
     * This is the main function of the latch mechanism that will acquire the latch or wait until the release
     * @param key the key for the latch
     * @param request current request
     * @return return null when the latch is released, or an instance of CountDownLatch when the latch is acquired
     */
    private CountDownLatch avoidParallelProcessingOfSameModule(String key, HttpServletRequest request) throws Exception {

        CountDownLatch latch;
        boolean mustWait = true;

        if (generatingModules.get(key) == null) {
            // get permit to generate fragment (based on maximum allowed number of fragment generation in parallel)
            getFragmentsGenerationPermit(key, request);
        }
        synchronized (generatingModules) {
            latch = generatingModules.get(key);
            if (latch == null) {
                latch = new CountDownLatch(1);
                generatingModules.put(key, latch);
                mustWait = false;
            }
        }
        if (mustWait) {
            // another thread wanted the same module and got the latch first, so release the semaphore immediately as we must wait
            releaseFragmentsGenerationPermit(request);

            try {
                if (!latch.await(getModuleGenerationWaitTime(), TimeUnit.MILLISECONDS)) {
                    manageThreadDump();
                    StringBuilder errorMsgBuilder = new StringBuilder(512);
                    errorMsgBuilder.append("Module generation takes too long due to module not generated fast enough (>")
                            .append(moduleGenerationWaitTime).append(" ms)- ").append(key).append(" - ")
                            .append(request.getRequestURI());
                    throw new JahiaServiceUnavailableException(errorMsgBuilder.toString());
                }
                latch = null;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new JahiaRuntimeException(e);
            }
        }
        return latch;
    }

    /**
     * Number of threads generating fragments in parallel is limited, this function is used to get the permission to generate a page
     * based on the property "maxModulesToGenerateInParallel". The permit is stored in the current request attributes and must be released
     * when the page is complete by the current thread request using the function "releaseRequestGenerationPermit(HttpServletRequest request)"
     * @param resourceIdentifier Identifier for the current resource, could be the cache key if available, or anything that could identify the resource ( only use for log purpose )
     * @param request the current request
     */
    protected void getFragmentsGenerationPermit(String resourceIdentifier, HttpServletRequest request) {
        if (!Boolean.TRUE.equals(request.getAttribute(HAS_PROCESSING_SEMAPHORE_PARAM))) {
            try {
                if (!getAvailableProcessings().tryAcquire(getModuleGenerationWaitTime(), TimeUnit.MILLISECONDS)) {
                    manageThreadDump();
                    StringBuilder errorMsgBuilder = new StringBuilder(512).append("Module generation takes too long due to maximum parallel processing reached (")
                            .append(maxModulesToGenerateInParallel).append(") - ").append(resourceIdentifier).append(" - ")
                            .append(request.getRequestURI());
                    throw new JahiaServiceUnavailableException(errorMsgBuilder.toString());
                } else {
                    request.setAttribute(HAS_PROCESSING_SEMAPHORE_PARAM, Boolean.TRUE);
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new JahiaRuntimeException(ie);
            }
        }
    }

    /**
     * Release a permit previously set for the current thread, this function is used when the current thread just finish
     * a page generation. A permit slot is released allowing an other thread to take it to generate a fragment.
     * @param request the current request
     */
    protected void releaseFragmentsGenerationPermit(HttpServletRequest request) {
        if (Boolean.TRUE.equals(request.getAttribute(HAS_PROCESSING_SEMAPHORE_PARAM))) {
            // another thread wanted the same module and got the latch first, so release the semaphore immediately as we must wait
            getAvailableProcessings().release();
            request.removeAttribute(HAS_PROCESSING_SEMAPHORE_PARAM);
        }
    }

    private void manageThreadDump() {
        boolean createDump = false;
        if (minimumIntervalAfterLastAutoThreadDump > -1 && (threadDumpToSystemOut || threadDumpToFile)) {
            long now = System.currentTimeMillis();
            synchronized (threadDumpCheckLock) {
                if (now > (lastThreadDumpTime + minimumIntervalAfterLastAutoThreadDump)) {
                    createDump = true;
                    lastThreadDumpTime = now;
                }
            }
        }
        if (createDump) {
            ThreadMonitor tm = ThreadMonitor.getInstance();
            tm.dumpThreadInfo(threadDumpToSystemOut, threadDumpToFile);
        }
    }

    /**
     * Get a latch for the given fragment key, this key will be unavailable for all other threads until the thread that put the latch
     * call the releaseLatch function.
     *
     * The first thread that will call this function will acquire the latch
     * The others threads that call also this "getLatch" fct, coming after will wait until first thread finish his work.
     * The first thread signal that the work is finished by calling the "releaseLatch()" function,
     *
     * This mechanism is used by the CacheFilter to allow first thread to generate a fragment, and other threads wait for it to finished
     * So then can get the generated fragment directly from the cache. By doing that we avoid to generate multiple time the same fragment
     *
     * This function also handle the maximum allowed fragment generation in parallel internally (based on maxModulesToGenerateInParallel property)
     * No need to call the functions to manage the permit of fragment generation additionally to "getLatch" function.
     *
     * @param renderContext current RenderContext
     * @param finalKey fragment key
     * @return false for first thread or return true for others thread when first thread have released the latch
     */
    protected boolean getLatch(RenderContext renderContext, String finalKey) throws Exception {
        // get the latch
        CountDownLatch latch = avoidParallelProcessingOfSameModule(finalKey, renderContext.getRequest());

        // latch is null mean that it's have been released
        if (latch == null) {
            return true;
        } else {
            // Latch have been acquired by current thread, store it
            Set<CountDownLatch> latches = processingLatches.get();
            if (latches == null) {
                latches = new HashSet<>();
                processingLatches.set(latches);
            }
            latches.add(latch);
            return false;
        }
    }

    /**
     * Release latch, this will signal that the current thread have finish its work
     * All the other waiting threads will be free to get/read the resource generated by the first thread
     *
     * This function also handle the maximum allowed fragment generation in parallel internally (based on maxModulesToGenerateInParallel property)
     * The permit is automatically released, no need to do additional call to release the permit.
     */
    protected void releaseLatch(String finalKey) {
        Set<CountDownLatch> latches = processingLatches.get();
        CountDownLatch latch = generatingModules.get(finalKey);
        if (latches != null && latches.contains(latch)) {
            latch.countDown();
            synchronized (generatingModules) {
                latches.remove(generatingModules.remove(finalKey));
            }
        }
    }

    public int getMaxModulesToGenerateInParallel() {
        return maxModulesToGenerateInParallel;
    }

    public long getModuleGenerationWaitTime() {
        return moduleGenerationWaitTime;
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
        generatingModules = new HashMap<String, CountDownLatch>(maxModulesToGenerateInParallel);
    }

    public void setMaxModulesToGenerateInParallel(int maxModulesToGenerateInParallel) {
        this.maxModulesToGenerateInParallel = maxModulesToGenerateInParallel;
        availableProcessings = null;
    }

    public void setModuleGenerationWaitTime(long moduleGenerationWaitTime) {
        this.moduleGenerationWaitTime = moduleGenerationWaitTime;
    }

    public void setMinimumIntervalAfterLastAutoThreadDump(long minimumIntervalAfterLastAutoThreadDump) {
        this.minimumIntervalAfterLastAutoThreadDump = minimumIntervalAfterLastAutoThreadDump;
    }

    public long getMinimumIntervalAfterLastAutoThreadDump() {
        return minimumIntervalAfterLastAutoThreadDump;
    }

    public boolean isThreadDumpToSystemOut() {
        return threadDumpToSystemOut;
    }

    public void setThreadDumpToSystemOut(boolean threadDumpToSystemOut) {
        this.threadDumpToSystemOut = threadDumpToSystemOut;
    }

    public boolean isThreadDumpToFile() {
        return threadDumpToFile;
    }

    public void setThreadDumpToFile(boolean threadDumpToFile) {
        this.threadDumpToFile = threadDumpToFile;
    }
}
