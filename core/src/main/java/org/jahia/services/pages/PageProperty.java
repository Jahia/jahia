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

 package org.jahia.services.pages;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * <p>Title: Page property bean object </p>
 * <p>Description: This object contains all the values for multilingual
 * page properties. This class does not support versioning or workflow
 * elements for the moment.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 *
 * @author Serge Huber
 * @version 1.0
 */

public class PageProperty implements Serializable {

    private final static String SHARED_LANGUAGE_MARKER = "shared";

    public final static String PAGE_URL_KEY_PROPNAME = "pageURLKey";

    private int pageID;
    private String name;
    // The following hashmas' key is a language code, and the value is
    // a string representing the value for the property. If the key is SHARED_LANGUAGE_MARKER
    // then this entry is matched for any languages.
    private Map languageValues;

    public PageProperty (int pageID, String name) {
        this.pageID = pageID;
        this.name = name;
        languageValues = new HashMap();
    }

    /**
     * Returns the page ID this property is attached to.
     *
     * @return an integer corresponding to the page ID of this property
     */
    public int getPageID () {
        return pageID;
    }

    /**
     * Returns the name of the property
     *
     * @return a string containing the name of the property
     */
    public String getName () {
        return name;
    }

    /**
     * Returns the value for the property. This is the "default value" that
     * is returned. If accessing multi-language properties, please use the
     * other getValue(String languageCode) method.
     *
     * @return a String containing the property default value.
     */
    public String getValue () {
        return getValue (SHARED_LANGUAGE_MARKER);
    }

    /**
     * Returns the value for the property according to the language passed.
     * This value might be empty if no value exists NEITHER for the language
     * nor the default value. If there is a default value it will be returned
     * in place of the language entry.
     *
     * @param languageCode the RFC 3066 language code for which to retrieve the
     *                     property value.
     *
     * @return a String containing the value of the property for the given
     *         language, or the default value, or an empty String if neither values
     *         could be found.
     */
    public String getValue (String languageCode) {
        if (languageValues.containsKey (languageCode)) {
            return (String) languageValues.get (languageCode);
        }

        if (languageValues.containsKey (SHARED_LANGUAGE_MARKER)) {
            return (String) languageValues.get (SHARED_LANGUAGE_MARKER);
        }

        return "";
    }

    /**
     * Sets the default value for the property. This value will be used either
     * when retrieving a value without specifying a language, or when trying
     * to retrieve a language for which no value is defined.
     *
     * @param value the String value for the property (must convert to String
     *              if coming from a non string value)
     */
    public void setValue (String value) {
        setValue (value, SHARED_LANGUAGE_MARKER);
    }

    /**
     * Sets the property value for a given language.
     *
     * @param value        the String value for the property (must convert to String
     *                     if coming from another type of object)
     * @param languageCode the RFC 3066 language code for which to stored the
     *                     property.
     */
    public void setValue (String value, String languageCode) {
        languageValues.put (languageCode, value);
    }

    /**
     * Returns an Map.Entry iterator on the language codes contained in the language
     * values internal structure
     *
     * @return an iterator of String object that correspond to the available
     *         languages in this property.
     */
    public Iterator getLanguageCodes () {
        Set languagesCodeSet = languageValues.entrySet ();
        return languagesCodeSet.iterator ();
    }

}