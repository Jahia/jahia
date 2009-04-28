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
import org.jahia.ajax.gwt.client.data.layoutmanager.GWTJahiaLayoutItem;
import org.jahia.ajax.gwt.client.data.layoutmanager.GWTJahiaLayoutManagerConfig;
import org.jahia.ajax.gwt.filemanagement.server.helper.FileManagerWorker;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.preferences.JahiaPreferencesProvider;
import org.jahia.services.preferences.JahiaPreferencesXpathHelper;
import org.jahia.services.preferences.JahiaPreference;
import org.jahia.services.preferences.exception.JahiaPreferenceProviderException;
import org.jahia.services.preferences.layoutmanager.LayoutmanagerJahiaPreference;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRLayoutItemNode;
import org.jahia.services.content.JCRLayoutNode;
import org.jahia.services.applications.ApplicationsManagerService;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.exceptions.JahiaException;
import org.jahia.data.applications.EntryPointInstance;
import org.jahia.api.Constants;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.util.*;
import java.security.Principal;

/**
 * User: jahia
 * Date: 19 mars 2008
 * Time: 17:24:54
 */
public class LayoutmanagerServiceImpl extends AbstractJahiaGWTServiceImpl implements LayoutmanagerService {
    private static transient final ServicesRegistry servicesRegistry = ServicesRegistry.getInstance();
    private static transient final Logger logger = Logger.getLogger(LayoutmanagerServiceImpl.class);
    private JCRStoreService jcrStoreService = ServicesRegistry.getInstance().getJCRStoreService();
    private static final String LAYOUTMANAGER_NODE_PATH = "/content/layoutmanager";


    /**
     * @param page               jahia parameter as pid, current mode,...
     * @param gwtJahiaLayoutItem draggableWidgetPreferences of the corresponding wiget
     */
    public String saveLayoutItem(GWTJahiaPageContext page, GWTJahiaLayoutItem gwtJahiaLayoutItem) throws GWTJahiaServiceException {
        try {
            // key
            JCRNodeWrapper layoutItemNode = jcrStoreService.getNodeByUUID(gwtJahiaLayoutItem.getUuid(), getRemoteJahiaUser());
            JCRLayoutItemNode jcrLayoutItemNode = new JCRLayoutItemNode(layoutItemNode);
            JCRNodeWrapper portletNode = jcrStoreService.getNodeByUUID(gwtJahiaLayoutItem.getPortlet(), getRemoteJahiaUser());
            if (portletNode != null) {
                jcrLayoutItemNode.setColumnIndex(gwtJahiaLayoutItem.getColumn());
                jcrLayoutItemNode.setRowIndex(gwtJahiaLayoutItem.getRow());
                jcrLayoutItemNode.setStatus(gwtJahiaLayoutItem.getStatus());
                jcrLayoutItemNode.setPortlet(portletNode);
                jcrLayoutItemNode.save();
            } else {
                logger.error("Portlet Instance with uuid[" + gwtJahiaLayoutItem.getPortlet() + "] not found.");
            }
            return jcrLayoutItemNode.getPortlet().getUUID();
        } catch (RepositoryException e) {
            logger.error(e, e);
            return null;
        }

    }

    /**
     * Save current configuration as default one
     *
     * @param jahiaPageContext
     * @throws GWTJahiaServiceException
     */
    public void saveAsDefault(GWTJahiaPageContext jahiaPageContext) throws GWTJahiaServiceException {
        try {
            ContentPage currentContentPage = ServicesRegistry.getInstance().getJahiaPageService().lookupContentPage(jahiaPageContext.getPid(), false);
            JCRNodeWrapper layoutmanagerNode = findLayoutmanagerNode();
            if (layoutmanagerNode != null) {
                JCRLayoutNode layoutNode = findDefaultLayoutNode();

                // remove layout_{pid} and its children
                if (layoutNode != null) {
                    List<JCRNodeWrapper> nodeWrappers = layoutNode.getChildren();
                    for (JCRNodeWrapper nodeWrapper : nodeWrappers) {
                        nodeWrapper.remove();
                    }
                } else {
                    // update layoutNode
                    layoutNode = new JCRLayoutNode(layoutmanagerNode.addNode("j:layout", Constants.JAHIANT_LAYOUT));
                    layoutNode.setPage(currentContentPage.getUUID());
                }

                // update layout node
                List<GWTJahiaLayoutItem> gwtJahiaLayoutItems = getLayoutItems(jahiaPageContext);
                for (GWTJahiaLayoutItem gwtJahiaLayoutItem : gwtJahiaLayoutItems) {
                    JCRNodeWrapper portletNode = jcrStoreService.getNodeByUUID(gwtJahiaLayoutItem.getPortlet(), getRemoteJahiaUser());
                    if (portletNode != null) {
                        // retrieve layout item node
                        if (gwtJahiaLayoutItem.getUuid() != null) {
                            JCRNodeWrapper nodeWrapper = layoutNode.addLayoutItem(portletNode, gwtJahiaLayoutItem.getColumn(), gwtJahiaLayoutItem.getRow(), gwtJahiaLayoutItem.getStatus());
                            JCRLayoutItemNode jcrLayoutItemNode = new JCRLayoutItemNode(nodeWrapper);
                            jcrLayoutItemNode.setColumnIndex(gwtJahiaLayoutItem.getColumn());
                            jcrLayoutItemNode.setRowIndex(gwtJahiaLayoutItem.getRow());
                            jcrLayoutItemNode.setStatus(gwtJahiaLayoutItem.getStatus());
                            jcrLayoutItemNode.setPortlet(portletNode);
                        }
                    }
                }

                // save changes
                layoutmanagerNode.save();
            }
        } catch (Exception e) {
            logger.error("Unable to save default config due to", e);

        }

    }

    /**
     * @param jahiaPageContext
     * @param gwtJahiaLayoutItems
     * @throws GWTJahiaServiceException
     */
    public void saveLayoutItems(GWTJahiaPageContext jahiaPageContext, List<GWTJahiaLayoutItem> gwtJahiaLayoutItems) throws GWTJahiaServiceException {
        try {
            ContentPage currentContentPage = ServicesRegistry.getInstance().getJahiaPageService().lookupContentPage(jahiaPageContext.getPid(), false);


            // get layout manager node
            JahiaPreference<JCRLayoutNode> layoutmanagerJahiaPreference = getLayoutManagerJahiaPreferencesProvider().getJahiaPreference(getRemoteJahiaUser(), JahiaPreferencesXpathHelper.getLayoutmanagerXpath(jahiaPageContext.getPid()));
            if (layoutmanagerJahiaPreference == null) {
                layoutmanagerJahiaPreference = createPartialLayoutmanagerPreference();
                // update page
                layoutmanagerJahiaPreference.getNode().setPage(currentContentPage.getUUID());
            }

            // update a layoutNode
            JCRLayoutNode layoutNode = layoutmanagerJahiaPreference.getNode();

            // update layout node
            updateLayoutNode(gwtJahiaLayoutItems, layoutNode);

        } catch (Exception e) {
            logger.error("Error while saving layoumanager pref.", e);

        }

    }

    /**
     * @param jahiaPageContext
     * @param gwtJahiaLayoutItem
     * @throws GWTJahiaServiceException
     */
    public void addLayoutItem(GWTJahiaPageContext jahiaPageContext, GWTJahiaLayoutItem gwtJahiaLayoutItem) throws GWTJahiaServiceException {
        try {

            JCRNodeWrapper portletNode = jcrStoreService.getNodeByUUID(gwtJahiaLayoutItem.getPortlet(), getRemoteJahiaUser());
            if (portletNode != null) {
                ContentPage currentContentPage = ServicesRegistry.getInstance().getJahiaPageService().lookupContentPage(jahiaPageContext.getPid(), false);


                // get layout manager node
                JahiaPreference<JCRLayoutNode> layoutmanagerJahiaPreference = getLayoutManagerJahiaPreferencesProvider().getJahiaPreference(getRemoteJahiaUser(), JahiaPreferencesXpathHelper.getLayoutmanagerXpath(jahiaPageContext.getPid()));
                if (layoutmanagerJahiaPreference == null) {
                    layoutmanagerJahiaPreference = createPartialLayoutmanagerPreference();
                    // update page
                    layoutmanagerJahiaPreference.getNode().setPage(currentContentPage.getUUID());
                }

                //add layout item
                JCRLayoutNode layoutNode = layoutmanagerJahiaPreference.getNode();
                layoutNode.addLayoutItem(portletNode, gwtJahiaLayoutItem.getColumn(), gwtJahiaLayoutItem.getRow(), gwtJahiaLayoutItem.getStatus());

                // save pref
                getLayoutManagerJahiaPreferencesProvider().setJahiaPreference(layoutmanagerJahiaPreference);
            }
        } catch (Exception e) {
            logger.error("Error while saving layoumanager pref.", e);

        }

    }

    /**
     * Update layoutnaode
     *
     * @param gwtJahiaLayoutItems
     * @param layoutNode
     * @throws RepositoryException
     */
    private void updateLayoutNode(List<GWTJahiaLayoutItem> gwtJahiaLayoutItems, JCRLayoutNode layoutNode) throws RepositoryException {
        for (GWTJahiaLayoutItem gwtJahiaLayoutItem : gwtJahiaLayoutItems) {
            JCRNodeWrapper portletNode = jcrStoreService.getNodeByUUID(gwtJahiaLayoutItem.getPortlet(), getRemoteJahiaUser());
            if (portletNode != null) {
                // retrieve layout item node
                if (gwtJahiaLayoutItem.getUuid() != null) {
                    JCRNodeWrapper nodeWrapper = jcrStoreService.getNodeByUUID(gwtJahiaLayoutItem.getUuid(), getRemoteJahiaUser());
                    JCRLayoutItemNode jcrLayoutItemNode = new JCRLayoutItemNode(nodeWrapper);
                    jcrLayoutItemNode.setColumnIndex(gwtJahiaLayoutItem.getColumn());
                    jcrLayoutItemNode.setRowIndex(gwtJahiaLayoutItem.getRow());
                    jcrLayoutItemNode.setStatus(gwtJahiaLayoutItem.getStatus());
                    jcrLayoutItemNode.setPortlet(portletNode);
                    jcrLayoutItemNode.save();
                }
            } else {
                logger.error("Portlet Instance with uuid[" + gwtJahiaLayoutItem.getPortlet() + "] not found.");
            }
        }
    }


    /**
     * Delete a widget from the layout manager
     *
     * @param gwtJahiaPageContext
     * @param layoutItem
     */
    public void removeLayoutItem(GWTJahiaPageContext gwtJahiaPageContext, GWTJahiaLayoutItem layoutItem) throws GWTJahiaServiceException {
        //get layout manager prefererences
        try {
            Node layoutItemNode = jcrStoreService.getNodeByUUID(layoutItem.getUuid(), getRemoteJahiaUser());
            if (layoutItemNode != null) {
                Node parentNode = layoutItemNode.getParent();
                layoutItemNode.remove();
                parentNode.save();
            }
        } catch (Exception e) {
            logger.error(e, e);
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
            // create layout[@pid] node
            JahiaPreference<JCRLayoutNode> layoutmanagerJahiaPreference = getLayoutManagerJahiaPreferencesProvider().getJahiaPreference(getRemoteJahiaUser(), JahiaPreferencesXpathHelper.getLayoutmanagerXpath(pageContext.getPid()));
            if (layoutmanagerJahiaPreference == null) {
                layoutmanagerJahiaPreference = createPartialLayoutmanagerPreference();
                ContentPage currentContentPage = ServicesRegistry.getInstance().getJahiaPageService().lookupContentPage(pageContext.getPid(), false);
                layoutmanagerJahiaPreference.getNode().setPage(currentContentPage.getUUID());
            }
            JCRLayoutNode node = layoutmanagerJahiaPreference.getNode();
            node.setNbColumns(gwtLayoutManagerConfig.getNbColumns());
            node.setLiveDraggable(gwtLayoutManagerConfig.isLiveDraggable());
            node.setLiveDraggable(gwtLayoutManagerConfig.isLiveQuickbarVisible());
            getLayoutManagerJahiaPreferencesProvider().setJahiaPreference(layoutmanagerJahiaPreference);
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
        JahiaPreference<JCRLayoutNode> layoutmanagerJahiaPreference = getLayoutManagerJahiaPreferencesProvider().getJahiaPreference(getRemoteJahiaUser(), JahiaPreferencesXpathHelper.getLayoutmanagerXpath(retrieveParamBean().getPageID()));
        GWTJahiaLayoutManagerConfig gwtLayoutManagerConfig = new GWTJahiaLayoutManagerConfig();
        gwtLayoutManagerConfig.setNbColumns(3);
        gwtLayoutManagerConfig.setLiveDraggable(true);
        gwtLayoutManagerConfig.setLiveQuickbarVisible(true);
        if (layoutmanagerJahiaPreference != null) {
            try {
                JCRLayoutNode node = layoutmanagerJahiaPreference.getNode();
                gwtLayoutManagerConfig.setNbColumns((int) node.getNbColumns());
                gwtLayoutManagerConfig.setLiveDraggable(node.isLiveDraggable());
                gwtLayoutManagerConfig.setLiveQuickbarVisible(node.isLiveEditable());
            } catch (Exception e) {
                logger.error(e, e);
            }
        }
        return gwtLayoutManagerConfig;
    }


    /**
     * Create a layoutmanager key
     *
     * @return
     */
    private JahiaPreference<JCRLayoutNode> createPartialLayoutmanagerPreference() {
        return getLayoutManagerJahiaPreferencesProvider().createJahiaPreferenceNode(getRemoteJahiaUser());
    }


    /**
     * @param jahiaPageContext jahia parameter as pid, current mode,...
     * @return Map <widget Id, widget preferences>
     */

    public List<GWTJahiaLayoutItem> getLayoutItems(GWTJahiaPageContext jahiaPageContext) throws GWTJahiaServiceException {
        List<GWTJahiaLayoutItem> layoutItems = new ArrayList<GWTJahiaLayoutItem>();

        try {
            JahiaPreference<JCRLayoutNode> layoutmanagerNode = getLayoutManagerJahiaPreferencesProvider().getJahiaPreference(getRemoteJahiaUser(), JahiaPreferencesXpathHelper.getLayoutmanagerXpath(jahiaPageContext.getPid()));

            if (layoutmanagerNode != null) {
                logger.debug("Layoutmanager config found for user [" + getRemoteUser() + "]");
                return fillGWTLayoutItems(jahiaPageContext, (JCRLayoutNode) layoutmanagerNode.getNode());
            } else {
                logger.debug("Layoutmanager for user [" + getRemoteUser() + "] not found --> load default one");
                return getDefaultLayoutItems(jahiaPageContext);
            }
        } catch (RepositoryException e) {
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
        JCRLayoutNode layoutNode = findDefaultLayoutNode();
        return fillGWTLayoutItems(jahiaPageContext, layoutNode);
    }

    /**
     * Create corresponding layout item list
     *
     * @param jahiaPageContext
     * @param jcrLayoutNode
     * @return
     * @throws RepositoryException
     */
    private List<GWTJahiaLayoutItem> fillGWTLayoutItems(GWTJahiaPageContext jahiaPageContext, JCRLayoutNode jcrLayoutNode) throws RepositoryException {
        // remove layout_{pid} and its children
        List<GWTJahiaLayoutItem> gwtJahiaLayoutItems = new ArrayList<GWTJahiaLayoutItem>();
        if (jcrLayoutNode != null) {
            List<JCRLayoutItemNode> jcrLayoutItemNodes = jcrLayoutNode.getLayoutItems();
            for (JCRLayoutItemNode layoutItemNode : jcrLayoutItemNodes) {
                // create preferences values
                gwtJahiaLayoutItems.add(fillGWTLayoutItem(layoutItemNode));
            }
        } else {
            logger.debug("There is no default config for page[pid=" + jahiaPageContext.getPid() + "]");
        }
        return gwtJahiaLayoutItems;
    }

    /**
     * Create a GWTJahiaDraggableWidget from a layout item
     *
     * @param jcrLayoutItemNode
     * @return
     * @throws RepositoryException
     */
    private GWTJahiaLayoutItem fillGWTLayoutItem(JCRLayoutItemNode jcrLayoutItemNode) throws RepositoryException {
        // get column
        int column = jcrLayoutItemNode.getColumnIndex();

        // get row
        int row = jcrLayoutItemNode.getRowIndex();

        // get status
        String status = jcrLayoutItemNode.getStatus();

        JCRNodeWrapper portletNode = (JCRNodeWrapper) jcrLayoutItemNode.getPortlet();

        return new GWTJahiaLayoutItem(jcrLayoutItemNode.getUUID(), FileManagerWorker.getGWTJahiaNode(portletNode), column, row, status);
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
     * @return the layout manager preferences
     */
    private JahiaPreferencesProvider<JCRLayoutNode> getLayoutManagerJahiaPreferencesProvider() {
        try {
            return servicesRegistry.getJahiaPreferencesService().getPreferencesProviderByClass(JCRLayoutNode.class);
        } catch (JahiaPreferenceProviderException e) {
            logger.error(e, e);
        }
        return null;
    }

    /*
    * Get default layout node of the current page
    * */
    private JCRLayoutNode findDefaultLayoutNode() {
        try {
            ContentPage currentContentPage = ServicesRegistry.getInstance().getJahiaPageService().lookupContentPage(retrieveParamBean().getPageID(), false);

            String path = "/"+LAYOUTMANAGER_NODE_PATH + "/j:layout[@j:page='" + currentContentPage.getUUID() + "']";
            NodeIterator ni = findNodeIteratorByXpath(getRemoteJahiaUser(), path);
            if (ni != null && ni.hasNext()) {
                return new JCRLayoutNode((JCRNodeWrapper) ni.nextNode());
            }
        } catch (JahiaException e) {
            logger.error(e, e);
        }
        return null;
    }

    /**
     * Get layoutmanager node
     *
     * @return
     */
    private JCRNodeWrapper findLayoutmanagerNode() {
        return getJCRStoreService().getFileNode(LAYOUTMANAGER_NODE_PATH, getRemoteJahiaUser());
    }


    /**
     * Find nodes by xpath
     *
     * @param p
     * @param path
     * @return
     */
    private NodeIterator findNodeIteratorByXpath(Principal p, String path) {
        logger.debug("Find node by xpath[ " + path + " ]");
        if (p instanceof JahiaGroup) {
            logger.warn("Preference provider not implemented for JahiaGroup");
            return null;
        }
        try {
            QueryManager queryManager = getJCRStoreService().getQueryManager((JahiaUser) p);
            if (queryManager != null) {
                Query q = queryManager.createQuery(path.toString(), Query.XPATH);
                // execute query
                QueryResult queryResult = q.execute();

                // get node iterator
                NodeIterator ni = queryResult.getNodes();
                if (ni.hasNext()) {
                    logger.debug("Path[" + path + "] --> found [" + ni.getSize() + "] values.");
                    return ni;
                } else {
                    logger.debug("Path[" + path + "] --> empty result.");
                }
            }
        } catch (javax.jcr.PathNotFoundException e) {
            logger.debug("javax.jcr.PathNotFoundException: Path[" + path + "]");
        } catch (javax.jcr.ItemNotFoundException e) {
            logger.debug(e, e);
        } catch (javax.jcr.query.InvalidQueryException e) {
            logger.error("InvalidQueryException ---> [" + path + "] is not valid.", e);
        }
        catch (RepositoryException e) {
            logger.error(e, e);
        }
        return null;
    }

    /**
     * Get the JCR store service
     *
     * @return
     */
    private JCRStoreService getJCRStoreService() {
        return ServicesRegistry.getInstance().getJCRStoreService();
    }

}
