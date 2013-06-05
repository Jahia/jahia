/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content.rules;

import org.jahia.api.Constants;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import java.util.ArrayList;
import java.util.List;

/**
 * Node facade that is used in rules.
 * User: toto
 * Date: 20 déc. 2007
 * Time: 11:53:45
 */
public class AddedNodeFact extends AbstractNodeFact implements Updateable {
    private static Logger logger = LoggerFactory.getLogger(AddedNodeFact.class);

    private AddedNodeFact parentNode;
    private String parentNodePath;
    private String name;
    private String type;

    public AddedNodeFact(JCRNodeWrapper node) throws RepositoryException {
        super(node);
    }

    public AddedNodeFact(AddedNodeFact parentNodeWrapper, String name, String type, KieSession drools) throws RepositoryException {
        super(null);
        this.parentNode = parentNodeWrapper;
        workspace = parentNode.getNode().getSession().getWorkspace().getName();

        JCRNodeWrapper node = (JCRNodeWrapper) parentNode.getNode();
        parentNodePath = node.getPath();
        this.name = name;
        if (type == null) {
            ExtendedNodeDefinition end = ((ExtendedNodeType) node.getPrimaryNodeType()).getChildNodeDefinitionsAsMap().get(name);
            NodeType nodetype = end.getRequiredPrimaryTypes()[0];
            type = nodetype.getName();
        }

        this.type = type;

        if (node.isLocked()) {
            logger.debug("Node is locked, delay property update to later");
            @SuppressWarnings("unchecked")
            List<Updateable> list = (List<Updateable>) drools.getGlobal("delayedUpdates");
            list.add(this);
        } else {
            if (node.isVersioned()) {
                node.checkout();
            }
            this.node = node.addNode(JCRContentUtils.findAvailableNodeName(node, name), type);
        }
    }

    public void doUpdate(JCRSessionWrapper s, List<Updateable> delayedUpdates) throws RepositoryException {
        try {
            JCRNodeWrapper node = s.getNode(parentNodePath);

            if (node.isLocked()) {
                logger.debug("Node is still locked, delay subnode creation to later");
                delayedUpdates.add(this);
            } else {
                node.addNode(name, type);
            }
        } catch (PathNotFoundException e) {
            logger.warn("Node does not exist " + parentNodePath);
        }
    }

    public String getPath() throws RepositoryException {
        if (node != null) {
            return node.getPath();
        } else if (parentNodePath != null && name != null) {
            return parentNodePath + "/" + name;
        }
        return null;
    }

    public String getName() throws RepositoryException {
        if (node != null) {
            return node.getName();
        } else if (name != null) {
            return name;
        }
        return null;
    }

    public List<ChangedPropertyFact> getProperties() throws RepositoryException {
        List<ChangedPropertyFact> results = new ArrayList<ChangedPropertyFact>();
        PropertyIterator it = node.getProperties();
        while (it.hasNext()) {
            JCRPropertyWrapper p = (JCRPropertyWrapper) it.nextProperty();
            results.add(new ChangedPropertyFact(this, p));
        }
        return results;
    }

    public ChangedPropertyFact getProperty(String propertyName) throws RepositoryException {
        return new ChangedPropertyFact(this, node.getProperty(propertyName));
    }

    public long getNumberOfChildren() throws RepositoryException {
        return node.getNodes().getSize();
    }

    public void addType(String type, KieSession drools) throws RepositoryException {
        if (node.isNodeType(type)) {
            return;
        }
        node.checkout();
        node.addMixin(type);
        drools.insert(new ChangedPropertyFact(this, node.getProperty(Constants.JCR_MIXINTYPES)));
        node.getSession().save();
        //        drools.update(this);
    }

    public void removeType(String type, KieSession drools) throws RepositoryException {
        node.checkout();
        node.removeMixin(type);
        try {
            JCRPropertyWrapper property = node.getProperty(Constants.JCR_MIXINTYPES);
            drools.insert(new ChangedPropertyFact(this, property));
        } catch (PathNotFoundException e) {
            drools.insert(new DeletedNodeFact(this, Constants.JCR_MIXINTYPES));
        }
        node.getSession().save();
        //        drools.update(this);
    }

    public AddedNodeFact getAncestor(String type) throws RepositoryException {
        AddedNodeFact ancestor = this;
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

    public String toString() {
        return node.getPath();
    }

    @Override
    public boolean equals(Object o) {
        if (logger.isDebugEnabled()) {
            logger.debug("Checking if " + this.toString() + " is equal to " + o.toString());
        }
        if (this == o) return true;
        if (!(o instanceof AddedNodeFact)) return false;

        AddedNodeFact that = (AddedNodeFact) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (node != null ? !node.equals(that.node) : that.node != null) return false;
        if (parentNode != null ? !parentNode.equals(that.parentNode) : that.parentNode != null) return false;
        if (parentNodePath != null ? !parentNodePath.equals(that.parentNodePath) : that.parentNodePath != null)
            return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        if (logger.isDebugEnabled()) {
            logger.debug("Requesting hashcode for AddedNodeFact " + this.toString());
        }
        int result = parentNode != null ? parentNode.hashCode() : 0;
        result = 31 * result + (parentNodePath != null ? parentNodePath.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (node != null ? node.hashCode() : 0);
        return result;
    }

    public String getIdentifier() throws RepositoryException {
        return node != null ? node.getIdentifier() : null;
    }

}
