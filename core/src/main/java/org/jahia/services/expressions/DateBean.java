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
package org.jahia.services.expressions;

import org.jahia.params.ProcessingContext;
import org.jahia.engines.calendar.CalendarHandler;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A Helper class used in Jahia Expression
 * <p/>
 * Example of expressions : ${date.formattedNowDate} // returns the current formatted date.
 * ${date.format(now-2*week)}.
 * <p/>
 * To change this template use File | Settings | File Templates.
 */
public class DateBean {

    public static final String SINCE_TODAY_EXP = "${date.formattedSinceToday}";
    public static final String SINCE_ONE_DAY_EXP = "${date.formattedSinceOneDay}";
    public static final String SINCE_TWO_DAYS_EXP = "${date.formattedSinceTwoDays}";
    public static final String SINCE_THREE_DAYS_EXP = "${date.formattedSinceThreeDays}";
    public static final String SINCE_THIS_WEEK_EXP = "${date.formattedSinceThisWeek}";
    public static final String SINCE_ONE_WEEK_EXP = "${date.formattedSinceOneWeek}";
    public static final String SINCE_TWO_WEEKS_EXP = "${date.formattedSinceTwoWeeks}";
    public static final String SINCE_THREE_WEEKS_EXP = "${date.formattedSinceThreeWeeks}";
    public static final String SINCE_THIS_MONTH_EXP = "${date.formattedSinceThisMonth}";
    public static final String SINCE_ONE_MONTH_EXP = "${date.formattedSinceOneMonth}";
    public static final String SINCE_TWO_MONTHS_EXP = "${date.formattedSinceTwoMonths}";
    public static final String SINCE_THREE_MONTHS_EXP = "${date.formattedSinceThreeMonths}";
    public static final String SINCE_SIX_MONTHS_EXP = "${date.formattedSinceSixMonths}";
    public static final String SINCE_THIS_YEAR_EXP = "${date.formattedSinceThisYear}";
    public static final String SINCE_ONE_YEAR_EXP = "${date.formattedSinceOneYear}";

    public static final String TIME_ZONE = "UTC";

    private SimpleDateFormat dateFormat;
    private Calendar cal;

    public DateBean() {
    }

    /**
     * @param context
     * @param dateFormat default <code>DATE_FORMAT</code>
     * @param timeZone   default <code>TIME_ZONE</code>
     */
    public DateBean(ProcessingContext context, String dateFormat, String timeZone) {
        if (dateFormat == null || dateFormat.trim().equals("")) {
            dateFormat = CalendarHandler.DEFAULT_DATE_FORMAT;
        }
        Locale l = Locale.getDefault();
        if (context != null && context.getLocale() != null) {
            l = context.getLocale();
        }
        this.dateFormat = new SimpleDateFormat(dateFormat, l);

        String tz = timeZone;
        if (tz == null || "".equals(tz.trim())) {
            tz = TIME_ZONE;
        }
        cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone(tz));
        cal.setTime(new Date());
    }

    public DateBean(ProcessingContext context, SimpleDateFormat dateFormat) {
        this.dateFormat = dateFormat;
        if (this.dateFormat == null) {
            Locale l = Locale.getDefault();
            if (context != null && context.getLocale() != null) {
                l = context.getLocale();
            }
            this.dateFormat = new SimpleDateFormat(CalendarHandler.DEFAULT_DATE_FORMAT, l);
        }
        cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone(TIME_ZONE));
        cal.setTime(new Date());
    }

    /**
     * Returns the current time
     *
     * @return
     */
    public Long getNow() {
        cal.setTime(new Date());
        cal.setTimeZone(TimeZone.getTimeZone(TIME_ZONE));
        return new Long(cal.getTimeInMillis());
    }

    /**
     * Returns a day long in millis
     *
     * @return
     */
    public Long getDay() {
        return new Long(1000 * 60 * 60 * 24);
    }

    /**
     * Returns a week duration in millis
     *
     * @return
     */
    public Long getWeek() {
        return new Long(1000 * 60 * 60 * 24 * 7);
    }

    /**
     * Returns a month duration in millis
     *
     * @return
     */
    public Long getMonth() {
        return new Long(1000 * 60 * 60 * 24 * 7 * 4L);
    }

    /**
     * Returns a year duration in millis
     *
     * @return
     */
    public Long getYear() {
        return new Long(1000 * 60 * 60 * 24 * 7 * 4L * 12L);
    }

    /**
     * Returns the formatted current time
     *
     * @return
     */
    public String getFormattedNowDate() {
        cal.setTime(new Date());
        cal.setTimeZone(TimeZone.getTimeZone(TIME_ZONE));
        return dateFormat.format(cal.getTime());
    }

    /**
     * Returns the formatted date at hour 0.
     *
     * @return
     */
    public String getFormattedSinceToday() {
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return dateFormat.format(cal.getTime());
    }

    public Date getToday() {
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }

    public Date getTomorrow() {
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_WEEK, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }

    public Date getYesterday() {
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_WEEK, -1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }

    public Date getNextDays(int offset) {
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_WEEK, offset);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }

    /**
     * Returns the formatted current date minus one day.
     *
     * @return
     */
    public String getFormattedSinceOneDay() {
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_WEEK, -1);
        return dateFormat.format(cal.getTime());
    }

    /**
     * Returns the formatted current date minus two days.
     *
     * @return
     */
    public String getFormattedSinceTwoDays() {
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_WEEK, -2);
        return dateFormat.format(cal.getTime());
    }

    public String getFormattedSinceThreeDays() {
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_WEEK, -3);
        return dateFormat.format(cal.getTime());
    }

    public String getFormattedSinceThisWeek() {
        cal.setTime(new Date());
        cal.set(Calendar.DAY_OF_WEEK_IN_MONTH, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return dateFormat.format(cal.getTime());
    }

    public String getFormattedSinceOneWeek() {
        cal.setTime(new Date());
        cal.add(Calendar.WEEK_OF_MONTH, -1);
        return dateFormat.format(cal.getTime());
    }

    public String getFormattedSinceTwoWeeks() {
        cal.setTime(new Date());
        cal.add(Calendar.WEEK_OF_MONTH, -2);
        return dateFormat.format(cal.getTime());
    }

    public String getFormattedSinceThreeWeeks() {
        cal.setTime(new Date());
        cal.add(Calendar.WEEK_OF_MONTH, -3);
        return dateFormat.format(cal.getTime());
    }

    public String getFormattedSinceThisMonth() {
        cal.setTime(new Date());
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return dateFormat.format(cal.getTime());
    }

    public String getFormattedSinceOneMonth() {
        cal.setTime(new Date());
        cal.add(Calendar.MONTH, -1);
        return dateFormat.format(cal.getTime());
    }

    public String getFormattedSinceTwoMonths() {
        cal.setTime(new Date());
        cal.add(Calendar.MONTH, -2);
        return dateFormat.format(cal.getTime());
    }

    public String getFormattedSinceThreeMonths() {
        cal.setTime(new Date());
        cal.add(Calendar.MONTH, -3);
        return dateFormat.format(cal.getTime());
    }

    public String getFormattedSinceSixMonths() {
        cal.setTime(new Date());
        cal.add(Calendar.MONTH, -6);
        return dateFormat.format(cal.getTime());
    }

    public String getFormattedSinceThisYear() {
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return dateFormat.format(cal.getTime());
    }

    public String getFormattedSinceOneYear() {
        cal.add(Calendar.YEAR, -1);
        return dateFormat.format(cal.getTime());
    }

    public String format(Date date) {
        return this.dateFormat.format(date);
    }

    public String format(Long time) {
        return this.dateFormat.format(new Date(time.longValue()));
    }

}
