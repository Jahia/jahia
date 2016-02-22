/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
import org.jahia.services.usermanager.JahiaUser;
import org.slf4j.Logger;
import static org.jahia.api.Constants.*;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import java.util.HashSet;
import java.util.Set;

/**
 * Listener implementation used to update node name property when a node is added/moved/renamed.
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
            JahiaUser user = ((JCREventIterator)eventIterator).getSession().getUser();
            JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(user, workspace, null, new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    final Set<Session> sessions = new HashSet<Session>();

                    while (eventIterator.hasNext()) {
                        Event event = eventIterator.nextEvent();

                        if (isExternal(event)) {
                            continue;
                        }

                        String path = event.getPath();
                        if (event.getType() == Event.NODE_ADDED) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Node has been added, we are updating its fullpath properties : " + path);
                            }
                            try {
                                JCRNodeWrapper item = (JCRNodeWrapper) session.getItem(path);
                                if (nodeAdded(item)) {
                                    sessions.add(item.getRealNode().getSession());
                                }
                            } catch (PathNotFoundException e) {
                                logger.debug("Node does not exist, continue");
                            }
                        }
                    }
                    for (Session jcrsession : sessions) {
                        jcrsession.save();
                    }
                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

    }

    private boolean nodeAdded(JCRNodeWrapper node) throws RepositoryException {
        boolean updated = false;
        if (node.isNodeType(JAHIAMIX_NODENAMEINFO)
                && (!node.hasProperty(NODENAME) || !StringUtils.equals(node.getProperty(NODENAME).getString(),
                        node.getName()))) {
            try {
                if (!node.isCheckedOut()) {
                    node.checkout();
                }
                node.setProperty(NODENAME, node.getName());
                updated = true;
                if (logger.isDebugEnabled() && !node.isNew()) {
                    logger.debug("Node has been added, we are updating its name " + node.getName() + ")");

                }
            } catch (UnsupportedRepositoryOperationException e) {
                // Ignore
            }
        }

        return updated;
    }

}
