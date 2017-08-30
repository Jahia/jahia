/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.commons.lang.StringUtils;
import org.drools.core.spi.KnowledgeHelper;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;

import javax.jcr.RepositoryException;

import java.util.List;

/**
 * Represents a deleted node fact.
 *
 * @author toto
 */
public class DeletedNodeFact implements NodeFact, ModifiedNodeFact {
    private String path;
    private String identifier;
    private JCRSessionWrapper session;
    private String name;
    private AddedNodeFact parent;

    private String workspace;
    private List<String> types;
    private List<String> resolvedTypes;
    private String operationType;

    public DeletedNodeFact(AddedNodeFact nodeWrapper, KnowledgeHelper drools) throws RepositoryException {
        path = nodeWrapper.getPath();
        JCRNodeWrapper node = nodeWrapper.getNode();
        workspace = node.getSession().getWorkspace().getName();

        // collect types
        resolvedTypes = AbstractNodeFact.recurseOnTypes(node.getPrimaryNodeType(), node.getMixinNodeTypes());

        node.remove();
        drools.retract(nodeWrapper);

        // should also retract properties and subnodes
    }

    public DeletedNodeFact(AddedNodeFact parent, String path) throws RepositoryException {
        this.parent = parent;
        this.path = path;
        this.name = StringUtils.substringAfterLast(path, "/");
        workspace = parent.getNode().getSession().getWorkspace().getName();
    }

    public String toString() {
        return "deleted " + path;
    }

    public String getPath() {
        return path;
    }

    public AddedNodeFact getParent() {
        return parent;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public JCRSessionWrapper getSession() throws RepositoryException {
        return session;
    }

    public void setSession(JCRSessionWrapper session) {
        this.session = session;
    }

    public String getWorkspace() throws RepositoryException {
        return workspace;
    }

    public String getLanguage() throws RepositoryException {
        return parent.getLanguage();
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

    public String getName() {
        return name;
    }

    public List<String> getTypes() throws RepositoryException {
        if (resolvedTypes == null && types != null) {
            resolvedTypes = AbstractNodeFact.recurseOnTypes(types);
        }

        return resolvedTypes;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    @Override
    public String getNodeIdentifier() throws RepositoryException {
        return getIdentifier();
    }

    @Override
    public String getNodeType() throws RepositoryException {
        return types.get(0);
    }
}
