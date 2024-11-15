/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content.files;

import org.jahia.services.content.*;
import org.jahia.services.render.filter.cache.ModuleCacheProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.NodeIterator;
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
    }

    public void setCacheManager(FileCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void setModuleCacheProvider(ModuleCacheProvider moduleCacheProvider) {
        this.moduleCacheProvider = moduleCacheProvider;
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
                String path = event.getPath();
                String parentPath = path.substring(0, path.lastIndexOf('/'));
                String name = path.substring(path.lastIndexOf('/') + 1);
                String parentName = parentPath.substring(parentPath.lastIndexOf('/') + 1);
                if ((event.getType() == Event.NODE_ADDED || event.getType() == Event.NODE_REMOVED) && name.equals(
                        "j:acl")) {
                    nodes.add(parentPath);
                }
                if ((event.getType() == Event.PROPERTY_ADDED || event.getType() == Event.PROPERTY_CHANGED
                        || event.getType() == Event.NODE_ADDED || event.getType() == Event.NODE_REMOVED)
                        && parentName.equals("j:acl")) {
                    parentPath = parentPath.substring(0, parentPath.lastIndexOf('/'));
                    nodes.add(parentPath);
                }
                if ((event.getType() == Event.PROPERTY_ADDED || event.getType() == Event.PROPERTY_CHANGED) &&
                    parentName.equals("jcr:content")) {
                    parentPath = parentPath.substring(0, parentPath.lastIndexOf('/'));
                    nodes.add(parentPath);
                }
                if ((event.getType() == Event.PROPERTY_ADDED || event.getType() == Event.PROPERTY_CHANGED) &&
                    parentName.equals("j:conditionalVisibility")) {
                    parentPath = parentPath.substring(0, parentPath.lastIndexOf('/'));
                    nodes.add(parentPath);
                }
                if (event.getType() == Event.PROPERTY_CHANGED && name.equals("j:published")) {
                    nodes.add(parentPath);
                }
                if ((event.getType() == Event.NODE_REMOVED) && name.indexOf(':') == -1) {
                    nodes.add(path);
                }
                if (event.getType() == Event.NODE_MOVED) {
                    @SuppressWarnings("rawtypes")
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
            if (logger.isDebugEnabled()) {
                logger.debug("Flushing file caches for {} node(s) in workspace {}: {}",
                        new Object[] { nodes.size(), workspace, nodes });
            }
            for (String s : nodes) {
                cacheManager.invalidate(workspace, s);
            }
        }
    }

    private void flushSubNodes(final String nodePath, final String srcAbsPath, final String destAbsPath) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, workspace, null, new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                boolean debugEnabled = logger.isDebugEnabled();
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
                        if (debugEnabled) {
                            logger.debug("Flushing file caches for nodes in workspace {}: {}, {}",
                                    new Object[] { workspace, path, replace });
                        }
                        flushSubNodes(path, srcAbsPath, destAbsPath);
                    }
                } catch (Exception e) {
                    cacheManager.invalidate(workspace, srcAbsPath);
                    cacheManager.invalidate(workspace, nodePath);
                    moduleCacheProvider.invalidate(nodePath);
                    moduleCacheProvider.invalidate(srcAbsPath);
                    if (debugEnabled) {
                        logger.debug("Flushing file caches for nodes in workspace {}: {}, {}",
                                new Object[] { workspace, srcAbsPath, nodePath });
                    }
                }
                return null;
            }
        });
    }

}
