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
package org.jahia.services.usermanager;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class stores user properties, which are different from regular
 * java.util.Properties because they can also store information about read-only
 * state as well as the original provider of the properties.
 *
 * @author Serge Huber
 * @deprecated
 */
@Deprecated
public class UserProperties implements Serializable {

    private static final long serialVersionUID = -5885566091509965795L;

    public static final Set<String> DEFAULT_PROPERTIES_NAME = new HashSet<>();

    static {
        DEFAULT_PROPERTIES_NAME.add("email");
        DEFAULT_PROPERTIES_NAME.add("lastname");
        DEFAULT_PROPERTIES_NAME.add("firstname");
        DEFAULT_PROPERTIES_NAME.add("organization");
        DEFAULT_PROPERTIES_NAME.add("emailNotificationsDisabled");
        DEFAULT_PROPERTIES_NAME.add("preferredLanguage");
    }

    private Map<String, UserProperty> properties = new HashMap<>();

    public UserProperties() {
        super();
    }

    /**
     * Copy constructor. All properties copied from will be marked as read-write
     * Be very careful when using this method or you will loose readOnly tagging
     *
     * @param properties Properties
     * @param readOnly   specifies whether the copied properties should be marked
     *                   as read-only or not.
     */
    public UserProperties(Properties properties, boolean readOnly) {
        Enumeration<?> sourceNameEnum = properties.propertyNames();
        while (sourceNameEnum.hasMoreElements()) {
            String curSourceName = (String)sourceNameEnum.nextElement();
            UserProperty curUserProperty = new UserProperty(curSourceName,
                    properties.getProperty(curSourceName), readOnly);
            this.properties.put(curSourceName, curUserProperty);
        }
    }

    /**
     * Copy constructor
     *
     * @param copy the properties to create the copy from
     */
    public UserProperties(UserProperties copy) {
        this.properties = copy.properties.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> new UserProperty(e.getValue())));
    }

    public void putAll(UserProperties ups) {
        ups.properties.keySet().removeAll(properties.keySet());
        properties.putAll(ups.properties);
    }

    public void putAll(Properties ups) {
        properties.keySet().removeAll(properties.keySet());
        for (Object o : ups.keySet()) {
            final String propName = o.toString();
            properties.put(propName, new UserProperty(propName, "", false));
        }
    }

    public UserProperty getUserProperty(String name) {
        return properties.get(name);
    }

    public void setUserProperty(String name, UserProperty value) {
        properties.put(name, value);
    }

    public UserProperty removeUserProperty(String name) {
        return properties.remove(name);
    }

    /**
     * Tests if a property is read-only.
     * Warning : this method also returns false if the property doesn't exist !
     *
     * @param name String
     * @return boolean
     */
    public boolean isReadOnly(String name) {
        UserProperty userProperty = properties.get(name);
        return userProperty != null && userProperty.isReadOnly();
    }

    public Iterator<String> propertyNameIterator() {
        return properties.keySet().iterator();
    }

    public Properties getProperties() {
        Properties propertiesCopy = new Properties();
        for (Map.Entry<String,UserProperty> o : properties.entrySet()) {
            Map.Entry<String,UserProperty> curEntry = o;
            UserProperty curUserProperty = curEntry.getValue();
            propertiesCopy.put(curUserProperty.getName(), curUserProperty.getValue());
        }
        return propertiesCopy;
    }

    public String getProperty(String name) {
        UserProperty userProperty = properties.get(name);
        return userProperty != null ? userProperty.getValue() : null;
    }

    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    public void setProperty(String name, String value)
            throws UserPropertyReadOnlyException {
        UserProperty userProperty = properties.get(name);
        if (userProperty != null) {
            if (userProperty.isReadOnly()) {
                throw new UserPropertyReadOnlyException(userProperty,
                        "Property " + name + " is readonly");
            }
            userProperty.setValue(value);
        } else {
            userProperty = new UserProperty(name, value, false);
        }
        properties.put(name, userProperty);
    }

    public String removeProperty(String name)
            throws UserPropertyReadOnlyException {
        UserProperty userProperty = properties.get(name);
        if (userProperty != null) {
            if (userProperty.isReadOnly()) {
                throw new UserPropertyReadOnlyException(userProperty,
                        "Property " + name + " is readonly");
            } else {
                return userProperty.getValue();
            }
        }
        return null;
    }

    public int size() {
        return properties.keySet().size();
    }

    @Override
    public String toString() {
        return getProperties().toString();
    }

}
