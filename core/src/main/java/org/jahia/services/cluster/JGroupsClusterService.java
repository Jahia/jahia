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
 package org.jahia.services.cluster;

import org.apache.log4j.Logger;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.blocks.NotificationBus;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of clustering service that uses JGroups to
 * communicate across the cluster.
 * User: Serge Huber
 * Date: Jul 12, 2005
 * Time: 6:03:37 PM
 */
public class JGroupsClusterService extends ClusterService implements NotificationBus.Consumer {

    private static final Logger logger = Logger.getLogger (JGroupsClusterService.class);

    boolean connected = false;
    Set<ClusterListener> clusterListeners = new HashSet<ClusterListener>();
    private String channelProperties;
    private String channelPropertiesTcp = JChannel.DEFAULT_PROTOCOL_STACK;
	private String channelPropertiesUdp = JChannel.DEFAULT_PROTOCOL_STACK;
    private String channelProtocol = "tcp";
    private String channelGroupName = "JahiaCluster";
    private String serverId;

    NotificationBus notificationBus;

    public void handleNotification(Serializable serializable) {
        if (logger.isDebugEnabled()) {
            logger.debug("Handling notification : " + serializable.getClass().getName() + " on channel "+channelGroupName);
        }
        for (ClusterListener curListener : clusterListeners) {
            curListener.messageReceived((ClusterMessage)serializable);
        }
    }

    public Serializable getCache() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void memberJoined(Address address) {
        logger.debug("memberJoined " + address);
        for (ClusterListener curListener : clusterListeners) {
            curListener.memberJoined(address);
        }
    }

    public void memberLeft(Address address) {
        logger.debug("memberLeft "+ address);
        for (ClusterListener curListener : clusterListeners) {
            curListener.memberLeft(address);
        }
    }

    public JGroupsClusterService() {
    }

    public void start() throws JahiaInitializationException {

        if (!isActivated()) {
            return;
        }
        String props = channelProperties;
        if (props == null) {
        	props = channelProtocol != null && "udp".equalsIgnoreCase(channelProtocol) ? channelPropertiesUdp : channelPropertiesTcp;        	
        }
        logger.debug("Creating channel with stack properties [" + props + "]");

        try {
            if (props != null) {
                notificationBus = new NotificationBus(channelGroupName, props);
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

    public void setChannelPropertiesTcp(String channelPropertiesTcp) {
        this.channelPropertiesTcp = channelPropertiesTcp;
    }

    public void setChannelProperties(String channelProperties) {
        this.channelProperties = channelProperties;
    }

    public void setChannelPropertiesUdp(String channelPropertiesUdp) {
        this.channelPropertiesUdp = channelPropertiesUdp;
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

	public void setChannelProtocol(String channelProtocol) {
    	this.channelProtocol = channelProtocol;
    }

}
