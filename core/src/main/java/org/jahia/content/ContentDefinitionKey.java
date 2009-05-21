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
