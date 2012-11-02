package org.jahia.services.content;

import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.jcr.RepositoryException;
import java.util.Locale;

public class PropertyConstraintViolationException extends NodeConstraintViolationException {
    private ExtendedPropertyDefinition definition;

    public PropertyConstraintViolationException(JCRNodeWrapper node, String constraintMessage, Locale locale, ExtendedPropertyDefinition definition) throws RepositoryException {
        super(node.getPath() + " " + definition.getLabel(LocaleContextHolder.getLocale(), node.getPrimaryNodeType()) + ": "+ constraintMessage, node, constraintMessage, locale);
        this.definition = definition;
    }

    public ExtendedPropertyDefinition getDefinition() {
        return definition;
    }

}
