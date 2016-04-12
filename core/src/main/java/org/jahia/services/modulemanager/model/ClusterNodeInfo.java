/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.modulemanager.model;

import java.io.Serializable;

/**
 * Data object that contains info about a cluster node. Also in a non-clustered environment represent the information about a standalone
 * server.
 * 
 * @author Sergiy Shyrkov
 */
public class ClusterNodeInfo implements Serializable {

    private static final long serialVersionUID = -9056316060892481092L;

    /**
     * Flag, indicating if the clustering is activated on the platform.
     */
    private boolean clusterActivated;

    /**
     * The cluster node identifier (jahia.node.properties).
     */
    private String id;

    /**
     * Is this a processing server node?
     */
    private boolean processingServer;

    /**
     * Initializes an instance of this class.
     */
    public ClusterNodeInfo() {
        super();
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param id
     *            the cluster node identifier (jahia.node.properties)
     * @param processingServer
     *            <code>true</code> if this is a processing node
     * @param clusterActivated
     *            <code>true</code> if the clustering is activated on the platform
     */
    public ClusterNodeInfo(String id, boolean processingServer, boolean clusterActivated) {
        super();
        this.id = id;
        this.processingServer = processingServer;
        this.clusterActivated = clusterActivated;
    }

    public String getId() {
        return id;
    }

    public boolean isClusterActivated() {
        return clusterActivated;
    }

    public boolean isProcessingServer() {
        return processingServer;
    }

    public void setClusterActivated(boolean clusterActivated) {
        this.clusterActivated = clusterActivated;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setProcessingServer(boolean processingServer) {
        this.processingServer = processingServer;
    }

}
