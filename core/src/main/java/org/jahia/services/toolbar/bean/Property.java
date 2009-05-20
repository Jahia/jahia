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
package org.jahia.services.toolbar.bean;

import org.apache.log4j.Logger;
import org.jahia.services.toolbar.resolver.PropertyResolver;

import java.io.Serializable;

/**
 * User: jahia
 * Date: 7 avr. 2008
 * Time: 09:30:53
 */
public class Property implements Serializable {
    
    private static final transient Logger logger = Logger.getLogger(Property.class);
    
    private String name;
    private String value;
    private String classProvider;
    private String inputProvider;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getClassProvider() {
        return classProvider;
    }

    public void setClassProvider(String classProvider) {
        this.classProvider = classProvider;
    }

    public String getInputProvider() {
        return inputProvider;
    }

    public void setInputProvider(String inputProvider) {
        this.inputProvider = inputProvider;
    }

    public String getRealValue(org.jahia.data.JahiaData jData) {
        if (value != null) {
            return value;
        } else {
            try {
                PropertyResolver propertiesValuesProvider = (PropertyResolver) Class.forName(classProvider).newInstance();
                return propertiesValuesProvider.getValue(jData,inputProvider);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return null;
            }
        }
    }

}
