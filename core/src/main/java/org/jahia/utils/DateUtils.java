/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.utils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;

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
     * Returns a human-readable representation of the time taken.
     * 
     * @param durationMillis
     *            the time take in milliseconds
     * @return a human-readable representation of the time taken
     * @see DurationFormatUtils#formatDurationWords(long, boolean, boolean)
     */
    public static String formatDurationWords(long durationMillis) {
        if (durationMillis <= 1000) {
            return durationMillis + " ms";
        } else {
            return DurationFormatUtils.formatDurationWords(durationMillis, true, true) + " (" + durationMillis + " ms)";
        }
    }

    /**
     * Initializes an instance of this class.
     */
    private DateUtils() {
        super();
    }
}
