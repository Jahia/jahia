/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.services.content.nodetypes.initializers;

import org.jahia.services.content.nodetypes.ValueImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a single item in the choice list.
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 17 nov. 2009
 */
public class ChoiceListValue implements Comparable<ChoiceListValue> {
    
    private static final Logger logger = LoggerFactory.getLogger(ChoiceListValue.class);

    private String displayName;
    private Value value;
    private Map<String, Object> properties;

    /**
     * Initializes an instance of this class.
     */
    public ChoiceListValue() {
        super();
    }

    public ChoiceListValue(String displayName, Map<String, Object> properties, Value value) {
        this();
        this.displayName = displayName;
        this.properties = properties;
        this.value = value;
    }

    public ChoiceListValue(String displayName, String stringValue) {
        this(displayName, null, new ValueImpl(stringValue));
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

    /**
     * Sets the string value for this choice list option.
     * 
     * @param stringValue
     *            the string value for this choice list option
     */
    public void setStringValue(String stringValue) {
        this.value = new ValueImpl(stringValue);
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
        return getDisplayName().compareToIgnoreCase(o.getDisplayName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChoiceListValue that = (ChoiceListValue) o;

        if (!displayName.equals(that.displayName)) return false;
        if (properties != null ? !properties.equals(that.properties) : that.properties != null) return false;
        try {
            if (!value.getString().equals(that.value.getString()) ) return false;
            if (value.getType() != that.value.getType()) return false;
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = displayName.hashCode();
        try {
            result = 31 * result + value.getString().hashCode();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        result = 31 * result + value.getType();
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        return result;
    }
}
