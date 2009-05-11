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
package org.jahia.taglibs.utility;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
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

    private static transient final Logger logger = Logger.getLogger(DateCalcTag.class);

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
            } else if (months == Utils.TO_MIN) {
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
