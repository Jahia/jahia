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
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Nov 27, 2009
 * Time: 1:58:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class InterceptorChain {
    private List<PropertyInterceptor> interceptors = new ArrayList<PropertyInterceptor>();
    private List<PropertyInterceptor> revInterceptors;
      
    public void setInterceptors(List<PropertyInterceptor> interceptors) {
        this.interceptors = interceptors;
        revInterceptors = new ArrayList<PropertyInterceptor>(interceptors);
        Collections.reverse(revInterceptors);
    }

    public Value beforeSetValue(JCRNodeWrapper node, ExtendedPropertyDefinition definition, Value originalValue) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        for (PropertyInterceptor interceptor : interceptors) {
            if (interceptor.canApplyOnProperty(node, definition))  {
                originalValue = interceptor.beforeSetValue(node, definition, originalValue);
            }
        }
        return originalValue;
    }

    public Value afterGetValue(JCRPropertyWrapper property, Value storedValue) throws ValueFormatException, RepositoryException {
        for (PropertyInterceptor interceptor : revInterceptors) {
            if (interceptor.canApplyOnProperty(property.getParent(), (ExtendedPropertyDefinition) property.getDefinition()))  {
                storedValue = interceptor.afterGetValue(property, storedValue);
            }
        }
        return storedValue;
    }

}
