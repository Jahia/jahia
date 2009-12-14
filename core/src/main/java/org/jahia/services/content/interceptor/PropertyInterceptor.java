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
