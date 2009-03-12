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

package org.jahia.ajax.gwt.templates.components.layoutmanager.server;


import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.commons.server.AbstractJahiaGWTServiceImpl;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.service.layoutmanager.LayoutmanagerService;
import org.jahia.ajax.gwt.client.data.rss.GWTJahiaRSSFeed;
import org.jahia.ajax.gwt.client.data.layoutmanager.GWTJahiaLayoutItem;
import org.jahia.ajax.gwt.client.data.layoutmanager.GWTJahiaLayoutManagerConfig;
import org.jahia.ajax.gwt.filemanagement.server.helper.FileManagerWorker;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.preferences.JahiaPreference;
import org.jahia.services.preferences.JahiaPreferencesProvider;
import org.jahia.services.preferences.exception.JahiaPreferenceProviderException;
import org.jahia.services.preferences.layoutmanager.LayoutmanagerJahiaPreferenceKey;
import org.jahia.services.preferences.layoutmanager.LayoutmanagerJahiaPreferenceValue;
import org.jahia.services.preferences.layoutmanager.LayoutmanagerJahiaPreference;
import org.jahia.services.preferences.widget.WidgetJahiaPreferenceKey;
import org.jahia.services.preferences.widget.WidgetJahiaPreferenceValue;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRStoreProvider;
import org.jahia.services.applications.ApplicationsManagerService;
import org.jahia.exceptions.JahiaException;
import org.jahia.data.applications.EntryPointInstance;
import org.jahia.api.Constants;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.net.URL;
import java.util.*;

/**
 * User: jahia
 * Date: 19 mars 2008
 * Time: 17:24:54
 */
public class LayoutmanagerServiceImpl extends AbstractJahiaGWTServiceImpl implements LayoutmanagerService {
    private static transient final ServicesRegistry servicesRegistry = ServicesRegistry.getInstance();
    private static transient final Logger logger = Logger.getLogger(LayoutmanagerServiceImpl.class);
    private JahiaPreferencesProvider jahiaPreferencesProvider;
    private JCRStoreService jcrStoreService = ServicesRegistry.getInstance().getJCRStoreService();
    private static final String LAYOUT_NODE_NAME = "j:layout";
    private static final String LAYOUT_ITEM_NODE_NAME = "j:item";
    private static final String COLUMN_INDEX_NODE_PROPERTY = "j:columnIndex";
    private static final String ROW_INDEX_NODE_PROPERTY = "j:rowIndex";
    private static final String STATUS_NODE_PROPERTY = "j:status";
    private static final String PORTLET_NODE_PROPERTY = "j:portlet";
    private static final String PAGE_NODE_PROPERTY = "j:page";
    private static final String LAYOUTMANAGER_NODE_PATH = "/content/layoutmanager";


    /**
     * @param page                       jahia parameter as pid, current mode,...
     * @param layoutItem draggableWidgetPreferences of the corresponding wiget
     */
    public String saveLayoutItem(GWTJahiaPageContext page, GWTJahiaLayoutItem layoutItem) throws GWTJahiaServiceException {
        // key
        LayoutmanagerJahiaPreferenceKey key = createLayoutmanagerPreferenceKey(page, layoutItem);

        //value
        LayoutmanagerJahiaPreferenceValue value = createLayoutmanagerPreferenceValue(layoutItem);

        //get layout manager prefererences
        getLayoutManagerJahiaPreferencesProvider().setJahiaPreference(key, value);

        return key.getWindowId();

    }

    public void saveAsDefault(GWTJahiaPageContext jahiaPageContext) throws GWTJahiaServiceException {
        try {
            String layoutNodeName = getLayoutNodeName(jahiaPageContext);
            JCRNodeWrapper layoutManagerNode = jcrStoreService.getFileNode(LAYOUTMANAGER_NODE_PATH, getRemoteJahiaUser());

            // remove layout_{pid} and its children
            if (layoutManagerNode.hasNode(layoutNodeName)) {
                layoutManagerNode.getNode(layoutNodeName).remove();
            }
            JCRStoreService jcrStoreService = ServicesRegistry.getInstance().getJCRStoreService();


            // create layout[@pid] node
            JCRNodeWrapper layoutNode = layoutManagerNode.addNode(layoutNodeName, Constants.JAHIANT_LAYOUT);
            layoutNode.setProperty(PAGE_NODE_PROPERTY, jahiaPageContext.getPid());

            // for each portlet, create a layoutItem node
            List<GWTJahiaLayoutItem> draggableWidgets = getLayoutItems(jahiaPageContext);
            for (GWTJahiaLayoutItem draggableWidgetPreferences : draggableWidgets) {
                JCRNodeWrapper portletNode = jcrStoreService.getNodeByUUID(draggableWidgetPreferences.getWindowId(), getRemoteJahiaUser());
                if (portletNode != null) {
                    JCRNodeWrapper layoutItemNode = layoutNode.addNode(LAYOUT_ITEM_NODE_NAME, Constants.JAHIANT_LAYOUTITEM);
                    layoutItemNode.setProperty(COLUMN_INDEX_NODE_PROPERTY, draggableWidgetPreferences.getColumn());
                    layoutItemNode.setProperty(ROW_INDEX_NODE_PROPERTY, draggableWidgetPreferences.getRow());
                    layoutItemNode.setProperty(STATUS_NODE_PROPERTY, draggableWidgetPreferences.getStatus());
                    layoutItemNode.setProperty(PORTLET_NODE_PROPERTY, portletNode);
                } else {
                    logger.error("Portlet Instance with uuid[" + draggableWidgetPreferences.getWindowId() + "] not found.");
                }
            }
            layoutManagerNode.save();
        } catch (Exception e) {
            logger.error("Unable to save default config due to", e);

        }

    }

    /**
     * Get layout node name depending on jahia page context
     *
     * @param jahiaPageContext
     * @return
     */
    private String getLayoutNodeName(GWTJahiaPageContext jahiaPageContext) {
        return LAYOUT_NODE_NAME + "_" + jahiaPageContext.getPid();
    }

    /**
     * Delete a widget from the layout manager
     *
     * @param gwtJahiaPageContext
     * @param layoutItem
     */
    public void removeLayoutItem(GWTJahiaPageContext gwtJahiaPageContext, GWTJahiaLayoutItem layoutItem) throws GWTJahiaServiceException {
        // key
        LayoutmanagerJahiaPreferenceKey key = createLayoutmanagerPreferenceKey(gwtJahiaPageContext, layoutItem);

        //get layout manager prefererences
        getLayoutManagerJahiaPreferencesProvider().deleteJahiaPreference(key);

    }

    /**
     * Save layout manager state
     *
     * @param pageContext
     * @param layoutItems
     */
    public void saveLayoutItems(GWTJahiaPageContext pageContext, List<GWTJahiaLayoutItem> layoutItems) throws GWTJahiaServiceException {
        for (GWTJahiaLayoutItem draggableWidgetPreferences : layoutItems) {
            saveLayoutItem(pageContext, draggableWidgetPreferences);
        }

    }

    /**
     * Save layout manager config
     *
     * @param pageContext
     * @param gwtLayoutManagerConfig
     */
    public void saveLayoutmanagerConfig(GWTJahiaPageContext pageContext, GWTJahiaLayoutManagerConfig gwtLayoutManagerConfig) throws GWTJahiaServiceException {
        try {
            setPagePreferenceValue("lm_" + gwtLayoutManagerConfig.getId() + "_" + "nbColumns", String.valueOf(gwtLayoutManagerConfig.getNbColumns()));
            setPagePreferenceValue("lm_" + gwtLayoutManagerConfig.getId() + "_" + "liveDraggable", String.valueOf(gwtLayoutManagerConfig.isLiveDraggable()));
            setPagePreferenceValue("lm_" + gwtLayoutManagerConfig.getId() + "_" + "liveQuickbarVisible", String.valueOf(gwtLayoutManagerConfig.isLiveQuickbarVisible()));
        } catch (Exception e) {
            logger.error("Can't save layout manager config.", e);
        }
    }

    /**
     * Get the layout manager config
     *
     * @return
     */
    public GWTJahiaLayoutManagerConfig getLayoutmanagerConfig() {
        GWTJahiaLayoutManagerConfig gwtLayoutManagerConfig = new GWTJahiaLayoutManagerConfig();
        String nbColumns = "3";
        String liveDraggable = "true";
        String liveQuickbarVisible = "true";
        try {
            gwtLayoutManagerConfig.setNbColumns(Integer.parseInt(nbColumns));
        } catch (NumberFormatException e) {
            logger.debug("'" + nbColumns + "' is not an integer --> set nnColumn = 3");
            gwtLayoutManagerConfig.setNbColumns(3);
        }
        if (liveDraggable == null) {
            gwtLayoutManagerConfig.setLiveDraggable(true);
        } else {
            gwtLayoutManagerConfig.setLiveDraggable(Boolean.parseBoolean(liveDraggable));
        }

        if (liveQuickbarVisible == null) {
            gwtLayoutManagerConfig.setLiveQuickbarVisible(true);
        } else {
            gwtLayoutManagerConfig.setLiveQuickbarVisible(Boolean.parseBoolean(liveQuickbarVisible));
        }
        return gwtLayoutManagerConfig;
    }


    /**
     * Create a layoutmanager key
     *
     * @param pageContext
     * @param draggableWidgetPreferences
     * @return
     */
    private LayoutmanagerJahiaPreferenceKey createLayoutmanagerPreferenceKey(GWTJahiaPageContext pageContext, GWTJahiaLayoutItem draggableWidgetPreferences) throws GWTJahiaServiceException {
        LayoutmanagerJahiaPreferenceKey key = createLayoutmanagerPreferenceKey(pageContext);
        key.setPid(String.valueOf(pageContext.getPid()));
        key.setWindowId(draggableWidgetPreferences.getWindowId());
        return key;
    }

    /**
     * Create a layout manager key
     *
     * @param page
     * @return
     */
    private LayoutmanagerJahiaPreferenceKey createLayoutmanagerPreferenceKey(GWTJahiaPageContext page) {
        LayoutmanagerJahiaPreferenceKey key = new LayoutmanagerJahiaPreferenceKey();
        key.setPrincipal(getRemoteJahiaUser());
        key.setPid(String.valueOf(page.getPid()));
        return key;
    }

    /**
     * Create a Layout manager JahiaPreference value from a GWTJahiaDraggableWidget
     *
     * @param gwtLayoutItem
     * @return
     */
    private LayoutmanagerJahiaPreferenceValue createLayoutmanagerPreferenceValue(GWTJahiaLayoutItem gwtLayoutItem) {
        LayoutmanagerJahiaPreferenceValue value = new LayoutmanagerJahiaPreferenceValue(gwtLayoutItem.getColumn(), gwtLayoutItem.getRow(), gwtLayoutItem.getStatus());
        return value;
    }

    /**
     * @param jahiaPageContext jahia parameter as pid, current mode,...
     * @return Map <widget Id, widget preferences>
     */

    public List<GWTJahiaLayoutItem> getLayoutItems(GWTJahiaPageContext jahiaPageContext) throws GWTJahiaServiceException {
        List<GWTJahiaLayoutItem> layoutItems = new ArrayList<GWTJahiaLayoutItem>();
        try {
            // create layout manager key
            LayoutmanagerJahiaPreferenceKey preferenceKey = createLayoutmanagerPreferenceKey(jahiaPageContext);

            //get layout manager prefererences
            List<String> revelantAttr = new ArrayList<String>();
            revelantAttr.add("pid");
            List<JahiaPreference> preferences = getLayoutManagerJahiaPreferencesProvider().getJahiaPreferencesByPartialKey(preferenceKey,revelantAttr);
            if (getRemoteJahiaUser() == null || (preferences != null && !preferences.isEmpty())) {
                //convert to "GWT" map
                for (JahiaPreference preference : preferences) {
                    // create preferences values
                    layoutItems.add(createGWTLayoutItem(preference));
                }
            } else {
                logger.debug("No preferences for principalKey: " + preferenceKey + "--> load default config");
                layoutItems = getDefaultLayoutItems(jahiaPageContext);
            }

        } catch (Exception e) {
            logger.error(e, e);
        }
        return layoutItems;
    }

    /**
     * load default config
     *
     * @param jahiaPageContext
     * @throws RepositoryException
     */
    private List<GWTJahiaLayoutItem> getDefaultLayoutItems(GWTJahiaPageContext jahiaPageContext) throws RepositoryException {
        List<GWTJahiaLayoutItem> layoutItems = new ArrayList<GWTJahiaLayoutItem>();
        String layoutNodeName = getLayoutNodeName(jahiaPageContext);
        JCRNodeWrapper layoutManagerNode = jcrStoreService.getFileNode(LAYOUTMANAGER_NODE_PATH, getRemoteJahiaUser());

        // remove layout_{pid} and its children
        if (layoutManagerNode.hasNode(layoutNodeName)) {
            Node layoutNode = layoutManagerNode.getNode(layoutNodeName);
            NodeIterator nodeIterator = layoutNode.getNodes();
            while (nodeIterator != null && nodeIterator.hasNext()) {
                // create preferences values
                GWTJahiaLayoutItem gwtLayoutItem = createGWTLayoutItem(nodeIterator.nextNode());

                // widget are ordered by rows
                int row = gwtLayoutItem.getRow();
                if (row < layoutItems.size() - 1) {
                    layoutItems.add(row, gwtLayoutItem);
                } else {
                    layoutItems.add(gwtLayoutItem);
                }

            }
        } else {
            logger.error("There is no default config for page[pid=" + jahiaPageContext.getPid() + "]");
        }

        return layoutItems;
    }

    /**
     * Create a GWTJahiaDraggableWidget from a layout item
     *
     * @param layoutItem
     * @return
     * @throws RepositoryException
     */
    private GWTJahiaLayoutItem createGWTLayoutItem(Node layoutItem) throws RepositoryException {
        // get column
        int column = (int) layoutItem.getProperty(COLUMN_INDEX_NODE_PROPERTY).getLong();

        // get row
        int row = (int) layoutItem.getProperty(ROW_INDEX_NODE_PROPERTY).getLong();

        // get status
        String status = layoutItem.getProperty(STATUS_NODE_PROPERTY).getString();

        JCRNodeWrapper portletNode = (JCRNodeWrapper) layoutItem.getProperty(PORTLET_NODE_PROPERTY).getNode();

        // create preferences values
        GWTJahiaNode gwtJahiaPortletNode = FileManagerWorker.getGWTJahiaNode(portletNode);
        GWTJahiaLayoutItem gwtLayoutItem = new GWTJahiaLayoutItem(gwtJahiaPortletNode, column, row, status);
        return gwtLayoutItem;
    }

    /**
     * Get widget state
     *
     * @param preference
     * @return
     */
    private GWTJahiaLayoutItem createGWTLayoutItem(JahiaPreference preference) {
        LayoutmanagerJahiaPreferenceKey key = (LayoutmanagerJahiaPreferenceKey) preference.getKey();
        LayoutmanagerJahiaPreferenceValue value = (LayoutmanagerJahiaPreferenceValue) preference.getValue();

        try {
            // retrieve instance id from path
            GWTJahiaNode gwtJahiaNode = FileManagerWorker.getNodeByUUID(key.getWindowId(), retrieveParamBean().getUser());
            return new GWTJahiaLayoutItem(gwtJahiaNode, value.getColumnIndex(), value.getRowIndex(), value.getStatus());
        } catch (RepositoryException e) {
            logger.error("Unable to get JCR Node with uuid " + key.getWindowId());
            return null;
        }
    }

    /**
     * Update rss widget preferecences
     *
     * @param pageContext
     * @param widgetId
     * @param url
     * @param maxEntryDisplay
     */
    public void updateRSSWidgetPreference(GWTJahiaPageContext pageContext, String widgetId, String url, int maxEntryDisplay) {
        // get widget preference
        JahiaPreferencesProvider jahiaPreferencesProvider = getWidgetJahiaPreferencesProvider();

        // create a widget key
        WidgetJahiaPreferenceKey key = (WidgetJahiaPreferenceKey) jahiaPreferencesProvider.createPartialJahiaPreferenceKey(retrieveParamBean(pageContext));
        WidgetJahiaPreferenceValue value = (WidgetJahiaPreferenceValue) jahiaPreferencesProvider.createEmptyJahiaPreferenceValue();

        // set key and value for url pref
        key.setWidgetId(widgetId);
        key.setName("url");
        value.setValue(url);

        // save
        jahiaPreferencesProvider.setJahiaPreference(key, value);

        // set key and value for maxEntryDisplay pref
        key.setWidgetId(widgetId);
        key.setName("maxEntryDisplay");
        value.setValue(String.valueOf(maxEntryDisplay));

        jahiaPreferencesProvider.setJahiaPreference(key, value);

    }

    public void createPorletInstanceWindow(GWTJahiaNode portletNode) {
        final ApplicationsManagerService service = ServicesRegistry.getInstance().getApplicationsManagerService();
        try {
            String entryPointInstanceID = portletNode.getUUID();
            EntryPointInstance app = service.getEntryPointInstance(entryPointInstanceID);

            if (app != null) {

                // check if the app has changed
                /* PortletWindowBean oldAppID = (PortletWindowBean) this.getObject();
              if ((oldAppID != null) &&
                      !oldAppID.getEntryPointInstanceID().equals(appID)) {
                  // App has changed, so delete old groups
                  service.deleteApplicationGroups(oldAppID.getEntryPointInstance());
              }

              // Create new groups on this field ( only if not exists )
              service.createEntryPointInstance()createApplicationGroups(app);  */
            }
        } catch (JahiaException e) {
            logger.error("Unable to create portlet Window Instance.");
        }
    }


    /**
     * Create a GWTRSSFeed from an url
     *
     * @param widgetId
     * @return
     */
    public GWTJahiaRSSFeed loalRssFeed(GWTJahiaPageContext pageContext, String widgetId) throws GWTJahiaServiceException {
        GWTJahiaRSSFeed gwtrssFeed = null;
        try {
            // get widget preference
            JahiaPreferencesProvider jahiaPreferencesProvider = getWidgetJahiaPreferencesProvider();

            // create a widget key
            WidgetJahiaPreferenceKey urlKey = (WidgetJahiaPreferenceKey) jahiaPreferencesProvider.createPartialJahiaPreferenceKey(retrieveParamBean(pageContext));
            urlKey.setWidgetId(widgetId);
            urlKey.setName("url");


            // get value
            WidgetJahiaPreferenceValue urlValue = (WidgetJahiaPreferenceValue) jahiaPreferencesProvider.getJahiaPreferenceValue(urlKey);
            if (urlValue != null) {
                String url = urlValue.getValue();
                if (url != null) {
                    // create a widget key
                    WidgetJahiaPreferenceKey maxEntriesDiplayedKey = (WidgetJahiaPreferenceKey) jahiaPreferencesProvider.createPartialJahiaPreferenceKey(retrieveParamBean(pageContext));
                    maxEntriesDiplayedKey.setWidgetId(widgetId);
                    maxEntriesDiplayedKey.setName("maxEntryDisplay");

                    WidgetJahiaPreferenceValue maxEntriesValue = (WidgetJahiaPreferenceValue) jahiaPreferencesProvider.getJahiaPreferenceValue(maxEntriesDiplayedKey);
                    int maxEntryDisplay = 5;
                    if (maxEntriesValue != null) {
                        try {
                            maxEntryDisplay = Integer.parseInt(maxEntriesValue.getValue());
                        } catch (NumberFormatException e) {
                            logger.error("Can't load maxEntryDisplay --> " + maxEntriesValue.getValue() + " is not an integer.");
                            maxEntryDisplay = 5;
                        }
                    }

                    //load corresponding url
                    URL urlObj = new URL(url);
                    gwtrssFeed = loadRssFeed(urlObj);
                    if (gwtrssFeed != null) {
                        gwtrssFeed.setUrl(url);
                        gwtrssFeed.setNbDisplayedEntries(maxEntryDisplay);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e, e);
            throw new GWTJahiaServiceException(e.getMessage());
        }

        return gwtrssFeed;
    }


    /**
     * @return the layout manager preferences
     */
    private JahiaPreferencesProvider getLayoutManagerJahiaPreferencesProvider() {
        try {
            JahiaPreferencesProvider layoutmanagerJahiaPreferencesProvider = servicesRegistry.getJahiaPreferencesService().getPreferencesProviderByType(LayoutmanagerJahiaPreference.PROVIDER_TYPE);
            return layoutmanagerJahiaPreferencesProvider;
        } catch (JahiaPreferenceProviderException e) {
            logger.error(e, e);
        }
        return null;
    }

    /**
     * Get widget preferences
     *
     * @return
     */
    private JahiaPreferencesProvider getWidgetJahiaPreferencesProvider() {
        try {
            if (jahiaPreferencesProvider == null) {
                jahiaPreferencesProvider = servicesRegistry.getJahiaPreferencesService().getPreferencesProviderByType("org.jahia.preferences.provider.widget");
            }
            return jahiaPreferencesProvider;
        } catch (JahiaPreferenceProviderException e) {
            logger.error(e, e);
        }
        return null;
    }
}
