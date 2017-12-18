/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.utils;

import javax.jcr.RepositoryException;

import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;

/**
 * JCR utilities.
 */
public class JcrUtils {

    /**
     * Name of the node intended for storing temporary data.
     */
    public static final String TEMP_NODE_NAME = "tmp";

    private JcrUtils() {
    }

    /**
     * Get node by its parent node, name and type; create the node if it does not exist yet.
     *
     * @param parent Parent node
     * @param name Name of the node
     * @param type Type of the node
     * @return The node
     * @throws IllegalStateException In case the node already exists, but has different type
     */
    public static JCRNodeWrapper getNodeCreateIfNeeded(JCRNodeWrapper parent, String name, String type) throws IllegalStateException {
        try {
            if (parent.hasNode(name)) {
                JCRNodeWrapper node = parent.getNode(name);
                if (!node.getPrimaryNodeTypeName().equals(type)) {
                    throw new IllegalStateException(String.format("Unexpected type of node %s: %s", node.getPath(), node.getPrimaryNodeTypeName()));
                }
                return node;
            }
            parent.addNode(name, type);
            parent.getSession().save();
            return parent.getNode(name);
        } catch (RepositoryException e) {
            throw new JahiaRuntimeException(e);
        }
    }

    /**
     * Get the node intended for storing temporary data; create the node if it does not exist yet.
     *
     * @param session JCR session
     * @return The node
     * @throws IllegalStateException In case the node already exists, but has unexpected type
     */
    public static JCRNodeWrapper getTempNodeCreateIfNeeded(JCRSessionWrapper session) throws IllegalStateException {
        JCRNodeWrapper root;
        try {
            root = session.getRootNode();
        } catch (RepositoryException e) {
            throw new JahiaRuntimeException(e);
        }
        return getNodeCreateIfNeeded(root, TEMP_NODE_NAME, Constants.NT_UNSTRUCTURED);
    }
}
