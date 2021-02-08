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

import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.ClusterBroadcastFilter;

/**
 * This is attached to a Broadcaster you want to have in a clustered situation.
 * <p/>
 * Each clustered broadcaster should have its own instance of a JGroupsFilter and
 * likewise, each JGroupsFilter should have a circular reference back to that broadcaster.
 * <p/>
 * Therefore, when the JGroupsFilter is added to the Broadcaster config,
 * remember to make the reference circular by calling JGroupsFilter.setBroadcaster(bc)
 * or simply using the constructor with the Broadcaster to begin with.
 * <p/>
 * Uri is not currently used because the 'cluster name' is driven from the
 * JGroupsChannel itself.  I suppose it could be used to 'look up' the JGroupsChannel
 * if there is a registry of them implemented somehow, but its easier to just
 * inject the JGroupsChannel into the filter.
 *
 * @author Jean-Francois Arcand (original version)
 * @author westraj
 */
public class JGroupsFilter implements ClusterBroadcastFilter {

    private JGroupsChannelImpl jChannel;
    private Broadcaster bc;

    /**
     * Constructor
     *
     * @param jChannel the channel implementation to filter
     */
    public JGroupsFilter(JGroupsChannelImpl jChannel) {
        // no default broadcaster is created. Must set a specific one now with setBroadcaster()
        this.jChannel = jChannel;
    }

    /**
     * Constructor with broadcaster
     *
     * @param jChannel the channel implementation to filter
     * @param bc       the broadcaster to filter
     */
    public JGroupsFilter(JGroupsChannelImpl jChannel, Broadcaster bc) {
        this(jChannel);
        this.bc = bc;
        addBroadcasterToChannel(this.bc);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.atmosphere.cpr.BroadcastFilterLifecycle#destroy()
     */
    @Override
    public void destroy() {
        jChannel.removeBroadcaster(bc);
        this.bc = null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.atmosphere.cpr.BroadcastFilterLifecycle#init()
     */
    @Override
    public void init(AtmosphereConfig config) {
        // We want to avoid side effect of custom configuration.
    }

    /**
     * Every time a message gets broadcasted, make sure we update the cluster.
     *
     * @param message the message to broadcast.
     * @return The same message.
     */
    @Override
    public BroadcastAction filter(String broadcasterId, Object originalMessage, Object message) {
        if (bc != null) {
            this.jChannel.send(this.bc.getID(), message);
        }

        return new BroadcastAction(message);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.atmosphere.cpr.ClusterBroadcastFilter#getBroadcaster()
     */
    @Override
    public Broadcaster getBroadcaster() {
        return bc;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.atmosphere.cpr.ClusterBroadcastFilter#setBroadcaster(org.atmosphere
     * .cpr.Broadcaster)
     */
    @Override
    public void setBroadcaster(Broadcaster bc) {
        this.bc = bc;

        addBroadcasterToChannel(this.bc);
    }

    private void addBroadcasterToChannel(Broadcaster bc) {
        // register this filter's broadcaster with the JGroupsChannel
        if (bc != null) {
            jChannel.addBroadcaster(bc);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.atmosphere.cpr.ClusterBroadcastFilter#setUri(java.lang.String)
     */
    @Override
    public void setUri(String clusterUri) {
        // NO OPS
    }
}
