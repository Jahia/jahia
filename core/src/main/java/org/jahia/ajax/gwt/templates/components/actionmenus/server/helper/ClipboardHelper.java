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
package org.jahia.ajax.gwt.templates.components.actionmenus.server.helper;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.actionmenu.actions.GWTJahiaAction;
import org.jahia.ajax.gwt.utils.JahiaObjectCreator;
import org.jahia.exceptions.JahiaException;
import org.jahia.data.beans.ContainerListBean;
import org.jahia.data.beans.ContainerBean;
import org.jahia.data.containers.JahiaContainerDefinition;
import org.jahia.params.ProcessingContext;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.lock.LockRegistry;
import org.jahia.services.lock.LockKey;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.services.importexport.CopyJob;
import org.jahia.registries.ServicesRegistry;
import org.jahia.content.ContentObject;
import org.jahia.content.ContentObjectKey;
import org.jahia.content.StructuralRelationship;
import org.quartz.JobDetail;
import org.quartz.JobDataMap;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import java.util.Set;
import java.util.HashSet;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 27 f�vr. 2008 - 15:01:25
 */
public class ClipboardHelper {

    private final static Logger logger = Logger.getLogger(ClipboardHelper.class) ;

    /**
     * Check if there is an object stored in the clipboard, and if it is compatible with the given destination.
     *
     * @param session the current session
     * @param jParams the processing context
     * @param destObjectKey the destination object key
     * @return true if allowed, false otherwise
     * @throws org.jahia.exceptions.JahiaException sthg bad happened
     */
    public static String isPasteAllowed(HttpSession session, ProcessingContext jParams, String destObjectKey) throws JahiaException {
        final String skey = (String) session.getAttribute(GWTJahiaAction.CLIPBOARD_CONTENT);

        // is there an object to paste ?
        if (skey == null) {
            return null ;
        }

        // source has to be a container, and target has to be a container list (TODO is that actually true ?)
        // TODO make this more general, a little hardcoded here sorry but the 'JahiaMess' is overwhelming :)
        if (!destObjectKey.startsWith(ContainerListBean.TYPE) || !skey.startsWith(ContainerBean.TYPE)) { // this will allow containers and containerlists to be pasted (skey)
            return null ;
        }

        String type ;
        if (skey.contains(ContainerListBean.TYPE)) {
            type = ActionMenuLabelProvider.CONTAINER_LIST ;
        } else {
            type = ActionMenuLabelProvider.CONTAINER ;
        }

        try {
            final ContentContainerList destContainerList = (ContentContainerList) JahiaObjectCreator.getContentObjectFromString(destObjectKey) ;
            final ContentObject clipSource = JahiaObjectCreator.getContentObjectFromString(skey) ;
            if (clipSource != null && clipSource.hasActiveOrStagingEntries() && !clipSource.isMarkedForDelete()) {
                final JahiaContainerDefinition dDef = destContainerList.getJahiaContainerList(jParams, jParams.getEntryLoadRequest()).getDefinition();
                if (clipSource instanceof ContentContainer) {
                    if (ServicesRegistry.getInstance().getImportExportService().isCompatible(dDef, (ContentContainer) clipSource, jParams)) {
                        ContentObject current = destContainerList ;
                        while (current != null) {
                            if (current.getObjectKey().equals(clipSource.getObjectKey())) {
                                break;
                            }
                            current = current.getParent(EntryLoadRequest.STAGED);
                        }
                        return current == null ? type : null ;
                    }
                } else if (clipSource instanceof ContentContainerList) {
                    final JahiaContainerDefinition sDef = ((ContentContainerList)clipSource).getJahiaContainerList(jParams, jParams.getEntryLoadRequest()).getDefinition();
                    if (ServicesRegistry.getInstance().getImportExportService().isCompatible(dDef, sDef)) {
                        ContentObject current = destContainerList ;
                        while (current != null) {
                            if (current.getObjectKey().equals(clipSource.getObjectKey())) {
                                break;
                            }
                            current = current.getParent(EntryLoadRequest.STAGED);
                        }
                        return current == null ? type : null ;
                    }
                }

            }
        } catch (ClassNotFoundException e) {
            logger.error("Can't check paste status", e);
        }
        return null ;
    }

    /**
     * Check the clipboard state to display an appropriate icon.
     *
     * @param request the current request
     * @return true ir empty, false otherwise
     */
    public static boolean clipboardIsEmpty(HttpServletRequest request) {
        Object clipboardContent = request.getSession().getAttribute(GWTJahiaAction.CLIPBOARD_CONTENT) ;
        return clipboardContent == null ;
    }

    public static boolean clipboardContentHasActiveEntry(HttpSession session, Locale locale) {
        final String skey = (String) session.getAttribute(GWTJahiaAction.CLIPBOARD_CONTENT);

        // is there an object to paste ?
        if (skey != null) {
            try {
                ContentObject obj = JahiaObjectCreator.getContentObjectFromString(skey) ;
                if (obj != null) {
                    ContentObjectEntryState state = ContentObjectEntryState.getEntryState(1, locale.toString());
                    return obj.getEntryState(state, false, false) != null;
                }
            } catch (ClassNotFoundException e) {
                logger.error(e, e) ;
            } catch (JahiaException e) {
                logger.error(e, e) ;
            }
        }
        return false ;
    }

    /**
     * Copy action store the selected object key in the session.
     *
     * @param session the current session
     * @param jParams the processing context
     * @param objectKey the object key to copy
     * @return true if copy succeeded
     */
    public static boolean clipboardCopy(HttpSession session, ProcessingContext jParams, String objectKey) {
        String oldkey = (String) session.getAttribute(GWTJahiaAction.CLIPBOARD_CONTENT);
        LockRegistry lockRegistry = LockRegistry.getInstance();
        try {
            if (oldkey != null) {
                ContentObjectKey k = (ContentObjectKey) ContentObjectKey.getInstance(oldkey);
                LockKey lock = LockKey.composeLockKey(LockKey.EXPORT_ACTION + "_" + k.getType(), k.getIdInType());
                lockRegistry.release(lock, jParams.getUser(), jParams.getUser().getUserKey());
            }
            ContentObjectKey k = (ContentObjectKey) ContentObjectKey.getInstance(objectKey);
            LockKey lock = LockKey.composeLockKey(LockKey.EXPORT_ACTION + "_" + k.getType(), k.getIdInType());

            if (lockRegistry.acquire(lock, jParams.getUser(), jParams.getUser().getUserKey(), BackgroundJob.getMaxExecutionTime())) {
                session.setAttribute(GWTJahiaAction.CLIPBOARD_CONTENT, objectKey) ;
                return true ;
            } else {
                session.removeAttribute(GWTJahiaAction.CLIPBOARD_CONTENT) ;
                logger.info("Cannot acquire lock, do not copy");
                return false ;
            }
        } catch (ClassNotFoundException e) {
            logger.error("Cannot use object key", e);
            session.removeAttribute(GWTJahiaAction.CLIPBOARD_CONTENT) ;
            return false ;
        }
    }

    /**
     * Paste action use import/export to copy the object corresponding to the object key stored in the session.
     *
     * @param session the current session
     * @param processingContext the processing context
     * @param destObjectKey the object key to copy
     * @param linkedCopy true to make a linked copy, false to make a real copy
     * @return true if paste succeeded, false otherwise
     */
    public static boolean clipboardPaste(HttpSession session, ProcessingContext processingContext, String destObjectKey, boolean linkedCopy) {
        final String skey = (String) session.getAttribute(GWTJahiaAction.CLIPBOARD_CONTENT);
        final LockRegistry lockRegistry = LockRegistry.getInstance();
        try {
            final ContentObject source = ContentObject.getContentObjectInstance(ContentObjectKey.getInstance(skey));

            if (source != null && source.hasActiveOrStagingEntries() && !source.isMarkedForDelete()) {
                final ContentObject dest = ContentObject.getContentObjectInstance(ContentObjectKey.getInstance(destObjectKey));

                Class jobClass = CopyJob.class;

                JobDetail jobDetail = BackgroundJob.createJahiaJob("Copy "+skey+ " to "+destObjectKey, jobClass, processingContext);

                JobDataMap jobDataMap = jobDetail.getJobDataMap();
                jobDataMap.put(CopyJob.SOURCE, source.getObjectKey().toString());
                jobDataMap.put(CopyJob.DEST, dest.getObjectKey().toString());
                if (linkedCopy) {
                    jobDataMap.put(CopyJob.LINK, StructuralRelationship.ACTIVATION_PICKER_LINK);
                }
                jobDataMap.put(CopyJob.SITESOURCE, ServicesRegistry.getInstance().getJahiaSitesService().getSite(source.getSiteID()).getSiteKey());
                jobDataMap.put(BackgroundJob.JOB_DESTINATION_SITE, ServicesRegistry.getInstance().getJahiaSitesService().getSite(dest.getSiteID()).getSiteKey());
                jobDataMap.put(BackgroundJob.JOB_TYPE, linkedCopy ? CopyJob.PICKERCOPY_TYPE : CopyJob.COPYPASTE_TYPE);

                synchronized(lockRegistry) {
                    boolean acq = true;
                    // transfer locks to job
                    Set<LockKey> locks = new HashSet<LockKey>();
                    LockKey lock = LockKey.composeLockKey(LockKey.EXPORT_ACTION + "_" + source.getObjectKey().getType(), source.getID());
                    lockRegistry.release(lock, processingContext.getUser(), processingContext.getUser().getUserKey());
                    lock = LockKey.composeLockKey(LockKey.EXPORT_ACTION + "_" + source.getObjectKey().getType(), source.getID());
                    locks.add(lock);
                    acq &= lockRegistry.acquire(lock, processingContext.getUser(), jobDetail.getName(), BackgroundJob.getMaxExecutionTime(), false);
                    lock = LockKey.composeLockKey(LockKey.ADD_ACTION + "_" + dest.getObjectKey().getType(), dest.getID());
                    locks.add(lock);
                    acq &= lockRegistry.acquire(lock, processingContext.getUser(), jobDetail.getName(), BackgroundJob.getMaxExecutionTime(), false);
                    if (!acq) {
                        logger.info("Cannot acquire lock, do not paste");
                        return false ;
                    }
                    jobDataMap.put(BackgroundJob.JOB_LOCKS, locks);
                }
                SchedulerService schedulerServ = ServicesRegistry.getInstance().getSchedulerService();
                schedulerServ.scheduleJobNow(jobDetail);
            }
            session.removeAttribute(GWTJahiaAction.CLIPBOARD_CONTENT) ;
            return true ;
        } catch (Exception e) {
            logger.error("Paste error", e) ;
            return false ;
        }
    }

}
