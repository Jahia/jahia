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

package org.jahia.operations.valves;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Category;
import org.jahia.bin.Jahia;
import org.jahia.data.JahiaData;
import org.jahia.engines.JahiaEngine;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaServerOverloadedException;
import org.jahia.operations.PageGeneratorQueue;
import org.jahia.operations.PageState;
import org.jahia.params.ProcessingContext;
import org.jahia.params.SessionState;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.Valve;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.registries.EnginesRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.GroupCacheKey;
/**
 * <p> Title: </p> <p> Description: </p> <p> Copyright: Copyright (c) 2004 </p> <p> Company: Jahia Ltd </p>
 *
 * @author Serge Huber
 * @version 1.0
 */

public class EngineValve implements Valve {
    private static Category logger = org.apache.log4j.Logger.getLogger(EngineValve.class);

    private PageGeneratorQueue generatorQueue;
    private SkeletonAggregatorValve skeletonAggregatorValve;

    public EngineValve() {
    }

    public void invoke(Object context, ValveContext valveContext) throws PipelineException {
        ProcessingContext processingContext = (ProcessingContext) context;
        PageState state = (PageState) ValveContext.valveResources.get();

        // So we did not find the page in the cache
        // check if the page is already generating or is generated
        // force generation
        CountDownLatch firstRequestLatch = getGeneratorQueue().getFirstRequestLatch();
        boolean firstRequest = false;
        if (firstRequestLatch == null) {
            synchronized (getGeneratorQueue()) {
                firstRequestLatch = getGeneratorQueue().getFirstRequestLatch();
                if (firstRequestLatch == null) {
                    firstRequestLatch = new CountDownLatch(1);
                    firstRequest = true;
                    getGeneratorQueue().setFirstRequestLatch(firstRequestLatch);
                }
            }
        }

        try {
            if (firstRequest || firstRequestLatch.await(getGeneratorQueue()
                    .getPageGenerationWaitTimeOnStartup(), TimeUnit.MILLISECONDS)) {

                processEngineRequest(state, processingContext, context, valveContext);

            }
            else {
                throw new JahiaServerOverloadedException(true,
                                                         org.jahia.settings.SettingsBean.getInstance().getSuggestedRetryTimeAfterTimeoutOnStartup());
            }
        } catch (InterruptedException ie) {
            logger.debug("The waiting thread has been interrupted :", ie);
            throw new PipelineException(ie);
        } catch (PipelineException pe) {
            throw pe;
        } catch (Exception je) {
            throw new PipelineException(je);
        } finally {
            if (firstRequest && firstRequestLatch != null) firstRequestLatch.countDown();
        }
    }

    public void initialize() {
    }

    /**
     * Retrieve the requested engine instance.
     *
     * @param name the engine name
     *
     * @return the reference to the engine, or <code>null</code> when the engine name is unknown in the Engines
     *         Registry.
     *
     * @throws JahiaException when the Engines Registry reference could not be retrieved.
     */
    private JahiaEngine getEngineInstance(String name) throws JahiaException {
        // Get the Engines Registry
        EnginesRegistry registry = EnginesRegistry.getInstance();
        if (registry == null) throw new JahiaException("Internal Error",
                                                       "Could not get the Engines Registry instance!",
                                                       JahiaException.INITIALIZATION_ERROR,
                                                       JahiaException.ERROR_SEVERITY);

        // Return the requested engine
        return (JahiaEngine) registry.getEngine(name);
    }

    private void processEngineRequest(PageState state, ProcessingContext processingContext, Object context,
            ValveContext valveContext) throws PipelineException {
        // Get the engine instance (can throw a JahiaException)
        String engineName = processingContext.getEngine();
        CountDownLatch latch = null;

        boolean semaphoreAcquired = false;
        try {
            if (state != null &&
                state.isCacheable() &&
                !getGeneratorQueue().getNotCacheablePage().containsKey(state.getKey())) {

                latch = avoidParallelProcessingOfSamePage(state, processingContext);

                if (skeletonAggregatorValve.checkCache(processingContext)) return;
            }

            if (!getGeneratorQueue().getAvailableProcessings()
                    .tryAcquire(getGeneratorQueue().getPageGenerationWaitTime(), TimeUnit.MILLISECONDS)) {
                throw new JahiaServerOverloadedException(false,
                                                         org.jahia.settings.SettingsBean.getInstance().getSuggestedRetryTimeAfterTimeout());
            }
            else {
                semaphoreAcquired = true;
            }

            JahiaEngine theEngine = getEngineInstance(engineName);
            // logger.debug("Engine=" + engineName);

            if (theEngine == null) {
                logger.debug("Could not get the engine [" + engineName + "] instance.");
                return;
            }

            JahiaData jData = null;
            if (theEngine.needsJahiaData(processingContext)) {
                // logger.debug("Engine need JahiaData");
                if (processingContext.getPage() != null) {
                    jData = new JahiaData(processingContext);
                }
                else {
                    jData = new JahiaData(processingContext, false);
                }
            }

            if (jData == null) {
                // at least create a jData with minimum data for taglibs usage everywhere
                jData = new JahiaData(processingContext, false);
            }

            // for JSp
            processingContext.setAttribute("org.jahia.data.JahiaData", jData);

            theEngine.handleActions(processingContext, jData);
            logger.debug("Operation handled for engine " + processingContext.getEngine());

            // destroys request-dependant objects
            jData = null;
            SessionState session = processingContext.getSessionState();
            // save last engine name in session
            session.setAttribute(ProcessingContext.SESSION_LAST_ENGINE_NAME, processingContext.getEngine());
            
            ValveContext.valveResources.set(state);
            valveContext.invokeNext(context);            

        } catch (InterruptedException ie) {
            logger.debug("The waiting thread has been interrupted :", ie);
            throw new PipelineException(ie);
        } catch (Exception je) {
            throw new PipelineException(je);
        } finally {
            if (semaphoreAcquired) {
                getGeneratorQueue().getAvailableProcessings().release();
            }
            if (latch != null) {
                latch.countDown();

                if (state != null && state.getKey() != null) {
                    Map generatingPages = getGeneratorQueue().getGeneratingPages();
                    synchronized (generatingPages) {
                        generatingPages.remove(state.getKey());
                    }
                }
            }

            ServicesRegistry.getInstance().getJahiaEventService().fireAggregatedEvents();
        }
    }

    private CountDownLatch avoidParallelProcessingOfSamePage(PageState state,
                                                    ProcessingContext processingContext) throws PipelineException {
        CountDownLatch latch = null;
        boolean mustWait = true;
        GroupCacheKey entryKey = state.getKey();
        Map generatingPages = getGeneratorQueue().getGeneratingPages();
        synchronized (generatingPages) {
            latch = (CountDownLatch) generatingPages.get(entryKey);
            if (latch == null) {
                latch = new CountDownLatch(1);
                generatingPages.put(entryKey, latch);
                mustWait = false;
            }
        }
        if (mustWait) {
            try {
                if (!latch.await(getGeneratorQueue()
                        .getPageGenerationWaitTime(), TimeUnit.MILLISECONDS)) {
                    throw new JahiaServerOverloadedException(false, Jahia
                            .getSettings().getSuggestedRetryTimeAfterTimeout());
                }
                latch = null;
            } catch (InterruptedException ie) {
                logger.debug("The waiting thread has been interrupted :", ie);
                throw new PipelineException(ie);
            } catch (Exception je) {
                throw new PipelineException(je);
            }
        }
        return latch;
    }

    public PageGeneratorQueue getGeneratorQueue() {
        return generatorQueue;
    }

    public void setGeneratorQueue(PageGeneratorQueue generatorQueue) {
        this.generatorQueue = generatorQueue;
    }

    public void setSkeletonAggregatorValve(
            SkeletonAggregatorValve skeletonAggregatorValve) {
        this.skeletonAggregatorValve = skeletonAggregatorValve;
    }

}
