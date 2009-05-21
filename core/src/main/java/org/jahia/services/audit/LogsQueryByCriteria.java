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

import org.hibernate.criterion.Criterion;
import org.jahia.content.TimeBasedPublishingState;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.audit.display.LogEntryItem;
import org.jahia.services.audit.display.LogsResultList;

import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 5 d�c. 2007
 * Time: 15:16:22
 * To change this template use File | Settings | File Templates.
 */
public abstract class LogsQueryByCriteria implements LogsQuery {

    protected CriteriaQueryBuilder criteriaQuery;

    protected LogsResultList<LogEntryItem> result;

    private List<String> contentDefinitionNames;
    private List<String> usernames;
    private List<Integer> objectTypes;

    private boolean initialized = false;

    private int timeBasedPublishingLoadFlag = TimeBasedPublishingState.IS_VALID_STATE_LOAD_FLAG;
    private boolean checkACL = true;
    private boolean uniqueContentObject = false;

    protected int dbMaxLimit = Integer.MAX_VALUE;
    protected int maxSize = Integer.MAX_VALUE;

    public LogsQueryByCriteria() {
        this.criteriaQuery = new CriteriaQueryBuilder();
    }

    /**
     * Subclass should implements and provide specific initialization
     *
     * @param context
     * @throws JahiaException
     */
    public void init(ProcessingContext context) throws JahiaException {
        addDefinitionNamesConstraint(context);
        addUsersConstraint(context);
        addObjectTypesConstraint(context);
        this.initialized = true;
    }

    /**
     *
     * @param context
     * @return
     * @throws JahiaException
     */
    public LogsResultList<LogEntryItem> executeQuery(ProcessingContext context) throws JahiaException {
        if ( criteriaQuery == null ){
            return null;
        }
        if ( !this.initialized ){
            this.init(context);
        }
        List<Object[]> result = criteriaQuery.executeQuery(this.dbMaxLimit);
        this.result = getLogsResultList(result,context);
        return this.result;
    }

    /**
     * Return a mapped LogsResultList for a raw logs result set
     *
     * @param logsResultSet
     * @param context
     * @return
     * @throws JahiaException
     */
    protected abstract LogsResultList<LogEntryItem> getLogsResultList(List<Object[]> logsResultSet,ProcessingContext context)
            throws JahiaException;

    public CriteriaQueryBuilder getCriteriaQuery() {
        return criteriaQuery;
    }

    public LogsResultList<LogEntryItem> getResult() {
        return result;
    }

    public int getDBMaxLimit() {
        return dbMaxLimit;
    }

    public void setDBMaxLimit(int maxResultSet) {
        this.dbMaxLimit = maxResultSet;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public int getMaxSize(){
        return this.maxSize;
    }

    public int getTimeBasedPublishingLoadFlag() {
        return timeBasedPublishingLoadFlag;
    }

    public void setTimeBasedPublishingLoadFlag(int timeBasedPublishingLoadFlag) {
        this.timeBasedPublishingLoadFlag = timeBasedPublishingLoadFlag;
    }

    public void disableTimeBasedPublishingCheck(){
        this.timeBasedPublishingLoadFlag = TimeBasedPublishingState.ALL_STATES_LOAD_FLAG;
    }

    public boolean getUniqueContentObject() {
        return uniqueContentObject;
    }

    public void setUniqueContentObject(boolean uniqueContentObject) {
        this.uniqueContentObject = uniqueContentObject;
    }

    /**
     * enable or disable acl check
     *
     * @param checkACL
     *
     */
    public void setCheckACL(boolean checkACL) {
        this.checkACL = checkACL;
    }

    public boolean getCheckACL() {
        return this.checkACL;
    }

    public int getDbMaxLimit() {
        return dbMaxLimit;
    }

    public void setDbMaxLimit(int dbMaxLimit) {
        this.dbMaxLimit = dbMaxLimit;
    }

    public List<Integer> getObjectTypes() {
        return objectTypes;
    }

    public void setObjectTypes(List<Integer> objectTypes) {
        this.objectTypes = objectTypes;
    }

    public List<String> getContentDefinitionNames() {
        return contentDefinitionNames;
    }

    public void setContentDefinitionNames(List<String> contentDefinitionNames) {
        this.contentDefinitionNames = contentDefinitionNames;
    }

    public List<String> getUsernames() {
        return usernames;
    }

    public void setUsernames(List<String> usernames) {
        this.usernames = usernames;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    protected void addDefinitionNamesConstraint(ProcessingContext context) throws JahiaException{
        List<String> definitionNames = getContentDefinitionNames();
        if ( definitionNames == null || definitionNames.isEmpty()){
            return;
        }
        Iterator<String> it = definitionNames.iterator();
        String name = null;
        Criterion criterion = null;
        while(it.hasNext()){
            name = it.next();
            criterion = CriteriaQueryBuilder.combineCriterion(criterion,CriteriaQueryBuilder
                    .getEqPropertyExpression(LogsBasedQueryConstant.PROPERTY_CONTENT,name),
                    LogsBasedQueryConstant.OR_LOGIC);
        }
        this.criteriaQuery.addCriterion(criterion,LogsBasedQueryConstant.AND_LOGIC);
    }

    protected void addUsersConstraint(ProcessingContext context) throws JahiaException{
        List<String> usernames = getUsernames();
        if (usernames == null || usernames.isEmpty()){
            return;
        }
        Iterator<String> it = usernames.iterator();
        String name = null;
        Criterion criterion = null;
        while(it.hasNext()){
            name = it.next();
            criterion = CriteriaQueryBuilder.combineCriterion(criterion,CriteriaQueryBuilder
                    .getEqPropertyExpression(LogsBasedQueryConstant.PROPERTY_USERNAME,name),
                    LogsBasedQueryConstant.OR_LOGIC);
        }
        this.criteriaQuery.addCriterion(criterion,LogsBasedQueryConstant.AND_LOGIC);
    }

    protected void addObjectTypesConstraint(ProcessingContext context) throws JahiaException{
        if (objectTypes == null || objectTypes.isEmpty()){
            return;
        }
        Iterator<Integer> it = objectTypes.iterator();
        Integer objType = null;
        Criterion criterion = null;
        while(it.hasNext()){
            objType = it.next();
            criterion = CriteriaQueryBuilder.combineCriterion(criterion,CriteriaQueryBuilder
                    .getEqPropertyExpression(LogsBasedQueryConstant.PROPERTY_OBJECT_TYPE,objType),
                    LogsBasedQueryConstant.OR_LOGIC);
        }
        this.criteriaQuery.addCriterion(criterion,LogsBasedQueryConstant.AND_LOGIC);
    }
}
