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
package org.jahia.services.content.nodetypes.initializers;

import javax.jcr.Value;

import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Represents a single item in the choice list.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 17 nov. 2009
 */
public class ChoiceListValue implements Comparable<ChoiceListValue> {

    private String displayName;
    private Value value;
    private Map<String, Object> properties;

    public ChoiceListValue(String displayName, Map<String, Object> properties, Value value) {
        this.displayName = displayName;
        this.properties = properties;
        this.value = value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public Value getValue() {
        return value;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public void addProperty(String key, Object value) {
        if(properties==null) {
            properties = new LinkedHashMap<String, Object>();
        }
        properties.put(key,value);
    }

    public int compareTo(ChoiceListValue o) {
        return getDisplayName().compareTo(o.getDisplayName());
    }
}
