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
package org.jahia.services.content.rules;

import javax.jcr.RepositoryException;

import org.jahia.services.content.JCRNodeWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Working memory fact that represents a copied node
 *
 * @author Sergiy Shyrkov
 */
public class CopiedNodeFact extends AddedNodeFact {

    private static Logger logger = LoggerFactory.getLogger(CopiedNodeFact.class);

    private String originalPath;

    private String sourceUuid;

    private boolean top;

    /**
     * Initializes an instance of this class.
     *
     * @param node
     *            the current JCR node object
     * @param sourceUuid
     *            the UUID of the source node
     * @param top
     *            <code>true</code> in case this was the original top (root) object of the copy operation
     * @throws RepositoryException in case of JCR-related errors
     */
    public CopiedNodeFact(JCRNodeWrapper node, String sourceUuid, boolean top)
            throws RepositoryException {
        super(node);
        this.sourceUuid = sourceUuid;
        this.top = top;
    }

    public String getOriginalPath() {
        if (originalPath == null && node != null && sourceUuid != null) {
            try {
                originalPath = node.getSession().getNodeByIdentifier(sourceUuid).getPath();
            } catch (RepositoryException e) {
                logger.warn(e.getMessage(), e);
            }
        }

        return originalPath;
    }

    public boolean isTop() {
        return top;
    }

    @Override
    public String toString() {
        return "copied " + node.getPath();
    }

}
