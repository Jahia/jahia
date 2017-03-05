/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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

            final Set<Session> sessions = new HashSet<Session>();
            final Set<String> nodes = new HashSet<String>();
            final Set<String> addedNodes = new HashSet<String>();
            final Set<String> reorderedNodes = new HashSet<String>();
            final List<String> autoPublishedIds;

            if (workspace.equals("default")) {
                autoPublishedIds = new ArrayList<String>();
            } else {
                autoPublishedIds = null;
            }

            JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(user, workspace, null, new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    Calendar c = GregorianCalendar.getInstance();
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
                                                if (lastPublished && !parent.isNodeType("jmix:autoPublish")) {
                                                    List<String> nodeTypes = (event instanceof JCRObservationManager.EventWrapper) ? ((JCRObservationManager.EventWrapper) event).getNodeTypes() : null;
                                                    if (nodeTypes != null) {
                                                        for (String nodeType : nodeTypes) {
                                                            ExtendedNodeType eventNodeType = NodeTypeRegistry.getInstance().getNodeType(nodeType);
                                                            if (eventNodeType != null && eventNodeType.isNodeType("jmix:autoPublish")) {
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
                                                parent.setProperty("j:deletedChildren", new String[]{event.getIdentifier()});
                                            } else if (!parent.hasProperty("j:deletedChildren")) {
                                                parent.setProperty("j:deletedChildren", new String[]{event.getIdentifier()});
                                            } else {
                                                parent.getProperty("j:deletedChildren").addValue(event.getIdentifier());
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
                        if ((event.getType() & Event.PROPERTY_CHANGED + Event.PROPERTY_ADDED + Event.PROPERTY_REMOVED) != 0) {
                            if (propertiesToIgnore.contains(StringUtils.substringAfterLast(path, "/"))) {
                                continue;
                            }
                        }
                        if (logger.isDebugEnabled()) {
                            logger.debug("Receiving event for lastModified date for : " + path);
                        }
                        if (event.getType() == Event.NODE_ADDED) {
                            addedNodes.add(path);
//                            if(!path.contains("j:translation")) {
//                                nodes.add(StringUtils.substringBeforeLast(path,"/"));
//                            }
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
                            logger.debug("Updating lastModified date for existing nodes : " +
                                    Arrays.deepToString(nodes.toArray(new String[nodes.size()])));
                            logger.debug("Updating lastModified date for added nodes : " +
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
        if (autoPublished != null) {
            if (!n.getPath().startsWith("/modules")) {
                if (!autoPublished.contains(n.getIdentifier()) && n.isNodeType("jmix:autoPublish")) {
                    autoPublished.add(n.getIdentifier());
                    return true;
                } else if (!autoPublished.contains(n.getIdentifier()) && n.isNodeType(JAHIANT_TRANSLATION) && n.getParent().isNodeType("jmix:autoPublish")) {
                    autoPublished.add(n.getIdentifier());
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized void setPublicationService(JCRPublicationService publicationService) {
        this.publicationService = publicationService;
    }
}