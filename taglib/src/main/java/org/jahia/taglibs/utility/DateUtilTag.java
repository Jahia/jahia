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
