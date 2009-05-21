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
