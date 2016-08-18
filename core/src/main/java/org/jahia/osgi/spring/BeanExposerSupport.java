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
package org.jahia.osgi.spring;

import java.util.Dictionary;
import java.util.Hashtable;

import org.jahia.bin.Jahia;
import org.osgi.framework.Constants;
import org.slf4j.Logger;

/**
 * Contains common functionality which may be useful for BeanExposer implementors.
 */
public abstract class BeanExposerSupport {

    /**
     * OSGi service property that displays the ID of the original Spring bean.
     */
    public static final String SERVICE_PROPERTY_SPRING_BEAN_ID = "springBeanID";

    /**
     * OSGi service property that displays the class of the original Spring bean.
     */
    public static final String SERVICE_PROPERTY_SPRING_BEAN_CLASS = "springBeanClass";

    /**
     * Generate basic OSGi service properties.
     *
     * @param beanID Spring bean ID
     * @param bean   Spring bean to be exposed as an OSGi service
     * @return Basic OSGi service properties
     */
    protected static Dictionary<String, Object> getBasicServiceProperties(String beanID, Object bean) {
        Hashtable<String, Object> properties = new Hashtable<>();
        properties.put(Constants.SERVICE_VENDOR, Jahia.VENDOR_NAME);
        properties.put(SERVICE_PROPERTY_SPRING_BEAN_ID, beanID);
        properties.put(SERVICE_PROPERTY_SPRING_BEAN_CLASS, bean.getClass().getName());
        return properties;
    }

    /**
     * Log information about bean exposed as an OSGi service.
     *
     * @param logger Logger to use for logging
     * @param beanID Spring bean ID
     * @param bean   Spring bean exposed as an OSGi service
     * @param clazz  OSGi service class the bean was exposed as
     */
    protected static void logBeanExposed(Logger logger, String beanID, Object bean, Class<?> clazz) {
        logger.info("Bean '{}' of class '{}' exposed to OSGi as '{}'", new Object[]{beanID, bean.getClass().getName(), clazz.getName()});
    }
}
