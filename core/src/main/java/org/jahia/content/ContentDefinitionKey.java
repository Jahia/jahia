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


/**
 * The purpose of this class is to construct ContentObject references that can
 * be used both externally and internally to references Jahia content objects
 * such as fields, containers, pages and other objects...
 *
 * The tentative format of this key is (subject to change in order to comply
 * with existing norms) :
 *
 * type_IDInType
 *
 * where type is a String specifiyng the type of the object, while the IDInType
 * is an integer that specifies which instances of the specific object type.
 *
 * @author Khue Nguyen
 */
public abstract class ContentDefinitionKey extends ObjectKey{

    private static final long serialVersionUID = -5061798309289612190L;

    /**
     * Protected constructor to use this class also as a factory by calling
     * the getChildInstance method
     */
    protected ContentDefinitionKey() {}

    /**
     * Object constructor method.
     * @param type a String specifying the object type. Normally this is used
     * mostly by children of this class but could be used for some "hacks".
     * @param IDInType the unique identifier within the type.
     */
    protected ContentDefinitionKey(String type,
                     String IDInType) {
        super(type,IDInType);
    }

    /**
     * Object constructor method, optimized version to avoid rebuilding the
     * key
     * @param type a String specifying the object type. Normally this is used
     * mostly by children of this class but could be used for some "hacks".
     * @param IDInType the unique identifier within the type.
     * @param objectKey the combined object key of the type and the IDInType. This is
     * generated with type + KEY_SEPARATOR + IDInType
     */
    protected ContentDefinitionKey(String type,
                     String IDInType, String objectKey) {
        super(type,IDInType, objectKey);
    }

}
