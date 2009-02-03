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

package org.jahia.content;

import org.apache.log4j.Logger;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.version.EntryLoadRequest;

import java.util.*;

public class  ContentContainerListKey extends ContentObjectKey {

    private static final long serialVersionUID = -1352904952838872167L;
    private static final transient Logger logger = Logger.getLogger(ContentContainerListKey.class);
    
    public static final String CONTAINERLIST_TYPE = "ContentContainerList";

    static {
        ObjectKey.registerType(CONTAINERLIST_TYPE, ContentContainerListKey.class);
    }

    /**
     * Protected constructor to use this class also as a factory by calling
     * the getChildInstance method
     */
    protected ContentContainerListKey() {}

    public ContentContainerListKey(int containerListID) {
        super(CONTAINERLIST_TYPE, Integer.toString(containerListID));
    }

    public ContentContainerListKey(int containerListID, String objectKey) {
        super(CONTAINERLIST_TYPE, Integer.toString(containerListID), objectKey);
    }

    public int getContainerListID() {
        return getIdInType();
    }

    /**
     * @deprecated This method should not be called directly, but rather it
     * should be replace by a call to the constructor with the proper IDInType.
     * This has been deprecated because the new getChildInstance() is much
     * faster
     * @param IDInType the IDInType
     * @return the ObjectKey corresponding to the ID for this class type
     */
    public static ObjectKey getChildInstance(String IDInType) {
        return new ContentContainerListKey(Integer.parseInt(IDInType));
    }

    public ObjectKey getChildInstance(String IDInType, String objectKey) {
        return new ContentContainerListKey(Integer.parseInt(IDInType), objectKey);
    }

    public String getType() {
        return CONTAINERLIST_TYPE;
    }

    void setParent(ContentObjectKey parent) {
        checkCache();
        final String entryKey = "parent" + toString();
        if(parent != null && !parent.equals(treeCache.get(entryKey))) {
            treeCache.put(entryKey, parent);
        }
    }

    public ContentObjectKey getParent(EntryLoadRequest request) {
        checkCache();
        ContentObjectKey p = (ContentObjectKey) treeCache.get("parent"+toString());
        if (p != null) {
            return p;
        }

        p = ServicesRegistry.getInstance().getJahiaContainersService().getListParentObjectKey(getIdInType(), request);

        treeCache.put("parent"+toString(), p);
        return p;
    }

    public Collection<ContentObjectKey> getChilds(EntryLoadRequest request, int loadFlag) {
        return getChilds(request);
    }
    
    public Collection<ContentObjectKey> getChilds(EntryLoadRequest request) {
        List<ContentObjectKey> results = new ArrayList<ContentObjectKey>();
        try {
            for (Integer integer : ServicesRegistry.getInstance().getJahiaContainersService().getctnidsInList(getIdInType(), request)) {
                ContentContainerKey cck = new ContentContainerKey(integer.intValue());
                cck.setParent(this);
                results.add(cck);
            }
        } catch (JahiaException e) {
            logger.error("Error getting children", e);
        }
        return results;
    }

    public static String toObjectKeyString(String idInType) {
        return toObjectKeyString(CONTAINERLIST_TYPE, idInType);
    }

    public static String toObjectKeyString(int idInType) {
        return toObjectKeyString(CONTAINERLIST_TYPE, Integer.toString(idInType));
    }

}
