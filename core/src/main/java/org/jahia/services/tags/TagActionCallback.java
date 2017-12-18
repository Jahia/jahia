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
package org.jahia.services.tags;

import org.jahia.services.content.JCRNodeWrapper;

import javax.jcr.RepositoryException;

/**
 * Callback used by tagging service when bench actions are made like for the renameTagUnderPath
 * exemple: used for manually flush the cache for the node after renaming a tag on it for example using afterTagAction
 * exemple: used for collect nodes in error using the onError and display custom error messages
 * exemple: used for return collected information using the end()
 * @author kevan
 */
public interface TagActionCallback<T> {
    /**
     * will be execute after each tag action (after a tag rename on a node, after a tag delete on a node)
     * @param node the node concern by the current tag action
     * @throws RepositoryException
     */
    void afterTagAction(JCRNodeWrapper node) throws RepositoryException;

    /**
     * will be call if a tag action throw a RepositoryException
     * @param node the node in error
     * @param e the exception
     * @throws RepositoryException
     */
    void onError(JCRNodeWrapper node, RepositoryException e) throws RepositoryException;

    /**
     * will be call at the end of all the operations
     * @return
     * @throws RepositoryException
     */
    T end() throws RepositoryException;
}
