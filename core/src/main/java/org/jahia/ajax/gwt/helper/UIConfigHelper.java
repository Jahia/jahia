/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.data.toolbar.*;
import org.jahia.ajax.gwt.client.data.toolbar.monitor.GWTJahiaStateInfo;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.util.Constants;
import org.jahia.ajax.gwt.client.widget.toolbar.action.ActionItem;
import org.jahia.ajax.gwt.client.widget.toolbar.action.LanguageSwitcherActionItem;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.services.uicomponents.bean.Visibility;
import org.jahia.services.uicomponents.bean.contentmanager.Column;
import org.jahia.services.uicomponents.bean.contentmanager.ManagerConfiguration;
import org.jahia.services.uicomponents.bean.contentmanager.Repository;
import org.jahia.services.uicomponents.bean.editmode.EditConfiguration;
import org.jahia.services.uicomponents.bean.editmode.Engine;
import org.jahia.services.uicomponents.bean.editmode.EngineTab;
import org.jahia.services.uicomponents.bean.editmode.SidePanelTab;
import org.jahia.services.uicomponents.bean.toolbar.*;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.i18n.JahiaResourceBundle;

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

    public void setLanguages(LanguageHelper languages) {
        this.languages = languages;
    }

    /**
     * Get gwt toolbar for the current user
     *
     * @return
     */
    public GWTJahiaToolbar getGWTToolbarSet(JCRSiteNode site, JahiaUser jahiaUser, Locale locale, Locale uiLocale, HttpServletRequest request, String toolbarGroup) throws GWTJahiaServiceException {
        try {
            // there is no pref or toolbar are hided
            // get all tool bars
            Toolbar toolbar = (Toolbar) SpringContextSingleton.getBean(toolbarGroup);
            Visibility visibility = toolbar.getVisibility();
            if ((visibility != null && visibility.getRealValue(site, jahiaUser, locale, request)) || visibility == null) {
                return createGWTToolbar(site, jahiaUser, locale, uiLocale, request, toolbar);
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
    public List<GWTJahiaToolbar> createGWTToolbarSet(JCRSiteNode site, JahiaUser jahiaUser, Locale locale, Locale uiLocale, HttpServletRequest request, List<Toolbar> toolbarSet) {
        if (toolbarSet == null || toolbarSet.isEmpty()) {
            logger.debug("toolbar set list is empty");
            return null;
        }

        // create  a gwtJahiaToolbarSet
        List<GWTJahiaToolbar> gwtJahiaToolbarSet = new ArrayList<GWTJahiaToolbar>();
        for (Toolbar toolbar : toolbarSet) {
            // add only tool bar that the user can view
            Visibility visibility = toolbar.getVisibility();
            if ((visibility != null && visibility.getRealValue(site, jahiaUser, locale, request)) || visibility == null) {
                GWTJahiaToolbar gwtToolbar = createGWTToolbar(site, jahiaUser, locale, uiLocale, request, toolbar);
                // add toolbar only if not empty
                if (gwtToolbar != null && gwtToolbar.getGwtToolbarItems() != null && !gwtToolbar.getGwtToolbarItems().isEmpty()) {
                    gwtJahiaToolbarSet.add(gwtToolbar);
                } else {
                    logger.debug("[" + (gwtToolbar != null) + "," + (gwtToolbar.getGwtToolbarItems() != null) + "," + (!gwtToolbar.getGwtToolbarItems().isEmpty()) + "]" + " toolbar: " + toolbar.getName() + " has no items -->  not visible");
                }
            } else {
                logger.debug("toolbar: " + toolbar.getName() + ":  not visible");
            }
        }
        return gwtJahiaToolbarSet;

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


            return gwtJahiaStateInfo;
        } catch (Exception e) {
            logger.error("Error when triing to load Jahia state info due to", e);
            throw new GWTJahiaServiceException("Error when triing to load Jahia state.");
        }
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
        gwtToolbar.setDisplayTitle(toolbar.isDisplayTitle());

        // load items-group
        List<GWTJahiaToolbarItem> gwtToolbarItemsGroupList = new ArrayList<GWTJahiaToolbarItem>();
        int index = 0;
        for (Item item : toolbar.getItems()) {
            // add only itemsgroup that the user can view
            Visibility visibility = item.getVisibility();
            if ((visibility != null && visibility.getRealValue(site, jahiaUser, locale, request)) || visibility == null) {
                if (item instanceof Menu) {
                    GWTJahiaToolbarMenu gwtMenu = createGWTItemsGroup(site, jahiaUser, locale, uiLocale, request, gwtToolbar.getName(), index, (Menu) item);
                    // add itemsGroup only if not empty
                    if (gwtMenu != null && gwtMenu.getGwtToolbarItems() != null && !gwtMenu.getGwtToolbarItems().isEmpty()) {
                        gwtToolbarItemsGroupList.add(gwtMenu);
                    }
                } else {
                    GWTJahiaToolbarItem gwtItem = createGWTItem(site, jahiaUser, locale, uiLocale, request, item);
                    if (gwtItem != null) {
                        gwtToolbarItemsGroupList.add(gwtItem);
                    }
                }
            } else {
                logger.debug("toolbar[" + gwtToolbar.getName() + "] - itemsGroup [" + item.getId() + "," + item.getTitleKey() + "]  not visible");
            }

            index++;
        }
        gwtToolbar.setGwtToolbarItems(gwtToolbarItemsGroupList);
        int barLayout = getLayoutAsInt(toolbar.getLayout());
        if (barLayout == -1) {
            barLayout = 0;
        }
        for (GWTJahiaToolbarItem gwtJahiaToolbarItem : gwtToolbarItemsGroupList) {
            if (gwtJahiaToolbarItem.getLayout() == -1) {
                gwtJahiaToolbarItem.setLayout(barLayout);
            }
        }

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
                gwtConfig.setHideLeftPanel(config.isHideLeftPanel());
                gwtConfig.setFilters(config.getFilters());
                gwtConfig.setMimeTypes(config.getMimeTypes());
                gwtConfig.setDefaultView(config.getDefaultView());
                gwtConfig.setEnableFileDoubleClick(config.isEnableFileDoubleClick());
                gwtConfig.setUseCheckboxForSelection(config.isUseCheckboxForSelection());
                gwtConfig.setExpandRoot(config.isExpandRoot());
                gwtConfig.setDisplaySearch(config.isDisplaySearch());
                gwtConfig.setDisplaySearchInPage(config.isDisplaySearchInPage());
                gwtConfig.setDisplaySearchInTag(config.isDisplaySearchInTag());
                gwtConfig.setDisplaySearchInFile(config.isDisplaySearchInFile());
                gwtConfig.setDisplaySearchInContent(config.isDisplaySearchInContent());

                // set toolbar
                gwtConfig.setToolbars(createGWTToolbarSet(site, jahiaUser, locale, uiLocale, request, config.getToolbars()));
                gwtConfig.setContextMenu(createGWTToolbar(site, jahiaUser, locale, uiLocale, request, config.getContextMenu()));

                // add table columns
                for (Column item : config.getTableColumns()) {
                    if (checkVisibility(site, jahiaUser, locale, request, item.getVisibility())) {
                        GWTColumn col = createGWTColumn(item, site, locale, uiLocale);
                        gwtConfig.addTableColumn(col);
                    }
                }
                for (Column item : config.getTreeColumns()) {
                    if (checkVisibility(site, jahiaUser, locale, request, item.getVisibility())) {
                        GWTColumn col = createGWTColumn(item, site, locale, uiLocale);
                        gwtConfig.addTreeColumn(col);
                    }
                }

                // add tabs
                for (EngineTab item : config.getTabs()) {
                    if (checkVisibility(site, jahiaUser, locale, request, item.getVisibility())) {
                        gwtConfig.addTab(item.getKey());
                    }

                }

                // add accordion panels
                for (Repository item : config.getRepositories()) {
                    if (checkVisibility(site, jahiaUser, locale, request, item.getVisibility())) {
                        GWTRepository repository  = new GWTRepository();
                        repository.setKey(item.getKey());
                        if (item.getTitleKey() != null) {
                            repository.setTitle(getResources(item.getTitleKey(), uiLocale != null ? uiLocale : locale, site));
                        } else if (item.getTitle() != null) {
                            repository.setTitle(item.getTitle());
                        } else {
                            repository.setTitle(item.getKey());
                        }
                        repository.setPaths(item.getPaths());
                        gwtConfig.addRepository(repository);
                    }
                }

                gwtConfig.setCreateEngines(createGWTEngineList(site, jahiaUser, locale, request, config.getCreateEngines()));
                gwtConfig.setEditEngines(createGWTEngineList(site, jahiaUser, locale, request, config.getEditEngines()));

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

    private GWTColumn createGWTColumn(Column item, JCRSiteNode site, Locale locale, Locale uiLocale) {
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
        if (item.getSize().equals("*")) {
            col.setSize(-1);
        } else {
            col.setSize(Integer.parseInt(item.getSize()));
        }
        return col;
    }


    /**
     * Create gwt items group
     *
     * @param toolbarName
     * @param index
     * @param menu
     * @return
     */
    private GWTJahiaToolbarMenu createGWTItemsGroup(JCRSiteNode site, JahiaUser jahiaUser, Locale locale, Locale uiLocale, HttpServletRequest request, String toolbarName, int index, Menu menu) {
        // don't add the items group if  has no items group
        List<Item> list = menu.getRealItems(site, jahiaUser, locale);
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
        GWTJahiaToolbarMenu gwtToolbarMenu = new GWTJahiaToolbarMenu();
        gwtToolbarMenu.setId(toolbarName + "_" + index);

        gwtToolbarMenu.setIcon(menu.getIcon());
        if (menu.getTitleKey() != null) {
            gwtToolbarMenu.setItemsGroupTitle(getResources(menu.getTitleKey(), uiLocale != null ? uiLocale : locale, site));
        } else {
            gwtToolbarMenu.setItemsGroupTitle(menu.getTitle());
        }
        gwtToolbarMenu.setGwtToolbarItems(gwtToolbarItemsList);
        return gwtToolbarMenu;
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
        if (item instanceof Menu) {
            for (Item subItem : ((Menu) item).getRealItems(site, jahiaUser, locale)) {
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
        gwtToolbarItem.setId(item.getId());
        if (item.getTitleKey() != null) {
            gwtToolbarItem.setTitle(getResources(item.getTitleKey(), uiLocale != null ? uiLocale : locale, site));
        } else {
            gwtToolbarItem.setTitle(item.getTitle());
        }
        gwtToolbarItem.setDisplayTitle(item.isDisplayTitle());
        if (item.getDescriptionKey() != null) {
            gwtToolbarItem.setDescription(getResources(item.getDescriptionKey(), uiLocale != null ? uiLocale : locale, site));
        } else {
            gwtToolbarItem.setDescription(gwtToolbarItem.getTitle());
        }
        gwtToolbarItem.setIcon(item.getIcon());
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


//        if (item.getWorkflowAction() != null) {
//            try {
//                List<WorkflowDefinition> def = WorkflowService.getInstance().getWorkflowsForAction(item.getWorkflowAction());
//                List<String> processes = new ArrayList<String>();
//                for (WorkflowDefinition workflowDefinition : def) {
//                    processes.add(workflowDefinition.getKey());
//                }
//                gwtToolbarItem.setProcesses(processes);
//                // todo : use the role assigned to the action for bypassing workflow ?
//                final WorkflowActionItem workflowActionItem = new WorkflowActionItem(processes, jahiaUser.isAdminMember(0), item.getActionItem());
//                gwtToolbarItem.setMinIconStyle("gwt-toolbar-icon-workflow-start");
//                gwtToolbarItem.setActionItem(workflowActionItem);
//            } catch (RepositoryException e) {
//                logger.error("Cannot get workflows", e);
//            }
//        } else {
            ActionItem actionItem = item.getActionItem();
            if (actionItem instanceof LanguageSwitcherActionItem) {
                ((LanguageSwitcherActionItem) actionItem).setSelectedLang(languages.getCurrentLang(locale));
                ((LanguageSwitcherActionItem) actionItem).setLanguages(languages.getLanguages(site, jahiaUser, locale));
            }

            gwtToolbarItem.setActionItem(actionItem);
//        }

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
                gwtSidePanel.setTooltip(getResources("label.selectorTab." + sidePanelTab.getKey(), uiLocale, site));
                gwtSidePanel.setTreeContextMenu(createGWTToolbar(site, jahiaUser, locale, uiLocale, request, sidePanelTab.getTreeContextMenu()));
                gwtSidePanel.setTableContextMenu(createGWTToolbar(site, jahiaUser, locale, uiLocale, request, sidePanelTab.getTableContextMenu()));
                gwtSidePanel.setParams(sidePanelTab.getParams());
                gwtSidePanel.setFilters(sidePanelTab.getFilters());
                gwtSidePanel.setFolderTypes(sidePanelTab.getFolderTypes());
                gwtSidePanel.setMimeTypes(sidePanelTab.getMimeTypes());
                gwtSidePanel.setPaths(sidePanelTab.getPaths());
                gwtSidePanel.setNodeTypes(sidePanelTab.getNodeTypes());
                gwtSidePanel.setIcon(sidePanelTab.getIcon());
                // add table columns
                for (Column item : sidePanelTab.getTableColumns()) {
                    if (checkVisibility(site, jahiaUser, locale, request, item.getVisibility())) {
                        GWTColumn col = createGWTColumn(item, site, locale, uiLocale);
                        gwtSidePanel.addTableColumn(col);
                    }
                }
                for (Column item : sidePanelTab.getTreeColumns()) {
                    if (checkVisibility(site, jahiaUser, locale, request, item.getVisibility())) {
                        GWTColumn col = createGWTColumn(item, site, locale, uiLocale);
                        gwtSidePanel.addTreeColumn(col);
                    }
                }

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
