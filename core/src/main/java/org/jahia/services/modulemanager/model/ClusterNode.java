/**
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

import org.apache.jackrabbit.ocm.mapper.impl.annotation.Collection;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;

import java.util.LinkedHashMap;
import java.util.TreeMap;

/**
 * This object represents a cluster node. 
 * 
 * @author Sergiy Shyrkov
 */
@Node(jcrType = "jnt:moduleManagementNode", discriminator = false)
public class ClusterNode extends BasePersistentObject {

    private static final long serialVersionUID = 4606202580861227782L;

    @Collection(jcrName = "bundles", proxy = true)
    private TreeMap<String, NodeBundle> bundles = new TreeMap<>();

    @Collection(jcrName = "operations", proxy = true)
    private LinkedHashMap<String, NodeOperation> operations = new LinkedHashMap<>();

    @Field(jcrName = "j:processingServer")
    private boolean processingServer = false;

    @Field(jcrName = "j:started")
    private boolean started = true;

    @Field(jcrName = "j:type")
    private String type;

    /**
     * Initializes an instance of this class.
     */
    public ClusterNode() {
        super();
    }

    /**
     * Initializes an instance of this class.
     *
     * @param name the cluster node identifier
     * @param processingServer <code>true</code> for processing node; <code>false</code> otherwise
     */
    public ClusterNode(String name, boolean processingServer) {
        super(name);
        this.processingServer = processingServer;
    }

    public TreeMap<String, NodeBundle> getBundles() {
        return bundles;
    }

    public LinkedHashMap<String, NodeOperation> getOperations() {
        return operations;
    }

    public String getType() {
        return type;
    }

    public boolean isProcessingServer() {
        return processingServer;
    }

    public boolean isStarted() {
        return started;
    }

    public void setBundles(TreeMap<String, NodeBundle> bundles) {
        this.bundles = bundles;
    }

    public void setOperations(LinkedHashMap<String, NodeOperation> operations) {
        this.operations = operations;
    }

    public void setProcessingServer(boolean processingServer) {
        this.processingServer = processingServer;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public void setType(String type) {
        this.type = type;
    }
}
