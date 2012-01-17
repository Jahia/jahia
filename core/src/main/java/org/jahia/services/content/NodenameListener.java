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

import org.apache.jackrabbit.core.security.JahiaLoginModule;
import org.slf4j.Logger;
import static org.jahia.api.Constants.*;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import java.util.HashSet;
import java.util.Set;

/**
 * Listener implementation used to update node name property a node is added/moved/renamed.
 * User: toto
 * Date: Jul 21, 2008
 * Time: 2:36:05 PM
 */
public class NodenameListener extends DefaultEventListener {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(NodenameListener.class);

    public int getEventTypes() {
        return Event.NODE_ADDED;
    }

    public void onEvent(final EventIterator eventIterator) {
        try {
            String userId = ((JCREventIterator)eventIterator).getSession().getUserID();
            if (userId.startsWith(JahiaLoginModule.SYSTEM)) {
                userId = userId.substring(JahiaLoginModule.SYSTEM.length());
            }
            JCRTemplate.getInstance().doExecuteWithSystemSession(userId, workspace, new JCRCallback() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    final Set<Session> sessions = new HashSet<Session>();

                    while (eventIterator.hasNext()) {
                        Event event = eventIterator.nextEvent();

                        if (isExternal(event)) {
                            continue;
                        }

                        String path = event.getPath();
                        if (event.getType() == Event.NODE_ADDED) {
                            if(logger.isDebugEnabled()) {
                                logger.debug("Node has been added, we are updating its fullpath properties : "+path);
                            }
                            JCRNodeWrapper item = (JCRNodeWrapper) session.getItem(path);
                            nodeAdded(item);
                            sessions.add(item.getRealNode().getSession());
                        }
                    }
                    for (Session jcrsession : sessions) {
                        jcrsession.save();
                    }
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

    }

    private void nodeAdded(JCRNodeWrapper node) throws RepositoryException {
        /*if (node.isNodeType(JAHIAMIX_SHAREABLE) && node.hasProperty(FULLPATH)) {
            NodeIterator ni = node.getSharedSet();
            while (ni.hasNext()) {
                JCRNodeWrapper shared = (JCRNodeWrapper) ni.next();
                if (shared.getPath().equals(oldPath)) {
                    return;
                }
            }
        }*/
        if (node.isNodeType(JAHIAMIX_NODENAMEINFO)) {
            if (!node.isCheckedOut()) {
                node.checkout();
            }
            if (logger.isDebugEnabled() && !node.isNew()) {
                logger.debug(
                        "Node has been added, we are updating its name " +node.getName() + ")");

            }
            node.setProperty(NODENAME, node.getName());
        }
    }

}
