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
     * @throws RepositoryException in case of JCR-related errors
     */
    void afterTagAction(JCRNodeWrapper node) throws RepositoryException;

    /**
     * will be call if a tag action throw a RepositoryException
     * @param node the node in error
     * @param e the exception
     * @throws RepositoryException in case of JCR-related errors
     */
    void onError(JCRNodeWrapper node, RepositoryException e) throws RepositoryException;

    /**
     * will be call at the end of all the operations
     * @return
     * @throws RepositoryException in case of JCR-related errors
     */
    T end() throws RepositoryException;
}
