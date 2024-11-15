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
package org.jahia.services.content;

import java.util.Collection;

import org.jahia.services.content.nodetypes.ExtendedNodeType;

/**
 * Provides for higher level publication and publication info retrieval operations.
 */
public interface ComplexPublicationService {

    /**
     * Get aggregated publication info about a JCR node.
     *
     * @param nodeIdentifier The UUID of the node that must exist in the EDIT workspace
     * @param language Publication language
     * @param subNodes Whether to take sub-nodes of the node into account when calculating the aggregated publication status
     * @param references Whether to take references into account when calculating the aggregated publication status
     * @param sourceSession Session representing the publication source workspace (the target workspace is always LIVE)
     * @return Aggregated publication info about the node
     */
    AggregatedPublicationInfo getAggregatedPublicationInfo(String nodeIdentifier, String language, boolean subNodes, boolean references, JCRSessionWrapper sourceSession);

    /**
     * Get full publication info about JCR nodes.
     *
     * @param nodeIdentifiers UUIDs of the nodes
     * @param languages Publication languages
     * @param allSubTree
     * @param sourceSession Session representing the publication source workspace (the target workspace is always LIVE)
     * @return A collection of full publication infos
     */
    Collection<FullPublicationInfo> getFullPublicationInfos(Collection<String> nodeIdentifiers, Collection<String> languages, boolean allSubTree, JCRSessionWrapper sourceSession);

    /**
     * Get full publication info about JCR nodes.
     *
     * @param nodeIdentifiers UUIDs of the nodes
     * @param languages Publication languages
     * @param allSubTree
     * @param sourceSession Session representing the publication source workspace (the target workspace is always LIVE)
     * @return A collection of full publication infos
     */
    Collection<FullPublicationInfo> getFullPublicationInfos(Collection<String> nodeIdentifiers, Collection<String> languages, boolean allSubTree, boolean includeRemoved, JCRSessionWrapper sourceSession);

    /**
     * Get full un-publication info about JCR nodes.
     *
     * @param nodeIdentifiers UUIDs of the nodes
     * @param languages Publication languages
     * @param allSubTree
     * @param session
     * @return A collection of full un-publication infos
     */
    Collection<FullPublicationInfo> getFullUnpublicationInfos(Collection<String> nodeIdentifiers, Collection<String> languages, boolean allSubTree, JCRSessionWrapper session);

    /**
     * Publish JCR nodes (along with their translation sub-nodes when appropriate) in certain languages.
     * Does not including sub tree
     * @param nodeIdentifiers UUIDs of the nodes
     * @param languages Publication languages
     * @param session
     */
    void publish(Collection<String> nodeIdentifiers, Collection<String> languages, JCRSessionWrapper session);

    /**
     * Publish JCR nodes (along with their translation sub-nodes when appropriate) in certain languages.
     *
     * @param nodeIdentifiers UUIDs of the nodes
     * @param languages Publication languages
     * @param includeAllSubTree Includes all nodes under the published node (included page nodes)
     * @param session
     */
    void publish(Collection<String> nodeIdentifiers, Collection<String> languages, JCRSessionWrapper session, Boolean includeAllSubTree);

    /**
     * Aggregated (un-)publication info about a JCR node.
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

        /**
         * @return JCR node UUID
         */
        String getNodeIdentifier();

        /**
         * @return JCR node path
         */
        String getNodePath();

        /**
         * @return JCR node title
         */
        String getNodeTitle();

        /**
         * @return JCR node type
         */
        ExtendedNodeType getNodeType();

        /**
         * @return Root publication node UUID
         */
        String getPublicationRootNodeIdentifier();

        /**
         * @return Root publication node path
         */
        String getPublicationRootNodePath();

        int getPublicationRootNodePathIndex();

        /**
         * @return Publication status of the node (see PublicationInfo status constants)
         */
        int getPublicationStatus();

        /**
         * @return Locked status of the node
         */
        boolean isLocked();

        /**
         * @return Work-in-progress status of the node
         */
        boolean isWorkInProgress();

        /**
         * @return Publication workflow title
         */
        String getWorkflowTitle();

        /**
         * @return Publication workflow definition name
         */
        String getWorkflowDefinition();

        /**
         * @return Publication workflow group name
         */
        String getWorkflowGroup();

        /**
         * @return Whether current user is allowed to publish the node omitting any workflows
         */
        boolean isAllowedToPublishWithoutWorkflow();

        /**
         * @return Publication language
         */
        String getLanguage();

        /**
         * @return The UUID of the translation node
         */
        String getTranslationNodeIdentifier();

        Collection<String> getDeletedTranslationNodeIdentifiers();

        /**
         * @return Whether the node is marked for deletion, while is not the root of the sub-tree of nodes to remove
         */
        boolean isNonRootMarkedForDeletion();

        /**
         * @return Whether the node can be published
         */
        boolean isPublishable();
    }
}
