/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.log4j.Logger;
import static org.jahia.api.Constants.*;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

/**
 * Listener implementation used to update field references when a node is moved/renamed.
 * User: toto
 * Date: Jul 21, 2008
 * Time: 2:36:05 PM
 */
public class FullpathListener extends DefaultEventListener {
    private static Logger logger = Logger.getLogger(FullpathListener.class);

    public int getEventTypes() {
        return Event.NODE_ADDED + Event.NODE_REMOVED;
    }

    public String getPath() {
        return "/";
    }

    public String[] getNodeTypes() {
        return null;
    }

    public void onEvent(final EventIterator eventIterator) {
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    while (eventIterator.hasNext()) {
                        Event event = eventIterator.nextEvent();

                        if (isExternal(event)) {
                            continue;
                        }

                        String path = event.getPath();
                        if (event.getType() == Event.NODE_ADDED) {
                            nodeAdded((Node) session.getItem(path));
                        } else if (event.getType() == Event.NODE_REMOVED) {
                            nodeRemoved(session, path);
                        }
                    }
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

    }

    private void nodeAdded(Node node) throws RepositoryException {
        if (node.isNodeType(JAHIAMIX_SHAREABLE) && node.hasProperty(FULLPATH)) {
            String oldPath = node.getProperty(FULLPATH).getString();

            NodeIterator ni = node.getSharedSet();
            while (ni.hasNext()) {
                JCRNodeWrapper shared = (JCRNodeWrapper) ni.next();
                if (shared.getPath().equals(oldPath)) {
                    return;
                }
            }
        }
        if (node.isNodeType(JAHIAMIX_HIERARCHYNODE)) {
            if (!node.isCheckedOut()) {
                node.checkout();
            }
            node.setProperty(FULLPATH, node.getPath());
            node.setProperty("j:nodename", node.getName());
            node.save();
        }
        if (node.isNodeType(NT_FOLDER)) {
            for (NodeIterator ni = node.getNodes(); ni.hasNext();) {
                nodeAdded(ni.nextNode());
            }
        }
    }

    private void nodeRemoved(JCRSessionWrapper session, String path) throws RepositoryException {
        Query q = session.getWorkspace().getQueryManager().createQuery("select * from [jmix:shareable] as node where node.[j:fullpath]= '"+path+"'", Query.JCR_SQL2);
        QueryResult qr = q.execute();
        // todo : update shared set if removed path was in j:fullpath
    }


}
