package org.apache.jackrabbit.core.query.lucene.join;

import org.apache.jackrabbit.commons.query.qom.OperandEvaluator;

import javax.jcr.*;
import javax.jcr.query.Row;
import javax.jcr.query.qom.*;
import java.util.Locale;
import java.util.Map;

public class JahiaOperandEvaluator extends OperandEvaluator {

    /** The locale to use in upper- and lower-case conversion. */
    private final Locale locale;

    public JahiaOperandEvaluator(ValueFactory factory, Map<String, Value> variables, Locale locale) {
        super(factory, variables, locale != null ? locale : Locale.ENGLISH);
        this.locale = locale;
    }

    /**
     * Evaluates the given operand in the context of the given row.
     *
     * @param operand operand to be evaluated
     * @param row query result row
     * @return values of the operand at the given row
     * @throws RepositoryException if the operand can't be evaluated
     */
    public Value[] getValues(Operand operand, Row row)
            throws RepositoryException {
        if (operand instanceof PropertyValue) {
            return getPropertyValues((PropertyValue) operand, row);
        }
        return super.getValues(operand, row);
    }

    /**
     * Evaluates the given operand in the context of the given node.
     *
     * @param operand operand to be evaluated
     * @param node node
     * @return values of the operand at the given node
     * @throws RepositoryException if the operand can't be evaluated
     */
    public Value[] getValues(Operand operand, Node node)
            throws RepositoryException {
        if (operand instanceof PropertyValue) {
            return getPropertyValues((PropertyValue) operand, node);
        }
        return super.getValues(operand, node);
    }

    /**
     * Returns the values of the given property value operand at the given row.
     *
     * @see #getProperty(PropertyValue, Row)
     * @param operand property value operand
     * @param row row
     * @return values of the operand at the given row
     * @throws RepositoryException if the operand can't be evaluated
     */
    private Value[] getPropertyValues(PropertyValue operand, Row row)
            throws RepositoryException {
        Property property = getProperty(operand, row);
        if (property == null) {
            return new Value[0];
        } else if (property.isMultiple()) {
            return property.getValues();
        } else {
            return new Value[] { property.getValue() };
        }
    }

    private Value[] getPropertyValues(PropertyValue operand, Node node)
            throws RepositoryException {
        Property property = getProperty(operand, node);
        if (property == null) {
            return new Value[0];
        } else if (property.isMultiple()) {
            return property.getValues();
        } else {
            return new Value[] { property.getValue() };
        }
    }

    /**
     * Returns the identified property from the given row. This method
     * is used by both the getValue(Length, Row) and the
     * getValue(PropertyValue, Row) methods to access properties.
     *
     * @param operand property value operand
     * @param row row
     * @return the identified property,
     *         or <code>null</code> if the property does not exist
     * @throws RepositoryException if the property can't be accessed
     */
    private Property getProperty(PropertyValue operand, Row row)
            throws RepositoryException {
        return getProperty(operand, row.getNode(operand.getSelectorName()));
    }

    /**
     * Returns the identified property from the given node.
     *
     * Can return <code>null</code> is the property doesn't exist or it is not
     * accessible.
     *
     * @param operand
     * @param node
     * @return identified property
     * @throws RepositoryException
     */
    private Property getProperty(PropertyValue operand, Node node)
            throws RepositoryException {
        if (node == null) {
            return null;
        }
        try {
            return node.getProperty(operand.getPropertyName());
        } catch (PathNotFoundException e) {
            try {
                if (locale != null && node.hasNode("j:translation_"+locale)) {
                    return node.getNode("j:translation_"+locale).getProperty(operand.getPropertyName());
                }
            } catch (PathNotFoundException e1) {
            }
            return null;
        }
    }

}
