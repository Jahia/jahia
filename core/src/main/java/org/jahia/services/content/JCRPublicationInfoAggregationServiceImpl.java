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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.services.workflow.WorkflowRule;
import org.jahia.services.workflow.WorkflowService;

/**
 * Service implementation that calculates aggregated publication info based on data retrieved from associated PublicationService.
 */
public class JCRPublicationInfoAggregationServiceImpl implements JCRPublicationInfoAggregationService {

    private JCRSessionFactory sessionFactory;
    private JCRPublicationService publicationService;
    private WorkflowService workflowService;

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

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @Override
    public AggregatedPublicationInfo getAggregatedPublicationInfo(String nodeIdentifier, String language, boolean subNodes, boolean references, JCRSessionWrapper session) {

        try {

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

    @Override
    public Collection<FullPublicationInfo> getFullPublicationInfos(Collection<String> nodeIdentifiers, Collection<String> languages, boolean allSubTree, JCRSessionWrapper session) {
        try {
            LinkedHashMap<String, FullPublicationInfo> result = new LinkedHashMap<String, FullPublicationInfo>();
            ArrayList<String> nodeIdentifierList = new ArrayList<>(nodeIdentifiers);
            for (String language : languages) {
                Collection<PublicationInfo> publicationInfos = publicationService.getPublicationInfos(nodeIdentifierList, Collections.singleton(language), true, true, allSubTree, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE);
                for (PublicationInfo publicationInfo : publicationInfos) {
                    publicationInfo.clearInternalAndPublishedReferences(nodeIdentifierList);
                }
                final Collection<FullPublicationInfo> infos = convert(publicationInfos, language, "publish", session);
//                String lastGroup = null;
//                String lastTitle = null;
//                Locale locale = new Locale(language);
                for (FullPublicationInfo info : infos) {
                    if (!info.isPublishable() && info.getPublicationStatus() != PublicationInfo.MANDATORY_LANGUAGE_UNPUBLISHABLE) {
                        continue;
                    }
                    if (info.getWorkflowDefinition() == null && !info.isAllowedToPublishWithoutWorkflow()) {
                        continue;
                    }
                    result.put(language + "/" + info.getNodeIdentifier(), info);
//                    if (lastGroup == null || !info.getWorkflowGroup().equals(lastGroup)) {
//                        lastGroup = info.getWorkflowGroup();
//                        lastTitle = info.getTitle() + " ( " + locale.getDisplayName(locale) + " )";
//                    }
//                    info.setWorkflowTitle(lastTitle);
                }
            }
            return new ArrayList<FullPublicationInfo>(result.values());
        } catch (RepositoryException e) {
            throw new JahiaRuntimeException(e);
        }
    }

    private Collection<FullPublicationInfo> convert(Collection<PublicationInfo> publicationInfos, String language, String workflowAction, JCRSessionWrapper session) throws RepositoryException {
        List<FullPublicationInfo> result = new ArrayList<FullPublicationInfo>();
        List<String> mainPaths = new ArrayList<String>();
        for (PublicationInfo publicationInfo : publicationInfos) {
            final Collection<FullPublicationInfo> infos = convert(publicationInfo, publicationInfo.getRoot(), mainPaths, language, workflowAction, session).values();
            result.addAll(infos);
        }
        return result;
    }

    private Map<String, FullPublicationInfo> convert(PublicationInfo publicationInfo, PublicationInfoNode root, Collection<String> mainPaths, String language, String workflowAction, JCRSessionWrapper session) {
        Map<String, FullPublicationInfo> infos = new LinkedHashMap<String, FullPublicationInfo>();
        return convert(publicationInfo, root, mainPaths, language, infos, workflowAction, session);
    }

    private Map<String, FullPublicationInfo> convert(
        PublicationInfo publicationInfo,
        PublicationInfoNode root,
        Collection<String> mainPaths,
        String language,
        Map<String, FullPublicationInfo> infos,
        String workflowAction,
        JCRSessionWrapper session
    ) {

        PublicationInfoNode node = publicationInfo.getRoot();
        List<PublicationInfo> referencePublicationInfos = new ArrayList<PublicationInfo>();
        convert(infos, root, mainPaths, null, node, referencePublicationInfos, language, workflowAction, session);
        Map<String, FullPublicationInfo> result = new LinkedHashMap<String, FullPublicationInfo>();
        result.putAll(infos);
        for (PublicationInfo referencePublicationInfo : referencePublicationInfos) {
            if (!infos.containsKey(referencePublicationInfo.getRoot().getUuid())) {
                result.putAll(convert(referencePublicationInfo, referencePublicationInfo.getRoot(), mainPaths, language, infos, workflowAction, session));
            }
        }
        return result;
    }

    private FullPublicationInfo convert(
        Map<String, FullPublicationInfo> allInfos,
        PublicationInfoNode root,
        Collection<String> mainPaths,
        WorkflowRule lastRule,
        PublicationInfoNode node,
        Collection<PublicationInfo> references,
        String language,
        String workflowAction,
        JCRSessionWrapper session
    ) {

        FullPublicationInfoImpl info = new FullPublicationInfoImpl(node.getUuid(), node.getStatus());
        try {
            JCRNodeWrapper jcrNode;
            if (node.getStatus() == PublicationInfo.DELETED) {
                JCRSessionWrapper liveSession = sessionFactory.getCurrentUserSession(Constants.LIVE_WORKSPACE, session.getLocale(), session.getFallbackLocale());
                jcrNode = liveSession.getNodeByUUID(node.getUuid());
            } else {
                jcrNode = session.getNodeByUUID(node.getUuid());
                if (lastRule == null || jcrNode.hasNode(WorkflowService.WORKFLOWRULES_NODE_NAME)) {
                    WorkflowRule rule = workflowService.getWorkflowRuleForAction(jcrNode, false, workflowAction);
                    if (rule != null) {
                        if (!rule.equals(lastRule)) {
                            if (workflowService.getWorkflowRuleForAction(jcrNode, true, workflowAction) != null) {
                                lastRule = rule;
                            } else {
                                lastRule = null;
                            }
                        }
                    }
                }
            }
//            if (jcrNode.hasProperty("jcr:title")) {
//                info.setTitle(jcrNode.getProperty("jcr:title").getString());
//            } else {
//                info.setTitle(jcrNode.getName());
//            }
            info.setNodePath(jcrNode.getPath());
//            info.setNodetype(jcrNode.getPrimaryNodeType().getLabel(currentUserSession.getLocale()));
            info.setAllowedToPublishWithoutWorkflow(jcrNode.hasPermission("publish"));
            info.setNonRootMarkedForDeletion(jcrNode.isNodeType(Constants.JAHIAMIX_MARKED_FOR_DELETION) && !jcrNode.isNodeType(Constants.JAHIAMIX_MARKED_FOR_DELETION_ROOT));
        } catch (RepositoryException e) {
            throw new JahiaRuntimeException(e);
        }

        info.setWorkInProgress(node.isWorkInProgress());
        String mainPath = root.getPath();
//        info.setMainPath(mainPath);
//        info.setMainUUID(root.getUuid());
//        info.setLanguage(language);
        if (!mainPaths.contains(mainPath)) {
            mainPaths.add(mainPath);
        }
//        info.setMainPathIndex(mainPaths.indexOf(mainPath));
        Map<String, FullPublicationInfoImpl> infosByNodePath = new HashMap<String, FullPublicationInfoImpl>();
        infosByNodePath.put(node.getPath(), info);
        List<String> referenceUuids = new ArrayList<String>();

        allInfos.put(node.getUuid(), info);

        if (lastRule != null) {
            info.setWorkflowGroup(language + lastRule.getDefinitionPath());
            info.setWorkflowDefinition(lastRule.getProviderKey() + ":" + lastRule.getWorkflowDefinitionKey());
        } else {
            info.setWorkflowGroup(language + " no-workflow");
        }

        String translationNodePath = node.getChildren().size() > 0 ? "/j:translation_" + language : null;
        for (PublicationInfoNode childNode : node.getChildren()) {
            if (childNode.getPath().contains(translationNodePath)) {
                String path = StringUtils.substringBeforeLast(childNode.getPath(), "/j:translation");
                FullPublicationInfoImpl lastInfo = infosByNodePath.get(path);
                if (lastInfo != null) {
                    if (childNode.getStatus() > lastInfo.getPublicationStatus()) {
                        lastInfo.setPublicationStatus(childNode.getStatus());
                    }
                    if (lastInfo.getPublicationStatus() == PublicationInfo.UNPUBLISHED && childNode.getStatus() != PublicationInfo.UNPUBLISHED) {
                        lastInfo.setPublicationStatus(childNode.getStatus());
                    }
                    if (childNode.isLocked()) {
                        info.setLocked(true);
                    }
                    if (childNode.isWorkInProgress()) {
                        info.setWorkInProgress(true);
                    }
                    lastInfo.setTranslationNodeIdentifier(childNode.getUuid());
                }
                for (PublicationInfo publicationInfo : childNode.getReferences()) {
                    if (!referenceUuids.contains(publicationInfo.getRoot().getUuid()) && !allInfos.containsKey(publicationInfo.getRoot().getUuid())) {
                        referenceUuids.add(publicationInfo.getRoot().getUuid());
                        allInfos.putAll(convert(publicationInfo, publicationInfo.getRoot(), mainPaths, language, allInfos, workflowAction, session));
                    }
                }
            } else if (childNode.getPath().contains("/j:translation") && (node.getStatus() == PublicationInfo.MARKED_FOR_DELETION || node.getStatus() == PublicationInfo.DELETED)) {
                String key = StringUtils.substringBeforeLast(childNode.getPath(), "/j:translation");
                FullPublicationInfoImpl lastPub = infosByNodePath.get(key);
                if (lastPub.getDeletedTranslationNodeIdentifier() != null) {
                    lastPub.setDeletedTranslationNodeIdentifier(lastPub.getDeletedTranslationNodeIdentifier() + " " + childNode.getUuid());
                } else {
                    lastPub.setDeletedTranslationNodeIdentifier(childNode.getUuid());
                }
            }
        }
        references.addAll(node.getReferences());

        for (PublicationInfo publicationInfo : node.getReferences()) {
            if (!referenceUuids.contains(publicationInfo.getRoot().getUuid())) {
                referenceUuids.add(publicationInfo.getRoot().getUuid());
                if (!mainPaths.contains(publicationInfo.getRoot().getPath()) && !allInfos.containsKey(publicationInfo.getRoot().getUuid())) {
                    allInfos.putAll(convert(publicationInfo, publicationInfo.getRoot(), mainPaths, language, allInfos, workflowAction, session));
                }
            }
        }

        // Move node after references
        allInfos.remove(node.getUuid());
        allInfos.put(node.getUuid(), info);

        for (PublicationInfoNode sub : node.getChildren()) {
            if (sub.getPath().indexOf("/j:translation") == -1) {
                convert(allInfos, root, mainPaths, lastRule, sub, references, language, workflowAction, session);
            }
        }

        return info;
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

    private static class FullPublicationInfoImpl implements FullPublicationInfo {

        private String nodeIdentifier;
        private String nodePath;
        private boolean publishable;
        private boolean locked;
        private boolean workInProgress;
        private int publicationStatus;
        private String workflowDefinition;
        private String workflowGroup;
        private boolean allowedToPublishWithoutWorkflow;
        private String translationNodeIdentifier;
        private String deletedTranslationNodeIdentifier;
        private boolean nonRootMarkedForDeletion;

        public FullPublicationInfoImpl(String nodeIdentifier, int publicationStatus) {
            this.nodeIdentifier = nodeIdentifier;
            this.publicationStatus = publicationStatus;
        }

        @Override
        public String getNodeIdentifier() {
            return nodeIdentifier;
        }

        @Override
        public String getNodePath() {
            return nodePath;
        }

        public void setNodePath(String nodePath) {
            this.nodePath = nodePath;
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
        public boolean isPublishable() {
            return publishable;
        }

        @Override
        public String getWorkflowDefinition() {
            return workflowDefinition;
        }

        public void setWorkflowDefinition(String workflowDefinition) {
            this.workflowDefinition = workflowDefinition;
        }

        @Override
        public String getWorkflowGroup() {
            return workflowGroup;
        }

        public void setWorkflowGroup(String workflowGroup) {
            this.workflowGroup = workflowGroup;
        }

        @Override
        public boolean isAllowedToPublishWithoutWorkflow() {
            return allowedToPublishWithoutWorkflow;
        }

        public void setAllowedToPublishWithoutWorkflow(boolean allowedToPublishWithoutWorkflow) {
            this.allowedToPublishWithoutWorkflow = allowedToPublishWithoutWorkflow;
        }

        @Override
        public String getTranslationNodeIdentifier() {
            return translationNodeIdentifier;
        }

        public void setTranslationNodeIdentifier(String translationNodeIdentifier) {
            this.translationNodeIdentifier = translationNodeIdentifier;
        }

        @Override
        public String getDeletedTranslationNodeIdentifier() {
            return deletedTranslationNodeIdentifier;
        }

        public void setDeletedTranslationNodeIdentifier(String deletedTranslationNodeIdentifier) {
            this.deletedTranslationNodeIdentifier = deletedTranslationNodeIdentifier;
        }

        @Override
        public boolean isNonRootMarkedForDeletion() {
            return nonRootMarkedForDeletion;
        }

        public void setNonRootMarkedForDeletion(boolean nonRootMarkedForDeletion) {
            this.nonRootMarkedForDeletion = nonRootMarkedForDeletion;
        }
    }
}
