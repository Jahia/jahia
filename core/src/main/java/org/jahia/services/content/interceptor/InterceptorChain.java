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
     * @throws RepositoryException
     */
    public Value beforeSetValue(JCRNodeWrapper node, String name, ExtendedPropertyDefinition definition, Value originalValue) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (node.getSession().isInterceptorsEnabled()) {
            for (PropertyInterceptor interceptor : interceptors) {
                if (interceptor.canApplyOnProperty(node, definition)) {
                    originalValue = interceptor.beforeSetValue(node, name, definition, originalValue);
                }
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
     * @throws RepositoryException
     */
    public Value[] beforeSetValues(JCRNodeWrapper node, String name, ExtendedPropertyDefinition definition, Value[] originalValues) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (node.getSession().isInterceptorsEnabled()) {
            for (PropertyInterceptor interceptor : interceptors) {
                if (interceptor.canApplyOnProperty(node, definition)) {
                    originalValues = interceptor.beforeSetValues(node, name, definition, originalValues);
                }
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
     * @throws RepositoryException
     */
    public Value afterGetValue(JCRPropertyWrapper property, Value storedValue) throws ValueFormatException, RepositoryException {
        if (property.getSession().isInterceptorsEnabled()) {
            for (PropertyInterceptor interceptor : revInterceptors) {
                if (interceptor.canApplyOnProperty(property.getParent(), (ExtendedPropertyDefinition) property.getDefinition())) {
                    storedValue = interceptor.afterGetValue(property, storedValue);
                }
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
     * @throws RepositoryException
     */
    public Value[] afterGetValues(JCRPropertyWrapper property, Value[] storedValues) throws ValueFormatException, RepositoryException {
        if (property.getSession().isInterceptorsEnabled()) {
            for (PropertyInterceptor interceptor : revInterceptors) {
                if (interceptor.canApplyOnProperty(property.getParent(), (ExtendedPropertyDefinition) property.getDefinition())) {
                    storedValues = interceptor.afterGetValues(property, storedValues);
                }
            }
        }
        return storedValues;
    }

}
