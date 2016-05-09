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

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Latch service used by the CacheFilter to be able to put a flag when a fragment is in generation to avoid other threads
 * to generate it also, Threads will wait until the fragment is available.
 *
 * When the latch is released a callback that can be implemented during the get of the latch will be execute.
 *
 * Created by jkevan on 09/05/2016.
 */
public class CacheLatchService {
    private static final Logger logger = LoggerFactory.getLogger(CacheLatchService.class);

    protected ModuleGeneratorQueue generatorQueue;
    protected static long lastThreadDumpTime = 0L;
    protected Byte[] threadDumpCheckLock = new Byte[0];

    protected static ThreadLocal<Set<CountDownLatch>> processingLatches = new ThreadLocal<Set<CountDownLatch>>();
    protected static ThreadLocal<String> acquiredSemaphore = new ThreadLocal<String>();
    protected static ThreadLocal<LinkedList<String>> userKeys = new ThreadLocal<LinkedList<String>>();

    /**
     * This is the main function of the latch mechanism that will acquire the latch or wait until the release
     * @param key the key for the latch
     * @param request current request
     * @return return null when the latch is released, or an instance of CountDownLatch when the latch is acquired
     * @throws Exception
     */
    private CountDownLatch avoidParallelProcessingOfSameModule(String key, HttpServletRequest request) throws Exception {
        CountDownLatch latch;
        boolean mustWait = true;

        Map<String, CountDownLatch> generatingModules = generatorQueue.getGeneratingModules();
        if (generatingModules.get(key) == null && acquiredSemaphore.get() == null) {
            if (!generatorQueue.getAvailableProcessings().tryAcquire(generatorQueue.getModuleGenerationWaitTime(),
                    TimeUnit.MILLISECONDS)) {
                manageThreadDump();
                throw new Exception("Module generation takes too long due to maximum parallel processing reached (" +
                        generatorQueue.getMaxModulesToGenerateInParallel() + ") - " + key + " - " +
                        request.getRequestURI());
            } else {
                acquiredSemaphore.set(key);
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
            if (acquiredSemaphore.get() != null) {
                // another thread wanted the same module and got the latch first, so release the semaphore immediately as we must wait
                generatorQueue.getAvailableProcessings().release();
                acquiredSemaphore.set(null);
            }
            try {
                if (!latch.await(generatorQueue.getModuleGenerationWaitTime(), TimeUnit.MILLISECONDS)) {
                    manageThreadDump();
                    throw new Exception("Module generation takes too long due to module not generated fast enough (>" +
                            generatorQueue.getModuleGenerationWaitTime() + " ms)- " + key + " - " +
                            request.getRequestURI());
                }
                latch = null;
            } catch (InterruptedException ie) {
                if (logger.isDebugEnabled()) {
                    logger.debug("The waiting thread has been interrupted :", ie);
                }
                throw new Exception(ie);
            }
        }
        return latch;
    }

    private void manageThreadDump() {
        boolean createDump = false;
        long minInterval = generatorQueue.getMinimumIntervalAfterLastAutoThreadDump();
        if (minInterval > -1 && (generatorQueue.isThreadDumpToSystemOut() || generatorQueue.isThreadDumpToFile())) {
            long now = System.currentTimeMillis();
            synchronized (threadDumpCheckLock) {
                if (now > (lastThreadDumpTime + minInterval)) {
                    createDump = true;
                    lastThreadDumpTime = now;
                }
            }
        }
        if (createDump) {
            ThreadMonitor tm = ThreadMonitor.getInstance();
            tm.dumpThreadInfo(generatorQueue.isThreadDumpToSystemOut(), generatorQueue.isThreadDumpToFile());
        }
    }

    /**
     * Put a latch for the given fragment key, this key will be unavailable for all other threads until the thread that put the latch
     * call the releaseLatch function.
     *
     * The first thread that will call this function will acquire the latch
     * The others threads that call also this "getLatch" fct, coming after will wait until first thread finish his work.
     * The first thread signal that the work is finished by calling the "releaseLatch()" function,
     * When the first thread call this "releaseLatch" to signal that the work is done all other waiting threads
     * will execute the latchReleasedCallback.doAfterLatchReleased
     *
     * This mechanism is used by the CacheFilter to allow first thread to generate a fragment, and other threads wait for it to finished
     * So then can get the generated fragment directly from the cache. By doing that we avoid to generate multiple time the same fragment
     *
     * @param renderContext current RenderContext
     * @param finalKey fragment key
     * @param latchReleasedCallback callback executed for waiting threads, that will be call after the latch is released by first thread
     * @return null for first thread or return the latchReleasedCallback.doAfterLatchReleased for others thread (when first thread have release the latch.
     *
     * @throws Exception
     */
    protected String getLatch(RenderContext renderContext, String finalKey, LatchReleasedCallback latchReleasedCallback) throws Exception {
        //Keeps a list of keys being generated to avoid infinite loop
        LinkedList<String> userKeysLinkedList = userKeys.get();
        if (userKeysLinkedList == null) {
            userKeysLinkedList = new LinkedList<>();
            userKeys.set(userKeysLinkedList);
        }
        if (userKeysLinkedList.contains(finalKey)) {
            return null;
        }
        userKeysLinkedList.add(0, finalKey);

        // get the latch
        CountDownLatch countDownLatch = avoidParallelProcessingOfSameModule(finalKey, renderContext.getRequest());

        // latch is null mean that it's have been released
        if (countDownLatch == null) {
            if(latchReleasedCallback != null) {
                return latchReleasedCallback.doAfterLatchReleased(renderContext, finalKey);
            }
        } else {
            // Latch have been acquired by current thread, store it
            Set<CountDownLatch> latches = processingLatches.get();
            if (latches == null) {
                latches = new HashSet<>();
                processingLatches.set(latches);
            }
            latches.add(countDownLatch);
        }
        return null;
    }

    /**
     * Release latch, this will signal that the current thread have finish his work
     * All the other waiting threads will be free to get/read the resource generate by the first thread
     */
    protected void releaseLatch() {
        LinkedList<String> userKeysLinkedList = userKeys.get();
        if (userKeysLinkedList != null && userKeysLinkedList.size() > 0) {

            String finalKey = userKeysLinkedList.remove(0);
            if (finalKey.equals(acquiredSemaphore.get())) {
                generatorQueue.getAvailableProcessings().release();
                acquiredSemaphore.set(null);
            }

            Set<CountDownLatch> latches = processingLatches.get();
            Map<String, CountDownLatch> countDownLatchMap = generatorQueue.getGeneratingModules();
            CountDownLatch latch = countDownLatchMap.get(finalKey);
            if (latches != null && latches.contains(latch)) {
                latch.countDown();
                synchronized (countDownLatchMap) {
                    latches.remove(countDownLatchMap.remove(finalKey));
                }
            }
        }
    }

    public interface LatchReleasedCallback {
        String doAfterLatchReleased(RenderContext renderContext, String finalKey);
    }

    public void setGeneratorQueue(ModuleGeneratorQueue generatorQueue) {
        this.generatorQueue = generatorQueue;
    }
}
