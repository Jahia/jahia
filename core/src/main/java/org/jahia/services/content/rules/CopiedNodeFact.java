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
     * @throws RepositoryException
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
