/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.ajax.gwt.commons.server;

/*
 * Copyright 2014 Jean-Francois Arcand
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import org.apache.jackrabbit.core.cluster.ClusterNode;
import org.apache.jackrabbit.core.journal.JournalException;
import org.apache.jackrabbit.core.journal.Record;
import org.apache.jackrabbit.core.journal.RecordConsumer;
import org.atmosphere.cpr.Broadcaster;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.services.content.impl.jackrabbit.SpringJackrabbitRepository;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * JGroupsChannel establishes a connection to a
 * JGroups cluster.  It sends/receives over that and forwards
 * the received messages to the appropriate Broadcaster on its
 * node.
 * <p>
 * Best practice would have only 1 of these per Atmosphere application.
 * Each JGroupsFilter instance has a reference to the
 * singleton JGroupsChannel object and registers its broadcaster via
 * the addBroadcaster() method.
 *
 * @author westraj
 */
public class JGroupsChannelImpl extends ReceiverAdapter implements JGroupsChannel, RecordConsumer {
    private static final Logger logger = LoggerFactory.getLogger(JGroupsChannelImpl.class);

    /**
     * JGroups  JChannel object
     */
    final JChannel jChannel;

    /**
     * JChannel cluster name
     */
    private final String clusterName;

    /**
     * registers all the Broadcasters that are filtered via a JGroupsFilter
     */
    private final Map<String, Broadcaster> broadcasters = new HashMap<>();

    /**
     * Holds original messages (not BroadcastMessage) received over a cluster broadcast
     */
    private final ConcurrentLinkedQueue<Object> receivedMessages = new ConcurrentLinkedQueue<>();
    private final ClusterNode clusterNode;
    private long revision;
    private Queue<BroadcastMessage> bcMessages = new ConcurrentLinkedQueue<>();

    /**
     * Constructor
     *
     * @param jChannel    unconnected JGroups  JChannel object
     * @param clusterName name of the group to connect the JChannel to
     */
    public JGroupsChannelImpl(JChannel jChannel, String clusterName) {
        if (jChannel.isConnected()) {
            throw new IllegalArgumentException("JChannel already connected");
        }

        this.jChannel = jChannel;
        this.clusterName = clusterName;
        this.clusterNode = SpringJackrabbitRepository.getInstance().getClusterNode();
        try {
            clusterNode.getJournal().register(this);
            this.setRevision(clusterNode.getRevision());
        } catch (JournalException e) {
            logger.debug(e.getMessage(), e);
        }
    }

    /**
     * Connect to the cluster
     *
     * @throws Exception if an exception happen during initialization
     */
    public void init() throws Exception {
        logger.info(
                "Starting Atmosphere JGroups Clustering support with group name {}",
                this.clusterName);
        try {
            this.jChannel.setReceiver(this);
            this.jChannel.connect(clusterName);
            this.jChannel.setDiscardOwnMessages(true);
        } catch (Exception e) {
            logger.warn("Failed to connect to cluster: {}", this.clusterName);
            throw e;
        }

    }

    /**
     * Shutdown the cluster.
     */
    public void destroy() {
        try {
            Util.shutdown(jChannel);
        } catch (Exception t) {
            Util.close(jChannel);
            logger.warn("failed to properly shutdown jgroups channel, closing abnormally", t);
        }
        receivedMessages.clear();
        broadcasters.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void receive(final Message jgroupMessage) {
        final Object payload = jgroupMessage.getObject();

        if (payload == null) {
            return;
        }
        //To avoid issue be sure to test that Jahia has started before trying to broadcast messages locally.
        if (JahiaContextLoaderListener.isContextInitialized() && BroadcastMessage.class.isAssignableFrom(payload.getClass())) {
            BroadcastMessage broadcastMsg = (BroadcastMessage) payload;
            if (clusterNode.getRevision() >= broadcastMsg.getRevision()) {
                broadcastMessage(broadcastMsg);
            } else {
                bcMessages.add(broadcastMsg);
            }
        }


    }

    private void broadcastMessage(BroadcastMessage broadcastMsg) {
        // original message from the sending node's JGroupsFilter.filter() method
        Object origMessage = broadcastMsg.getMessage();
        // add original message to list to check re-broadcast logic in send()
        receivedMessages.offer(origMessage);
        String topicId = broadcastMsg.getTopic();
        if (broadcasters.containsKey(topicId)) {
            Broadcaster bc = broadcasters.get(topicId);
            try {
                bc.broadcast(origMessage).get();
            } catch (Exception ex) {
                logger.error("Failed to broadcast message received over the JGroups cluster {}", this.clusterName, ex);
            }
        }
    }

    /**
     * Called from a ClusterBroadcastFilter filter() method
     * to send the message over to other Atmosphere cluster nodes
     *
     * @param topic   the channel topic
     * @param message the message to send
     */
    @Override
    public void send(String topic, Object message) {
        // Avoid re-broadcasting to cluster by checking if the message was
        // one already received from another cluster node
        if (jChannel.isConnected() && !receivedMessages.remove(message)) {
            try {
                BroadcastMessage broadcastMsg = new BroadcastMessage(topic, message, clusterNode.getRevision());
                Message jgroupMsg = new Message(null, null, broadcastMsg);
                jChannel.send(jgroupMsg);
            } catch (Exception e) {
                logger.warn("Failed to send message {}", message, e);
            }
        }
    }

    /**
     * Adds/replaces the broadcaster to the JGroupsChannel
     *
     * @param broadcaster add this broadcaster to the channel
     */
    public void addBroadcaster(Broadcaster broadcaster) {
        this.broadcasters.put(broadcaster.getID(), broadcaster);
    }

    /**
     * Removes the broadcaster from the JGroupsChannel
     *
     * @param broadcaster remove this broadcaster from the channel
     */
    public void removeBroadcaster(Broadcaster broadcaster) {
        this.broadcasters.remove(broadcaster.getID());
    }

    @Override
    public String getId() {
        return "ATMOSPHERE_BROADCAST";
    }

    @Override
    public long getRevision() {
        return revision;
    }

    @Override
    public void consume(Record notUsedRecord) {
        logger.error("This consumer can not handle records.");
    }

    @Override
    public void setRevision(long revision) {
        logger.debug("Broadcasting messages previous to revision: {}", revision);
        BroadcastMessage peek = bcMessages.peek();
        while (peek != null && peek.getRevision() <= revision) {
            bcMessages.remove();
            broadcastMessage(peek);
            peek = bcMessages.peek();
        }
        if (logger.isDebugEnabled()) {
            if (bcMessages.isEmpty()) {
                logger.debug("No more message to broadcast");
            } else {
                logger.debug("Still some messages to broadcast. Next revision to broadcast is {}",
                        bcMessages.peek().getRevision());
            }
        }
        this.revision = revision;
    }
}
