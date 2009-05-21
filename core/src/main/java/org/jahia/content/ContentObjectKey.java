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
 package org.jahia.content;

import org.apache.log4j.Logger;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.Cache;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.version.EntryLoadRequest;

import java.util.Collection;
import java.util.Iterator;

/**
 * <p>Title: Content object key abstract marker class</p>
 * <p>Description: This class is only used to regroup keys as descendants of
 * this class in order to test if a key is relative to a content object or
 * not. This class should *never* be used directly.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public abstract class ContentObjectKey extends ObjectKey {
    
    private static final transient Logger logger = Logger.getLogger(ContentObjectKey.class);

    private static final long serialVersionUID = 1L;

    private static final String TREE_CACHE = "ObjectTreeCache";
    protected static Cache<String, ContentObjectKey> treeCache;

    protected ContentObjectKey() {        
    }

    /**
     * Object constructor method.
     * @param type a String specifying the object type. Normally this is used
     * mostly by children of this class but could be used for some "hacks".
     * @param IDInType the unique identifier within the type.
     * @param objectKey combination of the type and IDInType 
     */
    protected ContentObjectKey(String type,
                               String IDInType,
                               String objectKey) {
        super(type, IDInType, objectKey);
    }

    protected ContentObjectKey(String type,
                               String IDInType) {
        super(type, IDInType);
    }

    public abstract String getType();

    public abstract ContentObjectKey getParent(EntryLoadRequest request);

    public abstract Collection<ContentObjectKey> getChilds(EntryLoadRequest request);
    
    public abstract Collection<ContentObjectKey> getChilds(EntryLoadRequest request, int loadFlag);

    public void dump() {
        Collection<ContentObjectKey> l = getChilds(EntryLoadRequest.STAGED);
        System.out.println("--->");
        for (Iterator<ContentObjectKey> iterator = l.iterator(); iterator.hasNext();) {
            ContentObjectKey contentObjectKey = iterator.next();
            System.out.println(contentObjectKey + " -<"  + contentObjectKey.getParent(EntryLoadRequest.STAGED));
            contentObjectKey.dump();
        }
        System.out.println("<---");
    }

    protected static void checkCache() {
        if(treeCache == null)
        treeCache = ServicesRegistry.getInstance().getCacheService().getCache(TREE_CACHE);
        if(treeCache==null) {
            try {
                treeCache = ServicesRegistry.getInstance().getCacheService().createCacheInstance(TREE_CACHE);
            } catch (JahiaInitializationException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public static void flushCache(ContentObject object) {
        if (treeCache != null && object != null) {
            ContentObjectKey parentK = ((ContentObjectKey) object.getObjectKey()).getParent(EntryLoadRequest.STAGED);
            if(object instanceof ContentPage) {
                String base = "parent" + object.getObjectKey() + "-";
                treeCache.remove(base + EntryLoadRequest.STAGING_WORKFLOW_STATE);
                treeCache.remove(base + EntryLoadRequest.ACTIVE_WORKFLOW_STATE);
                treeCache.remove(base + EntryLoadRequest.WAITING_WORKFLOW_STATE);
                treeCache.remove(base + EntryLoadRequest.DELETED_WORKFLOW_STATE);
            }
            treeCache.remove("childs"+ parentK);
        }
    }
}