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
package org.jahia.services.content.decorator;

import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.slf4j.Logger;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Node;

/**
 * TODO Comment me
 *
 * @author toto
 */
public class JCRReferenceNode extends JCRNodeDecorator {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(JCRReferenceNode.class);

    public JCRReferenceNode(JCRNodeWrapper node) {
        super(node);
    }

    public Node getNode() throws RepositoryException {
        if (hasProperty(Constants.NODE)) {
            return getProperty(Constants.NODE).getNode();
        }
        return null;
    }

    public void setNode(JCRNodeWrapper node) throws RepositoryException {
        setProperty(Constants.NODE, node);
    }

    @Override
    public String getDisplayableName() {
        String name = super.getDisplayableName();
        try {
            if (getName().equals(name) && getNode() != null && !this.getIdentifier().equals(getNode().getIdentifier())) {
                name = ((JCRNodeWrapper) getNode()).getDisplayableName();
            }
        } catch (RepositoryException e) {
            logger.warn("JCRReferenceNode : error while trying to display reference " + this.getPath());
        }
        return name;
    }
    
    public Node getContextualizedNode() throws RepositoryException {
        if (hasProperty(Constants.NODE)) {
            try {
                return getProperty(Constants.NODE).getContextualizedNode();
            } catch (ItemNotFoundException e) {
                return null;
            }
        }
        return null;
    }
}
