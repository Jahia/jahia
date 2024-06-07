/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
package org.jahia.ajax.gwt.helper;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.security.JahiaPrivilegeRegistry;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.*;
import org.jahia.ajax.gwt.client.data.toolbar.monitor.GWTJahiaStateInfo;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.util.Constants;
import org.jahia.ajax.gwt.client.util.security.PermissionsResolver;
import org.jahia.ajax.gwt.client.widget.toolbar.action.ActionItem;
import org.jahia.ajax.gwt.client.widget.toolbar.action.LanguageAware;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.uicomponents.bean.Visibility;
import org.jahia.services.uicomponents.bean.contentmanager.Column;
import org.jahia.services.uicomponents.bean.contentmanager.ManagerConfiguration;
import org.jahia.services.uicomponents.bean.contentmanager.Repository;
import org.jahia.services.uicomponents.bean.editmode.EditConfiguration;
import org.jahia.services.uicomponents.bean.editmode.EngineConfiguration;
import org.jahia.services.uicomponents.bean.editmode.EngineTab;
import org.jahia.services.uicomponents.bean.editmode.SidePanelTab;
import org.jahia.services.uicomponents.bean.toolbar.Item;
import org.jahia.services.uicomponents.bean.toolbar.Menu;
import org.jahia.services.uicomponents.bean.toolbar.Property;
import org.jahia.services.uicomponents.bean.toolbar.Toolbar;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.Url;
import org.jahia.utils.WebUtils;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static org.jahia.utils.i18n.ResourceBundles.getResources;

/**
 * Utility class for populating GWT UI configuration data.
 *
 * User: ktlili
 * Date: Apr 13, 2010
 * Time: 5:25:09 PM
 */
public class UIConfigHelper {

    private static final Logger logger = LoggerFactory.getLogger(UIConfigHelper.class);
    private LanguageHelper languages;
    private NavigationHelper navigation;
    private ChannelHelper channelHelper;

    public void setNavigation(NavigationHelper navigation) {
        this.navigation = navigation;
    }

    public void setChannelHelper(ChannelHelper channelHelper) {
        this.channelHelper = channelHelper;
    }

    public void setLanguages(LanguageHelper languages) {
        this.languages = languages;
    }

    /**
     * Get gwt toolbar for the current user
     *
     * @return
     */
    public GWTJahiaToolbar getGWTToolbarSet(JCRNodeWrapper contextNode, JCRSiteNode site, JahiaUser jahiaUser, Locale locale, Locale uiLocale, HttpServletRequest request, String toolbarGroup) throws GWTJahiaServiceException {
        try {
            // there is no pref or toolbar are hided
            // get all tool bars
            Toolbar toolbar = (Toolbar) SpringContextSingleton.getBean(toolbarGroup);
            Visibility visibility = toolbar.getVisibility();
            if ((visibility != null && visibility.getRealValue(contextNode, jahiaUser, locale, request)) || visibility == null) {
                return createGWTToolbar(contextNode, site, jahiaUser, locale, uiLocale, request, toolbar);
            } else {
                logger.info("Toolbar are not visible.");
                return null;
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.during.loading.toolbars", uiLocale, e.getMessage()));
        }
    }

    /**
     * create gwt toolabr set
     *
     *
     * @param contextNode
     * @param toolbarSet
     * @return
     */
    public List<GWTJahiaToolbar> createGWTToolbarSet(JCRNodeWrapper contextNode, JCRSiteNode site, JahiaUser jahiaUser, Locale locale, Locale uiLocale, HttpServletRequest request, List<Toolbar> toolbarSet) {
        if (toolbarSet == null || toolbarSet.isEmpty()) {
            logger.debug("toolbar set list is empty");
            return null;
        }

        // create  a gwtJahiaToolbarSet
        List<GWTJahiaToolbar> gwtJahiaToolbarSet = new ArrayList<GWTJahiaToolbar>();
        for (Toolbar toolbar : toolbarSet) {
            // add only tool bar that the user can view
            Visibility visibility = toolbar.getVisibility();
            if ((visibility != null && visibility.getRealValue(contextNode, jahiaUser, locale, request)) || visibility == null) {
                GWTJahiaToolbar gwtToolbar = createGWTToolbar(contextNode, site, jahiaUser, locale, uiLocale, request, toolbar);
                // add toolbar only if not empty
                if (gwtToolbar != null && gwtToolbar.getGwtToolbarItems() != null && !gwtToolbar.getGwtToolbarItems().isEmpty()) {
                    gwtJahiaToolbarSet.add(gwtToolbar);
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
            throw new GWTJahiaServiceException(Messages.getInternal("label.gwt.error.when.trying.to.load.jahia.state", uiLocale));
        }
    }

    /**
     * Create gwt toolbar
     *
     *
     * @param contextNode
     * @param toolbar
     * @return
     */
    public GWTJahiaToolbar createGWTToolbar(JCRNodeWrapper contextNode, JCRSiteNode site, JahiaUser jahiaUser, Locale locale, Locale uiLocale, HttpServletRequest request, Toolbar toolbar) {
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
        gwtToolbar.setName(StringUtils.substringBeforeLast(toolbar.getName(), "#"));
        gwtToolbar.setTitle(getResources(toolbar.getTitleKey(), uiLocale != null ? uiLocale : locale, site, jahiaUser));
        gwtToolbar.setDisplayTitle(toolbar.isDisplayTitle());

        // load items-group
        List<GWTJahiaToolbarItem> gwtToolbarItemsGroupList = new ArrayList<GWTJahiaToolbarItem>();
        int index = 0;
        for (Item item : toolbar.getItems()) {
            // add only itemsgroup that the user can view
            Visibility visibility = item.getVisibility();
            if ((visibility != null && visibility.getRealValue(contextNode, jahiaUser, locale, request)) || visibility == null) {
                if (item instanceof Menu) {
                    GWTJahiaToolbarMenu gwtMenu = createGWTItemsGroup(contextNode, site, jahiaUser, locale, uiLocale, request, gwtToolbar.getName(), index, (Menu) item);
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
     *
     * @param contextNode
     * @param site
     * @param jahiaUser
     * @param locale
     * @param uiLocale
     * @param request
     * @param name
     * @return
     * @throws GWTJahiaServiceException
     */
    public GWTManagerConfiguration getGWTManagerConfiguration(JCRNodeWrapper contextNode, JCRSiteNode site, JahiaUser jahiaUser, Locale locale, Locale uiLocale, HttpServletRequest request, String name) throws GWTJahiaServiceException {
        try {
            ManagerConfiguration config = (ManagerConfiguration) getThemedConfiguration(name, request);
            if (config != null) {
                logger.debug("Config. " + name + " found.");
                GWTManagerConfiguration gwtConfig = new GWTManagerConfiguration();
                gwtConfig.setName(name);

                // get a title from bundle if available
                String title = null;
                if (config.getTitleKey() != null) {
                    title =  getResources(config.getTitleKey(), uiLocale, site, jahiaUser);
                } else {
                    //read from JahiaInternalResource
                    title = getResources("label." + name, uiLocale, site, jahiaUser);
                }
                if (title != null) {
                    gwtConfig.setTitle(title);
                }

                //  set all properties
                gwtConfig.setNodeTypes(config.getNodeTypes());
                gwtConfig.setFolderTypes(config.getFolderTypes());
                gwtConfig.setHideLeftPanel(config.isHideLeftPanel());
                gwtConfig.setFilters(config.getFilters());
                gwtConfig.setMimeTypes(config.getMimeTypes());
                gwtConfig.setHiddenRegex(config.getHiddenRegex());
                gwtConfig.setHiddenTypes(config.getHiddenTypes());
                gwtConfig.setDefaultView(config.getDefaultView());
                gwtConfig.setEnableDragAndDrop(config.isEnableDragAndDrop());
                gwtConfig.setExcludedNodeTypes(config.getExcludedNodeTypes());
                gwtConfig.setAllowedNodeTypesForDragAndDrop(config.getAllowedNodeTypesForDragAndDrop());
                gwtConfig.setForbiddenNodeTypesForDragAndDrop(config.getForbiddenNodeTypesForDragAndDrop());
                gwtConfig.setEnableFileDoubleClick(config.isEnableFileDoubleClick());
                gwtConfig.setAllowsMultipleSelection(config.isAllowsMultipleSelection());
                gwtConfig.setExpandRoot(config.isExpandRoot());
                gwtConfig.setAllowRootNodeEditing(config.isAllowRootNodeEditing());
                gwtConfig.setDisplaySearch(config.isDisplaySearch());
                gwtConfig.setDisplaySearchInPage(config.isDisplaySearchInPage());
                gwtConfig.setDisplaySearchInTag(config.isDisplaySearchInTag());
                gwtConfig.setDisplaySearchInFile(config.isDisplaySearchInFile());
                gwtConfig.setDisplaySearchInContent(config.isDisplaySearchInContent());
                gwtConfig.setSearchInFile(config.isSearchInFile());
                gwtConfig.setSearchInContent(config.isSearchInContent());
                gwtConfig.setSearchInCurrentSiteOnly(config.isSearchInCurrentSiteOnly());
                gwtConfig.setSearchBasePath(config.getSearchBasePath());
                gwtConfig.setShowOnlyNodesWithTemplates(config.isShowOnlyNodesWithTemplates());
                gwtConfig.setDisplaySearchInDateMeta(config.isDisplaySearchInDateMeta());
                gwtConfig.setEditableGrid(config.isEditableGrid());
                gwtConfig.setComponentsPaths(config.getComponentsPaths());
                // set toolbar
                gwtConfig.setToolbars(createGWTToolbarSet(contextNode, site, jahiaUser, locale, uiLocale, request, config.getToolbars()));
                gwtConfig.setContextMenu(createGWTToolbar(contextNode, site, jahiaUser, locale, uiLocale, request, config.getContextMenu()));

                // add table columns
                for (Column item : config.getTableColumns()) {
                    if (checkVisibility(contextNode, jahiaUser, locale, request, item.getVisibility())) {
                        GWTColumn col = createGWTColumn(item, site, locale, uiLocale);
                        gwtConfig.addTableColumn(col);
                    }
                }
                for (Column item : config.getTreeColumns()) {
                    if (checkVisibility(contextNode, jahiaUser, locale, request, item.getVisibility())) {
                        GWTColumn col = createGWTColumn(item, site, locale, uiLocale);
                        gwtConfig.addTreeColumn(col);
                    }
                }

                // add accordion panels
                for (Repository item : config.getRepositories()) {
                    if (checkVisibility(contextNode, jahiaUser, locale, request, item.getVisibility())) {
                        GWTRepository repository  = new GWTRepository();
                        repository.setKey(item.getKey());
                        if (item.getTitleKey() != null) {
                            repository.setTitle(getResources(item.getTitleKey(), uiLocale != null ? uiLocale : locale, site,
                                    jahiaUser));
                        } else if (item.getTitle() != null) {
                            repository.setTitle(item.getTitle());
                        } else {
                            repository.setTitle(item.getKey());
                        }
                        repository.setPaths(new ArrayList<String>());
                        for (String path : item.getPaths()) {
                            if (path.equals("$rootPath")) {
                                repository.getPaths().add(contextNode.getPath());
                            } else {
                                repository.getPaths().add(path);
                            }
                        }
                        gwtConfig.addRepository(repository);
                    }
                }

                if (config.getEngineConfigurations() != null && !config.getEngineConfigurations().isEmpty()) {
                    gwtConfig.setEngineConfigurations(createGWTEngineConfigurations(contextNode, site, jahiaUser, locale, uiLocale, request, config.getEngineConfigurations(), config.getEngineTabs()));
                }

                // todo : use eanUtilsBean.getInstance().cloneBean when it works. Actually it does not copy properties of the bean.
                if (config.getEngineTabs() != null && !config.getEngineTabs().isEmpty()) {
                    List<GWTEngineTab> managerTabs = createGWTEngineList(contextNode, site, jahiaUser, locale, uiLocale, request, config.getEngineTabs());
                    gwtConfig.setManagerEngineTabs(managerTabs);
                }

                gwtConfig.setSuppressTreePublicationInfo(config.isSuppressTreePublicationInfo());
                gwtConfig.setSuppressTablePublicationInfo(config.isSuppressTablePublicationInfo());

                gwtConfig.setSiteNode(navigation.getGWTJahiaNode(site, GWTJahiaNode.DEFAULT_SITE_FIELDS, uiLocale));
                gwtConfig.setPermissions(JahiaPrivilegeRegistry.getRegisteredPrivilegeNames());

                gwtConfig.setUseLargeThumbnails(config.isUseLargeThumbnails());

                if (config.getCustomPickerConfiguration() != null) {
                    gwtConfig.setCustomPickerConfiguration(config.getCustomPickerConfiguration());
                }

                return gwtConfig;
            } else {
                logger.error("Config. " + name + " not found.");
                throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.config.not.found", uiLocale, name));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.config.not.found", uiLocale, name, e.getLocalizedMessage()));
        }
    }

    private GWTColumn createGWTColumn(Column item, JCRSiteNode site, Locale locale, Locale uiLocale) {
        GWTColumn col = new GWTColumn();
        col.setKey(item.getKey());
        if (item.getTitleKey() != null) {
            col.setTitle(item.getTitleKey().equals("empty") ? "" : getResources(item.getTitleKey(), uiLocale != null ? uiLocale : locale, site, null));
        } else if (item.getDeclaringNodeType() != null) {
            try {
                ExtendedPropertyDefinition epd = NodeTypeRegistry.getInstance().getNodeType(item.getDeclaringNodeType()).getPropertyDefinition(item.getKey());
                col.setTitle(epd.getLabel(uiLocale != null ? uiLocale : locale));
            } catch (Exception e) {
                logger.error("Cannot get node type name", e);
                col.setTitle("");
            }
        } else if (item.getTitle() != null) {
            col.setTitle(item.getTitle());
        } else {
            col.setTitle("");
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
     *
     * @param contextNode
     * @param toolbarName
     * @param index
     * @param menu
     * @return
     */
    private GWTJahiaToolbarMenu createGWTItemsGroup(JCRNodeWrapper contextNode, JCRSiteNode site, JahiaUser jahiaUser, Locale locale, Locale uiLocale, HttpServletRequest request, String toolbarName, int index, Menu menu) {
        // don't add the items group if  has no items group
        List<Item> list = menu.getItems();
        if (list == null || list.isEmpty()) {
            logger.debug("toolbar[" + toolbarName + "] itemlist is empty");
            return null;
        }


        List<GWTJahiaToolbarItem> gwtToolbarItemsList = new ArrayList<GWTJahiaToolbarItem>();
        // create items from definition
        for (Item item : list) {
            addToolbarItem(contextNode, site, jahiaUser, locale, uiLocale, request, gwtToolbarItemsList, item);
        }

        // don't add the items group if  has no items group
        if (gwtToolbarItemsList == null || gwtToolbarItemsList.isEmpty()) {
            logger.debug("toolbar[" + toolbarName + "] itemlist is empty");
            return null;
        }

        // creat items-group
        GWTJahiaToolbarMenu gwtToolbarMenu = new GWTJahiaToolbarMenu();
        gwtToolbarMenu.setId(StringUtils.substringBeforeLast(menu.getId(), "#"));

        gwtToolbarMenu.setIcon(menu.getIcon());
        if (menu.getTitleKey() != null) {
            gwtToolbarMenu.setItemsGroupTitle(getResources(menu.getTitleKey(), uiLocale != null ? uiLocale : locale, site, jahiaUser));
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
     * @param contextNode
     * @param gwtToolbarItemsList
     * @param item
     */
    private void addToolbarItem(JCRNodeWrapper contextNode, JCRSiteNode site, JahiaUser jahiaUser, Locale locale, Locale uiLocale, HttpServletRequest request, List<GWTJahiaToolbarItem> gwtToolbarItemsList, Item item) {
        if (item instanceof Menu) {
            for (Item subItem : ((Menu) item).getItems()) {
                addToolbarItem(contextNode, site, jahiaUser, locale, uiLocale, request, gwtToolbarItemsList, subItem);
            }
        } else {
            // add only item that the user can view
            logger.debug("Item: " + item.getId());
            Visibility visibility = item.getVisibility();

            // add only visible items
            if ((visibility != null && visibility.getRealValue(contextNode, jahiaUser, locale, request)) || visibility == null) {
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
        gwtToolbarItem.setId(StringUtils.substringBeforeLast(item.getId(), "#"));
        if (item.getTitleKey() != null) {
            gwtToolbarItem.setTitle(getResources(item.getTitleKey(), uiLocale != null ? uiLocale : locale, site,
                    jahiaUser));
        } else {
            gwtToolbarItem.setTitle(item.getTitle());
        }
        gwtToolbarItem.setDisplayTitle(item.isDisplayTitle());
        if (item.getDescriptionKey() != null) {
            gwtToolbarItem.setDescription(getResources(item.getDescriptionKey(), uiLocale != null ? uiLocale : locale, site,
                    jahiaUser));
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
            gwtProperty.setValue(currentProperty.getRealValue(site, jahiaUser, locale, request));
            pMap.put(gwtProperty.getName(), gwtProperty);
        }
        gwtToolbarItem.setLayout(getLayoutAsInt(item.getLayout()));
        gwtToolbarItem.setRequiredPermissions(item.getRequiredPermissions());
        gwtToolbarItem.setRequiredSitePermissions(item.getRequiredSitePermissions());
        gwtToolbarItem.setRequiredPermissionsResolver(toPermissionsResolver(item.getRequiredPermissionsStrategy()));
        gwtToolbarItem.setRequiredModule(item.getRequiredModule());
        gwtToolbarItem.setHideWhenDisabled(item.isHideWhenDisabled());
        gwtToolbarItem.setProperties(pMap);

        ActionItem actionItem = item.getActionItem();
        if (actionItem instanceof LanguageAware) {
            ((LanguageAware) actionItem).setSelectedLang(languages.getCurrentLang(locale));
        }

        gwtToolbarItem.setActionItem(actionItem);

        return gwtToolbarItem;
    }

    /**
     * Get edit configuration
     *
     * @return
     * @throws GWTJahiaServiceException
     */
    public GWTEditConfiguration getGWTEditConfiguration(String name, String contextPath, JahiaUser jahiaUser, Locale locale, Locale uiLocale, HttpServletRequest request, JCRSessionWrapper session) throws GWTJahiaServiceException {
        try {

            EditConfiguration config = (EditConfiguration) getThemedConfiguration(name, request);

            if (config != null) {
                GWTEditConfiguration gwtConfig = new GWTEditConfiguration();
                gwtConfig.setName(name);

                String defaultLocation = config.getDefaultLocation();
                if (defaultLocation.contains("$defaultSiteHome")) {
                    JahiaSitesService siteService = JahiaSitesService.getInstance();

                    JahiaSite resolvedSite = !Url.isLocalhost(request.getServerName()) ? siteService.getSiteByServerName(request.getServerName(), session) : null;
                    if (resolvedSite == null) {
                        resolvedSite = JahiaSitesService.getInstance().getDefaultSite(session);
                        if (resolvedSite != null && !((JCRSiteNode)resolvedSite).hasPermission(config.getRequiredPermission())) {
                            resolvedSite = null;
                        }
                        if (resolvedSite == null) {
                            List<JCRSiteNode> sites = JahiaSitesService.getInstance().getSitesNodeList(session);
                            for (JCRSiteNode site : sites) {
                                if (!"systemsite".equals(site.getName()) && (site.hasPermission(config.getRequiredPermission()))) {
                                    resolvedSite = site;
                                    break;
                                }
                            }
                        }
                    }
                    if (resolvedSite != null) {
                        JCRSiteNode siteNode = (JCRSiteNode) session.getNode(((JCRSiteNode)resolvedSite).getPath());
                        if (siteNode.getHome() != null) {
                            defaultLocation = defaultLocation.replace("$defaultSiteHome", siteNode.getHome().getPath());
                        } else {
                            defaultLocation = null;
                        }
                    } else {
                        defaultLocation = null;
                    }
                } else if (defaultLocation.contains("$user")) {
                    defaultLocation = defaultLocation.replace("$user", jahiaUser.getLocalPath());
                }
                gwtConfig.setDefaultLocation(defaultLocation);
                JCRNodeWrapper contextNode = null;
                JCRSiteNode site = null;
                if (contextPath == null) {
                    int nodeNameIndex = StringUtils.indexOf(defaultLocation, ".", StringUtils.lastIndexOf(defaultLocation, "/"));
                    contextPath = StringUtils.substring(defaultLocation, 0, nodeNameIndex);
                    if (defaultLocation != null && session.nodeExists(contextPath)) {
                        contextNode = session.getNode(contextPath);
                        site = contextNode.getResolveSite();
                    }
                } else {
                    if (session.nodeExists(contextPath)) {
                        contextNode = session.getNode(contextPath);
                        site = contextNode.getResolveSite();
                    }
                }

                if (config.getForcedSite() != null) {
                    site = (JCRSiteNode) session.getNode(config.getForcedSite());
                }

                if (site == null) {
                    contextNode = session.getNode("/sites/systemsite");
                    site = contextNode.getResolveSite();
                }

                // check locale
                final List<Locale> languagesAsLocales = site.getLanguagesAsLocales();
                if (languagesAsLocales != null && !languagesAsLocales.contains(locale)) {
                    final String defaultLanguage = site.getDefaultLanguage();
                    if (StringUtils.isNotEmpty(defaultLanguage)) {
                        locale = LanguageCodeConverters.languageCodeToLocale(defaultLanguage);
                    }
                }


                gwtConfig.setTopToolbars(new ArrayList<GWTJahiaToolbar>());
                for (Toolbar toolbar : config.getTopToolbars()) {
                    gwtConfig.getTopToolbars().add(createGWTToolbar(contextNode, site, jahiaUser, locale, uiLocale, request, toolbar));
                }
                gwtConfig.setSidePanelToolbar(createGWTToolbar(contextNode, site, jahiaUser, locale, uiLocale, request, config.getSidePanelToolbar()));
                gwtConfig.setMainModuleToolbar(createGWTToolbar(contextNode, site, jahiaUser, locale, uiLocale, request, config.getMainModuleToolbar()));
                gwtConfig.setContextMenu(createGWTToolbar(contextNode, site, jahiaUser, locale, uiLocale, request, config.getContextMenu()));
                gwtConfig.setTabs(createGWTSidePanelTabList(contextNode, site, jahiaUser, locale, uiLocale, request, config.getTabs()));
                gwtConfig.setEngineConfigurations(createGWTEngineConfigurations(contextNode, site, jahiaUser, locale, uiLocale, request, config.getEngineConfigurations()));
                gwtConfig.setSitesLocation(config.getSitesLocation());
                gwtConfig.setDragAndDropBehavior(config.getDragAndDropBehavior());
                gwtConfig.setRefreshOnExternalModification(config.getRefreshOnExternalModification());
                gwtConfig.setCreateChildrenDirectButtonsLimit(config.getCreateChildrenDirectButtonsLimit());
                gwtConfig.setDefaultUrlMapping(config.getDefaultUrlMapping());
                gwtConfig.setComponentsPaths(config.getComponentsPaths());
                gwtConfig.setEditableTypes(config.getEditableTypes());
                gwtConfig.setNonEditableTypes(config.getNonEditableTypes());
                gwtConfig.setSkipMainModuleTypesDomParsing(config.getSkipMainModuleTypesDomParsing());
                gwtConfig.setVisibleTypes(config.getVisibleTypes());
                gwtConfig.setNonVisibleTypes(config.getNonVisibleTypes());
                gwtConfig.setExcludedNodeTypes(config.getExcludedNodeTypes());
                List<String> configsList = new ArrayList<String>();
                // configsList will define the list of modes that share the same configuration to avoid reloading the main resource
                // when switching from edit to preview or live or any mode that has the same default location.
                // An exception has been added for system site that is used for dashboard or administration.
                for (EditConfiguration configuration : SpringContextSingleton.getBeansOfType(EditConfiguration.class).values()) {
                    if (StringUtils.equals(configuration.getSitesLocation(), config.getSitesLocation()) && !StringUtils.equals(config.getSitesLocation(), "/sites/systemsite")) {
                        configsList.add(configuration.getName());
                    }
                }
                gwtConfig.setSamePathConfigsList(configsList);
                gwtConfig.setSiteNode(navigation.getGWTJahiaNode(site, GWTJahiaNode.DEFAULT_SITE_FIELDS, uiLocale));

                if (config.isLoadSitesList()) {
                    List<GWTJahiaNode> sites = navigation.retrieveRoot(Arrays.asList(config.getSitesLocation()), Arrays.asList("jnt:virtualsite"), null, null, GWTJahiaNode.DEFAULT_SITEMAP_FIELDS, null, null, site, session, uiLocale, false, false, null, null);
                    String permission = ((EditConfiguration)SpringContextSingleton.getBean(name)).getRequiredPermission();
                    Map<String, GWTJahiaNode> sitesMap = new HashMap<String, GWTJahiaNode>();
                    for (GWTJahiaNode aSite : sites) {
                        if (session.getNodeByUUID(aSite.getUUID()).hasPermission(permission)) {
                            sitesMap.put(aSite.getSiteUUID(), aSite);
                        }
                    }
                    GWTJahiaNode systemSite = navigation.getGWTJahiaNode(session.getNode("/sites/systemsite"), GWTJahiaNode.DEFAULT_SITEMAP_FIELDS);
                    if (!sitesMap.containsKey(systemSite.getUUID())) {
                        sitesMap.put(systemSite.getUUID(), systemSite);
                    }
                    gwtConfig.setSitesMap(sitesMap);
                }

                gwtConfig.setPermissions(JahiaPrivilegeRegistry.getRegisteredPrivilegeNames());

                gwtConfig.setChannels(channelHelper.getChannels());

                gwtConfig.setUseFullPublicationInfoInMainAreaModules(config.isUseFullPublicationInfoInMainAreaModules());
                gwtConfig.setSupportChannelsDisplay(config.isSupportChannelsDisplay());
                gwtConfig.setNeedFrameParsing(config.isNeedFrameParsing());
                gwtConfig.setRefreshEnabled(config.isRefreshEnabled());
                gwtConfig.setEventDispatchingEnabled(config.isEventDispatchingEnabled());
                return gwtConfig;
            } else {
                throw new GWTJahiaServiceException(Messages.getInternal("label.gwt.error.bean.editconfig.not.found.in.spring.config.file", uiLocale));
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.config.not.found", uiLocale, name, e.getLocalizedMessage()));
        }
    }

    /**
     * Create GWTSidePanelTab list
     *
     *
     * @param contextNode
     * @param site
     * @param jahiaUser
     * @param locale
     * @param uiLocale
     * @param request
     * @param tabs
     * @return
     */
    private List<GWTSidePanelTab> createGWTSidePanelTabList(JCRNodeWrapper contextNode, JCRSiteNode site, JahiaUser jahiaUser, Locale locale, Locale uiLocale, HttpServletRequest request, List<SidePanelTab> tabs) {
        // create side panel tabs
        List<GWTSidePanelTab> gwtSidePanelTabList = new ArrayList<GWTSidePanelTab>();
        for (SidePanelTab sidePanelTab : tabs) {
            if (checkVisibility(contextNode, jahiaUser, locale, request, sidePanelTab.getVisibility())) {
                final GWTSidePanelTab gwtSidePanel = new GWTSidePanelTab(sidePanelTab.getKey());
                gwtSidePanel.setTooltip(getResources("label.selectorTab." + sidePanelTab.getKey(), uiLocale, site,
                        jahiaUser));
                gwtSidePanel.setTreeContextMenu(createGWTToolbar(contextNode, site, jahiaUser, locale, uiLocale, request, sidePanelTab.getTreeContextMenu()));
                gwtSidePanel.setTableContextMenu(createGWTToolbar(contextNode, site, jahiaUser, locale, uiLocale, request, sidePanelTab.getTableContextMenu()));
                gwtSidePanel.setIcon(sidePanelTab.getIcon());
                gwtSidePanel.setRequiredPermission(sidePanelTab.getRequiredPermission());

                gwtSidePanel.setTabItem(sidePanelTab.getTabItem());

                // add table columns
                for (Column item : sidePanelTab.getTableColumns()) {
                    if (checkVisibility(contextNode, jahiaUser, locale, request, item.getVisibility())) {
                        GWTColumn col = createGWTColumn(item, site, locale, uiLocale);
                        gwtSidePanel.addTableColumn(col);
                    }
                }
                for (Column item : sidePanelTab.getTreeColumns()) {
                    if (checkVisibility(contextNode, jahiaUser, locale, request, item.getVisibility())) {
                        GWTColumn col = createGWTColumn(item, site, locale, uiLocale);
                        gwtSidePanel.addTreeColumn(col);
                    }
                }

                gwtSidePanelTabList.add(gwtSidePanel);
            }
        }
        return gwtSidePanelTabList;
    }

    private Map<String, GWTEngineConfiguration> createGWTEngineConfigurations(JCRNodeWrapper contextNode, JCRSiteNode site, JahiaUser jahiaUser, Locale locale, Locale uiLocale, HttpServletRequest request, Map<String, EngineConfiguration> engineConfigurations) {
        return createGWTEngineConfigurations(contextNode, site, jahiaUser, locale, uiLocale, request, engineConfigurations, new ArrayList<EngineTab>());
    }

    private Map<String, GWTEngineConfiguration> createGWTEngineConfigurations(JCRNodeWrapper contextNode, JCRSiteNode site, JahiaUser jahiaUser, Locale locale, Locale uiLocale, HttpServletRequest request, Map<String, EngineConfiguration> engineConfigurations, List<EngineTab> defaultEngineTabs) {
        Map<String, GWTEngineConfiguration> gwtEngineConfigurations = new HashMap<String, GWTEngineConfiguration>();
        if (engineConfigurations != null) {
            for (Map.Entry<String, EngineConfiguration> type : engineConfigurations.entrySet()) {
                GWTEngineConfiguration gwtEngineConfiguration = new GWTEngineConfiguration();
                EngineConfiguration engineConfiguration = type.getValue();
                List<EngineTab> engineTabs = engineConfiguration.getEngineTabs();
                if (engineTabs == null) {
                    engineTabs = defaultEngineTabs;
                }
                gwtEngineConfiguration.setEngineTabs(createGWTEngineList(contextNode, site, jahiaUser, locale, uiLocale, request, engineTabs));
                gwtEngineConfiguration.setCreationButtons(engineConfiguration.getCreationButtons());
                gwtEngineConfiguration.setEditionButtons(engineConfiguration.getEditionButtons());
                gwtEngineConfiguration.setCommonButtons(engineConfiguration.getCommonButtons());
                gwtEngineConfigurations.put(type.getKey(), gwtEngineConfiguration);
            }
        }
        return gwtEngineConfigurations;
    }

    /**
     * Create gwt engine list
     *
     *
     * @param contextNode
     * @param site
     * @param jahiaUser
     * @param locale
     * @param request
     * @param engines
     * @return
     */
    private List<GWTEngineTab> createGWTEngineList(JCRNodeWrapper contextNode, JCRSiteNode site, JahiaUser jahiaUser, Locale locale, Locale uiLocale, HttpServletRequest request, List<EngineTab> engines) {
        final List<GWTEngineTab> engineTabs = new ArrayList<GWTEngineTab>();
        for (EngineTab engineTab : engines) {
            if (checkVisibility(contextNode, jahiaUser, locale, request, engineTab.getVisibility())) {
                GWTEngineTab gwtTab = createGWTEngineTab(engineTab, site, locale, uiLocale);
                engineTabs.add(gwtTab);
            }
        }
        return engineTabs;
    }

    private GWTEngineTab createGWTEngineTab(EngineTab engineTab, JCRSiteNode site, Locale locale, Locale uiLocale) {
        GWTEngineTab gwtTab = new GWTEngineTab();
        gwtTab.setId(engineTab.getId());

        if (engineTab.getTitleKey() != null) {
            gwtTab.setTitle(getResources(engineTab.getTitleKey(), uiLocale != null ? uiLocale : locale, site, null));
        } else {
            gwtTab.setTitle(engineTab.getTitle());
        }
        gwtTab.setTabItem(engineTab.getTabItem());
        gwtTab.setRequiredPermission(engineTab.getRequiredPermission());
        gwtTab.setShowInEngine(engineTab.showInEngine());
        return gwtTab;
    }

    private Object getThemedConfiguration(String name, HttpServletRequest request) {
        Object config = SpringContextSingleton.getBean(name);
        String theme = WebUtils.getUITheme(request);
        if (theme != null) {
            try {
                config = SpringContextSingleton.getBean(name + "-" + theme);
            } catch (NoSuchBeanDefinitionException e) {
                // Ignore
            }
        }
        return config;
    }

    private boolean checkVisibility(JCRNodeWrapper contextNode, JahiaUser jahiaUser, Locale locale, HttpServletRequest request, Visibility visibility) {
        return visibility == null || visibility.getRealValue(contextNode, jahiaUser, locale, request);
    }

    private static PermissionsResolver toPermissionsResolver(Item.PermissionsStrategy strategy) {
        switch (strategy) {
            case MATCH_ANY:
                return PermissionsResolver.MATCH_ANY;
            case MATCH_ALL:
                return PermissionsResolver.MATCH_ALL;
            default:
                logger.warn("Unsupported strategy {}", strategy);
                return PermissionsResolver.MATCH_ALL;
        }
    }

}
