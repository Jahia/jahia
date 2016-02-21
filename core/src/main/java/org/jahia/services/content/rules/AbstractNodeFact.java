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
package org.jahia.services.content.rules;

import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;

import javax.jcr.ItemNotFoundException;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractNodeFact implements NodeFact {
    
    protected static List<String> recurseOnTypes(List<String> nodeTypeNames) {
        if (nodeTypeNames == null || nodeTypeNames.size() == 0) {
            return Collections.emptyList();
        }
        List<String> res = new LinkedList<String>();
        NodeTypeRegistry ntRegistry = NodeTypeRegistry.getInstance();
        for (String n : nodeTypeNames) {
            try {
                recurseOnTypes(res, ntRegistry.getNodeType(n));
            } catch (NoSuchNodeTypeException e) {
                // ignore missing node type
            }
        }

        return res;
    }
    
    protected static void recurseOnTypes(List<String> res, NodeType... nt) {
        for (NodeType nodeType : nt) {
            if (!res.contains(nodeType.getName()))
                res.add(nodeType.getName());
            recurseOnTypes(res, nodeType.getSupertypes());
        }
    }

    protected static List<String> recurseOnTypes(NodeType primaryNodeType, NodeType... mixins) {
        List<String> res = new LinkedList<String>();
        if (primaryNodeType != null) {
            recurseOnTypes(res, primaryNodeType);
        }
        if (mixins != null) {
            recurseOnTypes(res, mixins);
        }

        return res;
    }

    protected JCRNodeWrapper node;
    protected String workspace;

    protected String operationType;
    protected List<String> installedModules;

    public AbstractNodeFact(JCRNodeWrapper node) throws RepositoryException {
        this.node = node;
        if (node != null) {
            workspace = node.getSession().getWorkspace().getName();
        }
    }

    public String getIdentifier() throws RepositoryException {
        return node.getIdentifier();
    }

    public AddedNodeFact getParent() throws RepositoryException {
        return new AddedNodeFact(node.getParent());
    }

    public String getPath() throws RepositoryException {
        return node.getPath();
    }

    public String getName() throws RepositoryException {
        return node.getName();
    }

    public String getWorkspace() throws RepositoryException {
        return workspace;
    }

    public String getLanguage() throws RepositoryException {
        return node.getLanguage();
    }

    @Override
    public JCRSessionWrapper getSession() throws RepositoryException {
        return node.getSession();
    }

    /**
     * Returns the current JCR operation type.
     *
     * @return the current JCR operation type
     * @throws javax.jcr.RepositoryException in case of a repository access error
     * @since Jahia 6.6
     */
    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    /**
     * returns the list of current installed modules on this site.
     * @return
     */
    public List<String> getInstalledModules() {
        return installedModules;
    }

    public void setInstalledModules(List<String> installedModules) {
        this.installedModules = installedModules;
    }

    public AddedNodeFact getContent() throws RepositoryException {
        if (node.hasNode(Constants.JCR_CONTENT)) {
            AddedNodeFact nodeFact = new AddedNodeFact(node.getNode(Constants.JCR_CONTENT));
            nodeFact.setOperationType(this.getOperationType());
            return nodeFact;
        }
        return null;
    }

    public String getMimeType() throws RepositoryException {
        if (node.hasNode(Constants.JCR_CONTENT)) {
            return node.getNode(Constants.JCR_CONTENT).getProperty(Constants.JCR_MIMETYPE).getString();
        }
        return null;
    }

    public List<AddedNodeFact> getChildNodes() throws RepositoryException {
        List<AddedNodeFact> results = new ArrayList<AddedNodeFact>();
        NodeIterator it = node.getNodes();
        while (it.hasNext()) {
            JCRNodeWrapper n = (JCRNodeWrapper) it.nextNode();
            AddedNodeFact nodeFact = new AddedNodeFact(n);
            nodeFact.setOperationType(this.getOperationType());
            results.add(nodeFact);
        }
        return results;
    }

    public List<ChangedPropertyFact> getProperties() throws RepositoryException {
        List<ChangedPropertyFact> results = new ArrayList<ChangedPropertyFact>();
        PropertyIterator it = node.getProperties();
        while (it.hasNext()) {
            JCRPropertyWrapper p = (JCRPropertyWrapper) it.nextProperty();
            results.add(new ChangedPropertyFact(new AddedNodeFact(node),p));
        }
        return results;
    }

    public ChangedPropertyFact getProperty(String propertyName) throws RepositoryException {
        return new ChangedPropertyFact(new AddedNodeFact(node),node.getProperty(propertyName));
    }

    public List<String> getTypes() throws RepositoryException {
        return recurseOnTypes(node.getPrimaryNodeType(), node.getMixinNodeTypes());
    }

    public AddedNodeFact getAncestor(String type) throws RepositoryException {
        AddedNodeFact ancestor = new AddedNodeFact(node);
        try {
            while ((ancestor = ancestor.getParent()) != null) {
                if (ancestor.getNode().isNodeType(type)) {
                    return ancestor;
                }
            }
        } catch (ItemNotFoundException e) {
        }
        return null;
    }

    public JCRNodeWrapper getNode() {
        return node;
    }

    public AddedNodeFact getNode(String relPath) throws RepositoryException {
        AddedNodeFact nodeFact = new AddedNodeFact(node.getNode(relPath));
        nodeFact.setOperationType(this.getOperationType());
        return nodeFact;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractNodeFact that = (AbstractNodeFact) o;

        if (node != null ? !node.equals(that.node) : that.node != null) return false;
        if (operationType != null ? !operationType.equals(that.operationType) : that.operationType != null)
            return false;
        if (workspace != null ? !workspace.equals(that.workspace) : that.workspace != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = node != null ? node.hashCode() : 0;
        result = 31 * result + (workspace != null ? workspace.hashCode() : 0);
        result = 31 * result + (operationType != null ? operationType.hashCode() : 0);
        return result;
    }
}
