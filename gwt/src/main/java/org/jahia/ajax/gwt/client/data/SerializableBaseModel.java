/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.ajax.gwt.client.data;

import com.extjs.gxt.ui.client.data.ModelData;

import java.io.Serializable;
import java.util.*;

/**
 * Simple implementation of ModelData which can be serialized
 */
@SuppressWarnings("unchecked")
public class SerializableBaseModel implements ModelData, Serializable {
    private static final long serialVersionUID = 1L;

    private Map<String, String> strings;
    private Map<String, Integer> integers;
    private Map<String, Boolean> booleans;

    private transient Map<String, Serializable> properties;

    public SerializableBaseModel() {
    }

    public <X> X get(String property) {
        if (strings != null && strings.containsKey(property)) {
            return (X) strings.get(property);
        }
        if (integers != null && integers.containsKey(property)) {
            return (X) integers.get(property);
        }
        if (booleans != null && booleans.containsKey(property)) {
            return (X) booleans.get(property);
        }
        if (properties != null) {
            return (X) properties.get(property);
        }
        return null;
    }


    public Map<String, Object> getProperties() {
        if (properties == null) {
            properties = new HashMap<String, Serializable>();
        }
        Map<String, Object> returning = new HashMap<String, Object>();
        for (Map.Entry<String, Serializable> entry : properties.entrySet()) {
            returning.put(entry.getKey(), entry.getValue());
        }
        if (strings != null) {
            for (Map.Entry<String, String> entry : strings.entrySet()) {
                returning.put(entry.getKey(), entry.getValue());
            }
        }
        if (integers != null) {
            for (Map.Entry<String, Integer> entry : integers.entrySet()) {
                returning.put(entry.getKey(), entry.getValue());
            }
        }
        if (booleans != null) {
            for (Map.Entry<String, Boolean> entry : booleans.entrySet()) {
                returning.put(entry.getKey(), entry.getValue());
            }
        }
        return returning;
    }


    public Collection<String> getPropertyNames() {
        if (properties == null) {
            properties = new HashMap<String, Serializable>();
        }
        Collection<String> returning = new HashSet<String>();
        returning.addAll(properties.keySet());
        if (strings != null) {
            returning.addAll(strings.keySet());
        }
        if (integers != null) {
            returning.addAll(integers.keySet());
        }
        if (booleans != null) {
            returning.addAll(booleans.keySet());
        }

        return returning;
    }


    public <X> X remove(String property) {
        if (strings != null && strings.containsKey(property)) {
            return (X) strings.remove(property);
        }
        if (integers != null && integers.containsKey(property)) {
            return (X) integers.remove(property);
        }
        if (booleans != null && booleans.containsKey(property)) {
            return (X) booleans.remove(property);
        }
        if (properties != null) {
            return (X) properties.remove(property);
        }
        return null;
    }


    public <X> X set(String property, X value) {
        if (value instanceof String) {
            if (strings == null) {
                strings = new LinkedHashMap<String, String>();
            }
            return (X) strings.put(property, (String) value);
        } else if (value instanceof Integer) {
            if (integers == null) {
                integers = new LinkedHashMap<String, Integer>();
            }
            return (X) integers.put(property, (Integer) value);
        } else if (value instanceof Boolean) {
            if (booleans == null) {
                booleans = new LinkedHashMap<String, Boolean>();
            }
            return (X) booleans.put(property, (Boolean) value);
        }
        if (properties == null) {
            properties = new LinkedHashMap<String, Serializable>();
        }
        return (X) properties.put(property, (Serializable) value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }

        SerializableBaseModel that = (SerializableBaseModel) o;

        if (booleans != null ? !booleans.equals(that.booleans) : that.booleans != null) {
            return false;
        }
        if (integers != null ? !integers.equals(that.integers) : that.integers != null) {
            return false;
        }
        if (properties != null ? !properties.equals(that.properties) : that.properties != null) {
            return false;
        }
        if (strings != null ? !strings.equals(that.strings) : that.strings != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = strings != null ? strings.hashCode() : 0;
        result = 31 * result + (integers != null ? integers.hashCode() : 0);
        result = 31 * result + (booleans != null ? booleans.hashCode() : 0);
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        return result;
    }
}

