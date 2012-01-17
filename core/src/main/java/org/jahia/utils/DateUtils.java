/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.utils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * Convenient date utility methods.
 * 
 * @author Sergiy Shyrkov
 */
public final class DateUtils {

    private static final Map<String, Integer> DAYS;
    /** Default date pattern */
    public static final String DEFAULT_DATE_FORMAT = "dd.MM.yyyy";

    /** Default datetime pattern */
    public static final String DEFAULT_DATETIME_FORMAT = "dd.MM.yyyy HH:mm";

    static {
        DAYS = new HashMap<String, Integer>(7);
        DAYS.put("monday", Calendar.MONDAY);
        DAYS.put("tuesday", Calendar.TUESDAY);
        DAYS.put("wednesday", Calendar.WEDNESDAY);
        DAYS.put("thursday", Calendar.THURSDAY);
        DAYS.put("friday", Calendar.FRIDAY);
        DAYS.put("saturday", Calendar.SATURDAY);
        DAYS.put("sunday", Calendar.SUNDAY);
        DAYS.put("mon", Calendar.MONDAY);
        DAYS.put("tue", Calendar.TUESDAY);
        DAYS.put("wed", Calendar.WEDNESDAY);
        DAYS.put("thu", Calendar.THURSDAY);
        DAYS.put("fri", Calendar.FRIDAY);
        DAYS.put("sat", Calendar.SATURDAY);
        DAYS.put("sun", Calendar.SUNDAY);
    }

    public static String convertDayOfWeekToCron(Object dayOfWeek) {
        System.out.println("object" + dayOfWeek);
        return "test";
    }

    /**
     * Returns the end of the day (23:59:59:999) for today.
     * 
     * @param date the date to be processed
     * @return the end of the day (23:59:59:999) for today
     */
    public static Calendar dayEnd() {
        return dayEnd(Calendar.getInstance());
    }

    /**
     * Returns the end of the day (23:59:59:999) for the specified date.
     * 
     * @param date the date to be processed
     * @return the end of the day (23:59:59:999) for the specified date
     */
    public static Calendar dayEnd(Calendar date) {
        Calendar c = (Calendar) date.clone();

        c.set(Calendar.HOUR_OF_DAY, c.getMaximum(Calendar.HOUR_OF_DAY));
        c.set(Calendar.MINUTE, c.getMaximum(Calendar.MINUTE));
        c.set(Calendar.SECOND, c.getMaximum(Calendar.SECOND));
        c.set(Calendar.MILLISECOND, c.getMaximum(Calendar.MILLISECOND));

        return c;
    }

    /**
     * Returns the start of the day (00:00:00:000) for today.
     * 
     * @return the start of the day (00:00:00:000) for today
     */
    public static Calendar dayStart() {
        return dayStart(Calendar.getInstance());
    }
    
    /**
     * Returns the start of the day (00:00:00:000) for the specified date.
     * 
     * @param date the date to be processed
     * @return the start of the day (00:00:00:000) for the specified date
     */
    public static Calendar dayStart(Calendar date) {
        Calendar c = (Calendar) date.clone();

        c.set(Calendar.HOUR_OF_DAY, c.getMinimum(Calendar.HOUR_OF_DAY));
        c.set(Calendar.MINUTE, c.getMinimum(Calendar.MINUTE));
        c.set(Calendar.SECOND, c.getMinimum(Calendar.SECOND));
        c.set(Calendar.MILLISECOND, c.getMinimum(Calendar.MILLISECOND));

        return c;
    }

    /**
     * Returns the {@link Calendar}'s day of week number, which corresponds to the provided day of week string. If the name is unknown,
     * <code>null</code> is returned.
     * 
     * @param dayOfWeek
     *            the day of week name
     * @return the {@link Calendar}'s day of week number, which corresponds to the provided day of week string. If the name is unknown,
     *         <code>null</code> is returned
     */
    public static Integer getDayOfWeek(String dayOfWeek) {
        return dayOfWeek != null ? DAYS.get(dayOfWeek.toLowerCase()) : null;
    }

    /**
     * Returns the the end of the day cron expression (Quartz scheduler) for the specified days of week.
     * 
     * @param daysOfWeek
     *            the day of week list
     * @return the the end of the day cron expression (Quartz scheduler) for the specified days of week
     */
    public static String getDayOfWeekEndCron(List<String> daysOfWeek) {
        return "59 59 23 ? * "
                + (daysOfWeek != null && !daysOfWeek.isEmpty() ? StringUtils.join(daysOfWeek, ',')
                        : "*");
    }

    /**
     * Returns the the start of the day cron expression (Quartz scheduler) for the specified days of week.
     * 
     * @param daysOfWeek
     *            the day of week list
     * @return the the start of the day cron expression (Quartz scheduler) for the specified days of week
     */
    public static String getDayOfWeekStartCron(List<String> daysOfWeek) {
        return "1 0 0 ? * "
                + (daysOfWeek != null && !daysOfWeek.isEmpty() ? StringUtils.join(daysOfWeek, ',')
                        : "*");
    }

    /**
     * Initializes an instance of this class.
     */
    private DateUtils() {
        super();
    }
}
