/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.apache.jackrabbit.core.query.lucene.join;

import java.util.Locale;
import java.util.Map;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.query.Row;
import javax.jcr.query.qom.Operand;
import javax.jcr.query.qom.PropertyValue;

import org.apache.jackrabbit.commons.query.qom.OperandEvaluator;

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
        return getValuesFrom(property);
    }

    private Value[] getValuesFrom(Property property) throws RepositoryException {
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
        return getValuesFrom(property);
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
     * @throws RepositoryException in case of JCR-related errors
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
                } else if (node.getName().startsWith("j:translation_")) {
                    return node.getParent().getProperty(operand.getPropertyName());
                }
            } catch (PathNotFoundException e1) {
            }
            return null;
        }
    }

}
