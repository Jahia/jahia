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
package org.jahia.ajax.gwt.helper;

import org.jahia.ajax.gwt.client.data.GWTJahiaAjaxActionResult;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.data.toolbar.*;
import org.jahia.ajax.gwt.client.data.toolbar.monitor.GWTJahiaProcessJobInfo;
import org.jahia.ajax.gwt.client.data.toolbar.monitor.GWTJahiaStateInfo;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.util.Constants;
import org.jahia.ajax.gwt.client.widget.toolbar.action.ActionItem;
import org.jahia.ajax.gwt.client.widget.toolbar.action.LanguageSwitcherActionItem;
import org.jahia.ajax.gwt.client.widget.toolbar.action.WorkflowActionItem;
import org.jahia.ajax.gwt.engines.pdisplay.server.ProcessDisplayServiceImpl;
import org.jahia.ajax.gwt.templates.components.toolbar.server.ajaxaction.AjaxAction;
import org.jahia.bin.Jahia;
import org.jahia.data.JahiaData;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.uicomponents.bean.Visibility;
import org.jahia.services.uicomponents.bean.contentmanager.Column;
import org.jahia.services.uicomponents.bean.contentmanager.Repository;
import org.jahia.services.uicomponents.bean.editmode.*;
import org.jahia.services.uicomponents.bean.editmode.EngineTab;
import org.jahia.services.uicomponents.bean.editmode.SidePanelTab;
import org.jahia.services.preferences.JahiaPreferencesService;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.services.uicomponents.bean.editmode.Engine;
import org.jahia.services.uicomponents.bean.contentmanager.ManagerConfiguration;
import org.jahia.services.uicomponents.bean.toolbar.*;
import org.jahia.services.uicomponents.bean.toolbar.Item;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.workflow.WorkflowDefinition;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Apr 13, 2010
 * Time: 5:25:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class UIConfigHelper {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(UIConfigHelper.class);
    private static Map<String, Class<?>> CLASS_CACHE = new HashMap<String, Class<?>>();
    private static transient SchedulerService SCHEDULER_SERVICE;
    private LanguageHelper languages;
    private JahiaPreferencesService preferencesService;

    public void setPreferencesService(JahiaPreferencesService preferencesService) {
        this.preferencesService = preferencesService;
    }

    public void setLanguages(LanguageHelper languages) {
        this.languages = languages;
    }

    /**
     * Get gwt toolbar for the current user
     *
     * @return
     */
    public GWTJahiaToolbarSet getGWTToolbarSet(JCRSiteNode site, JahiaUser jahiaUser, Locale locale, Locale uiLocale, HttpServletRequest request, String toolbarGroup) throws GWTJahiaServiceException {
        try {
            // there is no pref or toolbar are hided
            // get all tool bars
            ToolbarSet toolbarSet = (ToolbarSet) SpringContextSingleton.getBean(toolbarGroup);
            Visibility visibility = toolbarSet.getVisibility();
            if ((visibility != null && visibility.getRealValue(site, jahiaUser, locale, request)) || visibility == null) {
                GWTJahiaToolbarSet gwtJahiaToolbarSet = createGWTToolbarSet(site, jahiaUser, locale, uiLocale, request, toolbarSet);
                return gwtJahiaToolbarSet;
            } else {
                logger.info("Toolbar are not visible.");
                return null;
            }

        } catch (Exception e) {
            logger.error(e, e);
            throw new GWTJahiaServiceException("Error during loading toolbars due to " + e.getMessage());
        }
    }

    /**
     * create gwt toolabr set
     *
     * @param toolbarSet
     * @return
     */
    public GWTJahiaToolbarSet createGWTToolbarSet(JCRSiteNode site, JahiaUser jahiaUser, Locale locale, Locale uiLocale, HttpServletRequest request, ToolbarSet toolbarSet) {
        if (toolbarSet.getToolbars() == null || toolbarSet.getToolbars().isEmpty()) {
            logger.debug("toolbar set list is empty");
            return null;
        }

        // create  a gwtJahiaToolbarSet
        GWTJahiaToolbarSet gwtJahiaToolbarSet = new GWTJahiaToolbarSet();
        for (Toolbar toolbar : toolbarSet.getToolbars()) {
            // add only tool bar that the user can view
            Visibility visibility = toolbar.getVisibility();
            if ((visibility != null && visibility.getRealValue(site, jahiaUser, locale, request)) || visibility == null) {
                GWTJahiaToolbar gwtToolbar = createGWTToolbar(site, jahiaUser, locale, uiLocale, request, toolbar);
                // add toolbar only if not empty
                if (gwtToolbar != null && gwtToolbar.getGwtToolbarItemsGroups() != null && !gwtToolbar.getGwtToolbarItemsGroups().isEmpty()) {
                    gwtJahiaToolbarSet.addGWTToolbar(gwtToolbar);
                } else {
                    logger.debug("[" + (gwtToolbar != null) + "," + (gwtToolbar.getGwtToolbarItemsGroups() != null) + "," + (!gwtToolbar.getGwtToolbarItemsGroups().isEmpty()) + "]" + " toolbar: " + toolbar.getName() + " has no items -->  not visible");
                }
            } else {
                logger.debug("toolbar: " + toolbar.getName() + ":  not visible");
            }
        }
        return gwtJahiaToolbarSet;

    }

    /**
     * Execute action
     * ToDo:  remove JahiaData
     *
     * @param gwtPropertiesMap
     * @return
     * @throws GWTJahiaServiceException
     */
    public GWTJahiaAjaxActionResult execute(JahiaData jData, Map<String, GWTJahiaProperty> gwtPropertiesMap) throws GWTJahiaServiceException {
        final GWTJahiaProperty classActionProperty = gwtPropertiesMap.get(Constants.CLASS_ACTION);
        final GWTJahiaProperty actionProperty = gwtPropertiesMap.get(Constants.ACTION);

        GWTJahiaAjaxActionResult actionResult = new GWTJahiaAjaxActionResult("");

        // execute actionProperty depending on classAction and the actionProperty
        if (classActionProperty != null) {
            String classActionValue = classActionProperty.getValue();
            if (classActionValue != null && classActionValue.length() > 0) {
                String actionValue = null;
                if (actionProperty != null) {
                    actionValue = actionProperty.getValue();
                }

                // execute action
                try {
                    // remove useless properties
                    gwtPropertiesMap.remove(Constants.CLASS_ACTION);
                    gwtPropertiesMap.remove(Constants.ACTION);

                    // execute actionProperty
                    if (logger.isDebugEnabled()) {
                        logger.debug("Execute [" + classActionValue + "," + actionValue + "]");
                    }
                    AjaxAction ajaxAction = (AjaxAction) getClassInstance(classActionValue);
                    return ajaxAction.execute(jData, actionValue, gwtPropertiesMap);
                } catch (Exception e) {
                    logger.error(e, e);
                }


            } else {
                logger.info("Class Action property found but EMPTY");
            }

        } else {
            logger.info("Class Action property not found");
        }
        return actionResult;
    }

    /**
     * Update GWT Jahia State Info
     *
     * @param gwtJahiaStateInfo
     * @return
     */
    public GWTJahiaStateInfo updateGWTJahiaStateInfo(JCRSiteNode site, JahiaUser jahiaUser, Locale uiLocale, GWTJahiaStateInfo gwtJahiaStateInfo) throws GWTJahiaServiceException {
        try {
            if (gwtJahiaStateInfo == null) {
                gwtJahiaStateInfo = new GWTJahiaStateInfo();
                gwtJahiaStateInfo.setLastViewTime(System.currentTimeMillis());
                if (gwtJahiaStateInfo.isNeedRefresh()) {
                    gwtJahiaStateInfo.setIconStyle("gwt-toolbar-icon-notification-refresh");
                }
            } else {
                if (gwtJahiaStateInfo.isNeedRefresh()) {
                    return gwtJahiaStateInfo;
                }
            }

            // remove last alert message
            gwtJahiaStateInfo.setAlertMessage(null);

            // check pdisplay
            if (gwtJahiaStateInfo.isCheckProcessInfo()) {
                GWTJahiaProcessJobInfo gwtProcessJobInfo = updateGWTProcessJobInfo(jahiaUser, gwtJahiaStateInfo.getGwtProcessJobInfo());
                gwtJahiaStateInfo.setGwtProcessJobInfo(gwtProcessJobInfo);
                if (gwtProcessJobInfo.isJobExecuting()) {
                    gwtJahiaStateInfo.setIconStyle("gwt-toolbar-icon-wait-min");
                    gwtJahiaStateInfo.setText("Job is running (" + gwtProcessJobInfo.getNumberWaitingJobs() + ") waiting");
                } else if (gwtProcessJobInfo.getNumberWaitingJobs() > 0) {
                    gwtJahiaStateInfo.setIconStyle("gwt-toolbar-icon-notification-information");
                    gwtJahiaStateInfo.setText(gwtProcessJobInfo.getNumberWaitingJobs() + " waiting jobs");
                } else {
                    gwtJahiaStateInfo.setIconStyle("gwt-toolbar-icon-notification-ok");
                }

                // pdisplay need refresh need refresh
                if (gwtProcessJobInfo.isJobFinished()) {
                    gwtJahiaStateInfo.setAlertMessage(getResources("label.processManagering.jobfinished", uiLocale, site));

                    // current user job ended
                    if (gwtProcessJobInfo.isCurrentUserJob() && !gwtProcessJobInfo.isSystemJob()) {
                        gwtJahiaStateInfo.setCurrentUserJobEnded(true);
                    } else {
                        gwtJahiaStateInfo.setCurrentUserJobEnded(false);
                    }
                    // do we need to refresh ?
                    if (gwtJahiaStateInfo.isNeedRefresh() || gwtProcessJobInfo.isCurrentPageValidated()) {
                        gwtJahiaStateInfo.setNeedRefresh(true);
                        gwtJahiaStateInfo.setIconStyle("gwt-toolbar-icon-notification-refresh");
                        gwtJahiaStateInfo.setText(getResources("label.processManagering.reloadPage", uiLocale, site));
                        gwtJahiaStateInfo.setRefreshMessage(getResources("label.processManagering.reloadPage", uiLocale, site));
                    }

                } else {
                    gwtJahiaStateInfo.setCurrentUserJobEnded(false);
                    gwtJahiaStateInfo.setNeedRefresh(false);
                }
            }


            return gwtJahiaStateInfo;
        } catch (Exception e) {
            logger.error("Error when triing to load Jahia state info due to", e);
            throw new GWTJahiaServiceException("Error when triing to load Jahia state.");
        }
    }

    /**
     * Get Process Job stat
     *
     * @return
     */
    private GWTJahiaProcessJobInfo updateGWTProcessJobInfo(JahiaUser jahiaUser, GWTJahiaProcessJobInfo gwtProcessJobInfo) throws GWTJahiaServiceException {
        long lastExecutedJob = getSchedulerService().getLastJobCompletedTime();
        if (gwtProcessJobInfo == null) {
            gwtProcessJobInfo = new GWTJahiaProcessJobInfo();
            gwtProcessJobInfo.setLastViewTime(lastExecutedJob);
        }
        boolean isCurrentUser = false;
        boolean isSystemJob = true;
        boolean isCurrentPageValided = false;
        String lastExecutedJobLabel = "";
        String lastExecutionJobTitle = "";
        String link = null;
        JobDetail lastExecutedJobDetail = getSchedulerService().getLastCompletedJobDetail();
        if (lastExecutedJobDetail != null) {
            link = Jahia.getContextPath() + "/processing/jobreport.jsp?name=" + lastExecutedJobDetail.getName() + "&groupName=" + lastExecutedJobDetail.getGroup();
            JobDataMap lastExecutedJobDataMap = lastExecutedJobDetail.getJobDataMap();
            if (lastExecutedJobDataMap != null) {

                // set 'is current user' flag
                String lastExecutedJobUserKey = lastExecutedJobDataMap.getString(BackgroundJob.JOB_USERKEY);
                if (lastExecutedJobUserKey != null) {
                    isCurrentUser = lastExecutedJobUserKey.equalsIgnoreCase(jahiaUser.getUserKey());
                }

                // set title if any
                lastExecutionJobTitle = lastExecutedJobDataMap.getString(BackgroundJob.JOB_TITLE);

                // set 'is System Job'
                String lastExecutedJobType = lastExecutedJobDataMap.getString(BackgroundJob.JOB_TYPE);
                if (lastExecutedJobType != null) {
                    // is system job
//                    isSystemJob = !lastExecutedJobType.equalsIgnoreCase(AbstractActivationJob.WORKFLOW_TYPE);

                    // workflow
//                    if (lastExecutedJobType.equalsIgnoreCase(AbstractActivationJob.WORKFLOW_TYPE)) {
//                        lastExecutedJobLabel = getLocaleJahiaEnginesResource("org.jahia.engines.processDisplay.op.workflow.label");
//                    }
                }
            }
        }


        // current executing jobs
        try {

            List currentlyExecutingJobs = getSchedulerService().getCurrentlyExecutingJobs();
            if (currentlyExecutingJobs != null && currentlyExecutingJobs.size() > 0) {
                gwtProcessJobInfo.setJobExecuting(true);
            } else {
                gwtProcessJobInfo.setJobExecuting(false);
            }
            // get all job list
            List jobList = ProcessDisplayServiceImpl.getAllActiveJobsDetails();
            int waitingJobNumber = 0;
            int nextJobCurrentUserIndex = -1;
            String nextJobCurrentUserType = "-";
            int maxIndex = jobList.size();
            gwtProcessJobInfo.setNumberJobs(maxIndex);
            for (int jobIndex = 0; jobIndex < maxIndex; jobIndex++) {
                JobDetail currentJobDetail = (JobDetail) jobList.get(jobIndex);
                JobDataMap currentJobDataMap = currentJobDetail.getJobDataMap();
                // job: type
                String type = currentJobDataMap.getString(BackgroundJob.JOB_TYPE);

                // job: status
                String currentJobStatus = currentJobDataMap.getString(BackgroundJob.JOB_STATUS);

                // job: user key
                String currentJobUserKey = currentJobDataMap.getString(BackgroundJob.JOB_USERKEY);
                if (currentJobStatus.equalsIgnoreCase(BackgroundJob.STATUS_WAITING)) {
                    waitingJobNumber++;
                    if (currentJobUserKey != null && jahiaUser != null) {
                        if (currentJobUserKey.equalsIgnoreCase(jahiaUser.getUserKey())) {
                            if (nextJobCurrentUserIndex == -1) {
                                nextJobCurrentUserType = type;
                            }
                        } else {
                            // update nex job index
                            nextJobCurrentUserIndex++;
                        }
                    }
                }
                if (currentJobStatus.equalsIgnoreCase(BackgroundJob.STATUS_RUNNING)) {
                    // update nex job index
                    nextJobCurrentUserIndex++;
                    nextJobCurrentUserType = type;

                }
            }

            // set need to refresh flag
            gwtProcessJobInfo.setJobFinished(gwtProcessJobInfo.getLastViewTime() < lastExecutedJob);

            boolean pageRefresh = false;
            String value = preferencesService.getGenericPreferenceValue(ProcessDisplayServiceImpl.PREF_PAGE_REFRESH, jahiaUser);
            if (value != null && value.length() > 0) {
                try {
                    pageRefresh = Boolean.parseBoolean(value);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

            }


            // set values
            gwtProcessJobInfo.setCurrentUserJob(isCurrentUser);
            gwtProcessJobInfo.setCurrentPageValidated(false);
            gwtProcessJobInfo.setSystemJob(isSystemJob);
            gwtProcessJobInfo.setJobReportUrl(link);
            if (lastExecutionJobTitle == null) {
                lastExecutionJobTitle = "";
            }
            gwtProcessJobInfo.setJobType(lastExecutedJobLabel);
            gwtProcessJobInfo.setLastTitle(lastExecutionJobTitle);
            gwtProcessJobInfo.setAutoRefresh(pageRefresh);
            gwtProcessJobInfo.setLastViewTime(lastExecutedJob);
            gwtProcessJobInfo.setNumberWaitingJobs(waitingJobNumber);
            gwtProcessJobInfo.setNextJobCurrentUserIndex(nextJobCurrentUserIndex);
            gwtProcessJobInfo.setNextJobCurrentUserType(nextJobCurrentUserType);
        } catch (Exception e) {
            logger.error("Unable to get number of running job.", e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
        return gwtProcessJobInfo;
    }


    /**
     * Get Scheduler Service
     *
     * @return
     */
    private SchedulerService getSchedulerService() {
        if (SCHEDULER_SERVICE == null) {
            SCHEDULER_SERVICE = ServicesRegistry.getInstance().getSchedulerService();
        }
        return SCHEDULER_SERVICE;
    }

    /**
     * Create object from the given className
     *
     * @param className
     * @return
     */
    private Object getClassInstance(String className) {
        Class<?> clazz = CLASS_CACHE.get(className);
        if (null == clazz) {
            synchronized (UIConfigHelper.class) {
                if (null == clazz) {
                    try {
                        clazz = Class.forName(className);
                        CLASS_CACHE.put(className, clazz);
                    } catch (ClassNotFoundException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            }
        }

        Object classInstance = null;
        try {
            classInstance = clazz.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalArgumentException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
        return classInstance;
    }


    /**
     * Create gwt toolbar
     *
     * @param toolbar
     * @return
     */
    public GWTJahiaToolbar createGWTToolbar(JCRSiteNode site, JahiaUser jahiaUser, Locale locale, Locale uiLocale, HttpServletRequest request, Toolbar toolbar) {
        if (toolbar == null) {
            logger.debug("Toolbar parameter is null.");
            return null;
        }

        // don't add the tool bar if  has no items group
        if (toolbar.getItems() == null || toolbar.getItems().isEmpty()) {
            logger.debug("toolbar[" + toolbar.getName() + "] itemsgroup list is empty");
            return null;
        }

        // create gwtTollbar
        GWTJahiaToolbar gwtToolbar = new GWTJahiaToolbar();
        gwtToolbar.setName(toolbar.getName());
        gwtToolbar.setTitle(getResources(toolbar.getTitleKey(), uiLocale != null ? uiLocale : locale, site));
        gwtToolbar.setType(toolbar.getType());
        gwtToolbar.setDisplayTitle(toolbar.isDisplayTitle());
        gwtToolbar.setContextMenu(toolbar.isContextMenu());

        // load items-group
        List<GWTJahiaToolbarItemsGroup> gwtToolbarItemsGroupList = new ArrayList<GWTJahiaToolbarItemsGroup>();
        int index = 0;
        for (Item item : toolbar.getItems()) {
            ItemsGroup itemsGroup = null;
            if (item instanceof ItemsGroup) {
                itemsGroup = (ItemsGroup) item;
            } else {
                // create a single item group
                itemsGroup = new ItemsGroup();
                itemsGroup.addItem(item);
                itemsGroup.setLayout("button-label");
                itemsGroup.setVisibility(item.getVisibility());
            }

            // add only itemsgroup that the user can view
            Visibility visibility = itemsGroup.getVisibility();
            if ((visibility != null && visibility.getRealValue(site, jahiaUser, locale, request)) || visibility == null) {
                GWTJahiaToolbarItemsGroup gwtItemsGroup = createGWTItemsGroup(site, jahiaUser, locale, uiLocale, request, gwtToolbar.getName(), index, itemsGroup);

                // add itemsGroup only if not empty
                if (gwtItemsGroup != null && gwtItemsGroup.getGwtToolbarItems() != null && !gwtItemsGroup.getGwtToolbarItems().isEmpty()) {
                    gwtToolbarItemsGroupList.add(gwtItemsGroup);

                }
            } else {
                logger.debug("toolbar[" + gwtToolbar.getName() + "] - itemsGroup [" + itemsGroup.getId() + "," + itemsGroup.getTitleKey() + "]  not visible");
            }

            index++;
        }
        gwtToolbar.setGwtToolbarItemsGroups(gwtToolbarItemsGroupList);

        return gwtToolbar;
    }

    /**
     * Get manage configuration
     *
     * @param site
     * @param jahiaUser
     * @param locale
     * @param uiLocale
     * @param request
     * @param name
     * @return
     * @throws GWTJahiaServiceException
     */
    public GWTManagerConfiguration getGWTManagerConfiguration(JCRSiteNode site, JahiaUser jahiaUser, Locale locale, Locale uiLocale, HttpServletRequest request, String name) throws GWTJahiaServiceException {
        try {
            ManagerConfiguration config = (ManagerConfiguration) SpringContextSingleton.getBean(name);
            if (config != null) {
                logger.debug("Config. " + name + " found.");
                GWTManagerConfiguration gwtConfig = new GWTManagerConfiguration();
                gwtConfig.setName(name);

                //  set all properties
                gwtConfig.setNodeTypes(config.getNodeTypes());
                gwtConfig.setFolderTypes(config.getFolderTypes());
                gwtConfig.setEnableTextMenu(config.isEnableTextMenu());
                gwtConfig.setSelectedAccordion(config.getSelectedAccordion());
                gwtConfig.setHideLeftPanel(config.isHideLeftPanel());
                gwtConfig.setFolderTypes(config.getFolderTypes());
                gwtConfig.setNodeTypes(config.getNodeTypes());
                gwtConfig.setFilters(config.getFilters());
                gwtConfig.setMimeTypes(config.getMimeTypes());
                gwtConfig.setDefaultView(config.getDefaultView());
                gwtConfig.setEnableFileDoubleClick(config.isEnableFileDoubleClick());
                gwtConfig.setDisplaySize(config.isDisplaySize());
                gwtConfig.setDisplayExt(config.isDisplayExt());
                gwtConfig.setDisplayLock(config.isDisplayLock());
                gwtConfig.setDisplayDate(config.isDisplayDate());
                gwtConfig.setDisplayProvider(config.isDisplayProvider());
                gwtConfig.setUseCheckboxForSelection(config.isUseCheckboxForSelection());
                gwtConfig.setExpandRoot(config.isExpandRoot());
                gwtConfig.setDisplaySearch(config.isDisplaySearch());
                gwtConfig.setDisplaySearchInPage(config.isDisplaySearchInPage());
                gwtConfig.setDisplaySearchInTag(config.isDisplaySearchInTag());
                gwtConfig.setDisplaySearchInFile(config.isDisplaySearchInFile());
                gwtConfig.setDisplaySearchInContent(config.isDisplaySearchInContent());

                // set toolbar
                gwtConfig.setToolbarSet(createGWTToolbarSet(site, jahiaUser, locale, uiLocale, request, config.getToolbarSet()));

                // add table columns
                for (Column item : config.getTableColumns()) {
                    if (checkVisibility(site, jahiaUser, locale, request, item.getVisibility())) {
                        GWTColumn col = new GWTColumn();
                        col.setKey(item.getKey());
                        if (item.getTitleKey() != null) {
                            col.setTitle(getResources(item.getTitleKey(), uiLocale != null ? uiLocale : locale, site));
                        } else if (item.getDeclaringNodeType() != null) {
                            try {
                                ExtendedPropertyDefinition epd = NodeTypeRegistry.getInstance().getNodeType(item.getDeclaringNodeType()).getPropertyDefinition(item.getKey());
                                col.setTitle(epd.getLabel(uiLocale != null ? uiLocale : locale));
                            } catch (Exception e) {
                                logger.error("Cannot get node type name", e);
                                col.setTitle(item.getKey());
                            }
                        } else if (item.getTitle() != null) {
                            col.setTitle(item.getTitle());
                        } else {
                            col.setTitle(item.getKey());
                        }
                        col.setSize(item.getSize());
                        gwtConfig.addColumn(col);
                    }
                }

                // add tabs
                for (EngineTab item : config.getTabs()) {
                    if (checkVisibility(site, jahiaUser, locale, request, item.getVisibility())) {
                        gwtConfig.addTab(item.getKey());
                    }

                }

                // add accordion panels
                for (Repository item : config.getAccordionPanels()) {
                    if (checkVisibility(site, jahiaUser, locale, request, item.getVisibility())) {
                        gwtConfig.addAccordion(item.getKey());
                    }
                }


                return gwtConfig;
            } else {
                logger.error("Config. " + name + " not found.");
                throw new GWTJahiaServiceException("Config. " + name + " not found.");
            }
        } catch (GWTJahiaServiceException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }


    /**
     * Create gwt items group
     *
     * @param toolbarName
     * @param index
     * @param itemsGroup
     * @return
     */
    private GWTJahiaToolbarItemsGroup createGWTItemsGroup(JCRSiteNode site, JahiaUser jahiaUser, Locale locale, Locale uiLocale, HttpServletRequest request, String toolbarName, int index, ItemsGroup itemsGroup) {
        // don't add the items group if  has no items group
        List<Item> list = itemsGroup.getRealItems(site, jahiaUser, locale);
        if (list == null || list.isEmpty()) {
            logger.debug("toolbar[" + toolbarName + "] itemlist is empty");
            return null;
        }


        List<GWTJahiaToolbarItem> gwtToolbarItemsList = new ArrayList<GWTJahiaToolbarItem>();
        // create items from definition
        for (Item item : list) {
            addToolbarItem(site, jahiaUser, locale, uiLocale, request, gwtToolbarItemsList, item);
        }

        // don't add the items group if  has no items group
        if (gwtToolbarItemsList == null || gwtToolbarItemsList.isEmpty()) {
            logger.debug("toolbar[" + toolbarName + "] itemlist is empty");
            return null;
        }

        // creat items-group
        GWTJahiaToolbarItemsGroup gwtToolbarItemsGroup = new GWTJahiaToolbarItemsGroup();
        gwtToolbarItemsGroup.setId(toolbarName + "_" + index);
        gwtToolbarItemsGroup.setType(itemsGroup.getId());
        gwtToolbarItemsGroup.setLayout(getLayoutAsInt(itemsGroup.getLayout()));

        gwtToolbarItemsGroup.setNeedSeparator(itemsGroup.isSeparator());
        gwtToolbarItemsGroup.setMediumIconStyle(itemsGroup.getMediumIconStyle());
        gwtToolbarItemsGroup.setMinIconStyle(itemsGroup.getMinIconStyle());
        if (itemsGroup.getTitleKey() != null) {
            gwtToolbarItemsGroup.setItemsGroupTitle(getResources(itemsGroup.getTitleKey(), uiLocale != null ? uiLocale : locale, site));
        } else {
            gwtToolbarItemsGroup.setItemsGroupTitle(itemsGroup.getTitle());
        }
        gwtToolbarItemsGroup.setGwtToolbarItems(gwtToolbarItemsList);
        return gwtToolbarItemsGroup;
    }


    /**
     * Get layout as int
     *
     * @param layout
     * @return
     */
    private int getLayoutAsInt(String layout) {
        int layoutInt = -1;
        if (layout != null) {
            if (layout.equalsIgnoreCase("button")) {
                layoutInt = Constants.LAYOUT_BUTTON;
            } else if (layout.equalsIgnoreCase("label")) {
                layoutInt = Constants.LAYOUT_ONLY_LABEL;
            } else if (layout.equalsIgnoreCase("button-label")) {
                layoutInt = Constants.LAYOUT_BUTTON_LABEL;
            } else if (layout.equalsIgnoreCase("menu")) {
                layoutInt = Constants.LAYOUT_ITEMSGROUP_MENU;
            } else if (layout.equalsIgnoreCase("menu-radio")) {
                layoutInt = Constants.LAYOUT_ITEMSGROUP_MENU_RADIO;
            } else if (layout.equalsIgnoreCase("menu-checkbox")) {
                layoutInt = Constants.LAYOUT_ITEMSGROUP_MENU_CHECKBOX;
            } else {
                logger.debug("Warning: layout " + layout + " unknown.");
            }
        }
        return layoutInt;
    }

    /**
     * Add item
     *
     * @param gwtToolbarItemsList
     * @param item
     */
    private void addToolbarItem(JCRSiteNode site, JahiaUser jahiaUser, Locale locale, Locale uiLocale, HttpServletRequest request, List<GWTJahiaToolbarItem> gwtToolbarItemsList, Item item) {
        if (item instanceof ItemsGroup) {
            for (Item subItem : ((ItemsGroup) item).getRealItems(site, jahiaUser, locale)) {
                addToolbarItem(site, jahiaUser, locale, uiLocale, request, gwtToolbarItemsList, subItem);
            }
        } else {
            // add only item that the user can view
            logger.debug("Item: " + item.getId());
            Visibility visibility = item.getVisibility();

            // add only visible items
            if ((visibility != null && visibility.getRealValue(site, jahiaUser, locale, request)) || visibility == null) {
                GWTJahiaToolbarItem gwtToolbarItem = createGWTItem(site, jahiaUser, locale, uiLocale, request, item);
                if (gwtToolbarItem != null) {
                    gwtToolbarItemsList.add(gwtToolbarItem);
                }
            } else {
                logger.debug("Item: " + item.getTitleKey() + ":  not visible");
            }
        }
    }


    /**
     * Create gwt item
     *
     * @param item
     * @return
     */
    private GWTJahiaToolbarItem createGWTItem(JCRSiteNode site, JahiaUser jahiaUser, Locale locale, Locale uiLocale, HttpServletRequest request, Item item) {
        // GWTJahiaToolbarItem
        GWTJahiaToolbarItem gwtToolbarItem = new GWTJahiaToolbarItem();
        if (item.getTitleKey() != null) {
            gwtToolbarItem.setTitle(getResources(item.getTitleKey(), uiLocale != null ? uiLocale : locale, site));
        } else {
            gwtToolbarItem.setTitle(item.getTitle());
        }
        gwtToolbarItem.setType(item.getId());
        gwtToolbarItem.setDisplayTitle(item.isDisplayTitle());
        if (item.getDescriptionKey() != null) {
            gwtToolbarItem.setDescription(getResources(item.getDescriptionKey(), uiLocale != null ? uiLocale : locale, site));
        } else {
            gwtToolbarItem.setDescription(gwtToolbarItem.getTitle());
        }
        gwtToolbarItem.setMediumIconStyle(item.getMediumIconStyle());
        gwtToolbarItem.setMinIconStyle(item.getMinIconStyle());
        if (item.getSelected() != null) {
            gwtToolbarItem.setSelected(item.getSelected().getRealValue(site, jahiaUser, locale));
        } else {
            gwtToolbarItem.setSelected(false);
        }
        Map<String, GWTJahiaProperty> pMap = new HashMap<String, GWTJahiaProperty>();
        for (Property currentProperty : item.getProperties()) {
            GWTJahiaProperty gwtProperty = new GWTJahiaProperty();
            gwtProperty.setName(currentProperty.getName());
            gwtProperty.setValue(currentProperty.getRealValue(site, jahiaUser, locale));
            pMap.put(gwtProperty.getName(), gwtProperty);
        }
        gwtToolbarItem.setLayout(getLayoutAsInt(item.getLayout()));
        gwtToolbarItem.setProperties(pMap);


        if (item.getWorkflowAction() != null) {
            try {
                List<WorkflowDefinition> def = WorkflowService.getInstance().getWorkflowsForAction(item.getWorkflowAction());
                List<String> processes = new ArrayList<String>();
                for (WorkflowDefinition workflowDefinition : def) {
                    processes.add(workflowDefinition.getKey());
                }
                gwtToolbarItem.setProcesses(processes);
                // todo : use the role assigned to the action for bypassing workflow ?
                final WorkflowActionItem workflowActionItem = new WorkflowActionItem(processes, jahiaUser.isAdminMember(0), item.getActionItem());
                gwtToolbarItem.setMinIconStyle("gwt-toolbar-icon-workflow-start");
                gwtToolbarItem.setActionItem(workflowActionItem);
            } catch (RepositoryException e) {
                logger.error("Cannot get workflows", e);
            }
        } else {
            ActionItem actionItem = item.getActionItem();
            if (actionItem instanceof LanguageSwitcherActionItem) {
                ((LanguageSwitcherActionItem) actionItem).setSelectedLang(languages.getCurrentLang(uiLocale));
                ((LanguageSwitcherActionItem) actionItem).setLanguages(languages.getLanguages(site, jahiaUser, uiLocale));
            }

            gwtToolbarItem.setActionItem(actionItem);
        }

        return gwtToolbarItem;
    }

    /**
     * Get edit configuration
     *
     * @return
     * @throws GWTJahiaServiceException
     */
    public GWTEditConfiguration getGWTEditConfiguration(JCRSiteNode site, JahiaUser jahiaUser, Locale locale, Locale uiLocale, HttpServletRequest request, String name) throws GWTJahiaServiceException {
        try {
            EditConfiguration config = (EditConfiguration) SpringContextSingleton.getBean(name);
            if (config != null) {
                GWTEditConfiguration gwtConfig = new GWTEditConfiguration();
                gwtConfig.setName(config.getName());
                gwtConfig.setTopToolbar(createGWTToolbar(site, jahiaUser, locale, uiLocale, request, config.getTopToolbar()));
                gwtConfig.setContextMenu(createGWTToolbar(site, jahiaUser, locale, uiLocale, request, config.getContextMenu()));
                gwtConfig.setTabs(createGWTSidePanelTabList(site, jahiaUser, locale, uiLocale, request, config.getTabs()));
                gwtConfig.setCreateEngines(createGWTEngineList(site, jahiaUser, locale, request, config.getCreateEngines()));
                gwtConfig.setEditEngines(createGWTEngineList(site, jahiaUser, locale, request, config.getEditEngines()));
                return gwtConfig;
            } else {
                throw new GWTJahiaServiceException("Bean. 'editconfig'  not found in spring config file");
            }
        } catch (GWTJahiaServiceException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    /**
     * Create GWTSidePanelTab list
     *
     * @param site
     * @param jahiaUser
     * @param locale
     * @param uiLocale
     * @param request
     * @param tabs
     * @return
     */
    private List<GWTSidePanelTab> createGWTSidePanelTabList(JCRSiteNode site, JahiaUser jahiaUser, Locale locale, Locale uiLocale, HttpServletRequest request, List<SidePanelTab> tabs) {
        // create side panel tabs
        List<GWTSidePanelTab> gwtSidePanelTabList = new ArrayList<GWTSidePanelTab>();
        for (SidePanelTab sidePanelTab : tabs) {
            if (checkVisibility(site, jahiaUser, locale, request, sidePanelTab.getVisibility())) {
                final GWTSidePanelTab gwtSidePanel = new GWTSidePanelTab(sidePanelTab.getKey());
                gwtSidePanel.setTooltip(getResources("label." + sidePanelTab.getKey() + "Tab", uiLocale, site));
                gwtSidePanel.setTreeContextMenu(createGWTToolbar(site, jahiaUser, locale, uiLocale, request, sidePanelTab.getTreeContextMenu()));
                gwtSidePanel.setTableContextMenu(createGWTToolbar(site, jahiaUser, locale, uiLocale, request, sidePanelTab.getTableContextMenu()));
                gwtSidePanel.setParams(sidePanelTab.getParams());
                gwtSidePanelTabList.add(gwtSidePanel);
            }
        }
        return gwtSidePanelTabList;
    }

    /**
     * Create gwt engine list
     *
     * @param site
     * @param jahiaUser
     * @param locale
     * @param request
     * @param engines
     * @return
     */
    private List<GWTEngine> createGWTEngineList(JCRSiteNode site, JahiaUser jahiaUser, Locale locale, HttpServletRequest request, List<Engine> engines) {
        // edit engine
        List<GWTEngine> gwtEngineList = new ArrayList<GWTEngine>();
        for (Engine engine : engines) {
            if (checkVisibility(site, jahiaUser, locale, request, engine.getVisibility())) {
                final GWTEngine gwtEngine = new GWTEngine();
                gwtEngine.setNodeType(engine.getNodeType());

                final List<String> engineTabs = new ArrayList<String>();
                for (EngineTab engineTab : engine.getTabs()) {
                    if (checkVisibility(site, jahiaUser, locale, request, engineTab.getVisibility())) {
                        engineTabs.add(engineTab.getKey());
                    }
                }
                gwtEngine.setTabs(engineTabs);
                gwtEngineList.add(gwtEngine);
            }
        }
        return gwtEngineList;
    }

    /**
     * Get resources
     *
     * @param key
     * @param locale
     * @param site
     * @return
     */
    private String getResources(String key, Locale locale, JCRSiteNode site) {
        if (logger.isDebugEnabled()) {
            logger.debug("Resources key: " + key);
        }
        if (key == null || key.length() == 0) {
            return key;
        }
        String value = new JahiaResourceBundle(locale, site != null ? site.getTemplatePackageName() : null).get(key, null);
        if (value == null || value.length() == 0) {
            value = JahiaResourceBundle.getJahiaInternalResource(key, locale);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Resources value: " + value);
        }

        return value;
    }


    /**
     * Return tru
     *
     * @param visibility
     * @return
     */
    private boolean checkVisibility(JCRSiteNode site, JahiaUser jahiaUser, Locale locale, HttpServletRequest request, Visibility visibility) {
        return visibility == null || (visibility != null && visibility.getRealValue(site, jahiaUser, locale, request));
    }


}
