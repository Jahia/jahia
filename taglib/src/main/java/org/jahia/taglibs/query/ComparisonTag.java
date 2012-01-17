/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.taglibs.query;

import static javax.jcr.query.qom.QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO;
import static javax.jcr.query.qom.QueryObjectModelConstants.JCR_OPERATOR_GREATER_THAN;
import static javax.jcr.query.qom.QueryObjectModelConstants.JCR_OPERATOR_GREATER_THAN_OR_EQUAL_TO;
import static javax.jcr.query.qom.QueryObjectModelConstants.JCR_OPERATOR_LESS_THAN;
import static javax.jcr.query.qom.QueryObjectModelConstants.JCR_OPERATOR_LESS_THAN_OR_EQUAL_TO;
import static javax.jcr.query.qom.QueryObjectModelConstants.JCR_OPERATOR_LIKE;
import static javax.jcr.query.qom.QueryObjectModelConstants.JCR_OPERATOR_NOT_EQUAL_TO;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.DynamicOperand;
import javax.jcr.query.qom.PropertyValue;
import javax.jcr.query.qom.StaticOperand;
import javax.servlet.jsp.JspTagException;

import org.apache.commons.collections.FastHashMap;
import org.apache.commons.lang.StringUtils;

/**
 * Creates a query constraint, using comparison.
 * User: hollis
 * Date: 7 nov. 2007
 * Time: 15:33:24
 */
@SuppressWarnings("unchecked")
public class ComparisonTag extends ConstraintTag {

    /**
     * Defines allowed dynamic operand types to be applied for the left operand
     * of the comparison.
     * 
     * @author Sergiy Shyrkov
     */
    public enum OperandType {
        FULLTEXTSEARCHSCORE, LENGTH, LOWERCASE, NODELOCALNAME, NODENAME, PROPERTYVALUE, UPPERCASE;
    }

    private static final List<OperandType> DEF_OPERANDS = new LinkedList<OperandType>(Arrays
            .asList(new OperandType[] { OperandType.PROPERTYVALUE }));

    private static final Map<String, String> OPERATORS;

    private static final long serialVersionUID = -4684686849914698282L;

    static {
        FastHashMap ops = new FastHashMap(8);
        ops.put("=", JCR_OPERATOR_EQUAL_TO);
        ops.put(">", JCR_OPERATOR_GREATER_THAN);
        ops.put(">=", JCR_OPERATOR_GREATER_THAN_OR_EQUAL_TO);
        ops.put("<", JCR_OPERATOR_LESS_THAN);
        ops.put("<=", JCR_OPERATOR_LESS_THAN_OR_EQUAL_TO);
        ops.put("like", JCR_OPERATOR_LIKE);
        ops.put("!=", JCR_OPERATOR_NOT_EQUAL_TO);
        ops.put("<>", JCR_OPERATOR_NOT_EQUAL_TO);
        ops.setFast(true);
        OPERATORS = ops;
    }

    private List<OperandType> operandTypes = DEF_OPERANDS;
    private String operator = JCR_OPERATOR_EQUAL_TO;
    private String propertyName;
    private String value;

    @Override
    public Constraint getConstraint() throws Exception {
        return getQOMFactory().comparison(getOperand1(), getOperator(), getOperand2());
    }

    protected DynamicOperand getOperand1() throws InvalidQueryException, JspTagException, RepositoryException {
        DynamicOperand result = null;
        for (OperandType opType : operandTypes) {
            switch (opType) {
            case FULLTEXTSEARCHSCORE:
                result = getQOMFactory().fullTextSearchScore(getSelectorName());
                break;
            case LENGTH:
                if (result == null || !(result instanceof PropertyValue)) {
                    throw new IllegalArgumentException(
                            "Cannot find an operand to apply the 'Length' operand type to it."
                                    + " It must be preceded by PropertyValue operand.");
                }
                result = getQOMFactory().length((PropertyValue) result);
                break;
            case LOWERCASE:
                if (result == null) {
                    throw new IllegalArgumentException(
                            "Cannot find an operand to apply the 'LowerCase' operand type."
                                    + " It must be preceded by either PropertyValue, NodeName or NodeLocalName.");
                }
                result = getQOMFactory().lowerCase(result);
                break;
            case NODELOCALNAME:
                result = getQOMFactory().nodeLocalName(getSelectorName());
                break;
            case NODENAME:
                result = getQOMFactory().nodeName(getSelectorName());
                break;
            case PROPERTYVALUE:
                if (StringUtils.isEmpty(propertyName)) {
                    throw new IllegalArgumentException("propertyName attribute value is required for this constraint");
                }
                result = getQOMFactory().propertyValue(getSelectorName(), propertyName);
                break;
            case UPPERCASE:
                if (result == null) {
                    throw new IllegalArgumentException(
                            "Cannot find an operand to apply the 'UpperCase' operand type."
                                    + " It must be preceded by either PropertyValue, NodeName or NodeLocalName.");
                }
                result = getQOMFactory().upperCase(result);
                break;
            default:
                throw new IllegalArgumentException("Unknown DynamicOperand type '" + opType + "'");
            }
        }
        return result;
    }

    protected StaticOperand getOperand2() throws InvalidQueryException, JspTagException, RepositoryException {
        return getQOMFactory().literal(getQOMBuilder().getValueFactory().createValue(value));
    }

    protected String getOperator() {
        if (!operator.startsWith("{http://www.jcp.org/jcr/1.0}")) {
            String resolvedOperator = OPERATORS.get(operator.toLowerCase().trim());
            if (resolvedOperator != null) {
                operator = resolvedOperator;
            }
        }
        return operator;
    }

    @Override
    protected void resetState() {
        operandTypes = DEF_OPERANDS;
        operator = JCR_OPERATOR_EQUAL_TO;
        value = null;
        propertyName = null;
        super.resetState();
    }

    /**
     * Sets the sequence (a comma-separated string) of dynamic operand types to
     * be applied on the operand1 (left operand of the comparison).
     * 
     * @param appliedOperandsSequence the sequence (a comma-separated string) of
     *            dynamic operand types to be applied on the operand1 (left
     *            operand of the comparison)
     */
    public void setOperandTypes(String appliedOperandsSequence) {
        if (StringUtils.isEmpty(appliedOperandsSequence)) {
            throw new IllegalArgumentException("appliedOperands attribute value is required for this tag.");
        }
        List<OperandType> types = new LinkedList<OperandType>();
        appliedOperandsSequence = appliedOperandsSequence.toUpperCase();
        if (appliedOperandsSequence.contains(",")) {
            for (String op : appliedOperandsSequence.split(",")) {
                types.add(OperandType.valueOf(op.trim()));
            }
        } else {
            OperandType opType = OperandType.valueOf(appliedOperandsSequence);
            if (opType == OperandType.PROPERTYVALUE) {
                types = DEF_OPERANDS;
            } else {
                types.add(opType);
            }
        }
        this.operandTypes = types;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
