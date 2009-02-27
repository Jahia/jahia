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

package org.jahia.engines.timebasedpublishing;

import org.jahia.content.*;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.hibernate.manager.JahiaObjectDelegate;
import org.jahia.hibernate.manager.JahiaObjectManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockPrerequisites;
import org.jahia.services.lock.LockPrerequisitesResult;
import org.jahia.services.timebasedpublishing.*;
import org.jahia.engines.calendar.CalendarHandler;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * Date: Nov 20, 2003
 * Copyright Codeva 2003
 *
 * @author Thomas Draier
 */
public class TimeBasedPublishingEngine {
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(TimeBasedPublishingEngine.class);

    public static final String ENGINE_NAME = "timebasedpublishing_engine";
    public static final String SUB_ENGINE_MAP_NAME = "timebasedpublishing_engine_map";
    private static TimeBasedPublishingEngine instance = null;
    private static final String JSP = "/engines/timebasedpublishing/timebasedpublishing.jsp";
    public static final String READONLY_JSP = "/engines/timebasedpublishing/readonly_timebasedpublishing.jsp";
    private JahiaObjectManager jahiaObjectMgr = null;
    private Map oldRules = new HashMap();

    private TimeBasedPublishingEngine() {
        jahiaObjectMgr = (JahiaObjectManager) SpringContextSingleton
                .getInstance().getContext().getBean(JahiaObjectManager.class.getName());
    }

    /**
     * @return a single instance of the object
     */
    public static synchronized TimeBasedPublishingEngine getInstance() {
        if (instance == null) {
            instance = new TimeBasedPublishingEngine();
        }
        return instance;
    }

    public boolean handleActions(ProcessingContext jParams, int mode, Map engineMap, ObjectKey objectKey)
            throws JahiaException, JahiaSessionExpirationException {
        switch (mode) {
            case (JahiaEngine.LOAD_MODE) :
                return load(jParams, engineMap, objectKey);
            case (JahiaEngine.UPDATE_MODE) :
                return update(jParams, engineMap, objectKey);
            case (JahiaEngine.SAVE_MODE) :
                return save(jParams, engineMap, objectKey);
        }
        return false;
    }

    public boolean load(ProcessingContext jParams, Map engineMap, ObjectKey objectKey)
            throws JahiaException, JahiaSessionExpirationException {

        final Map subEngineMap = getSubEngineMap(jParams, engineMap, objectKey);
        /*
        final RangeRetentionRule currentRule = (RangeRetentionRule) subEngineMap.get(ENGINE_NAME + ".currentRule");
        if (currentRule != null) {
            try {
                RangeRetentionRule clonedRule = (RangeRetentionRule)currentRule.clone();
            oldRules.put(objectKey.toString(), clonedRule);
            } catch ( Exception t ){
                throw new JahiaException("Error creating rule clone","Error creating rule clone",JahiaException.ENGINE_ERROR,
                        JahiaException.ERROR_SEVERITY,t);
            }
        }*/
        engineMap.put("fieldsEditCallingEngineName", ENGINE_NAME);

        final LockPrerequisitesResult results = LockPrerequisites.getInstance().getLockPrerequisitesResult((LockKey) engineMap.get("LockKey"));
        boolean isLocked = false;
        if (results != null) {
            isLocked = results.getReadOnlyTabs().contains(LockPrerequisites.TIME_BASED_PUBLISHING) ||
                            results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT);
        }
        final boolean readOnly = (results != null && isLocked);

        if (readOnly) {
            engineMap.put(ENGINE_NAME + ".fieldForm",
                    ServicesRegistry.getInstance().getJahiaFetcherService().fetchServlet((ParamBean) jParams, READONLY_JSP));
        } else {
            engineMap.put(ENGINE_NAME + ".fieldForm", ServicesRegistry.getInstance().
                    getJahiaFetcherService().fetchServlet((ParamBean) jParams, JSP));
        }

        return true;
    }

    public boolean update(ProcessingContext jParams, Map engineMap, ObjectKey objectKey)
            throws JahiaException, JahiaSessionExpirationException {

        engineMap.remove(ENGINE_NAME + ".EngineValidationError");

        Map subEngineMap = getSubEngineMap(jParams, engineMap, objectKey);
        TimeBasedPublishingService tbpServ = ServicesRegistry.getInstance().getTimeBasedPublishingService();

        boolean changed = false;

        RangeRetentionRule rule = (RangeRetentionRule) subEngineMap.get(ENGINE_NAME + ".currentRule");
        String value = null;
        if (rule != null) {
            // update rule
            value = jParams.getParameter("inherited");
            if (value == null) {
                value = "0";
            }
            changed |= !Boolean.valueOf("1".equals(value)).equals(rule.getInherited());
            rule.setInherited(Boolean.valueOf("1".equals(value)));

            value = jParams.getParameter("ruleType");
            if (value != null && !value.equals(rule.getRuleType())) {
                rule.setRuleType(value);
                changed = true;
            }
            if (!rule.getInherited().booleanValue() && !BaseRetentionRule.RULE_NONE.equals(rule.getRuleType())) {
              changed |= updateRuleRecurrenceSettings(rule, jParams, engineMap, subEngineMap, objectKey);
            }  
        }

        value = jParams.getParameter("ruledefs");
        if ("none".equals(value)) {
            subEngineMap.remove(ENGINE_NAME + ".currentRuleDef");
        } else if (value != null) {
            RetentionRuleDef currentRuleDef = (RetentionRuleDef) subEngineMap.get(ENGINE_NAME + ".currentRuleDef");
            if (currentRuleDef == null || !currentRuleDef.getId().equals(new Integer(value))) {
                currentRuleDef = tbpServ.getRetentionRuleDef(new Integer(value));
                subEngineMap.put(ENGINE_NAME + ".currentRuleDef", currentRuleDef);
                if (rule == null || rule.getRetentionRuleDef().getId().intValue()
                        != currentRuleDef.getId().intValue()) {
                    rule = (RangeRetentionRule)currentRuleDef.createRule();
                    subEngineMap.put(ENGINE_NAME + ".currentRule", rule);
                    //todo supoort ofr other rule
                    subEngineMap.put(ENGINE_NAME + ".fromDateCalHandler", getCalHandler(rule, "fromDate", jParams));
                    subEngineMap.put(ENGINE_NAME + ".toDateCalHandler", getCalHandler(rule, "toDate", jParams));
                    changed = true;
                }
            }
        }
        engineMap.put("tbpUpdated", changed);
        List ruleErrors = rule.validate(this);
        if ( !ruleErrors.isEmpty() ){
            EngineValidationHelper evh = new EngineValidationHelper();
            evh.setNextScreen("timeBasedPublishing");
            evh.setPreviousScreen("timeBasedPublishing");
            //ValidationError ve = new ValidationError(this, "ValidToDateMustBeBiggerThanValidFromDate");
            //evh.addError(ve);
            evh.getErrors().addAll(ruleErrors);
            engineMap.put(ENGINE_NAME + ".EngineValidationError",evh);

            final LockPrerequisitesResult results = LockPrerequisites.getInstance().getLockPrerequisitesResult((LockKey) engineMap.get("LockKey"));
            boolean isLocked = false;
            if (results != null) {
                isLocked = results.getReadOnlyTabs().contains(LockPrerequisites.TIME_BASED_PUBLISHING) ||
                                results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT);
            }
            final boolean readOnly = (results != null && isLocked);
            String currentScreen = (String)engineMap.get("screen");
            engineMap.put("screen","timeBasedPublishing");
            if (readOnly) {
                engineMap.put(ENGINE_NAME + ".fieldForm",
                        ServicesRegistry.getInstance().getJahiaFetcherService().fetchServlet((ParamBean) jParams, READONLY_JSP));
            } else {
                engineMap.put(ENGINE_NAME + ".fieldForm", ServicesRegistry.getInstance().
                        getJahiaFetcherService().fetchServlet((ParamBean) jParams, JSP));
            }
            engineMap.put("screen",currentScreen);
            return false;
        }

        return true;

    }

    private boolean updateRuleRecurrenceSettings(RangeRetentionRule rule, ProcessingContext jParams, Map engineMap,
                                              Map subEngineMap, ObjectKey objectKey)
    throws JahiaException, JahiaSessionExpirationException {
        boolean changed = false;
        long timeOffSet = 0;
        String oldRecurrenceType = jParams.getParameter("oldRuleType");

        Boolean oldEnableImmediatePublication = (Boolean)subEngineMap.get(ENGINE_NAME + ".enableImmediatePublication");
        if ( oldEnableImmediatePublication == null ){
            oldEnableImmediatePublication = Boolean.TRUE;
        }
        String enableImmediatePublicationStr = jParams.getParameter("enableImmediatePublication");
        boolean enableImmediatePublication = true;
        if ( enableImmediatePublicationStr != null ){
            enableImmediatePublication = "true".equals(enableImmediatePublicationStr);
            subEngineMap.put(ENGINE_NAME + ".enableImmediatePublication",
                    Boolean.valueOf(enableImmediatePublication));
        }

        if ( RetentionRule.RULE_NONE.equals(oldRecurrenceType) ){
            throw new UnsupportedOperationException("Not implemented yet!");
        } else if ( RetentionRule.RULE_START_AND_END_DATE.equals(oldRecurrenceType) ){
            long oldFrom = rule.getValidFromDate();
            long oldTo = rule.getValidToDate();

            CalendarHandler calHandler = null;
            calHandler = (CalendarHandler) subEngineMap.get(ENGINE_NAME + ".fromDateCalHandler");
            calHandler.update(jParams);
            try {
                if (calHandler.getDateLong().longValue() > 0) {
                    rule.setStartDate(calHandler.getDateLong());
                    rule.setValidFromDate(calHandler.getDateLong());
                } else {
                    rule.setValidFromDate(new Long(0));
                }
            } catch (Exception t) {
                rule.setValidFromDate(new Long(0));
            }
            changed |= oldFrom != rule.getValidFromDate();
            calHandler = (CalendarHandler) subEngineMap.get(ENGINE_NAME + ".toDateCalHandler");
            calHandler.update(jParams);
            try {
                if (calHandler.getDateLong().longValue() > 0) {
                    rule.setEndDate(calHandler.getDateLong());
                    rule.setValidToDate(calHandler.getDateLong());
                } else {
                    rule.setValidToDate(new Long(0));
                }
            } catch (Exception t) {
                rule.setValidToDate(new Long(0));
            }
            changed |= oldTo != rule.getValidToDate();
        } else if ( RetentionRule.RULE_DAILY.equals(oldRecurrenceType) ){
            CalendarHandler calHandler = null;
            calHandler = (CalendarHandler) subEngineMap.get(ENGINE_NAME + ".fromDateCalHandler");
            calHandler.update(jParams);
            Long l = calHandler.getTimeZoneOffSet();
            if ( l == null ){
                l = new Long(0);
            }
            timeOffSet -= l.longValue();

            // update daily recurrence parameters
            String value = jParams.getParameter("fromHours");
            if (value != null) {
                changed |= rule.getDailyFromHours() != Integer.parseInt(value);
                rule.setDailyFromHours(Integer.parseInt(value));
            }
            value = jParams.getParameter("fromMinutes");
            if (value != null) {
                changed |= rule.getDailyFromMinutes() != Integer.parseInt(value);
                rule.setDailyFromMinutes(Integer.parseInt(value));
            }
            value = jParams.getParameter("toHours");
            if (value != null) {
                changed |= rule.getDailyToHours() != Integer.parseInt(value);
                rule.setDailyToHours(Integer.parseInt(value));
            }
            value = jParams.getParameter("toMinutes");
            if (value != null) {
                changed |= rule.getDailyToMinutes() != Integer.parseInt(value);
                rule.setDailyToMinutes(Integer.parseInt(value));
            }
        } else if ( RetentionRule.RULE_XDAYINWEEK.equals(oldRecurrenceType) ){
            CalendarHandler calHandler = null;
            calHandler = (CalendarHandler) subEngineMap.get(ENGINE_NAME + ".fromDateCalHandler");
            calHandler.update(jParams);
            Long l = calHandler.getTimeZoneOffSet();
            if ( l == null ){
                l = new Long(0);
            }
            timeOffSet -= l.longValue();

            // update weekly recurrence parameters
            Iterator it = rule.getDaysInWeek().iterator();

            DayInWeekBean dayBean = null;
            String[] daysInWeek = jParams.getParameterValues("daysInWeek");
            List selectedDays = daysInWeek != null ? Arrays.asList(daysInWeek):new ArrayList();
            while ( it.hasNext() ){
                dayBean = (DayInWeekBean)it.next();
                changed |= selectedDays.contains(dayBean.getDay()) != dayBean.isSelected();
                dayBean.setSelected(selectedDays.contains(dayBean.getDay()));

                int value = Integer.parseInt(jParams.getParameter("from_" + dayBean.getDay() + "Hours"));
                changed |= value != dayBean.getFromHours();
                dayBean.setFromHours(value);

                value = Integer.parseInt(jParams.getParameter("from_" + dayBean.getDay() + "Minutes"));
                changed |= value != dayBean.getFromMinutes();
                dayBean.setFromMinutes(value);

                value = Integer.parseInt(jParams.getParameter("to_" + dayBean.getDay() + "Hours"));
                changed |= value != dayBean.getToHours();
                dayBean.setToHours(value);

                value = Integer.parseInt(jParams.getParameter("to_" + dayBean.getDay() + "Minutes"));
                changed |= value != dayBean.getToMinutes();
                dayBean.setToMinutes(value);
            }
            
        }

        // compute next publish and next unpublish date
        updateNextPublishAndExpireDate(rule,objectKey,timeOffSet,enableImmediatePublication,
                oldEnableImmediatePublication.booleanValue() != enableImmediatePublication);
        return changed;

    }

    private void updateNextPublishAndExpireDate(RangeRetentionRule rule,ObjectKey objectKey, long timeOffSet,
                                                boolean enableImmediatePublication,
                                                boolean enableImmediatePublicationHasChange)
    throws JahiaException {
        final RangeRetentionRule oldRule = (RangeRetentionRule) oldRules.get(objectKey.toString());
        if ( isSame(oldRule,rule) && !enableImmediatePublicationHasChange ){
            return;
        }
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        int serverDefaultOffSet = cal.getTimeZone().getRawOffset();
        int serverDsts = cal.getTimeZone().getDSTSavings();
        int timeOffSetInt = 0;
        if ( timeOffSet != 0 ){
            Long l = new Long((timeOffSet+serverDefaultOffSet+serverDsts)/(60*60*1000));
            timeOffSetInt = -l.intValue();
        }
        if ( RetentionRule.RULE_DAILY.equals(rule.getRuleType()) ){
            int fromTime = rule.getDailyFromHours() * 60 + rule.getDailyFromMinutes();
            int toTime = rule.getDailyToHours() * 60 + rule.getDailyToMinutes();
            int currentTime = cal.get(Calendar.HOUR_OF_DAY) + timeOffSetInt;
            if ( currentTime < 0 ){
                currentTime = 24-currentTime;
            }
            currentTime = currentTime * 60  + cal.get(Calendar.MINUTE);
            boolean inPeriod = (fromTime<toTime && fromTime<=currentTime && currentTime<=toTime)
                    || (fromTime>toTime && (currentTime>=fromTime || currentTime<=toTime) );
            cal.set(Calendar.SECOND,0);
            cal.set(Calendar.MILLISECOND,0);
            cal.set(Calendar.HOUR_OF_DAY,rule.getDailyFromHours());
            cal.set(Calendar.MINUTE,rule.getDailyFromMinutes());
            if ( rule.getDailyFromHours() == rule.getDailyToHours() ){
                if ( rule.getDailyToMinutes()<rule.getDailyFromMinutes() ){
                    if ( inPeriod ){
                        if ( enableImmediatePublication ){
                            cal.setTimeInMillis(cal.getTimeInMillis()-86400000);
                        } else if ( fromTime<=currentTime ) {
                            cal.setTimeInMillis(cal.getTimeInMillis()+86400000);
                        }
                    }
                } else {
                    if ( inPeriod ){
                        if ( !enableImmediatePublication ){
                            cal.setTimeInMillis(cal.getTimeInMillis()+86400000);
                        }
                    }
                }
            } else if ( rule.getDailyFromHours() > rule.getDailyToHours() ){
                if ( inPeriod ){
                    if ( enableImmediatePublication ){
                        cal.setTimeInMillis(cal.getTimeInMillis()-86400000);
                    } else if ( fromTime<=currentTime ) {
                        cal.setTimeInMillis(cal.getTimeInMillis()+86400000);
                    }
                }
            } else {
                if ( inPeriod ){
                    if ( !enableImmediatePublication ){
                        cal.setTimeInMillis(cal.getTimeInMillis()+86400000);
                    }
                }
            }
            rule.setValidFromDate(new Long(cal.getTimeInMillis()+timeOffSet));
            cal = Calendar.getInstance(TimeZone.getDefault());
            cal.set(Calendar.SECOND,0);
            cal.set(Calendar.MILLISECOND,0);
            cal.set(Calendar.HOUR_OF_DAY,rule.getDailyToHours());
            cal.set(Calendar.MINUTE,rule.getDailyToMinutes());
            if ( rule.getDailyFromHours() == rule.getDailyToHours() ){
                if ( rule.getDailyToMinutes()<rule.getDailyFromMinutes() ){
                    if ( inPeriod ){
                        if ( !enableImmediatePublication ){
                            if (fromTime<=currentTime){
                                cal.setTimeInMillis(cal.getTimeInMillis()+86400000*2);
                            } else {
                                cal.setTimeInMillis(cal.getTimeInMillis()+86400000);
                            }
                        }
                    } else {
                        cal.setTimeInMillis(cal.getTimeInMillis()+86400000);
                    }
                } else {
                    if ( inPeriod ){
                        if ( !enableImmediatePublication ){
                            cal.setTimeInMillis(cal.getTimeInMillis()+86400000);
                        }
                    }
                }
            } else if ( rule.getDailyFromHours() > rule.getDailyToHours() ){
                if ( inPeriod ){
                    if ( !enableImmediatePublication ){
                        if (fromTime<=currentTime){
                            cal.setTimeInMillis(cal.getTimeInMillis()+86400000*2);
                        } else {
                            cal.setTimeInMillis(cal.getTimeInMillis()+86400000);
                        }
                    }
                } else {
                    cal.setTimeInMillis(cal.getTimeInMillis()+86400000);
                }
            } else {
                if ( inPeriod ){
                    if ( !enableImmediatePublication ){
                        cal.setTimeInMillis(cal.getTimeInMillis()+86400000);
                    }
                }
            }
            rule.setValidToDate(new Long(cal.getTimeInMillis()+timeOffSet));
        } else if ( RetentionRule.RULE_XDAYINWEEK.equals(rule.getRuleType()) ){
            DayInWeekBean.computeRuleNextEventDate(rule,0,cal,timeOffSet,
                    enableImmediatePublication);
        }
    }

    private boolean save(ProcessingContext jParams, Map engineMap, ObjectKey objectKey)
            throws JahiaException {

        Map subEngineMap = getSubEngineMap(jParams, engineMap, objectKey);
        TimeBasedPublishingService tbpServ = ServicesRegistry.getInstance().getTimeBasedPublishingService();
        RetentionRuleDef currentRuleDef = (RetentionRuleDef) subEngineMap.get(ENGINE_NAME + ".currentRuleDef");

        final RangeRetentionRule oldRule = (RangeRetentionRule) oldRules.get(objectKey.toString());

        try {
            Boolean enableImmediatePublication = (Boolean)subEngineMap.get(ENGINE_NAME + ".enableImmediatePublication");
            ContentObject contentObject = ContentObject.getContentObjectInstance(objectKey);
            String val = contentObject
                    .getProperty(TimeBasedPublishingEngine.ENGINE_NAME+".enableImmediatePublication");
            boolean oldValue = ("true".equals(val) || val==null);
            if ( enableImmediatePublication != null && enableImmediatePublication.booleanValue() != oldValue ){
                contentObject.setProperty(TimeBasedPublishingEngine.ENGINE_NAME+".enableImmediatePublication",
                        enableImmediatePublication.toString());
            }
        } catch ( Exception t ){
        }

        if (currentRuleDef != null) {
            final RangeRetentionRule rule = (RangeRetentionRule) subEngineMap.get(ENGINE_NAME + ".currentRule");
            if (logger.isDebugEnabled()) {
                logger.debug("old: " + oldRule);
                logger.warn("new: " + rule);
            }
            if (isSame(oldRule,rule)) {
                boolean applyParentState = false;
                if (rule.getId().intValue() == -1) {
                    // newly rule, we have to check on parent
                    try {
                        if (ContentPageKey.PAGE_TYPE
                                .equals(objectKey.getType())
                                || ContentContainerKey.CONTAINER_TYPE
                                        .equals(objectKey.getType())
                                || ContentContainerListKey.CONTAINERLIST_TYPE
                                        .equals(objectKey.getType())) {
                            ObjectKey parentObjectKey = tbpServ
                                    .getParentObjectKeyForTimeBasedPublishing(
                                            objectKey, jParams.getUser(),
                                            jParams.getEntryLoadRequest(),
                                            jParams.getOperationMode());
                            if (parentObjectKey != null) {
                                final JahiaObjectManager jahiaObjectManager = (JahiaObjectManager) SpringContextSingleton
                                        .getInstance().getContext().getBean(
                                                JahiaObjectManager.class
                                                        .getName());
                                JahiaObjectDelegate parentObjectDelegate = jahiaObjectManager
                                        .getJahiaObjectDelegate(parentObjectKey);
                                if (parentObjectDelegate.isNotValid()
                                        || parentObjectDelegate.isExpired()
                                        || parentObjectDelegate
                                                .willExpire(System
                                                        .currentTimeMillis())) {
                                    applyParentState = true;
                                }
                            }
                        }
                    } catch (Exception t) {
                    }
                }
                if( !applyParentState ){
                    return true;
                }
            }
            logger.debug("InvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsi");
            try {
                tbpServ.scheduleBackgroundJob(objectKey,TimeBasedPublishingJob.UPDATE_OPERATION,rule,jParams);
            } catch (Exception e) {
                throw new JahiaException("Error saving Retention Rule", "Error saving Retention Rule",
                        JahiaException.ENGINE_ERROR, JahiaException.ERROR_SEVERITY, e);
            }
        } else {
            try {
                final RetentionRule rule = tbpServ.getRetentionRule(objectKey);
                if (rule != null) {
                    tbpServ.scheduleBackgroundJob(objectKey,TimeBasedPublishingJob.DELETE_OPERATION,rule,jParams);
                }
            } catch (Exception e) {
                throw new JahiaException("Error deleting Retention Rule", "Error saving Retention Rule",
                        JahiaException.ENGINE_ERROR, JahiaException.ERROR_SEVERITY, e);
            }
        }
        return true;
    }

    private Map getSubEngineMap(ProcessingContext jParams, Map engineMap, ObjectKey objectKey)
            throws JahiaException {
        Map subEngineMap = (Map) engineMap.get(SUB_ENGINE_MAP_NAME);
        if (subEngineMap == null) {
            subEngineMap = new HashMap();
            engineMap.put(SUB_ENGINE_MAP_NAME, subEngineMap);
            JahiaObjectDelegate jahiaObjectDelegate = jahiaObjectMgr.getJahiaObjectDelegate(objectKey);
            subEngineMap.put(ENGINE_NAME + ".jahiaObjectDelegate", jahiaObjectDelegate);
            TimeBasedPublishingService tbpServ = ServicesRegistry.getInstance().getTimeBasedPublishingService();
            RetentionRule rule = tbpServ.getRetentionRule(objectKey);

            if (rule == null) {
                final RetentionRuleDef baseDef = tbpServ.getBaseRetentionRuleDef();
                rule = baseDef.createRule();
                rule.setInherited(Boolean.TRUE);
            }
            try {
                subEngineMap.put(ENGINE_NAME + ".currentRule", rule.clone());
            } catch ( Exception t ){
                throw new JahiaException("Error cloning rule","Error cloning rule",JahiaException.APPLICATION_ERROR,
                    JahiaException.ERROR_SEVERITY,t);
            }
            oldRules.put(objectKey.toString(), rule);
            subEngineMap.put(ENGINE_NAME + ".currentRuleDef", rule.getRetentionRuleDef());
            // todo supoort ofr other rule
            subEngineMap.put(ENGINE_NAME + ".fromDateCalHandler", getCalHandler(rule, "fromDate", jParams));
            subEngineMap.put(ENGINE_NAME + ".toDateCalHandler", getCalHandler(rule, "toDate", jParams));

            subEngineMap.put(ENGINE_NAME + ".ruleDefs", tbpServ.getRetentionRuleDefs());

            try {
                ContentObject contentObject = ContentObject.getContentObjectInstance(objectKey);
                if (contentObject != null) {
                    String val = contentObject
                            .getProperty(TimeBasedPublishingEngine.ENGINE_NAME+".enableImmediatePublication");
                    boolean enableImmediatePublication = ("true".equals(val) || val==null);
                    subEngineMap.put(ENGINE_NAME + ".enableImmediatePublication", Boolean.valueOf(enableImmediatePublication));
                }
            } catch ( Exception t ){
            }
        }
        return subEngineMap;
    }

    public static CalendarHandler getCalHandler(RetentionRule rule, String calIdentifier, ProcessingContext jParams) {
        RangeRetentionRule rangeRule = (RangeRetentionRule) rule;
        BaseRetentionRuleDef ruleDef = (BaseRetentionRuleDef) rangeRule.getRetentionRuleDef();
        Long dateLong = new Long(0);
        if ("fromDate".equals(calIdentifier) && rangeRule.getValidFromDate() != null &&
                rangeRule.getValidFromDate().longValue()>0) {
            dateLong = rangeRule.getValidFromDate();
        }
        if ("toDate".equals(calIdentifier) && rangeRule.getValidToDate() != null  &&
                rangeRule.getValidToDate().longValue()>0) {
            dateLong = rangeRule.getValidToDate();
        }
        return new CalendarHandler(jParams.settings().getJahiaEnginesHttpPath(),
                        calIdentifier,
                        "mainForm",
                        ruleDef.getDateFormat(),
                        dateLong,
                        jParams.getLocale(),
                        new Long(0));
    }

    private boolean isSame(RetentionRule oldRetentionRule, RetentionRule newRetentionRule){
        if ( oldRetentionRule == null && newRetentionRule == null ){
            return true;
        }
        if ( oldRetentionRule != null && newRetentionRule != null ){
            boolean result = oldRetentionRule.equals(newRetentionRule);
            return (result && (oldRetentionRule.getInherited().booleanValue()
                        == newRetentionRule.getInherited().booleanValue()));

        }
        return false;
    }
}
