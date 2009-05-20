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
package org.jahia.data.applications;

import org.jahia.content.ObjectKey;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: 26 janv. 2007
 * Time: 16:26:42
 */
public class EntryPointObjectKey extends ObjectKey {

    private static final long serialVersionUID = 1135214230216807396L;
    
    public static final String ENTRY_POINT_TYPE = "entrypoint";
    
    static {
        ObjectKey.registerType(ENTRY_POINT_TYPE, EntryPointObjectKey.class);
    }

    /*TO DO: ckeck value of this IDInType */
    private String IDInType = "100";
    private int idInType;

    /**
     * Protected constructor to use this class also as a factory by calling
     * the getChildInstance method. Please do not use this constructor directly
     */
    public EntryPointObjectKey() {}

    public EntryPointObjectKey(String appID_entryPointName) {
        super(ENTRY_POINT_TYPE, appID_entryPointName);
    }

    public EntryPointObjectKey(String appID_entryPointName, String objectKey) {
        super(ENTRY_POINT_TYPE, appID_entryPointName, objectKey);
    }

    public String getType() {
        return ENTRY_POINT_TYPE;
    }

    public String getIDInType() {
        return this.IDInType;
    }

    public int getIdInType() {
        if (idInType <= 0) {
            try {
                idInType = Integer.parseInt(IDInType);
            } catch (NumberFormatException e) {
                idInType = -1;
            }
        }
        return idInType;
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
        return new EntryPointObjectKey(IDInType);
    }

    public ObjectKey getChildInstance(String IDInType, String objectKey) {
        return new EntryPointObjectKey(IDInType, objectKey);
    }

}
