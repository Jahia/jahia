/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.usermanager;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * This class stores user properties, which are different from regular
 * java.util.Properties because they can also store information about read-only
 * state as well as the original provider of the properties.
 *
 * @author Serge Huber
 */
public class UserProperties implements Serializable {

    private static final long serialVersionUID = -5885566091509965795L;
    
    public static final Set<String> DEFAULT_PROPERTIES_NAME = new HashSet<String>();

    static {
        DEFAULT_PROPERTIES_NAME.add("email");
        DEFAULT_PROPERTIES_NAME.add("lastname");
        DEFAULT_PROPERTIES_NAME.add("firstname");
        DEFAULT_PROPERTIES_NAME.add("organization");
        DEFAULT_PROPERTIES_NAME.add("emailNotificationsDisabled");
        DEFAULT_PROPERTIES_NAME.add("preferredLanguage");
    }

    private Map<String, UserProperty> properties = new HashMap<String, UserProperty>();

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

    protected UserProperties(UserProperties copy) {
        for (Map.Entry<String, UserProperty> stringUserPropertyEntry : copy.properties.entrySet()) {
            UserProperty curUserProperty = (UserProperty) ((Map.Entry<String, UserProperty>) stringUserPropertyEntry).getValue();
            properties.put(curUserProperty.getName(), (UserProperty) curUserProperty.clone());
        }
    }

    public Object clone() {
        return new UserProperties(this);
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

    public String toString() {
        return getProperties().toString();
    }

    public int size() {
        return properties.keySet().size();
    }
}
