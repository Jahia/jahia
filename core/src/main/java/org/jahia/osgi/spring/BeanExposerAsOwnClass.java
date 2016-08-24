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

import java.text.MessageFormat;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exposes a bean as one of bean's own classes (either bean class, or one of the super classes,
 * or one of the interfaces implemented), dependent on exposer's own configuration.
 */
public class BeanExposerAsOwnClass extends BeanExposerSupport implements SpringBridge.BeanExposer {

    private static final Logger logger = LoggerFactory.getLogger(BeanExposerAsOwnClass.class);

    private Class<?> clazz;

    /**
     * Create an instance that will expose a bean as its class as returned by bean.getClass()
     */
    public BeanExposerAsOwnClass() {
    }

    /**
     * Create an instance that will expose a bean as a specific class or interface;
     * the class/interface must be extended/implemented by the bean class.
     *
     * @param clazz Class or interface to expose a bean as
     */
    public BeanExposerAsOwnClass(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void exposeBean(String beanID, Object bean, BundleContext bundleContext) {
        Class<?> cl;
        if (clazz == null) {
            cl = bean.getClass();
        } else {
            if (!clazz.isInstance(bean)) {
                throw new IllegalArgumentException(MessageFormat.format("{0} bean is not a {1} instance", clazz.getName()));
            }
            cl = clazz;
        }
        bundleContext.registerService(cl.getName(), bean, getBasicServiceProperties(beanID, bean));
        logBeanExposed(logger, beanID, bean, cl);
    }
}
