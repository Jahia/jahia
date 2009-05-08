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
package org.jahia.operations.valves;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Category;
import org.jahia.bin.Jahia;
import org.jahia.data.JahiaData;
import org.jahia.data.beans.JahiaBean;
import org.jahia.data.beans.PageBean;
import org.jahia.data.beans.RequestBean;
import org.jahia.data.beans.SiteBean;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.calendar.CalendarHandler;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaServerOverloadedException;
import org.jahia.gui.GuiBean;
import org.jahia.operations.PageGeneratorQueue;
import org.jahia.operations.PageState;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.params.SessionState;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.Valve;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.registries.EnginesRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.GroupCacheKey;
import org.jahia.services.expressions.DateBean;
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
            
            setContentAccessBeans((ParamBean)processingContext);
            
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
                    Map<GroupCacheKey, CountDownLatch> generatingPages = getGeneratorQueue().getGeneratingPages();
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
        Map<GroupCacheKey, CountDownLatch> generatingPages = getGeneratorQueue().getGeneratingPages();
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

    private void setContentAccessBeans(ParamBean jParams) {
        final HttpServletRequest request = jParams.getRequest();
        PageBean pageBean = null;
        if (jParams.getPage()!=null) {
            pageBean = new PageBean (jParams.getPage (), jParams);
            request.setAttribute ("currentPage", pageBean);
        }
        SiteBean siteBean = new SiteBean (jParams.getSite (), jParams);
        request.setAttribute ("currentSite", siteBean);
        
        request.setAttribute ("currentUser", jParams.getUser ());
        
        RequestBean requestBean = new RequestBean (new GuiBean (jParams), jParams);
        request.setAttribute ("currentRequest", requestBean);

        DateBean dateBean = new DateBean(jParams,
                new SimpleDateFormat(CalendarHandler.DEFAULT_DATE_FORMAT));
        request.setAttribute("dateBean", dateBean);

        JahiaBean jahiaBean = new JahiaBean(jParams, siteBean, pageBean, requestBean, dateBean, jParams.getUser());
        request.setAttribute ("currentJahia", jahiaBean);
        request.setAttribute("jahia", jahiaBean);

        boolean isIE = false;
        final String userAgent = jParams.getRequest ().getHeader ("user-agent");
        if (userAgent != null) {
            isIE = (userAgent.indexOf ("IE") != -1);
        }

        if (isIE) {
            request.setAttribute ("isIE", Boolean.TRUE);
        } else {
            request.setAttribute ("isIE", Boolean.FALSE);
        }

        request.setAttribute ("javaScriptPath",
                jParams.settings ().getJsHttpPath ());
        request.setAttribute ("URL",
                getJahiaCoreHttpPath (jParams) +
                jParams.settings ().
                getEnginesContext ());
        request.setAttribute (JahiaEngine.ENGINE_URL_PARAM,
                getJahiaCoreHttpPath (jParams) +
                jParams.settings ().
                getEnginesContext ());
        request.setAttribute ("serverURL",
                getJahiaCoreHttpPath (jParams));
        request.setAttribute ("httpJsContextPath",
                getJahiaCoreHttpPath (jParams) +
                jParams.settings ().
                getJavascriptContext ());
   
    }
    
    /**
     * Build an http path containing the server name for the current site, instead of the path
     * from JahiaPrivateSettings.
     *
     * @return An http path leading to Jahia, built with the server name, and the server port if
     *         nonstandard.
     */
    private String getJahiaCoreHttpPath (final ProcessingContext jParams) {
        return jParams.getContextPath ();
    }
}
