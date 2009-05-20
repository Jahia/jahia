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
package org.jahia.engines.calendar;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;
import org.jahia.data.fields.JahiaDateFieldUtil;
import org.jahia.params.ProcessingContext;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Khue Nguyen
 */
public class CalendarHandler {

    public static final String DEFAULT_DATE_FORMAT = "dd.MM.yyyy HH:mm";
    public static final String DEFAULT_DATEONLY_FORMAT = "dd.MM.yyyy";

    private static transient final Logger logger = Logger.getLogger(CalendarHandler.class);

    private final static String TIMEZONEOFFSET = "timeZoneOffSet";
    private final static String SERVERCLIENT_TIMEDIFF = "serverClientTimeDiff";
    private TimeZone tz = TimeZone.getTimeZone("UTC");
    private String identifier = "";
    private String formName = "";
    private String dateFormat = "";
    private Long initialDate = 0L;
    private Locale locale = Locale.getDefault();
    private FastDateFormat dateFormatter;
    private Long dateLong = 0L;
    private String engineHomeURL = "";
    private Long timeZoneOffSet = 0L;

    private Long serverClientTimeDiff = 0L;

    public CalendarHandler() {
    }

    public CalendarHandler(String engineHomeURL,
                           String identifier,
                           String dateFormat,
                           Long initialDate,
                           Locale locale,
                           Long timeZoneOffSet) {
        this(engineHomeURL, identifier, null, dateFormat, initialDate, locale, timeZoneOffSet);
    }

    public CalendarHandler(String engineHomeURL,
                           String identifier,
                           String formName,
                           String dateFormat,
                           Long initialDate,
                           Locale locale,
                           Long timeZoneOffSet) {
        if (engineHomeURL != null) {
            this.engineHomeURL = engineHomeURL;
        }
        if (identifier != null) {
            this.identifier = identifier;
        }
        if (formName != null) {
            this.formName = formName;
        }
        if (dateFormat != null) {
            this.dateFormat = dateFormat;
        }
        if (initialDate != null) {
            this.initialDate = initialDate;
        }
        if (locale != null) {
            this.locale = locale;
        }
        this.dateLong = this.initialDate;

        if (timeZoneOffSet != null) {
            this.timeZoneOffSet = timeZoneOffSet;
        }
    }

    public Long update(ProcessingContext context) {
        String val = context.getParameter(this.identifier + TIMEZONEOFFSET);
        if (val != null) {
            if ("".equals(val.trim())) {
                this.timeZoneOffSet = 0L;
            } else {
                try {
                    this.timeZoneOffSet = Long.parseLong(val);
                } catch (Exception t) {
                    //
                }
            }
        }

        val = context.getParameter(this.identifier + SERVERCLIENT_TIMEDIFF);
        if (val != null) {
            if ("".equals(val.trim())) {
                serverClientTimeDiff = 0L;
            } else {
                try {
                    serverClientTimeDiff = Long.parseLong(val);
                } catch (Exception t) {
                    //
                }
            }
        }
        if (serverClientTimeDiff < 60000) {
            serverClientTimeDiff = 0L;
        }
        val = context.getParameter(this.identifier);
        if (logger.isDebugEnabled()) {
            logger.debug("this.identifier: " + this.identifier + " -> " + val);
        }
        if (val != null) {
            if ("".equals(val.trim())) {
                dateLong = 0L;
            } else {
                // update value only if there is real change
                try {
                    final Date d = getSimpleDateFormat().parse(val);
                    long valLong = d.getTime();
                    if (dateLong != (valLong - this.timeZoneOffSet)) {
                        dateLong = valLong - this.timeZoneOffSet - this.serverClientTimeDiff;
                    }
                } catch (final Exception t) {
                    logger.error("Error in CalendarHandler", t);
                }
            }
        }

        return dateLong;
    }

    public String getFormatedDate() {
        if (dateLong == 0) {
            return "";
        } else {
            Calendar cal = Calendar.getInstance(tz);
            cal.setTimeInMillis(dateLong);
            return this.getDateFormatter().format(cal.getTime());
        }
    }

    public TimeZone getTz() {
        return tz;
    }

    public void setTz(TimeZone tz) {
        this.tz = tz;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public Long getInitialDate() {
        return initialDate;
    }

    public void setInitialDate(Long initialDate) {
        this.initialDate = initialDate;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    // this is here just to support legacy templates
    public SimpleDateFormat getSimpleDateFormat() {
        if (dateFormatter == null) {
            dateFormatter = JahiaDateFieldUtil
                    .getDateFormat(dateFormat, locale);
        }
        return new SimpleDateFormat(dateFormatter.getPattern(), dateFormatter.getLocale());
    }

    public FastDateFormat getDateFormatter() {
        if (dateFormatter == null) {
            dateFormatter = JahiaDateFieldUtil
                    .getDateFormat(dateFormat, locale);
        }
        return dateFormatter;
    }

    public void setDateFormatter(FastDateFormat dateFormatter) {
        this.dateFormatter = dateFormatter;
    }

    public Long getDateLong() {
        return dateLong;
    }

    public void setDateLong(Long dateLong) {
        this.dateLong = dateLong;
    }

    public String getEngineHomeURL() {
        return engineHomeURL;
    }

    public void setEngineHomeURL(String engineHomeURL) {
        this.engineHomeURL = engineHomeURL;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public Long getTimeZoneOffSet() {
        return timeZoneOffSet;
    }

    public void setTimeZoneOffSet(Long timeZoneOffSet) {
        this.timeZoneOffSet = timeZoneOffSet;
    }

    public Long getServerClientTimeDiff() {
        return serverClientTimeDiff;
    }

    public void setServerClientTimeDiff(Long serverClientTimeDiff) {
        this.serverClientTimeDiff = serverClientTimeDiff;
    }

    /**
     * Reset initial Date value, date value, timezoneoffset and server client diff time to 0
     */
    public void reset() {
        this.initialDate = 0L;
        this.dateLong = 0L;
        this.timeZoneOffSet = 0L;
        this.serverClientTimeDiff = 0L;
    }
}


