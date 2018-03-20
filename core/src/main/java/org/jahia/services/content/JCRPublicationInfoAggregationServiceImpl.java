package org.jahia.services.content;

import java.util.Collections;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaRuntimeException;

public class JCRPublicationInfoAggregationServiceImpl implements JCRPublicationInfoAggregationService {

    private JCRSessionFactory sessionFactory;
    private JCRPublicationService publicationService;

    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void setPublicationService(JCRPublicationService publicationService) {
        this.publicationService = publicationService;
    }

    @Override
    public AggregatedPublicationInfo getAggregatedPublicationInfo(String nodeIdentifier, String language, boolean includeSubNodes, boolean includeReferences) {

        try {

            JCRSessionWrapper session = sessionFactory.getCurrentUserSession();
            JCRNodeWrapper node = session.getNodeByIdentifier(nodeIdentifier);

            PublicationInfo publicationInfo = publicationService.getPublicationInfo(nodeIdentifier, Collections.singleton(language), includeReferences, includeSubNodes, false, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE).get(0);

            if (!includeSubNodes) {
                // We don't include sub-nodes, but we still need the translation node to get correct status.
                String translationNodeName = "j:translation_" + language;
                if (node.hasNode(translationNodeName)) {
                    JCRNodeWrapper translationNode = node.getNode(translationNodeName);
                    PublicationInfo translationInfo = publicationService.getPublicationInfo(translationNode.getIdentifier(), Collections.singleton(language), includeReferences, false, false, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE).get(0);
                    publicationInfo.getRoot().addChild(translationInfo.getRoot());
                }
            }

            AggregatedPublicationInfoImpl result = new AggregatedPublicationInfoImpl(publicationInfo.getRoot().getStatus());

            String translationNodeRelPath = (publicationInfo.getRoot().getChildren().size() > 0 ? ("/j:translation_" + language) : null);
            for (PublicationInfoNode childNode : publicationInfo.getRoot().getChildren()) {
                if (childNode.getPath().contains(translationNodeRelPath)) {
                    if (childNode.getStatus() > result.getPublicationStatus()) {
                        result.setPublicationStatus(childNode.getStatus());
                    }
                    if (result.getPublicationStatus() == PublicationInfo.UNPUBLISHED && childNode.getStatus() != PublicationInfo.UNPUBLISHED) {
                        result.setPublicationStatus(childNode.getStatus());
                    }
                    if (childNode.isLocked()) {
                        result.setLocked(true);
                    }
                    if (childNode.isWorkInProgress()) {
                        result.setWorkInProgress(true);
                    }
                }
            }

            result.setAllowedToPublishWithoutWorkflow(node.hasPermission("publish"));
            result.setNonRootMarkedForDeletion(result.getPublicationStatus() == PublicationInfo.MARKED_FOR_DELETION && !node.isNodeType(Constants.JAHIAMIX_MARKED_FOR_DELETION_ROOT));

            if (result.getPublicationStatus() == PublicationInfo.PUBLISHED) {
                // Check if any of the descendant nodes or references are modified or unpublished.
                Set<Integer> allStatuses = publicationInfo.getTreeStatus(language);
                boolean modified = !allStatuses.isEmpty() && Collections.max(allStatuses) > PublicationInfo.PUBLISHED;
                if (!modified) {
                    for (PublicationInfo refInfo : publicationInfo.getAllReferences()) {
                        allStatuses = refInfo.getTreeStatus(language);
                        if (!allStatuses.isEmpty() && Collections.max(allStatuses) > PublicationInfo.PUBLISHED) {
                            modified = true;
                            break;
                        }
                    }
                }
                if (modified) {
                    result.setPublicationStatus(PublicationInfo.MODIFIED);
                }
            }

            return result;
        } catch (RepositoryException e) {
            throw new JahiaRuntimeException(e);
        }
    }

    private static class AggregatedPublicationInfoImpl implements AggregatedPublicationInfo {

        private int publicationStatus;
        private boolean locked;
        private boolean workInProgress;
        private boolean allowedToPublishWithoutWorkflow;
        private boolean nonRootMarkedForDeletion;

        public AggregatedPublicationInfoImpl(int publicationStatus) {
            this.publicationStatus = publicationStatus;
        }

        @Override
        public int getPublicationStatus() {
            return publicationStatus;
        }

        public void setPublicationStatus(int publicationStatus) {
            this.publicationStatus = publicationStatus;
        }

        @Override
        public boolean isLocked() {
            return locked;
        }

        public void setLocked(boolean locked) {
            this.locked = locked;
        }

        @Override
        public boolean isWorkInProgress() {
            return workInProgress;
        }

        public void setWorkInProgress(boolean workInProgress) {
            this.workInProgress = workInProgress;
        }

        @Override
        public boolean isAllowedToPublishWithoutWorkflow() {
            return allowedToPublishWithoutWorkflow;
        }

        public void setAllowedToPublishWithoutWorkflow(boolean allowedToPublishWithoutWorkflow) {
            this.allowedToPublishWithoutWorkflow = allowedToPublishWithoutWorkflow;
        }

        @Override
        public boolean isNonRootMarkedForDeletion() {
            return nonRootMarkedForDeletion;
        }

        public void setNonRootMarkedForDeletion(boolean nonRootMarkedForDeletion) {
            this.nonRootMarkedForDeletion = nonRootMarkedForDeletion;
        }
    };
}
