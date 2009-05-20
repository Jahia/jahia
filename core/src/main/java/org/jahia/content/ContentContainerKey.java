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
import org.jahia.data.containers.JahiaContainerStructure;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.version.EntryLoadRequest;

import java.util.*;

public class  ContentContainerKey extends ContentObjectKey {

    private static final long serialVersionUID = 8598340825885706084L;
    private static final transient Logger logger = Logger.getLogger(ContentContainerKey.class);

    public static final String CONTAINER_TYPE = "ContentContainer";

    static {
        ObjectKey.registerType(CONTAINER_TYPE, ContentContainerKey.class);
    }

    /**
     * Protected constructor to use this class also as a factory by calling
     * the getChildInstance method
     */
    protected ContentContainerKey() {}
    
    public ContentContainerKey(int containerID) {
        super(CONTAINER_TYPE, Integer.toString(containerID));
    }

    public ContentContainerKey(int containerID, String objectKey) {
        super(CONTAINER_TYPE, Integer.toString(containerID), objectKey);
    }

    public int getContainerID() {
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
        return new ContentContainerKey(Integer.parseInt(IDInType));
    }

    public ObjectKey getChildInstance(String IDInType, String objectKey) {
        return new ContentContainerKey(Integer.parseInt(IDInType), objectKey);
    }

    public String getType() {
        return CONTAINER_TYPE;
    }

    void setParent(ContentObjectKey parent) {
        if (parent == null) {
            throw new IllegalArgumentException("parent cannot be null");
        }
        checkCache();
        final String entryKey = "parent" + toString();
        if(!parent.equals(treeCache.get(entryKey))) {
            treeCache.put(entryKey, parent);
        }
    }

    public ContentObjectKey getParent(EntryLoadRequest request) {
        checkCache();
        ContentObjectKey p = (ContentObjectKey) treeCache.get("parent"+toString());
        if (p != null) {
            return p;
        }

        int i = ServicesRegistry.getInstance().getJahiaContainersService().getContainerParentListId(getIdInType(), request);
        if (i > 0) {
            p = new ContentContainerListKey(i);
            treeCache.put("parent"+toString(), p);
        }
        return p;
    }
    public Collection<ContentObjectKey> getChilds(EntryLoadRequest request) {
        return getChilds(request, JahiaContainerStructure.ALL_TYPES);
    }
    
    public Collection<ContentObjectKey> getChilds(EntryLoadRequest request, int loadFlag) {
        Collection<ContentObjectKey> results = new ArrayList<ContentObjectKey>();
        if ((loadFlag & JahiaContainerStructure.JAHIA_FIELD) != 0) {
            results = getChildsFields(request);
        }
        if ((loadFlag & JahiaContainerStructure.JAHIA_CONTAINER) != 0) {
            for (Integer integer : ServicesRegistry.getInstance().getJahiaContainersService()
                    .getSubContainerListIDs(getIdInType(), request)) {
                ContentContainerListKey cclk = new ContentContainerListKey(
                        integer.intValue());
                cclk.setParent(this);
                results.add(cclk);
            }
        }

        return results;
    }

    public Collection<ContentObjectKey> getChildsFields(EntryLoadRequest request) {
        List<ContentObjectKey> results = new ArrayList<ContentObjectKey>();
        try {
            for (Object[] o : ServicesRegistry.getInstance().getJahiaContainersService().getFieldIDsAndTypesInContainer(getIdInType(), request)) {
                ContentFieldKey key = new ContentFieldKey(((Integer) o[0]).intValue());
                key.setFieldType(((Integer) o[1]).intValue());
                key.setParent(this);
                results.add(key);
            }
        } catch (JahiaException e) {
            logger.error("Error getting children fields", e);
        }
        return results;
    }

    public static String toObjectKeyString(String idInType) {
        return toObjectKeyString(CONTAINER_TYPE, idInType);
    }

    public static String toObjectKeyString(int idInType) {
        return toObjectKeyString(CONTAINER_TYPE, Integer.toString(idInType));
    }

}
