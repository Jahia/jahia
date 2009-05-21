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
