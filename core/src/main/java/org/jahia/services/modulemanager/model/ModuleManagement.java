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
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;

import java.util.LinkedHashMap;
import java.util.TreeMap;

/**
 * Represents the root of the JCR tree structure for module management, including list of available bundles, list of operations, list of
 * cluster nodes with their corresponding structure (node bundles and operations).
 * 
 * @author Sergiy Shyrkov
 */
@Node(jcrType = "jnt:moduleManagement", discriminator = false)
public class ModuleManagement extends BasePersistentObject {

    private static final long serialVersionUID = 3721980381995804955L;

    @Collection(jcrName = "bundles", proxy = true)
    private TreeMap<String, Bundle> bundles = new TreeMap<>();

    @Collection(jcrName = "nodes", proxy = true)
    private LinkedHashMap<String, ClusterNode> nodes = new LinkedHashMap<>();

    @Collection(jcrName = "operations", proxy = true)
    private LinkedHashMap<String, Operation> operations = new LinkedHashMap<>();

    /**
     * Initializes an instance of this class.
     */
    public ModuleManagement() {
        super();
    }

    /**
     * Initializes an instance of this class.
     *
     * @param path
     *            the persistent path
     */
    public ModuleManagement(String path) {
        this();
        setPath(path);
    }

    public TreeMap<String, Bundle> getBundles() {
        return bundles;
    }

    public LinkedHashMap<String, ClusterNode> getNodes() {
        return nodes;
    }

    public LinkedHashMap<String, Operation> getOperations() {
        return operations;
    }

    public void setBundles(TreeMap<String, Bundle> bundles) {
        this.bundles = bundles;
    }

    public void setNodes(LinkedHashMap<String, ClusterNode> nodes) {
        this.nodes = nodes;
    }

    public void setOperations(LinkedHashMap<String, Operation> operations) {
        this.operations = operations;
    }
}
