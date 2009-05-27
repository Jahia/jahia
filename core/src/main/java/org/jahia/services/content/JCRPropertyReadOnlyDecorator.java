package org.jahia.services.content;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.PropertyDefinition;

/**
 * This class allows restrained access to a JCR Property object.
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

    public boolean isMultiple() throws RepositoryException {
        return getPropertyDefinition().isMultiple();
    }

    public Value getValue() throws RepositoryException {
        return property.getValue();
    }

    public Value[] getValues() throws RepositoryException {
        return property.getValues();
    }
}
