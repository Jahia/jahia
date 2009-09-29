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
package org.jahia.ajax.gwt.templates.components.layoutmanager.server;


import org.apache.log4j.Logger;
import org.apache.pluto.container.PortletWindow;
import org.jahia.ajax.gwt.commons.server.JahiaRemoteService;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.service.layoutmanager.LayoutmanagerService;
import org.jahia.ajax.gwt.client.data.layoutmanager.GWTJahiaLayoutItem;
import org.jahia.ajax.gwt.client.data.layoutmanager.GWTJahiaLayoutManagerConfig;
import org.jahia.ajax.gwt.helper.NavigationHelper;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRPortletNode;
import org.jahia.services.content.decorator.JCRReferenceNode;
import org.jahia.services.applications.ApplicationsManagerService;
import org.jahia.exceptions.JahiaException;
import org.jahia.data.applications.EntryPointInstance;
import org.jahia.data.applications.EntryPointDefinition;
import org.jahia.data.beans.portlets.PortletWindowBean;
import org.jahia.data.beans.portlets.PortletModeBean;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.portlet.PortletMode;
import java.util.*;

/**
 * User: jahia
 * Date: 19 mars 2008
 * Time: 17:24:54
 */
public class LayoutmanagerServiceImpl extends JahiaRemoteService implements LayoutmanagerService {
    private static transient final Logger logger = Logger.getLogger(LayoutmanagerServiceImpl.class);
    private static final String LAYOUTMANAGER_NODE_PATH = "/content/layoutmanager";

    private JCRSessionFactory sessionFactory;
    private NavigationHelper navigation;

    public void setNavigation(NavigationHelper navigation) {
        this.navigation = navigation;
    }


    /**
     * @param gwtJahiaLayoutItem draggableWidgetPreferences of the corresponding wiget
     */
    public String saveLayoutItem(GWTJahiaLayoutItem gwtJahiaLayoutItem) throws GWTJahiaServiceException {
        try {
            // key
            JCRReferenceNode layoutItemNode = (JCRReferenceNode) sessionFactory.getCurrentUserSession().getNodeByUUID(gwtJahiaLayoutItem.getUuid());

            JCRNodeWrapper referencedNode = sessionFactory.getCurrentUserSession().getNodeByUUID(gwtJahiaLayoutItem.getNode());
            if (referencedNode != null) {
                layoutItemNode.setProperty("j:columnIndex",gwtJahiaLayoutItem.getColumn());
                layoutItemNode.setProperty("j:rowIndex",gwtJahiaLayoutItem.getRow());
//                jcrLayoutItemNode.setStatus(gwtJahiaLayoutItem.getStatus());
                layoutItemNode.setNode(referencedNode);
                layoutItemNode.save();
            } else {
                logger.error("Portlet Instance with uuid[" + gwtJahiaLayoutItem.getNode() + "] not found.");
            }
            return layoutItemNode.getNode().getUUID();
        } catch (RepositoryException e) {
            logger.error(e, e);
            return null;
        }

    }


    /**
     * Update layoutnaode
     *
     * @param gwtJahiaLayoutItems
     * @throws RepositoryException
     */
    private void updateLayoutNode(List<GWTJahiaLayoutItem> gwtJahiaLayoutItems) throws RepositoryException {
        for (GWTJahiaLayoutItem gwtJahiaLayoutItem : gwtJahiaLayoutItems) {

            JCRNodeWrapper referencedNode = sessionFactory.getCurrentUserSession().getNodeByUUID(gwtJahiaLayoutItem.getNode());
            if (referencedNode != null) {
                // retrieve layout item node
                if (gwtJahiaLayoutItem.getUuid() != null) {
                    JCRReferenceNode layoutItemNode = (JCRReferenceNode) sessionFactory.getCurrentUserSession().getNodeByUUID(gwtJahiaLayoutItem.getUuid());
                    layoutItemNode.setProperty("j:columnIndex",gwtJahiaLayoutItem.getColumn());
                    layoutItemNode.setProperty("j:rowIndex",gwtJahiaLayoutItem.getRow());
//                    jcrLayoutItemNode.setStatus(gwtJahiaLayoutItem.getStatus());
                    layoutItemNode.setNode(referencedNode);
                    layoutItemNode.save();
                }
            } else {
                logger.error("Portlet Instance with uuid[" + gwtJahiaLayoutItem.getNode() + "] not found.");
            }
        }
    }




    /**
     * Save current configuration as default one
     *
     * @throws GWTJahiaServiceException
     */
    public void saveAsDefault(String containerUUID) throws GWTJahiaServiceException {
//        try {
//            JahiaPreference<JCRLayoutNode> userLayoutmanagerNode = getLayoutManagerJahiaPreferencesProvider().getJahiaPreference(getRemoteJahiaUser(), JahiaPreferencesQueryHelper.getLayoutmanagerSQL(jahiaPageContext.getPid()));
//
//            JCRNodeWrapper layoutmanagerNode = findLayoutmanagerNode();
//            if (layoutmanagerNode != null) {
//                JCRLayoutNode defaultLayoutNode = findDefaultLayoutNode();
//
//                // remove layout_{pid} and its children or create a new one
//                if (defaultLayoutNode != null) {
//                    List<JCRNodeWrapper> nodeWrappers = defaultLayoutNode.getChildren();
//                    for (JCRNodeWrapper nodeWrapper : nodeWrappers) {
//                        nodeWrapper.remove();
//                    }
//                } else {
//                    defaultLayoutNode = new JCRLayoutNode(layoutmanagerNode.addNode("j:layout", Constants.JAHIANT_LAYOUT));
//                }
//
//                // copy user config
//                if (userLayoutmanagerNode != null) {
//                    defaultLayoutNode.setPage(userLayoutmanagerNode.getNode().getPage());
//                    defaultLayoutNode.setNbColumns(userLayoutmanagerNode.getNode().getNbColumns());
//                    defaultLayoutNode.setLiveDraggable(userLayoutmanagerNode.getNode().isLiveDraggable());
//                    defaultLayoutNode.setLiveEditable(userLayoutmanagerNode.getNode().isLiveEditable());
//                }
//
//                // copy user layoutIems
//                List<GWTJahiaLayoutItem> gwtJahiaLayoutItems = getLayoutItems(jahiaPageContext);
//                for (GWTJahiaLayoutItem gwtJahiaLayoutItem : gwtJahiaLayoutItems) {
//                    JCRNodeWrapper portletNode = jcrStoreService.getNodeByUUID(gwtJahiaLayoutItem.getPortlet(), getRemoteJahiaUser());
//                    if (portletNode != null) {
//                        // retrieve layout item node
//                        if (gwtJahiaLayoutItem.getUuid() != null) {
//                            JCRNodeWrapper nodeWrapper = defaultLayoutNode.addLayoutItem(portletNode, gwtJahiaLayoutItem.getColumn(), gwtJahiaLayoutItem.getRow(), gwtJahiaLayoutItem.getStatus());
//                            JCRReferenceNode jcrLayoutItemNode = new JCRReferenceNode(nodeWrapper);
//                            jcrLayoutItemNode.setProperty("j:columnIndex",gwtJahiaLayoutItem.getColumn());
//                            jcrLayoutItemNode.setProperty("j:rowIndex",gwtJahiaLayoutItem.getRow());
////                            jcrLayoutItemNode.setStatus(gwtJahiaLayoutItem.getStatus());
//                            jcrLayoutItemNode.setNode(portletNode);
//                        }
//                    }
//                }
//
//                // save changes
//                layoutmanagerNode.save();
//            }
//        } catch (Exception e) {
//            logger.error("Unable to save default config due to", e);
//
//        }

    }

    public void restoreDefault(String containerUUID) throws GWTJahiaServiceException {
//        JahiaPreference<JCRLayoutNode> layoutmanagerNode = getLayoutManagerJahiaPreferencesProvider().getJahiaPreference(getRemoteJahiaUser(), JahiaPreferencesQueryHelper.getLayoutmanagerSQL(jahiaPageContext.getPid()));
//        if (layoutmanagerNode != null) {
//            getLayoutManagerJahiaPreferencesProvider().deleteJahiaPreference(layoutmanagerNode);
//        }
    }

    /**
     * @param gwtJahiaLayoutItems
     * @throws GWTJahiaServiceException
     */
    public void saveLayoutItems(List<GWTJahiaLayoutItem> gwtJahiaLayoutItems) throws GWTJahiaServiceException {
        try {
//            ContentPage currentContentPage = ServicesRegistry.getInstance().getJahiaPageService().lookupContentPage(jahiaPageContext.getPid(), false);


//            // get layout manager node
//            JahiaPreference<JCRLayoutNode> layoutmanagerJahiaPreference = getLayoutManagerJahiaPreferencesProvider().getJahiaPreference(getRemoteJahiaUser(), JahiaPreferencesQueryHelper.getLayoutmanagerSQL(jahiaPageContext.getPid()));
//            if (layoutmanagerJahiaPreference == null) {
//                layoutmanagerJahiaPreference = createPartialLayoutmanagerPreference();
//                // update page
//                layoutmanagerJahiaPreference.getNode().setPage(currentContentPage.getUUID());
//            }
//
//            // update a layoutNode
//            JCRLayoutNode layoutNode = layoutmanagerJahiaPreference.getNode();

            // update layout node
            updateLayoutNode(gwtJahiaLayoutItems);

        } catch (Exception e) {
            logger.error("Error while saving layoumanager pref.", e);

        }

    }

    /**
     * @param gwtJahiaLayoutItem
     * @throws GWTJahiaServiceException
     */
    public void addLayoutItem(String containerUUID, GWTJahiaLayoutItem gwtJahiaLayoutItem) throws GWTJahiaServiceException {
        try {

            JCRNodeWrapper node = sessionFactory.getCurrentUserSession().getNodeByUUID(gwtJahiaLayoutItem.getNode());
            JCRNodeWrapper container = sessionFactory.getCurrentUserSession().getNodeByUUID(containerUUID);

            addLayoutItem(container, node, gwtJahiaLayoutItem.getColumn(), gwtJahiaLayoutItem.getRow(), gwtJahiaLayoutItem.getStatus());
        } catch (Exception e) {
            logger.error("Error while saving layoumanager pref.", e);
        }

    }

    /**
     * Add layout manager item
     *
     * @param layoutItem
     * @param column
     * @param row
     * @param status
     * @throws JahiaException
     * @throws RepositoryException
     */
    private JCRReferenceNode addLayoutItem(JCRNodeWrapper container, JCRNodeWrapper layoutItem, int column, int row, String status) throws JahiaException, RepositoryException {
        JCRReferenceNode layoutItemNode = null;
        if (layoutItem != null) {
//            ContentPage currentContentPage = ServicesRegistry.getInstance().getJahiaPageService().lookupContentPage(pid, false);
//
//            // get layout manager node
//            JahiaPreference<JCRLayoutNode> layoutmanagerJahiaPreference = getLayoutManagerJahiaPreferencesProvider().getJahiaPreference(getRemoteJahiaUser(), JahiaPreferencesQueryHelper.getLayoutmanagerSQL(pid));
//            if (layoutmanagerJahiaPreference == null) {
//                layoutmanagerJahiaPreference = createPartialLayoutmanagerPreference();
//                // update page
//                layoutmanagerJahiaPreference.getNode().setPage(currentContentPage.getUUID());
//            }
//
//            //add layout item
//            JCRLayoutNode container = layoutmanagerJahiaPreference.getNode();

            JCRReferenceNode jcrNodeWrapper = (JCRReferenceNode) container.addNode("j:item"+System.currentTimeMillis(), "jnt:nodeReference");

            jcrNodeWrapper.setNode(layoutItem);

            jcrNodeWrapper.addMixin("jmix:positionable");
            jcrNodeWrapper.setProperty("j:columnIndex",column);
            jcrNodeWrapper.setProperty("j:rowIndex",row);
//            jcrLayoutItemNode.setStatus(status);
            container.save();

            // save pref
//            getLayoutManagerJahiaPreferencesProvider().setJahiaPreference(layoutmanagerJahiaPreference);
        }
        return layoutItemNode;
    }



    /**
     * Delete a widget from the layout manager
     *
     * @param layoutItem
     */
    public void removeLayoutItem(GWTJahiaLayoutItem layoutItem) throws GWTJahiaServiceException {
        //get layout manager prefererences
        try {
            Node layoutItemNode = sessionFactory.getCurrentUserSession().getNodeByUUID(layoutItem.getUuid());
            if (layoutItemNode != null) {
                Node containerNode = layoutItemNode.getParent();
                layoutItemNode.remove();
                containerNode.save();
            }
        } catch (Exception e) {
            logger.error(e, e);
        }

    }

    /**
     * Save layout manager config
     *
     * @param gwtLayoutManagerConfig
     */
    public void saveLayoutmanagerConfig(GWTJahiaLayoutManagerConfig gwtLayoutManagerConfig) throws GWTJahiaServiceException {
//        try {
////             create layout[@pid] node
//            JahiaPreference<JCRLayoutNode> layoutmanagerJahiaPreference = getLayoutManagerJahiaPreferencesProvider().getJahiaPreference(getRemoteJahiaUser(), JahiaPreferencesQueryHelper.getLayoutmanagerSQL(getContainerUUID.getPid()));
//            if (layoutmanagerJahiaPreference == null) {
//                layoutmanagerJahiaPreference = createPartialLayoutmanagerPreference();
//                ContentPage currentContentPage = ServicesRegistry.getInstance().getJahiaPageService().lookupContentPage(getContainerUUID.getPid(), false);
//                layoutmanagerJahiaPreference.getNode().setPage(currentContentPage.getUUID());
//            }
//            JCRLayoutNode node = layoutmanagerJahiaPreference.getNode();
//            node.setNbColumns(gwtLayoutManagerConfig.getNbColumns());
//            node.setLiveDraggable(gwtLayoutManagerConfig.isLiveDraggable());
//            node.setLiveDraggable(gwtLayoutManagerConfig.isLiveQuickbarVisible());
//            getLayoutManagerJahiaPreferencesProvider().setJahiaPreference(layoutmanagerJahiaPreference);
//        } catch (Exception e) {
//            logger.error("Can't save layout manager config.", e);
//        }
    }

    /**
     * Get the layout manager config
     *
     * @return
     */
    public GWTJahiaLayoutManagerConfig getLayoutmanagerConfig(String containerUuid) {
        GWTJahiaLayoutManagerConfig gwtLayoutManagerConfig = new GWTJahiaLayoutManagerConfig(containerUuid);
        gwtLayoutManagerConfig.setNbColumns(3);
        gwtLayoutManagerConfig.setLiveDraggable(true);
        gwtLayoutManagerConfig.setLiveQuickbarVisible(true);
//        try {
//            JahiaPreference<JCRLayoutNode> layoutmanagerJahiaPreference = getLayoutManagerJahiaPreferencesProvider().getJahiaPreference(getRemoteJahiaUser(), JahiaPreferencesQueryHelper.getLayoutmanagerSQL(retrieveParamBean().getPageID()));
//
//            JCRLayoutNode node = null;
//            if (layoutmanagerJahiaPreference != null) {
//                node = layoutmanagerJahiaPreference.getNode();
//            } else {
//                // copy default config
//                node = copyDefaultConfig();
//
//                // copy default layout node
//                getDefaultLayoutItems(retrieveParamBean().getPageID());
//            }
//
//
//            if (node != null) {
//                gwtLayoutManagerConfig.setNbColumns((int) node.getNbColumns());
//                gwtLayoutManagerConfig.setLiveDraggable(node.isLiveDraggable());
//                gwtLayoutManagerConfig.setLiveQuickbarVisible(node.isLiveEditable());
//            }
//        } catch (Exception e) {
//            logger.error(e, e);
//        }
        return gwtLayoutManagerConfig;
    }

    /**
     * Copy default config
     *
     * @return
     */
//    private JCRLayoutNode copyDefaultConfig() {
//        JahiaPreference<JCRLayoutNode> layoutmanagerJahiaPreference = createPartialLayoutmanagerPreference();
//        if (layoutmanagerJahiaPreference != null) {
//            JCRLayoutNode node = findDefaultLayoutNode();
//
//            try {
//                if (node != null) {
//                    layoutmanagerJahiaPreference.getNode().setPage(node.getPage());
//                    layoutmanagerJahiaPreference.getNode().setLiveDraggable(node.isLiveDraggable());
//                    layoutmanagerJahiaPreference.getNode().setLiveEditable(node.isLiveEditable());
//                    layoutmanagerJahiaPreference.getNode().setNbColumns(node.getNbColumns());
//                    layoutmanagerJahiaPreference.getNode().setPage(node.getPage());
//
//                    layoutmanagerJahiaPreference.getNode().setPage(node.getPage());
//                    layoutmanagerJahiaPreference.getNode().setLiveDraggable(node.isLiveDraggable());
//                    layoutmanagerJahiaPreference.getNode().setLiveEditable(node.isLiveEditable());
//                    layoutmanagerJahiaPreference.getNode().setNbColumns(node.getNbColumns());
//                    layoutmanagerJahiaPreference.getNode().setPage(node.getPage());
//
//                    // save pref
//                    getLayoutManagerJahiaPreferencesProvider().setJahiaPreference(layoutmanagerJahiaPreference);
//                }
//
//
//            } catch (Exception e) {
//                logger.error(e, e);
//            }
//            return layoutmanagerJahiaPreference.getNode();
//        }
//        return null;
//    }


    /**
     * Create a layoutmanager key
     *
     * @return
     */
//    private JahiaPreference<JCRLayoutNode> createPartialLayoutmanagerPreference() {
//        return getLayoutManagerJahiaPreferencesProvider().createJahiaPreferenceNode(getRemoteJahiaUser());
//    }


    /**
     * @param containerUUID jahia parameter as pid, current mode,...
     * @return Map <widget Id, widget preferences>
     */

    public List<GWTJahiaLayoutItem> getLayoutItems(String containerUUID) throws GWTJahiaServiceException {
        List<GWTJahiaLayoutItem> layoutItems = new ArrayList<GWTJahiaLayoutItem>();

        try {

            JCRNodeWrapper containerNode = sessionFactory.getCurrentUserSession().getNodeByUUID(containerUUID);

//            JahiaPreference<JCRLayoutNode> layoutmanagerNode = getLayoutManagerJahiaPreferencesProvider().getJahiaPreference(getRemoteJahiaUser(), JahiaPreferencesQueryHelper.getLayoutmanagerSQL(getContainerUUID.getPid()));
//
//            if (layoutmanagerNode != null) {
//                logger.debug("Layoutmanager config found for user [" + getRemoteUser() + "]");
//                return fillGWTLayoutItems(getContainerUUID, layoutmanagerNode.getNode());
//            } else {
//                logger.debug("Layoutmanager for user [" + getRemoteUser() + "] not found --> load default one");
                return getDefaultLayoutItems(containerNode);
//            }
        } catch (RepositoryException e) {
            logger.error(e, e);
        }

        return layoutItems;
    }

    /**
     * load default config
     *
     * @throws RepositoryException
     */
    private List<GWTJahiaLayoutItem> getDefaultLayoutItems(JCRNodeWrapper containerNode) throws RepositoryException {


        List<GWTJahiaLayoutItem> gwtJahiaLayoutItems = new ArrayList<GWTJahiaLayoutItem>();

        List<JCRNodeWrapper> nodeWrappers = containerNode.getChildren();
        if (nodeWrappers != null) {
            for (JCRNodeWrapper n : nodeWrappers) {
                if (n instanceof JCRReferenceNode && n.isNodeType("jmix:positionable")) {
                    gwtJahiaLayoutItems.add(fillGWTLayoutItem((JCRReferenceNode) n));
                }
            }
        }

        return gwtJahiaLayoutItems;
    }

//    /**
//     * Create corresponding layout item list
//     *
//     * @param containerNode
//     * @param jcrLayoutNode
//     * @return
//     * @throws RepositoryException
//     */
//    private List<GWTJahiaLayoutItem> fillGWTLayoutItems(GWTJahiaNode containerNode, JCRLayoutNode jcrLayoutNode) throws RepositoryException {
//        // remove layout_{pid} and its children
//        List<GWTJahiaLayoutItem> gwtJahiaLayoutItems = new ArrayList<GWTJahiaLayoutItem>();
//        if (jcrLayoutNode != null) {
//            List<JCRReferenceNode> jcrLayoutItemNodes = jcrLayoutNode.getLayoutItems();
//            for (JCRReferenceNode layoutItemNode : jcrLayoutItemNodes) {
//                // create preferences values
//                gwtJahiaLayoutItems.add(fillGWTLayoutItem(layoutItemNode));
//            }
//        } else {
//            logger.debug("There is no default config for page[pid=" + containerNode.getPid() + "]");
//        }
//        return gwtJahiaLayoutItems;
//    }

    /**
     * Create a GWTJahiaDraggableWidget from a layout item
     *
     * @param jcrLayoutItemNode
     * @return
     * @throws RepositoryException
     */
    private GWTJahiaLayoutItem fillGWTLayoutItem(JCRReferenceNode jcrLayoutItemNode) throws RepositoryException {
        // get column
        int column = (int) jcrLayoutItemNode.getProperty("j:columnIndex").getLong();

        // get row
        int row = (int) jcrLayoutItemNode.getProperty("j:rowIndex").getLong();

        // get status
        String status = "open";

        JCRNodeWrapper referencedNode = (JCRNodeWrapper) jcrLayoutItemNode.getNode();
        String uuid = referencedNode.getUUID();

        // get modes urls
        if (referencedNode.getUUID() != null) {
            String viewModeUrl = null;
            String editModeUrl = null;
            String helpModeUrl = null;
            int currentPortletMode = 0;
            if (referencedNode instanceof JCRPortletNode) {
                try {
                    JCRPortletNode portletNode = (JCRPortletNode) referencedNode;
                    EntryPointDefinition entryPointDefinition = portletNode.getEntryPointDefinition();
                    EntryPointInstance entryPointInstance = ServicesRegistry.getInstance().getApplicationsManagerService().getEntryPointInstance(uuid);
                    PortletWindow window = ServicesRegistry.getInstance().getApplicationsManagerService().getPortletWindow(entryPointInstance, uuid, retrieveParamBean());
                    PortletWindowBean portletWindowBean = new PortletWindowBean(retrieveParamBean(), window);
                    portletWindowBean.setEntryPointInstance(entryPointInstance);
                    portletWindowBean.setEntryPointDefinition(entryPointDefinition);
                    List<PortletModeBean> portletModeBeans = portletWindowBean.getPortletModeBeans();
                    for (PortletModeBean portletModeBean : portletModeBeans) {
                        String portletModeName = portletModeBean.getName();
                        if (portletModeName != null) {
                            if (portletModeName.equalsIgnoreCase("view")) {
                                viewModeUrl = retrieveParamBean().composeUrl(portletModeBean.getURL());
                            } else if (portletModeName.equalsIgnoreCase("edit")) {
                                editModeUrl = retrieveParamBean().composeUrl(portletModeBean.getURL());
                            } else if (portletModeName.equalsIgnoreCase("help")) {
                                helpModeUrl = retrieveParamBean().composeUrl(portletModeBean.getURL());
                            } else {
                                logger.warn("Unknown portlet mode [" + portletModeBean.getName() + "]");
                            }
                        }
                    }

                    // current mode
                    PortletModeBean currentPortletModeBean = portletWindowBean.getCurrentPortletModeBean();
                    if (currentPortletModeBean != null) {
                        String portletModeName = currentPortletModeBean.getName();
                        if (portletModeName != null) {
                            if (portletModeName.equalsIgnoreCase(PortletMode.VIEW.toString())) {
                                currentPortletMode = GWTJahiaLayoutItem.MODE_VIEW;
                            } else if (portletModeName.equalsIgnoreCase(PortletMode.EDIT.toString())) {
                                currentPortletMode = GWTJahiaLayoutItem.MODE_EDIT;
                            } else if (portletModeName.equalsIgnoreCase(PortletMode.HELP.toString())) {
                                currentPortletMode = GWTJahiaLayoutItem.MODE_HELP;
                            } else {
                                logger.warn("Unknown portlet mode [" + currentPortletModeBean.getName() + "]");
                            }
                        }
                    }


                } catch (JahiaException e) {
                    logger.error(e, e);
                }
            }
            return new GWTJahiaLayoutItem(jcrLayoutItemNode.getUUID(), navigation.getGWTJahiaNode(referencedNode,true), viewModeUrl, editModeUrl, helpModeUrl, column, row, status, currentPortletMode);
        } else {
            return null;
        }

    }


    /**
     * Create a Portlet Instance
     *
     * @param portletNode
     */
    public void createPortletInstanceWindow(GWTJahiaNode portletNode) {
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

    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    //    /**
//     * @return the layout manager preferences
//     */
//    private JahiaPreferencesProvider<JCRLayoutNode> getLayoutManagerJahiaPreferencesProvider() {
//        try {
//            return servicesRegistry.getJahiaPreferencesService().getPreferencesProviderByClass(JCRLayoutNode.class);
//        } catch (JahiaPreferenceProviderException e) {
//            logger.error(e, e);
//        }
//        return null;
//    }

//    /*
//    * Get default layout node of the current page
//    * */
//    private JCRLayoutNode findDefaultLayoutNode() {
//        try {
//            ContentPage currentContentPage = ServicesRegistry.getInstance().getJahiaPageService().lookupContentPage(retrieveParamBean().getPageID(), false);
//
//            String path = "/" + LAYOUTMANAGER_NODE_PATH + "/j:layout[@j:page='" + currentContentPage.getUUID() + "']";
//            NodeIterator ni = findNodeIteratorByXpath(getRemoteJahiaUser(), path);
//            if (ni != null && ni.hasNext()) {
//                return new JCRLayoutNode((JCRNodeWrapper) ni.nextNode());
//            }
//        } catch (JahiaException e) {
//            logger.error(e, e);
//        }
//        return null;
//    }

//    /**
//     * Find nodes by xpath
//     *
//     * @param p
//     * @param path
//     * @return
//     */
//    private NodeIterator findNodeIteratorByXpath(Principal p, String path) {
//        logger.debug("Find node by xpath[ " + path + " ]");
//        if (p instanceof JahiaGroup) {
//            logger.warn("Preference provider not implemented for JahiaGroup");
//            return null;
//        }
//        try {
//            QueryManager queryManager = getJCRStoreService().getQueryManager((JahiaUser) p);
//            if (queryManager != null) {
//                Query q = queryManager.createQuery(path.toString(), Query.XPATH);
//                // execute query
//                QueryResult queryResult = q.execute();
//
//                // get node iterator
//                NodeIterator ni = queryResult.getNodes();
//                if (ni.hasNext()) {
//                    logger.debug("Path[" + path + "] --> found [" + ni.getSize() + "] values.");
//                    return ni;
//                } else {
//                    logger.debug("Path[" + path + "] --> empty result.");
//                }
//            }
//        } catch (javax.jcr.PathNotFoundException e) {
//            logger.debug("javax.jcr.PathNotFoundException: Path[" + path + "]");
//        } catch (javax.jcr.ItemNotFoundException e) {
//            logger.debug(e, e);
//        } catch (javax.jcr.query.InvalidQueryException e) {
//            logger.error("InvalidQueryException ---> [" + path + "] is not valid.", e);
//        }
//        catch (RepositoryException e) {
//            logger.error(e, e);
//        }
//        return null;
//    }
}
