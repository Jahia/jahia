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

package org.jahia.services.preferences;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;
import org.jahia.services.preferences.exception.JahiaPreferenceNotDefinedAttributeException;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUser;

import java.lang.reflect.InvocationTargetException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * User: ktlili
 * Date: 19 mars 2008
 * Time: 11:47:51
 * <p/>
 * Represents the key of a preference. An instance can be created with method createEmptyJahiaPreferencKey(...) of a JahiaPreferenceProvider object.
 */
public abstract class JahiaPreferenceKey {
    private static Logger logger = Logger.getLogger(JahiaPreferenceKey.class);
    private static String[] USELESS_PROPERTIES_NAME = {"class", "principal", "principalType", "principalKey", "providerType"};

    private String providerType;
    private Principal principal;
    public static String PRINCIPALE_TYPE_USER = "user";
    public static String PRINCIPALE_TYPE_GROUP = "group";

    public JahiaPreferenceKey(String providerType) {
        this.providerType = providerType;
    }

    public JahiaPreferenceKey(String providerType, Principal principal) {
        this.providerType = providerType;
        this.principal = principal;
    }

    public JahiaPreferenceKey(String providerType, Principal principal, Map<String, String> extraAttributes) throws JahiaPreferenceNotDefinedAttributeException {
        this.providerType = providerType;
        this.principal = principal;
        setProperties(extraAttributes);
    }


    public JahiaPreferenceKey(Map<String, String> attribute) throws JahiaPreferenceNotDefinedAttributeException {
        setProperties(attribute);
    }


    /**
     * Get principal
     *
     * @return
     */
    public Principal getPrincipal() {
        return principal;
    }

    /**
     * set principal
     *
     * @param principal
     */
    public void setPrincipal(Principal principal) {
        this.principal = principal;
    }


    /**
     * Get principal key
     *
     * @return
     */
    public String getPrincipalKey() {
        if (principal != null) {
            if (principal instanceof JahiaUser) {
                return ((JahiaUser) principal).getUserKey();
            } else if (principal instanceof JahiaGroup) {
                return ((JahiaGroup) principal).getGroupKey();
            }
        }
        return null;
    }


    /**
     * Get Principal type
     *
     * @return
     */
    public String getPrincipalType() {
        if (principal instanceof JahiaUser) {
            return PRINCIPALE_TYPE_USER;
        } else if (principal instanceof JahiaGroup) {
            return PRINCIPALE_TYPE_GROUP;
        }
        return null;
    }


    /**
     * Get provider Type
     *
     * @return
     */
    public String getProviderType() {
        return providerType;
    }

    /**
     * Set provider type
     *
     * @param providerType
     */
    public void setProviderType(String providerType) {
        this.providerType = providerType;
    }

    /**
     * Set all properties that are in the maps
     *
     * @param properties
     * @throws JahiaPreferenceNotDefinedAttributeException
     *
     */
    public void setProperties(Map<String, String> properties) throws JahiaPreferenceNotDefinedAttributeException {
        try {
            BeanUtils.populate(this, properties);
        } catch (IllegalAccessException e) {
            throw new JahiaPreferenceNotDefinedAttributeException(e);
        } catch (InvocationTargetException e) {
            throw new JahiaPreferenceNotDefinedAttributeException(e);
        }
    }

    /**
     * @return Return the entire set of properties for which current key provides a read method.
     */
    public Map describe() {
        try {
            Map propertiesMap = BeanUtils.describe(this);
            if (propertiesMap != null) {
                removeUselessProperties(propertiesMap);
                Iterator it = propertiesMap.keySet().iterator();
                if (logger.isDebugEnabled()) {
                    while (it.hasNext()) {
                        logger.debug("found property:" + it.next());
                    }
                }

            } else {
                logger.debug("Jahia preference key has no properties");
            }
            return propertiesMap;
        } catch (IllegalAccessException e) {
            logger.error(e, e);
        } catch (InvocationTargetException e) {
            logger.error(e, e);
        } catch (NoSuchMethodException e) {
            logger.error(e, e);
        }
        return new HashMap();
    }

    /**
     * Remove useless propoerties
     *
     * @param propertiesMap
     */
    private void removeUselessProperties(Map propertiesMap) {
        // remove non-key properties
        for (String propName : USELESS_PROPERTIES_NAME) {
            propertiesMap.remove(propName);
        }
    }

    /**
     * Set property value by its name
     *
     * @param name
     * @param value
     * @throws JahiaPreferenceNotDefinedAttributeException
     *
     */
    public void setProperty(String name, Object value) throws JahiaPreferenceNotDefinedAttributeException {
        try {
            BeanUtils.setProperty(this, name, value);
        } catch (IllegalAccessException e) {
            throw new JahiaPreferenceNotDefinedAttributeException(e);
        } catch (InvocationTargetException e) {
            throw new JahiaPreferenceNotDefinedAttributeException(e);
        }
    }


    /**
     * Get property value by its name
     *
     * @param name
     * @return
     * @throws JahiaPreferenceNotDefinedAttributeException
     *
     */
    public String getProperty(String name) throws JahiaPreferenceNotDefinedAttributeException {
        try {
            Object o = BeanUtils.getProperty(this, name);
            if (o != null) {
                return o.toString();
            } else {
                return null;
            }
        } catch (IllegalAccessException e) {
            throw new JahiaPreferenceNotDefinedAttributeException(e);
        } catch (InvocationTargetException e) {
            throw new JahiaPreferenceNotDefinedAttributeException(e);
        } catch (NoSuchMethodException e) {
            throw new JahiaPreferenceNotDefinedAttributeException(e);
        }
    }


    /**
     * Copy all fiels into the current current bean
     *
     * @param origin
     * @throws JahiaPreferenceNotDefinedAttributeException
     *
     */
    public void copy(JahiaPreferenceKey origin) throws JahiaPreferenceNotDefinedAttributeException {
        try {
            BeanUtils.copyProperties(this, origin);
        } catch (IllegalAccessException e) {
            throw new JahiaPreferenceNotDefinedAttributeException(e);
        } catch (InvocationTargetException e) {
            throw new JahiaPreferenceNotDefinedAttributeException(e);
        }
    }

    /**
     * Equals method
     *
     * @param obj
     * @return
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj != null && obj instanceof JahiaPreferenceKey) {
            JahiaPreferenceKey castObj = (JahiaPreferenceKey) obj;
            if (castObj.getProviderType() != null && castObj.getProviderType().equalsIgnoreCase(getProviderType())) {
                try {
                    Map propMap1 = BeanUtils.describe(this);
                    Map propMap2 = BeanUtils.describe(castObj);
                    if (propMap1 == null) {
                        if (propMap2 == null) {
                            // propMap1 == propMap2 == null
                            return true;
                        } else {
                            // propMap1 == null && propMap2 != null
                            return false;
                        }
                    } else if (propMap2 == null) {
                        // propMap1 != null && propMap2 == null
                        return false;
                    } else {
                        // construct an equals builder that compares all properties 
                        Iterator keys = propMap1.keySet().iterator();
                        EqualsBuilder equalsBuilder = new EqualsBuilder();
                        while (keys.hasNext()) {
                            Object key = keys.next();
                            equalsBuilder.append(propMap1.get(key), propMap2.get(key));
                        }
                        return equalsBuilder.isEquals();
                    }

                } catch (IllegalAccessException e) {
                    return false;
                } catch (InvocationTargetException e) {
                    return false;
                } catch (NoSuchMethodException e) {
                    return false;
                }
            }
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(providerType)
                .append(principal.hashCode())
                .toHashCode();
    }


}
