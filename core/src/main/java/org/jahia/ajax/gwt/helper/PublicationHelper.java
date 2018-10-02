/**
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
package org.jahia.ajax.gwt.helper;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.widget.publication.PublicationWorkflow;
import org.jahia.api.Constants;
import org.jahia.bin.Render;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.notification.HttpClientService;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.workflow.WorkflowDefinition;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.utils.i18n.Messages;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.net.URL;
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
    private HttpClientService httpClientService;

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

    public void setHttpClientService(HttpClientService httpClientService) {
        this.httpClientService = httpClientService;
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

        List<GWTJahiaPublicationInfo> all = getFullPublicationInfos(uuids, languages, currentUserSession, allSubTree,
                false);

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
        try {
            Collection<ComplexPublicationService.FullPublicationInfo> infos;
            if (checkForUnpublication) {
                infos = complexPublicationService.getFullUnpublicationInfos(uuids, languages, allSubTree, currentUserSession);
            } else {
                infos = complexPublicationService.getFullPublicationInfos(uuids, languages, allSubTree, currentUserSession);
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
            publicationService.unpublish(uuids);
        } catch (RepositoryException e) {
            logger.error("repository exception", e);
            throw new GWTJahiaServiceException("Cannot get unpublish nodes " + uuids + ". Cause: " + e.getLocalizedMessage(), e);
        }
    }

    public void validateConnection(Map<String, String> props, JCRSessionWrapper jcrSession, Locale uiLocale)
            throws GWTJahiaServiceException {
        PostMethod post = null;
        URL url = null;
        try {
            String languageCode = jcrSession.getNodeByIdentifier(props.get("node")).getResolveSite().getDefaultLanguage();
            String theUrl = props.get("remoteUrl") + Render.getRenderServletPath() + "/live/" + languageCode + props.get("remotePath") + ".preparereplay.do";
            url = new URL(theUrl);
            post = new PostMethod(theUrl);
            post.addParameter("testOnly", "true");
            post.addRequestHeader("accept", "application/json");
            HttpState state = new HttpState();
            state.setCredentials(
                    new AuthScope(url.getHost(), url.getPort()),
                    new UsernamePasswordCredentials(props.get("remoteUser"), props
                            .get("remotePassword")));
            HttpClient httpClient = httpClientService.getHttpClient(theUrl);
            Credentials proxyCredentials = httpClient.getState().getProxyCredentials(AuthScope.ANY);
            if (proxyCredentials != null) {
                state.setProxyCredentials(AuthScope.ANY, proxyCredentials);
            }
            if (httpClient.executeMethod(null, post, state) != 200) {
                logger.warn("Connection to URL: {} failed with status {}", url,
                        post.getStatusLine());
                throw new GWTJahiaServiceException(
                        Messages.getInternalWithArguments("label.gwt.error.connection.failed.with.the.status", uiLocale, post.getStatusLine()));
            }
        } catch (RepositoryException e) {
            logger.error("Unable to get source node with identifier: " + props.get("node")
                    + ". Cause: " + e.getMessage(), e);
            throw new GWTJahiaServiceException(
                    Messages.getInternalWithArguments("label.gwt.error.connection.failed.with.the.an.error", uiLocale, e.getMessage()));
        } catch (HttpException e) {
            logger.error(
                    "Unable to get the content of the URL: " + url + ". Cause: " + e.getMessage(),
                    e);
            throw new GWTJahiaServiceException(
                    Messages.getInternalWithArguments("label.gwt.error.connection.failed.with.the.an.error", uiLocale, e.getMessage()));
        } catch (IOException e) {
            logger.error(
                    "Unable to get the content of the URL: " + url + ". Cause: " + e.getMessage(),
                    e);
            throw new GWTJahiaServiceException(
                    Messages.getInternalWithArguments("label.gwt.error.connection.failed.with.the.an.error", uiLocale, e.getMessage()));
        } finally {
            if (post != null) {
                post.releaseConnection();
            }
        }
    }
}
