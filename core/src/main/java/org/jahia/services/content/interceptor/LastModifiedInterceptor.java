package org.jahia.services.content.interceptor;

import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 5, 2010
 * Time: 6:08:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class LastModifiedInterceptor implements PropertyInterceptor {
    public boolean canApplyOnProperty(JCRNodeWrapper node, ExtendedPropertyDefinition definition)
            throws RepositoryException {
        return definition.getDeclaringNodeType().getName().equals(Constants.MIX_LAST_MODIFIED) && node.getSession().getLocale() != null;
    }

    public void beforeRemove(JCRNodeWrapper node, String name, ExtendedPropertyDefinition definition)
            throws VersionException, LockException, ConstraintViolationException, RepositoryException {
    }

    public Value beforeSetValue(JCRNodeWrapper node, String name, ExtendedPropertyDefinition definition,
                                Value originalValue)
            throws ValueFormatException, VersionException, LockException, ConstraintViolationException,
            RepositoryException {
        return originalValue;
    }

    public Value[] beforeSetValues(JCRNodeWrapper node, String name, ExtendedPropertyDefinition definition,
                                   Value[] originalValues)
            throws ValueFormatException, VersionException, LockException, ConstraintViolationException,
            RepositoryException {
        return originalValues;
    }

    public Value afterGetValue(JCRPropertyWrapper property, Value storedValue)
            throws ValueFormatException, RepositoryException {
        try {
            Node i18n = property.getParent().getI18N(property.getSession().getLocale());
            if (i18n.hasProperty("jcr:lastModified")) {
                final boolean isLM = property.getName().equals("jcr:lastModified");
                Value lastModified = isLM ? storedValue :
                        property.getParent().getRealNode().getProperty(Constants.JCR_LASTMODIFIED).getValue();
                Value i18nLastModified = i18n.getProperty(Constants.JCR_LASTMODIFIED).getValue();
                if (i18nLastModified.getDate().after(lastModified.getDate())) {
                    return isLM ? i18nLastModified : i18n.getProperty(property.getName()).getValue();
                }
            }
        } catch (ItemNotFoundException e) {
        }
        return storedValue;
    }

    public Value[] afterGetValues(JCRPropertyWrapper property, Value[] storedValues)
            throws ValueFormatException, RepositoryException {
        return storedValues;
    }
}
