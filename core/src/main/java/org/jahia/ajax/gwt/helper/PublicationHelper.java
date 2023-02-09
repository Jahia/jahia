/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.helper;

import org.apache.commons.lang.StringUtils;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.widget.poller.ContentUnpublishedEvent;
import org.jahia.ajax.gwt.client.widget.publication.PublicationWorkflow;
import org.jahia.ajax.gwt.commons.server.ManagedGWTResource;
import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.atmosphere.AtmosphereServlet;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.workflow.WorkflowDefinition;
import org.jahia.services.workflow.WorkflowService;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.*;

/**
 * User: toto
 * Date: Sep 28, 2009
 * Time: 2:15:34 PM
 */
public class PublicationHelper {

    private static final Logger logger = LoggerFactory.getLogger(PublicationHelper.class);

    private JCRPublicationService publicationService;
    private ComplexPublicationService complexPublicationService;
    private WorkflowHelper workflowHelper;
    private WorkflowService workflowService;

    public void setPublicationService(JCRPublicationService publicationService) {
        this.publicationService = publicationService;
    }

    public void setComplexPublicationService(ComplexPublicationService complexPublicationService) {
        this.complexPublicationService = complexPublicationService;
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public WorkflowHelper getWorkflowHelper() {
        return workflowHelper;
    }

    public void setWorkflowHelper(WorkflowHelper workflowHelper) {
        this.workflowHelper = workflowHelper;
    }

    /**
     * Get the publication status information for a particular path.
     *
     * @param node node to get publication info for
     * @param currentUserSession
     * @param includeReferences
     * @param includeSubNodes
     * @return a GWTJahiaPublicationInfo object filled with the right status for the publication state of this path
     * @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException in case of any RepositoryException
     */
    public Map<String, GWTJahiaPublicationInfo> getAggregatedPublicationInfosByLanguage(JCRNodeWrapper node, Set<String> languages, JCRSessionWrapper currentUserSession, boolean includeReferences, boolean includeSubNodes) throws GWTJahiaServiceException {
        try {
            HashMap<String, GWTJahiaPublicationInfo> infos = new HashMap<String, GWTJahiaPublicationInfo>(languages.size());
            for (String language : languages) {
                ComplexPublicationService.AggregatedPublicationInfo aggregatedInfo = complexPublicationService.getAggregatedPublicationInfo(node.getIdentifier(), language, includeSubNodes, includeReferences, currentUserSession);
                GWTJahiaPublicationInfo gwtInfo = new GWTJahiaPublicationInfo(node.getIdentifier(), aggregatedInfo.getPublicationStatus());
                gwtInfo.setLocked(aggregatedInfo.isLocked());
                gwtInfo.setWorkInProgress(aggregatedInfo.isWorkInProgress());
                gwtInfo.setIsAllowedToPublishWithoutWorkflow(aggregatedInfo.isAllowedToPublishWithoutWorkflow());
                gwtInfo.setIsNonRootMarkedForDeletion(aggregatedInfo.isNonRootMarkedForDeletion());
                infos.put(language, gwtInfo);
            }
            return infos;
        } catch (Exception e) {
            logger.error("Cannot get publication status for node " + node.getPath() + ". Cause: " + e.getLocalizedMessage(), e);
            throw new GWTJahiaServiceException("Cannot get publication status for node " + node.getPath() + ". Cause: " + e.getLocalizedMessage(), e);
        }
    }

    public Map<String, List<GWTJahiaPublicationInfo>> getFullPublicationInfosByLanguage(List<String> uuids, Set<String> languages,
                                                            JCRSessionWrapper currentUserSession, boolean allSubTree) throws GWTJahiaServiceException {

        List<GWTJahiaPublicationInfo> all = getFullPublicationInfos(uuids, languages, currentUserSession, allSubTree, false, false);

        Map<String, List<GWTJahiaPublicationInfo>> res = new HashMap<String, List<GWTJahiaPublicationInfo>>();

        for (GWTJahiaPublicationInfo info : all) {
            if (!res.containsKey(info.getLanguage())) {
                res.put(info.getLanguage(), new ArrayList<GWTJahiaPublicationInfo>());
            }
            res.get(info.getLanguage()).add(info);
        }

        return res;
    }

    public List<GWTJahiaPublicationInfo> getFullPublicationInfos(List<String> uuids, Set<String> languages,
                                                                 JCRSessionWrapper currentUserSession,
                                                                 boolean allSubTree, boolean checkForUnpublication) throws GWTJahiaServiceException {
        return getFullPublicationInfos(uuids, languages, currentUserSession, allSubTree, false, checkForUnpublication);
    }

    public List<GWTJahiaPublicationInfo> getFullPublicationInfos(List<String> uuids, Set<String> languages,
                                                                 JCRSessionWrapper currentUserSession,
                                                                 boolean allSubTree, boolean includeRemoved, boolean checkForUnpublication) throws GWTJahiaServiceException {
        try {
            Collection<ComplexPublicationService.FullPublicationInfo> infos;
            if (checkForUnpublication) {
                infos = complexPublicationService.getFullUnpublicationInfos(uuids, languages, allSubTree, currentUserSession);
            } else {
                infos = complexPublicationService.getFullPublicationInfos(uuids, languages, allSubTree, includeRemoved, currentUserSession);
            }

            return convert(infos, currentUserSession);
        } catch (Exception e) {
            logger.error("Cannot get nodes " + uuids + ". Cause: " + e.getLocalizedMessage(), e);
            throw new GWTJahiaServiceException("Cannot get nodes " + uuids + ". Cause: " + e.getLocalizedMessage(), e);
        }
    }

    private static List<GWTJahiaPublicationInfo> convert(Collection<ComplexPublicationService.FullPublicationInfo> infos, JCRSessionWrapper session) {
        LinkedList<GWTJahiaPublicationInfo> gwtInfos = new LinkedList<>();
        for (ComplexPublicationService.FullPublicationInfo info : infos) {
            GWTJahiaPublicationInfo gwtInfo = convert(info, session);
            gwtInfos.add(gwtInfo);
        }
        return gwtInfos;
    }

    private static GWTJahiaPublicationInfo convert(ComplexPublicationService.FullPublicationInfo info, JCRSessionWrapper session) {
        GWTJahiaPublicationInfo gwtInfo = new GWTJahiaPublicationInfo(info.getNodeIdentifier(), info.getPublicationStatus());
        gwtInfo.setPath(info.getNodePath());
        gwtInfo.setTitle(info.getNodeTitle());
        gwtInfo.setNodetype(info.getNodeType() != null ? info.getNodeType().getLabel(session.getLocale()) : "");
        if (info.getNodeType().isNodeType("jnt:page")) {
            gwtInfo.set("isPage", Boolean.TRUE);
        }
        gwtInfo.setMainUUID(info.getPublicationRootNodeIdentifier());
        gwtInfo.setMainPath(info.getPublicationRootNodePath());
        gwtInfo.setMainPathIndex(info.getPublicationRootNodePathIndex());
        gwtInfo.setLocked(info.isLocked());
        gwtInfo.setWorkInProgress(info.isWorkInProgress());
        gwtInfo.setWorkflowTitle(info.getWorkflowTitle());
        gwtInfo.setWorkflowDefinition(info.getWorkflowDefinition());
        gwtInfo.setWorkflowGroup(info.getWorkflowGroup());
        gwtInfo.setIsAllowedToPublishWithoutWorkflow(info.isAllowedToPublishWithoutWorkflow());
        gwtInfo.setLanguage(info.getLanguage());
        gwtInfo.setI18NUuid(info.getTranslationNodeIdentifier());
        Collection<String> deletedTranslationNodeIdentifiers = info.getDeletedTranslationNodeIdentifiers();
        if (!deletedTranslationNodeIdentifiers.isEmpty()) {
            gwtInfo.setDeletedI18nUuid(StringUtils.join(deletedTranslationNodeIdentifiers, ' '));
        }
        gwtInfo.setIsNonRootMarkedForDeletion(info.isNonRootMarkedForDeletion());
        return gwtInfo;
    }

    public Map<PublicationWorkflow, WorkflowDefinition> createPublicationWorkflows(List<GWTJahiaPublicationInfo> all) {
        final TreeMap<String, List<GWTJahiaPublicationInfo>> infosListByWorflowGroup = new TreeMap<String, List<GWTJahiaPublicationInfo>>();

        Map<String, String> workflowGroupToKey = new HashMap<String, String>();
        List<String> keys = new ArrayList<String>();

        for (GWTJahiaPublicationInfo info : all) {
            String workflowGroup = info.getWorkflowGroup();
            if (!infosListByWorflowGroup.containsKey(workflowGroup)) {
                infosListByWorflowGroup.put(workflowGroup, new ArrayList<GWTJahiaPublicationInfo>());
            }
            infosListByWorflowGroup.get(workflowGroup).add(info);
            if (info.getWorkflowDefinition() != null) {
                workflowGroupToKey.put(info.getWorkflowGroup(), info.getWorkflowDefinition());
                if (!keys.contains(info.getWorkflowDefinition())) {
                    keys.add(info.getWorkflowDefinition());
                }
            }
        }

        Map<PublicationWorkflow, WorkflowDefinition> result = new LinkedHashMap<PublicationWorkflow, WorkflowDefinition>();

        Map<String, WorkflowDefinition> workflows = new HashMap<String, WorkflowDefinition>();
        for (String wf : keys) {
            WorkflowDefinition w = workflowService.getWorkflowDefinition(StringUtils.substringBefore(wf, ":"), StringUtils.substringAfter(wf, ":"), null);
            workflows.put(wf, w);
        }

        for (Map.Entry<String, List<GWTJahiaPublicationInfo>> entry : infosListByWorflowGroup.entrySet()) {
            result.put(new PublicationWorkflow(entry.getValue()), workflows.get(workflowGroupToKey.get(entry.getKey())));
        }

        return result;
    }

    /**
     * Publish a list of nodes into the live workspace.
     * Referenced nodes will also be published.
     * Parent node must be published, or will be published if publishParent is true.
     *
     * @param uuids    list of uuids of the nodes to publish
     * @param comments
     */
    public void publish(List<String> uuids, JCRSessionWrapper session, JCRSiteNode site, List<GWTJahiaNodeProperty> properties, List<String> comments) throws GWTJahiaServiceException {
        try {
            // todo : if workflow started on untranslated node, translation will be created and not added into the publish tree calculated here

            final String workspaceName = session.getWorkspace().getName();
            List<String> publicationPath = new ArrayList<>();
            for (String uuid : uuids) {
                try {
                    publicationPath.add(session.getNodeByIdentifier(uuid).getPath());
                } catch (RepositoryException e) {
                    logger.debug("Cannot get item " + uuid, e);
                }
            }
            JobDetail jobDetail = BackgroundJob.createJahiaJob("Publication", PublicationJob.class);
            JobDataMap jobDataMap = jobDetail.getJobDataMap();
            jobDataMap.put(BackgroundJob.JOB_SITEKEY, site.getName());
            jobDataMap.put(PublicationJob.PUBLICATION_PROPERTIES, properties);
            jobDataMap.put(PublicationJob.PUBLICATION_COMMENTS, comments);
            jobDataMap.put(PublicationJob.PUBLICATION_UUIDS, uuids);
            jobDataMap.put(PublicationJob.PUBLICATION_PATHS, publicationPath);
            jobDataMap.put(PublicationJob.SOURCE, workspaceName);
            jobDataMap.put(PublicationJob.DESTINATION, Constants.LIVE_WORKSPACE);
            jobDataMap.put(PublicationJob.CHECK_PERMISSIONS, true);

            ServicesRegistry.getInstance().getSchedulerService().scheduleJobNow(jobDetail);
        } catch (SchedulerException e) {
            logger.error("repository exception", e);
            throw new GWTJahiaServiceException("Cannot get publish nodes " + uuids + ". Cause: " + e.getLocalizedMessage(), e);
        }
    }


    /**
     * Publish a node to live workspace immediately.
     *
     *
     * @param uuids     uuids of the nodes to publish
     * @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException
     *          in case of any RepositoryException
     */
    public void publish(List<String> uuids) throws GWTJahiaServiceException {
        try {
            publicationService.publish(uuids, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null);
        } catch (RepositoryException e) {
            logger.error("repository exception", e);
            throw new GWTJahiaServiceException("Cannot get publish nodes " + uuids + ". Cause: " + e.getLocalizedMessage(), e);
        }
    }

    /**
     * Unpublish a node from live workspace.
     * Referenced Node will not be unpublished.
     *
     * @param uuids     uuids of the nodes to unpublish
     * @param languages Set of languages to unpublish if null unpublish all languages
     * @param user      the user for obtaining the jcr session
     * @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException
     *          in case of any RepositoryException
     */
    public void unpublish(List<String> uuids, Set<String> languages, JahiaUser user) throws GWTJahiaServiceException {
        try {
            sendPushNotiticationEvent(publicationService.unpublish(uuids));
        } catch (RepositoryException e) {
            logger.error("repository exception", e);
            throw new GWTJahiaServiceException("Cannot get unpublish nodes " + uuids + ". Cause: " + e.getLocalizedMessage(), e);
        }
    }

    private void sendPushNotiticationEvent(List<String> uuids) {
        BroadcasterFactory broadcasterFactory = AtmosphereServlet.getBroadcasterFactory();
        if (broadcasterFactory != null) {
            Broadcaster broadcaster = broadcasterFactory.lookup(ManagedGWTResource.GWT_BROADCASTER_ID);
            if (broadcaster != null) {
                // we send an event only in case there are active broadcasters (listeners) registered and we do not propagate this to cluster
                broadcaster.broadcast(new ContentUnpublishedEvent(uuids));
            }
        }
    }
}
