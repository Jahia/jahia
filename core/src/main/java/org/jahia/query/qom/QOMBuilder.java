/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.query.qom;

import org.jahia.services.content.JCRStoreService;
import org.jahia.params.ProcessingContext;

import org.apache.jackrabbit.spi.commons.query.jsr283.qom.*;
import javax.jcr.query.QueryManager;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 13 nov. 2008
 * Time: 12:16:19
 * To change this template use File | Settings | File Templates.
 */
public class QOMBuilder implements QueryObjectModelFactory {

    private QueryManager queryManager;
    private QueryObjectModelFactory qomFactory;
    private Constraint c;
    private Source s;
    private List<Ordering> orderings;
    private List<Column> columns;

    /**
     *
     * @param context
     * @param props
     */
    public QOMBuilder(ProcessingContext context, Properties props) {
        queryManager = JCRStoreService.getInstance().getQueryManager(context.getUser(),context,props);
        qomFactory = ((QueryManagerImpl)queryManager).getQOMFactory();
        orderings = new ArrayList<Ordering>();
        columns = new ArrayList<Column>();
    }

    public QueryManager getQueryManager() {
        return queryManager;
    }

    public QueryObjectModelFactory getQomFactory() {
        return qomFactory;
    }

    public QueryObjectModel createQOM() throws InvalidQueryException, RepositoryException {
        Ordering[] orderingsAr = new Ordering[orderings.size()];
        orderingsAr = orderings.toArray(orderingsAr);
        Column[] columnsAr = new Column[columns.size()];
        columnsAr = columns.toArray(columnsAr);
        return this.qomFactory.createQuery(s,c,orderingsAr,columnsAr);
    }

    public Constraint getConstraint() {
        return c;
    }

    public void setConstraint(Constraint c) {
        this.c = c;
    }

    public Source getSource() {
        return s;
    }

    public void setSource(Source s) {
        this.s = s;
    }

    public List<Ordering> getOrderings() {
        return orderings;
    }

    public void setOrderings(List<Ordering> orderings) {
        this.orderings = orderings;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public void andConstraint(Constraint c) throws Exception {
        if (c == null){
            return;
        }
        if ( this.getConstraint() == null ){
            this.setConstraint(c);
        } else {
            Constraint constraint = this.qomFactory.and(this.getConstraint(),c);
            this.setConstraint(constraint);
        }
    }

    public void orConstraint(Constraint c) throws Exception {
        if (c == null){
            return;
        }
        if ( this.getConstraint() == null ){
            this.setConstraint(c);
        } else {
            Constraint constraint = this.qomFactory.or(this.getConstraint(),c);
            this.setConstraint(constraint);
        }
    }

    public void addOrdering(Ordering ordering) {
        if (ordering==null){
            return;
        }
        this.getOrderings().add(ordering);
    }

    public QueryObjectModel createQuery(Selector selector, Constraint constraint, Ordering[] orderings, Column[] columns)
    throws InvalidQueryException, RepositoryException {
        return this.qomFactory.createQuery(selector, constraint, orderings, columns);
    }

    public QueryObjectModel createQuery(Source source, Constraint constraint, Ordering[] orderings, Column[] columns)
    throws InvalidQueryException, RepositoryException {
        return this.qomFactory.createQuery(source,constraint,orderings,columns);
    }

    public Selector selector(String nodeTypeName) throws InvalidQueryException, RepositoryException {
        return this.qomFactory.selector(nodeTypeName);
    }

    public Selector selector(String nodeTypeName, String selectorName)
    throws InvalidQueryException, RepositoryException {
        return this.qomFactory.selector(nodeTypeName,selectorName);
    }

    public Join join(Source left, Source right, int joinType, JoinCondition joinCondition)
    throws InvalidQueryException, RepositoryException {
        return this.qomFactory.join(left, right, joinType, joinCondition);
    }

    public EquiJoinCondition equiJoinCondition(String selector1Name, String property1Name, String selector2Name, String property2Name)
    throws InvalidQueryException, RepositoryException {
        return this.qomFactory.equiJoinCondition(selector1Name, property1Name, selector2Name, property2Name);
    }

    public SameNodeJoinCondition sameNodeJoinCondition(String selector1Name, String selector2Name)
    throws InvalidQueryException, RepositoryException {
        return this.qomFactory.sameNodeJoinCondition(selector1Name,selector2Name);
    }

    public SameNodeJoinCondition sameNodeJoinCondition(String selector1Name, String selector2Name, String selector2Path)
    throws InvalidQueryException, RepositoryException {
        return this.qomFactory.sameNodeJoinCondition(selector1Name,selector2Name,selector2Path);
    }

    public ChildNodeJoinCondition childNodeJoinCondition(String childSelectorName, String parentSelectorName)
    throws InvalidQueryException, RepositoryException {
        return this.qomFactory.childNodeJoinCondition(childSelectorName, parentSelectorName);
    }

    public DescendantNodeJoinCondition descendantNodeJoinCondition(String descendantSelectorName,
                                                                   String ancestorSelectorName)
    throws InvalidQueryException, RepositoryException {
        return this.qomFactory.descendantNodeJoinCondition(descendantSelectorName, ancestorSelectorName);
    }

    public And and(Constraint constraint1, Constraint constraint2) throws InvalidQueryException, RepositoryException {
        return this.qomFactory.and(constraint1, constraint2);
    }

    public Or or(Constraint constraint1, Constraint constraint2) throws InvalidQueryException, RepositoryException {
        return this.qomFactory.or(constraint1, constraint2);
    }

    public Not not(Constraint constraint) throws InvalidQueryException, RepositoryException {
        return this.qomFactory.not(constraint);
    }

    public Comparison comparison(DynamicOperand operand1, int operator, StaticOperand operand2)
    throws InvalidQueryException, RepositoryException {
        return this.qomFactory.comparison(operand1, operator, operand2);
    }

    public PropertyExistence propertyExistence(String propertyName) throws InvalidQueryException, RepositoryException {
        return this.qomFactory.propertyExistence(propertyName);
    }

    public PropertyExistence propertyExistence(String selectorName, String propertyName)
    throws InvalidQueryException, RepositoryException {
        return this.qomFactory.propertyExistence(selectorName,propertyName);
    }

    public FullTextSearch fullTextSearch(String propertyName, String fullTextSearchExpression)
    throws InvalidQueryException, RepositoryException {
        return this.qomFactory.fullTextSearch(propertyName,fullTextSearchExpression);
    }

    public FullTextSearch fullTextSearch(String selectorName, String propertyName, String fullTextSearchExpression)
    throws InvalidQueryException, RepositoryException {
        return this.qomFactory.fullTextSearch(selectorName,propertyName,fullTextSearchExpression);
    }

    public SameNode sameNode(String path) throws InvalidQueryException, RepositoryException {
        return this.qomFactory.sameNode(path);
    }

    public SameNode sameNode(String selectorName, String path) throws InvalidQueryException, RepositoryException {
        return this.qomFactory.sameNode(path);
    }

    public ChildNode childNode(String path) throws InvalidQueryException, RepositoryException {
        return this.qomFactory.childNode(path);
    }

    public ChildNode childNode(String selectorName, String path) throws InvalidQueryException, RepositoryException {
        return this.qomFactory.childNode(selectorName,path);
    }

    public DescendantNode descendantNode(String path) throws InvalidQueryException, RepositoryException {
        return this.qomFactory.descendantNode(path);
    }

    public DescendantNode descendantNode(String selectorName, String path)
    throws InvalidQueryException, RepositoryException {
        return this.qomFactory.descendantNode(selectorName,path);
    }

    public PropertyValue propertyValue(String propertyName) throws InvalidQueryException, RepositoryException {
        return this.qomFactory.propertyValue(propertyName);
    }

    public PropertyValue propertyValue(String selectorName, String propertyName)
    throws InvalidQueryException, RepositoryException {
        return this.qomFactory.propertyValue(selectorName,propertyName);
    }

    public Length length(PropertyValue propertyValue) throws InvalidQueryException, RepositoryException {
        return this.qomFactory.length(propertyValue);
    }

    public NodeName nodeName() throws InvalidQueryException, RepositoryException {
        return this.qomFactory.nodeName();
    }

    public NodeName nodeName(String selectorName) throws InvalidQueryException, RepositoryException {
        return this.qomFactory.nodeName(selectorName);
    }

    public NodeLocalName nodeLocalName() throws InvalidQueryException, RepositoryException {
        return this.qomFactory.nodeLocalName();
    }

    public NodeLocalName nodeLocalName(String selectorName) throws InvalidQueryException, RepositoryException {
        return this.qomFactory.nodeLocalName(selectorName);
    }

    public FullTextSearchScore fullTextSearchScore() throws InvalidQueryException, RepositoryException {
        return this.qomFactory.fullTextSearchScore();
    }

    public FullTextSearchScore fullTextSearchScore(String selectorName)
        throws InvalidQueryException, RepositoryException {
        return this.qomFactory.fullTextSearchScore(selectorName);
    }

    public LowerCase lowerCase(DynamicOperand operand) throws InvalidQueryException, RepositoryException {
        return this.qomFactory.lowerCase(operand);
    }

    public UpperCase upperCase(DynamicOperand operand) throws InvalidQueryException, RepositoryException {
        return this.qomFactory.upperCase(operand);
    }

    public BindVariableValue bindVariable(String bindVariableName) throws InvalidQueryException, RepositoryException {
        return this.qomFactory.bindVariable(bindVariableName);
    }

    public Literal literal(Value value) throws InvalidQueryException, RepositoryException {
        return this.qomFactory.literal(value);
    }

    public Ordering ascending(DynamicOperand operand)
    throws InvalidQueryException, RepositoryException {
        return this.qomFactory.ascending(operand);
    }    
    
    public Ordering ascending(DynamicOperand operand, boolean localeSensitive)
    throws InvalidQueryException, RepositoryException {
        return ((QueryObjectModelFactoryImpl)this.qomFactory).ascending(operand,localeSensitive);
    }

    public Ordering descending(DynamicOperand operand)
    throws InvalidQueryException, RepositoryException {
        return this.qomFactory.descending(operand);
    }    
    
    public Ordering descending(DynamicOperand operand, boolean localeSensitive)
    throws InvalidQueryException, RepositoryException {
        return ((QueryObjectModelFactoryImpl)this.qomFactory).descending(operand,localeSensitive);
    }

    public Column column(String propertyName) throws InvalidQueryException, RepositoryException {
        return this.qomFactory.column(propertyName);
    }

    public Column column(String propertyName, String columnName) throws InvalidQueryException, RepositoryException {
        return this.qomFactory.column(propertyName, columnName);
    }

    public Column column(String selectorName, String propertyName, String columnName)
    throws InvalidQueryException, RepositoryException {
        return this.qomFactory.column(selectorName, propertyName, columnName);
    }
}
