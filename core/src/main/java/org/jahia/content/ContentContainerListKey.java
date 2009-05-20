/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
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
