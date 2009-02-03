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

package org.jahia.services.audit;

import java.util.Iterator;
import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.audit.display.LastFirstPublishedContentList;
import org.jahia.services.audit.display.LogEntryItem;
import org.jahia.services.audit.display.LogsResultList;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 5 d�c. 2007
 * Time: 11:43:17
 * To change this template use File | Settings | File Templates.
 */
public class LastPublishedContent extends LogsQueryByCriteria {


    public LastPublishedContent() {
    }

    public void init(ProcessingContext context) throws JahiaException {
        super.init(context);
        ProjectionList projections = criteriaQuery.getProjectionList();
        projections.add(Projections.distinct(Projections.groupProperty(LogsBasedQueryConstant.PROPERTY_OBJECT_ID)));
        projections.add(Projections.groupProperty(LogsBasedQueryConstant.PROPERTY_OBJECT_TYPE));
        projections.add(Projections.groupProperty(LogsBasedQueryConstant.PROPERTY_USERNAME));
        projections.add(Projections.alias(Projections.max(LogsBasedQueryConstant.PROPERTY_TIME),"logTime"));

        // define order
        criteriaQuery.addOrder("logTime",false);
    }

    protected void addObjectTypesConstraint(ProcessingContext context) throws JahiaException{
        super.addObjectTypesConstraint(context);
        List<Integer> objectTypes = getObjectTypes();
        if (objectTypes == null || objectTypes.isEmpty()){
            return;
        }
        Iterator<Integer> it = objectTypes.iterator();
        Integer objType = null;
        Criterion operationCriterion = null;
        String operation = null;
        while(it.hasNext()){
            objType = (Integer)it.next();
            if (objType.intValue()==LogsBasedQueryConstant.CONTAINER_TYPE){
                operation = LogsBasedQueryConstant.OPERATION_CONTAINER_ACTIVATION;
            } else if (objType.intValue()==LogsBasedQueryConstant.PAGE_TYPE){
                operation = LogsBasedQueryConstant.OPERATION_PAGE_ACTIVATION;
            }
            if ( operation != null ){
                operationCriterion = CriteriaQueryBuilder.combineCriterion(operationCriterion,
                        CriteriaQueryBuilder.getEqPropertyExpression(LogsBasedQueryConstant.PROPERTY_OPERATION,
                        operation),LogsBasedQueryConstant.OR_LOGIC);
            }
        }
        this.criteriaQuery.addCriterion(operationCriterion,LogsBasedQueryConstant.AND_LOGIC);
    }

    public LogsResultList<LogEntryItem> getLogsResultList(List<Object[]> logsResultSet,ProcessingContext context) throws JahiaException {
        LastFirstPublishedContentList<LogEntryItem> result = new LastFirstPublishedContentList<LogEntryItem>();
        result.setMaxSize(maxSize);
        result.setUniqueContentObject(this.getUniqueContentObject());
        result.buildList(logsResultSet, context, this.getTimeBasedPublishingLoadFlag(), this.getCheckACL());
        return result;
    }
}