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

package org.jahia.services.content.files;

import org.jahia.services.content.*;
import org.jahia.services.render.filter.cache.ModuleCacheProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Listener for flushing file content cache
 *
 * @author toto
 */
public class FileCacheListener extends DefaultEventListener {
    private static Logger logger = LoggerFactory.getLogger(FileCacheListener.class);

    private FileCacheManager cacheManager;
    private ModuleCacheProvider moduleCacheProvider;

    public FileCacheListener() {
        cacheManager = FileCacheManager.getInstance();
        moduleCacheProvider = ModuleCacheProvider.getInstance();
    }

    public int getEventTypes() {
        return Event.NODE_ADDED + Event.NODE_REMOVED + Event.NODE_MOVED + Event.PROPERTY_ADDED +
               Event.PROPERTY_CHANGED + Event.PROPERTY_REMOVED;
    }

    public void onEvent(EventIterator eventIterator) {
        final Set<String> nodes = new HashSet<String>();
        while (eventIterator.hasNext()) {
            Event event = eventIterator.nextEvent();
            try {
                if (isExternal(event)) {
                    continue;
                }

                String path = event.getPath();
                String parentPath = path.substring(0, path.lastIndexOf('/'));
                String name = path.substring(path.lastIndexOf('/') + 1);
                String parentName = parentPath.substring(parentPath.lastIndexOf('/') + 1);
                if ((event.getType() == Event.NODE_ADDED || event.getType() == Event.NODE_REMOVED) && name.equals(
                        "j:acl")) {
                    nodes.add(parentPath);
                }
                if ((event.getType() == Event.PROPERTY_ADDED || event.getType() == Event.PROPERTY_CHANGED) &&
                    parentName.equals("j:acl")) {
                    parentPath = parentPath.substring(0, parentPath.lastIndexOf('/'));
                    nodes.add(parentPath);
                }
                if ((event.getType() == Event.PROPERTY_ADDED || event.getType() == Event.PROPERTY_CHANGED) &&
                    parentName.equals("jcr:content")) {
                    parentPath = parentPath.substring(0, parentPath.lastIndexOf('/'));
                    nodes.add(parentPath);
                }
                if ((event.getType() == Event.NODE_REMOVED) && name.indexOf(':') == -1) {
                    nodes.add(path);
                }
                if (event.getType() == Event.NODE_MOVED) {
                    Map eventInfo = event.getInfo();
                    if (eventInfo != null) {
                        Object absPath = eventInfo.get("srcAbsPath");
                        if (absPath != null) {
                            final String srcAbsPath = absPath.toString();
                            final String destAbsPath = eventInfo.get("destAbsPath").toString();
                            flushSubNodes(destAbsPath, srcAbsPath, destAbsPath);
                        }
                    }
                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
        if (!nodes.isEmpty()) {
            try {
                JCRTemplate.getInstance().doExecuteWithSystemSession(null, workspace, new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        for (String s : nodes) {
                            try {
                                JCRNodeWrapper n = (JCRNodeWrapper) session.getItem(s);
                                cacheManager.invalidate(workspace, n.getPath());
                            } catch (Exception e) {
                                cacheManager.invalidate(workspace, s);
                            }
                        }
                        return null;
                    }
                });
            } catch (PathNotFoundException e) {
                logger.debug("Path not found", e);
            } catch (RepositoryException e) {
                logger.error("Error while accessing repository", e);
            }
        }
    }

    private void flushSubNodes(final String nodePath, final String srcAbsPath, final String destAbsPath) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(null, workspace, new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                try {
                    JCRNodeWrapper n = (JCRNodeWrapper) session.getItem(nodePath);
                    NodeIterator nodeIterator = n.getNodes();
                    while (nodeIterator.hasNext()) {
                        JCRNodeWrapper next = (JCRNodeWrapper) nodeIterator.next();
                        String path = next.getPath();
                        cacheManager.invalidate(workspace, path);
                        moduleCacheProvider.invalidate(path);
                        String replace = path.replace(destAbsPath, srcAbsPath);
                        cacheManager.invalidate(workspace, replace);
                        moduleCacheProvider.invalidate(replace);
                        flushSubNodes(path,srcAbsPath, destAbsPath);
                    }
                } catch (Exception e) {
                    cacheManager.invalidate(workspace, srcAbsPath);
                    cacheManager.invalidate(workspace, nodePath);
                    moduleCacheProvider.invalidate(nodePath);
                    moduleCacheProvider.invalidate(srcAbsPath);
                }
                return null;
            }
        });
    }

}
