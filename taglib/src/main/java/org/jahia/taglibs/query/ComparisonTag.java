/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
import org.jahia.utils.Patterns;

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
            for (String op : Patterns.COMMA.split(appliedOperandsSequence)) {
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
