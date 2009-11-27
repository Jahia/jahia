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
 * Interceptors are called one after the other
 *
 * Setting a property :
 *
 * v1 = interceptor1.beforeSet(v)
 * v2 = interceptor2.beforeSet(v1)
 * v3 = interceptor3.beforeSet(v2)
 * ..
 * property set (v3)
 *
 * Getting a property :
 *
 * v = get property
 * v1 = interceptor3.afterGet(v)
 * v2 = interceptor2.afterGet(v1)
 * v3 = interceptor1.afterGet(v2)
 * ...
 * return v3
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

    /**
     * Called before setting the value on the property. Can throw an exception if the value is not valid, and transform
     * the value into another value.
     *
     * The interceptor can also directly operate on the property before the property is effectively set.
     *
     * Returns the value to set - or null if no property need to be set, but without sending an error.
     *
     * @param node
     *@param definition
     * @param originalValue Original value  @return Value to set, or null
     * @throws ValueFormatException
     * @throws VersionException
     * @throws LockException
     * @throws ConstraintViolationException
     */
    Value beforeSetValue(JCRNodeWrapper node, ExtendedPropertyDefinition definition, Value originalValue) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException ;

//    /**
//     * Called before getting the value -
//     * @param property
//     * @return
//     */
//    Value beforeGetValue(JCRPropertyWrapper property);

    /**
     * Called after getting the value. Stored value is passed to the interceptor and can be transformed.
     *
     * @param property 
     * @param storedValue
     * @return
     */
    Value afterGetValue(JCRPropertyWrapper property, Value storedValue) throws ValueFormatException, RepositoryException;



}
