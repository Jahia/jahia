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
 package org.jahia.resourcebundle;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 26 avr. 2005
 * Time: 10:01:38
 * To change this template use File | Settings | File Templates.
 */
public interface DatabaseResourceBean {
    /**
     * @return the name of the resource
     */
    String getName();

    /**
     * Sets the name of the resource for a new resource
     * @param name the name of the resource
     */
    void setName(String name);

    /**
     * @return contains the value for the resource in the given language code
     */
    String getValue();

    /**
     * Sets the value for the resource in the languageCode stored in this
     * bean
     * @param value the value for the languageCode
     */
    void setValue(String value);

    /**
     * @return the languageCode for this resource. For the format of this
     * language code see the setLanguageCode documentation
     */
    String getLanguageCode();

    /**
     * Sets the languageCode for this resource entry
     * @param languageCode a language code identifier in the format equivalent
     * to the result of a Locale.toString() conversion.
     */
    void setLanguageCode(String languageCode);
}
