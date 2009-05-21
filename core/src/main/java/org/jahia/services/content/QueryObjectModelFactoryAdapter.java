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
package org.jahia.services.content;


import org.apache.jackrabbit.spi.commons.query.jsr283.qom.And;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.NodeName;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.StaticOperand;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Constraint;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Join;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Column;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.FullTextSearchScore;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.SameNode;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Comparison;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.PropertyValue;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Length;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.DynamicOperand;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.BindVariableValue;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.DescendantNode;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.NodeLocalName;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.PropertyExistence;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.UpperCase;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.ChildNode;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Selector;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.LowerCase;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Ordering;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.QueryObjectModel;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Not;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.ChildNodeJoinCondition;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.QueryObjectModelFactory;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Or;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.JoinCondition;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.EquiJoinCondition;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.FullTextSearch;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.SameNodeJoinCondition;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Literal;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.DescendantNodeJoinCondition;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Source;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 13 nov. 2008
 * Time: 15:43:10
 * To change this template use File | Settings | File Templates.
 */
public class QueryObjectModelFactoryAdapter implements QueryObjectModelFactory {

    private org.apache.jackrabbit.spi.commons.query.jsr283.qom.QueryObjectModelFactory queryObjectModelFactory;

    public QueryObjectModelFactoryAdapter(org.apache.jackrabbit.spi.commons.query.jsr283.qom.QueryObjectModelFactory
            queryObjectModelFactory) {
        this.queryObjectModelFactory = queryObjectModelFactory;
    }

    public QueryObjectModel createQuery(Selector selector, Constraint constraint, Ordering[] orderings, Column[] columns) 
    throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.createQuery(selector,constraint,orderings,columns);
    }

    public QueryObjectModel createQuery(Source source, Constraint constraint, Ordering[] orderings, Column[] columns)
    throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.createQuery(source,constraint,orderings,columns);
    }

    public Selector selector(String nodeTypeName) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.selector(nodeTypeName);
    }

    public Selector selector(String nodeTypeName, String selectorName) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.selector(nodeTypeName, selectorName);
    }

    public Join join(Source left, Source right, int joinType, JoinCondition joinCondition) throws InvalidQueryException,
            RepositoryException {
        return queryObjectModelFactory.join(left, right, joinType, joinCondition);
    }

    public EquiJoinCondition equiJoinCondition(String selector1Name, String property1Name, String selector2Name, String property2Name)
            throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.equiJoinCondition(selector1Name, property1Name, selector2Name, property2Name);
    }

    public SameNodeJoinCondition sameNodeJoinCondition(String selector1Name, String selector2Name) throws InvalidQueryException,
            RepositoryException {
        return queryObjectModelFactory.sameNodeJoinCondition(selector1Name, selector2Name);
    }

    public SameNodeJoinCondition sameNodeJoinCondition(String selector1Name, String selector2Name, String selector2Path)
            throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.sameNodeJoinCondition(selector1Name, selector2Name, selector2Path);
    }

    public ChildNodeJoinCondition childNodeJoinCondition(String childSelectorName, String parentSelectorName)
            throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.childNodeJoinCondition(childSelectorName, parentSelectorName);
    }

    public DescendantNodeJoinCondition descendantNodeJoinCondition(String descendantSelectorName, String ancestorSelectorName)
            throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.descendantNodeJoinCondition(descendantSelectorName, ancestorSelectorName);
    }

    public And and(Constraint constraint1, Constraint constraint2) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.and(constraint1, constraint2);
    }

    public Or or(Constraint constraint1, Constraint constraint2) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.or(constraint1, constraint2);
    }

    public Not not(Constraint constraint) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.not(constraint);
    }

    public Comparison comparison(DynamicOperand operand1, int operator, StaticOperand operand2) throws InvalidQueryException,
            RepositoryException {
        return queryObjectModelFactory.comparison(operand1, operator, operand2);
    }

    public PropertyExistence propertyExistence(String propertyName) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.propertyExistence(propertyName);
    }

    public PropertyExistence propertyExistence(String selectorName, String propertyName) throws InvalidQueryException,
            RepositoryException {
        return queryObjectModelFactory.propertyExistence(selectorName, propertyName);
    }

    public FullTextSearch fullTextSearch(String propertyName, String fullTextSearchExpression) throws InvalidQueryException,
            RepositoryException {
        return queryObjectModelFactory.fullTextSearch(propertyName, fullTextSearchExpression);
    }

    public FullTextSearch fullTextSearch(String selectorName, String propertyName, String fullTextSearchExpression)
            throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.fullTextSearch(selectorName, propertyName, fullTextSearchExpression);
    }

    public SameNode sameNode(String path) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.sameNode(path);
    }

    public SameNode sameNode(String selectorName, String path) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.sameNode(selectorName, path);
    }

    public ChildNode childNode(String path) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.childNode(path);
    }

    public ChildNode childNode(String selectorName, String path) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.childNode(selectorName, path);
    }

    public DescendantNode descendantNode(String path) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.descendantNode(path);
    }

    public DescendantNode descendantNode(String selectorName, String path) throws InvalidQueryException,
            RepositoryException {
        return queryObjectModelFactory.descendantNode(selectorName, path);
    }

    public PropertyValue propertyValue(String propertyName) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.propertyValue(propertyName);
    }

    public PropertyValue propertyValue(String selectorName, String propertyName) throws InvalidQueryException,
            RepositoryException {
        return queryObjectModelFactory.propertyValue(selectorName, propertyName);
    }

    public Length length(PropertyValue propertyValue) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.length(propertyValue);
    }

    public NodeName nodeName() throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.nodeName();
    }

    public NodeName nodeName(String selectorName) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.nodeName(selectorName);
    }

    public NodeLocalName nodeLocalName() throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.nodeLocalName();
    }

    public NodeLocalName nodeLocalName(String selectorName) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.nodeLocalName(selectorName);
    }

    public FullTextSearchScore fullTextSearchScore() throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.fullTextSearchScore();
    }

    public FullTextSearchScore fullTextSearchScore(String selectorName) throws InvalidQueryException,
            RepositoryException {
        return queryObjectModelFactory.fullTextSearchScore(selectorName);
    }

    public LowerCase lowerCase(DynamicOperand operand) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.lowerCase(operand);
    }

    public UpperCase upperCase(DynamicOperand operand) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.upperCase(operand);
    }

    public BindVariableValue bindVariable(String bindVariableName) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.bindVariable(bindVariableName);
    }

    public Literal literal(Value value) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.literal(value);
    }

    public Ordering ascending(DynamicOperand operand)
            throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.ascending(operand);
    }    
    
    public Ordering descending(DynamicOperand operand)
            throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.descending(operand);
    }    
    
    public Column column(String propertyName) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.column(propertyName);
    }

    public Column column(String propertyName, String columnName) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.column(propertyName, columnName);
    }

    public Column column(String selectorName, String propertyName, String columnName) throws InvalidQueryException,
            RepositoryException {
        return queryObjectModelFactory.column(selectorName, propertyName, columnName);
    }

}
