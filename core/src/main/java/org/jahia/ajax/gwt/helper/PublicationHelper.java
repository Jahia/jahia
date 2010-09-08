/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.helper;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.api.Constants;
import org.jahia.services.content.*;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.workflow.WorkflowDefinition;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.services.workflow.WorkflowVariable;

import javax.jcr.RepositoryException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 28, 2009
 * Time: 2:15:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class PublicationHelper {
    private static Logger logger = Logger.getLogger(PublicationHelper.class);

    private JCRPublicationService publicationService;
    private WorkflowService workflowService;

    public void setPublicationService(JCRPublicationService publicationService) {
        this.publicationService = publicationService;
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    /**
     * Get the publication status information for a particular path.
     *
     * @param uuid               to get publication info from
     * @param currentUserSession
     * @return a GWTJahiaPublicationInfo object filled with the right status for the publication state of this path
     * @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException
     *          in case of any RepositoryException
     */
    public GWTJahiaPublicationInfo getSimplePublicationInfo(String uuid, Set<String> languages, JCRSessionWrapper currentUserSession) throws GWTJahiaServiceException {
        try {
            PublicationInfo pubInfo = publicationService.getPublicationInfo(uuid, languages, false, true, false, currentUserSession.getWorkspace().getName(), Constants.LIVE_WORKSPACE).get(0);
            GWTJahiaPublicationInfo gwtInfo = new GWTJahiaPublicationInfo(pubInfo.getRoot().getPath(), pubInfo.getRoot().getStatus(), pubInfo.getRoot().isCanPublish());
            for (PublicationInfoNode sub : pubInfo.getRoot().getChildren()) {
                if (sub.getPath().contains("/j:translation")) {
                    String key = StringUtils.substringBeforeLast(sub.getPath(), "/j:translation");
                    if (sub.getStatus() > gwtInfo.getStatus()) {
                        gwtInfo.setStatus(sub.getStatus());
                    }
                    if (gwtInfo.getStatus() == GWTJahiaPublicationInfo.UNPUBLISHED && sub.getStatus() != GWTJahiaPublicationInfo.UNPUBLISHED) {
                        gwtInfo.setStatus(sub.getStatus());
                    }
                }
            }
            gwtInfo.setSubnodesStatus(pubInfo.getTreeStatus());
            return gwtInfo;
        } catch (RepositoryException e) {
            logger.error("repository exception", e);
            throw new GWTJahiaServiceException(e.getMessage());
        }

    }

    public List<GWTJahiaPublicationInfo> getPublicationInfo(List<String> uuids, Set<String> languages,
                                                            JCRSessionWrapper currentUserSession, boolean allSubTree) throws GWTJahiaServiceException {
        try {
            List<PublicationInfo> infos = publicationService.getPublicationInfos(uuids, languages, true, true, allSubTree, currentUserSession.getWorkspace().getName(), Constants.LIVE_WORKSPACE);
            List<GWTJahiaPublicationInfo> list = convert(infos, currentUserSession);
//            List<GWTJahiaPublicationInfo> res = new ArrayList<GWTJahiaPublicationInfo>(list);
//            for (GWTJahiaPublicationInfo info : list) {
//                if (info.getStatus() == GWTJahiaPublicationInfo.PUBLISHED) {
//                    res.remove(info);
//                }
//            }
            return list;
        } catch (RepositoryException e) {
            logger.error("repository exception", e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public List<GWTJahiaPublicationInfo> convert(List<PublicationInfo> pubInfos, JCRSessionWrapper currentUserSession) {
        List<GWTJahiaPublicationInfo> gwtInfos = new ArrayList<GWTJahiaPublicationInfo>();
        for (PublicationInfo pubInfo : pubInfos) {
            PublicationInfoNode node = pubInfo.getRoot();
            gwtInfos.addAll(convert(pubInfo, pubInfo.getRoot().getPath(), currentUserSession));
        }
        return gwtInfos;
    }

    private List<GWTJahiaPublicationInfo> convert(PublicationInfo pubInfo, String mainTitle, JCRSessionWrapper currentUserSession) {
        PublicationInfoNode node = pubInfo.getRoot();
        List<GWTJahiaPublicationInfo> gwtInfos = new ArrayList<GWTJahiaPublicationInfo>();
        convert(gwtInfos, mainTitle, node, currentUserSession);
        return gwtInfos;
    }

    private GWTJahiaPublicationInfo convert(List<GWTJahiaPublicationInfo> all, String mainTitle, PublicationInfoNode node,
                                            JCRSessionWrapper currentUserSession) {
        GWTJahiaPublicationInfo gwtInfo = convert(node, currentUserSession);
        all.add(gwtInfo);
        gwtInfo.set("mainTitle", mainTitle);

        Map<String, GWTJahiaPublicationInfo> gwtInfos = new HashMap<String, GWTJahiaPublicationInfo>();
        gwtInfos.put(node.getPath(), gwtInfo);
        for (PublicationInfoNode sub : node.getChildren()) {
            if (sub.getPath().contains("/j:translation")) {
                String key = StringUtils.substringBeforeLast(sub.getPath(), "/j:translation");
                GWTJahiaPublicationInfo lastPub = gwtInfos.get(key);
                if (lastPub != null) {
                    if (sub.getStatus() > lastPub.getStatus()) {
                        lastPub.setStatus(sub.getStatus());
                    }
                    if (lastPub.getStatus() == GWTJahiaPublicationInfo.UNPUBLISHED && sub.getStatus() != GWTJahiaPublicationInfo.UNPUBLISHED) {
                        lastPub.setStatus(sub.getStatus());
                    }
                }
            } else if (sub.getPath().indexOf("/j:translation") == -1) {
                GWTJahiaPublicationInfo lastPub = convert(all, mainTitle, sub, currentUserSession);
                gwtInfos.put(lastPub.getPath(), lastPub);
            }
        }
        List<String> refUuids = new ArrayList<String>();
        for (PublicationInfo pi : node.getReferences()) {
            if (!refUuids.contains(pi.getRoot().getUuid())) {
                refUuids.add(pi.getRoot().getUuid());
                all.addAll(convert(pi, "reference", currentUserSession));
            }
        }

        return gwtInfo;
    }

    private GWTJahiaPublicationInfo convert(PublicationInfoNode node, JCRSessionWrapper currentUserSession) {
        GWTJahiaPublicationInfo gwtInfo = new GWTJahiaPublicationInfo(node.getPath(), node.getStatus(), node.isCanPublish());
        try {
            JCRNodeWrapper n = currentUserSession.getNodeByUUID(node.getUuid());
            if (n.hasProperty("jcr:title")) {
                gwtInfo.setTitle(n.getProperty("jcr:title").getString());
            } else {
                gwtInfo.setTitle(n.getName());
            }
            gwtInfo.setNodetype(n.getPrimaryNodeType().getLabel(currentUserSession.getLocale()));
        } catch (RepositoryException e) {
            gwtInfo.setTitle(node.getPath());
        }
        return gwtInfo;
    }

    /**
     * Publish a node into the live workspace.
     * Referenced nodes will also be published.
     * Parent node must be published, or will be published if publishParent is true.
     *
     * @param path      Path of the node to publish
     * @param languages Set of languages to publish if null publish all languages
     * @param reverse
     * @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException
     *          in case of any RepositoryException
     */
    public void publish(String path, Set<String> languages, boolean allSubTree, boolean reverse, JCRSessionWrapper session) throws GWTJahiaServiceException {
        try {
            if (reverse) {
                publicationService.publish(path, Constants.LIVE_WORKSPACE, session.getWorkspace().getName(), languages,
                        allSubTree);
            } else {
                publicationService.publish(path, session.getWorkspace().getName(), Constants.LIVE_WORKSPACE, languages,
                        allSubTree);
            }
        } catch (RepositoryException e) {
            logger.error("repository exception", e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    /**
     * Publish a list of nodes into the live workspace.
     * Referenced nodes will also be published.
     * Parent node must be published, or will be published if publishParent is true.
     *
     * @param uuids     list of uuids of the nodes to publish
     * @param languages Set of languages to publish if null publish all languages
     * @param workflow  @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException
     */
    public void publish(List<String> uuids, Set<String> languages, boolean allSubTree, boolean workflow, boolean reverse, JCRSessionWrapper session,
                        List<GWTJahiaNodeProperty> properties) throws GWTJahiaServiceException {
        try {
            // todo : if workflow started on untranslated node, translation will be created and not added into the publish tree calculated here 

            final String workspaceName = session.getWorkspace().getName();
            List<PublicationInfo> infos = publicationService.getPublicationInfos(uuids, languages, true, true, allSubTree,
                    workspaceName, Constants.LIVE_WORKSPACE);
            if (workflow) {
                Map<WorkflowDefinition, List<PublicationInfo>> m = new HashMap<WorkflowDefinition, List<PublicationInfo>>();

                for (PublicationInfo info : infos) {
                    if (info.needPublication()) {
                        splitWorkflows(m, info.getRoot(), null, session);
                    }
                }

                HashMap<String, Object> map = new HashMap<String, Object>();
                List<WorkflowVariable> values = new ArrayList<WorkflowVariable>();
                map.put("jcr:title", infos.get(0).getRoot().getPath());
                if (properties != null) {
                    for (GWTJahiaNodeProperty property : properties) {
                        List<GWTJahiaNodePropertyValue> propertyValues = property.getValues();
                        values = new ArrayList<WorkflowVariable>(propertyValues.size());
                        boolean toBeAdded = false;
                        for (GWTJahiaNodePropertyValue value : propertyValues) {
                            String s = value.getString();
                            if (s != null && !"".equals(s)) {
                                values.add(new WorkflowVariable(s, value.getType()));
                                toBeAdded = true;
                            }
                        }
                        if (toBeAdded) {
                            map.put(property.getName(), values);
                        } else {
                            map.put(property.getName(), new ArrayList<WorkflowVariable>());
                        }
                    }
                }
                for (Map.Entry<WorkflowDefinition, List<PublicationInfo>> entry : m.entrySet()) {
                    List<String> ids = new ArrayList<String>();
                    map.put("publicationInfos", entry.getValue());
                    for (PublicationInfo node : entry.getValue()) {
                        ids.add(node.getRoot().getUuid());
                    }
                    workflowService.startProcess(ids,session,entry.getKey().getKey(), entry.getKey().getProvider(), map);
                }
            } else {
                if (reverse) {
                    publicationService.publish(infos, Constants.LIVE_WORKSPACE, workspaceName);
                } else {
                    publicationService.publish(infos, workspaceName, Constants.LIVE_WORKSPACE);
                    String label = "published_at_"+ new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(GregorianCalendar.getInstance().getTime());
                    for (PublicationInfo publicationInfo : infos) {
                        JCRVersionService.getInstance().addVersionLabel(publicationInfo.getAllUuids(),"live_"+label,Constants.LIVE_WORKSPACE);
                        JCRVersionService.getInstance().addVersionLabel(publicationInfo.getAllUuids(),workspaceName+"_"+label,workspaceName);
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error("repository exception", e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public boolean splitWorkflows(Map<WorkflowDefinition, List<PublicationInfo>> m, PublicationInfoNode node, WorkflowDefinition currentDef, JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper n = session.getNodeByUUID(node.getUuid());
        boolean split = false;

        if (currentDef == null || n.hasNode("j:workflow")) {
            WorkflowDefinition def = workflowService.getPossibleWorkflowForAction(n, session.getUser(), "publish",null);
            if (def == null) {
                return false;
            } else {
                if (!def.equals(currentDef)) {
                    currentDef = def;
                    if (!m.containsKey(currentDef)) {
                        m.put(currentDef, new ArrayList<PublicationInfo>());
                    }
                    m.get(currentDef).add(new PublicationInfo(node));
                    split = true;
                }
            }
        }
        List<PublicationInfoNode> childSplit = new ArrayList<PublicationInfoNode>();
        for (PublicationInfoNode childNode : node.getChildren()) {
            if (splitWorkflows(m, childNode, currentDef, session)) {
                childSplit.add(childNode);
            }
        }
        node.getChildren().removeAll(childSplit);

        List<PublicationInfo> refSplit = new ArrayList<PublicationInfo>();
        for (PublicationInfo publicationNode : node.getReferences()) {
            if (publicationNode.needPublication()) {
                if (splitWorkflows(m, publicationNode.getRoot(), currentDef, session)) {
                    refSplit.add(publicationNode);
                }
            } else {
                refSplit.add(publicationNode);
            }
        }
        node.getReferences().removeAll(refSplit);
        return split;
    }


    /**
     * Unpublish a node from live workspace.
     * Referenced Node will not be unpublished.
     *
     * @param path      path of the node to unpublish
     * @param languages Set of languages to unpublish if null unpublish all languages
     * @param user      the user for obtaining the jcr session
     * @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException
     *          in case of any RepositoryException
     */
    public void unpublish(String uuid, Set<String> languages, JahiaUser user) throws GWTJahiaServiceException {
        try {
            publicationService.unpublish(uuid, languages);
        } catch (RepositoryException e) {
            logger.error("repository exception", e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }
}
