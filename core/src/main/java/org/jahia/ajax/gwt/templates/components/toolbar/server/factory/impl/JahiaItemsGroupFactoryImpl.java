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

package org.jahia.ajax.gwt.templates.components.toolbar.server.factory.impl;

import org.jahia.ajax.gwt.commons.client.beans.GWTJahiaLabel;
import org.jahia.ajax.gwt.commons.client.beans.GWTJahiaNodeOperationResult;
import org.jahia.ajax.gwt.commons.client.beans.GWTJahiaNodeOperationResultItem;
import org.jahia.ajax.gwt.commons.client.beans.GWTProperty;
import org.jahia.ajax.gwt.engines.workflow.client.model.GWTJahiaWorkflowElement;
import org.jahia.ajax.gwt.engines.workflow.server.helper.WorkflowServiceHelper;
import org.jahia.ajax.gwt.templates.components.toolbar.client.bean.GWTToolbarItem;
import org.jahia.ajax.gwt.templates.components.toolbar.client.ui.mygwt.provider.JahiaProviderFactory;
import org.jahia.ajax.gwt.templates.components.toolbar.server.factory.ItemsGroupFactory;
import org.jahia.content.ContentObjectKey;
import org.jahia.data.JahiaData;
import org.jahia.data.beans.history.HistoryBean;
import org.jahia.exceptions.JahiaException;
import org.jahia.operations.valves.HistoryValve;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.resourcebundle.JahiaResourceBundle;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.preferences.JahiaPreference;
import org.jahia.services.preferences.JahiaPreferenceKey;
import org.jahia.services.preferences.JahiaPreferencesProvider;
import org.jahia.services.preferences.bookmarks.BookmarksJahiaPreferenceKey;
import org.jahia.services.preferences.exception.JahiaPreferenceProviderException;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaGroupManagerService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: jahia
 * Date: 5 ao�t 2008
 * Time: 09:19:15
 */
public class JahiaItemsGroupFactoryImpl implements ItemsGroupFactory {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JahiaItemsGroupFactoryImpl.class);
    private static JahiaPreferencesProvider bookmarksPreferencesProvider;

    public final static String LANG_WORFLOW = "lang.worflow";
    public final static String HISTORY = "history";
    public final static String SITES = "sites";
    public final static String BOOKMARKS = "bookmarks";
    public final static String QUICK_WORKFLOW = "quick.workflow";


    /**
     * Create a list of items depending on the type
     *
     * @param jahiaData
     * @param input
     * @return
     */
    public List<GWTToolbarItem> populateItemsList(List<GWTToolbarItem> gwtToolbarItemsList, JahiaData jahiaData, String input, Map<String, String> properties) {
        if (input != null) {
            // case of history
            if (input.equalsIgnoreCase(HISTORY)) {
                int historyPathLength = 5;
                return populateWithHistoryItems(gwtToolbarItemsList, jahiaData, historyPathLength);
            }
            else if (input.equalsIgnoreCase(BOOKMARKS)) {
                return populateWithBookmarksItems(gwtToolbarItemsList, jahiaData);
            }
            else if (input.equalsIgnoreCase(SITES)) {
                return populateWithChangeSiteItems(gwtToolbarItemsList, jahiaData);
            }
            else if (input.equalsIgnoreCase(QUICK_WORKFLOW)) {
                return populateWithQuickWorkflowItems(gwtToolbarItemsList, jahiaData);
            }
        }
        return gwtToolbarItemsList;
    }


    /**
     * Populate with hystory items
     *
     * @param jahiaData
     * @param historyPathLength
     * @return
     */
    public List<GWTToolbarItem> populateWithHistoryItems(List<GWTToolbarItem> gwtToolbarItemsList, JahiaData jahiaData, int historyPathLength) {
        // create the list item
        List<HistoryBean> historyBeanList = (List<HistoryBean>) jahiaData.getProcessingContext().getSessionState().getAttribute(HistoryValve.ORG_JAHIA_TOOLBAR_HISTORY);
        if (historyBeanList != null) {
            if (historyPathLength < 0) {
                historyPathLength = historyBeanList.size();
            }
            int iteratorsize = historyPathLength < historyBeanList.size() ? historyPathLength : historyBeanList.size();
            for (int i = 0; i < iteratorsize; i++) {
                HistoryBean cHistoryBean = historyBeanList.get(i);
                int pid = cHistoryBean.getPid();
                try {
                    GWTToolbarItem gwtToolbarItem = createRedirectItem(jahiaData, null, pid);
                    if (gwtToolbarItem != null) {
                        String minIconStyle = "gwt-toolbar-ItemsGroup-icons-page-min";
                        String maxIconStyle = "gwt-toolbar-ItemsGroup-icons-page-normal";
                        gwtToolbarItem.setMediumIconStyle(maxIconStyle);
                        gwtToolbarItem.setMinIconStyle(minIconStyle);
                        // add to itemsgroup
                        gwtToolbarItemsList.add(gwtToolbarItem);
                    }
                } catch (Exception e) {
                    logger.error(e, e);
                }
            }
        }
        return gwtToolbarItemsList;
    }

    /**
     * populate with bookmark items
     */
    public List<GWTToolbarItem> populateWithBookmarksItems(List<GWTToolbarItem> gwtToolbarItemsList, JahiaData jahiaData) {
        // get bookmarks provider
        JahiaPreferencesProvider jahiaPreferencesProvider = getBookmarksJahiaPreferencesProvider();
        Map<JahiaPreferenceKey, JahiaPreference> jahiaPreferencesMap = jahiaPreferencesProvider.getJahiaPreferences(jahiaData.getProcessingContext());
        if (jahiaPreferencesMap != null) {
            for (JahiaPreferenceKey key : jahiaPreferencesMap.keySet()) {
                // current bookmark
                BookmarksJahiaPreferenceKey bKey = (BookmarksJahiaPreferenceKey) key;
                int pid = bKey.getPid();
                try {
                    GWTToolbarItem gwtToolbarItem = createRedirectItem(jahiaData, null, pid);
                    if (gwtToolbarItem != null) {
                        String minIconStyle = "gwt-toolbar-ItemsGroup-icons-bookmark-min";
                        String maxIconStyle = "gwt-toolbar-ItemsGroup-icons-bookmark-min";
                        gwtToolbarItem.setMediumIconStyle(maxIconStyle);
                        gwtToolbarItem.setMinIconStyle(minIconStyle);
                        // add to itemsgroup
                        gwtToolbarItemsList.add(gwtToolbarItem);
                    }
                } catch (Exception e) {
                    logger.error(e);
                }
            }
        }
        return gwtToolbarItemsList;
    }

    /**
     * Get Bookmark jahia preference provider
     *
     * @return
     */
    private JahiaPreferencesProvider getBookmarksJahiaPreferencesProvider() {
        try {
            if (bookmarksPreferencesProvider == null) {
                bookmarksPreferencesProvider = ServicesRegistry.getInstance().getJahiaPreferencesService().getPreferencesProviderByType("org.jahia.preferences.provider.bookmarks");
            }
            return bookmarksPreferencesProvider;
        } catch (JahiaPreferenceProviderException e) {
            logger.error(e, e);
        }
        return null;
    }

    /**
     * Switch site items
     *
     * @param gwtToolbarItemsList
     * @param jahiaData
     * @return
     */
    public List<GWTToolbarItem> populateWithChangeSiteItems(List<GWTToolbarItem> gwtToolbarItemsList, JahiaData jahiaData) {
        try {
            JahiaGroupManagerService jahiaGroupManagerService = ServicesRegistry.getInstance().getJahiaGroupManagerService();
            List<JahiaSite> sitesList = jahiaGroupManagerService.getAdminGrantedSites(jahiaData.getProcessingContext().getUser());
            if (sitesList != null && sitesList.size() > 1) {
                for (JahiaSite site : sitesList) {
                    GWTToolbarItem gwtToolbarItem = createRedirectItem(jahiaData, site.getTitle(), site.getHomePage());
                    // add to itemsgroup
                    if (gwtToolbarItem != null) {
                        String minIconStyle = "gwt-toolbar-ItemsGroup-icons-site-min";
                        String maxIconStyle = "gwt-toolbar-ItemsGroup-icons-site-min";
                        gwtToolbarItem.setMediumIconStyle(maxIconStyle);
                        gwtToolbarItem.setMinIconStyle(minIconStyle);
                        if (jahiaData.getProcessingContext().getSiteID() == site.getID()) {
                            gwtToolbarItem.setSelected(true);
                        }
                        // add to group lis
                        gwtToolbarItemsList.add(gwtToolbarItem);
                    }
                }
            }
        } catch (JahiaException e) {
            logger.error("JahiaException: Error while creating change site link", e);
        } catch (Exception e) {
            logger.error("Error while creating change site link", e);
        }
        return gwtToolbarItemsList;
    }

    /**
     * Get available actions for current page
     *
     * @param gwtToolbarItemsList
     * @param jahiaData
     * @return
     */
    public List<GWTToolbarItem> populateWithQuickWorkflowItems(List<GWTToolbarItem> gwtToolbarItemsList, JahiaData jahiaData) {
        try {
            ProcessingContext processingContext = jahiaData.getProcessingContext();
            ContentObjectKey currentObjectKey = (ContentObjectKey) processingContext.getContentPage().getObjectKey();
            String lang = processingContext.getLocale().getLanguage();
            List<String> languages = new ArrayList<String>(1);
            languages.add(lang);
            GWTJahiaWorkflowElement elem = WorkflowServiceHelper.getWorkflowElement(processingContext.getContentPage(), true, processingContext);
            Map<String, GWTJahiaNodeOperationResult> val = elem.getValidation();
            if (val != null && val.size() > 0) {
                int warns = 0;
                int errors = 0;
                List<GWTJahiaNodeOperationResultItem> vals = new ArrayList<GWTJahiaNodeOperationResultItem>();
                GWTJahiaNodeOperationResult validation = val.get(processingContext.getCurrentLocale().toString());
                if (validation != null)  {
                    vals.addAll(validation.getErrorsAndWarnings());
                    for (GWTJahiaNodeOperationResultItem resultItem : vals) {
                        if (resultItem.getLevel() == GWTJahiaNodeOperationResultItem.WARNING) {
                            warns++;
                        } else if (resultItem.getLevel() == GWTJahiaNodeOperationResultItem.ERROR) {
                            errors++;
                        }
                    }
                }
                if (warns>0) {
                    GWTToolbarItem gwtToolbarItem = new GWTToolbarItem();
                    gwtToolbarItem.setTitle(vals.size()+" warnings");
                    gwtToolbarItem.setType(JahiaProviderFactory.ORG_JAHIA_TOOLBAR_ITEM_QUICK_WORKFLOW);
                    gwtToolbarItem.setDisplayTitle(true);
                    
                    gwtToolbarItem.setMinIconStyle("gwt-toolbar-ItemsGroup-icons-workflow-warn");
                    gwtToolbarItemsList.add(gwtToolbarItem);
                }
                if (errors>0) {
                    GWTToolbarItem gwtToolbarItem = new GWTToolbarItem();
                    gwtToolbarItem.setTitle(vals.size()+" errors");
                    gwtToolbarItem.setType(JahiaProviderFactory.ORG_JAHIA_TOOLBAR_ITEM_QUICK_WORKFLOW);
                    gwtToolbarItem.setDisplayTitle(true);
                    gwtToolbarItem.setMinIconStyle("gwt-toolbar-ItemsGroup-icons-workflow-warn");
                    gwtToolbarItemsList.add(gwtToolbarItem);
                }
            }
            Map<String, Set<String>> actionsMap = elem.getAvailableAction();
            if (elem.getStealLock() != null || elem.isValidationBlocker()) {
                actionsMap.clear();
            }
            if (actionsMap.size() > 0 && actionsMap.containsKey(lang)) {
                for (GWTJahiaLabel actionLabel : WorkflowServiceHelper.getAvailableActions(processingContext.getLocale())) {
                    if (actionsMap.get(lang).contains(actionLabel.getKey())) {
                        String key = actionLabel.getKey();
                        String label = actionLabel.getLabel();
                        GWTToolbarItem gwtToolbarItem = createQuickWorkflowItem(currentObjectKey.getKey(), lang, key, label);
                        // add to itemsgroup
                        if (gwtToolbarItem != null) {
                            String minIconStyle = "gwt-toolbar-ItemsGroup-icons-action-" + key + "-min";
                            String maxIconStyle = "gwt-toolbar-ItemsGroup-icons-action-" + key + "-min";
                            gwtToolbarItem.setMediumIconStyle(maxIconStyle);
                            gwtToolbarItem.setMinIconStyle(minIconStyle);
                            // add to group lis
                            gwtToolbarItemsList.add(gwtToolbarItem);
                        }
                    }
                }
            }

            if (ServicesRegistry.getInstance().getJahiaACLManagerService().getSiteActionPermission("engines.actions.publishAll", processingContext.getUser(),
                    JahiaBaseACL.READ_RIGHTS, processingContext.getSiteID()) > 0) {
                Set<String> m = ServicesRegistry.getInstance().getWorkflowService().getAllStagingAndWaitingObject(jahiaData.getProcessingContext().getSiteID()).keySet();
                if (!m.isEmpty()) {
                    String publishAllLabel = JahiaResourceBundle.getEngineResource("org.jahia.engines.workflow.publishAll",
                            processingContext,
                            processingContext.getLocale() );
                    GWTToolbarItem gwtToolbarItem = createPublishAllItem(publishAllLabel);
                    // add to itemsgroup
                
                    if (gwtToolbarItem != null) {
                        String minIconStyle = "gwt-toolbar-ItemsGroup-icons-action-publish-min";
                        String maxIconStyle = "gwt-toolbar-ItemsGroup-icons-action-publish-min";
                        gwtToolbarItem.setMediumIconStyle(maxIconStyle);
                        gwtToolbarItem.setMinIconStyle(minIconStyle);
                        // add to group lis
                        gwtToolbarItemsList.add(gwtToolbarItem);
                    }
                }
            }
        } catch (JahiaException e) {
            logger.error("JahiaException: Error while creating change site link", e);
        } catch (Exception e) {
            logger.error("Error while creating change site link", e);
        }
        return gwtToolbarItemsList;
    }

    /**
     * create a redirect toolitem. If itemTitle is null, then the pageTitle will be the itemTitle.
     *
     * @param jahiaData
     * @param pid
     * @return
     * @throws JahiaException
     */
    private GWTToolbarItem createRedirectItem(JahiaData jahiaData, String itemTitle, Integer pid) {
        try {
            JahiaPage jahiaPage = ServicesRegistry.getInstance().getJahiaPageService().lookupPage(pid, jahiaData.getProcessingContext());

            return createRedirectItem(jahiaData, itemTitle, jahiaPage);
        } catch (JahiaException e) {
            logger.debug("Page with id[" + pid + "] has been deleted");
            return null;
        }
    }

    /**
     * create a redirect toolitem
     *
     * @param jahiaData
     * @return
     * @throws JahiaException
     */
    private GWTToolbarItem createRedirectItem(JahiaData jahiaData, String itemTitle, JahiaPage jahiaPage) throws JahiaException {
        if (jahiaPage != null) {
            String url = jahiaData.getProcessingContext().composePageUrl(jahiaPage);
            if (url == null) {
                return null;
            }
            String title = itemTitle;
            if (title == null) {
                title = jahiaPage.getTitle();
                if (title == null || title.length() == 0) {
                    title = "[pid=" + jahiaPage.getID() + "]";
                }
            }

            // create the toolitem
            GWTToolbarItem gwtToolbarItem = new GWTToolbarItem();
            gwtToolbarItem.setTitle(title);
            gwtToolbarItem.setType(JahiaProviderFactory.ORG_JAHIA_TOOLBAR_ITEM_REDIRECT_WINDOW);
            gwtToolbarItem.setDisplayTitle(true);

            // add url property
            GWTProperty gwtProperty = new GWTProperty();
            gwtProperty.setName("url");
            gwtProperty.setValue(url);
            gwtToolbarItem.addProperty(gwtProperty);
            return gwtToolbarItem;

        }
        return null;
    }


    /**
     * create a quick workflow action toolitem
     *
     * @param objectKey
     * @param language
     * @param action
     * @param label
     * @return
     * @throws JahiaException
     */
    private GWTToolbarItem createQuickWorkflowItem(String objectKey, String language, String action, String label) throws JahiaException {
        if (objectKey != null && objectKey.length() > 0 && action != null && action.length() > 0) {

            // create the toolitem
            GWTToolbarItem gwtToolbarItem = new GWTToolbarItem();
            gwtToolbarItem.setTitle(label);
            gwtToolbarItem.setType(JahiaProviderFactory.ORG_JAHIA_TOOLBAR_ITEM_QUICK_WORKFLOW);
            gwtToolbarItem.setDisplayTitle(true);

            // add url property
            GWTProperty gwtProperty = new GWTProperty();
            gwtProperty.setName("action");
            gwtProperty.setValue(action);
            gwtToolbarItem.addProperty(gwtProperty);
            gwtProperty = new GWTProperty();
            gwtProperty.setName("language");
            gwtProperty.setValue(language);
            gwtToolbarItem.addProperty(gwtProperty);
            gwtProperty = new GWTProperty();
            gwtProperty.setName("objectKey");
            gwtProperty.setValue(objectKey);
            gwtToolbarItem.addProperty(gwtProperty);
            gwtProperty = new GWTProperty();
            gwtProperty.setName("label");
            gwtProperty.setValue(label);
            gwtToolbarItem.addProperty(gwtProperty);

            return gwtToolbarItem;
        }
        return null;
    }

    /**
     * create a quick workflow batch action toolitem (publish all, notify all)
     *
     * @param label
     * @return
     * @throws JahiaException
     */
    private GWTToolbarItem createPublishAllItem(String label) throws JahiaException {
        // create the toolitem
        GWTToolbarItem gwtToolbarItem = new GWTToolbarItem();
        gwtToolbarItem.setTitle(label);
        gwtToolbarItem.setType(JahiaProviderFactory.ORG_JAHIA_TOOLBAR_ITEM_QUICK_WORKFLOW);
        gwtToolbarItem.setDisplayTitle(true);

        // add label property
        GWTProperty gwtProperty = new GWTProperty();
        gwtProperty.setName("action");
        gwtProperty.setValue("publishAll");
        gwtToolbarItem.addProperty(gwtProperty);
        gwtProperty = new GWTProperty();
        gwtProperty.setName("label");
        gwtProperty.setValue(label);
        gwtToolbarItem.addProperty(gwtProperty);
        gwtProperty = new GWTProperty();
        gwtProperty.setName("language");
        gwtProperty.setValue("all");
        gwtToolbarItem.addProperty(gwtProperty);
        gwtProperty = new GWTProperty();
        gwtProperty.setName("objectKey");
        gwtProperty.setValue("all");
        gwtToolbarItem.addProperty(gwtProperty);

        return gwtToolbarItem;
    }

}
