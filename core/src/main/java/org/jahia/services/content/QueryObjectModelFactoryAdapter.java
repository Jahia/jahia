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


import javax.jcr.query.qom.And;
import javax.jcr.query.qom.NodeName;
import javax.jcr.query.qom.StaticOperand;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.Join;
import javax.jcr.query.qom.Column;
import javax.jcr.query.qom.FullTextSearchScore;
import javax.jcr.query.qom.SameNode;
import javax.jcr.query.qom.Comparison;
import javax.jcr.query.qom.PropertyValue;
import javax.jcr.query.qom.Length;
import javax.jcr.query.qom.DynamicOperand;
import javax.jcr.query.qom.BindVariableValue;
import javax.jcr.query.qom.DescendantNode;
import javax.jcr.query.qom.NodeLocalName;
import javax.jcr.query.qom.PropertyExistence;
import javax.jcr.query.qom.UpperCase;
import javax.jcr.query.qom.ChildNode;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.LowerCase;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.Not;
import javax.jcr.query.qom.ChildNodeJoinCondition;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Or;
import javax.jcr.query.qom.JoinCondition;
import javax.jcr.query.qom.EquiJoinCondition;
import javax.jcr.query.qom.FullTextSearch;
import javax.jcr.query.qom.SameNodeJoinCondition;
import javax.jcr.query.qom.Literal;
import javax.jcr.query.qom.DescendantNodeJoinCondition;
import javax.jcr.query.qom.Source;
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

    private javax.jcr.query.qom.QueryObjectModelFactory queryObjectModelFactory;

    public QueryObjectModelFactoryAdapter(javax.jcr.query.qom.QueryObjectModelFactory
            queryObjectModelFactory) {
        this.queryObjectModelFactory = queryObjectModelFactory;
    }

    public QueryObjectModel createQuery(Source source, Constraint constraint, Ordering[] orderings, Column[] columns) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.createQuery(source, constraint, orderings, columns);
    }

    public Selector selector(String nodeTypeName, String selectorName) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.selector(nodeTypeName, selectorName);
    }

    public Join join(Source left, Source right, String joinType, JoinCondition joinCondition) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.join(left, right, joinType, joinCondition);
    }

    public EquiJoinCondition equiJoinCondition(String selector1Name, String property1Name, String selector2Name, String property2Name) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.equiJoinCondition(selector1Name, property1Name, selector2Name, property2Name);
    }

    public SameNodeJoinCondition sameNodeJoinCondition(String selector1Name, String selector2Name, String selector2Path) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.sameNodeJoinCondition(selector1Name, selector2Name, selector2Path);
    }

    public ChildNodeJoinCondition childNodeJoinCondition(String childSelectorName, String parentSelectorName) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.childNodeJoinCondition(childSelectorName, parentSelectorName);
    }

    public DescendantNodeJoinCondition descendantNodeJoinCondition(String descendantSelectorName, String ancestorSelectorName) throws InvalidQueryException, RepositoryException {
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

    public Comparison comparison(DynamicOperand operand1, String operator, StaticOperand operand2) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.comparison(operand1, operator, operand2);
    }

    public PropertyExistence propertyExistence(String selectorName, String propertyName) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.propertyExistence(selectorName, propertyName);
    }

    public FullTextSearch fullTextSearch(String s, String s1, StaticOperand staticOperand) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.fullTextSearch(s, s1, staticOperand);
    }

    public SameNode sameNode(String selectorName, String path) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.sameNode(selectorName, path);
    }

    public ChildNode childNode(String selectorName, String path) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.childNode(selectorName, path);
    }

    public DescendantNode descendantNode(String selectorName, String path) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.descendantNode(selectorName, path);
    }

    public PropertyValue propertyValue(String selectorName, String propertyName) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.propertyValue(selectorName, propertyName);
    }

    public Length length(PropertyValue propertyValue) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.length(propertyValue);
    }

    public NodeName nodeName(String selectorName) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.nodeName(selectorName);
    }

    public NodeLocalName nodeLocalName(String selectorName) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.nodeLocalName(selectorName);
    }

    public FullTextSearchScore fullTextSearchScore(String selectorName) throws InvalidQueryException, RepositoryException {
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

    public Literal literal(Value literalValue) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.literal(literalValue);
    }

    public Ordering ascending(DynamicOperand operand) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.ascending(operand);
    }

    public Ordering descending(DynamicOperand operand) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.descending(operand);
    }

    public Column column(String selectorName, String propertyName, String columnName) throws InvalidQueryException, RepositoryException {
        return queryObjectModelFactory.column(selectorName, propertyName, columnName);
    }
}
