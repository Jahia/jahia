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

import java.util.Collections;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaRuntimeException;

/**
 * Service implementation that calculates aggregated publication info based on data retrieved from associated PublicationService.
 */
public class JCRPublicationInfoAggregationServiceImpl implements JCRPublicationInfoAggregationService {

    private JCRSessionFactory sessionFactory;
    private JCRPublicationService publicationService;

    /**
     * @param sessionFactory Associated JCR session factory
     */
    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * @param sessionFactory Associated publication service
     */
    public void setPublicationService(JCRPublicationService publicationService) {
        this.publicationService = publicationService;
    }

    @Override
    public AggregatedPublicationInfo getAggregatedPublicationInfo(String nodeIdentifier, String language, boolean subNodes, boolean references) {

        try {

            JCRSessionWrapper session = sessionFactory.getCurrentUserSession();
            JCRNodeWrapper node = session.getNodeByIdentifier(nodeIdentifier);

            PublicationInfo publicationInfo = publicationService.getPublicationInfo(nodeIdentifier, Collections.singleton(language), references, subNodes, false, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE).get(0);

            if (!subNodes) {
                // We don't include sub-nodes, but we still need the translation node to get correct status.
                String translationNodeName = "j:translation_" + language;
                if (node.hasNode(translationNodeName)) {
                    JCRNodeWrapper translationNode = node.getNode(translationNodeName);
                    PublicationInfo translationInfo = publicationService.getPublicationInfo(translationNode.getIdentifier(), Collections.singleton(language), references, false, false, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE).get(0);
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
