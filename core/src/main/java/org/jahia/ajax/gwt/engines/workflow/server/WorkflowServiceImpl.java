/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.engines.workflow.server;

import org.jahia.ajax.gwt.client.service.workflow.WorkflowService;
import org.jahia.ajax.gwt.engines.workflow.server.helper.WorkflowServiceHelper;
import org.jahia.ajax.gwt.engines.workflow.server.helper.GWTJahiaWorkflowElementComparator;
import org.jahia.ajax.gwt.commons.server.JahiaRemoteService;
import org.jahia.ajax.gwt.client.data.GWTJahiaLabel;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.utils.JahiaObjectCreator;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowBatch;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowElement;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowHistoryEntry;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowManagerState;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.workflow.AbstractActivationJob;
import org.jahia.services.workflow.ActivationJob;
import org.jahia.services.workflow.WorkflowAction;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.params.ProcessingContext;
import org.jahia.params.ParamBean;
import org.jahia.content.ObjectKey;
import org.jahia.content.ContentObject;
import org.jahia.content.ContentObjectKey;
import org.jahia.content.ObjectKeyInterface;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.data.beans.PageBean;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.JobDataMap;

import java.util.*;

import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.BaseListLoadResult;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 17 juil. 2008 - 15:58:14
 */
public class WorkflowServiceImpl extends JahiaRemoteService implements WorkflowService {

    private static final org.apache.log4j.Logger logger = Logger.getLogger(WorkflowServiceImpl.class);

    public final static String WORKFLOW_BATCH = "WorkflowBatch" ;

    /**
     * Retrieve all active languages for the current site.
     *
     * @return a list of ordered language codes
     */
    public List<String> getAvailableLanguages() {
        ProcessingContext jParams = retrieveParamBean() ;
        return WorkflowServiceHelper.retrieveOrderedLanguageCodesForSite(jParams.getSite()) ;
    }

    /**
     * Retrieve all the pages elements for building the content object tree (containers are not displayed)
     *
     * @param parent the parent element
     * @return the children of the given parent element
     */
    public List<GWTJahiaWorkflowElement> getSubElements(GWTJahiaWorkflowElement parent) throws GWTJahiaServiceException {
        ProcessingContext jParams = retrieveParamBean() ;
        // this case should not be used any more
        if (parent == null) {
            if (jParams != null) {
                List<GWTJahiaWorkflowElement> result = new ArrayList<GWTJahiaWorkflowElement>();
                ContentPage homePage = jParams.getSite().getHomeContentPage();
                if (homePage.checkReadAccess(jParams.getUser())) {
                    ObjectKey key = homePage.getObjectKey() ;
                    String title = homePage.getTitle(jParams) ;
                    if (title == null || title.trim().length() == 0) {
                        title = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.workflow.display.notitle", jParams.getLocale());
                    }
                    boolean hasChildren = WorkflowServiceHelper.hasSeparateWorkflowChildren(homePage, jParams.getUser(), true) ;
                    GWTJahiaWorkflowElement workflowElement = new GWTJahiaWorkflowElement(homePage.getID(), key.getKey(), key.getType(), title, title, hasChildren, WorkflowServiceHelper.getWorkflowStates(homePage));
                    try {
                        workflowElement.setAvailableAction(WorkflowServiceHelper.getAvailableActionsForObject((ContentObjectKey) key, workflowElement.getWorkflowStates().keySet(), jParams));
                    } catch (JahiaException e) {
                        logger.error(e.getMessage(), e);
                    }
                    result.add(workflowElement) ;
                }
                return result ;
            } else {
                return new ArrayList<GWTJahiaWorkflowElement>();
            }

        // this is the new default case, retrieving the current page as subroot and crawl ip to the home page or to the first unreadable page
        } else if (parent.getPath() == null && parent.getObjectKey() != null) {
            String objectKey = parent.getObjectKey() ;
            int pid ;
            if (objectKey.length() == 0) {
                pid = jParams.getPageID() ;
            } else {
                String id = parent.getObjectKey().replace(PageBean.TYPE + ObjectKeyInterface.KEY_SEPARATOR, "") ;
                try {
                    pid = Integer.parseInt(id) ;
                } catch (NumberFormatException e) {
                    logger.error(e.getMessage(), e);
                    pid = jParams.getPageID() ;
                }
            }
            List<GWTJahiaWorkflowElement> result = new ArrayList<GWTJahiaWorkflowElement>();
            try {
                List<GWTJahiaWorkflowElement> hierarchyRetrieved = WorkflowServiceHelper.getParentAndSiblingPages(pid, jParams) ;
                if (hierarchyRetrieved != null) {
                    if (hierarchyRetrieved.size() == 0) {
                        logger.debug("There is a problem, no hierarchy could have been retrieved");
                    } else {
                        result.addAll(hierarchyRetrieved) ;
                    }
                } else {
                    logger.debug("Home page is the current page for the workflow tree");
                    ContentPage rootPage = ContentPage.getPage(pid) ;
                    if (rootPage != null) {
                        if (rootPage.checkReadAccess(jParams.getUser())) {
                            if (objectKey.length() == 0) {
                                objectKey = rootPage.getObjectKey().getKey() ;
                            }
                            String title = rootPage.getTitle(jParams) ;
                            if (title == null || title.trim().length() == 0) {
                                title = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.workflow.display.notitle", jParams.getLocale());
                            }
                            boolean hasChildren = WorkflowServiceHelper.hasSeparateWorkflowChildren(rootPage, jParams.getUser(), true) ;
                            GWTJahiaWorkflowElement workflowElement = new GWTJahiaWorkflowElement(pid, objectKey, PageBean.TYPE, title, title, hasChildren, WorkflowServiceHelper.getWorkflowStates(rootPage));
                            workflowElement.setAvailableAction(WorkflowServiceHelper.getAvailableActionsForObject((ContentObjectKey) rootPage.getObjectKey(), workflowElement.getWorkflowStates().keySet(), jParams));
                            result.add(workflowElement) ;
                        } else {
                            if (logger.isDebugEnabled()) {
                                logger.debug("No read access for page") ;
                            }
                        }
                    }
                }
            } catch (JahiaException e) {
                logger.error(e.getMessage(), e);
            }
            return result ;

        // this is the sub content objects case, retrieving separate workflow objects for the given parent
        } else {
            ContentObject object = null ;
            try {
                object = JahiaObjectCreator.getContentObjectFromString(parent.getObjectKey()) ;
            } catch (ClassNotFoundException e) {
                logger.error(e.getMessage(), e);
            }
            return WorkflowServiceHelper.getSeparateWorkflowChildren(parent, object, true, jParams) ;
        }
    }

    /**
     * Retrieve all the worlfow-independent elements for building the content object flattened table.
     *
     * @param parent the parent element
     * @param depth the depth to flatten
     * @return a list of flattened elements
     */
    public List<GWTJahiaWorkflowElement> getFlattenedSubElements(GWTJahiaWorkflowElement parent, int depth) {
        ProcessingContext jParams = retrieveParamBean() ;
        ContentObject object = null ;
        try {
            object = JahiaObjectCreator.getContentObjectFromString(parent.getObjectKey()) ;
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Getting flattened children for parent " + parent.getObjectKey() + " with depth=" + String.valueOf(depth)) ;
        }
        List<GWTJahiaWorkflowElement> result = WorkflowServiceHelper.getSubElementsRec(parent, object, depth, true, jParams) ;
        if (logger.isDebugEnabled()) {
            logger.debug("Retrieved flattened children for parent " + parent.getObjectKey() + " with depth=" + String.valueOf(depth)) ;
        }
        return result ;
    }

    /**
     * Retrieve all the worlfow-independent elements for building the content object flattened table.
     *
     * @param parent the parent element
     * @param depth the depth to flatten
     * @param offset the paging offset
     * @param sortParameter the field to sort with
     * @param isAscending true if ascending order
     * @return a list of flattened elements
     */
    public PagingLoadResult<GWTJahiaWorkflowElement> getPagedFlattenedSubElements(GWTJahiaWorkflowElement parent, int depth, int offset, int pageSize, String sortParameter, boolean isAscending) throws GWTJahiaServiceException {
        try {
            List<GWTJahiaWorkflowElement> sublist ;
            ProcessingContext jParams = retrieveParamBean() ;
            ContentObject object = null ;
            try {
                object = JahiaObjectCreator.getContentObjectFromString(parent.getObjectKey()) ;
            } catch (ClassNotFoundException e) {
                logger.error(e.getMessage(), e);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Getting flattened children for parent " + parent.getObjectKey() + " with depth=" + String.valueOf(depth)) ;
            }

            List<GWTJahiaWorkflowElement> result = new ArrayList<GWTJahiaWorkflowElement>() ;
            for (GWTJahiaWorkflowElement wfEl: WorkflowServiceHelper.getSubElementsRec(parent, object, depth, true, jParams)) {
                if (wfEl.isAccessibleInTable()) { // filter hidden pages (no rights except read)
                    result.add(wfEl) ;
                }
            }

            if (sortParameter != null) {
                Collections.sort(result, new GWTJahiaWorkflowElementComparator<GWTJahiaWorkflowElement>(sortParameter, isAscending));
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Retrieved flattened children for parent " + parent.getObjectKey() + " with depth=" + String.valueOf(depth)) ;
            }
            sublist = new ArrayList<GWTJahiaWorkflowElement>();
            for (int i=offset; i<offset+pageSize; i++) {
                if (i<result.size()) {
                    sublist.add(result.get(i)) ;
                } else {
                    break ;
                }
            }
            return new BasePagingLoadResult<GWTJahiaWorkflowElement>(sublist, offset, result.size());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException("Failed to retrieve workflow elements\n" + e.toString()) ;
        }
    }

    public List<GWTJahiaLabel> getAvailableActions() {
        ProcessingContext jParams = retrieveParamBean() ;
        return WorkflowServiceHelper.getAvailableActions(jParams.getLocale());
    }

    public void storeBatch(Map<String, Map<String, Set<String>>> batch) {
        WorkflowServiceHelper.storeBatch(batch, retrieveParamBean());
    }

    public Map<String, Map<String, Set<String>>> restoreBatch() {
        return WorkflowServiceHelper.restoreBatch(retrieveParamBean());
    }

    public void executeStoredBatch(String name, String comment) {
        Map<String, Map<String, Set<String>>> batch = restoreBatch();
        if (batch != null) {
            execute(new GWTJahiaWorkflowBatch(batch, name, comment));
        }
    }

    public void execute(GWTJahiaWorkflowBatch batch) {
        ParamBean jParams = retrieveParamBean();

        final Class jobClass = ActivationJob.class;

        final JobDetail jobDetail = BackgroundJob.createJahiaJob("Activating", jobClass, jParams);

        List<WorkflowAction> actions = new ArrayList<WorkflowAction>();
        for (String action:  batch.getBatch().keySet()) {
            for (String key: batch.getBatch().get(action).keySet()) {
                try {
                    actions.add(new WorkflowAction((ContentObjectKey) ContentObjectKey.getInstance(key), batch.getBatch().get(action).get(key), action, batch.getComment())) ;
                } catch (ClassNotFoundException e) {
                    logger.error(e.getMessage(), e) ;
                }
            }
        }

//        final Set locks = transferAllLock(jParams, jobDetail.getName(), selectedEntries.keySet());
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
//        jobDataMap.put(BackgroundJob.JOB_LOCKS, locks);

        jobDataMap.put(BackgroundJob.JOB_TYPE, ActivationJob.WORKFLOW_TYPE);
        jobDataMap.put(BackgroundJob.JOB_TITLE, batch.getTitle());
        jobDataMap.put(AbstractActivationJob.COMMENTS_INPUT, batch.getComment());
        jobDataMap.put(ActivationJob.ACTIONS, actions);

        final SchedulerService schedulerServ = ServicesRegistry.getInstance().getSchedulerService();
        try {
            schedulerServ.scheduleJobNow(jobDetail);
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public ListLoadResult<GWTJahiaWorkflowHistoryEntry> getHistory(GWTJahiaWorkflowElement item) {
        ProcessingContext jParams = retrieveParamBean() ;
        List<GWTJahiaWorkflowHistoryEntry> l = WorkflowServiceHelper.getHistory(item, jParams);
        return new BaseListLoadResult<GWTJahiaWorkflowHistoryEntry>(l);
    }

    public void saveWorkflowManagerState(GWTJahiaWorkflowManagerState state) {
        getThreadLocalRequest().getSession().setAttribute("workflow.manager.state", state) ;
    }

    public GWTJahiaWorkflowManagerState getWorkflowManagerState() {
        GWTJahiaWorkflowManagerState workflowState = (GWTJahiaWorkflowManagerState) getThreadLocalRequest().getSession().getAttribute("workflow.manager.state") ;
        getThreadLocalRequest().getSession().removeAttribute("workflow.manager.state");
        if (workflowState != null) {
            Map<String, String> titleForObjectKey = new HashMap<String, String>() ;
            ProcessingContext jParams = retrieveParamBean() ;
            for (String objectKey: workflowState.getTitleForObjectKey().keySet()) {
                try {
                    ContentObject co = JahiaObjectCreator.getContentObjectFromString(objectKey) ;
                    String title ;
                    if (co != null) {
                        title = co.getDisplayName(jParams) ;
                    } else {
                        title = objectKey ;
                    }
                    if (title == null || title.trim().length() == 0) {
                        title = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.workflow.display.notitle", jParams.getLocale());
                    }
                    titleForObjectKey.put(objectKey, title) ;
                } catch (ClassNotFoundException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            workflowState.setTitleForObjectKey(titleForObjectKey);
        } else {
            workflowState = new GWTJahiaWorkflowManagerState(null, null, null, WorkflowServiceHelper.restoreBatch(retrieveParamBean())) ;
        }
        workflowState.setAvailableLanguages(getAvailableLanguages());
        return workflowState ;
    }

}
