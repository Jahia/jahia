/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.timebasedpublishing;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.*;
import java.io.Serializable;

/**
 * Holds information for a day in X Days in week recurrence.
 *
 */
public class DayInWeekBean implements Serializable {

    public static final String MONDAY = "mon";
    public static final String TUESDAY = "tue";
    public static final String WEDNESDAY = "wed";
    public static final String THURSDAY = "thu";
    public static final String FRIDAY = "fri";
    public static final String SATURSDAY = "sat";
    public static final String SUNDAY = "sun";

    private String day;
    private int dayIndex;
    private boolean selected;
    private int fromHours;
    private int toHours;
    private int fromMinutes;
    private int toMinutes;

    public DayInWeekBean(){
    }

    public DayInWeekBean(String day, int dayIndex) {
        this.day = day;
        this.dayIndex = dayIndex;
    }

    public DayInWeekBean(String day, boolean selected, int fromHours, int toHours, int fromMinutes,
                         int toMinutes, int dayIndex) {
        this.day = day;
        this.selected = selected;
        this.fromHours = fromHours;
        this.toHours = toHours;
        this.fromMinutes = fromMinutes;
        this.toMinutes = toMinutes;
        this.dayIndex = dayIndex;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getFromHours() {
        return fromHours;
    }

    public void setFromHours(int fromHours) {
        this.fromHours = fromHours;
    }

    public int getToHours() {
        return toHours;
    }

    public void setToHours(int toHours) {
        this.toHours = toHours;
    }

    public int getFromMinutes() {
        return fromMinutes;
    }

    public void setFromMinutes(int fromMinutes) {
        this.fromMinutes = fromMinutes;
    }

    public int getToMinutes() {
        return toMinutes;
    }

    public void setToMinutes(int toMinutes) {
        this.toMinutes = toMinutes;
    }

    /**
     * The day index starting from 0 -> monday,...1 -> sunday
     *
     * @return
     */
    public int getDayIndex() {
        return dayIndex;
    }

    public void setDayIndex(int dayIndex) {
        this.dayIndex = dayIndex;
    }

    public static List createDaysList(){
        List days = new ArrayList();
        days.add(new DayInWeekBean(MONDAY,0));
        days.add(new DayInWeekBean(TUESDAY,1));
        days.add(new DayInWeekBean(WEDNESDAY,2));
        days.add(new DayInWeekBean(THURSDAY,3));
        days.add(new DayInWeekBean(FRIDAY,4));
        days.add(new DayInWeekBean(SATURSDAY,5));
        days.add(new DayInWeekBean(SUNDAY,6));
        return days;
    }

    /**
     * return the correct Day of week in java Calendar contention
     *
     * @param dayBean
     * @return
     */
    public static int getJavaCalendarDayOfWeek(DayInWeekBean dayBean){
        if ( dayBean.getDayIndex()<6 ){
            return dayBean.getDayIndex()+2; // Calendar.MONDAY = 2
        } else {
            return 1; // Calendar.SUNDAY = 1
        }
    }

    /**
     * Test wether two days list are same
     *
     * @param days1
     * @param days2
     * @return
     */
    public static boolean sameDaysSetting(List days1, List days2){
        if ( days1 == days2 ){
            return true;
        }
        if ( days1 == null && days2 == null ){
            return true;
        }
        if ( days1 == null || days2 == null ){
            return false;
        }
        if ( days1.size() != days2.size() ){
            return false;
        }
        Iterator it = days1.iterator();
        DayInWeekBean dayBean = null;
        DayInWeekBean dayBean2 = null;
        while ( it.hasNext() ){
            dayBean = (DayInWeekBean)it.next();
            dayBean2 = (DayInWeekBean)days2.get(dayBean.getDayIndex());
            if ( !dayBean.equals(dayBean2) ){
                return false;
            }
        }
        return true;
    }

    public static List cloneList(List days){
        if ( days == null ){
            return null;
        }
        if ( days.isEmpty() ){
            return new ArrayList();
        }
        List result = new ArrayList();
        Iterator it = days.iterator();
        while ( it.hasNext() ){
            result.add(((DayInWeekBean)it.next()).clone());
        }
        return result;
    }

    public static void computeRuleNextEventDate(RangeRetentionRule newRule,
                                                long startDate, Calendar cal, long timeOffSet,
                                                boolean enableImmediatePublication){
        DayInWeekBean dayBean = null;
        long nextPublishTime = 0;
        long nextExpireTime = 0;
        Iterator it = newRule.getDaysInWeek().iterator();
        SortedSet nextPublishDates = new TreeSet();
        SortedSet nextExpireDates = new TreeSet();
        while ( it.hasNext() ){
            dayBean = (DayInWeekBean)it.next();
            if ( dayBean.isSelected() ){
                computeNextEventsTime(cal,dayBean,nextPublishDates,nextExpireDates,timeOffSet);
            }
        }
        // get the next nearest publish date
        Date date = null;
        it = nextPublishDates.iterator();
        if ( !enableImmediatePublication ){
            List inversedDateList = new ArrayList();
            while ( it.hasNext() ){
                inversedDateList.add(0,it.next());
            }
            it = inversedDateList.iterator();
        }
        long now = cal.getTimeInMillis()+timeOffSet;
        if ( it.hasNext() ){
            while ( it.hasNext() ){
                date = (Date)it.next();
                if (date.getTime()>startDate ){
                    if ( nextPublishTime == 0 ){
                        nextPublishTime = date.getTime();
                    } else if ( enableImmediatePublication && date.getTime()<now && date.getTime()>nextPublishTime ){
                        nextPublishTime = date.getTime();
                    } else if ( !enableImmediatePublication && date.getTime()>now && date.getTime()<nextPublishTime ){
                        nextPublishTime = date.getTime();
                    }
                }
            }
            newRule.setValidFromDate(new Long(nextPublishTime));
        } else {
            newRule.setValidFromDate(new Long(0));
        }

        // get the next nearest expire date
        it = nextExpireDates.iterator();
        if ( nextPublishTime >0 && it.hasNext() ){
            while ( it.hasNext() ){
                date = (Date)it.next();
                if ( date.getTime()>nextPublishTime ){
                    nextExpireTime = date.getTime();
                    break;
                }
            }
            newRule.setValidToDate(new Long(nextExpireTime));
        } else {
            newRule.setValidToDate(new Long(0));
        }
    }

    private static void computeNextEventsTime(Calendar nowCalendar,
                                       DayInWeekBean dayBean,
                                       Set nextPublishDates,
                                       Set nextExpireDates,
                                       long timeOffSet){

        if ( ((dayBean.getFromHours()+dayBean.getFromMinutes())==0)
              && ((dayBean.getToHours()+dayBean.getToMinutes())==0) ) {
            return;
        }

        Calendar nowCal = (Calendar)nowCalendar.clone();
        int todayDayInWeek = nowCalendar.get(Calendar.DAY_OF_WEEK);
        int dayInWeek = DayInWeekBean.getJavaCalendarDayOfWeek(dayBean);
        nowCal.set(Calendar.DAY_OF_WEEK,dayInWeek);
        if ( dayInWeek != 1 && dayInWeek<=todayDayInWeek ){
            nowCal.add(Calendar.WEEK_OF_MONTH,1); // add one week
        }
        nowCal.set(Calendar.HOUR_OF_DAY,dayBean.getFromHours());
        nowCal.set(Calendar.MINUTE,dayBean.getFromMinutes());
        nowCal.set(Calendar.SECOND,0);
        nowCal.set(Calendar.MILLISECOND,0);
        nowCal.setTimeInMillis(nowCal.getTimeInMillis()+timeOffSet);
        nextPublishDates.add(nowCal.getTime());

        nowCal = (Calendar)nowCalendar.clone();
        nowCal.set(Calendar.HOUR_OF_DAY,dayBean.getToHours());
        nowCal.set(Calendar.MINUTE,dayBean.getToMinutes());
        nowCal.set(Calendar.SECOND,0);
        nowCal.set(Calendar.MILLISECOND,0);
        nowCal.set(Calendar.DAY_OF_WEEK,dayInWeek);
        if ( dayInWeek != 1 && dayInWeek<=todayDayInWeek ){
            nowCal.add(Calendar.WEEK_OF_MONTH,1); // add one week
        }
        if ( (dayBean.getFromHours() == dayBean.getToHours() &&
             dayBean.getToMinutes()< dayBean.getFromMinutes())
                || dayBean.getFromHours()>dayBean.getToHours() ){
            nowCal.setTimeInMillis(nowCal.getTimeInMillis()+86400000);
        }
        nowCal.setTimeInMillis(nowCal.getTimeInMillis()+timeOffSet);
        nextExpireDates.add(nowCal.getTime());


        int fromTime = dayBean.getFromHours() * 60 + dayBean.getFromMinutes();
        int toTime = dayBean.getToHours() * 60 + dayBean.getToMinutes();

        nowCal = (Calendar)nowCalendar.clone();
        if ( todayDayInWeek == dayInWeek ){
            nowCal.set(Calendar.DAY_OF_WEEK,dayInWeek);
            nowCal.set(Calendar.HOUR_OF_DAY,dayBean.getFromHours());
            nowCal.set(Calendar.MINUTE,dayBean.getFromMinutes());
            nowCal.set(Calendar.SECOND,0);
            nowCal.set(Calendar.MILLISECOND,0);
            nowCal.setTimeInMillis(nowCal.getTimeInMillis()+timeOffSet);
            nextPublishDates.add(nowCal.getTime());
            nowCal = (Calendar)nowCalendar.clone();
            nowCal.set(Calendar.HOUR_OF_DAY,dayBean.getToHours());
            nowCal.set(Calendar.MINUTE,dayBean.getToMinutes());
            nowCal.set(Calendar.SECOND,0);
            nowCal.set(Calendar.MILLISECOND,0);
            if ( (dayBean.getFromHours() == dayBean.getToHours() &&
                 dayBean.getToMinutes()< dayBean.getFromMinutes())
                    || dayBean.getFromHours()>dayBean.getToHours() ){
                nowCal.setTimeInMillis(nowCal.getTimeInMillis()+86400000);
            }
            nowCal.setTimeInMillis(nowCal.getTimeInMillis()+timeOffSet);
            nextExpireDates.add(nowCal.getTime());
        } else if ( (todayDayInWeek - dayInWeek == 1)
                || (todayDayInWeek == Calendar.SUNDAY && dayInWeek == Calendar.SATURDAY) ){
            if ( fromTime>toTime ){
                // add yesterday
                nowCal.set(Calendar.HOUR_OF_DAY,dayBean.getFromHours());
                nowCal.set(Calendar.MINUTE,dayBean.getFromMinutes());
                nowCal.set(Calendar.SECOND,0);
                nowCal.set(Calendar.MILLISECOND,0);
                nowCal.setTimeInMillis(nowCal.getTimeInMillis()+timeOffSet-86400000);
                nextPublishDates.add(nowCal.getTime());
                nowCal = (Calendar)nowCalendar.clone();
                nowCal.set(Calendar.HOUR_OF_DAY,dayBean.getToHours());
                nowCal.set(Calendar.MINUTE,dayBean.getToMinutes());
                nowCal.set(Calendar.SECOND,0);
                nowCal.set(Calendar.MILLISECOND,0);
                nowCal.setTimeInMillis(nowCal.getTimeInMillis()+timeOffSet);
                nextExpireDates.add(nowCal.getTime());
            }
        }
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            final DayInWeekBean castOther = (DayInWeekBean) obj;
            return new EqualsBuilder()
                .append(this.getDay(), castOther.getDay())
                .append(this.getDayIndex(), castOther.getDayIndex())
                .append(this.isSelected(), castOther.isSelected())
                .append(this.getFromHours(), castOther.getFromHours())
                .append(this.getToHours(), castOther.getToHours())
                .append(this.getFromMinutes(), castOther.getFromMinutes())
                .append(this.getToMinutes(), castOther.getToMinutes())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.getDay())
                .append(this.getDayIndex())
                .append(this.isSelected())
                .append(this.getFromHours())
                .append(this.getToHours())
                .append(this.getFromMinutes())
                .append(this.getToMinutes())
                .toHashCode();
    }

    public Object clone(){
        DayInWeekBean clone = new DayInWeekBean(this.getDay(),this.getDayIndex());
        clone.setFromHours(this.getFromHours());
        clone.setFromMinutes(this.getFromMinutes());
        clone.setToHours(this.getToHours());
        clone.setToMinutes(this.getToMinutes());
        clone.setSelected(this.isSelected());
        return clone;
    }

}
