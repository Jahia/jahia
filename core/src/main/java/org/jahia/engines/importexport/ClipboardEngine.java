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
 package org.jahia.engines.importexport;

import org.jahia.content.ContentObject;
import org.jahia.content.ContentObjectKey;
import org.jahia.data.JahiaData;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.params.SessionState;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.importexport.CopyJob;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockRegistry;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.containers.ContentContainer;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 25 aout 2005
 * Time: 15:04:17
 * @version $Id$
 */
public class ClipboardEngine implements JahiaEngine {
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(JahiaEngine.class);

    private JahiaSitesService siteservice;

    /**
     * Default constructor, creates a new <code>ClipboardEngine</code> instance.
     */
    public ClipboardEngine() {
    }

    public JahiaSitesService getSiteservice() {
        return siteservice;
    }

    public void setSiteservice(JahiaSitesService siteservice) {
        this.siteservice = siteservice;
    }

    public boolean authoriseRender(ProcessingContext processingContext) {
        return true;
    }

    public String renderLink(final ProcessingContext processingContext, final Object theObj) throws JahiaException {
        return processingContext.composeEngineUrl("clipboard", "?key=" + ((ContentObject) theObj).getObjectKey());
    }

    public boolean needsJahiaData(ProcessingContext processingContext) {
        return false;
    }

    public EngineValidationHelper handleActions(final ProcessingContext processingContext, final JahiaData jData) throws JahiaException {
        final String op = processingContext.getParameter("cop");
        final String key = processingContext.getParameter("key");

        logger.debug("ClipboardEngine - op: " + op + ", key: " + key);

        final SessionState sessionState = processingContext.getSessionState();
        LockRegistry lockRegistry = LockRegistry.getInstance();
        if ("cut".equals(op) || "copy".equals(op)) {
            String oldkey = (String) sessionState.getAttribute("clipboard_key");
            try {
                if (oldkey != null) {
                    ContentObjectKey k = (ContentObjectKey) ContentObjectKey.getInstance(oldkey);
                    LockKey lock = LockKey.composeLockKey(LockKey.EXPORT_ACTION + "_" + k.getType(), k.getIdInType(), k.getIdInType());
                    lockRegistry.release(lock, processingContext.getUser(), processingContext.getUser().getUserKey());
                }
                ContentObjectKey k = (ContentObjectKey) ContentObjectKey.getInstance(key);
                LockKey lock = LockKey.composeLockKey(LockKey.EXPORT_ACTION + "_" + k.getType(), k.getIdInType(), k.getIdInType());

                OutputStream os = ((ParamBean)processingContext).getResponse().getOutputStream();
                Writer w = new OutputStreamWriter(os);

                if (lockRegistry.acquire(lock, processingContext.getUser(), processingContext.getUser().getUserKey(), BackgroundJob.getMaxExecutionTime())) {
                    sessionState.setAttribute("clipboard_op", op);
                    sessionState.setAttribute("clipboard_key", key);
                    w.write("copied");
                } else {
                    logger.info("Cannot acquire lock, do not copy");
                    w.write("locked");
                }
                w.close();
            } catch (Exception e) {
                logger.error("Cannot write response",e);
            }
        } else if ("paste".equals(op)) {
            try {
                sessionState.removeAttribute("clipboard_op");

                final String skey = (String) processingContext.getSessionState().getAttribute("clipboard_key");
                sessionState.removeAttribute("clipboard_key");
                final ContentObject source = ContentObject.getContentObjectInstance(ContentObjectKey.getInstance(skey));

                OutputStream os = ((ParamBean)processingContext).getResponse().getOutputStream();
                Writer w = new OutputStreamWriter(os);

                if (source != null && ((ContentContainer) source).getJahiaContainer(processingContext, processingContext.getEntryLoadRequest()) != null && !source.isMarkedForDelete()) {
                    final String dkey = processingContext.getParameter("key");
                    final ContentObject dest = ContentObject.getContentObjectInstance(ContentObjectKey.getInstance(dkey));

                    Class jobClass = CopyJob.class;

                    JobDetail jobDetail = BackgroundJob.createJahiaJob("Copy "+skey+ " to "+dkey, jobClass, processingContext);

                    JobDataMap jobDataMap;
                    jobDataMap = jobDetail.getJobDataMap();
                    jobDataMap.put(CopyJob.SOURCE, source.getObjectKey().toString());
                    jobDataMap.put(CopyJob.DEST, dest.getObjectKey().toString());
                    jobDataMap.put(CopyJob.SITESOURCE, siteservice.getSite(source.getSiteID()).getSiteKey());
                    jobDataMap.put(BackgroundJob.JOB_DESTINATION_SITE, siteservice.getSite(dest.getSiteID()).getSiteKey());
                    jobDataMap.put(BackgroundJob.JOB_TYPE, CopyJob.COPYPASTE_TYPE);

                    synchronized(lockRegistry) {
                        boolean acq = true;
                        // transfer locks to job
                        Set locks = new HashSet();
                        LockKey lock = LockKey.composeLockKey(LockKey.EXPORT_ACTION + "_" + source.getObjectKey().getType(), source.getID(), source.getID());
                        lockRegistry.release(lock, processingContext.getUser(), processingContext.getUser().getUserKey());
                        lock = LockKey.composeLockKey(LockKey.EXPORT_ACTION + "_" + source.getObjectKey().getType(), source.getID(), source.getID());
                        locks.add(lock);
                        acq &= lockRegistry.acquire(lock, processingContext.getUser(), jobDetail.getName(), BackgroundJob.getMaxExecutionTime(), false);
                        lock = LockKey.composeLockKey(LockKey.ADD_ACTION + "_" + dest.getObjectKey().getType(), dest.getID(), dest.getID());
                        locks.add(lock);
                        acq &= lockRegistry.acquire(lock, processingContext.getUser(), jobDetail.getName(), BackgroundJob.getMaxExecutionTime(), false);
                        if (!acq) {
                            logger.info("Cannot acquire lock, do not paste");
                            w.write("locked");
                            w.close();
                            return null;
                        }
                        jobDataMap.put(BackgroundJob.JOB_LOCKS, locks);
                    }

                    SchedulerService schedulerServ = ServicesRegistry.getInstance().getSchedulerService();
                    schedulerServ.scheduleJobNow(jobDetail);
                    w.write("pasted");
                } else {
                    w.write("cancelled");
                }
                w.close();
            } catch (Exception e) {
                logger.error("Cannot write response",e);
            }
        }
        return null;
    }

    public String getName() {
        return "clipboard";
    }
}

