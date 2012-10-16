package org.jahia.services.content;

import javax.jcr.nodetype.ConstraintViolationException;
import java.util.Locale;

public class NodeConstraintViolationException extends ConstraintViolationException {
    private JCRNodeWrapper node;
    private String constraintMessage;
    private Locale locale;

    public NodeConstraintViolationException(JCRNodeWrapper node, String constraintMessage, Locale locale) {
        super(constraintMessage);
        this.node = node;
        this.constraintMessage = constraintMessage;
        this.locale = locale;
    }

    public NodeConstraintViolationException(String message, JCRNodeWrapper node, String constraintMessage, Locale locale) {
        super(message);
        this.node = node;
        this.constraintMessage = constraintMessage;
        this.locale = locale;
    }

    public JCRNodeWrapper getNode() {
        return node;
    }

    public String getConstraintMessage() {
        return constraintMessage;
    }

    public Locale getLocale() {
        return locale;
    }

}
