/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.taglibs.query;

import static javax.jcr.query.qom.QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO;
import static javax.jcr.query.qom.QueryObjectModelConstants.JCR_OPERATOR_GREATER_THAN;
import static javax.jcr.query.qom.QueryObjectModelConstants.JCR_OPERATOR_GREATER_THAN_OR_EQUAL_TO;
import static javax.jcr.query.qom.QueryObjectModelConstants.JCR_OPERATOR_LESS_THAN;
import static javax.jcr.query.qom.QueryObjectModelConstants.JCR_OPERATOR_LESS_THAN_OR_EQUAL_TO;
import static javax.jcr.query.qom.QueryObjectModelConstants.JCR_OPERATOR_LIKE;
import static javax.jcr.query.qom.QueryObjectModelConstants.JCR_OPERATOR_NOT_EQUAL_TO;

import java.util.Map;

import javax.jcr.query.qom.Comparison;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.Literal;
import javax.jcr.query.qom.PropertyValue;
import javax.servlet.jsp.JspTagException;

import org.apache.commons.collections.FastHashMap;
import org.apache.commons.lang.StringUtils;

/**
 * Creates a query constraint, using property comparison.
 * User: hollis
 * Date: 7 nov. 2007
 * Time: 15:33:24
 */
public class ComparisonTag extends ConstraintTag {

    /**
     * Tag used to create an Equal To ConstraintImpl
     *
     * User: hollis
     * Date: 7 nov. 2007
     * Time: 15:33:24
     */
    public static class EqualToTag extends ComparisonTag  {

        private static final long serialVersionUID = 8865525184678009416L;

        @Override
        public String getOperator() {
            return JCR_OPERATOR_EQUAL_TO;
        }
    }
    
    /**
     * Tag used to create a Greater Than Or Equal To ConstraintImpl
     *
     * User: hollis
     * Date: 7 nov. 2007
     * Time: 15:33:24
     */
    public static class GreaterThanOrEqualToTag extends ComparisonTag  {

        private static final long serialVersionUID = -5107469694370118296L;

        @Override
        public String getOperator() {
            return JCR_OPERATOR_GREATER_THAN_OR_EQUAL_TO;
        }

    }
    
    /**
     * Tag used to create a Greater Than ConstraintImpl
     *
     * User: hollis
     * Date: 7 nov. 2007
     * Time: 15:33:24
     */
    public static class GreaterThanTag extends ComparisonTag  {

        private static final long serialVersionUID = -2438321416967009003L;

        @Override
        public String getOperator() {
            return JCR_OPERATOR_GREATER_THAN;
        }
    }
    
    /**
     * Tag used to create a Less Than Or Equal To ConstraintImpl
     *
     * User: hollis
     * Date: 7 nov. 2007
     * Time: 15:33:24
     */
    public static class LessThanOrEqualToTag extends ComparisonTag  {

        private static final long serialVersionUID = 3486347263065135691L;

        @Override
        public String getOperator() {
            return JCR_OPERATOR_LESS_THAN_OR_EQUAL_TO;
        }
    }
    
    /**
     * Tag used to create a Less Than ConstraintImpl
     *
     * User: hollis
     * Date: 7 nov. 2007
     * Time: 15:33:24
     */
    public static class LessThanTag extends ComparisonTag  {

        private static final long serialVersionUID = 5372749170431370477L;

        @Override
        public String getOperator() {
            return JCR_OPERATOR_LESS_THAN;
        }
    }
    
    /**
     * Tag used to create a Like ConstraintImpl
     *
     * User: hollis
     * Date: 7 nov. 2007
     * Time: 15:33:24
     */
    public static class LikeTag extends ComparisonTag  {

        private static final long serialVersionUID = -1074263299926419105L;

        @Override
        public String getOperator() {
            return JCR_OPERATOR_LIKE;
        }
    }
    
    /**
     * Tag used to create an NotImpl Equal To ConstraintImpl
     *
     * User: hollis
     * Date: 7 nov. 2007
     * Time: 15:33:24
     */
    public static class NotEqualToTag extends ComparisonTag  {

        private static final long serialVersionUID = 7245404863830970391L;

        @Override
        public String getOperator() {
            return JCR_OPERATOR_NOT_EQUAL_TO;
        }
    }
    
    private static final long serialVersionUID = -4684686849914698282L;
    
    private static final Map<String, String> OPERATORS;
    
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

    private Comparison comparison;
    private String operator = JCR_OPERATOR_EQUAL_TO;
    private String propertyName;
    private String value;

    public Constraint getConstraint() throws Exception {
        if (comparison != null) {
            return comparison;
        }
        if (StringUtils.isEmpty(propertyName)) {
            throw new JspTagException("Attribute propertyName is empty or null");
        }
        PropertyValue propValue = getQueryFactory().propertyValue(getSelectorName(), propertyName);
        Literal literal = (Literal) getQueryFactory().literal(getValueFactory().createValue(value));
        comparison = getQueryFactory().comparison(propValue, getOperator(), literal);
        return comparison;
    }

    public String getOperator() {
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
        comparison = null;
        operator = JCR_OPERATOR_EQUAL_TO;
        value = null;
        propertyName = null;
        super.resetState();
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
