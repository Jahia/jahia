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

package org.jahia.services.content;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.jahia.api.Constants;

import javax.jcr.*;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;

import java.util.*;

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

    public String getPath() {
        return "/";
    }

    public String[] getNodeTypes() {
        return new String[] { Constants.MIX_LAST_MODIFIED };
    }

    public void onEvent(final EventIterator eventIterator) {
        try {
            final String userId = ((JCREventIterator)eventIterator).getSession().getUserID();
            JCRTemplate.getInstance().doExecuteWithSystemSession(userId, workspace, new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    Set<String> nodes = new HashSet<String>();
                    Set<String> addedNodes = new HashSet<String>();
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
                            if(!path.contains("j:translation")) {
                                nodes.add(StringUtils.substringBeforeLast(path,"/"));
                            }
                        }
                        else {
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
                                updateProperty(n, c, userId);
                            } catch (PathNotFoundException e) {
                                // node has been removed
                            }
                        }
                        for (String addedNode : addedNodes) {
                            try {
                                JCRNodeWrapper n = session.getNode(addedNode);
                                if (!n.hasProperty("j:originWS") && n.isNodeType("jmix:originWS")) {
                                    n.setProperty("j:originWS", workspace);
                                }
                                updateProperty(n, c, userId);
                            } catch (PathNotFoundException e) {
                                // node has been removed
                            }
                        }
                        session.save();
                    }
                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

    }

    private void updateProperty(JCRNodeWrapper n, Calendar c, String userId) throws RepositoryException {
        while (!n.isNodeType(Constants.MIX_LAST_MODIFIED)) {
            try {
                n = n.getParent();
            } catch (ItemNotFoundException e) {
                return;
            }
        }
        if (!n.isCheckedOut()) {
            n.checkout();
        }
        n.setProperty("jcr:lastModified",c);
        n.setProperty("jcr:lastModifiedBy",userId);
    }

    private void handleTranslationNodes(final JCRNodeWrapper node, final Calendar c,
            final String userId) throws RepositoryException {
        NodeIterator ni = node.getNodes("j:translation*");

        while (ni.hasNext()) {
            Node translation = ni.nextNode();
            if (!translation.isCheckedOut()) {
                translation.getSession().getWorkspace().getVersionManager()
                        .checkout(translation.getPath());
            }
            translation.setProperty(Constants.JCR_LASTMODIFIED, c);
            translation.setProperty(Constants.JCR_LASTMODIFIEDBY, userId);
        }
    }
}