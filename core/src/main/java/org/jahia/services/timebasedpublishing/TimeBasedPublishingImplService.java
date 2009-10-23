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
 package org.jahia.services.timebasedpublishing;

import org.jahia.content.*;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.manager.*;
import org.jahia.params.AdvPreviewSettings;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.services.usermanager.JahiaAdminUser;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.utils.JahiaTools;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;

import java.util.*;

public class TimeBasedPublishingImplService extends TimeBasedPublishingService {

    public final String BASE_RETENTIONRULE_DEFINITION_NAME = "baseRetentionRuleDef";

    JahiaObjectManager jahiaObjectMgr;
    JahiaRetentionRuleManager ruleMgr;
    JahiaRetentionRuleDefManager ruleDefMgr;
    JahiaFieldsDataManager fieldsDataMgr;
    JahiaContainerListManager containerListMgr;    
    SchedulerService schedulerService;
    Properties config;
    
    private int batchLoadingSize;

    public void setConfig(Properties config) {
        this.config = config;
    }

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger (TimeBasedPublishingImplService.class);

    static private TimeBasedPublishingImplService instance = null;

    private List defaultRulesDef = null;

    protected TimeBasedPublishingImplService() {
    }

    public static TimeBasedPublishingImplService getInstance() {
        if (instance == null) {
            instance = new TimeBasedPublishingImplService();
        }
        return instance;
    }

    public JahiaObjectManager getJahiaObjectMgr() {
        return jahiaObjectMgr;
    }

    public void setJahiaObjectMgr(JahiaObjectManager jahiaObjectMgr) {
        this.jahiaObjectMgr = jahiaObjectMgr;
    }

    public JahiaRetentionRuleManager getRuleMgr() {
        return ruleMgr;
    }

    public void setRuleMgr(JahiaRetentionRuleManager ruleMgr) {
        this.ruleMgr = ruleMgr;
    }

    public void setSchedulerService(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }    
    
    public JahiaRetentionRuleDefManager getRuleDefMgr() {
        return ruleDefMgr;
    }

    public void setRuleDefMgr(JahiaRetentionRuleDefManager ruleDefMgr) {
        this.ruleDefMgr = ruleDefMgr;
    }

    /**
     * @throws JahiaInitializationException
     */
    public void start()
    throws JahiaInitializationException{
        if ( defaultRulesDef != null ){
            Iterator iterator = defaultRulesDef.iterator();
            BaseRetentionRuleDef def = null;
            BaseRetentionRuleDef existDef = null;
            while ( iterator.hasNext() ){
                def = (BaseRetentionRuleDef)iterator.next();
                existDef = ruleDefMgr.getRetentionRuleDefByName(def.getName());
                if ( existDef == null ){
                    ruleDefMgr.save(def);
                } else {
                    existDef.setDateFormat(def.getDateFormat());
                    existDef.setTitle(def.getTitle());
                    existDef.setRuleHelperClassName(def.getRuleHelperClassName());
                    existDef.setRuleClassName(def.getRuleClassName());
                    ruleDefMgr.save(existDef);
                }
            }
        }
        startRetentionRuleIntegritryCheckJob();
    }

    protected void startRetentionRuleIntegritryCheckJob(){

        long maxElapsedTime = JahiaTools.getTimeAsLong(
            config.getProperty("org.jahia.services.timebasedpublishing.maxElapsedTime","2h"),"2h").longValue();
        long checkInterval = JahiaTools.getTimeAsLong(
            config.getProperty("org.jahia.services.timebasedpublishing.checkInterval","12h"),"12h").longValue();

        JobDetail jobDetail = null;
        try {
            // for migration only, remove old job name - should be removed soon
            schedulerService.deleteJob(RetentionRuleIntegrityCheckJob.JOB_NAME, RetentionRuleIntegrityCheckJob.JOB_NAME);

            jobDetail = schedulerService.getJobDetail(RetentionRuleIntegrityCheckJob.JOB_NAME,
                    SchedulerService.SYSTEM_JOB_GROUP);
        } catch (Exception t) {
        }
        if (jobDetail == null) {
            long startTime = System.currentTimeMillis() + 120000L;
            jobDetail =
                    new JobDetail(RetentionRuleIntegrityCheckJob.JOB_NAME,
                            SchedulerService.SYSTEM_JOB_GROUP,
                            RetentionRuleIntegrityCheckJob.class);
            jobDetail.getJobDataMap().put(RetentionRuleIntegrityCheckJob.MAX_ELAPSED_INTERVAL,new Long(maxElapsedTime));
            SimpleTrigger trigger =
                    new SimpleTrigger(RetentionRuleIntegrityCheckJob.TRIGGER_NAME,
                            SchedulerService.REPEATED_TRIGGER_GROUP, new Date(startTime),
                            null, SimpleTrigger.REPEAT_INDEFINITELY, checkInterval);
            jobDetail.setDurability(false);
            jobDetail.setRequestsRecovery(false);
            jobDetail.setVolatility(true);
            trigger.setVolatility(true);
            try {
                schedulerService.scheduleRamJob(jobDetail, trigger);
            } catch (Exception t) {
                logger.debug("Exception scheduling " + RetentionRuleIntegrityCheckJob.JOB_NAME, t);
            }
        } else {
            long l = jobDetail.getJobDataMap().getLong(RetentionRuleIntegrityCheckJob.MAX_ELAPSED_INTERVAL);
            if ( l != maxElapsedTime){
                jobDetail.getJobDataMap().put(RetentionRuleIntegrityCheckJob.MAX_ELAPSED_INTERVAL,new Long(maxElapsedTime));
            }
        }
    }

    public void stop() {}

    public void setDefaultRulesDef(List defaultRulesDef) {
        this.defaultRulesDef = defaultRulesDef;
    }

    /**
     * Returns a RetentionRule instance
     *
     * @param id
     * @return
     */
    public RetentionRule getRetentionRule(int id){
        return ruleMgr.getRetentionRuleById(id);
    }

    /**
     * Returns a RetentionRule instance for the given ObjectKey
     *
     * @param objectKey
     * @return
     */
    public RetentionRule getRetentionRule(ObjectKey objectKey){
        return  ruleMgr.getRetentionRuleByObjectKey(objectKey);
    }

    /**
     * Returns all RetentionRule instances
     *
     * @return
     */
    public List getRetentionRules(){
        return  ruleMgr.getJahiaRetentionRules();
    }

    /**
     * Saves the RetentionRule instance
     *
     * @param rule
     */
    public void saveRetentionRule(RetentionRule rule) throws Exception {
        rule.save();
        //RetentionRuleEvent event = new RetentionRuleEvent(this, Jahia.getThreadParamBean(),
        //        rule.getId().intValue(),RetentionRuleEvent.UPDATING_RULE,-1);
        //ServicesRegistry.getInstance().getJahiaEventService().fireTimeBasedPublishingStateChange(event);
    }


    /**
     * Deletes the RetentionRule instance
     *
     * @param rule
     * @return
     */
    public boolean deleteRetentionRule(RetentionRule rule) throws Exception {
        rule.deleteJob();
        rule.delete();
        return true;
    }

    /**
     * Returns the RetentionRuleDef instance
     *
     * @param id
     * @return
     */
    public RetentionRuleDef getRetentionRuleDef(Integer id){
        return ruleDefMgr.getRetentionRuleDefById(id.intValue());
    }

    /**
     * Returns the base retention rule Def instance.
     * The one that must support at least inheritance
     * @return
     */
    public RetentionRuleDef getBaseRetentionRuleDef(){
        return ruleDefMgr.getRetentionRuleDefByName(this.BASE_RETENTIONRULE_DEFINITION_NAME);
    }

    /**
     * Returns all RetentionRuleDef instances
     *
     * @return
     */
    public List getRetentionRuleDefs(){
        return ruleDefMgr.getJahiaRetentionRuleDefs();
    }

    /**
     * Retrieve the object owning the retention rule and propagate retention event's to all it's child
     * if they inherit this retention rule
     *
     * @param theEvent
     */
    public void handleTimeBasedPublishingEvent( RetentionRuleEvent theEvent )
    {
        RetentionRule rule = ruleMgr.getRetentionRuleById(theEvent.getRuleId());
        if ( rule != null ){
            Collection c = jahiaObjectMgr.getJahiaObjectDelegateByRuleId(rule.getId().intValue());
            for (Iterator iterator = c.iterator(); iterator.hasNext();) {
                JahiaObjectDelegate jahiaObjectDelegate = (JahiaObjectDelegate) iterator.next();
                if ( jahiaObjectDelegate != null ){
                    try {
                        handleTimeBasedPublishingEvent(theEvent, rule, jahiaObjectDelegate.getObjectKey());
                    } catch ( Exception t) {
                        logger.debug("Error converting JahiaObjectDelegate to JahiaObjec",t);
                    }
                }
            }
        }

    }

    /**
     * return the list of inconsistent object in regards to the publishing state.
     *
     * @param checkTime the publication or expiration time
     * @param maxElapsedTime the elapsed time max above which the state should be considered inconsistent
     *
     * @return
     */
    public List findInconsistentObjects(long checkTime, long maxElapsedTime) {
        return jahiaObjectMgr.findInconsistentObjects(checkTime, maxElapsedTime);
    }

    /**
     * Schedule a background job that will apply rule changes in background
     *
     * @param objectKey
     * @param operation
     * @param rule
     * @param jParams
     * @throws Exception
     */
    public void scheduleBackgroundJob(ObjectKey objectKey, String operation, RetentionRule rule,
                                       ProcessingContext jParams) throws Exception {
        SchedulerService schedulerService = ServicesRegistry.getInstance().getSchedulerService();
            try {
            JobDetail jobDetail = schedulerService.getJobDetail(TimeBasedPublishingJob.JOB_NAME_PREFIX + objectKey,
                    BackgroundJob.getGroupName(TimeBasedPublishingJob.class));

            if (jobDetail == null) {
                jobDetail = TimeBasedPublishingJob.createJahiaJob(TimeBasedPublishingJob.JOB_NAME_PREFIX,
                        TimeBasedPublishingJob.class, jParams);
                jobDetail.setName(TimeBasedPublishingJob.JOB_NAME_PREFIX + objectKey);
                jobDetail.setVolatility(false);
                jobDetail.setRequestsRecovery(true);
            } else {
                logger.debug("delete previous retentionRule job "+TimeBasedPublishingJob.JOB_NAME_PREFIX + objectKey);
                schedulerService.deleteJob(jobDetail.getName(), jobDetail.getGroup());
            }

            JobDataMap jobDataMap = jobDetail.getJobDataMap();

            //set the type of job
            jobDataMap.put(TimeBasedPublishingJob.JOB_TYPE, TimeBasedPublishingJob.TIMEBASED_PUBLISHING_TYPE);

            jobDataMap.put(TimeBasedPublishingJob.OBJECT_KEY, objectKey);
            jobDataMap.put(TimeBasedPublishingJob.RULE_ID, rule.getId());
            jobDataMap.put(TimeBasedPublishingJob.RULE_DEF_ID, rule.getRetentionRuleDef().getId());
            jobDataMap.put(TimeBasedPublishingJob.RULE_SETTINGS, rule.getSettings());
            jobDataMap.put(TimeBasedPublishingJob.OPERATION, operation);
            schedulerService.scheduleJobAtEndOfRequest(jobDetail);
        } catch (JahiaException e) {
            logger.debug("Error during scheduling timebasedpublishing job ", e);
        }
    }

    /**
     *
     * @param theEvent
     * @param rule
     * @param jahiaObject
     */
    private void handleTimeBasedPublishingEvent(RetentionRuleEvent theEvent,
                                                RetentionRule rule,
                                                ObjectKey jahiaObjectKey){

        JahiaObjectDelegate jahiaObjectDelegate = jahiaObjectMgr
                .getJahiaObjectDelegate(jahiaObjectKey);
        if ( jahiaObjectDelegate == null ){
            return;
        }
        boolean newRuleCreated = false;
        RetentionRule localRule = jahiaObjectDelegate.getRule();
        if ( localRule != null && localRule.getId().intValue() != rule.getId().intValue() && !localRule.getInherited().booleanValue() ){
            // do not propagate parent's rule change
            return;
        }

        if ( !newRuleCreated && !stateWillChange(theEvent, rule, jahiaObjectDelegate) ){
            if ( theEvent.getEventType() == RetentionRuleEvent.DELETING_RULE ){
                applyTimeBasedPublishingEvent(theEvent, rule, jahiaObjectDelegate);
            }
            return;
        }
        JahiaUser adminUser = JahiaAdminUser.getAdminUser(jahiaObjectDelegate.getSiteId().intValue());
        RetentionRule effectiveRule = rule;
        
        if ( localRule != null && localRule.getId().intValue() == rule.getId().intValue() &&
                localRule.getInherited().booleanValue() ){
            // as the rule inherit from parent, we propagate parent state instead of rule
            try {
                ObjectKey parentObjectKey = 
                        getParentObjectKeyForTimeBasedPublishing(jahiaObjectKey,adminUser,EntryLoadRequest.STAGED,ParamBean.EDIT, false);
                if ( parentObjectKey != null ){
                    JahiaObjectDelegate parentObjectDelegate =
                            jahiaObjectMgr.getJahiaObjectDelegate(parentObjectKey);
                    if ( parentObjectDelegate != null ){
                        copyParentStateToCurrentObjectAndChilds(jahiaObjectKey, parentObjectDelegate);
                    }
                }
            } catch ( Exception t){
                logger.debug("Error copying parent timebased publishing statues to child"
                        + jahiaObjectDelegate.getObjectKey(),t);
            }
            return;
        }        

        // apply to childs
        try {
            Collection childs = getChildObjectKeysForTimeBasedPublishing(jahiaObjectKey, adminUser, EntryLoadRequest.STAGED, ParamBean.EDIT);
            Iterator iterator = childs.iterator();
            ObjectKey child = null;
            while ( iterator.hasNext() ){
                child = (ObjectKey)iterator.next();
                handleTimeBasedPublishingEvent(theEvent, effectiveRule, child);
            }
        } catch ( Exception t) {
            logger.warn("Error converting JahiaObjectDelegate to JahiaObjec",t);
        }
        // apply rule to current object
        applyTimeBasedPublishingEvent(theEvent, effectiveRule, jahiaObjectDelegate);
    }

    private void copyParentStateToCurrentObjectAndChilds(ObjectKey jahiaObjectKey,
                                                         JahiaObjectDelegate parentObjectDelegate){
        // use the first parent rule that is not inherited
        // apply to childs
        try {
            Collection childs = getChildObjectKeysForTimeBasedPublishing(jahiaObjectKey,
                    JahiaAdminUser.getAdminUser(parentObjectDelegate.getSiteId().intValue()),
                    EntryLoadRequest.STAGED,ParamBean.EDIT);
            Iterator iterator = childs.iterator();
            ObjectKey childObjectKey = null;
            JahiaObjectDelegate childObjectDelegate = null;
            while ( iterator.hasNext() ){
                childObjectKey = (ObjectKey)iterator.next();
                childObjectDelegate = jahiaObjectMgr.getJahiaObjectDelegate(childObjectKey);
                if (childObjectDelegate != null
                        && (childObjectDelegate.getRule() == null || childObjectDelegate
                                .getRule().getInherited().booleanValue())) {
                    copyParentStateToCurrentObjectAndChilds(childObjectKey, parentObjectDelegate);
                }
            }
            JahiaObjectDelegate objectDelegate = jahiaObjectMgr.getJahiaObjectDelegate(jahiaObjectKey);
            if ( objectDelegate != null ){
                objectDelegate.setTimeBPState(parentObjectDelegate.getTimeBPState());
                objectDelegate.setValidFromDate(parentObjectDelegate.getValidFromDate());
                objectDelegate.setValidToDate(parentObjectDelegate.getValidToDate());
                jahiaObjectMgr.save(objectDelegate);
            }
        } catch ( Exception t) {
            logger.debug("Error copying parent state to childs",t);
        }
    }

/**
 *
 * @param theEvent
 * @param rule
 * @param jahiaObject
 * @return true if object state will change otherwise false
     */
    private boolean stateWillChange(RetentionRuleEvent theEvent,
                                    RetentionRule rule,
                                    JahiaObjectDelegate jahiaObject){
        if ( theEvent.getEventType() == RetentionRuleEvent.DELETING_RULE ){
            return ( jahiaObject.getTimeBPState().intValue() != IS_VALID_STATE);
        } else if ( theEvent.getEventType() == RetentionRuleEvent.RULE_SCHEDULING_NOTIFICATION ){
            if ( theEvent.getDateReached() == RetentionRuleEvent.VALID_TO_DATE_REACHED ){
                return ( jahiaObject.getTimeBPState().intValue() != EXPIRED_STATE );
            } else {
                return ( jahiaObject.getTimeBPState().intValue() != IS_VALID_STATE );
            }
        } else if ( theEvent.getEventType() == RetentionRuleEvent.UPDATING_RULE ){
            RangeRetentionRule rangeRule = (RangeRetentionRule)rule;
            if ( rule.getInherited().booleanValue() ){
                // check agains parent state
                try {
                    ObjectKey parentObjectKey = getParentObjectKeyForTimeBasedPublishing(jahiaObject.getObjectKey(), JahiaAdminUser.getAdminUser(jahiaObject.getSiteId().intValue()),
                                    EntryLoadRequest.STAGED,ParamBean.EDIT);
                    if ( parentObjectKey != null ) {
                        JahiaObjectDelegate parentDelegate = jahiaObjectMgr
                                .getJahiaObjectDelegate(parentObjectKey);
                        if ( parentDelegate != null ){
                            return ( jahiaObject.getTimeBPState().intValue()
                                    != parentDelegate.getTimeBPState().intValue() );
                        }
                    } else {
                        //case of home page without parent
                        if ( !jahiaObject.isValid() ){
                            JahiaObjectDelegate delegate = jahiaObjectMgr
                                    .getJahiaObjectDelegate(jahiaObject.getObjectKey());
                            delegate.setTimeBPState(new Integer(IS_VALID_STATE));
                            jahiaObjectMgr.save(delegate);
                            copyParentStateToCurrentObjectAndChilds(jahiaObject.getObjectKey(), delegate);
                            return true;
                        }
                    }
                } catch ( Exception t){
                    logger.debug("exception checking state change",t);
                }
            }
            if ( jahiaObject.getValidFromDate().longValue() != rangeRule.getValidFromDate().longValue() ){
                return true;
            } else if ( jahiaObject.getValidToDate().longValue() != rangeRule.getValidToDate().longValue() ){
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param theEvent
     * @param rule
     * @param jahiaObject
     * @return true if object state changed otherwise false
     */
    private boolean applyTimeBasedPublishingEvent(RetentionRuleEvent theEvent,
                                                  RetentionRule rule,
                                                  JahiaObjectDelegate jahiaObject){
        if ( theEvent.getEventType() == RetentionRuleEvent.DELETING_RULE ){
            jahiaObject.setTimeBPState(new Integer(IS_VALID_STATE));
            jahiaObject.setValidFromDate(new Long(0));
            jahiaObject.setValidToDate(new Long(0));
            RetentionRule localRule = jahiaObject.getRule();
            if ( localRule != null && localRule.getId().intValue() == rule.getId().intValue() ){
                jahiaObject.setRule(null);
            }
            jahiaObjectMgr.save(jahiaObject);
            return true;
        } else if ( theEvent.getEventType() == RetentionRuleEvent.RULE_SCHEDULING_NOTIFICATION ){
            if ( theEvent.getDateReached() == RetentionRuleEvent.VALID_TO_DATE_REACHED ){
                if ( jahiaObject.getTimeBPState().intValue() != EXPIRED_STATE ){
                    jahiaObject.setTimeBPState(new Integer(EXPIRED_STATE));
                    jahiaObjectMgr.save(jahiaObject);
                    return true;
                }
            } else {
                if ( jahiaObject.getTimeBPState().intValue() != IS_VALID_STATE ){
                    jahiaObject.setTimeBPState(new Integer(IS_VALID_STATE));
                    jahiaObjectMgr.save(jahiaObject);
                    return true;
                }
            }
        } else if ( theEvent.getEventType() == RetentionRuleEvent.UPDATING_RULE ){
            RangeRetentionRule rangeRule = (RangeRetentionRule)rule;
            //Long oldValidFromDate = jahiaObject.getValidFromDate();
            Long oldValidToDate = jahiaObject.getValidToDate();
            jahiaObject.setValidFromDate(rangeRule.getValidFromDate());
            jahiaObject.setValidToDate(rangeRule.getValidToDate());
            if ( jahiaObject.getValidFromDate().longValue() == 0
                    && jahiaObject.getTimeBPState().intValue() == NOT_VALID_STATE){
                jahiaObject.setTimeBPState(new Integer(IS_VALID_STATE));
            }
            if ( jahiaObject.getValidToDate().longValue() == 0
                    && jahiaObject.getTimeBPState().intValue() == EXPIRED_STATE){
                jahiaObject.setTimeBPState(new Integer(IS_VALID_STATE));
            }

            long serverUtcTime = System.currentTimeMillis();
            if ( jahiaObject.getValidToDate().longValue() > 0 ){
                try {
                    if ( serverUtcTime>jahiaObject.getValidToDate().longValue() ){
                        jahiaObject.setTimeBPState(new Integer(EXPIRED_STATE));
                        jahiaObjectMgr.save(jahiaObject);
                        return true;
                    } else {
                        if (jahiaObject.isExpired() && oldValidToDate != null
                                && oldValidToDate.longValue()<jahiaObject.getValidToDate().longValue()){
                            jahiaObject.setTimeBPState(new Integer(IS_VALID_STATE));
                        }
                    }
                } catch ( Exception t){
                }
            }
            if ( jahiaObject.getValidFromDate().longValue() > 0 ){
                try {
                    if ( serverUtcTime<jahiaObject.getValidFromDate().longValue() ){
                        jahiaObject.setTimeBPState(new Integer(NOT_VALID_STATE));
                    }
                } catch ( Exception t){
                }
            }
            jahiaObjectMgr.save(jahiaObject);
            return true;
        }
        return false;
    }
    
    /**
     * Returns child object keys of objects that implement TimeBasedPublishingJahiaObject interface
     * 
     * @param user
     * @param loadRequest
     * @param operationMode
     * @return
     */
    public Collection getChildObjectKeysForTimeBasedPublishing(
            ObjectKey jahiaObjectKey, JahiaUser user,
            EntryLoadRequest loadRequest, String operationMode)
            throws JahiaException {
        Collection filteredChildKeys = Collections.emptyList();
        
        if (ContentPageKey.PAGE_TYPE.equals(jahiaObjectKey.getType())) {
            ContentPageKey contentPageKey = (ContentPageKey) jahiaObjectKey;
            Collection children = contentPageKey.getChildLists(loadRequest);
            ContentContainerListKey child;
            filteredChildKeys = new ArrayList();
            for (Iterator it = children.iterator(); it.hasNext(); ) {
                child = (ContentContainerListKey) it.next();
                filteredChildKeys.addAll(getChildObjectKeysForTimeBasedPublishing(child, user, loadRequest, operationMode));
            }            
        } else if (ContentContainerListKey.CONTAINERLIST_TYPE
                .equals(jahiaObjectKey.getType())) {
            ContentContainerListKey contentContainerListKey = (ContentContainerListKey) jahiaObjectKey;            
            filteredChildKeys = contentContainerListKey.getChilds(loadRequest);
        } else if (ContentContainerKey.CONTAINER_TYPE.equals(jahiaObjectKey
                .getType())) {
            ContentContainerKey contentContainerKey = (ContentContainerKey) jahiaObjectKey;
            Collection childs = contentContainerKey.getChilds(loadRequest);
            
            Object child = null;
            filteredChildKeys = new ArrayList();
            for (Iterator it = childs.iterator(); it.hasNext();) {
                child = it.next();
                if (child instanceof ContentContainerListKey) {
                    filteredChildKeys.add(child);
                } else if (child instanceof ContentFieldKey) {
                    ContentFieldKey contentFieldKey = (ContentFieldKey) child;
                    // field child
                    filteredChildKeys.addAll(contentFieldKey.getChilds(loadRequest));
                }
            }
        }
        return filteredChildKeys;
    }

    /**
     * Returns the parent object key of the current object for timebased publishing
     *
     * @return
     */
    public ObjectKey getParentObjectKeyForTimeBasedPublishing(ObjectKey jahiaObjectKey,
            JahiaUser user, EntryLoadRequest loadRequest, String operationMode)
            throws JahiaException {
        return getParentObjectKeyForTimeBasedPublishing(jahiaObjectKey, user, loadRequest,
                operationMode, false);
    }

    /**
     * Returns the parent object key of the current object for timebased publishing
     */
    public ObjectKey getParentObjectKeyForTimeBasedPublishing(ObjectKey jahiaObjectKey,
            JahiaUser user, EntryLoadRequest loadRequest, String operationMode,
            boolean withoutInheritedRule) throws JahiaException {
        ObjectKey parentObjectKey = null;
        if (ContentPageKey.PAGE_TYPE.equals(jahiaObjectKey.getType())) {
            ContentPageKey contentPageKey = (ContentPageKey) jahiaObjectKey;
            
            ContentObjectKey pageFieldKey = contentPageKey.getParent(loadRequest);
            
            if (pageFieldKey != null) {
                parentObjectKey = pageFieldKey.getParent(loadRequest);
            }            
        } else if (ContentContainerListKey.CONTAINERLIST_TYPE
                .equals(jahiaObjectKey.getType())) {
            
            ContentContainerListKey contentContainerListKey = (ContentContainerListKey) jahiaObjectKey;
            parentObjectKey = contentContainerListKey.getParent(loadRequest);
            
        } else if (ContentContainerKey.CONTAINER_TYPE.equals(jahiaObjectKey
                .getType())) {
            
            ContentContainerKey contentContainerKey = (ContentContainerKey) jahiaObjectKey;
            parentObjectKey = contentContainerKey.getParent(loadRequest).getParent(loadRequest);
        }
        
        if (withoutInheritedRule && parentObjectKey != null) {
            final RetentionRule retRule = ServicesRegistry
                    .getInstance().getTimeBasedPublishingService()
                    .getRetentionRule(parentObjectKey);
            if (retRule == null
                    || retRule.getInherited().booleanValue()) {
                parentObjectKey = getParentObjectKeyForTimeBasedPublishing(
                        parentObjectKey, user, loadRequest,
                        operationMode, withoutInheritedRule);
            }
        }
        return parentObjectKey;
    }

    public JahiaFieldsDataManager getFieldsDataMgr() {
        return fieldsDataMgr;
    }

    public void setFieldsDataMgr(JahiaFieldsDataManager fieldsDataMgr) {
        this.fieldsDataMgr = fieldsDataMgr;
    }

    public JahiaContainerListManager getContainerListMgr() {
        return containerListMgr;
    }

    public void setContainerListMgr(JahiaContainerListManager containerListMgr) {
        this.containerListMgr = containerListMgr;
    }

    /**
     * Returns true if the given object is valid checking agains the AdvPreviewSettings
     *
     * @param objectKey
     * @param user
     * @param loadRequest
     * @param operationMode
     * @param advPreviewSettings
     * @return
     * @throws JahiaException
     */
    public boolean isValid(ObjectKey objectKey,
            JahiaUser user, EntryLoadRequest loadRequest, String operationMode, AdvPreviewSettings advPreviewSettings)
    throws JahiaException {

        Date date = null;
        if (advPreviewSettings != null && advPreviewSettings.isEnabled() && advPreviewSettings.getPreviewDate() != 0){
            date = new Date(advPreviewSettings.getPreviewDate());
        }
        return isValid(objectKey,user,loadRequest,operationMode,date);
    }

    /**
     * Returns true if the given object is valid at the given date
     * If the given date is null, returns delegate.isValid().
     *
     * @param objectKey
     * @param user
     * @param loadRequest
     * @param operationMode
     * @param date
     * @return
     * @throws JahiaException
     */
    public boolean isValid(ObjectKey objectKey,
            JahiaUser user, EntryLoadRequest loadRequest, String operationMode, Date date) throws JahiaException {

        JahiaObjectDelegate delegate = null;
        if (batchLoadingSize > 1){
            delegate = jahiaObjectMgr.getJahiaObjectDelegate(objectKey, batchLoadingSize);
        } else {
            delegate = jahiaObjectMgr.getJahiaObjectDelegate(objectKey);
        }
        if ( delegate == null ){
            return false;
        }
        if (date == null){
            return delegate.isValid();
        }
        RetentionRule rule = delegate.getRule();
        if (rule==null){
            return true;
        }
        if (Boolean.TRUE.equals(rule.getInherited())){
            objectKey = getParentObjectKeyForTimeBasedPublishing(objectKey,
            user, loadRequest, operationMode, true);
            if (objectKey == null){
                return false;
            }
            return isValid(objectKey,user,loadRequest,operationMode,date);
        }
        try {
            return rule.isValid(date);
        } catch (Exception e){
            throw new JahiaException("Exception checking timebased publishing status",
                    "Exception checking timebased publishing status",JahiaException.APPLICATION_ERROR,
                    JahiaException.ERROR_SEVERITY);
        }
    }

	public void setBatchLoadingSize(int batchLoadingSize) {
    	this.batchLoadingSize = batchLoadingSize;
    }
}
