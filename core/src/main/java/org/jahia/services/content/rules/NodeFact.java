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
package org.jahia.services.content.rules;

import org.jahia.services.content.JCRSessionWrapper;

import javax.jcr.RepositoryException;

/**
 * Common interface for node facts in rules engine.
 *
 * @author Sergiy Shyrkov
 */
public interface NodeFact {

    /**
     * Returns the UUID of the corresponding node.
     *
     * @return the UUID of the corresponding nodeo
     * @throws RepositoryException
     *             in case of a repository access error
     */
    String getIdentifier() throws RepositoryException;

    /**
     * Returns the parent node fact.
     *
     * @return the parent node fact
     * @throws RepositoryException
     *             in case of a repository access error
     */
    AddedNodeFact getParent() throws RepositoryException;

    /**
     * Returns the corresponding path of the node.
     *
     * @return the corresponding path of the node
     * @throws RepositoryException
     *             in case of a repository access error
     */
    String getPath() throws RepositoryException;

    /**
     * Returns the current JCR workspace name.
     *
     * @return the current JCR workspace name
     * @throws RepositoryException
     *             in case of a repository access error
     * @since Jahia 6.6
     */
    String getWorkspace() throws RepositoryException;

    /**
     * Returns the current language.
     *
     * @return the current language
     * @throws RepositoryException
     *             in case of a repository access error
     * @since Jahia 6.6
     */
    String getLanguage() throws RepositoryException;

    /**
     * Returns the current JCR operation type.
     *
     * @return the current JCR operation type
     * @since Jahia 6.6
     */
    String getOperationType();

    void setOperationType(String operationType);

    /**
     * Returns the session that execute the rule
     *
     * @return the session that execute the rule
     * @throws RepositoryException in case of JCR-related errors
     */
    JCRSessionWrapper getSession() throws RepositoryException;
}
