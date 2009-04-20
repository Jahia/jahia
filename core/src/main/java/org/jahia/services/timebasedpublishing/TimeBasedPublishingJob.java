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

package org.jahia.services.timebasedpublishing;

import org.jahia.bin.Jahia;
import org.jahia.content.ObjectKey;
import org.jahia.content.ContentObject;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaObjectDelegate;
import org.jahia.hibernate.manager.JahiaObjectManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.scheduler.BackgroundJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 30 ao�t 2005
 * Time: 10:46:21
 * To change this template use File | Settings | File Templates.
 */
public class TimeBasedPublishingJob extends BackgroundJob {
    public static final String JOB_NAME_PREFIX = "TimebasedPublishingJob_";
    public static final String TRIGGER_NAME_PREFIX = "TimebasedPublishingJobTrigger_";
    public static final String RULE_ID = "ruleId";
    public static final String RULE_DEF_ID = "ruleDefId";
    public static final String RULE_SETTINGS = "ruleSettings";
    public static final String OBJECT_KEY = "objectKey";
    public static final String OPERATION = "ruleOperation";
    public static final String UPDATE_OPERATION = "ruleUpdate";
    public static final String DELETE_OPERATION = "ruleDelete";

    public static final String TIMEBASED_PUBLISHING_TYPE = "timebasedpublishing";

    public void executeJahiaJob(JobExecutionContext jobExecutionContext, ProcessingContext jParams) throws JobExecutionException {
        JobDetail jobDetail = jobExecutionContext.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();

        int id = jobDataMap.getInt(RULE_ID);
        int ruleDefId = jobDataMap.getInt(RULE_DEF_ID);
        TimeBasedPublishingService tbpServ = ServicesRegistry.getInstance().getTimeBasedPublishingService();
        RangeRetentionRule rule = null;
        if ( id != -1 ){
            rule = (RangeRetentionRule)tbpServ.getRetentionRule(id);
        } else {
            rule = (RangeRetentionRule)ServicesRegistry.getInstance().getTimeBasedPublishingService()
                    .getRetentionRuleDef(new Integer(ruleDefId)).createRule();
        }
        String settings = jobDataMap.getString(RULE_SETTINGS);
        try {
            rule.loadSettings(settings);
        } catch (Exception e) {
            JahiaException je = new JahiaException("Error loading rule settings from data map",
                    "Error loading rule settings from data map",
                    JahiaException.APPLICATION_ERROR, JahiaException.ERROR_SEVERITY, e);
            throw new JobExecutionException(je);
        }
        String op = jobDataMap.getString(OPERATION);
        ObjectKey objectKey = (ObjectKey)jobDataMap.get(OBJECT_KEY);

        if ( rule == null ){
            throw new JobExecutionException("Retention rule is null in JobDataMap. No Operation performed");
        }

        if ( objectKey == null ){
            throw new JobExecutionException("Object Key is null in JobDataMap. No Operation performed");
        }

        if ( op == null ){
            throw new JobExecutionException("Operation is null in JobDataMap. No Operation performed");
        }

        JahiaObjectManager jahiaObjectMgr = (JahiaObjectManager) SpringContextSingleton
                .getInstance().getContext().getBean(JahiaObjectManager.class.getName());


        if (DELETE_OPERATION.equals(op)) {
            try {
                RetentionRuleEvent event = new RetentionRuleEvent(this, Jahia.getThreadParamBean(),
                        rule.getId().intValue(),RetentionRuleEvent.DELETING_RULE,-1);
                ServicesRegistry.getInstance().getJahiaEventService().fireTimeBasedPublishingStateChange(event);
                
                tbpServ.deleteRetentionRule(rule);
                //@todo: invalidate esi.
                //jParams.getSessionState().setAttribute("FireContainerUpdated", "true");
            } catch (Exception e) {
                throw new JobExecutionException("Error deleting Retention Rule");
            }
        } else if (UPDATE_OPERATION.equals(op)) {
            try {
                tbpServ.saveRetentionRule(rule);
                JahiaObjectDelegate jahiaObjectDelegate = jahiaObjectMgr.getJahiaObjectDelegate(objectKey);
                jahiaObjectDelegate.setRule(rule);
                jahiaObjectDelegate.setObjectKey(objectKey);
                jahiaObjectMgr.save(jahiaObjectDelegate);

                Set<ContentObject> pickers = ContentObject.getContentObjectInstance(objectKey).getPickerObjects();
                for (ContentObject contentObject : pickers) {
                    tbpServ.scheduleBackgroundJob(contentObject.getObjectKey(),TimeBasedPublishingJob.UPDATE_OPERATION,rule,jParams);

                    jahiaObjectDelegate = jahiaObjectMgr.getJahiaObjectDelegate(contentObject.getObjectKey());
                    jahiaObjectDelegate.setRule(rule);
                    jahiaObjectDelegate.setObjectKey(objectKey);
                    jahiaObjectMgr.save(jahiaObjectDelegate);
                }

                RetentionRuleEvent event = new RetentionRuleEvent(this, jParams,
                        rule.getId().intValue(), RetentionRuleEvent.UPDATING_RULE, -1);
                ServicesRegistry.getInstance().getJahiaEventService().fireTimeBasedPublishingStateChange(event);
                rule.startJob();
                //@todo: invalidate esi.
            } catch (Exception e) {
                JahiaException je = new JahiaException("Error saving Retention Rule", "Error saving Retention Rule",
                        JahiaException.ENGINE_ERROR, JahiaException.ERROR_SEVERITY, e);
                throw new JobExecutionException(je);
            }
        }
    }
}
