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
 package org.jahia.services.cluster;

import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.blocks.NotificationBus;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Implementation of clustering service that uses JGroups to
 * communicate across the cluster.
 * User: Serge Huber
 * Date: Jul 12, 2005
 * Time: 6:03:37 PM
 */
public class JGroupsClusterService extends ClusterService implements NotificationBus.Consumer {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (JGroupsClusterService.class);

    boolean connected = false;
    Set clusterListeners = new HashSet();
    private String channelProperties = JChannel.DEFAULT_PROTOCOL_STACK;
    private String channelGroupName = "JahiaCluster";
    private String serverId;

    NotificationBus notificationBus;

    public void handleNotification(Serializable serializable) {
        if (logger.isDebugEnabled()) {
            logger.debug("Handling notification : " + serializable.getClass().getName() + " on channel "+channelGroupName);
        }
        Iterator clusterListenerIter = clusterListeners.iterator();
        while (clusterListenerIter.hasNext()) {
            ClusterListener curListener = (ClusterListener) clusterListenerIter.next();
            curListener.messageReceived((ClusterMessage)serializable);
        }
    }

    public Serializable getCache() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void memberJoined(Address address) {
        logger.debug("memberJoined " + address);
        Iterator clusterListenerIter = clusterListeners.iterator();
        while (clusterListenerIter.hasNext()) {
            ClusterListener curListener = (ClusterListener) clusterListenerIter.next();
            curListener.memberJoined(address);
        }
    }

    public void memberLeft(Address address) {
        logger.debug("memberLeft "+ address);
        Iterator clusterListenerIter = clusterListeners.iterator();
        while (clusterListenerIter.hasNext()) {
            ClusterListener curListener = (ClusterListener) clusterListenerIter.next();
            curListener.memberLeft(address);
        }
    }

    public JGroupsClusterService() {
    }

    public void start() throws JahiaInitializationException {

        if (!isActivated()) {
            return;
        }

        logger.debug("Creating channel with stack properties [" + channelProperties + "]");

        try {
            if (channelProperties != null) {
                notificationBus = new NotificationBus(channelGroupName, channelProperties);
            } else {
                notificationBus = new NotificationBus(channelGroupName);                
            }
            // by settings the following option we say that we are not interested in our
            // own messages.
            notificationBus.getChannel().setOpt(Channel.LOCAL, Boolean.FALSE);
            notificationBus.getChannel().setOpt(Channel.AUTO_RECONNECT, Boolean.TRUE);
            notificationBus.getChannel().setOpt(Channel.AUTO_GETSTATE, Boolean.TRUE);
            notificationBus.getChannel().setOpt(Channel.GET_STATE_EVENTS, Boolean.TRUE);
            notificationBus.setConsumer(this);
            notificationBus.start();
            connected = true;
        } catch (Exception e) {
            throw new JahiaInitializationException("Error while starting notification bus", new ClusterException(e));
        }
    }

    public synchronized void stop ()
            throws JahiaException {
        if (!isActivated()) {
            return;
        }
        notificationBus.stop();
        clusterListeners.clear();
        connected = false;
    }

    public void sendMessage(ClusterMessage clusterMessage) {
        if (!isActivated()) {
            return;
        }
        if (!connected) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Sending notification object " + clusterMessage.getObject().getClass().getName() + " on channel "+ channelGroupName);
        }
        notificationBus.sendNotification(clusterMessage);
    }

    public void addListener(ClusterListener listener) {
        if (!isActivated()) {
            return;
        }
        clusterListeners.add(listener);
    }

    public void removeListener(ClusterListener listener) {
        if (!isActivated()) {
            return;
        }
        clusterListeners.remove(listener);
    }

    public String getChannelProperties() {
        return channelProperties;
    }

    public void setChannelProperties(String channelProperties) {
        this.channelProperties = channelProperties;
    }

    public String getChannelGroupName() {
        return channelGroupName;
    }

    public void setChannelGroupName(String channelGroupName) {
        this.channelGroupName = channelGroupName;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public int getNbNodes() {
        if (!isActivated()) {
            return 0;
        }
        if (!connected) {
            return 0;
        }
        // we are now in the case where it is activated and connected.
        return notificationBus.getMembership().size();
    }

}
