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
package org.jahia.services.render.filter.cache;

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

        if (generatingModules.get(key) == null && generatingKeySemaphore.get() == null) {
            if (!getAvailableProcessings().tryAcquire(moduleGenerationWaitTime, TimeUnit.MILLISECONDS)) {
                manageThreadDump();
                throw new Exception("Module generation takes too long due to maximum parallel processing reached (" + maxModulesToGenerateInParallel + ") - " + key + " - " + request.getRequestURI());
            } else {
                generatingKeySemaphore.set(key);
            }
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
            if (generatingKeySemaphore.get() != null) {
                // another thread wanted the same module and got the latch first, so release the semaphore immediately as we must wait
                getAvailableProcessings().release();
                generatingKeySemaphore.set(null);
            }
            try {
                if (!latch.await(getModuleGenerationWaitTime(), TimeUnit.MILLISECONDS)) {
                    manageThreadDump();
                    throw new Exception("Module generation takes too long due to module not generated fast enough (" + moduleGenerationWaitTime + " ms) - " + key + " - " + request.getRequestURI());
                }
                latch = null;
            } catch (InterruptedException e) {
                logger.debug("The waiting thread has been interrupted", e);
                throw new Exception(e);
            }
        }
        return latch;
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
     */
    protected void releaseLatch(String finalKey) {
        if (finalKey.equals(generatingKeySemaphore.get())) {
            getAvailableProcessings().release();
            generatingKeySemaphore.set(null);
        }

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
