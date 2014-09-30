/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
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
public class DeletedNodeFact implements NodeFact {
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

    public JCRSessionWrapper getSession() {
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
}
