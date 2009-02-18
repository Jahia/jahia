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

package org.jahia.ajax.gwt.templates.components.toolbar.server;


import org.jahia.ajax.gwt.engines.pdisplay.server.ProcessDisplayServiceImpl;
import org.jahia.ajax.gwt.engines.workflow.server.helper.WorkflowServiceHelper;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.data.toolbar.*;
import org.jahia.ajax.gwt.commons.server.AbstractJahiaGWTServiceImpl;
import org.jahia.ajax.gwt.client.data.GWTJahiaAjaxActionResult;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.service.toolbar.ToolbarService;
import org.jahia.ajax.gwt.client.data.toolbar.analytics.GWTJahiaAnalyticsParameter;
import org.jahia.ajax.gwt.client.data.toolbar.monitor.GWTJahiaStateInfo;
import org.jahia.ajax.gwt.client.data.toolbar.monitor.GWTJahiaProcessJobInfo;
import org.jahia.ajax.gwt.client.util.ToolbarConstants;
import org.jahia.ajax.gwt.client.widget.toolbar.ToolbarManager;
import org.jahia.ajax.gwt.templates.components.toolbar.server.ajaxaction.AjaxAction;
import org.jahia.ajax.gwt.templates.components.toolbar.server.factory.ItemsGroupFactory;
import org.jahia.ajax.gwt.utils.JahiaObjectCreator;
import org.jahia.data.JahiaData;
import org.jahia.params.ParamBean;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.preferences.JahiaPreferencesProvider;
import org.jahia.services.preferences.exception.JahiaPreferenceProviderException;
import org.jahia.services.preferences.toolbar.ToolbarJahiaPreference;
import org.jahia.services.preferences.toolbar.ToolbarJahiaPreferenceKey;
import org.jahia.services.preferences.toolbar.ToolbarJahiaPreferenceValue;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.services.scheduler.ProcessAction;
import org.jahia.services.toolbar.JahiaToolbarService;
import org.jahia.services.toolbar.bean.*;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.workflow.AbstractActivationJob;
import org.jahia.services.importexport.ActivationContentPickerJob;
import org.jahia.services.importexport.CopyJob;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.SiteLanguageSettings;
import org.jahia.services.pages.ContentPage;
import org.jahia.content.ContentPageKey;
import org.jahia.analytics.data.GAdataCollector;
import org.jahia.exceptions.JahiaException;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;

import java.util.*;


/**
 * User: jahia
 * Date: 4 mars 2008
 * Time: 17:29:58
 */
public class ToolbarServiceImpl extends AbstractJahiaGWTServiceImpl implements ToolbarService {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ToolbarServiceImpl.class);
    private static JahiaPreferencesProvider toolbarPreferencesProvider;

    private static final ServicesRegistry SERVICES_REGISTRY = ServicesRegistry.getInstance();
    private static transient JahiaToolbarService JAHIA_TOOLBAR_SERVICE;
    private static transient SchedulerService SCHEDULER_SERVICE;

    /**
     * Get gwt toolbar for the current user
     *
     * @param pageContext
     * @return
     */
    public GWTJahiaToolbarSet getGWTToolbars(GWTJahiaPageContext pageContext, boolean reset) throws GWTJahiaServiceException {
        try {
            if (reset) {
                getToolbarJahiaPreferencesProvider().deleteAllPreferencesByPrincipal(getRemoteJahiaUser());
            }
            JahiaData jData = retrieveJahiaData(pageContext);
            String value = getGenericPreferenceValue(ToolbarManager.TOOLBARS_DISPLAYED_PREF);
            if (value == null || Boolean.parseBoolean(value)) {
                // there is no pref or toolbar are hided
                // get all tool bars
                ToolbarSet toolbarSet = getToolbarService().getToolbarSet(retrieveParamBean(pageContext));
                Visibility visibility = toolbarSet.getVisibility();
                if ((visibility != null && visibility.getRealValue(jData)) || visibility == null) {
                    GWTJahiaToolbarSet gwtJahiaToolbarSet = createGWTToolbarSet(pageContext, toolbarSet);
                    return gwtJahiaToolbarSet;
                } else {
                    logger.info("Toolbars are not visible.");
                    return null;
                }
            } else {
                logger.info("Toolbars are not displayed");
                return null;
            }
        } catch (Exception e) {
            logger.error(e, e);
            throw new GWTJahiaServiceException("Error during loading toolbars due to " + e.getMessage());
        }
    }

    public GWTJahiaToolbarSet createGWTToolbarSet(GWTJahiaPageContext pageContext, ToolbarSet toolbarSet) {
        if (toolbarSet.getToolbarList() == null || toolbarSet.getToolbarList().isEmpty()) {
            logger.debug("toolbar set list is empty");
            return null;
        }
        JahiaData jData = retrieveJahiaData(pageContext);

        // create  a gwtJahiaToolbarSet
        GWTJahiaToolbarSet gwtJahiaToolbarSet = new GWTJahiaToolbarSet();
        for (Toolbar toolbar : toolbarSet.getToolbarList()) {
            // add only tool bar that the user can view
            Visibility visibility = toolbar.getVisibility();
            if ((visibility != null && visibility.getRealValue(jData)) || visibility == null) {
                GWTJahiaToolbar gwtToolbar = createGWTToolbar(pageContext, toolbar);
                // add toolbar only if not empty
                if (gwtToolbar != null && gwtToolbar.getGwtToolbarItemsGroups() != null && !gwtToolbar.getGwtToolbarItemsGroups().isEmpty()) {
                    gwtJahiaToolbarSet.addGWTToolbar(gwtToolbar);
                }
            } else {
                logger.debug("toolbar: " + toolbar.getType() + ":  not visible");
            }
        }
        return gwtJahiaToolbarSet;

    }

    /**
     * update toolbars
     *
     * @param pageContext
     * @param toolbarList
     */
    public void updateToolbars(GWTJahiaPageContext pageContext, List<GWTJahiaToolbar> toolbarList) throws GWTJahiaServiceException {
        for (GWTJahiaToolbar o : toolbarList) {
            updateToolbar(pageContext, o);
        }
    }

    /**
     * Update toolbar
     *
     * @param pageContext
     * @param gwtToolbar
     * @throws GWTJahiaServiceException
     */
    public void updateToolbar(GWTJahiaPageContext pageContext, GWTJahiaToolbar gwtToolbar) throws GWTJahiaServiceException {
        ToolbarJahiaPreferenceKey key = createToolbarPreferenceKey(gwtToolbar);
        ToolbarJahiaPreferenceValue value = createToolbarJahiaPreferenceValue(gwtToolbar);
        getToolbarJahiaPreferencesProvider().setJahiaPreference(key, value);
    }


    /**
     * load toolbar
     *
     * @param pageContext
     * @return
     */
    public GWTJahiaToolbar loadGWTToolbar(GWTJahiaPageContext pageContext, GWTJahiaToolbar gwtToolbar) {
        ToolbarJahiaPreferenceKey key = createToolbarPreferenceKey(gwtToolbar);
        ToolbarJahiaPreferenceValue value = createToolbarJahiaPreferenceValue(gwtToolbar);
        getToolbarJahiaPreferencesProvider().setJahiaPreference(key, value);
        Toolbar toolbar = getToolbarService().getToolbarByIndex(gwtToolbar.getIndex());
        return createGWTToolbar(pageContext, toolbar);
    }

    /**
     * Execute ItemAjaxAction
     *
     * @param pageContext
     * @param gwtPropertiesMap
     * @return
     */
    public GWTJahiaAjaxActionResult execute(GWTJahiaPageContext pageContext, Map<String, GWTJahiaProperty> gwtPropertiesMap) throws GWTJahiaServiceException {
        final GWTJahiaProperty classActionProperty = gwtPropertiesMap.get(ToolbarConstants.CLASS_ACTION);
        final GWTJahiaProperty actionProperty = gwtPropertiesMap.get(ToolbarConstants.ACTION);

        GWTJahiaAjaxActionResult actionResult = new GWTJahiaAjaxActionResult("");

        // execute actionProperty depending on classActin and the actionProperty
        if (classActionProperty != null) {
            String classActionValue = classActionProperty.getValue();
            if (classActionValue != null && classActionValue.length() > 0) {
                String actionValue = null;
                if (actionProperty != null) {
                    actionValue = actionProperty.getValue();
                }

                // execute action
                try {
                    // remove useless propertiey
                    gwtPropertiesMap.remove(ToolbarConstants.CLASS_ACTION);
                    gwtPropertiesMap.remove(ToolbarConstants.ACTION);
                    JahiaData jData = retrieveJahiaData(pageContext);

                    // execute actionProperty
                    logger.debug("Execute [" + classActionValue + "," + actionValue + "]");
                    AjaxAction ajaxAction = (AjaxAction) Class.forName(classActionValue).newInstance();
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
     * @return the toobar preferences provider
     */
    private JahiaPreferencesProvider getToolbarJahiaPreferencesProvider() {
        try {
            if (toolbarPreferencesProvider == null) {
                toolbarPreferencesProvider = SERVICES_REGISTRY.getJahiaPreferencesService().getPreferencesProviderByType(ToolbarJahiaPreference.PROVIDER_TYPE);
            }
            return toolbarPreferencesProvider;
        } catch (JahiaPreferenceProviderException e) {
            logger.error(e, e);
        }
        return null;
    }


    private GWTJahiaToolbar createGWTToolbar(GWTJahiaPageContext pageContext, Toolbar toolbar) {
        // don't add the tool bar if  has no items group
        if (toolbar.getItemsGroupList() == null || toolbar.getItemsGroupList().isEmpty()) {
            logger.debug("toolbar itemsgroup list is empty");
            return null;
        }

        JahiaData jData = retrieveJahiaData(pageContext);
        ParamBean paramBean = retrieveParamBean(pageContext);

        // create gwtTollbar
        GWTJahiaToolbar gwtToolbar = new GWTJahiaToolbar();
        gwtToolbar.setIndex(toolbar.getIndex());
        gwtToolbar.setName(toolbar.getName());
        gwtToolbar.setTitle(getResources(paramBean, toolbar.getTitleKey()));
        gwtToolbar.setType(toolbar.getType());
        gwtToolbar.setDraggable(toolbar.isDraggable());
        gwtToolbar.setMandatory(toolbar.isMandatory());
        gwtToolbar.setDisplayTitle(toolbar.isDisplayTitle());
        //create and set state
        GWTJahiaState defaultGWTState = new GWTJahiaState();
        String state = toolbar.getState();
        if (state != null) {
            if (state.equalsIgnoreCase("top")) {
                defaultGWTState.setValue(ToolbarConstants.TOOLBAR_TOP);
            } else if (state.equalsIgnoreCase("box")) {
                defaultGWTState.setValue(ToolbarConstants.TOOLBAR_HORIZONTAL_BOX);
            } else if (state.equalsIgnoreCase("right")) {
                defaultGWTState.setValue(ToolbarConstants.TOOLBAR_RIGHT);
            } else {
                defaultGWTState.setValue(ToolbarConstants.TOOLBAR_TOP);
            }

            // by defautl load toolbar if mandatory value is 'true' or displayed value
            defaultGWTState.setDisplay(toolbar.isMandatory() || toolbar.isDisplayed());
        }

        gwtToolbar.setState(createJahiaToolbarState(gwtToolbar, defaultGWTState));

        // load items-group
        List<GWTJahiaToolbarItemsGroup> gwtToolbarItemsGroupList = new ArrayList<GWTJahiaToolbarItemsGroup>();
        int index = 0;
        for (ItemsGroup itemsGroup : toolbar.getItemsGroupList()) {
            // add only itemsgroup that the user can view
            Visibility visibility = itemsGroup.getVisibility();
            if ((visibility != null && visibility.getRealValue(jData)) || visibility == null) {
                GWTJahiaToolbarItemsGroup gwtItemsGroup = createGWTItemsGroup(pageContext, gwtToolbar.getName(), index, itemsGroup);

                // add itemsGroup only if not empty
                if (gwtItemsGroup != null && gwtItemsGroup.getGwtToolbarItems() != null && !gwtItemsGroup.getGwtToolbarItems().isEmpty()) {
                    gwtToolbarItemsGroupList.add(gwtItemsGroup);
                    // other itemsgroup will be lazy loaded
                    if (!gwtToolbar.getState().isDisplay()) {
                        break;
                    }
                }
            } else {
                logger.debug("itemsGroup: " + itemsGroup.getTitleKey() + ":  not visible");
            }
            index++;
        }
        gwtToolbar.setGwtToolbarItemsGroups(gwtToolbarItemsGroupList);

        return gwtToolbar;
    }

    private GWTJahiaState createJahiaToolbarState(GWTJahiaToolbar gwtToolbar, GWTJahiaState defaultState) {
        JahiaPreferencesProvider provider = getToolbarJahiaPreferencesProvider();
        ToolbarJahiaPreferenceKey key = createToolbarPreferenceKey(gwtToolbar);
        if (key != null) {
            ToolbarJahiaPreference preference = (ToolbarJahiaPreference) provider.getJahiaPreference(key);
            if (preference != null && preference.getValue() != null) {
                logger.debug("Preference found for toolbar: " + gwtToolbar.getName());
                ToolbarJahiaPreferenceValue value = (ToolbarJahiaPreferenceValue) preference.getValue();
                GWTJahiaState state = new GWTJahiaState();
                state.setValue(value.getState());
                state.setIndex(value.getIndex());
                state.setPagePositionX(value.getPositionX());
                state.setPagePositionY(value.getPositionY());
                state.setDisplay(gwtToolbar.isMandatory() || value.getDisplay());
                return state;
            }
        }

        return defaultState;
    }

    private ToolbarJahiaPreferenceKey createToolbarPreferenceKey(GWTJahiaToolbar toolbar) {
        JahiaUser remoteJahiaUser = getRemoteJahiaUser();
        ToolbarJahiaPreferenceKey key = new ToolbarJahiaPreferenceKey();
        key.setPrincipal(remoteJahiaUser);
        key.setName(toolbar.getName());
        key.setType(toolbar.getType());
        logger.debug("toolbar: " + toolbar.getType() + "," + toolbar.getName());
        return key;
    }

    private ToolbarJahiaPreferenceValue createToolbarJahiaPreferenceValue(GWTJahiaToolbar gwtToolbar) {
        ToolbarJahiaPreferenceValue value = new ToolbarJahiaPreferenceValue();
        value.setState(gwtToolbar.getState().getValue());
        value.setIndex(gwtToolbar.getState().getIndex());
        value.setPositionX(gwtToolbar.getState().getPagePositionX());
        value.setPositionY(gwtToolbar.getState().getPagePositionY());
        value.setDisplay(gwtToolbar.getState().isDisplay());
        return value;
    }

    private GWTJahiaToolbarItemsGroup createGWTItemsGroup(GWTJahiaPageContext page, String toolbarName, int index, ItemsGroup itemsGroup) {
        // don't add the items group if  has no items group
        if (itemsGroup.getItemList() == null || itemsGroup.getItemList().isEmpty()) {
            logger.debug("itemlist is empty");
            return null;
        }

        JahiaData jData = retrieveJahiaData(page);
        ParamBean paramBean = retrieveParamBean(page);

        List<GWTJahiaToolbarItem> gwtToolbarItemsList = new ArrayList<GWTJahiaToolbarItem>();
        // create items from definition
        for (int i = 0; i < itemsGroup.getItemList().size(); i++) {
            // case of item
            Object o = itemsGroup.getItemList().get(i);
            if (o instanceof Item) {
                Item item = (Item) o;
                // add only item that the user can view
                logger.debug("Item: " + item.getType());
                Visibility visibility = item.getVisibility();

                // add only visible items
                if ((visibility != null && visibility.getRealValue(jData)) || visibility == null) {
                    GWTJahiaToolbarItem gwtToolbarItem = createGWTItem(page, item);
                    if (gwtToolbarItem != null) {
                        gwtToolbarItemsList.add(gwtToolbarItem);
                    }
                } else {
                    logger.debug("Item: " + item.getTitleResourceBundleKey() + ":  not visible");
                }
            }
            // case of items provider
            else if (o instanceof ItemsProvider) {
                ItemsProvider itemsProvider = (ItemsProvider) o;
                String classProvider = itemsProvider.getClassProvider();
                logger.debug("ItemsProvider: " + classProvider);
                try {
                    ItemsGroupFactory groupFactory = (ItemsGroupFactory) Class.forName(classProvider).newInstance();
                    gwtToolbarItemsList = groupFactory.populateItemsList(gwtToolbarItemsList, jData, itemsProvider.getInputProvider(), itemsProvider.getProperties(jData));
                } catch (InstantiationException e) {
                    logger.error("Unable to instanciate, class[" + classProvider + "]");
                } catch (IllegalAccessException e) {
                    logger.error("IllegalAccessException, class[" + classProvider + "]");

                } catch (ClassNotFoundException e) {
                    logger.error("ClassNotFoundException, class[" + classProvider + "]");
                }
            }
        }

        // don't add the items group if  has no items group
        if (gwtToolbarItemsList == null || gwtToolbarItemsList.isEmpty()) {
            logger.debug("itemlist is empty");
            return null;
        }

        // creat items-group
        GWTJahiaToolbarItemsGroup gwtToolbarItemsGroup = new GWTJahiaToolbarItemsGroup();
        gwtToolbarItemsGroup.setId(toolbarName + "_" + index);
        gwtToolbarItemsGroup.setType(itemsGroup.getType());
        gwtToolbarItemsGroup.setLayout(itemsGroup.getLayout());
        gwtToolbarItemsGroup.setNeedSeparator(itemsGroup.isSeparator());
        gwtToolbarItemsGroup.setMediumIconStyle(itemsGroup.getMediumIconStyle());
        gwtToolbarItemsGroup.setMinIconStyle(itemsGroup.getMinIconStyle());
        if (itemsGroup.getTitleKey() != null) {
            gwtToolbarItemsGroup.setItemsGroupTitle(getResources(paramBean, itemsGroup.getTitleKey()));
        }
        gwtToolbarItemsGroup.setGwtToolbarItems(gwtToolbarItemsList);
        return gwtToolbarItemsGroup;
    }

    private GWTJahiaToolbarItem createGWTItem(GWTJahiaPageContext page, Item item) {
        JahiaData jData = retrieveJahiaData(page);
        ParamBean paramBean = retrieveParamBean(page);

        // GWTJahiaToolbarItem
        GWTJahiaToolbarItem gwtToolbarItem = new GWTJahiaToolbarItem();
        gwtToolbarItem.setTitle(getResources(paramBean, item.getTitleResourceBundleKey()));
        gwtToolbarItem.setType(item.getType());
        gwtToolbarItem.setDisplayTitle(item.isDisplayTitle());
        gwtToolbarItem.setDescription(getResources(paramBean, item.getDescriptionResourceBundleKey()));
        gwtToolbarItem.setMediumIconStyle(item.getMediumIconStyle());
        gwtToolbarItem.setMinIconStyle(item.getMinIconStyle());
        if (item.getSelected() != null) {
            gwtToolbarItem.setSelected(item.getSelected().getRealValue(jData));
        } else {
            gwtToolbarItem.setSelected(false);
        }
        Map<String, GWTJahiaProperty> pMap = new HashMap<String, GWTJahiaProperty>();
        for (Property currentProperty : item.getPropertyList()) {
            GWTJahiaProperty gwtProperty = new GWTJahiaProperty();
            gwtProperty.setName(currentProperty.getName());
            gwtProperty.setValue(currentProperty.getRealValue(jData));
            pMap.put(gwtProperty.getName(), gwtProperty);
        }
        gwtToolbarItem.setProperties(pMap);
        return gwtToolbarItem;
    }


    /**
     * Update GWT Jahia State Info
     *
     * @param gwtJahiaStateInfo
     * @return
     */
    public GWTJahiaStateInfo updateGWTJahiaStateInfo(GWTJahiaPageContext pageContext, GWTJahiaStateInfo gwtJahiaStateInfo) throws GWTJahiaServiceException {
        try {
            if (gwtJahiaStateInfo == null) {
                gwtJahiaStateInfo = new GWTJahiaStateInfo();
                gwtJahiaStateInfo.setLastViewTime(System.currentTimeMillis());
                if (gwtJahiaStateInfo.isNeedRefresh()) {
                    gwtJahiaStateInfo.setIconStyle("gwt-toolbar-ItemsGroup-icons-notification-refresh");
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
                GWTJahiaProcessJobInfo gwtProcessJobInfo = updateGWTProcessJobInfo(gwtJahiaStateInfo.getGwtProcessJobInfo(), pageContext.getPid());
                gwtJahiaStateInfo.setGwtProcessJobInfo(gwtProcessJobInfo);
                if (gwtProcessJobInfo.isJobExecuting()) {
                    gwtJahiaStateInfo.setIconStyle("gwt-toolbar-ItemsGroup-icons-wait-min");
                    gwtJahiaStateInfo.setText("Job is running (" + gwtProcessJobInfo.getNumberWaitingJobs() + ") waiting");
                } else if (gwtProcessJobInfo.getNumberWaitingJobs() > 0) {
                    gwtJahiaStateInfo.setIconStyle("gwt-toolbar-ItemsGroup-icons-notification-information");
                    gwtJahiaStateInfo.setText(gwtProcessJobInfo.getNumberWaitingJobs() + " waiting jobs");
                } else {
                    gwtJahiaStateInfo.setIconStyle("gwt-toolbar-ItemsGroup-icons-notification-ok");
                }

                // pdisplay need refresh need refresh
                if (gwtProcessJobInfo.isJobFinished()) {
                    gwtJahiaStateInfo.setAlertMessage(getLocaleJahiaAdminResource("button.monitoring.jobfinished"));

                    // current user job ended
                    if (gwtProcessJobInfo.isCurrentUserJob() && !gwtProcessJobInfo.isSystemJob()) {
                        gwtJahiaStateInfo.setCurrentUserJobEnded(true);
                    } else {
                        gwtJahiaStateInfo.setCurrentUserJobEnded(false);
                    }
                    // do we need to refresh ?
                    if (gwtJahiaStateInfo.isNeedRefresh() || gwtProcessJobInfo.isCurrentPageValidated()) {
                        gwtJahiaStateInfo.setNeedRefresh(true);
                        gwtJahiaStateInfo.setIconStyle("gwt-toolbar-ItemsGroup-icons-notification-refresh");
                        gwtJahiaStateInfo.setText(getLocaleJahiaAdminResource("button.monitoring.reloadPage"));
                        gwtJahiaStateInfo.setRefreshMessage(getLocaleJahiaAdminResource("button.monitoring.reloadPage"));
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
    private GWTJahiaProcessJobInfo updateGWTProcessJobInfo(GWTJahiaProcessJobInfo gwtProcessJobInfo, int currentPageId) throws GWTJahiaServiceException {
        long lastExecutedJob = getSchedulerService().getLastJobCompletedTime();
        if (gwtProcessJobInfo == null) {
            gwtProcessJobInfo = new GWTJahiaProcessJobInfo();
            gwtProcessJobInfo.setLastViewTime(lastExecutedJob);
        }
        boolean isCurrentUser = false;
        boolean isSystemJob = true;
        boolean isCurrentPageValided = false;
        String lastExecutedJobLabel = "";
        String link = null;
        JobDetail lastExecutedJobDetail = getSchedulerService().getLastCompletedJobDetail();
        if (lastExecutedJobDetail != null) {
            link = retrieveParamBean().getContextPath() + "/jsp/jahia/processing/jobreport.jsp?name=" + lastExecutedJobDetail.getName() + "&groupName=" + lastExecutedJobDetail.getGroup();
            JobDataMap lastExecutedJobDataMap = lastExecutedJobDetail.getJobDataMap();
            if (lastExecutedJobDataMap != null) {

                // set 'is current user' flag
                String lastExecutedJobUserKey = lastExecutedJobDataMap.getString(BackgroundJob.JOB_USERKEY);
                if (lastExecutedJobUserKey != null) {
                    isCurrentUser = lastExecutedJobUserKey.equalsIgnoreCase(getRemoteUser());
                }

                // set 'is System Job'
                String lastExecutedJobType = lastExecutedJobDataMap.getString(BackgroundJob.JOB_TYPE);
                if (lastExecutedJobType != null) {
                    // is system job
                    isSystemJob = !lastExecutedJobType.equalsIgnoreCase(AbstractActivationJob.WORKFLOW_TYPE)
                            && !lastExecutedJobType.equalsIgnoreCase(CopyJob.COPYPASTE_TYPE);

                    // workflow
                    if (lastExecutedJobType.equalsIgnoreCase(AbstractActivationJob.WORKFLOW_TYPE)) {
                        lastExecutedJobLabel = getLocaleJahiaEnginesResource("org.jahia.engines.processDisplay.op.workflow.label");
                    } else if (lastExecutedJobType.equalsIgnoreCase(ActivationContentPickerJob.PICKED_TYPE)) {
                        lastExecutedJobLabel = getLocaleJahiaEnginesResource("org.jahia.engines.processDisplay.op.pickercopy.label");
                    } else if (lastExecutedJobType.equalsIgnoreCase(CopyJob.COPYPASTE_TYPE)) {
                        lastExecutedJobLabel = getLocaleJahiaEnginesResource("org.jahia.engines.processDisplay.op.copypaste.label");
                    }
                    
                    // check if current page validated
                    List<ProcessAction> processActionList = (List<ProcessAction>) lastExecutedJobDataMap.get(BackgroundJob.ACTIONS);
                    if (processActionList != null) {
                        for (ProcessAction processAction : processActionList) {
                            if (processAction.getKey() != null) {
                                if (processAction.getKey() instanceof ContentPageKey) {
                                    ContentPageKey contentPageKey = (ContentPageKey) processAction.getKey();
                                    if (contentPageKey.getPageID() == currentPageId) {
                                        isCurrentPageValided = true;
                                        break;
                                    }
                                } else {
                                    try {
                                        if (JahiaObjectCreator.getContentObjectFromKey(processAction.getKey()).getPageID() == currentPageId) {
                                            isCurrentPageValided = true;
                                            break;
                                        }
                                    } catch (ClassNotFoundException e) {
                                        //
                                    }
                                }
                            }
                        }
                    }
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
            List jobList = ProcessDisplayServiceImpl.getAllJobsDetails();
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
                    if (currentJobUserKey != null && getRemoteUser() != null) {
                        if (currentJobUserKey.equalsIgnoreCase(getRemoteUser())) {
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
            String value = getGenericPreferenceValue(ProcessDisplayServiceImpl.PREF_PAGE_REFRESH);
            if (value != null && value.length() > 0) {
                try {
                    pageRefresh = Boolean.parseBoolean(value);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

            }


            // set values
            gwtProcessJobInfo.setCurrentUserJob(isCurrentUser);
            gwtProcessJobInfo.setCurrentPageValidated(isCurrentPageValided);
            gwtProcessJobInfo.setSystemJob(isSystemJob);
            gwtProcessJobInfo.setJobReportUrl(link);
            gwtProcessJobInfo.setJobType(lastExecutedJobLabel);
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
     * Get Toolbar Service
     *
     * @return
     */
    private JahiaToolbarService getToolbarService() {
        if (JAHIA_TOOLBAR_SERVICE == null) {
            JAHIA_TOOLBAR_SERVICE = ServicesRegistry.getInstance().getJahiaToolbarService();
        }
        return JAHIA_TOOLBAR_SERVICE;
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
     * Quick validate the current page/language using the given action.
     *
     * @param objectKey the objectKey corresponding to the current page
     * @param lang      the language code corresponding to the current language
     * @param action    the action to execute
     * @param comment   an optional comment
     * @throws GWTJahiaServiceException
     */
    public void quickValidate(String objectKey, String lang, String action, String comment) throws GWTJahiaServiceException {
        WorkflowServiceHelper.quickValidate(objectKey, lang, action, comment, retrieveParamBean());
    }

    public void quickAddToBatch(String objectKey, String lang, String action) throws GWTJahiaServiceException {
        WorkflowServiceHelper.addToBatch(objectKey, lang, action, retrieveParamBean());
    }

    /**
     * Quick validation of the whole site.
     *
     * @param comment an optional comment
     * @throws GWTJahiaServiceException
     */
    public void publishAll(String comment) throws GWTJahiaServiceException {
        WorkflowServiceHelper.publishAll(comment, retrieveParamBean());
    }

    /*
   * Google analytics   todo should be adapted to the new version
   * */
    public Map<String, String> getGAdata(GWTJahiaAnalyticsParameter p) throws GWTJahiaServiceException {
        ParamBean paramBean = retrieveParamBean();
        JahiaSite currentSite = paramBean.getSite();
        String gaLogin = currentSite.getSettings().getProperty(p.getJahiaGAprofile()+"_"+currentSite.getSiteKey()+"_gaLogin");
        String gaPassword =  currentSite.getSettings().getProperty(p.getJahiaGAprofile()+"_"+currentSite.getSiteKey()+"_gaPassword");
        String gaAccount =  currentSite.getSettings().getProperty(p.getJahiaGAprofile()+"_"+currentSite.getSiteKey()+"_gaUserAccount");
        String profile = currentSite.getSettings().getProperty(p.getJahiaGAprofile()+"_"+currentSite.getSiteKey()+"_gaProfile");
       
        String statType = p.getStatType();
        String dateRange = p.getDateRange();
        String chartType = p.getChartType();
        String siteORpage = p.getSiteORpage();
        //logger.info("getGAdata");
        Map<String, String> data = (new GAdataCollector()).getData(gaLogin, gaPassword, profile, gaAccount.split("-")[1], dateRange, statType, chartType, siteORpage);
        //logger.info("Data ok");
        return data;

    }

    /*
   * Google analytics  
   *
   * */
    public Map<String, String> getGAsiteProperties(int pid) {

        Map<String, String> gaSiteProperties = new HashMap<String, String>();
        //logger.info("retreive ga parameters");
        try {
            org.jahia.services.importexport.ImportExportService ies = SERVICES_REGISTRY.getImportExportService();
            String uuid = ies.getUuid(ContentPage.getPage(pid));
            ParamBean paramBean = retrieveParamBean();
            JahiaSite currentSite = paramBean.getSite();

            List<SiteLanguageSettings> languages = currentSite.getLanguageSettings();
            Iterator it = languages.iterator();
            String lan = "";
            while (it.hasNext()) {
                lan = lan + "#" + ((SiteLanguageSettings) it.next()).getCode();
            }
            gaSiteProperties.put("siteLanguages", lan);
            gaSiteProperties.put("uuid", uuid);
            /*gaSiteProperties.put("gaProfileCustom", currentSite.getSettings().getProperty("gaProfileCustom"));
            gaSiteProperties.put("gaProfileDefault", currentSite.getSettings().getProperty("gaProfileDefault"));*/
            it = ((currentSite.getSettings()).keySet()).iterator();
           // logger.info("###############################################################################");
           // logger.info("----------------------GA settings-----------------------");
            while (it.hasNext()) {
                String key = (String) it.next();
                if (key.startsWith("jahiaGAprofile")) {
                    // logger.info("--------------------------------------------------");
                    //logger.info("profile = " + currentSite.getSettings().get(key));// profile name
                    gaSiteProperties.put("jahiaGAprofileName"+currentSite.getSettings().get(key), (String) currentSite.getSettings().get(key));
                    //logger.info("gaUserAccount = " + currentSite.getSettings().getProperty(currentSite.getSettings().get(key) + "_" + currentSite.getSiteKey() + "_gaUserAccount"));
                    gaSiteProperties.put(currentSite.getSettings().get(key)+"#gaUserAccount", currentSite.getSettings().getProperty(currentSite.getSettings().get(key) + "_" + currentSite.getSiteKey() + "_gaUserAccount"));
                   // logger.info("gaProfile = " + currentSite.getSettings().getProperty(currentSite.getSettings().get(key) + "_" + currentSite.getSiteKey() + "_gaProfile"));
                    gaSiteProperties.put(currentSite.getSettings().get(key)+"#gaProfile", currentSite.getSettings().getProperty(currentSite.getSettings().get(key) + "_" + currentSite.getSiteKey() + "_gaProfile"));
                    //logger.info("gaLogin = " + currentSite.getSettings().getProperty(currentSite.getSettings().get(key) + "_" + currentSite.getSiteKey() + "_gaLogin"));
                    gaSiteProperties.put(currentSite.getSettings().get(key)+"#gaLogin", currentSite.getSettings().getProperty(currentSite.getSettings().get(key) + "_" + currentSite.getSiteKey() + "_gaLogin"));
                    //logger.info("gaPassword = " + currentSite.getSettings().getProperty(currentSite.getSettings().get(key) + "_" + currentSite.getSiteKey() + "_gaPassword"));
                    //gaSiteProperties.put(currentSite.getSettings().get(key)+"#gaPassword", currentSite.getSettings().getProperty(currentSite.getSettings().get(key) + "_" + currentSite.getSiteKey() + "_gaPassword"));
                    //logger.info("trackedUrls = " + currentSite.getSettings().getProperty(currentSite.getSettings().get(key) + "_" + currentSite.getSiteKey() + "_trackedUrls"));
                    gaSiteProperties.put(currentSite.getSettings().get(key)+"#trackedUrls", currentSite.getSettings().getProperty(currentSite.getSettings().get(key) + "_" + currentSite.getSiteKey() + "_trackedUrls"));
                    //logger.info("trackingEnabled = " + currentSite.getSettings().getProperty(currentSite.getSettings().get(key) + "_" + currentSite.getSiteKey() + "_trackingEnabled"));
                    gaSiteProperties.put(currentSite.getSettings().get(key)+"#trackingEnabled", currentSite.getSettings().getProperty(currentSite.getSettings().get(key) + "_" + currentSite.getSiteKey() + "_trackingEnabled"));
                    //logger.info("--------------------------------------------------");

                }
            }
            //logger.info("###############################################################################");
        } catch (JahiaException e) {
            logger.error("ga parameters' retreiving failure");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return gaSiteProperties;
    }

    public boolean isTracked() { //todo should be adapted to the new version

        ParamBean paramBean = retrieveParamBean();
        JahiaSite currentSite = paramBean.getSite();
        Iterator it = ((currentSite.getSettings()).keySet()).iterator();
        // check if at least one profile is configured
        boolean oneConfigured = false;
        while (it.hasNext()) {
            String key = (String) it.next();
            if (key.startsWith("jahiaGAprofile")) {
                    oneConfigured = true;
                    break;
            }
        }
        return oneConfigured;
    }


}
