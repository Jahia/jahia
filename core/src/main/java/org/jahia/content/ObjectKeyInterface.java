/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

 package org.jahia.content;

/**
 * <p>Title: Content Object Unique Identifier Key Interface</p>
 * <p>Description: This interface contains accessor methods to a content object
 * unique identifier key parameters, such as the key itself, the type of the
 * content object, as well as the ID in the type.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Serge Huber
 * @version 1.0
 */

public interface ObjectKeyInterface {

    static final String KEY_SEPARATOR = "_";

    /**
     * Returns the actual key of the content object. This is a key that is
     * unique within a Jahia installation, regardless of the sites, the pages
     * we are on, etc..
     * @return a String containing a content object unique key, in the form
     * of something like this : type_IDInType
     */
    public String getKey();

    /**
     * Returns a type identifier for a content object. This can also be thought
     * of as the "class" of the object.
     * @return a String representing the type identifier of the content object
     * key
     */
    public String getType();

    /**
     * Returns a String that represents an identifier for a content object
     * within a type. This might also be thought of as the "instance" of the
     * content object of a certain type (or "class").
     * @return an String representing the identifier within the type of the
     * content object key
     */
    public String getIDInType();

    /**
     * Factory method to create an instance of the corresponding ObjectKey
     * implementation class. This is called by the ObjectKey.getInstance()
     * method and should never be called directly.
     * @param IDInType identifier within type
     * @param objectKey the full ObjectKey string representation. This is
     * an optimization to avoid having to rebuild it as when we call this
     * method we already have it.
     * @return an ObjectKey instance of the appropriate subclass.
     */
    public ObjectKey getChildInstance(String IDInType, String objectKey);

}