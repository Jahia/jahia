package org.jahia.services.content;

public interface JCRPublicationInfoAggregationService {

    AggregatedPublicationInfo getAggregatedPublicationInfo(String nodeIdentifier, String language, boolean includeSubNodes, boolean includeReferences);

    interface AggregatedPublicationInfo {

        int getPublicationStatus();
        boolean isLocked();
        boolean isWorkInProgress();
        boolean isAllowedToPublishWithoutWorkflow();
        boolean isNonRootMarkedForDeletion();
    }
}
