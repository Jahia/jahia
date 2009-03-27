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

import org.jahia.data.fields.FieldTypes;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.version.EntryLoadRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class  ContentFieldKey extends ContentObjectKey {

    private static final long serialVersionUID = 5862355155220949415L;

    public static final String FIELD_TYPE = "ContentField";

    private transient int fieldType = -1;

    static {
        ObjectKey.registerType(FIELD_TYPE, ContentFieldKey.class);
    }

    /**
     * Protected constructor to use this class also as a factory by calling
     * the getChildInstance method
     */
    protected ContentFieldKey() {}

    public ContentFieldKey(int fieldID) {
        super(FIELD_TYPE, Integer.toString(fieldID));
    }

    public ContentFieldKey(int fieldID, String objectKey) {
        super(FIELD_TYPE, Integer.toString(fieldID), objectKey);
    }

    public int getFieldID() {
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
        return new ContentFieldKey(Integer.parseInt(IDInType));
    }

    public ObjectKey getChildInstance(String IDInType, String objectKey) {
        return new ContentFieldKey(Integer.parseInt(IDInType), objectKey);
    }

    public String getType() {
        return FIELD_TYPE;
    }

    public int getFieldType() {
        return fieldType;
    }

    void setFieldType(int fieldType) {
        this.fieldType = fieldType;
    }

    public void setParent(ContentObjectKey parent) {
        checkCache();
        final String entryKey = "parent" + toString();
        if (parent != null && !parent.equals(treeCache.get(entryKey))) {
            treeCache.put(entryKey, parent);
        }
    }

    public ContentObjectKey getParent(EntryLoadRequest request) {
        checkCache();
        final String entryKey = "parent" + toString();
        ContentObjectKey p = (ContentObjectKey) treeCache.get(entryKey);
        if (p != null) {
            return p;
        }
        p = ServicesRegistry.getInstance().getJahiaFieldService().getFieldParentObjectKey(getIdInType(), request);
        treeCache.put(entryKey, p);
        return p;
    }

    public Collection<ContentObjectKey> getChilds(EntryLoadRequest request, int loadFlag) {
        return getChilds(request);
    }
    
    public Collection<ContentObjectKey> getChilds(EntryLoadRequest request) {
        List<ContentObjectKey> results = Collections.emptyList();
        if (fieldType == -1 || fieldType == FieldTypes.PAGE) {
            int[] i = ServicesRegistry.getInstance().getJahiaFieldService().getSubPageIdAndType(getIdInType(), request);
            if ( i[0] > 0) {
                ContentPageKey o = new ContentPageKey(i[0]);
                o.setPageType(i[1]);
                results = new ArrayList<ContentObjectKey>();                
                results.add(o);
            }
        }
        return results;
    }

    public static String toObjectKeyString(String idInType) {
        return toObjectKeyString(FIELD_TYPE, idInType);
    }

    public static String toObjectKeyString(int idInType) {
        return toObjectKeyString(FIELD_TYPE, Integer.toString(idInType));
    }

}
