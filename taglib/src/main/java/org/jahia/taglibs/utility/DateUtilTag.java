/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
package org.jahia.taglibs.utility;

import org.slf4j.Logger;
import org.jahia.taglibs.internal.date.AbstractDateTag;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.text.ParseException;


/**
 * @author Xavier Lawrence
 *
 *
 *  This tag parses a String passed as 'currentDate' attribute and tries to convert it as a java.util.Date Object.
 *  If conversion has been successful executed, the timestamp retrieved from this Date object is stored in a scoped variable defined
 *  in the 'valueId' attribute. If not, -1 is stored.
 */
@SuppressWarnings("serial")
public class DateUtilTag extends AbstractDateTag {

    private static transient final Logger logger = org.slf4j.LoggerFactory.getLogger(DateUtilTag.class);

    private int days = Integer.MIN_VALUE;
    private int months = Integer.MIN_VALUE;
    private int years = Integer.MIN_VALUE;
    private int hours = Integer.MIN_VALUE;
    private int minutes = Integer.MIN_VALUE;
    private int seconds = Integer.MIN_VALUE;

    private String currentDate;
    private String datePattern;

    private final Calendar cal = Calendar.getInstance();

    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }

    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public void setMonths(int months) {
        this.months = months;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public void setYears(int years) {
        this.years = years;
    }

    public int doStartTag() {
        try {
            if (datePattern == null || datePattern.length() == 0) {
                datePattern = DATE_PATTERN_NO_TIME;
            }
            final SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
            try {
                final Date date = sdf.parse(currentDate);

                cal.setTime(date);

                if (days == 0) {
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                } else if (days > Integer.MIN_VALUE) {
                    cal.add(Calendar.DAY_OF_MONTH, days);
                }

                if (months == 0) {
                    cal.set(Calendar.MONTH, 1);
                } else if (months > Integer.MIN_VALUE) {
                    cal.add(Calendar.MONTH, months);
                }

                if (years != 0 && years > Integer.MIN_VALUE) {
                    cal.add(Calendar.YEAR, years);
                }

                if (hours == 0) {
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                } else if (hours > Integer.MIN_VALUE) {
                    cal.add(Calendar.HOUR_OF_DAY, hours);
                }

                if (minutes == 0) {
                    cal.set(Calendar.MINUTE, 0);
                } else if (minutes > Integer.MIN_VALUE) {
                    cal.add(Calendar.MINUTE, minutes);
                }

                if (seconds == 0) {
                    cal.set(Calendar.SECOND, 0);
                } else if (seconds > Integer.MIN_VALUE) {
                    cal.add(Calendar.SECOND, seconds);
                }

                Date valueToSet = cal.getTime();
                if (getVar() != null) {
                    pageContext.setAttribute(getVar(), valueToSet);
                }
            } catch (ParseException pe) {
                logger.debug("String passed to DateUtilTag could not be parsed as Date: " + currentDate);
                Date newDate = new Date();
                if (getVar() != null) {
                    pageContext.setAttribute(getVar(), newDate);
                }
            }

        } catch (final Exception e) {
            logger.error("Error in DateUtilTag", e);
        }

        return SKIP_BODY;
    }

    public int doEndTag() {
        resetState();
        return EVAL_PAGE;
    }

    @Override
    protected void resetState() {
        super.resetState();
        days = Integer.MIN_VALUE;
        months = Integer.MIN_VALUE;
        years = Integer.MIN_VALUE;
        hours = Integer.MIN_VALUE;
        minutes = Integer.MIN_VALUE;
        seconds = Integer.MIN_VALUE;
        currentDate = null;
        datePattern = null;
    }
}
