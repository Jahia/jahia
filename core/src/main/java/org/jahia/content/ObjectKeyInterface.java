/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
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