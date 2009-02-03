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

 package org.jahia.resourcebundle;

import java.util.Locale;
import org.jahia.content.ObjectKey;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.hibernate.manager.JahiaResourceManager;

/**
 * <p>Title: database-related bean for resources stored in the Jahia database</p>
 * <p>Description: This bean holds a single resource entry for a specific
 * language code.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public class DatabaseResourceBeanImpl implements DatabaseResourceBean {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (DatabaseResourceBeanImpl.class);

    private String name;
    private String value;
    private String languageCode;

    /**
     * Empty constructor. Use setter to fill this bean with data
     */
    public DatabaseResourceBeanImpl() {
    }

    /**
     * Existing data constructor
     * @param name the name of the resource
     * @param value the value of the resource in this languageCode
     * @param languageCode the languageCode for the value
     */
    public DatabaseResourceBeanImpl(String name, String value, String languageCode) {
        this.name = name;
        this.value = value;
        this.languageCode = languageCode;
    }

    /**
     * @return the name of the resource
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the resource for a new resource
     * @param name the name of the resource
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return contains the value for the resource in the given language code
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value for the resource in the languageCode stored in this
     * bean
     * @param value the value for the languageCode
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the languageCode for this resource. For the format of this
     * language code see the setLanguageCode documentation
     */
    public String getLanguageCode() {
        return languageCode;
    }

    /**
     * Sets the languageCode for this resource entry
     * @param languageCode a language code identifier in the format equivalent
     * to the result of a Locale.toString() conversion.
     */
    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    /**
     * Return a resource value with a key that is the combination of
     *
     * objectKey.toString()+"."+fieldName+locale.toString()
     *
     * @param objectKey ObjectKey
     * @param fieldName String
     * @param locale Locale
     * @return String
     */
    static public String getValue(ObjectKey objectKey, String fieldName, Locale locale){
        if (objectKey == null || fieldName == null || locale == null) {
            return null;
        }
        String value = null;
        DatabaseResourceBean resourceBean = null;
        try {
            JahiaResourceManager manager = (JahiaResourceManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaResourceManager.class.getName());
            resourceBean = manager.getResource(objectKey.toString() + "." + fieldName,
                                               locale.toString());
            if (resourceBean != null) {
                value = resourceBean.getValue();
            }
        } catch ( Exception t ){
            logger.debug(t);
        }
        return value;
    }

}
