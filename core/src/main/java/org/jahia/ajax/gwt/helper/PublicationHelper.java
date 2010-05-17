package org.jahia.ajax.gwt.helper;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.PublicationInfo;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.workflow.WorkflowDefinition;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.services.workflow.WorkflowVariable;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
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
    public GWTJahiaPublicationInfo getPublicationInfo(String uuid, Set<String> languages, boolean full, JCRSessionWrapper currentUserSession) throws GWTJahiaServiceException {
        try {
            PublicationInfo pubInfo = publicationService.getPublicationInfo(uuid, languages, full, true);
            return convert(pubInfo, full, currentUserSession);
        } catch (RepositoryException e) {
            logger.error("repository exception", e);
            throw new GWTJahiaServiceException(e.getMessage());
        }

    }

    private GWTJahiaPublicationInfo convert(PublicationInfo pubInfo, boolean full, JCRSessionWrapper currentUserSession) {
        GWTJahiaPublicationInfo gwtInfo = new GWTJahiaPublicationInfo(pubInfo.getPath(), pubInfo.getStatus(), pubInfo.isCanPublish());
        if (full) {
            try {
                JCRNodeWrapper n = currentUserSession.getNode(pubInfo.getPath());
                if (n.hasProperty("jcr:title")) {
                    gwtInfo.setTitle(n.getProperty("jcr:title").getString());
                } else {
                    gwtInfo.setTitle(n.getName());
                }
            } catch (RepositoryException e) {
                gwtInfo.setTitle(pubInfo.getPath());
            }
        }

        Map<String, GWTJahiaPublicationInfo> gwtInfos = new HashMap<String, GWTJahiaPublicationInfo>();
        gwtInfos.put(pubInfo.getPath(), gwtInfo);
        for (Map.Entry<String, PublicationInfo> entry : pubInfo.getSubnodes().entrySet()) {
            PublicationInfo pi = entry.getValue();
            if (entry.getKey().contains("/j:translation")) {
                String key = StringUtils.substringBeforeLast(entry.getKey(), "/j:translation");
                GWTJahiaPublicationInfo lastPub = gwtInfos.get(key);
                if (lastPub != null) {
                    if (pi.getStatus() != GWTJahiaPublicationInfo.UNPUBLISHABLE && pi.getStatus() > lastPub.getStatus()) {
                        lastPub.setStatus(pi.getStatus());
                    }
                    if (lastPub.getStatus() == GWTJahiaPublicationInfo.UNPUBLISHED && pi.getStatus() != GWTJahiaPublicationInfo.UNPUBLISHED) {
                        lastPub.setStatus(pi.getStatus());
                    }
                }
            } else if (full && entry.getKey().indexOf("/j:translation") == -1) {
                GWTJahiaPublicationInfo lastPub = convert(pi, full, currentUserSession);
                gwtInfo.add(lastPub);
                gwtInfos.put(lastPub.getPath(), lastPub);
            }
            gwtInfo.addSubnodesStatus(pi.getStatus());
        }

        for (String p : pubInfo.getReferences().keySet()) {
            PublicationInfo pi = pubInfo.getReferences().get(p);
            gwtInfo.add(convert(pi, full, currentUserSession));
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
                publicationService.publish(path, Constants.LIVE_WORKSPACE, session.getWorkspace().getName(), languages, false, allSubTree);
            } else {
                publicationService.publish(path, session.getWorkspace().getName(), Constants.LIVE_WORKSPACE, languages, false, allSubTree);
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
     * @param comments
     * @param workflow  @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException
     */
    public void publish(List<String> uuids, Set<String> languages, boolean allSubTree, String comments, boolean workflow, boolean reverse, JCRSessionWrapper session) throws GWTJahiaServiceException {
        try {
            if (workflow) {
                // todo : handle allsubtree / reverse
                Map<WorkflowDefinition, List<JCRNodeWrapper>> m = new HashMap<WorkflowDefinition, List<JCRNodeWrapper>>();
                for (String uuid : uuids) {
                    JCRNodeWrapper n = session.getNodeByUUID(uuid);
                    List<WorkflowDefinition> def = workflowService.getPossibleWorkflows(n, session.getUser(), "publish");
                    if (def.isEmpty()) {
                        publicationService.publish(n.getPath(), session.getWorkspace().getName(), Constants.LIVE_WORKSPACE, languages, false, allSubTree);
                    } else {
                        if (!m.containsKey(def.get(0))) {
                            m.put(def.get(0), new ArrayList<JCRNodeWrapper>());
                        }
                        m.get(def.get(0)).add(n);
                    }
                }

                HashMap<String, Object> map = new HashMap<String, Object>();
                List<WorkflowVariable> values = new ArrayList<WorkflowVariable>();
                values.add(new WorkflowVariable(comments, PropertyType.STRING));
                map.put("jcr:title", values);

                for (Map.Entry<WorkflowDefinition, List<JCRNodeWrapper>> entry : m.entrySet()) {
                    for (JCRNodeWrapper wrapper : entry.getValue()) {
                        workflowService.startProcess(wrapper, entry.getKey().getKey(), entry.getKey().getProvider(), map);
                    }
                }
            } else {
                for (String uuid : uuids) {
                    JCRNodeWrapper n = session.getNodeByUUID(uuid);
                    if (reverse) {
                        publicationService.publish(n.getPath(), Constants.LIVE_WORKSPACE, session.getWorkspace().getName(), languages, false, allSubTree);
                    } else {
                        publicationService.publish(n.getPath(), session.getWorkspace().getName(), Constants.LIVE_WORKSPACE, languages, false, allSubTree);
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error("repository exception", e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
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
    public void unpublish(String path, Set<String> languages, JahiaUser user) throws GWTJahiaServiceException {
        try {
            publicationService.unpublish(path, languages);
        } catch (RepositoryException e) {
            logger.error("repository exception", e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }
}
