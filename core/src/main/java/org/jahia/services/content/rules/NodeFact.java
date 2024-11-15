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
