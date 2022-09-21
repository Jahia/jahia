/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.usermanager.JahiaUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import java.util.*;

import static org.jahia.api.Constants.*;

/**
 * Listener implementation used to update last modification date.
 * User: toto
 * Date: Feb 15, 2010
 * Time: 2:36:05 PM
 */
public class LastModifiedListener extends DefaultEventListener {
    public static final String JMIX_AUTO_PUBLISH = "jmix:autoPublish";
    public static final String J_DELETED_CHILDREN = "j:deletedChildren";
    private static Logger logger = LoggerFactory.getLogger(LastModifiedListener.class);

    private JCRPublicationService publicationService;

    public int getEventTypes() {
        return Event.NODE_ADDED + Event.NODE_REMOVED + Event.PROPERTY_CHANGED + Event.PROPERTY_ADDED + Event.PROPERTY_REMOVED + Event.NODE_MOVED;
    }

    public void onEvent(final EventIterator eventIterator) {
        try {
            final JahiaUser user = ((JCREventIterator)eventIterator).getSession().getUser();
            final int type = ((JCREventIterator)eventIterator).getOperationType();

            if (type == JCRObservationManager.NODE_CHECKOUT || type == JCRObservationManager.NODE_CHECKIN) {
                return;
            }

            final Set<Session> sessions = new LinkedHashSet<>();
            final Set<String> nodes = new LinkedHashSet<>();
            final Set<String> addedNodes = new LinkedHashSet<>();
            final Set<String> reorderedNodes = new LinkedHashSet<>();
            final List<String> autoPublishedIds;

            if (workspace.equals("default")) {
                autoPublishedIds = new ArrayList<String>();
            } else {
                autoPublishedIds = null;
            }

            JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(user, workspace, null, new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    Calendar c = Calendar.getInstance();
                    while (eventIterator.hasNext()) {
                        final Event event = eventIterator.nextEvent();
                        if (event.getType() == Event.NODE_REMOVED && event.getIdentifier() != null) {
                            try {
                                session.getNodeByIdentifier(event.getIdentifier());
                            } catch (ItemNotFoundException infe) {
                                try {
                                    final JCRNodeWrapper parent = session.getNode(StringUtils.substringBeforeLast(event.getPath(), "/"));
                                    if (!session.getWorkspace().getName().equals(Constants.LIVE_WORKSPACE) && parent.getProvider().getMountPoint().equals("/")) {
                                        // Test if published and has lastPublished property
                                        boolean lastPublished = JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(user, Constants.LIVE_WORKSPACE, null, new JCRCallback<Boolean>() {
                                            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                                                JCRNodeWrapper nodeByIdentifier = session.getNodeByIdentifier(event.getIdentifier());
                                                boolean lastPublished = nodeByIdentifier.hasProperty(Constants.LASTPUBLISHED);
                                                if (lastPublished && !parent.isNodeType(JMIX_AUTO_PUBLISH)) {
                                                    List<String> nodeTypes = (event instanceof JCRObservationManager.EventWrapper) ? ((JCRObservationManager.EventWrapper) event).getNodeTypes() : null;
                                                    if (nodeTypes != null) {
                                                        for (String nodeType : nodeTypes) {
                                                            ExtendedNodeType eventNodeType = NodeTypeRegistry.getInstance().getNodeType(nodeType);
                                                            if (eventNodeType != null && eventNodeType.isNodeType(JMIX_AUTO_PUBLISH)) {
                                                                nodeByIdentifier.remove();
                                                                session.save();
                                                                return false;
                                                            }
                                                        }
                                                    }
                                                }
                                                return lastPublished;
                                            }
                                        });

                                        if (lastPublished) {
                                            if (!parent.isNodeType("jmix:deletedChildren")) {
                                                parent.addMixin("jmix:deletedChildren");
                                                parent.setProperty(J_DELETED_CHILDREN, new String[]{event.getIdentifier()});
                                            } else if (!parent.hasProperty(J_DELETED_CHILDREN)) {
                                                parent.setProperty(J_DELETED_CHILDREN, new String[]{event.getIdentifier()});
                                            } else {
                                                parent.getProperty(J_DELETED_CHILDREN).addValue(event.getIdentifier());
                                            }
                                            sessions.add(parent.getRealNode().getSession());
                                        }
                                    }
                                } catch (PathNotFoundException e) {
                                    // no parent
                                } catch (ItemNotFoundException e) {
                                    // no live
                                }
                            }
                        }

                        if (isExternal(event)) {
                            continue;
                        }

                        String path = event.getPath();
                        if (path.startsWith("/jcr:system/")) {
                            continue;
                        }
                        if ((event.getType() & Event.PROPERTY_CHANGED + Event.PROPERTY_ADDED + Event.PROPERTY_REMOVED) != 0 &&
                                propertiesToIgnore.contains(StringUtils.substringAfterLast(path, "/"))) {
                            continue;
                        }
                        if (logger.isDebugEnabled()) {
                            logger.debug("Receiving event for lastModified date for : {}", path);
                        }
                        if (event.getType() == Event.NODE_ADDED) {
                            addedNodes.add(path);
                        } else if (Event.NODE_MOVED == event.getType()) {
                            // in the case of a real node move, we won't track this as we want to have last modification
                            // properties on moved nodes so we only handle the reordering case.
                            if (event.getInfo().get("srcChildRelPath") != null) {
                                // this case is a node reordering in it's parent
                                reorderedNodes.add(path);
                            }
                            nodes.add(StringUtils.substringBeforeLast(path, "/"));
                        } else {
                            nodes.add(StringUtils.substringBeforeLast(path, "/"));
                        }
                    }
                    if (reorderedNodes.size() > 0) {
                        addedNodes.removeAll(reorderedNodes);
                    }
                    if (addedNodes.size() > 0) {
                        nodes.removeAll(addedNodes);
                    }
                    if (!nodes.isEmpty() || !addedNodes.isEmpty()) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Updating lastModified date for existing nodes : {}",
                                    Arrays.deepToString(nodes.toArray(new String[nodes.size()])));
                            logger.debug("Updating lastModified date for added nodes : {}",
                                    Arrays.deepToString(addedNodes.toArray(new String[addedNodes.size()])));
                        }
                        for (String node : nodes) {
                            try {
                                JCRNodeWrapper n = session.getNode(node);
                                sessions.add(n.getRealNode().getSession());
                                updateProperty(n, c, user, autoPublishedIds, type);
                            } catch (UnsupportedRepositoryOperationException e) {
                                // Cannot write property
                            } catch (PathNotFoundException e) {
                                // node has been removed
                            }
                        }
                        for (String addedNode : addedNodes) {
                            try {
                                JCRNodeWrapper n = session.getNode(addedNode);
                                sessions.add(n.getRealNode().getSession());
                                if (!n.hasProperty("j:originWS") && n.isNodeType("jmix:originWS")) {
                                    n.setProperty("j:originWS", workspace);
                                }
                                updateProperty(n, c, user, autoPublishedIds, type);
                            } catch (UnsupportedRepositoryOperationException e) {
                                // Cannot write property
                            } catch (PathNotFoundException e) {
                                // node has been removed
                            }
                        }
                        for (Session jcrsession : sessions) {
                            try {
                                jcrsession.save();
                            } catch (RepositoryException e) {
                                logger.debug("Cannot update lastModification properties");
                            }
                        }
                    }
                    return null;
                }
            });

            if (autoPublishedIds != null && !autoPublishedIds.isEmpty()) {
                synchronized (this) {
                    publicationService.publish(autoPublishedIds, "default", "live", false, null);
                }
            }

        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

    }

    private void updateProperty(JCRNodeWrapper n, Calendar c, JahiaUser user, List<String> autoPublished, int type) throws RepositoryException {
        while (!n.isNodeType(MIX_LAST_MODIFIED)) {
            addAutoPublish(n, autoPublished);
            try {
                n = n.getParent();
            } catch (ItemNotFoundException e) {
                return;
            }
        }

        boolean isAutoPublished = addAutoPublish(n, autoPublished);

        if (type != JCRObservationManager.IMPORT || isAutoPublished) {
            n.getSession().checkout(n);
            n.setProperty(JCR_LASTMODIFIED,c);
            n.setProperty(JCR_LASTMODIFIEDBY, user != null ? user.getUsername() : "");
            if (n.isNodeType("nt:resource")) {
                JCRNodeWrapper parent = n.getParent();
                if (parent.isNodeType(MIX_LAST_MODIFIED)) {
                    parent.setProperty(JCR_LASTMODIFIED, c);
                    parent.setProperty(JCR_LASTMODIFIEDBY, user != null ? user.getUsername() : "");
                }
            }
        }
    }

    private boolean addAutoPublish(JCRNodeWrapper n, List<String> autoPublished) throws RepositoryException {
        if (autoPublished != null &&
                !n.getPath().startsWith("/modules") &&
                !autoPublished.contains(n.getIdentifier()) &&
                (n.isNodeType(JMIX_AUTO_PUBLISH) || (n.isNodeType(JAHIANT_TRANSLATION) && n.getParent().isNodeType(JMIX_AUTO_PUBLISH)))) {
            autoPublished.add(n.getIdentifier());
            return true;
        }
        return false;
    }

    public synchronized void setPublicationService(JCRPublicationService publicationService) {
        this.publicationService = publicationService;
    }
}