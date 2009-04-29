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

import org.jahia.data.JahiaData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: jahia
 * Date: 7 ao�t 2008
 * Time: 09:30:28
 */
public class ItemsProvider implements Serializable {
    private String classProvider;
    private String inputProvider;
    private List<Property> propertyList = new ArrayList<Property>();

    public String getInputProvider() {
        return inputProvider;
    }

    public void setInputProvider(String inputProvider) {
        this.inputProvider = inputProvider;
    }

    public String getClassProvider() {
        return classProvider;
    }

    public void setClassProvider(String classProvider) {
        this.classProvider = classProvider;
    }

    public List<Property> getPropertyList() {
        return propertyList;
    }

    public void addProperty(Property property) {
        propertyList.add(property);
    }

    public Map<String, String> getProperties(JahiaData jahiaData) {
        Map<String, String> propertiesMap = new HashMap<String, String>();
        for (Property property : propertyList) {
            propertiesMap.put(property.getName(), property.getRealValue(jahiaData));
        }
        return propertiesMap;
    }
}
