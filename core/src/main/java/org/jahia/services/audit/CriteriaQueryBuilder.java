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
package org.jahia.services.audit;

import org.hibernate.criterion.*;
import org.jahia.content.ContentContainerKey;
import org.jahia.content.ContentContainerListKey;
import org.jahia.content.ContentPageKey;
import org.jahia.content.ObjectKey;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaAuditLogManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.hibernate.model.JahiaAuditLog;
import org.jahia.query.qom.JahiaQueryObjectModelConstants;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 15 nov. 2007
 * Time: 15:55:02
 * To change this template use File | Settings | File Templates.
 */
public class CriteriaQueryBuilder {

    private Criterion criterion;

    private DetachedCriteria criteria;

    private ProjectionList projectionList;

    public CriteriaQueryBuilder() {
        criteria = DetachedCriteria.forClass(JahiaAuditLog.class,LogsBasedQueryConstant.LOG_ALIAS);
        projectionList = Projections.projectionList();
    }

    public DetachedCriteria getCriteria() {
        return criteria;
    }

    public void setCriteria(DetachedCriteria criteria) {
        this.criteria = criteria;
    }

    public ProjectionList getProjectionList() {
        return projectionList;
    }

    public void setProjectionList(ProjectionList projectionList) {
        this.projectionList = projectionList;
    }

    public Criterion getCriterion() {
        return criterion;
    }

    public void setCriterion(Criterion criterion) {
        this.criterion = criterion;
    }

    /**
     * Add a Critetion using the given logic to the existing internal critetion
     *
     * @param criterion
     * @param logic
     * @throws JahiaException
     */
    public void addCriterion(Criterion criterion, int logic) throws JahiaException {
        this.criterion = combineCriterion(this.criterion,criterion,logic);
    }

    /**
     *  Add a Container Content Type criterion.
     *
     * @param logic
     */
    public void addContainerTypeCriterion(int logic) throws JahiaException {
        addCriterion(getContainerTypeCriterion(), logic);
    }

    public static Criterion getContainerTypeCriterion(){
        Property propCrit = Property.forName(LogsBasedQueryConstant.PROPERTY_OBJECT_TYPE);
        return propCrit.eq(new Integer(LogsBasedQueryConstant.CONTAINER_TYPE));
    }

    /**
     *  Add a Container Creation Operation criterion.
     *
     * @param logic
     */
    public void addContainerCreationCriterion(int logic) throws JahiaException {
        List<String> operations = new ArrayList<String>();
        operations.add(LogsBasedQueryConstant.OPERATION_ADDED_CONTAINER);
        addOperationCriterion(operations,logic);
    }

    public static Criterion getContainerCreationCriterion(){
        Property propCrit = Property.forName(LogsBasedQueryConstant.PROPERTY_OBJECT_TYPE);
        return propCrit.eq(new Integer(LogsBasedQueryConstant.CONTAINER_TYPE));
    }

    /**
     *  Add a Container Modification Operation criterion.
     *
     * @param logic
     */
    public void addContainerModificationCriterion(int logic) throws JahiaException {
        List<String> operations = new ArrayList<String>();
        operations.add(LogsBasedQueryConstant.OPERATION_ADDED_CONTAINER);
        operations.add(LogsBasedQueryConstant.OPERATION_UPDATED_CONTAINER);
        operations.add(LogsBasedQueryConstant.OPERATION_DELETED_CONTAINER);
        addOperationCriterion(operations,logic);
    }

    /**
     *  Add a Container Validation Operation criterion.
     *
     * @param logic
     */
    public void addContainerValidationCriterion(int logic) throws JahiaException {
        List<String> operations = new ArrayList<String>();
        operations.add(LogsBasedQueryConstant.OPERATION_CONTAINER_ACTIVATION);
        addOperationCriterion(operations,logic);
    }

    /**
     *  Add a Container First Validation Operation criterion.
     *
     * @param logic
     */
    public void addContainerFirstValidationCriterion(int logic) throws JahiaException {
        List<String> operations = new ArrayList<String>();
        operations.add(LogsBasedQueryConstant.OPERATION_FIRST_CONTAINER_ACTIVATION);
        addOperationCriterion(operations,logic);
    }

    /**
     * Add a Modification Operation criterion using the list of operations ( combined as an OR )
     *
     * @param operations
     * @param logic
     * @throws JahiaException
     */
    public void addOperationCriterion(List<String> operations, int logic) throws JahiaException {
        Iterator<String> it = operations.iterator();
        String operation = null;
        Criterion propExp = null;
        Criterion criterion = null;
        while (it.hasNext()){
            operation = it.next();
            propExp = getEqPropertyExpression(LogsBasedQueryConstant.PROPERTY_OPERATION,operation);
            criterion = combineCriterion(criterion,propExp,LogsBasedQueryConstant.OR_LOGIC);
        }
        this.addCriterion(criterion,logic);
    }

    /**
     *  Create a Container Validation Operation criterion.
     */
    public static Criterion getContainerValidationCriterion() throws JahiaException {
        List<String> operations = new ArrayList<String>();
        operations.add(LogsBasedQueryConstant.OPERATION_CONTAINER_ACTIVATION);
        return getOperationCriterion(operations);
    }

    /**
     * Create a Modification Operation criterion using the list of operations ( combined as an OR )
     *
     * @param operations
     * @throws JahiaException
     */
    public static Criterion getOperationCriterion(List<String> operations) throws JahiaException {
        Iterator<String> it = operations.iterator();
        String operation = null;
        Criterion propExp = null;
        Criterion criterion = null;
        while (it.hasNext()){
            operation = (String)it.next();
            propExp = getEqPropertyExpression(LogsBasedQueryConstant.PROPERTY_OPERATION,operation);
            criterion = combineCriterion(criterion,propExp,LogsBasedQueryConstant.OR_LOGIC);
        }
        return criterion;
    }

    /**
     * Add a User equality criterion
     *
     * @param userKey
     * @param logic
     * @throws JahiaException
     */
    public void addEqUserCriterion(String userKey, int logic) throws JahiaException {
        Criterion propExp = getEqPropertyExpression(LogsBasedQueryConstant.PROPERTY_USERNAME,userKey);
        this.addCriterion(propExp,logic);
    }

    /**
     * Add a Site equality criterion
     *
     * @param siteKey
     * @param logic
     * @throws JahiaException
     */
    public void addSiteCriterion(String siteKey, int logic) throws JahiaException {
        Criterion propExp = getEqPropertyExpression(LogsBasedQueryConstant.PROPERTY_SITE,siteKey);
        this.addCriterion(propExp,logic);
    }

    /**
     * Add a Site equality criterion given the list of siteKeys ( combining as an OR )
     *
     * @param siteKeys
     * @param logic
     * @throws JahiaException
     */
    public void addSiteCriterion(List<String> siteKeys, int logic) throws JahiaException {
        String siteKey = null;
        Iterator<String> it = siteKeys.iterator();
        Criterion criterion = null;
        Criterion propExp = null;
        while (it.hasNext()){
            siteKey = (String)it.next();
            propExp = getEqPropertyExpression(LogsBasedQueryConstant.PROPERTY_SITE,siteKey);
            criterion = combineCriterion(criterion,propExp,LogsBasedQueryConstant.OR_LOGIC);
        }
        this.addCriterion(criterion,logic);
    }

    /**
     * Add a Parent criterion
     *
     * @param parentKey
     * @param logic
     * @throws JahiaException
     */
    public void addParentCriterion(ObjectKey parentKey,
                                   int logic) throws JahiaException {
        Criterion idCrit = getEqPropertyExpression(LogsBasedQueryConstant.PROPERTY_PARENT_ID,parentKey.getIDInType());
        int parentType = 0;
        if ( parentKey.getType().equals(ContentContainerKey.CONTAINER_TYPE) ){
            parentType = LogsBasedQueryConstant.CONTAINER_TYPE;
        } else if ( parentKey.getType().equals(ContentPageKey.PAGE_TYPE) ){
            parentType = LogsBasedQueryConstant.PAGE_TYPE;
        } else if ( parentKey.getType().equals(ContentContainerListKey.CONTAINERLIST_TYPE) ){
            parentType = LogsBasedQueryConstant.CONTAINER_LIST_TYPE;
        }
        Criterion typeCrit = getEqPropertyExpression(LogsBasedQueryConstant.PROPERTY_PARENT_TYPE,
                String.valueOf(parentType));
        Criterion crit = combineCriterion(typeCrit,idCrit,LogsBasedQueryConstant.AND_LOGIC);
        this.addCriterion(crit,logic);
    }

    /**
     * Add an Event Time criterion
     *
     * @param time
     * @param operator
     * @param logic
     * @throws JahiaException
     */
    public void addEventTimeCriterion(long time, int operator,  int logic) throws JahiaException {
        Criterion propExp = null;
        if ( operator == JahiaQueryObjectModelConstants.OPERATOR_EQUAL_TO ){
            propExp = getPropertyExpression(LogsBasedQueryConstant.PROPERTY_TIME,operator, String.valueOf(time));
        }
        if ( propExp != null ){
            this.addCriterion(propExp,logic);
        }
    }

    /**
     * Add a Range Event Time criterion
     *
     * @param lowerTime
     * @param lowerOperator
     * @param upperTime
     * @param upperOperator
     * @param logic
     * @throws JahiaException
     */
    public void addEventTimeRangeCriterion(long lowerTime, int lowerOperator,
                                           long upperTime, int upperOperator, int logic) throws JahiaException {
        Property prop = Property.forName(LogsBasedQueryConstant.PROPERTY_TIME);
        this.addCriterion(prop.between(new Long(lowerTime),new Long(upperTime)), logic);
    }

    /**
     * Add a Content Definition Name criterion
     *
     * @param contentDefinitionName
     * @param logic
     * @throws JahiaException
     */
    public void addContentDefinitionNameCriterion(String contentDefinitionName,
                                                  int logic) throws JahiaException {
        Criterion propExp = getEqPropertyExpression(LogsBasedQueryConstant.PROPERTY_CONTENT,contentDefinitionName);
        this.addCriterion(propExp,logic);
    }

    /**
     * Add a Content Definition Name criterion for the given content definition name list
     *
     * @param contentDefinitionNames
     * @param logic
     * @throws JahiaException
     */
    public void addContentDefinitionNameCriterion(List<String> contentDefinitionNames,
                                                  int logic) throws JahiaException {
        if (contentDefinitionNames==null||contentDefinitionNames.isEmpty()){
            return;
        }
        String contentDefinitionName = null;
        Iterator<String> it = contentDefinitionNames.iterator();
        Criterion criterion = null;
        Criterion propExp = null;
        while (it.hasNext()){
            contentDefinitionName = (String)it.next();
            propExp = getEqPropertyExpression(LogsBasedQueryConstant.PROPERTY_CONTENT,contentDefinitionName);
            criterion = combineCriterion(criterion,propExp,LogsBasedQueryConstant.OR_LOGIC);
        }
        this.addCriterion(criterion,logic);
    }

    public void addOrder(String property, boolean ascendant){
        Order order = null;
        if ( ascendant ){
            order = Order.asc(property);
        } else {
            order = Order.desc(property);
        }
        criteria.addOrder(order);
    }

    public void addProjection(String property, boolean grouped){
        if ( grouped ){
            projectionList.add(Projections.groupProperty(property));
        } else {
            projectionList.add(Projections.property(property));
        }
    }

    /**
     *
     * @param propName
     * @param value
     */
    public static Criterion getEqPropertyExpression(String propName,Object value){
        return getPropertyExpression(propName, JahiaQueryObjectModelConstants.OPERATOR_EQUAL_TO,value);
    }

    /**
     *
     * @param propName
     * @param operator
     * @param value
     * @return
     */
    public static Criterion getPropertyExpression(String propName, int operator, Object value){
        Property propCrit = Property.forName(propName);
        if ( operator == JahiaQueryObjectModelConstants.OPERATOR_EQUAL_TO ){
            return propCrit.eq(value);
        } else if ( operator == JahiaQueryObjectModelConstants.OPERATOR_GREATER_THAN ){
            return propCrit.gt(value);
        } else if ( operator == JahiaQueryObjectModelConstants.OPERATOR_LESS_THAN ){
            return propCrit.lt(value);
        } else if ( operator == JahiaQueryObjectModelConstants.OPERATOR_LIKE ){
            return propCrit.like(value.toString(),MatchMode.START);
        } else if ( operator == JahiaQueryObjectModelConstants.OPERATOR_NOT_EQUAL_TO ){
            return propCrit.ne(value);
        }
        return null;
    }

    /**
     * Combine two criterions using the given logic
     *
     * @param criterion1
     * @param criterion2
     * @param logic
     * @return
     * @throws JahiaException
     */
    public static Criterion combineCriterion(Criterion criterion1, Criterion criterion2, int logic) throws JahiaException {
        if ( criterion1 == null ){
            return criterion2;
        } else if ( criterion2 == null ){
            return criterion1;
        }
        if (logic == LogsBasedQueryConstant.AND_LOGIC){
            return Restrictions.and(criterion1,criterion2);
        } else {
            return Restrictions.or(criterion1,criterion2);
        }
    }

    /**
     * create a Range property criterion
     *
     * @param propertyName
     * @param lowerTime
     * @param lowerOperator
     * @param upperTime
     * @param upperOperator
     * @return
     * @throws JahiaException
     */
    public static Criterion getPropertyRangeCriterion(  String propertyName,
                                                        long lowerTime, int lowerOperator,
                                                        long upperTime, int upperOperator)
    throws JahiaException {
        Property prop = Property.forName(propertyName);
        return prop.between(new Long(lowerTime),new Long(upperTime));
    }

    /**
     * Execute the internal Criterion and returns the result
     * 
     * @param maxResultSet
     * @return
     * @throws JahiaException
     */
    public List<Object[]> executeQuery(int maxResultSet) throws JahiaException {
        if ( this.criteria == null ){
            return null;
        }
        ApplicationContext springContext = SpringContextSingleton.getInstance().getContext();
        JahiaAuditLogManager mgr = (JahiaAuditLogManager)springContext.getBean(
                JahiaAuditLogManager.class.getName());
        if (this.criterion != null){
            criteria.add(this.criterion);
        }
        if (this.projectionList != null){
            criteria.setProjection(this.projectionList);
        }
        return mgr.executeCriteria(criteria,maxResultSet);
    }

}
