package org.jahia.services.content;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.PropertyDefinition;

/**
 * User: romain
 * Date: 27 mai 2009
 * Time: 15:07:20
 */
public class JCRPropertyReadOnlyDecorator {

    private Property property;

    public JCRPropertyReadOnlyDecorator(Property prop) {
        this.property = prop;
    }

    public PropertyDefinition getPropertyDefinition() throws RepositoryException {
        return property.getDefinition();
    }

}
