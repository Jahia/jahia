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
package org.jahia.services.content.interceptor;

import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;

import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

/**
 * Property interceptor catch get and set on properties. They can transform the value and veto a set operation, and
 * retransform it back when getting values.
 *
 * An interceptor is called or not on a property, based on the parent node and property definition.
 *
 * Interceptors are called only in localized sessions.
 *
 */
public interface PropertyInterceptor {

    /**
     * Checks if this interceptor need to be called on that property.
     *
     * @param node
     *@param definition @return
     */
    boolean canApplyOnProperty(JCRNodeWrapper node, ExtendedPropertyDefinition definition) throws RepositoryException;

    void beforeRemove(JCRNodeWrapper node, String name, ExtendedPropertyDefinition definition) throws VersionException, LockException, ConstraintViolationException, RepositoryException;

    /**
     * Called before setting the value on the property. Can throw an exception if the value is not valid, and transform
     * the value into another value.
     *
     * The interceptor can also directly operate on the property before the property is effectively set.
     *
     * Returns the value to set - or null if no property need to be set, but without sending an error.
     *
     * @param node
     *@param name
     * @param definition
     * @param originalValue Original value  @return Value to set, or null   @throws ValueFormatException
     * @throws VersionException
     * @throws LockException
     * @throws ConstraintViolationException
     */
    Value beforeSetValue(JCRNodeWrapper node, String name, ExtendedPropertyDefinition definition, Value originalValue) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException ;

    /**
     * Called before setting the value on the property. Can throw an exception if the value is not valid, and transform
     * the value into another value.
     *
     * The interceptor can also directly operate on the property before the property is effectively set.
     *
     * Returns the value to set - or null if no property need to be set, but without sending an error.
     *
     * @param node
     *@param name
     * @param definition
     * @param originalValues Original value  @return Value to set, or null   @throws ValueFormatException
     * @throws VersionException
     * @throws LockException
     * @throws ConstraintViolationException
     */
    Value[] beforeSetValues(JCRNodeWrapper node, String name, ExtendedPropertyDefinition definition, Value[] originalValues) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException ;

    /**
     * Called after getting the value. Stored value is passed to the interceptor and can be transformed.
     *
     * @param property 
     * @param storedValue
     * @return
     */
    Value afterGetValue(JCRPropertyWrapper property, Value storedValue) throws ValueFormatException, RepositoryException;

    /**
     * Called after getting the value. Stored value is passed to the interceptor and can be transformed.
     *
     * @param property
     * @param storedValues
     * @return
     */
    Value[] afterGetValues(JCRPropertyWrapper property, Value[] storedValues) throws ValueFormatException, RepositoryException;



}
