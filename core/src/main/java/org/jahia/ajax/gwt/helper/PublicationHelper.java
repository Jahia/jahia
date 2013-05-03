/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.helper;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
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
import org.jahia.services.workflow.WorkflowRule;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.utils.i18n.Messages;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;

/**
 * User: toto
 * Date: Sep 28, 2009
 * Time: 2:15:34 PM
 */
public class PublicationHelper {
    private static Logger logger = LoggerFactory.getLogger(PublicationHelper.class);

    private JCRPublicationService publicationService;
    private WorkflowHelper workflowHelper;
    private WorkflowService workflowService;
	private HttpClientService httpClientService;

    public void setPublicationService(JCRPublicationService publicationService) {
        this.publicationService = publicationService;
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
     *
     *
     * @param node               to get publication info from
     * @param currentUserSession
     * @param includesReferences
     * @param includesSubnodes
     * @return a GWTJahiaPublicationInfo object filled with the right status for the publication state of this path
     * @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException
     *          in case of any RepositoryException
     */
    public Map<String,GWTJahiaPublicationInfo> getAggregatedPublicationInfosByLanguage(JCRNodeWrapper node, Set<String> languages, JCRSessionWrapper currentUserSession, boolean includesReferences, boolean includesSubnodes) throws GWTJahiaServiceException {
        try {
            HashMap<String, GWTJahiaPublicationInfo> infos = new HashMap<String, GWTJahiaPublicationInfo>(languages.size());
            PublicationInfo pubInfo = publicationService.getPublicationInfo(node.getIdentifier(), languages, includesReferences, includesSubnodes, false, currentUserSession.getWorkspace().getName(), Constants.LIVE_WORKSPACE).get(0);
            if (!includesSubnodes) {
                // We don't include subnodes, but we still need the translation nodes to get the correct status
                final JCRSessionWrapper unlocalizedSession = JCRSessionFactory.getInstance().getCurrentUserSession();
                NodeIterator ni = unlocalizedSession.getNodeByIdentifier(node.getIdentifier()).getNodes(JCRNodeWrapperImpl.TRANSLATION_NODES_PATTERN);
                while (ni.hasNext()) {
                    JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
                    PublicationInfo translationInfo = publicationService.getPublicationInfo(next.getIdentifier(), languages, includesReferences, false, false, currentUserSession.getWorkspace().getName(), Constants.LIVE_WORKSPACE).get(0);
                    pubInfo.getRoot().addChild(translationInfo.getRoot());
                }
            }

            for (String language : languages) {
                GWTJahiaPublicationInfo gwtInfo = new GWTJahiaPublicationInfo(pubInfo.getRoot().getUuid(), pubInfo.getRoot().getStatus());
//                if (pubInfo.getRoot().isLocked()  ) {
//                gwtInfo.setLocked(true);
//                }
                String translationNodeName = pubInfo.getRoot().getChildren().size() > 0 ? "/j:translation_"+language : null;
                for (PublicationInfoNode sub : pubInfo.getRoot().getChildren()) {
                    if (sub.getPath().contains(translationNodeName)) {
                        if (sub.getStatus() > gwtInfo.getStatus()) {
                            gwtInfo.setStatus(sub.getStatus());
                        }
                        if (gwtInfo.getStatus() == GWTJahiaPublicationInfo.UNPUBLISHED && sub.getStatus() != GWTJahiaPublicationInfo.UNPUBLISHED) {
                            gwtInfo.setStatus(sub.getStatus());
                        }
                        if (sub.isLocked()) {
                            gwtInfo.setLocked(true);
                        }
                    }
                }

                gwtInfo.setIsAllowedToPublishWithoutWorkflow(node.hasPermission("publish"));
                gwtInfo.setIsNonRootMarkedForDeletion(gwtInfo.getStatus() == GWTJahiaPublicationInfo.MARKED_FOR_DELETION && !node.isNodeType("jmix:markedForDeletionRoot"));

                if (gwtInfo.getStatus() == GWTJahiaPublicationInfo.PUBLISHED) {
                    // the item status is published: check if the tree status or references are modified or unpublished
                    Set<Integer> status = pubInfo.getTreeStatus(language);
                    boolean overrideStatus = !status.isEmpty()
                            && Collections.max(status) > GWTJahiaPublicationInfo.PUBLISHED;
                    if (!overrideStatus) {
                        // check references
                        for (PublicationInfo refInfo : pubInfo.getAllReferences()) {
                            status = refInfo.getTreeStatus(language);
                            if (!status.isEmpty() && Collections.max(status) > GWTJahiaPublicationInfo.PUBLISHED) {
                                overrideStatus = true;
                                break;
                            }
                        }
                    }
                    if (overrideStatus) {
                        gwtInfo.setStatus(GWTJahiaPublicationInfo.MODIFIED);
                    }
                }

                infos.put(language, gwtInfo);
            }
            return infos;
        } catch (RepositoryException e) {
            logger.error("repository exception", e);
            throw new GWTJahiaServiceException(e.getMessage());
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
            if (!checkForUnpublication) {
                List<PublicationInfo> infos = publicationService.getPublicationInfos(uuids, languages, true, true, allSubTree, currentUserSession.getWorkspace().getName(), Constants.LIVE_WORKSPACE);
                for (PublicationInfo info : infos) {
                    info.clearInternalAndPublishedReferences(uuids);
                }
                LinkedHashMap<String, GWTJahiaPublicationInfo> res = new LinkedHashMap<String, GWTJahiaPublicationInfo>();
                for (String language : languages) {
                    final List<GWTJahiaPublicationInfo> infoList = convert(infos, currentUserSession, language, "publish");
                    String lastGroup = null;
                    String lastTitle = null;
                    Locale l = new Locale(language);
                    for (GWTJahiaPublicationInfo info : infoList) {
                        if ((info.isPublishable() && (info.getWorkflowDefinition() != null || info.isAllowedToPublishWithoutWorkflow()))) {
                            res.put(language + "/" + info.getUuid(), info);
                            if (lastGroup == null || !info.getWorkflowGroup().equals(lastGroup)) {
                                lastGroup = info.getWorkflowGroup();
                                lastTitle = info.getTitle() + " ( " + l.getDisplayName(l) + " )";
                            }
                            info.setWorkflowTitle(lastTitle);
                        }
                    }
                }
                return new ArrayList<GWTJahiaPublicationInfo>(res.values());
            } else {
                List<PublicationInfo> infos = publicationService.getPublicationInfos(uuids, null, false, true, allSubTree, currentUserSession.getWorkspace().getName(), Constants.LIVE_WORKSPACE);
                LinkedHashMap<String, GWTJahiaPublicationInfo> res = new LinkedHashMap<String, GWTJahiaPublicationInfo>();
                for (String language : languages) {
                    final List<GWTJahiaPublicationInfo> infoList = convert(infos, currentUserSession, language, "unpublish");
                    String lastGroup = null;
                    String lastTitle = null;
                    Locale l = new Locale(language);
                    for (GWTJahiaPublicationInfo info : infoList) {
                        if ((info.getStatus() == GWTJahiaPublicationInfo.PUBLISHED && (info.getWorkflowDefinition() != null || info.isAllowedToPublishWithoutWorkflow()))) {
                            res.put(language + "/" + info.getUuid(), info);
                            if (lastGroup == null || !info.getWorkflowGroup().equals(lastGroup)) {
                                lastGroup = info.getWorkflowGroup();
                                lastTitle = info.getTitle() + " ( " + l.getDisplayName(l) + " )";
                            }
                            info.setWorkflowTitle(lastTitle);
                        }
                    }
                }
                for (PublicationInfo info : infos) {
                    Set<String> publishedLanguages = info.getAllPublishedLanguages();
                    if (!languages.containsAll(publishedLanguages)) {
                        keepOnlyTranslation(res);
                    }
                }
                return new ArrayList<GWTJahiaPublicationInfo>(res.values());
            }
        } catch (RepositoryException e) {
            logger.error("repository exception", e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public void keepOnlyTranslation(LinkedHashMap<String, GWTJahiaPublicationInfo> all) throws RepositoryException {
        Set<String> keys = new HashSet<String>(all.keySet());

        for (String key : keys) {
            GWTJahiaPublicationInfo gwtinfo = all.get(key);
            if (gwtinfo.getI18nUuid() == null) {
                all.remove(key);
            } else {
                gwtinfo.remove("uuid");
            }
        }
    }

    private List<GWTJahiaPublicationInfo> convert(List<PublicationInfo> pubInfos, JCRSessionWrapper currentUserSession,
                                                  String language, String workflowAction) throws RepositoryException {

        List<GWTJahiaPublicationInfo> gwtInfos = new ArrayList<GWTJahiaPublicationInfo>();
        List<String> mainPaths = new ArrayList<String>();
        for (PublicationInfo pubInfo : pubInfos) {
            final Collection<GWTJahiaPublicationInfo> infoCollection =
                    (Collection<GWTJahiaPublicationInfo>) convert(pubInfo, pubInfo.getRoot(), mainPaths,
                            currentUserSession, language, workflowAction).values();
            gwtInfos.addAll(infoCollection);
        }
        return gwtInfos;
    }

    private Map<String, GWTJahiaPublicationInfo> convert(PublicationInfo pubInfo, PublicationInfoNode root, List<String> mainPaths, JCRSessionWrapper currentUserSession, String language, String workflowAction) {
        Map<String, GWTJahiaPublicationInfo> gwtInfos = new LinkedHashMap<String, GWTJahiaPublicationInfo>();
        return convert(pubInfo, root, mainPaths, currentUserSession, language, gwtInfos, workflowAction);
    }

    private Map<String, GWTJahiaPublicationInfo> convert(PublicationInfo pubInfo, PublicationInfoNode root, List<String> mainPaths, JCRSessionWrapper currentUserSession, String language, Map<String, GWTJahiaPublicationInfo> gwtInfos, String workflowAction) {
        PublicationInfoNode node = pubInfo.getRoot();
        List<PublicationInfo> references = new ArrayList<PublicationInfo>();

        convert(gwtInfos, root, mainPaths, null, node, references, currentUserSession, language, workflowAction);

        Map<String, GWTJahiaPublicationInfo> res = new LinkedHashMap<String, GWTJahiaPublicationInfo>();

        res.putAll(gwtInfos);
        for (PublicationInfo pi : references) {
            if (!gwtInfos.containsKey(pi.getRoot().getUuid())) {
                res.putAll(convert(pi, pi.getRoot(), mainPaths, currentUserSession, language, gwtInfos, workflowAction));
            }
        }
        return res;
    }

    private GWTJahiaPublicationInfo convert(Map<String, GWTJahiaPublicationInfo> all, PublicationInfoNode root, List<String> mainPaths,
                                            WorkflowRule lastRule, PublicationInfoNode node, List<PublicationInfo> references,
                                            JCRSessionWrapper currentUserSession, String language, String workflowAction) {
        GWTJahiaPublicationInfo gwtInfo = new GWTJahiaPublicationInfo(node.getUuid(), node.getStatus());
        try {
            JCRNodeWrapper jcrNode;
            if (node.getStatus() == PublicationInfo.DELETED) {
                JCRSessionWrapper liveSession = JCRTemplate.getInstance().getSessionFactory().getCurrentUserSession("live", currentUserSession.getLocale(), currentUserSession.getFallbackLocale());
                jcrNode = liveSession.getNodeByUUID(node.getUuid());
            } else {
                jcrNode = currentUserSession.getNodeByUUID(node.getUuid());
                if (lastRule == null || jcrNode.hasNode(WorkflowService.WORKFLOWRULES_NODE_NAME)) {
                    WorkflowRule rule = workflowService.getWorkflowRuleForAction(jcrNode, false, workflowAction, null);
                    if (rule != null) {
                        if (!rule.equals(lastRule)) {
                            if (workflowService.getWorkflowRuleForAction(jcrNode, true, workflowAction, null) != null) {
                                lastRule = rule;
                            } else {
                                lastRule = null;
                            }
                        }
                    }
                }
            }
            if (jcrNode.hasProperty("jcr:title")) {
                gwtInfo.setTitle(jcrNode.getProperty("jcr:title").getString());
            } else {
                gwtInfo.setTitle(jcrNode.getName());
            }
            gwtInfo.setPath(jcrNode.getPath());
            gwtInfo.setNodetype(jcrNode.getPrimaryNodeType().getLabel(currentUserSession.getLocale()));
            gwtInfo.setIsAllowedToPublishWithoutWorkflow(jcrNode.hasPermission("publish"));
            gwtInfo.setIsNonRootMarkedForDeletion(jcrNode.isNodeType("jmix:markedForDeletion") && !jcrNode.isNodeType("jmix:markedForDeletionRoot"));
        } catch (RepositoryException e1) {
            gwtInfo.setTitle(node.getPath());
        }

        String mainPath = root.getPath();
        gwtInfo.setMainPath(mainPath);
        gwtInfo.setMainUUID(root.getUuid());
        gwtInfo.setLanguage(language);
        if (!mainPaths.contains(mainPath)) {
            mainPaths.add(mainPath);
        }
        gwtInfo.setMainPathIndex(mainPaths.indexOf(mainPath));
        Map<String, GWTJahiaPublicationInfo> gwtInfos = new HashMap<String, GWTJahiaPublicationInfo>();
        gwtInfos.put(node.getPath(), gwtInfo);
        List<String> refUuids = new ArrayList<String>();
//        if (node.isLocked()  ) {
//            gwtInfo.setLocked(true);
//        }

        all.put(node.getUuid(), gwtInfo);


        if (lastRule != null) {
            gwtInfo.setWorkflowGroup(language + lastRule.getDefinitionPath());
            gwtInfo.setWorkflowDefinition(lastRule.getProviderKey()+":"+lastRule.getWorkflowDefinitionKey());
        } else {
            gwtInfo.setWorkflowGroup("no-workflow");
        }

        String translationNodeName = node.getChildren().size() > 0 ? "/j:translation_" + language : null;
        for (PublicationInfoNode sub : node.getChildren()) {
            if (sub.getPath().contains(translationNodeName)) {
                String key = StringUtils.substringBeforeLast(sub.getPath(), "/j:translation");
                GWTJahiaPublicationInfo lastPub = gwtInfos.get(key);
                if (lastPub != null) {
                    if (sub.getStatus() > lastPub.getStatus()) {
                        lastPub.setStatus(sub.getStatus());
                    }
                    if (lastPub.getStatus() == GWTJahiaPublicationInfo.UNPUBLISHED && sub.getStatus() != GWTJahiaPublicationInfo.UNPUBLISHED) {
                        lastPub.setStatus(sub.getStatus());
                    }
                    if (sub.isLocked()) {
                        gwtInfo.setLocked(true);
                    }
                    lastPub.setI18NUuid(sub.getUuid());
                }
//                references.addAll(sub.getReferences());
                for (PublicationInfo pi : sub.getReferences()) {
                    if (!refUuids.contains(pi.getRoot().getUuid()) && !all.containsKey(pi.getRoot().getUuid())) {
                        refUuids.add(pi.getRoot().getUuid() );
                        all.putAll(convert(pi, pi.getRoot(), mainPaths, currentUserSession, language, all, workflowAction));
                    }
                }

            }
        }
        references.addAll(node.getReferences());

        for (PublicationInfo pi : node.getReferences()) {
            if (!refUuids.contains(pi.getRoot().getUuid())) {
                refUuids.add(pi.getRoot().getUuid());
                if (!mainPaths.contains(pi.getRoot().getPath()) && !all.containsKey(pi.getRoot().getUuid())) {
                    all.putAll(convert(pi, pi.getRoot(), mainPaths, currentUserSession, language, all, workflowAction));
                }
            }
        }

        // Move node after references
        all.remove(node.getUuid());
        all.put(node.getUuid(), gwtInfo);

        for (PublicationInfoNode sub : node.getChildren()) {
            if (sub.getPath().indexOf("/j:translation") == -1) {
                convert(all, root, mainPaths, lastRule, sub, references, currentUserSession, language, workflowAction);
            }
        }


        return gwtInfo;
    }


    public Map<PublicationWorkflow, WorkflowDefinition> createPublicationWorkflows(List<GWTJahiaPublicationInfo> all) {
        final TreeMap<String, List<GWTJahiaPublicationInfo>> infosListByWorflowGroup = new TreeMap<String, List<GWTJahiaPublicationInfo>>();

        Map<String,String> workflowGroupToKey = new HashMap<String, String>();
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

        Map<String,WorkflowDefinition> workflows = new HashMap<String,WorkflowDefinition>();
        for (String wf : keys) {
            WorkflowDefinition w = workflowService.getWorkflowDefinition(StringUtils.substringBefore(wf,":"), StringUtils.substringAfter(wf,":"),null);
            workflows.put(wf, w);
        }

        for (Map.Entry<String, List<GWTJahiaPublicationInfo>> entry : infosListByWorflowGroup.entrySet()) {
            result.put(new PublicationWorkflow(entry.getValue()),workflows.get(workflowGroupToKey.get(entry.getKey())));
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

            JobDetail jobDetail = BackgroundJob.createJahiaJob("Publication", PublicationJob.class);
            JobDataMap jobDataMap = jobDetail.getJobDataMap();
            jobDataMap.put(BackgroundJob.JOB_SITEKEY, site.getName());
            jobDataMap.put(PublicationJob.PUBLICATION_PROPERTIES, properties);
            jobDataMap.put(PublicationJob.PUBLICATION_COMMENTS, comments);
            jobDataMap.put(PublicationJob.PUBLICATION_UUIDS, uuids);
            jobDataMap.put(PublicationJob.SOURCE, workspaceName);
            jobDataMap.put(PublicationJob.DESTINATION, Constants.LIVE_WORKSPACE);
            jobDataMap.put(PublicationJob.CHECK_PERMISSIONS, true);

            ServicesRegistry.getInstance().getSchedulerService().scheduleJobNow(jobDetail);
        } catch (SchedulerException e) {
            logger.error("repository exception", e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }


    /**
     * Publish a node to live workspace immediately.
     *
     *
     * @param uuids     uuids of the nodes to unpublish
     * @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException
     *          in case of any RepositoryException
     */
    public void publish(List<String> uuids) throws GWTJahiaServiceException {
        try {
            publicationService.publish(uuids, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null);
        } catch (RepositoryException e) {
            logger.error("repository exception", e);
            throw new GWTJahiaServiceException(e.getMessage());
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
            throw new GWTJahiaServiceException(e.getMessage());
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
			if (httpClientService.getHttpClient().executeMethod(null, post, state) != 200) {
				logger.warn("Connection to URL: {} failed with status {}", url,
				        post.getStatusLine());
				throw new GWTJahiaServiceException(
				        Messages.getInternalWithArguments("label.gwt.error.connection.failed.with.the.status",uiLocale, post.getStatusLine()));
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
			        Messages.getInternalWithArguments("label.gwt.error.connection.failed.with.the.an.error",uiLocale, e.getMessage()));
		} catch (IOException e) {
			logger.error(
			        "Unable to get the content of the URL: " + url + ". Cause: " + e.getMessage(),
			        e);
			throw new GWTJahiaServiceException(
			        Messages.getInternalWithArguments("label.gwt.error.connection.failed.with.the.an.error",uiLocale, e.getMessage()));
		} finally {
			if (post != null) {
				post.releaseConnection();
			}
		}
	}

	public void setHttpClientService(HttpClientService httpClientService) {
		this.httpClientService = httpClientService;
	}

}
