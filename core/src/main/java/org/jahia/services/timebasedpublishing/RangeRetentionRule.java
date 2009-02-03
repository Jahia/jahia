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

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.jahia.content.JahiaObject;
import org.jahia.engines.validation.ValidationError;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaObjectDelegate;
import org.jahia.hibernate.model.JahiaRangeRetentionRule;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.SchedulerService;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 28 juil. 2005
 * Time: 12:53:53
 * To change this template use File | Settings | File Templates.
 */
public class RangeRetentionRule extends BaseRetentionRule {

    private static final transient Logger logger = Logger.getLogger(TimeBasedPublishingJob.class);

    public static final String VALID_FROM_DATE_EL = "valid-from-date";
    public static final String VALID_TO_DATE_EL = "valid-to-date";

    private Long validFromDate = new Long(0);

    private Long validToDate = new Long(0);

    private Boolean notifiedValidFromDate = Boolean.FALSE;

    private Boolean notifiedValidToDate = Boolean.FALSE;

    public RangeRetentionRule() {
        super();
    }

    /**
     *
     * @param validFromDate
     * @param validToDate
     * @param notifiedValidFromDate
     * @param notifiedValidToDate
     * @param recurrenceType
     * @param startDate
     * @param endDate
     * @param dailyStartDate
     * @param dailyEndDate
     * @param dailyFromHours
     * @param dailyFromMinutes
     * @param dailyToHours
     * @param dailyToMinutes
     * @param daysInWeekStartDate
     * @param daysInWeekEndDate
     * @param daysInWeek
     */
    public RangeRetentionRule(final Long validFromDate,
                              final Long validToDate,
                              final Boolean notifiedValidFromDate,
                              final Boolean notifiedValidToDate,
                              final String recurrenceType,
                              final Long startDate,
                              final Long endDate,
                              final Long dailyStartDate,
                              final Long dailyEndDate,
                              final int dailyFromHours,
                              final int dailyFromMinutes,
                              final int dailyToHours,
                              final int dailyToMinutes,
                              final Long daysInWeekStartDate,
                              final Long daysInWeekEndDate,
                              final List daysInWeek) {
        super();
        this.validFromDate = validFromDate;
        this.validToDate = validToDate;
        this.notifiedValidFromDate = notifiedValidFromDate;
        this.notifiedValidToDate = notifiedValidToDate;
        this.ruleType = recurrenceType;
        this.setStartDate(startDate);
        this.setEndDate(endDate);
        this.setDailyStartDate(dailyStartDate);
        this.setDailyEndDate(dailyEndDate);
        this.setDailyFromHours(dailyFromHours);
        this.setDailyFromMinutes(dailyFromMinutes);
        this.setDailyToHours(dailyToHours);
        this.setDailyToMinutes(dailyToMinutes);
        this.setDaysInWeekStartDate(daysInWeekStartDate);
        this.setDaysInWeekEndDate(daysInWeekEndDate);
        this.setDaysInWeek(daysInWeek);
    }

    public Long getValidFromDate() {
        return validFromDate;
    }

    public void setValidFromDate(final Long validFromDate) {
        this.validFromDate = validFromDate;
    }

    public Long getValidToDate() {
        return validToDate;
    }

    public void setValidToDate(final Long validToDate) {
        this.validToDate = validToDate;
    }

    public Boolean getNotifiedValidFromDate() {
        return notifiedValidFromDate;
    }

    public void setNotifiedValidFromDate(final Boolean notifiedValidFromDate) {
        this.notifiedValidFromDate = notifiedValidFromDate;
    }

    public Boolean getNotifiedValidToDate() {
        return notifiedValidToDate;
    }

    public void setNotifiedValidToDate(final Boolean notifiedValidToDate) {
        this.notifiedValidToDate = notifiedValidToDate;
    }

    /**
     * Start the rule scheduling job
     *
     * @return
     * @throws Exception
     */
    public boolean startJob() throws Exception {
        SchedulerService schedulerServ = ServicesRegistry.getInstance().getSchedulerService();
        schedulerServ.deleteJob(RetentionRuleJob.JOB_NAME_PREFIX + this.getId(), RetentionRuleJob.JOB_GROUP_NAME);
        
        RangeRetentionRule effectiveRule = this;
        if (Boolean.TRUE.equals(effectiveRule.getInherited())) {
            //if rule is inherited then the job from the inherited rule will take care
          return false;
        }

        long serverUTCTime = System.currentTimeMillis();
        long fromDate = 0;
        if (effectiveRule.getValidFromDate() != null && effectiveRule.getValidFromDate().longValue()>0) {
            fromDate = effectiveRule.getValidFromDate().longValue();
            if (fromDate < serverUTCTime) {
                fromDate = serverUTCTime;
            }            
        }

        long toDate = 0;
        if (effectiveRule.getValidToDate() != null && effectiveRule.getValidToDate().longValue()>0) {
            toDate = effectiveRule.getValidToDate().longValue();
        }
        if ((fromDate == 0 && toDate == 0)
                || (toDate != 0 && fromDate != 0 && (toDate - fromDate) < 0 && fromDate != serverUTCTime)) {
            return false;
        }
        JobDetail jobDetail =
                new JobDetail(RetentionRuleJob.JOB_NAME_PREFIX + this.getId(),
                        RetentionRuleJob.JOB_GROUP_NAME, RetentionRuleJob.class);
        jobDetail.setRequestsRecovery(true);
        jobDetail.getJobDataMap().put(BackgroundJob.JOB_TYPE, "timebased");
        jobDetail.getJobDataMap().put(RetentionRuleJob.RULE_ID, this.getId().intValue());
        SimpleTrigger trigger = null;
        if (fromDate == 0) {
            jobDetail.getJobDataMap().put(RetentionRuleJob.FROM_DATE_ALREADY_HANDLED, Boolean.TRUE);
            trigger = new SimpleTrigger(RetentionRuleJob.TRIGGER_NAME_PREFIX + this.getId(),
                    SchedulerService.SCHEDULED_TRIGGER_GROUP, new Date(toDate), null, 0, 0L);
        } else {
            if (toDate == 0) {
                trigger = new SimpleTrigger(RetentionRuleJob.TRIGGER_NAME_PREFIX + this.getId(),
                        SchedulerService.SCHEDULED_TRIGGER_GROUP, new Date(fromDate), null, 0, 0L);
            } else {
                long diff = toDate - fromDate;
                int repeatCount = 1;
                if (diff < 0) {
                    diff = 0;
                    repeatCount = 0;
                    jobDetail.getJobDataMap().put(RetentionRuleJob.FROM_DATE_ALREADY_HANDLED, Boolean.TRUE);
                }
                trigger = new SimpleTrigger(RetentionRuleJob.TRIGGER_NAME_PREFIX + this.getId(),
                        SchedulerService.SCHEDULED_TRIGGER_GROUP, new Date(fromDate), null, repeatCount, diff);
            }
        }
        if (trigger != null) {
            jobDetail.setDurability(false);
            jobDetail.setRequestsRecovery(true);
            jobDetail.setVolatility(false);
            trigger.setVolatility(false);
            schedulerServ.scheduleJob(jobDetail, trigger);
            return true;
        }
        return false;
    }

    /**
     * Stop the rule scheduling job
     *
     * @return
     * @throws Exception
     */
    public boolean stopJob() throws Exception {
        SchedulerService schedulerServ =
                ServicesRegistry.getInstance().getSchedulerService();
        schedulerServ.deleteJob(RetentionRuleJob.JOB_NAME_PREFIX + this.getId(),
                RetentionRuleJob.JOB_GROUP_NAME);
        return true;
    }

    /**
     * Delete the rule scheduling job if any
     *
     * @return
     * @throws Exception
     */
    public boolean deleteJob() throws Exception {
        SchedulerService schedulerServ =
                ServicesRegistry.getInstance().getSchedulerService();
        schedulerServ.deleteJob(RetentionRuleJob.JOB_NAME_PREFIX + this.getId(),
                RetentionRuleJob.JOB_GROUP_NAME);
        return true;
    }

    /**
     * This method is called once a retention rule job finished.
     * Implementing classes should check for recurrence type and schedule next fire time
     *
     * @param context
     * @return the next fire time. 0 if no fire time
     * @throws Exception
     */
    public long scheduleNextJob(ProcessingContext context) throws Exception {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        long nextFireTime = 0;
        if ( RetentionRule.RULE_DAILY.equals(getRuleType()) ){
            cal.set(Calendar.SECOND,0);
            cal.set(Calendar.MILLISECOND,0);
            cal.set(Calendar.HOUR_OF_DAY,getDailyFromHours());
            cal.set(Calendar.MINUTE,getDailyFromMinutes());
            long timeShift = 0;
            boolean fromHourSmallerThanToHour =
                (getDailyFromHours() < getDailyToHours()
                || (getDailyFromHours() == getDailyToHours()
                        && getDailyFromMinutes()<getDailyToMinutes()) ) ;
            if (fromHourSmallerThanToHour){
                timeShift = 86400000;
            }
            setValidFromDate(new Long(cal.getTimeInMillis()+timeShift));
            cal.set(Calendar.HOUR_OF_DAY,getDailyToHours());
            cal.set(Calendar.MINUTE,getDailyToMinutes());
            timeShift = 86400000;
            setValidToDate(new Long(cal.getTimeInMillis()+timeShift));
        } else if ( RetentionRule.RULE_XDAYINWEEK.equals(getRuleType()) ){
            DayInWeekBean.computeRuleNextEventDate(this,cal.getTimeInMillis(),cal,0,false);
        }
        boolean canSchedule =  postScheduleNextJob();
        if ( !canSchedule ){
            return 0;
        }
        TimeBasedPublishingService tbServ = ServicesRegistry.getInstance().getTimeBasedPublishingService();
        Collection c =  tbServ.getJahiaObjectMgr().getJahiaObjectDelegateByRuleId(this.getId().intValue());
        for (Iterator iterator = c.iterator(); iterator.hasNext();) {
            JahiaObjectDelegate jahiaObjectDelegate = (JahiaObjectDelegate) iterator.next();
            if ( jahiaObjectDelegate != null ){
                try {
                    JahiaObject jahiaObject = JahiaObject
                            .getInstance(jahiaObjectDelegate.getObjectKey());
                    tbServ.scheduleBackgroundJob(jahiaObject.getObjectKey(),TimeBasedPublishingJob.UPDATE_OPERATION,this,context);
                } catch ( Exception t) {
                    logger.debug("Error converting JahiaObjectDelegate to JahiaObjec",t);
                    return 0;
                }
            }
        }
        return nextFireTime;
    }

    /**
     * Sub classes should override this method to extends the default behavior.
     *
     * @return
     * @throws Exception
     */
    protected boolean postScheduleNextJob() throws Exception {
        return true;
    }

    protected org.jahia.hibernate.model.JahiaRetentionRule getJahiaRetentionRuleFromSubClass() throws JahiaException {
        JahiaRangeRetentionRule rule = new JahiaRangeRetentionRule();
        rule.setId(this.getId());
        rule.setTitle(this.getTitle());
        rule.setComment(this.getComment());
        rule.setEnabled(this.getEnabled());
        rule.setInherited(this.getInherited());
        rule.setShared(this.getShared());
        rule.setValidFromDate(this.getValidFromDate());
        rule.setValidToDate(this.getValidToDate());
        rule.setNotifiedValidFromDate(this.getNotifiedValidFromDate());
        rule.setNotifiedValidToDate(this.getNotifiedValidToDate());
        rule.setSettings(this.getSettings());
        return rule;
    }

    /**
     * Sub classes may override this method to store extended settings in base XML document.
     * This method is called by @see #getSettings
     *
     * @param settings
     */
    public void appendExtendedSettings(Element settings){
        if ( settings == null ){
            return;
        }
        settings.addElement(VALID_FROM_DATE_EL).addText(String.valueOf(this.validFromDate));
        settings.addElement(VALID_TO_DATE_EL).addText(String.valueOf(this.validToDate));
    }

    /**
     * Sub classes may override this method to store extended settings in base XML document.
     * This method is called by @see #getSettings
     *
     * @param settings
     */
    public void loadExtendedSettings(Element settings){
        if ( settings == null ){
            return;
        }
        Element el = settings.element(VALID_FROM_DATE_EL);
        if ( el != null ){
            this.setValidFromDate(new Long(el.getText()));
        }
        el = settings.element(VALID_TO_DATE_EL);
        if ( el != null ){
            this.setValidToDate(new Long(el.getText()));
        }
    }

    /**
     * Return a list of ValidationErrors if any
     *
     * @param source
     * @return
     */
    public List validate(Object source) {
        List errors = super.validate(source);
        if ( !errors.isEmpty() ){
            return errors;
        }
        if (getValidFromDate() != null && getValidToDate() != null
                && getValidToDate().longValue() > 0
                && (getValidToDate().longValue() < getValidFromDate().longValue())){
            errors.add(new ValidationError(source,"ValidToDateMustBeBiggerThanValidFromDate"));
        }
        return errors;
    }

    public Object clone() throws CloneNotSupportedException {
        RangeRetentionRule clone = new RangeRetentionRule();
        clone.setComment(getComment());
        clone.setEnabled(getEnabled());
        clone.setId(getId());
        clone.setInherited(getInherited());
        clone.setRuleType(getRuleType());
        clone.setNotifiedValidFromDate(this.getNotifiedValidFromDate());
        clone.setNotifiedValidToDate(this.getNotifiedValidToDate());
        clone.setRetentionRuleDef(this.getRetentionRuleDef());
        clone.setShared(getShared());
        clone.setTitle(getTitle());
        clone.setValidFromDate(getValidFromDate());
        clone.setValidToDate(getValidToDate());
        clone.setStartDate(getStartDate());
        clone.setEndDate(getEndDate());
        clone.setDailyStartDate(getDailyStartDate());
        clone.setDailyEndDate(getDailyEndDate());
        clone.setDailyFromHours(getDailyFromHours());
        clone.setDailyFromMinutes(getDailyFromMinutes());
        clone.setDailyToHours(getDailyToHours());
        clone.setDailyToMinutes(getDailyToMinutes());
        clone.setDaysInWeekStartDate(getDaysInWeekStartDate());
        clone.setDaysInWeekEndDate(getDaysInWeekEndDate());
        clone.setDaysInWeek(DayInWeekBean.cloneList(getDaysInWeek()));
        return clone;
    }

    public boolean equals(Object o) {
        if (this == o) return true;

        if (o != null && this.getClass() == o.getClass()) {
            final RangeRetentionRule castOther = (RangeRetentionRule) o;
            EqualsBuilder equalsBuilder = new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .append(this.getInherited(), castOther.getInherited())
                .append(this.getRuleType(), castOther.getRuleType())
                .append(this.getValidFromDate(), castOther.getValidFromDate())
                .append(this.getValidToDate(), castOther.getValidToDate())
                .append(this.getNotifiedValidFromDate(), castOther.getNotifiedValidFromDate())
                .append(this.getNotifiedValidToDate(), castOther.getNotifiedValidToDate())
                .append(this.getStartDate(), castOther.getStartDate())
                .append(this.getEndDate(), castOther.getEndDate())
                .append(this.getDailyStartDate(), castOther.getDailyStartDate())
                .append(this.getDailyEndDate(), castOther.getDailyEndDate())
                .append(this.getDailyFromHours(), castOther.getDailyFromHours())
                .append(this.getDailyFromMinutes(), castOther.getDailyFromMinutes())
                .append(this.getDailyToHours(), castOther.getDailyToHours())
                .append(this.getDailyToMinutes(), castOther.getDailyToMinutes())
                .append(this.getDaysInWeekStartDate(), castOther.getDaysInWeekStartDate())
                .append(this.getDaysInWeekEndDate(), castOther.getDaysInWeekEndDate())
                .append(this.getDailyEndDate(), castOther.getDailyEndDate());

                if ( !equalsBuilder.isEquals() ){
            return false;
                }
                return DayInWeekBean.sameDaysSetting(this.getDaysInWeek(),castOther.getDaysInWeek());
        }
            return false;
    }

    public String toString() {
        final StringBuffer buff = new StringBuffer();
        buff.append(this.getClass().getName());
        buff.append(" id: ").append(id);
        buff.append(" ruleType: ").append(ruleType);
        buff.append(", validFrom: ").append(validFromDate);
        buff.append(", validTo: ").append(validToDate);
        return buff.toString();
    }
}
