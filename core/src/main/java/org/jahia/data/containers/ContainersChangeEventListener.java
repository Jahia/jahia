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
//
//
//
// 28.05.2002 NK Creation



package org.jahia.data.containers;

import org.apache.log4j.Logger;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.events.JahiaEventListener;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.cluster.ClusterListener;
import org.jahia.services.cluster.ClusterMessage;
import org.jahia.services.cluster.ClusterService;
import org.jgroups.Address;

/**
 * Listener for containers and container list change events.
 * Created for container search caching purpose.
 * You must access this Singleton through JahiaListenersRegistry
 *
 * @see org.jahia.registries.JahiaListenersRegistry
 * @author Khue Nguyen <a href="mailto:khue@jahia.org">khue@jahia.org</a>
 */
public class ContainersChangeEventListener extends JahiaEventListener implements ClusterListener
{

    private static Logger logger =
            Logger.getLogger (ContainersChangeEventListener.class);

    public static final String CONTAINER_ADDED = "containerAdded";
    public static final String CONTAINER_UPDATED = "containerUpdated";
    public static final String CONTAINER_DELETED = "containerDeleted";
    public static final String CONTAINER_ACTIVATED = "containerActivated";
    public static final String CONTAINER_MARKED_FOR_DELETION = "containerMarkedForDeletion";
    
    // private static Cache cache = null;
    // Cache that holds containers update  status ( last modif time etc... )
    public static final String CONTAINER_UPDATE_DATE_CACHE = "ContainerUpdateDateCache";
    // Cache that holds containers update  status ( last modif time etc... )
    public static final String CONTAINERLIST_UPDATE_DATE_CACHE = "ContainerListUpdateDateCache";

    private long lastContainerUpdateTime = -1;
    private ClusterService clusterService;

    public ContainersChangeEventListener() throws JahiaException {
    }

    public ClusterService getClusterService() {
        return clusterService;
    }

    public void setClusterService(ClusterService clusterService) {
        this.clusterService = clusterService;
        clusterService.addListener(this);
    }//--------------------------------------------------------------------------
    /**
     * triggered when Jahia adds a container
     *
     * @param je the associated JahiaEvent
     */
    public void containerAdded( JahiaEvent je ) {
        logger.debug("Started");
        JahiaContainer theObject    = (JahiaContainer) je.getObject();
        try {
            ContentContainer contentContainer =
                ContentContainer.getContainer(theObject.getID());
            notifyChange(contentContainer, CONTAINER_ADDED);
        } catch ( Exception t ){
            logger.error("Error notify added container ",t);
        }
    }

    //--------------------------------------------------------------------------
    /**
     * triggered when Jahia updates a container
     *
     * @param je the associated JahiaEvent
     */
    public void containerUpdated( JahiaEvent je ) {
        logger.debug("Started");
        JahiaContainer theObject    = (JahiaContainer) je.getObject();
        try {
            ContentContainer contentContainer =
                ContentContainer.getContainer(theObject.getID());
            notifyChange(contentContainer, CONTAINER_UPDATED);
        } catch ( Exception t ){
            logger.error("Error notify updated container ",t);
        }
    }

    //--------------------------------------------------------------------------
    /**
     * triggered when Jahia adds a container
     *
     * @param je the associated JahiaEvent
     */
    public void containerDeleted( JahiaEvent je ) {
        logger.debug("Started");
        JahiaContainer theObject    = (JahiaContainer) je.getObject();
        try {
            ContentContainer contentContainer =
                ContentContainer.getContainer(theObject.getID());
            notifyChange(contentContainer, CONTAINER_DELETED);
        } catch ( Exception t ){
            logger.error("Error notify deleted container ",t);
        }
    }

    //--------------------------------------------------------------------------
    /**
     * set the last modifying time for the given container,
     * the container list containing the container is updated too.
     * Notify registered observers
     *
     * @param JahiaContainer the container.
     */
    public synchronized void notifyChange( ContentContainer container, String operation  ) {

        if ( container == null ){
            return;
        }

        // Set search time
        lastContainerUpdateTime = System.currentTimeMillis();

        ContainerChangeClusterMessage clusterMessage = new ContainerChangeClusterMessage(new Long(lastContainerUpdateTime));
        clusterService.sendMessage(clusterMessage);
    }


    //--------------------------------------------------------------------------
    /**
     * Returns the last modifying time for any container.
     *
     * @return long time, the last change time. -1 if information not available.
     */
    public long getContainerLastChangeTime() {
        return lastContainerUpdateTime;
    }


    public void messageReceived (ClusterMessage clusterMessage) {
        if (clusterMessage instanceof ContainerChangeClusterMessage) {
            Long timeFromCluster = ((ContainerChangeClusterMessage) clusterMessage).getChangeTime();
            lastContainerUpdateTime = timeFromCluster.longValue();
        }
    }

    public void memberJoined(Address address) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void memberLeft(Address address) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

} // end ContainersChangeEventListener
