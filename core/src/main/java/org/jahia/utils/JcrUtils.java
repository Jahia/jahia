/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
