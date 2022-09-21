/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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

import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.jahia.services.content.nodetypes.ValueImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.LinkedHashMap;

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
