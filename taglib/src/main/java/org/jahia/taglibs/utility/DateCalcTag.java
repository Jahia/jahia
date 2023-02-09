/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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

import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.jahia.taglibs.internal.date.AbstractDateTag;

/**
 * @author Xavier Lawrence
 *
 *
 *         This tag can be used to add/subtract to or from a java.util.Date
 *         variable or to set fields of the date to its minimum or maximum
 *         values. The date is passed in the value attribute and must already be
 *         converted to java.util.Date. Use <fmt:parseDate> to convert Strings
 *         to java.util.Date. The new Date is exposed in a variable named with
 *         var.
 */
@SuppressWarnings("serial")
public class DateCalcTag extends AbstractDateTag {

    private static transient final Logger logger = org.slf4j.LoggerFactory.getLogger(DateCalcTag.class);

    private int years = 0;

    private int months = 0;

    private int days = 0;

    private int hours = 0;

    private int minutes = 0;

    private int seconds = 0;

    private int milliseconds = 0;

    private Date value;

    private final Calendar cal = Calendar.getInstance();

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
            final Date date = getValue();

            cal.setTime(date);

            if (years != 0 && years > Utils.TO_MIN && years < Utils.TO_MAX) {
                cal.add(Calendar.YEAR, years);
            }
            if (months != 0 && months > Utils.TO_MIN && months < Utils.TO_MAX) {
                cal.add(Calendar.MONTH, months);
            }
            if (days != 0 && days > Utils.TO_MIN && days < Utils.TO_MAX) {
                cal.add(Calendar.DAY_OF_MONTH, days);
            }
            if (hours != 0 && hours > Utils.TO_MIN && hours < Utils.TO_MAX) {
                cal.add(Calendar.HOUR_OF_DAY, hours);
            }
            if (minutes != 0 && minutes > Utils.TO_MIN && minutes < Utils.TO_MAX) {
                cal.add(Calendar.MINUTE, minutes);
            }
            if (seconds != 0 && seconds > Utils.TO_MIN && seconds < Utils.TO_MAX) {
                cal.add(Calendar.SECOND, seconds);
            }
            if (milliseconds != 0 && milliseconds > Utils.TO_MIN && milliseconds < Utils.TO_MAX) {
                cal.add(Calendar.MILLISECOND, milliseconds);
            }

            if (months == Utils.TO_MIN) {
                cal.set(Calendar.MONTH, cal.getActualMinimum(Calendar.MONTH));
            } else if (months == Utils.TO_MAX) {
                cal.set(Calendar.MONTH, cal.getActualMaximum(Calendar.MONTH));
            }
            if (days == Utils.TO_MIN) {
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
            } else if (days == Utils.TO_MAX) {
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            }
            if (hours == Utils.TO_MIN) {
                cal.set(Calendar.HOUR_OF_DAY, cal.getActualMinimum(Calendar.HOUR_OF_DAY));
            } else if (hours == Utils.TO_MAX) {
                cal.set(Calendar.HOUR_OF_DAY, cal.getActualMaximum(Calendar.HOUR_OF_DAY));
            }
            if (minutes == Utils.TO_MIN) {
                cal.set(Calendar.MINUTE, cal.getActualMinimum(Calendar.MINUTE));
            } else if (minutes == Utils.TO_MAX) {
                cal.set(Calendar.MINUTE, cal.getActualMaximum(Calendar.MINUTE));
            }
            if (seconds == Utils.TO_MIN) {
                cal.set(Calendar.SECOND, cal.getActualMinimum(Calendar.SECOND));
            } else if (seconds == Utils.TO_MAX) {
                cal.set(Calendar.SECOND, cal.getActualMaximum(Calendar.SECOND));
            }
            if (milliseconds == Utils.TO_MIN) {
                cal.set(Calendar.MILLISECOND, cal.getActualMinimum(Calendar.MILLISECOND));
            } else if (milliseconds == Utils.TO_MAX) {
                cal.set(Calendar.MILLISECOND, cal.getActualMaximum(Calendar.MILLISECOND));
            }

            pageContext.setAttribute(getVar(), cal.getTime());

        } catch (final Exception e) {
            logger.error("Error in DateUtilTag", e);
        }

        return SKIP_BODY;
    }

    public int doEndTag() {
        resetState();
        return EVAL_PAGE;
    }

    public void setValue(Date value) {
        this.value = value;
    }

    public Date getValue() {
        return value;
    }

    public void setMilliseconds(int milliseconds) {
        this.milliseconds = milliseconds;
    }

    public int getMilliseconds() {
        return milliseconds;
    }

    @Override
    protected void resetState() {
        super.resetState();
        setDays(0);
        setMonths(0);
        setYears(0);
        setHours(0);
        setMinutes(0);
        setSeconds(0);
        setMilliseconds(0);
        setValue(null);
        setVar(null);
    }
}
