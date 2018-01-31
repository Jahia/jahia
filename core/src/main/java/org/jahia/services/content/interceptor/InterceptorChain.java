/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content.interceptor;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Calls all property interceptors in a chain. Interceptors are called one after the other.
 * <p/>
 * Setting a property :
 * <p/>
 * v1 = interceptor1.beforeSet(v)
 * v2 = interceptor2.beforeSet(v1)
 * v3 = interceptor3.beforeSet(v2)
 * ..
 * property set (v3)
 * <p/>
 * Getting a property :
 * <p/>
 * v = get property
 * v1 = interceptor3.afterGet(v)
 * v2 = interceptor2.afterGet(v1)
 * v3 = interceptor1.afterGet(v2)
 * ...
 * return v3
 */
public class InterceptorChain {

    private List<PropertyInterceptor> interceptors = new ArrayList<PropertyInterceptor>();
    private List<PropertyInterceptor> revInterceptors;

    public void setInterceptors(List<PropertyInterceptor> interceptors) {
        this.interceptors = interceptors;
        revInterceptors = new ArrayList<PropertyInterceptor>(interceptors);
        Collections.reverse(revInterceptors);
    }

    public void beforeRemove(JCRNodeWrapper node, String name, ExtendedPropertyDefinition definition)
            throws VersionException, LockException, ConstraintViolationException, RepositoryException {
        for (PropertyInterceptor interceptor : interceptors) {
            if (interceptor.canApplyOnProperty(node, definition)) {
                interceptor.beforeRemove(node, name, definition);
            }
        }
    }

    /**
     * Calls all property interceptors in a chain. Interceptors are called one after the other.
     * <p/>
     * v = get property
     * v1 = interceptor3.afterGet(v)
     * v2 = interceptor2.afterGet(v1)
     * v3 = interceptor1.afterGet(v2)
     * ...
     * return v3
     *
     * @param node
     * @param name
     * @param definition
     * @param originalValue @return
     * @throws ValueFormatException
     * @throws VersionException
     * @throws LockException
     * @throws ConstraintViolationException
     * @throws RepositoryException in case of JCR-related errors
     */
    public Value beforeSetValue(JCRNodeWrapper node, String name, ExtendedPropertyDefinition definition,
                                Value originalValue)
            throws ValueFormatException, VersionException, LockException, ConstraintViolationException,
            RepositoryException {
        for (PropertyInterceptor interceptor : interceptors) {
            if (interceptor.canApplyOnProperty(node, definition)) {
                originalValue = interceptor.beforeSetValue(node, name, definition, originalValue);
            }
        }
        return originalValue;
    }

    /**
     * Calls all property interceptors in a chain. Interceptors are called one after the other.
     * <p/>
     * v = get property
     * v1 = interceptor3.afterGet(v)
     * v2 = interceptor2.afterGet(v1)
     * v3 = interceptor1.afterGet(v2)
     * ...
     * return v3
     *
     * @param node
     * @param name
     * @param definition
     * @param originalValues @return
     * @throws ValueFormatException
     * @throws VersionException
     * @throws LockException
     * @throws ConstraintViolationException
     * @throws RepositoryException in case of JCR-related errors
     */
    public Value[] beforeSetValues(JCRNodeWrapper node, String name, ExtendedPropertyDefinition definition,
                                   Value[] originalValues)
            throws ValueFormatException, VersionException, LockException, ConstraintViolationException,
            RepositoryException {
        for (PropertyInterceptor interceptor : interceptors) {
            if (interceptor.canApplyOnProperty(node, definition)) {
                originalValues = interceptor.beforeSetValues(node, name, definition, originalValues);
            }
        }
        return originalValues;
    }

    /**
     * Calls all property interceptors in a chain. Interceptors are called one after the other in reverse order.
     * <p/>
     * v1 = interceptor1.beforeSet(v)
     * v2 = interceptor2.beforeSet(v1)
     * v3 = interceptor3.beforeSet(v2)
     * ..
     * property set (v3)
     *
     * @param property
     * @param storedValue
     * @return
     * @throws ValueFormatException
     * @throws RepositoryException in case of JCR-related errors
     */
    public Value afterGetValue(JCRPropertyWrapper property, Value storedValue)
            throws ValueFormatException, RepositoryException {
        for (PropertyInterceptor interceptor : revInterceptors) {
            if (interceptor
                    .canApplyOnProperty(property.getParent(), (ExtendedPropertyDefinition) property.getDefinition())) {
                storedValue = interceptor.afterGetValue(property, storedValue);
            }
        }
        return storedValue;
    }

    /**
     * Calls all property interceptors in a chain. Interceptors are called one after the other in reverse order.
     * <p/>
     * v1 = interceptor1.beforeSet(v)
     * v2 = interceptor2.beforeSet(v1)
     * v3 = interceptor3.beforeSet(v2)
     * ..
     * property set (v3)
     *
     * @param property
     * @param storedValues
     * @return
     * @throws ValueFormatException
     * @throws RepositoryException in case of JCR-related errors
     */
    public Value[] afterGetValues(JCRPropertyWrapper property, Value[] storedValues)
            throws ValueFormatException, RepositoryException {
        for (PropertyInterceptor interceptor : revInterceptors) {
            if (interceptor
                    .canApplyOnProperty(property.getParent(), (ExtendedPropertyDefinition) property.getDefinition())) {
                storedValues = interceptor.afterGetValues(property, storedValues);
            }
        }
        return storedValues;
    }

}
