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
package org.jahia.utils.security;

import javax.jcr.Item;
import javax.jcr.NamespaceException;
import javax.jcr.RepositoryException;

/**
 * This interface is used to manage paths in AccessManagerUtils, since it have to deal with both JCR and external API
 *
 * JCR Access controls use {@link org.apache.jackrabbit.spi.Path} internally
 * External API Access controls use {@link String} internally
 *
 * The Wrapper is here to provide functions on top of both representations
 * @author jkevan
 */
public interface PathWrapper {
    /**
     * get the original object behind the wrapper
     * @return The original path object
     */
    Object getInnerObject();

    /**
     * get the length of the current path in the wrapper
     * @return the length of the path
     */
    int getLength();

    /**
     * Check if the current path in the wrapper is the root path
     * @return true if the current path is root
     */
    boolean isRoot();

    /**
     * Get the string representation of the current path in the wrapper
     * @return the string representation of the path
     */
    String getPathStr();

    /**
     * Get the node name for the current path in the wrapper
     * @return the node name
     * @throws NamespaceException
     */
    String getNodeName() throws NamespaceException;

    /**
     * Check if the item exist for the current path in the wrapper
     * @return true if the item exist
     * @throws RepositoryException in case of JCR-related errors
     */
    boolean itemExist() throws RepositoryException;

    /**
     * Get the item for the current path in the wrapper
     * @return the item
     * @throws RepositoryException in case of JCR-related errors
     */
    Item getItem() throws RepositoryException;

    /**
     * Get the direct parent path wrapped
     * @return the parent path wrapper
     * @throws RepositoryException in case of JCR-related errors
     */
    PathWrapper getAncestor() throws RepositoryException;

    /**
     * Get a new wrapped path
     * @param path the path to be wrapped
     * @return the wrapped path
     * @throws RepositoryException in case of JCR-related errors
     */
    PathWrapper getNewPathWrapper(String path) throws RepositoryException;
}
