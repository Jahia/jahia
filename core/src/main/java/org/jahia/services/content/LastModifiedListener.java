/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.content;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.security.JahiaLoginModule;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.slf4j.Logger;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.ConstraintViolationException;
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
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(LastModifiedListener.class);

    public int getEventTypes() {
        return Event.NODE_ADDED + Event.NODE_REMOVED + Event.PROPERTY_CHANGED + Event.PROPERTY_ADDED + Event.PROPERTY_REMOVED + Event.NODE_MOVED;
    }

    public void onEvent(final EventIterator eventIterator) {
        try {
            String userId = ((JCREventIterator)eventIterator).getSession().getUserID();
            final int type = ((JCREventIterator)eventIterator).getOperationType();

            if (type == JCRObservationManager.NODE_CHECKOUT || type == JCRObservationManager.NODE_CHECKIN) {
                return;
            }

            if (userId.startsWith(JahiaLoginModule.SYSTEM)) {
                userId = userId.substring(JahiaLoginModule.SYSTEM.length());
            }
            final String finalUserId = userId;

            final Set<Session> sessions = new HashSet<Session>();
            final Set<String> nodes = new HashSet<String>();
            final Set<String> addedNodes = new HashSet<String>();
            final List<String> autoPublishedIds;

            if (workspace.equals("default")) {
                autoPublishedIds = new ArrayList<String>();
            } else {
                autoPublishedIds = null;
            }

            JCRTemplate.getInstance().doExecuteWithSystemSession(finalUserId, workspace, new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    Calendar c = GregorianCalendar.getInstance();
                    while (eventIterator.hasNext()) {
                        Event event = eventIterator.nextEvent();

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
                        } else {
                            nodes.add(StringUtils.substringBeforeLast(path,"/"));
                        }
                    }
                    nodes.removeAll(addedNodes);
                    if (!nodes.isEmpty() || !addedNodes.isEmpty()) {
                        if(logger.isDebugEnabled()) {
                            logger.debug("Updating lastModified date for existing nodes : "+
                                         Arrays.deepToString(nodes.toArray(new String[nodes.size()])));
                            logger.debug("Updating lastModified date for added nodes : "+
                                         Arrays.deepToString(addedNodes.toArray(new String[addedNodes.size()])));
                        }
                        for (String node : nodes) {
                            try {
                                JCRNodeWrapper n = session.getNode(node);
                                sessions.add(n.getRealNode().getSession());
                                updateProperty(n, c, finalUserId, autoPublishedIds, type);
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
                                updateProperty(n, c, finalUserId, autoPublishedIds, type);
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
                    JCRPublicationService.getInstance().publish(autoPublishedIds, "default", "live", null);
                }
            }

        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

    }

    private void updateProperty(JCRNodeWrapper n, Calendar c, String userId, List<String> autoPublished, int type) throws RepositoryException {
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
            if (!n.isCheckedOut()) {
                n.checkout();
            }
            n.setProperty(JCR_LASTMODIFIED,c);
            n.setProperty(JCR_LASTMODIFIEDBY, userId);
            if (n.isNodeType("nt:resource")) {
                JCRNodeWrapper file = n.getParent();
                file.setProperty(JCR_LASTMODIFIED, c);
                file.setProperty(JCR_LASTMODIFIEDBY, userId);
            }
        }
    }

    private boolean addAutoPublish(JCRNodeWrapper n, List<String> autoPublished) throws RepositoryException {
        if (autoPublished != null) {
            if (!autoPublished.contains(n.getIdentifier()) && n.isNodeType("jmix:autoPublish")) {
                autoPublished.add(n.getIdentifier());
                return true;
            } else if (!autoPublished.contains(n.getIdentifier()) && n.isNodeType(JAHIANT_TRANSLATION) && n.getParent().isNodeType("jmix:autoPublish")) {
                autoPublished.add(n.getIdentifier());
                return true;
            }
        }
        return false;
    }
}