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
import org.jahia.services.toolbar.resolver.SelectedResolver;

import java.io.Serializable;

/**
 * User: jahia
 * Date: 8 avr. 2008
 * Time: 10:21:27
 */
public class Selected implements Serializable {
    
    private static final transient Logger logger = Logger
            .getLogger(Selected.class);
    
    private String value;
    private String classResolver;
    private String inputResolver;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getClassResolver() {
        return classResolver;
    }

    public void setClassResolver(String classResolver) {
        this.classResolver = classResolver;
    }

    public String getInputResolver() {
        return inputResolver;
    }

    public void setInputResolver(String inputResolver) {
        this.inputResolver = inputResolver;
    }

    public boolean getRealValue(org.jahia.data.JahiaData jData) {
        if (value != null) {
            return Boolean.parseBoolean(value);
        } else {
            try {
                SelectedResolver resolver = (SelectedResolver) Class.forName(classResolver).newInstance();
                return resolver.isSelected(jData,inputResolver);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return true;
            }
        }
    }
}
