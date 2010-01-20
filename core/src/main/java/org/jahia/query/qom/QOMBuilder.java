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

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.query.QueryService;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.QueryManager;
import javax.jcr.query.qom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Utility class to help building a query object mode in several steps. It is mainly used by the 
 * query tag library to build the QOM with our tags. 
 * 
 * @author Benjamin Papez
 */
public class QOMBuilder implements QueryObjectModelFactory {
    private QueryManager queryManager;
    private QueryObjectModelFactory qomFactory;
    private Constraint c;
    private Source s;
    private List<Ordering> orderings;
    private List<Column> columns;
    private Locale locale;

    /**
     * QOMBuilder constructor
     * @param locale current locale used to limit/modify the query to return only nodes for the current locale
     * @throws RepositoryException in case the query manager cannot be retrieved
     */
    public QOMBuilder(Locale locale) throws RepositoryException {
        queryManager = JCRSessionFactory.getInstance().getCurrentUserSession().getWorkspace().getQueryManager();
        qomFactory = queryManager.getQOMFactory();
        orderings = new ArrayList<Ordering>();
        columns = new ArrayList<Column>();
        this.locale = locale; 
    }

    /**
     * @return the JCR based query manager implementation
     */
    public QueryManager getQueryManager() {
        return queryManager;
    }

    /**
     * @return the JCR based query object model factory implementation
     */
    public QueryObjectModelFactory getQomFactory() {
        return qomFactory;
    }

    /**
     * @return the created query object model (including modifications and optimizations)
     * @throws InvalidQueryException
     * @throws RepositoryException
     */
    public QueryObjectModel createQOM() throws InvalidQueryException,
            RepositoryException {
        Ordering[] orderingsAr = getOrderings().toArray(
                new Ordering[getOrderings().size()]);
        Column[] columnsAr = getColumns().toArray(
                new Column[getColumns().size()]);
        QueryService queryService = ServicesRegistry.getInstance().getQueryService();
        return queryService.modifyAndOptimizeQuery(getSource(), getConstraint(),
                orderingsAr, columnsAr, locale, queryService.getQueryObjectModelFactory());
    }

    /**
     * @return the current root constraint object
     */
    public Constraint getConstraint() {
        return c;
    }

    /**
     * Sets the current constraint root object
     * @param c constraint root object
     */
    public void setConstraint(Constraint c) {
        this.c = c;
    }

    /**
     * @return the current source root object
     */
    public Source getSource() {
        return s;
    }

    /**
     * Sets the current source root object
     * @param s source root object
     */
    public void setSource(Source s) {
        this.s = s;
    }

    /**
     * @return ordering object list
     */
    public List<Ordering> getOrderings() {
        return orderings;
    }

    /**
     * Set ordering object list
     * @param orderings list with ordering objects
     */
    public void setOrderings(List<Ordering> orderings) {
        this.orderings = orderings;
    }

    /**
     * @return column object list
     */
    public List<Column> getColumns() {
        return columns;
    }

    /**
     * Set column object list
     * @param columns list with column objects
     */
    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    /**
     * Add a constraint. If it is the first constraint it becomes the root constraint, otherwise it is
     * added with a logical conjunction (AND).
     * 
     * @param c a constraint to add
     * @throws InvalidQueryException if a particular validity test is possible on this method,
     * the implementation chooses to perform that test (and not leave it until later,
     * on {@link #createQuery}, and the parameters given fail that test
     * @throws RepositoryException   if the operation otherwise fails
     */
    public void andConstraint(Constraint c) throws InvalidQueryException, RepositoryException {
        if (c == null){
            return;
        }
        if ( this.getConstraint() == null ){
            this.setConstraint(c);
        } else {
            Constraint constraint = getQomFactory().and(this.getConstraint(),c);
            this.setConstraint(constraint);
        }
    }

    /**
     * Add a constraint. If it is the first constraint it becomes the root constraint, otherwise it is
     * added with a logical disjunction (OR).
     * 
     * @param c a constraint to add
     * @throws InvalidQueryException if a particular validity test is possible on this method,
     * the implementation chooses to perform that test (and not leave it until later,
     * on {@link #createQuery}, and the parameters given fail that test
     * @throws RepositoryException   if the operation otherwise fails
     */
    public void orConstraint(Constraint c) throws InvalidQueryException, RepositoryException {
        if (c == null){
            return;
        }
        if ( this.getConstraint() == null ){
            this.setConstraint(c);
        } else {
            Constraint constraint = getQomFactory().or(this.getConstraint(),c);
            this.setConstraint(constraint);
        }
    }

    /**
     * Appends the specified ordering to the end of the oredreings list
     * @param ordering 
     */
    public void addOrdering(Ordering ordering) {
        if (ordering==null){
            return;
        }
        this.getOrderings().add(ordering);
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.qom.QueryObjectModelFactory#createQuery(javax.jcr.query.qom.Source, javax.jcr.query.qom.Constraint, javax.jcr.query.qom.Ordering[], javax.jcr.query.qom.Column[])
     */
    public QueryObjectModel createQuery(Source source, Constraint constraint, Ordering[] orderings, Column[] columns) throws InvalidQueryException, RepositoryException {
        return getQomFactory().createQuery(source, constraint, orderings, columns);
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.qom.QueryObjectModelFactory#selector(java.lang.String, java.lang.String)
     */
    public Selector selector(String nodeTypeName, String selectorName) throws InvalidQueryException, RepositoryException {
        return getQomFactory().selector(nodeTypeName, selectorName);
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.qom.QueryObjectModelFactory#join(javax.jcr.query.qom.Source, javax.jcr.query.qom.Source, java.lang.String, javax.jcr.query.qom.JoinCondition)
     */
    public Join join(Source left, Source right, String joinType, JoinCondition joinCondition) throws InvalidQueryException, RepositoryException {
        return getQomFactory().join(left, right, joinType, joinCondition);
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.qom.QueryObjectModelFactory#equiJoinCondition(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public EquiJoinCondition equiJoinCondition(String selector1Name, String property1Name, String selector2Name, String property2Name) throws InvalidQueryException, RepositoryException {
        return getQomFactory().equiJoinCondition(selector1Name, property1Name, selector2Name, property2Name);
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.qom.QueryObjectModelFactory#sameNodeJoinCondition(java.lang.String, java.lang.String, java.lang.String)
     */
    public SameNodeJoinCondition sameNodeJoinCondition(String selector1Name, String selector2Name, String selector2Path) throws InvalidQueryException, RepositoryException {
        return getQomFactory().sameNodeJoinCondition(selector1Name, selector2Name, selector2Path);
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.qom.QueryObjectModelFactory#childNodeJoinCondition(java.lang.String, java.lang.String)
     */
    public ChildNodeJoinCondition childNodeJoinCondition(String childSelectorName, String parentSelectorName) throws InvalidQueryException, RepositoryException {
        return getQomFactory().childNodeJoinCondition(childSelectorName, parentSelectorName);
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.qom.QueryObjectModelFactory#descendantNodeJoinCondition(java.lang.String, java.lang.String)
     */
    public DescendantNodeJoinCondition descendantNodeJoinCondition(String descendantSelectorName, String ancestorSelectorName) throws InvalidQueryException, RepositoryException {
        return getQomFactory().descendantNodeJoinCondition(descendantSelectorName, ancestorSelectorName);
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.qom.QueryObjectModelFactory#and(javax.jcr.query.qom.Constraint, javax.jcr.query.qom.Constraint)
     */
    public And and(Constraint constraint1, Constraint constraint2) throws InvalidQueryException, RepositoryException {
        return getQomFactory().and(constraint1, constraint2);
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.qom.QueryObjectModelFactory#or(javax.jcr.query.qom.Constraint, javax.jcr.query.qom.Constraint)
     */
    public Or or(Constraint constraint1, Constraint constraint2) throws InvalidQueryException, RepositoryException {
        return getQomFactory().or(constraint1, constraint2);
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.qom.QueryObjectModelFactory#not(javax.jcr.query.qom.Constraint)
     */
    public Not not(Constraint constraint) throws InvalidQueryException, RepositoryException {
        return getQomFactory().not(constraint);
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.qom.QueryObjectModelFactory#comparison(javax.jcr.query.qom.DynamicOperand, java.lang.String, javax.jcr.query.qom.StaticOperand)
     */
    public Comparison comparison(DynamicOperand operand1, String operator, StaticOperand operand2) throws InvalidQueryException, RepositoryException {
        return getQomFactory().comparison(operand1, operator, operand2);
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.qom.QueryObjectModelFactory#propertyExistence(java.lang.String, java.lang.String)
     */
    public PropertyExistence propertyExistence(String selectorName, String propertyName) throws InvalidQueryException, RepositoryException {
        return getQomFactory().propertyExistence(selectorName, propertyName);
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.qom.QueryObjectModelFactory#fullTextSearch(java.lang.String, java.lang.String, javax.jcr.query.qom.StaticOperand)
     */
    public FullTextSearch fullTextSearch(String s, String s1, StaticOperand staticOperand) throws InvalidQueryException, RepositoryException {
        return getQomFactory().fullTextSearch(s, s1, staticOperand);
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.qom.QueryObjectModelFactory#sameNode(java.lang.String, java.lang.String)
     */
    public SameNode sameNode(String selectorName, String path) throws InvalidQueryException, RepositoryException {
        return getQomFactory().sameNode(selectorName, path);
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.qom.QueryObjectModelFactory#childNode(java.lang.String, java.lang.String)
     */
    public ChildNode childNode(String selectorName, String path) throws InvalidQueryException, RepositoryException {
        return getQomFactory().childNode(selectorName, path);
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.qom.QueryObjectModelFactory#descendantNode(java.lang.String, java.lang.String)
     */
    public DescendantNode descendantNode(String selectorName, String path) throws InvalidQueryException, RepositoryException {
        return getQomFactory().descendantNode(selectorName, path);
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.qom.QueryObjectModelFactory#propertyValue(java.lang.String, java.lang.String)
     */
    public PropertyValue propertyValue(String selectorName, String propertyName) throws InvalidQueryException, RepositoryException {
        return getQomFactory().propertyValue(selectorName, propertyName);
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.qom.QueryObjectModelFactory#length(javax.jcr.query.qom.PropertyValue)
     */
    public Length length(PropertyValue propertyValue) throws InvalidQueryException, RepositoryException {
        return getQomFactory().length(propertyValue);
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.qom.QueryObjectModelFactory#nodeName(java.lang.String)
     */
    public NodeName nodeName(String selectorName) throws InvalidQueryException, RepositoryException {
        return getQomFactory().nodeName(selectorName);
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.qom.QueryObjectModelFactory#nodeLocalName(java.lang.String)
     */
    public NodeLocalName nodeLocalName(String selectorName) throws InvalidQueryException, RepositoryException {
        return getQomFactory().nodeLocalName(selectorName);
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.qom.QueryObjectModelFactory#fullTextSearchScore(java.lang.String)
     */
    public FullTextSearchScore fullTextSearchScore(String selectorName) throws InvalidQueryException, RepositoryException {
        return getQomFactory().fullTextSearchScore(selectorName);
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.qom.QueryObjectModelFactory#lowerCase(javax.jcr.query.qom.DynamicOperand)
     */
    public LowerCase lowerCase(DynamicOperand operand) throws InvalidQueryException, RepositoryException {
        return getQomFactory().lowerCase(operand);
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.qom.QueryObjectModelFactory#upperCase(javax.jcr.query.qom.DynamicOperand)
     */
    public UpperCase upperCase(DynamicOperand operand) throws InvalidQueryException, RepositoryException {
        return getQomFactory().upperCase(operand);
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.qom.QueryObjectModelFactory#bindVariable(java.lang.String)
     */
    public BindVariableValue bindVariable(String bindVariableName) throws InvalidQueryException, RepositoryException {
        return getQomFactory().bindVariable(bindVariableName);
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.qom.QueryObjectModelFactory#literal(javax.jcr.Value)
     */
    public Literal literal(Value literalValue) throws InvalidQueryException, RepositoryException {
        return getQomFactory().literal(literalValue);
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.qom.QueryObjectModelFactory#ascending(javax.jcr.query.qom.DynamicOperand)
     */
    public Ordering ascending(DynamicOperand operand) throws InvalidQueryException, RepositoryException {
        return getQomFactory().ascending(operand);
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.qom.QueryObjectModelFactory#descending(javax.jcr.query.qom.DynamicOperand)
     */
    public Ordering descending(DynamicOperand operand) throws InvalidQueryException, RepositoryException {
        return getQomFactory().descending(operand);
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.qom.QueryObjectModelFactory#column(java.lang.String, java.lang.String, java.lang.String)
     */
    public Column column(String selectorName, String propertyName, String columnName) throws InvalidQueryException, RepositoryException {
        return getQomFactory().column(selectorName, propertyName, columnName);
    }
}
