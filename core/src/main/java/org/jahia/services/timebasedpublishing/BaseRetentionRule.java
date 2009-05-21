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

import org.apache.commons.collections.FastArrayList;
import org.dom4j.Element;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.model.JahiaRetentionRule;
import org.jahia.params.ProcessingContext;

import java.util.*;

public abstract class BaseRetentionRule implements RetentionRule {

    public static final List ruleTypes;
    protected static final int MINIGHT_IN_MINUTES = getMinutes(24,0);

    protected Integer id = new Integer(-1);
    private RetentionRuleDef retentionRuleDef;
    private String title = "";
    private String comment = "";
    private Boolean shared = Boolean.FALSE;
    private Boolean enabled = Boolean.TRUE;
    private Boolean inherited = Boolean.FALSE;

    protected String ruleType = RULE_START_AND_END_DATE;

    // start and end date
    protected Long startDate = new Long(0);
    protected Long endDate = new Long(0);

    // recurrence daily settings
    protected Long dailyStartDate = new Long(0);
    protected Long dailyEndDate = new Long(0);
    protected int dailyFromHours = 0;
    protected int dailyFromMinutes = 0;
    protected int dailyToHours = 0;
    protected int dailyToMinutes = 0;

    // recurrence x days in week settings
    protected List daysInWeek = DayInWeekBean.createDaysList();
    protected Long daysInWeekStartDate = new Long(0);
    protected Long daysInWeekEndDate = new Long(0);

    static {
        FastArrayList types = new FastArrayList(4);
        types.add(RULE_NONE);
        types.add(RULE_START_AND_END_DATE);
        types.add(RULE_DAILY);
        types.add(RULE_XDAYINWEEK);
        types.setFast(true);
        ruleTypes = types;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final BaseRetentionRule that = (BaseRetentionRule) o;

        return !(id != null ? !id.equals(that.id) : that.id != null);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public RetentionRuleDef getRetentionRuleDef() {
        return retentionRuleDef;
    }

    public void setRetentionRuleDef(
            RetentionRuleDef retentionRuleDef) {
        this.retentionRuleDef = retentionRuleDef;
    }

    public Boolean getInherited() {
        return inherited;
    }

    public void setInherited(Boolean inherited) {
        this.inherited = inherited;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Boolean getShared() {
        return shared;
    }

    public void setShared(Boolean shared) {
        this.shared = shared;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String recurrenceType) {
        this.ruleType = recurrenceType;
    }

    public Long getDailyStartDate() {
        return dailyStartDate;
    }

    public void setDailyStartDate(Long dailyStartDate) {
        this.dailyStartDate = dailyStartDate;
    }

    public Long getDailyEndDate() {
        return dailyEndDate;
    }

    public void setDailyEndDate(Long dailyEndDate) {
        this.dailyEndDate = dailyEndDate;
    }

    public int getDailyFromHours() {
        return dailyFromHours;
    }

    public void setDailyFromHours(int dailyFromHours) {
        this.dailyFromHours = dailyFromHours;
    }

    public int getDailyFromMinutes() {
        return dailyFromMinutes;
    }

    public void setDailyFromMinutes(int dailyFromMinutes) {
        this.dailyFromMinutes = dailyFromMinutes;
    }

    public int getDailyToHours() {
        return dailyToHours;
    }

    public void setDailyToHours(int dailyToHours) {
        this.dailyToHours = dailyToHours;
    }

    public int getDailyToMinutes() {
        return dailyToMinutes;
    }

    public void setDailyToMinutes(int dailyToMinutes) {
        this.dailyToMinutes = dailyToMinutes;
    }

    public List getDaysInWeek() {
        return daysInWeek;
    }

    public void setDaysInWeek(List daysInWeek) {
        this.daysInWeek = daysInWeek;
    }

    public Long getDaysInWeekStartDate() {
        return daysInWeekStartDate;
    }

    public void setDaysInWeekStartDate(Long daysInWeekStartDate) {
        this.daysInWeekStartDate = daysInWeekStartDate;
    }

    public Long getDaysInWeekEndDate() {
        return daysInWeekEndDate;
    }

    public void setDaysInWeekEndDate(Long daysInWeekEndDate) {
        this.daysInWeekEndDate = daysInWeekEndDate;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }
    
    public JahiaRetentionRule getJahiaRetentionRule() throws JahiaException {
        JahiaRetentionRule rule = getJahiaRetentionRuleFromSubClass();
        if (rule!= null){
            rule.setRetentionRuleDef(((BaseRetentionRuleDef)this.getRetentionRuleDef()).getJahiaRetentionRuleDef());
        }
        return rule;
    }

    /**
     * Build an XML representation of all recurrence settings
     *
     * @return
     * @throws Exception
     */
    public String getSettings() throws JahiaException {
        return RetentionRuleXMLTools.getRuleSettings(this);
    }

    /**
     * Sub classes may override this method to store extended settings in base XML document.
     * This method is called by @see #getSettings
     *
     * @param settings
     */
    public void appendExtendedSettings(Element settings){
    }

    /**
     * Initialize the rule's internal state with the settings stored as XML document
     * 
     * @param settings
     * @throws JahiaException
     */
    public void loadSettings(String settings) throws JahiaException {
        RetentionRuleXMLTools.loadRuleSettings(this,settings);
    }

    /**
     * Sub classes may override this method to store extended settings in base XML document.
     * This method is called by @see #getSettings
     *
     * @param settings
     */
    public void loadExtendedSettings(Element settings){
    }

    /**
     * Return a list of ValidationErrors if any
     *
     * @param source
     * @return
     */
    public List validate(Object source) {
        return new ArrayList();
        /*
        List errors = new ArrayList();
        Iterator it = this.getDaysInWeek().iterator();
        DayInWeekBean dayBean = null;
        while (it.hasNext()){
            dayBean = (DayInWeekBean)it.next();
            if(dayBean.getFromHours()*60+dayBean.getFromMinutes()>dayBean.getToHours()*60+dayBean.getToMinutes()){
                errors.add(new ValidationError(source,"FromHourMustBeBiggerThanToHour_"+dayBean.getDay()));
            }
        }
        return errors;*/
    }

    protected abstract JahiaRetentionRule getJahiaRetentionRuleFromSubClass() throws JahiaException;

    public void save() throws Exception {
        this.getRetentionRuleDef().saveRule(this);
    }

    public void delete() throws Exception {
        this.getRetentionRuleDef().deleteRule(this);
    }

    /**
     * Start the rule scheduling job
     * @return
     * @throws Exception
     */
    public abstract boolean startJob() throws Exception;

    /**
     * Stop the rule scheduling job
     * @return
     * @throws Exception
     */
    public abstract boolean stopJob() throws Exception;

    /**
     * Delete the rule scheduling job if any
     * @return
     * @throws Exception
     */
    public abstract boolean deleteJob() throws Exception;    

    /**
     * This method is called once a retention rule job finished.
     * Implementing classes should check for recurrence type and schedule next fire time
     *
     * @param context
     * @return the next fire time
     * @throws Exception
     */
    public abstract long scheduleNextJob(ProcessingContext context) throws Exception;

    public abstract Object clone() throws CloneNotSupportedException;

    /**
     * Returns true if the rule is valid at the given date
     *
     * @param date
     * @return
     * @throws Exception
     */
    public boolean isValid(Date date) throws Exception {

        if (Boolean.TRUE.equals(this.inherited)){
            throw new JahiaException("Cannot invoke this method on a rule that inherits from its parent",
                "Cannot invoke this method on a rule that inherits from its parent",JahiaException.APPLICATION_ERROR,
                JahiaException.ERROR_SEVERITY);
        }
        long dateTime = date.getTime();
        if (RetentionRule.RULE_NONE.equals(this.ruleType)) {
            // always valid
            return true;
        } else if (RetentionRule.RULE_START_AND_END_DATE.equals(this.ruleType)){
            return ( (this.getStartDate()==null||this.getStartDate().longValue()<=dateTime)
                    && (this.getEndDate()==null || this.getEndDate().longValue()>dateTime));
        } else if (RetentionRule.RULE_DAILY.equals(this.ruleType)){
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(dateTime);
            int hours = calendar.get(Calendar.HOUR_OF_DAY);
            int minutes = calendar.get(Calendar.MINUTE);
            int hoursInMinutes = getMinutes(hours, minutes);
            int fromHoursInMinutes = getMinutes(dailyFromHours,dailyFromMinutes);
            int toHoursInMinutes = getMinutes(dailyToHours,dailyToMinutes);
            if (fromHoursInMinutes < toHoursInMinutes){
                return (fromHoursInMinutes<=hoursInMinutes && toHoursInMinutes>hoursInMinutes);
            } else if (fromHoursInMinutes>toHoursInMinutes){
                return ( (fromHoursInMinutes<hoursInMinutes && hoursInMinutes<MINIGHT_IN_MINUTES)
                        || (0<=hoursInMinutes && hoursInMinutes<toHoursInMinutes) );
            }
        } else if (RetentionRule.RULE_XDAYINWEEK.equals(this.ruleType)){
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(dateTime);
            int hours = calendar.get(Calendar.HOUR_OF_DAY);
            int minutes = calendar.get(Calendar.MINUTE);
            int hoursInMinutes = getMinutes(hours, minutes);

            int dateDay= calendar.get(Calendar.DAY_OF_WEEK);
            DayInWeekBean dateDayBean = null;
            DayInWeekBean dayBeforeDateDayBean = null;
            Iterator dayInWeekIterator = this.daysInWeek.iterator();
            DayInWeekBean dayBean = null;
            int javaCalendarDayOfWeek = 0;
            while (dayInWeekIterator.hasNext()){
                dayBean = (DayInWeekBean)dayInWeekIterator.next();
                if (!dayBean.isSelected()){
                    continue;
                }
                javaCalendarDayOfWeek = DayInWeekBean.getJavaCalendarDayOfWeek(dayBean);
                if (dateDay==javaCalendarDayOfWeek){
                    dateDayBean = dayBean;
                } else if (dateDay==Calendar.SUNDAY && javaCalendarDayOfWeek == Calendar.SATURDAY){
                    dayBeforeDateDayBean = dayBean;
                } else if (javaCalendarDayOfWeek == dateDay-1){
                    dayBeforeDateDayBean = dayBean;
                }
            }

            if (dateDayBean == null && dayBeforeDateDayBean == null){
                return true;
            }
            int fromHoursInMinutes = 0;
            int toHoursInMinutes = 0;
            if ( dateDayBean != null ){
                fromHoursInMinutes = getMinutes(dateDayBean.getFromHours(),dateDayBean.getFromMinutes());
                toHoursInMinutes = getMinutes(dateDayBean.getToHours(),dateDayBean.getToMinutes());
                if (fromHoursInMinutes < toHoursInMinutes
                    && (fromHoursInMinutes<=hoursInMinutes && toHoursInMinutes>hoursInMinutes)){
                    return true;
                } else if (fromHoursInMinutes>toHoursInMinutes
                    && ( (fromHoursInMinutes<hoursInMinutes && hoursInMinutes<MINIGHT_IN_MINUTES)
                            || (0<=hoursInMinutes && hoursInMinutes<toHoursInMinutes) )){
                    return true;
                }
            }
            if ( dayBeforeDateDayBean != null ){
                fromHoursInMinutes = getMinutes(dayBeforeDateDayBean.getFromHours(),
                        dayBeforeDateDayBean.getFromMinutes());
                toHoursInMinutes = getMinutes(dayBeforeDateDayBean.getToHours(),
                        dayBeforeDateDayBean.getToMinutes());
                if (fromHoursInMinutes>toHoursInMinutes
                    && (0<=hoursInMinutes && hoursInMinutes<toHoursInMinutes) ){
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    protected static int getMinutes(int hours, int minutes){
        return hours * 60 + minutes;
    }
}
