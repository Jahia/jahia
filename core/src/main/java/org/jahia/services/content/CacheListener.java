/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;

import org.jahia.content.ContentContainerKey;
import org.jahia.content.ContentContainerListKey;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheFactory;
import org.jahia.services.cache.ContainerHTMLCache;
import org.jahia.services.fields.ContentField;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.webdav.UsageEntry;

/**
 * Listener for flushing container HTML cache entries.
 * User: toto
 * Date: 25 févr. 2008
 * Time: 14:36:14
 */
public class CacheListener extends DefaultEventListener {
    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(CacheListener.class);

    public static Cache<String, ?> cache;

    public CacheListener() {
        try {
            cache = CacheFactory.getInstance().createCacheInstance("WebdavCache");
        } catch (JahiaInitializationException e) {
            e.printStackTrace();
        }
    }

    public int getEventTypes() {
        return Event.NODE_ADDED + Event.PROPERTY_ADDED + Event.PROPERTY_CHANGED + Event.PROPERTY_REMOVED;
    }

    public String getPath() {
        return "/";
    }

    public String[] getNodeTypes() {
        return null;
    }

    public void onEvent(EventIterator eventIterator) {
        Set<String> nodes = new HashSet<String>();
        while (eventIterator.hasNext()) {
            Event event = eventIterator.nextEvent();
            try {
                if (isExternal(event)) {
                    continue;
                }

                String path = event.getPath();
                String parentPath = path.substring(0,path.lastIndexOf('/'));
                String name = path.substring(path.lastIndexOf('/')+1);
                String parentName = parentPath.substring(parentPath.lastIndexOf('/')+1);
                if ((event.getType() == Event.NODE_ADDED || event.getType() == Event.NODE_REMOVED) && name.equals("j:acl")) {
                    nodes.add(parentPath);
                }
                if ((event.getType() == Event.PROPERTY_ADDED || event.getType() == Event.PROPERTY_CHANGED) && parentName.equals("j:acl")) {
                    parentPath = parentPath.substring(0,parentPath.lastIndexOf('/'));
                    nodes.add(parentPath);
                }
                if (event.getType() == Event.PROPERTY_CHANGED && name.equals("j:fullpath")) {
                    // invalidate container HTML cache when the file is moved/renamed 
                    nodes.add(parentPath);
                }
                if ((event.getType() == Event.NODE_REMOVED) && name.indexOf(':')==-1) {
                    nodes.add(path);
                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
        if (!nodes.isEmpty()) {
            JCRSessionWrapper session = null;
            try {
                session = JCRStoreService.getInstance().getSystemSession();

                for (Iterator<String> iterator = nodes.iterator(); iterator.hasNext();) {
                    String s = iterator.next();
                    JCRNodeWrapper n = (JCRNodeWrapper) session.getItem(provider.decodeInternalName(s));
                    flushNodeRefs(n);
                    cache.remove(n.getPath());
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                if (session != null) {
                    session.logout();
                }
            }
        }
//        System.out.println("----------------> "+nodes);
    }

    private void flushNodeRefs(JCRNodeWrapper n) throws RepositoryException, JahiaException {
        if (n.isValid()) {
            if (n.isFile()) {
                for (UsageEntry usageEntry : n.findUsages(false)) {
                    int id = usageEntry.getId();
                    ContentField field = ContentField.getField(id);
                    ContentContainerKey key = new ContentContainerKey(field.getContainerID());
                    ContentContainerListKey listkey = (ContentContainerListKey) key.getParent(null);
                    ContainerHTMLCache containerHTMLCache = ServicesRegistry.getInstance().getCacheService().getContainerHTMLCacheInstance();
                    if (usageEntry.getWorkflow() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                        containerHTMLCache.invalidateContainerEntries(key.toString(), ProcessingContext.NORMAL, usageEntry.getLang());
                        containerHTMLCache.invalidateContainerEntries(key.toString(), ProcessingContext.COMPARE, usageEntry.getLang());
                        if (listkey != null) {
                            containerHTMLCache.invalidateContainerEntries(listkey.toString(), ProcessingContext.NORMAL, usageEntry.getLang());
                            containerHTMLCache.invalidateContainerEntries(listkey.toString(), ProcessingContext.COMPARE, usageEntry.getLang());
                        }
                        if (!field.hasStagingEntries()) {
                            containerHTMLCache.invalidateContainerEntries(key.toString(), ProcessingContext.EDIT, usageEntry.getLang());
                            containerHTMLCache.invalidateContainerEntries(key.toString(), ProcessingContext.PREVIEW, usageEntry.getLang());
                            if (listkey != null) {
                                containerHTMLCache.invalidateContainerEntries(listkey.toString(), ProcessingContext.EDIT, usageEntry.getLang());
                                containerHTMLCache.invalidateContainerEntries(listkey.toString(), ProcessingContext.PREVIEW, usageEntry.getLang());
                            }
                        }
                    } else if (usageEntry.getWorkflow() == EntryLoadRequest.STAGING_WORKFLOW_STATE ||
                            usageEntry.getWorkflow() == EntryLoadRequest.WAITING_WORKFLOW_STATE) {
                        containerHTMLCache.invalidateContainerEntries(key.toString(), ProcessingContext.EDIT, usageEntry.getLang());
                        containerHTMLCache.invalidateContainerEntries(key.toString(), ProcessingContext.PREVIEW, usageEntry.getLang());
                        containerHTMLCache.invalidateContainerEntries(key.toString(), ProcessingContext.COMPARE, usageEntry.getLang());
                        if (listkey != null) {
                            containerHTMLCache.invalidateContainerEntries(listkey.toString(), ProcessingContext.EDIT, usageEntry.getLang());
                            containerHTMLCache.invalidateContainerEntries(listkey.toString(), ProcessingContext.PREVIEW, usageEntry.getLang());
                            containerHTMLCache.invalidateContainerEntries(listkey.toString(), ProcessingContext.COMPARE, usageEntry.getLang());
                        }
                    }
                }
            } else {
                List<JCRNodeWrapper> children = n.getChildren();
                for (JCRNodeWrapper child : children) {
                    flushNodeRefs(child);
                }
            }
        }
    }
}
