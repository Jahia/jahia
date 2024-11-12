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
package org.jahia.services.content;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.services.workflow.WorkflowRule;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.utils.LanguageCodeConverters;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import java.util.*;

import static org.jahia.services.content.PublicationInfo.*;

/**
 * Service implementation that:
 * - delegates lower level info retrieval operations to the associated JCRPublicationService
 * - performs publication via an asynchronous job
 */
public class ComplexPublicationServiceImpl implements ComplexPublicationService {
    private static final transient Logger logger = LoggerFactory.getLogger(ComplexPublicationServiceImpl.class);
    private static final String J_TRANSLATION = "/j:translation";
    private static final String J_TRANSLATION_UNDERSCORE = "/j:translation_";
    private static final String PUBLISH = "publish";


    private JCRSessionFactory sessionFactory;
    private JCRPublicationService publicationService;
    private WorkflowService workflowService;
    private SchedulerService schedulerService;

    /**
     * @param sessionFactory Associated JCR session factory
     */
    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * @param publicationService Associated JCR publication service
     */
    public void setPublicationService(JCRPublicationService publicationService) {
        this.publicationService = publicationService;
    }

    /**
     * @param workflowService Associated workflow service
     */
    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    /**
     * @param schedulerService Associated scheduler service
     */
    public void setSchedulerService(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @Override
    public AggregatedPublicationInfo getAggregatedPublicationInfo(String nodeIdentifier, String language,
                                                                  boolean subNodes, boolean references, JCRSessionWrapper sourceSession) {

        try {

            JCRNodeWrapper node = sourceSession.getNodeByIdentifier(nodeIdentifier);

            PublicationInfo publicationInfo = publicationService.getPublicationInfo(nodeIdentifier, Collections.singleton(language), references, subNodes, false, sourceSession.getWorkspace().getName(), Constants.LIVE_WORKSPACE).get(0);

            if (!subNodes) {
                // We don't include sub-nodes, but we still need the translation node to get correct status.
                setCorrectTranslationNodeStatus(nodeIdentifier, language, references, sourceSession, node, publicationInfo);
            }

            AggregatedPublicationInfoImpl result = new AggregatedPublicationInfoImpl(publicationInfo.getRoot().getStatus());

            if(publicationInfo.getRoot().isWorkInProgress()){
                result.setWorkInProgress(true);
            }

            String translationNodeRelPath = (!publicationInfo.getRoot().getChildren().isEmpty() ? (J_TRANSLATION_UNDERSCORE + language) : null);
            for (PublicationInfoNode childNode : publicationInfo.getRoot().getChildren()) {
                resolveChildNodesInfo(nodeIdentifier, language, result, translationNodeRelPath, childNode);
            }

            result.setAllowedToPublishWithoutWorkflow(node.hasPermission(PUBLISH));
            result.setNonRootMarkedForDeletion(result.getPublicationStatus() == PublicationInfo.MARKED_FOR_DELETION && !node.isNodeType(Constants.JAHIAMIX_MARKED_FOR_DELETION_ROOT));

            if (result.getPublicationStatus() == PublicationInfo.PUBLISHED) {
                // Check if any of the descendant nodes or references are modified or unpublished.
                checkDescendantNodesAndReferences(language, publicationInfo, result);
            }

            return result;
        } catch (RepositoryException e) {
            throw new JahiaRuntimeException(e);
        }
    }

    private void checkDescendantNodesAndReferences(String language, PublicationInfo publicationInfo, AggregatedPublicationInfoImpl result) {
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

    private void resolveChildNodesInfo(String nodeIdentifier, String language, AggregatedPublicationInfoImpl result, String translationNodeRelPath, PublicationInfoNode childNode) throws RepositoryException {
        if (childNode.getPath().contains(translationNodeRelPath)) {
            if(childNode.getStatus() == NOT_PUBLISHED) {
                if (!isPublished(nodeIdentifier, language) && result.getPublicationStatus() != MANDATORY_LANGUAGE_VALID && result.getPublicationStatus() != UNPUBLISHED) {
                    result.setPublicationStatus(NOT_PUBLISHED);
                }
            } else if (childNode.getStatus() > result.getPublicationStatus()) {
                result.setPublicationStatus(childNode.getStatus());
            }
            if (result.getPublicationStatus() == UNPUBLISHED && childNode.getStatus() != UNPUBLISHED && childNode.getStatus() != NOT_PUBLISHED) {
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

    private void setCorrectTranslationNodeStatus(String nodeIdentifier, String language, boolean references, JCRSessionWrapper sourceSession, JCRNodeWrapper node, PublicationInfo publicationInfo) throws RepositoryException {
        String translationNodeName = "j:translation_" + language;
        if (node.hasNode(translationNodeName)) {
            JCRNodeWrapper translationNode = node.getNode(translationNodeName);
            PublicationInfo translationInfo = publicationService.getPublicationInfo(translationNode.getIdentifier(), Collections.singleton(language), references, false, false, sourceSession.getWorkspace().getName(), Constants.LIVE_WORKSPACE).get(0);
            publicationInfo.getRoot().addChild(translationInfo.getRoot());
        } else if (publicationInfo.getRoot().getStatus() == PublicationInfo.PUBLISHED && node.getNodes("j:translation_*").hasNext() && !isPublished(nodeIdentifier, language)) {
            publicationInfo.getRoot().setStatus(NOT_PUBLISHED);
        }
    }

    private boolean isPublished(String nodeId, String language) throws RepositoryException {
        try {
            JCRSessionFactory.getInstance().getCurrentUserSession(Constants.LIVE_WORKSPACE, LanguageCodeConverters.languageCodeToLocale(language)).getNodeByIdentifier(nodeId);
            return true;
        } catch (ItemNotFoundException e) {
            return false;
        }
    }

    @Override
    public Collection<FullPublicationInfo> getFullPublicationInfos(Collection<String> nodeIdentifiers, Collection<String> languages, boolean allSubTree, JCRSessionWrapper sourceSession) {
        return getFullPublicationInfos(nodeIdentifiers, languages, allSubTree, false, sourceSession);
    }

    @Override
    public Collection<FullPublicationInfo> getFullPublicationInfos(Collection<String> nodeIdentifiers, Collection<String> languages, boolean allSubTree, boolean includeRemoved, JCRSessionWrapper sourceSession) {
        try {
            if (languages == null) {
                languages = Collections.singletonList(null);
            }
            LinkedHashMap<String, FullPublicationInfo> result = new LinkedHashMap<>();
            ArrayList<String> nodeIdentifierList = new ArrayList<>(nodeIdentifiers);
            for (String language : languages) {
                Collection<PublicationInfo> publicationInfos = publicationService.getPublicationInfos(nodeIdentifierList, Collections.singleton(language), true, true, allSubTree, sourceSession.getWorkspace().getName(), Constants.LIVE_WORKSPACE);
                for (PublicationInfo publicationInfo : publicationInfos) {
                    publicationInfo.clearInternalAndPublishedReferences(nodeIdentifierList);
                }
                Collection<FullPublicationInfoImpl> infos = convert(publicationInfos, language, PUBLISH, sourceSession);
                addItem(result, language, infos, includeRemoved);
            }
            return new ArrayList<>(result.values());
        } catch (RepositoryException e) {
            throw new JahiaRuntimeException(e);
        }
    }

    private void addItem(LinkedHashMap<String, FullPublicationInfo> result, String language, Collection<FullPublicationInfoImpl> infos, boolean includeRemoved) {
        String lastGroup = null;
        String lastTitle = null;
        Locale locale = language != null ? new Locale(language) : null;
        for (FullPublicationInfoImpl info : infos) {
            if ((info.isPublishable() || info.getPublicationStatus() == PublicationInfo.MANDATORY_LANGUAGE_UNPUBLISHABLE || (info.isNonRootMarkedForDeletion() && includeRemoved)) &&
                    (info.getWorkflowDefinition() != null || info.isAllowedToPublishWithoutWorkflow())) {
                result.put(language != null ? (language + "/" + info.getNodeIdentifier()) : info.getNodeIdentifier(), info);
                if (!info.getWorkflowGroup().equals(lastGroup)) {
                    lastGroup = info.getWorkflowGroup();
                    lastTitle = locale != null ? (info.getNodeTitle() + " ( " + locale.getDisplayName(locale) + " )") : info.getNodeTitle();
                }
                info.setWorkflowTitle(lastTitle);
            }
        }
    }

    @Override
    public Collection<FullPublicationInfo> getFullUnpublicationInfos(Collection<String> nodeIdentifiers, Collection<String> languages, boolean allSubTree, JCRSessionWrapper session) {
        try {
            // When asked for un-publication infos, we use live workspace as both source and destination,
            // because in case of an un-publication there is nothing copied from default to live, or from live to default.
            // In that case we rather just remove live node, so the only workspace concerned is LIVE.
            // Doing so, we are able to un-publish a node that have been moved in DEFAULT workspace (because it have the same UUID in DEFAULT and LIVE)
            List<PublicationInfo> publicationInfos = publicationService.getPublicationInfos(new ArrayList<>(nodeIdentifiers), null, false, true, allSubTree, Constants.LIVE_WORKSPACE, Constants.LIVE_WORKSPACE);
            LinkedHashMap<String, FullPublicationInfoImpl> result = new LinkedHashMap<>();
            for (String language : languages) {
                Collection<FullPublicationInfoImpl> infos = convert(publicationInfos, language, "unpublish", session);
                String lastGroup = null;
                String lastTitle = null;
                Locale locale = new Locale(language);
                for (FullPublicationInfoImpl info : infos) {
                    if (info.getPublicationStatus() == PublicationInfo.PUBLISHED && (info.getWorkflowDefinition() != null || info.isAllowedToPublishWithoutWorkflow())) {
                        result.put(language + "/" + info.getNodeIdentifier(), info);
                        if (!info.getWorkflowGroup().equals(lastGroup)) {
                            lastGroup = info.getWorkflowGroup();
                            lastTitle = info.getNodeTitle() + " ( " + locale.getDisplayName(locale) + " )";
                        }
                        info.setWorkflowTitle(lastTitle);
                    }
                }
            }
            // remove identifier of contents with a translation
            Set<String> clearedIdentifiers = clearNodeIdentifiersOnTranslationNodes(result);
            // filter out shared node that should not be part of the unpublication
            removeSharedNodes(result, languages, clearedIdentifiers);
            return new ArrayList<>(result.values());
        } catch (RepositoryException e) {
            throw new JahiaRuntimeException(e);
        }
    }

    private Collection<FullPublicationInfoImpl> convert(Collection<PublicationInfo> publicationInfos, String language, String workflowAction, JCRSessionWrapper session) {
        List<FullPublicationInfoImpl> result = new ArrayList<>();
        List<String> mainPaths = new ArrayList<>();
        for (PublicationInfo publicationInfo : publicationInfos) {
            final Collection<FullPublicationInfoImpl> infos = convert(publicationInfo, publicationInfo.getRoot(), mainPaths, language, workflowAction, session).values();
            result.addAll(infos);
        }
        return result;
    }

    private Map<String, FullPublicationInfoImpl> convert(PublicationInfo publicationInfo, PublicationInfoNode root, List<String> mainPaths, String language, String workflowAction, JCRSessionWrapper session) {
        Map<String, FullPublicationInfoImpl> infos = new LinkedHashMap<>();
        return convert(publicationInfo, root, mainPaths, language, infos, workflowAction, session);
    }

    private Map<String, FullPublicationInfoImpl> convert(
        PublicationInfo publicationInfo,
        PublicationInfoNode root,
        List<String> mainPaths,
        String language,
        Map<String, FullPublicationInfoImpl> infos,
        String workflowAction,
        JCRSessionWrapper session
    ) {

        PublicationInfoNode node = publicationInfo.getRoot();
        List<PublicationInfo> referencePublicationInfos = new ArrayList<>();
        convert(infos, root, mainPaths, null, node, referencePublicationInfos, language, workflowAction, session);
        Map<String, FullPublicationInfoImpl> result = new LinkedHashMap<>(infos);
        for (PublicationInfo referencePublicationInfo : referencePublicationInfos) {
            if (!infos.containsKey(referencePublicationInfo.getRoot().getUuid())) {
                result.putAll(convert(referencePublicationInfo, referencePublicationInfo.getRoot(), mainPaths, language, infos, workflowAction, session));
            }
        }
        return result;
    }

    @SuppressWarnings("squid:S00107")
    private void convert(
        Map<String, FullPublicationInfoImpl> allInfos,
        PublicationInfoNode root,
        List<String> mainPaths,
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
                    lastRule = getWorkflowRule(lastRule, workflowAction, jcrNode, rule);
                }
            }
            setNodeTitle(info, jcrNode);
            info.setNodePath(jcrNode.getPath());
            info.setNodeType(jcrNode.getPrimaryNodeType());
            info.setAllowedToPublishWithoutWorkflow(jcrNode.hasPermission(PUBLISH));
            info.setNonRootMarkedForDeletion(jcrNode.isNodeType(Constants.JAHIAMIX_MARKED_FOR_DELETION) && !jcrNode.isNodeType(Constants.JAHIAMIX_MARKED_FOR_DELETION_ROOT));
        } catch (RepositoryException e) {
            logger.warn("Issue when reading workflow and delete status of node " + node.getPath(), e);
            info.setNodeTitle(node.getPath());
        }

        info.setAllPublishedLanguagesInSubTree(getAllPublishedLanguagesInSubTree(node));
        info.setLanguage(language);
        setWorkInProgressRelatedInformation(node, info);
        info.setPublicationRootNodeIdentifier(root.getUuid());
        info.setChildOfWIPNode(node.isWorkInProgressChild());
        String mainPath = root.getPath();
        info.setPublicationRootNodePath(mainPath);
        if (!mainPaths.contains(mainPath)) {
            mainPaths.add(mainPath);
        }
        info.setPublicationRootNodePathIndex(mainPaths.indexOf(mainPath));
        Map<String, FullPublicationInfoImpl> infosByNodePath = new HashMap<>();
        infosByNodePath.put(node.getPath(), info);
        List<String> referenceUuids = new ArrayList<>();

        allInfos.put(node.getUuid(), info);

        setWorkflowGroupForNode(lastRule, language, info);

        String translationNodePath = !node.getChildren().isEmpty() ? J_TRANSLATION_UNDERSCORE + language : null;
        for (PublicationInfoNode childNode : node.getChildren()) {
            processChildNodes(allInfos, mainPaths, node, language, workflowAction, session, info, infosByNodePath, referenceUuids, translationNodePath, childNode);
        }
        references.addAll(node.getReferences());

        for (PublicationInfo publicationInfo : node.getReferences()) {
            resolveReferencesInfo(allInfos, mainPaths, language, workflowAction, session, referenceUuids, publicationInfo);
        }

        // Move node after references
        allInfos.remove(node.getUuid());
        allInfos.put(node.getUuid(), info);

        for (PublicationInfoNode sub : node.getChildren()) {
            if (!sub.getPath().contains(J_TRANSLATION) && !sub.getPath().contains("j:referenceInField_")) {
                convert(allInfos, root, mainPaths, lastRule, sub, references, language, workflowAction, session);
            }
        }

    }

    @SuppressWarnings("squid:S00107")
    private void processChildNodes(Map<String, FullPublicationInfoImpl> allInfos, List<String> mainPaths,
                                   PublicationInfoNode node, String language, String workflowAction,
                                   JCRSessionWrapper session, FullPublicationInfoImpl info, Map<String, FullPublicationInfoImpl> infosByNodePath, List<String> referenceUuids, String translationNodePath, PublicationInfoNode childNode) {
        if (childNode.getPath().contains(translationNodePath)) {
            resolveInfoForTranslationNode(allInfos, mainPaths, language,
                    workflowAction, session, info, infosByNodePath, referenceUuids, childNode);
        } else if (childNode.getPath().contains(J_TRANSLATION) && (node.getStatus() == PublicationInfo.MARKED_FOR_DELETION || node.getStatus() == PublicationInfo.DELETED)) {
            String key = StringUtils.substringBeforeLast(childNode.getPath(), J_TRANSLATION);
            FullPublicationInfoImpl lastInfo = infosByNodePath.get(key);
            lastInfo.addDeletedTranslationNodeIdentifier(childNode.getUuid());
        }
    }


    @SuppressWarnings("squid:S00107")
    private void resolveReferencesInfo(Map<String, FullPublicationInfoImpl> allInfos, List<String> mainPaths, String language, String workflowAction, JCRSessionWrapper session, List<String> referenceUuids, PublicationInfo publicationInfo) {
        if (!referenceUuids.contains(publicationInfo.getRoot().getUuid())) {
            referenceUuids.add(publicationInfo.getRoot().getUuid());
            if (!mainPaths.contains(publicationInfo.getRoot().getPath()) && !allInfos.containsKey(publicationInfo.getRoot().getUuid())) {
                allInfos.putAll(convert(publicationInfo, publicationInfo.getRoot(), mainPaths, language, allInfos, workflowAction, session));
            }
        }
    }

    @SuppressWarnings("squid:S00107")
    private void resolveInfoForTranslationNode(Map<String, FullPublicationInfoImpl> allInfos, List<String> mainPaths, String language, String workflowAction, JCRSessionWrapper session, FullPublicationInfoImpl info, Map<String, FullPublicationInfoImpl> infosByNodePath, List<String> referenceUuids, PublicationInfoNode childNode) {
        String path = StringUtils.substringBeforeLast(childNode.getPath(), J_TRANSLATION);
        FullPublicationInfoImpl lastInfo = infosByNodePath.get(path);
        if (lastInfo != null) {
            if (childNode.getStatus() > lastInfo.getPublicationStatus()) {
                lastInfo.setPublicationStatus(childNode.getStatus());
            }
            if (lastInfo.getPublicationStatus() == UNPUBLISHED && childNode.getStatus() != UNPUBLISHED) {
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
    }

    private void setWorkInProgressRelatedInformation(PublicationInfoNode node, FullPublicationInfoImpl info) {
        info.setWorkInProgress(node.isWorkInProgress());
        if (node.isWorkInProgress()) {
            markNodesAsWIPChildren(node);
        }
    }

    private void setNodeTitle(FullPublicationInfoImpl info, JCRNodeWrapper jcrNode) throws RepositoryException {
        if (jcrNode.hasProperty("jcr:title")) {
            info.setNodeTitle(jcrNode.getProperty("jcr:title").getString());
        } else {
            info.setNodeTitle(jcrNode.getName());
        }
    }

    private void setWorkflowGroupForNode(WorkflowRule lastRule, String language, FullPublicationInfoImpl info) {
        if (lastRule != null) {
            info.setWorkflowGroup(language + lastRule.getDefinitionPath());
            info.setWorkflowDefinition(lastRule.getProviderKey() + ":" + lastRule.getWorkflowDefinitionKey());
        } else {
            info.setWorkflowGroup(language + " no-workflow");
        }
    }

    private WorkflowRule getWorkflowRule(WorkflowRule lastRule, String workflowAction, JCRNodeWrapper jcrNode, WorkflowRule rule) throws RepositoryException {
        if (rule != null && !rule.equals(lastRule)) {
            if (workflowService.getWorkflowRuleForAction(jcrNode, true, workflowAction) != null) {
                lastRule = rule;
            } else {
                lastRule = null;
            }
        }
        return lastRule;
    }

    private void markNodesAsWIPChildren(PublicationInfoNode node) {
        if (node == null) {
            return;
        }
        node.setWorkInProgressChild(true);
        for (PublicationInfoNode publicationInfo : node.getChildren()) {
            markNodesAsWIPChildren(publicationInfo);
        }
    }

    private Set<String> getAllPublishedLanguagesInSubTree(PublicationInfoNode node) {
        final Set<String> result = new HashSet<>();
        if (node.getStatus() != UNPUBLISHED  && node.getStatus() != NOT_PUBLISHED  && node.getPath().contains(J_TRANSLATION_UNDERSCORE)) {
            result.add(StringUtils.substringAfterLast(node.getPath(), J_TRANSLATION_UNDERSCORE));
        }
        for (PublicationInfoNode childNode : node.getChildren()) {
            result.addAll(getAllPublishedLanguagesInSubTree(childNode));
        }
        return result;
    }

    private static void removeSharedNodes(Map<String, FullPublicationInfoImpl> infosByPath, Collection<String> languages, Set<String> clearedIdentifiers) {
        Set<String> paths = new HashSet<>(infosByPath.keySet());
        // Filter out info of content we need to keep.
        for (String path : paths) {
            FullPublicationInfoImpl info = infosByPath.get(path);

            // Remove info: in case we already have an info for the i18n node
            boolean clearedForTranslation = clearedIdentifiers.contains(info.getNodeIdentifier());

            // check if subtree contains published translations
            boolean hasI18NSubContent = !info.getAllPublishedLanguagesInSubTree().isEmpty();

            // Remove info: in case subtree contains published translations and
            boolean contentNotPublishedInLanguage = hasI18NSubContent && !info.getAllPublishedLanguagesInSubTree().contains(info.getLanguage());

            // Keep content that is still publish in another language.
            Set<String> remainingPublishedLanguages = new HashSet<>(info.getAllPublishedLanguagesInSubTree());
            remainingPublishedLanguages.removeAll(languages);
            boolean contentStillPublishedInOtherLanguage = hasI18NSubContent && !remainingPublishedLanguages.isEmpty();
            boolean sharedNodeToBeUnpublish = shouldUnpublishSharedNode(infosByPath, info, languages);
            if (info.getTranslationNodeIdentifier() == null && (clearedForTranslation || contentNotPublishedInLanguage || contentStillPublishedInOtherLanguage || !sharedNodeToBeUnpublish)) {
                infosByPath.remove(path);
            }
        }
    }

    /**
     * Determines whether a shared node should be unpublished.
     *
     * @param infosByPath a map of publication information indexed by path
     * @param info the publication information of the node to check
     * @param languages a collection of languages to consider
     * @return {@code true} if the node should be unpublished, otherwise {@code false}
     */
    private static boolean shouldUnpublishSharedNode(Map<String, FullPublicationInfoImpl> infosByPath, FullPublicationInfoImpl info, Collection<String> languages){
        if (info.getNodeIdentifier() == null) {
            return true;
        }

        boolean isRootNode = info.getNodeIdentifier().equals(info.getPublicationRootNodeIdentifier());
        if (isRootNode) {
            return true;
        }

        if (info.getAllPublishedLanguagesInSubTree().isEmpty()) {
           for (String language : languages) {
               FullPublicationInfoImpl rootNodeInfo = infosByPath.get(language + "/" + info.getPublicationRootNodeIdentifier());
               if (rootNodeInfo != null) {
                   Set<String> remainingLanguages = new HashSet<>(rootNodeInfo.getAllPublishedLanguagesInSubTree());
                   remainingLanguages.removeAll(languages);
                   return remainingLanguages.isEmpty();
               }
           }
            return true;
        }
        return true;
    }

    private static Set<String> clearNodeIdentifiersOnTranslationNodes(Map<String, FullPublicationInfoImpl> infosByPath) {
        Set<String> clearedIdentifiers = new HashSet<>();
        for (FullPublicationInfoImpl info : infosByPath.values()) {
            if (info.getTranslationNodeIdentifier() != null) {
                clearedIdentifiers.add(info.getNodeIdentifier());
                info.clearNodeIdentifier();
            }
        }
        return clearedIdentifiers;
    }

    @Override
    public void publish(Collection<String> nodeIdentifiers, Collection<String> languages, JCRSessionWrapper session) {
        publish(nodeIdentifiers, languages, session, false);
    }
    @Override
    public void publish(Collection<String> nodeIdentifiers, Collection<String> languages, JCRSessionWrapper session, Boolean includeAllSubTree) {

        Collection<FullPublicationInfo> infos = getFullPublicationInfos(nodeIdentifiers, languages, includeAllSubTree, session);

        LinkedList<String> uuids = new LinkedList<>();
        for (FullPublicationInfo info : infos) {
            if (info.getPublicationStatus() == PublicationInfo.DELETED) {
                continue;
            }
            if (!info.isAllowedToPublishWithoutWorkflow()) {
                continue;
            }
            if (info.getNodeIdentifier() != null) {
                uuids.add(info.getNodeIdentifier());
            }
            if (info.getTranslationNodeIdentifier() != null) {
                uuids.add(info.getTranslationNodeIdentifier());
            }
            for (String deletedTranslationNodeIdentifier : info.getDeletedTranslationNodeIdentifiers()) {
                uuids.add(deletedTranslationNodeIdentifier);
            }
        }

        session.disableSessionCache();
        String workspaceName = session.getWorkspace().getName();
        List<String> paths = new ArrayList<>();
        for (String uuid : uuids) {
            try {
                paths.add(session.getNodeByIdentifier(uuid).getPath());
            } catch (RepositoryException e) {
                throw new JahiaRuntimeException(e);
            }
        }
        JobDetail jobDetail = BackgroundJob.createJahiaJob("Publication", PublicationJob.class);
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        jobDataMap.put(PublicationJob.PUBLICATION_UUIDS, uuids);
        jobDataMap.put(PublicationJob.PUBLICATION_PATHS, paths);
        jobDataMap.put(PublicationJob.SOURCE, workspaceName);
        jobDataMap.put(PublicationJob.DESTINATION, Constants.LIVE_WORKSPACE);
        jobDataMap.put(PublicationJob.CHECK_PERMISSIONS, true);
        try {
            schedulerService.scheduleJobNow(jobDetail);
        } catch (SchedulerException e) {
            throw new JahiaRuntimeException(e);
        }
    }

    private static class PublicationInfoSupport {

        private int publicationStatus;
        private boolean locked;
        private boolean workInProgress;
        private boolean childOfWIPNode;
        private boolean allowedToPublishWithoutWorkflow;
        private boolean nonRootMarkedForDeletion;

        public PublicationInfoSupport(int publicationStatus) {
            this.publicationStatus = publicationStatus;
        }

        public boolean isChildOfWIPNode() {
            return childOfWIPNode;
        }

        public void setChildOfWIPNode(boolean childOfWIPNode) {
            this.childOfWIPNode = childOfWIPNode;
        }

        public int getPublicationStatus() {
            return publicationStatus;
        }

        public void setPublicationStatus(int publicationStatus) {
            this.publicationStatus = publicationStatus;
        }

        public boolean isLocked() {
            return locked;
        }

        public void setLocked(boolean locked) {
            this.locked = locked;
        }

        public boolean isWorkInProgress() {
            return workInProgress;
        }

        public void setWorkInProgress(boolean workInProgress) {
            this.workInProgress = workInProgress;
        }

        public boolean isAllowedToPublishWithoutWorkflow() {
            return allowedToPublishWithoutWorkflow;
        }

        public void setAllowedToPublishWithoutWorkflow(boolean allowedToPublishWithoutWorkflow) {
            this.allowedToPublishWithoutWorkflow = allowedToPublishWithoutWorkflow;
        }

        public boolean isNonRootMarkedForDeletion() {
            return nonRootMarkedForDeletion;
        }

        public void setNonRootMarkedForDeletion(boolean nonRootMarkedForDeletion) {
            this.nonRootMarkedForDeletion = nonRootMarkedForDeletion;
        }
    };

    private static class AggregatedPublicationInfoImpl extends PublicationInfoSupport implements AggregatedPublicationInfo {

        public AggregatedPublicationInfoImpl(int publicationStatus) {
            super(publicationStatus);
        }
    }

    private static class FullPublicationInfoImpl extends PublicationInfoSupport implements FullPublicationInfo {

        private String nodeIdentifier;
        private String nodePath;
        private String nodeTitle;
        private ExtendedNodeType nodeType;
        private String publicationRootNodeIdentifier;
        private String publicationRootNodePath;
        private int publicationRootNodePathIndex;
        private String workflowTitle;
        private String workflowDefinition;
        private String workflowGroup;
        private String language;
        private String translationNodeIdentifier;
        private LinkedHashSet<String> deletedTranslationNodeIdentifiers = new LinkedHashSet<>();
        private Set<String> allPublishedLanguagesInSubTree = new HashSet<>();

        public FullPublicationInfoImpl(String nodeIdentifier, int publicationStatus) {
            super(publicationStatus);
            this.nodeIdentifier = nodeIdentifier;
        }

        @Override
        public String getNodeIdentifier() {
            return nodeIdentifier;
        }

        public void clearNodeIdentifier() {
            nodeIdentifier = null;
        }

        @Override
        public String getNodePath() {
            return nodePath;
        }

        public void setNodePath(String nodePath) {
            this.nodePath = nodePath;
        }

        @Override
        public String getNodeTitle() {
            return nodeTitle;
        }

        public void setNodeTitle(String nodeTitle) {
            this.nodeTitle = nodeTitle;
        }

        @Override
        public ExtendedNodeType getNodeType() {
            return nodeType;
        }

        public void setNodeType(ExtendedNodeType nodeType) {
            this.nodeType = nodeType;
        }

        @Override
        public String getPublicationRootNodeIdentifier() {
            return publicationRootNodeIdentifier;
        }

        public void setPublicationRootNodeIdentifier(String publicationRootNodeIdentifier) {
            this.publicationRootNodeIdentifier = publicationRootNodeIdentifier;
        }

        @Override
        public String getPublicationRootNodePath() {
            return publicationRootNodePath;
        }

        public void setPublicationRootNodePath(String publicationRootNodePath) {
            this.publicationRootNodePath = publicationRootNodePath;
        }

        @Override
        public int getPublicationRootNodePathIndex() {
            return publicationRootNodePathIndex;
        }

        public void setPublicationRootNodePathIndex(int publicationRootNodePathIndex) {
            this.publicationRootNodePathIndex = publicationRootNodePathIndex;
        }

        @Override
        public String getWorkflowTitle() {
            return workflowTitle;
        }

        public void setWorkflowTitle(String workflowTitle) {
            this.workflowTitle = workflowTitle;
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
        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        @Override
        public String getTranslationNodeIdentifier() {
            return translationNodeIdentifier;
        }

        public void setTranslationNodeIdentifier(String translationNodeIdentifier) {
            this.translationNodeIdentifier = translationNodeIdentifier;
        }

        @Override
        public Collection<String> getDeletedTranslationNodeIdentifiers() {
            return Collections.unmodifiableCollection(deletedTranslationNodeIdentifiers);
        }

        public void addDeletedTranslationNodeIdentifier(String deletedTranslationNodeIdentifier) {
            this.deletedTranslationNodeIdentifiers.add(deletedTranslationNodeIdentifier);
        }

        public Set<String> getAllPublishedLanguagesInSubTree() {
            return allPublishedLanguagesInSubTree;
        }

        public void setAllPublishedLanguagesInSubTree(Set<String> allPublishedLanguagesInSubTree) {
            this.allPublishedLanguagesInSubTree = allPublishedLanguagesInSubTree;
        }

        @Override
        public boolean isPublishable() {

            if (isLocked()) {
                return false;
            }

            int publicationStatus = getPublicationStatus();
            if (publicationStatus <= PublicationInfo.PUBLISHED) {
                return false;
            }
            if (publicationStatus == PublicationInfo.MANDATORY_LANGUAGE_UNPUBLISHABLE) {
                return false;
            }
            if (publicationStatus == PublicationInfo.MANDATORY_LANGUAGE_VALID) {
                return false;
            }

            if (isWorkInProgress()) {
                return false;
            }

            if (isChildOfWIPNode()) {
                return false;
            }

            return !isNonRootMarkedForDeletion();
        }
    }
}
