package org.jahia.services.content.interceptor;

import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;

import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.lock.LockException;
import javax.jcr.version.VersionException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Calls all property interceptors in a chain. Interceptors are called one after the other.
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
     *
     * v = get property
     * v1 = interceptor3.afterGet(v)
     * v2 = interceptor2.afterGet(v1)
     * v3 = interceptor1.afterGet(v2)
     * ...
     * return v3
     *
     * @param node
     * @param definition
     * @param originalValue
     * @return
     * @throws ValueFormatException
     * @throws VersionException
     * @throws LockException
     * @throws ConstraintViolationException
     * @throws RepositoryException
     */
    public Value beforeSetValue(JCRNodeWrapper node, ExtendedPropertyDefinition definition, Value originalValue) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        if (node.getSession().isInterceptorsEnabled()) {
            for (PropertyInterceptor interceptor : interceptors) {
                if (interceptor.canApplyOnProperty(node, definition))  {
                    originalValue = interceptor.beforeSetValue(node, definition, originalValue);
                }
            }
        }
        return originalValue;
    }

    /**
     * Calls all property interceptors in a chain. Interceptors are called one after the other in reverse order.
     *
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
                if (interceptor.canApplyOnProperty(property.getParent(), (ExtendedPropertyDefinition) property.getDefinition()))  {
                    storedValue = interceptor.afterGetValue(property, storedValue);
                }
            }
        }
        return storedValue;
    }

}
