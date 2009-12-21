package org.jahia.services.content.interceptor;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;

import javax.jcr.*;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.lock.LockException;
import javax.jcr.version.VersionException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 2, 2009
 * Time: 11:38:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class I18NInterceptor implements PropertyInterceptor {
    public boolean canApplyOnProperty(JCRNodeWrapper node, ExtendedPropertyDefinition definition) throws RepositoryException {
        return !definition.isInternationalized() && node.hasNode("j:translation");
    }

    public Value beforeSetValue(JCRNodeWrapper node, String name, ExtendedPropertyDefinition definition, Value originalValue) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        NodeIterator ni = node.getNodes("j:translation");

        // duplicate on all translation nodes

        while (ni.hasNext()) {
            Node translation =  ni.nextNode();
            translation.setProperty(name, originalValue);
        }
        return originalValue;
    }

    public Value[] beforeSetValues(JCRNodeWrapper node, String name, ExtendedPropertyDefinition definition, Value[] originalValues) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        NodeIterator ni = node.getNodes("j:translation");

        // duplicate on all translation nodes

        while (ni.hasNext()) {
            Node translation =  ni.nextNode();
            translation.setProperty(name, originalValues);
        }
        return originalValues;
    }

    public Value afterGetValue(JCRPropertyWrapper property, Value storedValue) throws ValueFormatException, RepositoryException {
        return storedValue;
    }

    public Value[] afterGetValues(JCRPropertyWrapper property, Value[] storedValues) throws ValueFormatException, RepositoryException {
        return storedValues;
    }
}
