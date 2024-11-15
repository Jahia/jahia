/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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

    @Override
    public String toString() {
        return "deleted " + path;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public AddedNodeFact getParent() {
        return parent;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public JCRSessionWrapper getSession() throws RepositoryException {
        return session;
    }

    public void setSession(JCRSessionWrapper session) {
        this.session = session;
    }

    @Override
    public String getWorkspace() throws RepositoryException {
        return workspace;
    }

    @Override
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
    @Override
    public String getOperationType() {
        return operationType;
    }

    @Override
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
