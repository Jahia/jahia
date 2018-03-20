package org.jahia.services.content;

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
}
