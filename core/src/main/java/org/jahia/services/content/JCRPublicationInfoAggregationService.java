/*
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
package org.jahia.services.content;

import java.util.Collection;

/**
 * Provides for aggregated publication info about a JCR node.
 */
public interface JCRPublicationInfoAggregationService {

    /**
     * Get aggregated publication info about a JCR node.
     *
     * @param nodeIdentifier The UUID of the node that must exist in the EDIT workspace
     * @param language Publication language
     * @param subNodes Whether to take sub-nodes of the node into account when calculating the aggregated publication status
     * @param references Whether to take references into account when calculating the aggregated publication status
     * @return Aggregated publication info about the node
     */
    AggregatedPublicationInfo getAggregatedPublicationInfo(String nodeIdentifier, String language, boolean subNodes, boolean references);

    Collection<FullPublicationInfo> getFullPublicationInfos(Collection<String> nodeIdentifiers, Collection<String> language, boolean allSubTree);

    /**
     * Aggregated publication info about a JCR node.
     */
    interface AggregatedPublicationInfo {

        /**
         * @return Aggregated publication status of the node (see PublicationInfo status constants)
         */
        int getPublicationStatus();

        /**
         * @return Aggregated locked status of the node
         */
        boolean isLocked();

        /**
         * @return Aggregated work-in-progress status of the node
         */
        boolean isWorkInProgress();

        /**
         * @return Whether current user is allowed to publish the node omitting any workflows
         */
        boolean isAllowedToPublishWithoutWorkflow();

        /**
         * @return Whether the node is marked for deletion, while is not the root of the sub-tree of nodes to remove
         */
        boolean isNonRootMarkedForDeletion();
    }

    interface FullPublicationInfo {

        String getNodeIdentifier();
        String getNodePath();
        int getPublicationStatus();
        boolean isPublishable();
        boolean isLocked();
        boolean isWorkInProgress();
        String getWorkflowDefinition();
        String getWorkflowGroup();
        boolean isAllowedToPublishWithoutWorkflow();
        String getTranslationNodeIdentifier();
        String getDeletedTranslationNodeIdentifier();
        boolean isNonRootMarkedForDeletion();
    }
}
