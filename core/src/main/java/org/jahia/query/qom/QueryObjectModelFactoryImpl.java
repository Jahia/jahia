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
package org.jahia.query.qom;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.QueryResult;

import org.apache.jackrabbit.spi.commons.query.jsr283.qom.And;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.BindVariableValue;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.ChildNode;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.ChildNodeJoinCondition;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Column;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Comparison;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Constraint;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.DescendantNode;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.DescendantNodeJoinCondition;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.DynamicOperand;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.EquiJoinCondition;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.FullTextSearch;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.FullTextSearchScore;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Join;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.JoinCondition;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Length;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Literal;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.LowerCase;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.NodeLocalName;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.NodeName;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Not;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Or;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Ordering;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.PropertyExistence;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.PropertyValue;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.QueryObjectModel;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.QueryObjectModelConstants;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.QueryObjectModelFactory;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.SameNode;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.SameNodeJoinCondition;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Selector;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Source;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.StaticOperand;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.UpperCase;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.expressions.SearchExpressionContext;
import org.jahia.services.usermanager.JahiaUser;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 7 nov. 2007
 * Time: 09:57:44
 * To change this template use File | Settings | File Templates.
 */
public class QueryObjectModelFactoryImpl implements QueryObjectModelFactory {

    private Properties properties;
    private SearchExpressionContext searchExpressionContext;
    private JahiaUser user;
    private QueryExecute queryExecute;

    /**
     *
     * @param queryExecute
     * @param properties
     * @param searchExpressionContext
     * @param user
     */
    public QueryObjectModelFactoryImpl(QueryExecute queryExecute, Properties properties, SearchExpressionContext searchExpressionContext,
                                       JahiaUser user) {
        this.queryExecute = queryExecute;
        this.properties = properties;
        if (this.properties == null) {
            this.properties = new Properties();
        }
        this.searchExpressionContext = searchExpressionContext;
        this.user = user;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * method for creating a query with multiple selectors
     *
     * @param selector
     * @param constraint
     * @param orderings
     * @param columns
     * @return
     * @throws InvalidQueryException
     * @throws RepositoryException
     */
    public QueryObjectModel createQuery(Source selector, Constraint constraint,
                                        Ordering[] orderings, Column[] columns)
            throws InvalidQueryException, RepositoryException {

        OrderingImpl[] orderingImpls = new OrderingImpl[]{};
        if (orderings != null) {
            List<Ordering> orderingsList = Arrays.asList(orderings);
            orderingImpls = (OrderingImpl[]) orderingsList.toArray(orderingImpls);
        }
        return new QueryObjectModelImpl(this,this.queryExecute,selector, constraint, orderingImpls, properties);
    }

    /**
     * method for creating a query with one single selector
     *
     * @param selector
     * @param constraint
     * @param orderings
     * @param columns
     * @return
     * @throws InvalidQueryException
     * @throws RepositoryException
     */
    public QueryObjectModel createQuery(Selector selector, Constraint constraint,
                                        Ordering[] orderings, Column[] columns)
            throws InvalidQueryException, RepositoryException {
        return new QueryObjectModelImpl(this,this.queryExecute,selector, constraint, orderings, properties);
    }

    /**
     * Selects a subset of the nodes in the repository based on node type.
     * <p/>
     * The selector name is the node type name.
     *
     * @param nodeTypeName the name of the required node type; non-null
     * @return the selector; non-null
     * @throws InvalidQueryException if the query is invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public Selector selector(String nodeTypeName)
            throws InvalidQueryException, RepositoryException {
        return new SelectorImpl(nodeTypeName, null);
    }

    /**
     * Selects a subset of the nodes in the repository based on node type.
     *
     * @param nodeTypeName the name of the required node type; non-null
     * @param selectorName the selector name; non-null
     * @return the selector; non-null
     * @throws InvalidQueryException if the query is invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public Selector selector(String nodeTypeName, String selectorName)
            throws InvalidQueryException, RepositoryException {
        return new SelectorImpl(nodeTypeName, selectorName);
    }

    ///
    /// JOIN
    ///

    /**
     * Performs a join between two node-tuple sources.
     *
     * @param left          the left node-tuple source; non-null
     * @param right         the right node-tuple source; non-null
     * @param joinType      either
     *                      <ul>
     *                      <li>{@link QueryObjectModelConstants#JOIN_TYPE_INNER},</li>
     *                      <li>{@link QueryObjectModelConstants#JOIN_TYPE_LEFT_OUTER},</li>
     *                      <li>{@link QueryObjectModelConstants#JOIN_TYPE_RIGHT_OUTER}</li>
     *                      </ul>
     * @param joinCondition the join condition; non-null
     * @return the join; non-null
     * @throws InvalidQueryException if the query is invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public Join join(
            Source left,
            Source right,
            int joinType,
            JoinCondition joinCondition
    ) throws InvalidQueryException, RepositoryException {
        //@todo not implemented yet
        return null;
    }

    ///
    /// JOINCONDITION
    ///

    /**
     * Tests whether the value of a property in a first selector is equal to the
     * value of a property in a second selector.
     *
     * @param selector1Name the name of the first selector; non-null
     * @param property1Name the property name in the first selector; non-null
     * @param selector2Name the name of the second selector; non-null
     * @param property2Name the property name in the second selector; non-null
     * @return the constraint; non-null
     * @throws InvalidQueryException if the query is invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public EquiJoinCondition equiJoinCondition
    (
        String selector1Name,
        String property1Name,
        String selector2Name,
        String property2Name
    ) throws InvalidQueryException, RepositoryException {
        //@todo not implemented yet
        return null;
    }

    /**
     * Tests whether a first selector's node is the same as a second selector's
     * node.
     *
     * @param selector1Name the name of the first selector; non-null
     * @param selector2Name the name of the second selector; non-null
     * @return the constraint; non-null
     * @throws InvalidQueryException if the query is invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public SameNodeJoinCondition sameNodeJoinCondition
    (
            String selector1Name,
            String selector2Name
    ) throws InvalidQueryException, RepositoryException {
        //@todo not implemented yet
        return null;
    }

    /**
     * Tests whether a first selector's node is the same as a node identified
     * by relative path from a second selector's node.
     *
     * @param selector1Name the name of the first selector; non-null
     * @param selector2Name the name of the second selector; non-null
     * @param selector2Path the path relative to the second selector; non-null
     * @return the constraint; non-null
     * @throws InvalidQueryException if the query is invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public SameNodeJoinCondition sameNodeJoinCondition
    (
            String selector1Name,
            String selector2Name,
            String selector2Path
    ) throws InvalidQueryException, RepositoryException {
        //@todo not implemented yet
        return null;
    }

    /**
     * Tests whether a first selector's node is a child of a second selector's
     * node.
     *
     * @param childSelectorName  the name of the child selector; non-null
     * @param parentSelectorName the name of the parent selector; non-null
     * @return the constraint; non-null
     * @throws InvalidQueryException if the query is invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public ChildNodeJoinCondition childNodeJoinCondition
            (
                    String childSelectorName,
                    String parentSelectorName
            ) throws InvalidQueryException, RepositoryException {
        //@todo not implemented yet
        return null;
    }

    /**
     * Tests whether a first selector's node is a descendant of a second
     * selector's node.
     *
     * @param descendantSelectorName the name of the descendant selector; non-null
     * @param ancestorSelectorName   the name of the ancestor selector; non-null
     * @return the constraint; non-null
     * @throws InvalidQueryException if the query is invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public DescendantNodeJoinCondition descendantNodeJoinCondition
            (
                    String descendantSelectorName,
                    String ancestorSelectorName
            ) throws InvalidQueryException, RepositoryException {
        //@todo not implemented yet
        return null;
    }

    ///
    /// CONSTRAINT
    ///

    /**
     * Performs a logical conjunction of two other constraints.
     *
     * @param constraint1 the first constraint; non-null
     * @param constraint2 the second constraint; non-null
     * @return the <code>And</code> constraint; non-null
     * @throws InvalidQueryException if the query is invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public And and(Constraint constraint1, Constraint constraint2)
            throws InvalidQueryException, RepositoryException {
        return new AndImpl((ConstraintImpl) constraint1, (ConstraintImpl) constraint2);
    }

    /**
     * Performs a logical disjunction of two other constraints.
     *
     * @param constraint1 the first constraint; non-null
     * @param constraint2 the second constraint; non-null
     * @return the <code>Or</code> constraint; non-null
     * @throws InvalidQueryException if the query is invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public Or or(Constraint constraint1, Constraint constraint2)
            throws InvalidQueryException, RepositoryException {
        return new OrImpl((ConstraintImpl) constraint1, (ConstraintImpl) constraint2);
    }

    /**
     * Performs a logical negation of another constraint.
     *
     * @param constraint the constraint to be negated; non-null
     * @return the <code>Not</code> constraint; non-null
     * @throws InvalidQueryException if the query is invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public Not not(Constraint constraint)
            throws InvalidQueryException, RepositoryException {
        return new NotImpl((ConstraintImpl) constraint);
    }

    /**
     * Filters node-tuples based on the outcome of a binary operation.
     *
     * @param operand1 the first operand; non-null
     * @param operator the operator; either
     *                 <ul>
     *                 <li>{@link #OPERATOR_EQUAL_TO},</li>
     *                 <li>{@link #OPERATOR_NOT_EQUAL_TO},</li>
     *                 <li>{@link #OPERATOR_LESS_THAN},</li>
     *                 <li>{@link #OPERATOR_LESS_THAN_OR_EQUAL_TO},</li>
     *                 <li>{@link #OPERATOR_GREATER_THAN},</li>
     *                 <li>{@link #OPERATOR_GREATER_THAN_OR_EQUAL_TO}, or</li>
     *                 <li>{@link #OPERATOR_LIKE}</li>
     *                 </ul>
     * @param operand2 the second operand; non-null
     * @return the constraint; non-null
     * @throws InvalidQueryException if the query is invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public Comparison comparison
            (
                    DynamicOperand operand1,
                    int operator,
                    StaticOperand operand2
            ) throws InvalidQueryException, RepositoryException {
        return new ComparisonImpl((PropertyValueImpl) operand1, operator, (StaticOperandImpl) operand2);
    }

    /**
     * Tests the existence of a property in the default selector.
     *
     * @param propertyName the property name; non-null
     * @return the constraint; non-null
     * @throws InvalidQueryException if the query has no default selector
     *                               or is otherwise invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public PropertyExistence propertyExistence(String propertyName)               // CM
            throws InvalidQueryException, RepositoryException {
        //@todo not implemented yet
        return null;
    }

    /**
     * Tests the existence of a property in the specified selector.
     *
     * @param selectorName the selector name; non-null
     * @param propertyName the property name; non-null
     * @return the constraint; non-null
     * @throws InvalidQueryException if the query is invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public PropertyExistence propertyExistence
            (
                    String selectorName,
                    String propertyName
            ) throws InvalidQueryException, RepositoryException {
        //@todo not implemented yet
        return null;
    }

    /**
     * Performs a full-text search against the default selector.
     *
     * @param propertyName             the property name, or null to search all
     *                                 full-text indexed properties of the node
     *                                 (or node subtree, in some implementations)
     * @param fullTextSearchExpression the full-text search expression; non-null
     * @return the constraint; non-null
     * @throws InvalidQueryException if the query has no default selector
     *                               or is otherwise invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public FullTextSearch fullTextSearch
            (
                    String propertyName,
                    String fullTextSearchExpression
            ) throws InvalidQueryException, RepositoryException {
        return new FullTextSearchImpl(propertyName, fullTextSearchExpression);
    }

    /**
     * Performs a full-text search against the specified selector.
     *
     * @param selectorName             the selector name; non-null
     * @param propertyName             the property name, or null to search all
     *                                 full-text indexed properties of the node
     *                                 (or node subtree, in some implementations)
     * @param fullTextSearchExpression the full-text search expression; non-null
     * @return the constraint; non-null
     * @throws InvalidQueryException if the query is invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public FullTextSearch fullTextSearch
            (
                    String selectorName,
                    String propertyName,
                    String fullTextSearchExpression
            ) throws InvalidQueryException, RepositoryException {
        return new FullTextSearchImpl(selectorName, propertyName, fullTextSearchExpression);
    }

    /**
     * Tests whether a node in the default selector is reachable by a specified
     * absolute path.
     *
     * @param path an absolute path; non-null
     * @return the constraint; non-null
     * @throws InvalidQueryException if the query has no default selector
     *                               or is otherwise invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public SameNode sameNode(String path)
            throws InvalidQueryException, RepositoryException {
        //@todo not implemented yet
        return null;
    }

    /**
     * Tests whether a node in the specified selector is reachable by a specified
     * absolute path.
     *
     * @param selectorName the selector name; non-null
     * @param path         an absolute path; non-null
     * @return the constraint; non-null
     * @throws InvalidQueryException if the query is invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public SameNode sameNode(String selectorName, String path)
            throws InvalidQueryException, RepositoryException {
        //@todo not implemented yet
        return null;
    }

    /**
     * Tests whether a node in the default selector is a child of a node
     * reachable by a specified absolute path.
     *
     * @param path an absolute path; non-null
     * @return the constraint; non-null
     * @throws InvalidQueryException if the query has no default selector
     *                               or is otherwise invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public ChildNode childNode(String path)
            throws InvalidQueryException, RepositoryException {
        return new ChildNodeImpl(null, path);
    }

    /**
     * Tests whether a node in the specified selector is a child of a node
     * reachable by a specified absolute path.
     *
     * @param selectorName the selector name; non-null
     * @param path         an absolute path; non-null
     * @return the constraint; non-null
     * @throws InvalidQueryException if the query is invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public ChildNode childNode(String selectorName, String path)
            throws InvalidQueryException, RepositoryException {
        return new ChildNodeImpl(selectorName, path);
    }

    /**
     * Tests whether a node in the default selector is a descendant of a node
     * reachable by a specified absolute path.
     *
     * @param path an absolute path; non-null
     * @return the constraint; non-null
     * @throws InvalidQueryException if the query has no default selector
     *                               or is otherwise invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public DescendantNode descendantNode(String path)
            throws InvalidQueryException, RepositoryException {
        return new DescendantNodeImpl(null, path);
    }

    /**
     * Tests whether a node in the specified selector is a descendant of a node
     * reachable by a specified absolute path.
     *
     * @param selectorName the selector name; non-null
     * @param path         an absolute path; non-null
     * @return the constraint; non-null
     * @throws InvalidQueryException if the query is invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public DescendantNode descendantNode(String selectorName, String path)
            throws InvalidQueryException, RepositoryException {
        return new DescendantNodeImpl(selectorName, path);
    }

    ///
    /// OPERAND
    ///

    /**
     * Evaluates to the value (or values, if multi-valued) of a property of
     * the default selector.
     *
     * @param propertyName the property name; non-null
     * @return the operand; non-null
     * @throws InvalidQueryException if the query has no default selector
     *                               or is otherwise invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public PropertyValue propertyValue(String propertyName)                       // CM
            throws InvalidQueryException, RepositoryException {
        return new PropertyValueImpl(null, propertyName);
    }

    /**
     * Evaluates to the value (or values, if multi-valued) of a property in the
     * specified selector.
     *
     * @param selectorName the selector name; non-null
     * @param propertyName the property name; non-null
     * @return the operand; non-null
     * @throws InvalidQueryException if the query is invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public PropertyValue propertyValue(String selectorName, String propertyName)
            throws InvalidQueryException, RepositoryException {
        return new PropertyValueImpl(selectorName, propertyName);
    }

    /**
     * Evaluates to the length (or lengths, if multi-valued) of a property.
     *
     * @param propertyValue the property value for which to compute the length;
     *                      non-null
     * @return the operand; non-null
     * @throws InvalidQueryException if the query is invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public Length length(PropertyValue propertyValue)
            throws InvalidQueryException, RepositoryException {
        //@todo not implemented yet
        return null;
    }

    /**
     * Evaluates to a <code>NAME</code> value equal to the prefix-qualified name
     * of a node in the default selector.
     *
     * @return the operand; non-null
     * @throws InvalidQueryException if the query has no default selector
     *                               or is otherwise invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public NodeName nodeName()                                                    // CM
            throws InvalidQueryException, RepositoryException {
        //@todo not implemented yet
        return null;
    }

    /**
     * Evaluates to a <code>NAME</code> value equal to the prefix-qualified name
     * of a node in the specified selector.
     *
     * @param selectorName the selector name; non-null
     * @return the operand; non-null
     * @throws InvalidQueryException if the query is invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public NodeName nodeName(String selectorName)
            throws InvalidQueryException, RepositoryException {
        //@todo not implemented yet
        return null;
    }

    /**
     * Evaluates to a <code>NAME</code> value equal to the local (unprefixed)
     * name of a node in the default selector.
     *
     * @return the operand; non-null
     * @throws InvalidQueryException if the query has no default selector
     *                               or is otherwise invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public NodeLocalName nodeLocalName()                                          // CM
            throws InvalidQueryException, RepositoryException {
        //@todo not implemented yet
        return null;
    }

    /**
     * Evaluates to a <code>NAME</code> value equal to the local (unprefixed)
     * name of a node in the specified selector.
     *
     * @param selectorName the selector name; non-null
     * @return the operand; non-null
     * @throws InvalidQueryException if the query is invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public NodeLocalName nodeLocalName(String selectorName)
            throws InvalidQueryException, RepositoryException {
        //@todo not implemented yet
        return null;
    }

    /**
     * Evaluates to a <code>DOUBLE</code> value equal to the full-text search
     * score of a node in the default selector.
     *
     * @return the operand; non-null
     * @throws InvalidQueryException if the query has no default selector
     *                               or is otherwise invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public FullTextSearchScore fullTextSearchScore()                              // CM
            throws InvalidQueryException, RepositoryException {
        //@todo not implemented yet
        return null;
    }

    /**
     * Evaluates to a <code>DOUBLE</code> value equal to the full-text search
     * score of a node in the specified selector.
     *
     * @param selectorName the selector name; non-null
     * @return the operand; non-null
     * @throws InvalidQueryException if the query is invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public FullTextSearchScore fullTextSearchScore(String selectorName)
            throws InvalidQueryException, RepositoryException {
        //@todo not implemented yet
        return null;
    }

    /**
     * Evaluates to the lower-case string value (or values, if multi-valued)
     * of an operand.
     *
     * @param operand the operand whose value is converted to a
     *                lower-case string; non-null
     * @return the operand; non-null
     * @throws InvalidQueryException if the query is invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public LowerCase lowerCase(DynamicOperand operand)
            throws InvalidQueryException, RepositoryException {
        //@todo need to be implemented.
        return null;
    }

    /**
     * Evaluates to the upper-case string value (or values, if multi-valued)
     * of an operand.
     *
     * @param operand the operand whose value is converted to a
     *                upper-case string; non-null
     * @return the operand; non-null
     * @throws InvalidQueryException if the query is invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public UpperCase upperCase(DynamicOperand operand)
            throws InvalidQueryException, RepositoryException {
        //@todo need to be implemented.
        return null;
    }

    /**
     * Evaluates to the value of a bind variable.
     *
     * @param bindVariableName the bind variable name; non-null
     * @return the operand; non-null
     * @throws InvalidQueryException if the query is invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public BindVariableValue bindVariable(String bindVariableName)
            throws InvalidQueryException, RepositoryException {
        return new BindVariableValueImpl(bindVariableName);
    }

    /**
     * Evaluates to a Literal.
     *
     * @param value
     * @return
     * @throws InvalidQueryException
     * @throws RepositoryException
     */
    public Literal literal(Value value)
            throws InvalidQueryException, RepositoryException {
        return new LiteralImpl(value, this.searchExpressionContext);
    }

    ///
    /// ORDERING
    ///

    /**
     * Orders by the value of the specified operand, in ascending order.
     *
     * @param operand the operand by which to order; non-null
     * @return the ordering
     * @throws InvalidQueryException if the query is invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public Ordering ascending(DynamicOperand operand)
            throws InvalidQueryException, RepositoryException {
        return new OrderingImpl((PropertyValueImpl) operand, JahiaQueryObjectModelConstants.ORDER_ASCENDING, false);
    }    
    
    /**
     * Orders by the value of the specified operand, in ascending order.
     *
     * @param operand the operand by which to order; non-null
     * @return the ordering
     * @throws InvalidQueryException if the query is invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public Ordering ascending(DynamicOperand operand, boolean localeSensitive)
            throws InvalidQueryException, RepositoryException {
        return new OrderingImpl((PropertyValueImpl) operand, JahiaQueryObjectModelConstants.ORDER_ASCENDING, localeSensitive);
    }

    /**
     * Orders by the value of the specified operand, in descending order.
     *
     * @param operand the operand by which to order; non-null
     * @return the ordering
     * @throws InvalidQueryException if the query is invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public Ordering descending(DynamicOperand operand)
            throws InvalidQueryException, RepositoryException {
        return new OrderingImpl((PropertyValueImpl) operand, JahiaQueryObjectModelConstants.ORDER_DESCENDING, false);
    }    
    
    /**
     * Orders by the value of the specified operand, in descending order.
     *
     * @param operand the operand by which to order; non-null
     * @return the ordering
     * @throws InvalidQueryException if the query is invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public Ordering descending(DynamicOperand operand, boolean localeSensitive)
            throws InvalidQueryException, RepositoryException {
        return new OrderingImpl((PropertyValueImpl) operand, JahiaQueryObjectModelConstants.ORDER_DESCENDING, localeSensitive);
    }

    ///
    /// COLUMN
    ///

    /**
     * Identifies a property in the default selector to include in the tabular
     * view of query results.
     * <p/>
     * The column name is the property name.
     *
     * @param propertyName the property name, or null to include a column
     *                     for each single-value non-residual property of
     *                     the selector's node type
     * @return the column; non-null
     * @throws InvalidQueryException if the query has no default selector
     *                               or is otherwise invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public Column column(String propertyName)                                     // CM
            throws InvalidQueryException, RepositoryException {
        //@todo need to be implemented.
        return null;
    }

    /**
     * Identifies a property in the default selector to include in the tabular
     * view of query results.
     *
     * @param propertyName the property name, or null to include a column
     *                     for each single-value non-residual property of
     *                     the selector's node type
     * @param columnName   the column name; must be null if
     *                     <code>propertyName</code> is null
     * @return the column; non-null
     * @throws InvalidQueryException if the query has no default selector
     *                               or is otherwise invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public Column column(String propertyName, String columnName)                  // CM
            throws InvalidQueryException, RepositoryException {
        //@todo need to be implemented.
        return null;
    }

    /**
     * Identifies a property in the specified selector to include in the tabular
     * view of query results.
     *
     * @param selectorName the selector name; non-null
     * @param propertyName the property name, or null to include a column
     *                     for each single-value non-residual property of
     *                     the selector's node type
     * @param columnName   the column name; if null, defaults to
     *                     <code>propertyName</code>; must be null if
     *                     <code>propertyName</code> is null
     * @throws InvalidQueryException if the query is invalid
     * @throws RepositoryException   if the operation otherwise fails
     */
    public Column column
            (
                    String selectorName,
                    String propertyName,
                    String columnName
            ) throws InvalidQueryException, RepositoryException {
        //@todo need to be implemented.
        return null;
    }


    public And and(List<Constraint> constraints) throws InvalidQueryException, RepositoryException {
        And andConstraint = null;
        Iterator<Constraint> it = constraints.iterator();
        Constraint c1 = null;
        Constraint c2 = null;
        while (it.hasNext()) {
            if (andConstraint == null) {
                c1 = (Constraint) it.next();
                c2 = (Constraint) it.next();
                andConstraint = this.and(c1, c2);
            } else {
                c1 = (Constraint) it.next();
                andConstraint = this.and(andConstraint, c1);
            }
        }
        return andConstraint;
    }


    public Or or(List<Constraint> constraints) throws InvalidQueryException, RepositoryException {
        Or orConstraint = null;
        Iterator<Constraint> it = constraints.iterator();
        Constraint c1 = null;
        Constraint c2 = null;
        while (it.hasNext()) {
            if (orConstraint == null) {
                c1 = (Constraint) it.next();
                c2 = (Constraint) it.next();
                orConstraint = this.or(c1, c2);
            } else {
                c1 = (Constraint) it.next();
                orConstraint = this.or(orConstraint, c1);
            }
        }
        return orConstraint;
    }

    public Not not(List<Constraint> constraints) throws InvalidQueryException, RepositoryException {
        Constraint c = this.and(constraints);
        if (c != null) {
            return this.not(c);
        }
        return null;
    }

    /**
     *
     * @param queryObjectModel
     * @return
     * @throws InvalidQueryException
     * @throws RepositoryException
     */
    QueryResult execute(QueryObjectModelImpl queryObjectModel) throws InvalidQueryException, RepositoryException {
        return JCRStoreService.getInstance().execute(queryObjectModel,user);
    }
}