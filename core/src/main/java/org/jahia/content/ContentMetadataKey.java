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

import org.jahia.services.version.EntryLoadRequest;

import java.util.Collection;

public class ContentMetadataKey extends ContentObjectKey {

    private static final long serialVersionUID = -85856793441914586L;

    public static final String METADATA_TYPE = "ContentMetadata";

    static {
        ObjectKey.registerType(METADATA_TYPE, ContentMetadataKey.class);
    }

    /**
     * Protected constructor to use this class also as a factory by calling
     * the getChildInstance method
     */
    protected ContentMetadataKey() {}
    
    public ContentMetadataKey(int metadataID) {
        super(METADATA_TYPE, Integer.toString(metadataID));
    }

    public ContentMetadataKey(int metadataID, String objectKey) {
        super(METADATA_TYPE, Integer.toString(metadataID), objectKey);
    }

    public int getMetadataID() {
        return Integer.parseInt(getIDInType());
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
        return new ContentMetadataKey(Integer.parseInt(IDInType));
    }

    public ObjectKey getChildInstance(String IDInType, String objectKey) {
        return new ContentMetadataKey(Integer.parseInt(IDInType), objectKey);
    }

    public String getType() {
        return METADATA_TYPE;
    }

    public ContentObjectKey getParent(EntryLoadRequest request) {
        return null;  // is this class used somewhere ?
    }

    public Collection<ContentObjectKey> getChilds(EntryLoadRequest request) {
        return null;  // is this class used somewhere ?
    }

    public Collection<ContentObjectKey> getChilds(EntryLoadRequest request, int loadFlag) {
        return null;  // is this class used somewhere ?
    }    
    
    public static String toObjectKeyString(String idInType) {
        return toObjectKeyString(METADATA_TYPE, idInType);
    }

    public static String toObjectKeyString(int idInType) {
        return toObjectKeyString(METADATA_TYPE, Integer.toString(idInType));
    }

}
