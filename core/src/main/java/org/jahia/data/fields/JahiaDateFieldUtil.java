/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.data.fields;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.FastDateFormat;

/**
 * Utility class for date related operation.
 * @author hollis
 */
public class JahiaDateFieldUtil {
    /**
     * The date value container.
     *
     * @author Sergiy Shyrkov
     */
    public static class DateValues implements Serializable {
        private Object valueObject;
        private String valueString;

        /**
         * Initializes an instance of this class.
         * 
         * @param strValue
         *                the string value
         * @param objValue
         *                the object value
         */
        DateValues(String strValue, Object objValue) {
            valueString = strValue;
            valueObject = objValue;
        }

        /**
         * @return the valueObject
         */
        public Object getValueObject() {
            return valueObject;
        }

        /**
         * @return the valueString
         */
        public String getValueString() {
            return valueString;
        }

    }

    public static class CalendarPatternInfo implements Serializable {
        private String pattern;

        private boolean showTime;

        public CalendarPatternInfo(String pattern, boolean showTime) {
            super();
            this.pattern = pattern;
            this.showTime = showTime;
        }

        public String getPattern() {
            return pattern;
        }

        public boolean isShowTime() {
            return showTime;
        }

    }

    private static final String DEFAULT_PATTERN = new SimpleDateFormat()
            .toPattern();
    
    private static final String PATTERN_CHARS = "GyMdkHmsSEDFwWahKzZ";
    
    /**
     * Returns the date format for the specified locale and default time zone,
     * created using the format, parsed from the default field value, e.g. from
     * <code><jahia_calendar[dd.MM.yyyy / HH:mm]></code>.
     * 
     * @param defaultValue the default value string
     * @param defaultFormat a default format
     * @param locale the current locale
     * @return the date format for the specified locale, created using the format,
     *         parsed from the default field value
     */
    public static SimpleDateFormat getSimpleDateFormat(String defaultValue,
            String defaultFormat, Locale locale) {
        SimpleDateFormat sdf = null;
        try {
            sdf = new SimpleDateFormat(getDateFormatPattern(defaultValue, locale), locale);
        } catch (Exception t) {
        }
        if (sdf == null || StringUtils.isEmpty(sdf.toPattern())) {
            sdf = new SimpleDateFormat(defaultFormat, locale);
            if (sdf == null || StringUtils.isEmpty(sdf.toPattern())) {
                sdf = new SimpleDateFormat(DEFAULT_PATTERN, locale);
            }
        }
        return sdf;
    }    
    
    /**
     * Returns the date format for the specified locale and default time zone,
     * created using the format, parsed from the default field value, e.g. from
     * <code><jahia_calendar[dd.MM.yyyy / HH:mm]></code>.
     * 
     * @param defaultValue the default value string
     * @param locale the current locale
     * @return the date format for the specified locale, created using the format,
     *         parsed from the default field value
     */
    public static FastDateFormat getDateFormat(String defaultValue,
            Locale locale) {
        FastDateFormat fdf = null;
        try {
            fdf = FastDateFormat.getInstance(getDateFormatPattern(defaultValue,
                      locale), locale);
        } catch (Exception t) {
        }
        if (fdf == null || StringUtils.isEmpty(fdf.getPattern())) {
            fdf = FastDateFormat.getInstance(DEFAULT_PATTERN, locale);
        }
        return fdf;
    }    
    
    /**
     * Returns the default date parsed from the default date marker of the form
     * <code><jahia_calendar[dd.MM.yyyy / HH:mm]>09.05.1979 07:30</code>. The
     * following patterns are also supported:<br>
     * <code><jahia_calendar[dd.MM.yyyy / HH:mm]>now</code> - in this case the
     * current date will be returned;<br>
     * <code><jahia_calendar[dd.MM.yyyy / HH:mm]>now + 1000*60*60*24*7</code> - in this case the
     * (current date + 7 days) will be returned.
     * 
     * @param defaultValueMarker the default value marker
     * @param languageCode the current language
     * @return the default date parsed from the default date marker
     */
    public static DateValues parseDateFieldDefaultValue(
            String defaultValueMarker, String languageCode) {
        DateValues values = null;
        String format = parseDatePattern(defaultValueMarker);
        if (format != null) {
            if (defaultValueMarker.lastIndexOf(">") != -1
                    && defaultValueMarker.lastIndexOf(">") < defaultValueMarker
                            .length() - 1) {
                String defaultValueString = defaultValueMarker
                        .substring(defaultValueMarker.lastIndexOf(">") + 1);
                String defaultValueLower = defaultValueString.toLowerCase();
                // special case --> current date marker is used
                if (defaultValueLower.indexOf("now") != -1) {
                    if ("now".equals(defaultValueLower)) {
                        Date now = new Date();
                        values = new DateValues(FastDateFormat.getInstance(
                                format, new Locale(languageCode)).format(now),
                                String.valueOf(now.getTime()));
                    } else {
                        try {
                            Expression expr = ExpressionFactory
                                    .createExpression(defaultValueString);
                            JexlContext ctx = JexlHelper.createContext();
                            ctx.getVars().put("now",
                                    new Long(System.currentTimeMillis()));
                            long evaluationResult = ((Long) expr.evaluate(ctx))
                                    .longValue();

                            values = new DateValues(FastDateFormat.getInstance(
                                    format, new Locale(languageCode)).format(
                                    new Date(evaluationResult)), String
                                    .valueOf(evaluationResult));
                        } catch (Exception ex) {
                            throw new IllegalArgumentException(
                                    "Unable to parse the default date value marker '"
                                            + defaultValueString + "'. Cause: "
                                            + ex.getMessage(), ex);
                        }
                    }
                } else {
                    try {
                        Date parsedDate = new SimpleDateFormat(format)
                                .parse(defaultValueString);
                        values = new DateValues(defaultValueString, String
                                .valueOf(parsedDate.getTime()));
                    } catch (ParseException ex) {
                        throw new IllegalArgumentException(
                                "Unable to parse the default date value marker '"
                                        + defaultValueString
                                        + "' using format '" + format
                                        + "' and language '" + languageCode
                                        + "'. Cause: " + ex.getMessage(), ex);
                    }
                }
            }
        }
        return values;
    }
    
    /**
     * Returns the date format pattern parsed from the default field value, i.e.
     * from <code><jahia_calendar[dd.MM.yyyy / HH:mm]></code>.
     * 
     * @param defaultValue the default field value
     * @return the date format pattern parsed from the default field value
     */
    private static final String parseDatePattern(String defaultValue) {
        String format = null;
        if (defaultValue.toLowerCase().indexOf("jahia_calendar") != -1
                && defaultValue.indexOf("[") != -1) {
            format = defaultValue.substring(defaultValue.indexOf("[") + 1,
                    defaultValue.indexOf("]"));
        } else if ( StringUtils.isNotBlank(defaultValue) ) {
            format = defaultValue;
        }
        return format;
    }
    
    /**
     * Returns the date format for the specified locale and default time zone,
     * created using the format, parsed from the default field value, e.g. from
     * <code><jahia_calendar[dd.MM.yyyy / HH:mm]></code>.
     * 
     * @param defaultValue the default value string
     * @param locale the current locale
     * @return the date format for the specified locale, created using the format,
     *         parsed from the default field value
     */
    public static SimpleDateFormat getDateFormatForParsing(String defaultValue,
            Locale locale) {
        return getSimpleDateFormat(defaultValue, DEFAULT_PATTERN, locale);
    } 

    /**
     * Returns the date format pattern for the specified locale and default time
     * zone, created using the format, parsed from the default field value, e.g.
     * from <code><jahia_calendar[dd.MM.yyyy / HH:mm]></code>.
     * 
     * @param defaultValue
     *            the default value string
     * @param locale
     *            the current locale
     * @return the date format pattern for the specified locale, created using
     *         the format, parsed from the default field value
     */
    public static String getDateFormatPattern(String defaultValue, Locale locale) {
        String pattern = null;
        if (StringUtils.isNotEmpty(defaultValue))
            pattern = parseDatePattern(defaultValue);
        return pattern != null ? pattern : DEFAULT_PATTERN;
    } 

    /**
     * Return the JavaScript Calendar pattern for the given Java DateFormat
     * pattern.
     * <p>
     * This method is based on the one from Click Framework Copyright 2004-2008
     * Malcolm A. Edgar Licensed under the Apache License, Version 2.0
     * </p>
     * 
     * @param pattern
     *            the Java DateFormat pattern
     * @return JavaScript Calendar pattern
     */
    public static CalendarPatternInfo getJSCalendarPatternInfo(String pattern) {
        boolean showTime = false;
        StringBuffer jsPattern = new StringBuffer(20);
        int tokenStart = -1;
        int tokenEnd = -1;
        boolean debug = false;
        boolean quoted = false;

        for (int i = 0; i < pattern.length(); i++) {
            char aChar = pattern.charAt(i);
            boolean skip = false;
            if (debug) {
                System.err.print("[" + i + "," + tokenStart + "," + tokenEnd
                        + "]=" + aChar);
            }

            // If character is in SimpleDateFormat pattern character set
            if (quoted || PATTERN_CHARS.indexOf(aChar) == -1) {
                if (debug) {
                    System.err.println(" N");
                }
                if (tokenStart > -1) {
                    tokenEnd = i;
                }
                
                if (aChar == '\'') {
                    if (quoted) {
                        if (i == pattern.length() - 1) {
                            quoted = false;
                            skip = true;
                        } else if (pattern.charAt(i+1) != '\'') {
                            quoted = false;
                            skip = true;
                        } else {
                            i++;
                        }
                    } else {
                        quoted = true;
                        skip = true;
                    }
                }
            } else {
                if (debug) {
                    System.err.println(" Y");
                }
                if (tokenStart == -1) {
                    tokenStart = i;
                }
            }

            if (tokenStart > -1) {

                if (tokenEnd == -1 && i == pattern.length() - 1) {
                    tokenEnd = pattern.length();
                }

                if (tokenEnd > -1) {
                    String token = pattern.substring(tokenStart, tokenEnd);

                    if ("yyyy".equals(token)) {
                        jsPattern.append("%Y");
                    } else if ("yy".equals(token)) {
                        jsPattern.append("%y");
                    } else if ("MMMM".equals(token)) {
                        jsPattern.append("%B");
                    } else if ("MMM".equals(token)) {
                        jsPattern.append("%b");
                    } else if ("MM".equals(token)) {
                        jsPattern.append("%m");
                    } else if ("M".equals(token)) {
                        jsPattern.append("%m");
                    } else if ("dd".equals(token)) {
                        jsPattern.append("%d");
                    } else if ("d".equals(token)) {
                        jsPattern.append("%e");
                    } else if ("EEEE".equals(token)) {
                        jsPattern.append("%A");
                    } else if ("EEE".equals(token)) {
                        jsPattern.append("%a");
                    } else if ("EE".equals(token)) {
                        jsPattern.append("%a");
                    } else if ("E".equals(token)) {
                        jsPattern.append("%a");
                    } else if ("aaa".equals(token)) {
                        jsPattern.append("%p");
                    } else if ("aa".equals(token)) {
                        jsPattern.append("%p");
                    } else if ("a".equals(token)) {
                        jsPattern.append("%p");
                    } else if ("HH".equals(token)) {
                        jsPattern.append("%H");
                        showTime = true;
                    } else if ("H".equals(token)) {
                        jsPattern.append("%H");
                        showTime = true;
                    } else if ("hh".equals(token)) {
                        jsPattern.append("%I");
                        showTime = true;
                    } else if ("h".equals(token)) {
                        jsPattern.append("%l");
                        showTime = true;
                    } else if ("k".equals(token)) {
                        // there is no similar pattern --> use %k
                        jsPattern.append("%k");
                        showTime = true;
                    } else if ("kk".equals(token)) {
                        // there is no similar pattern --> use %H
                        jsPattern.append("%H");
                        showTime = true;
                    } else if ("K".equals(token)) {
                        // there is no similar pattern --> use %l
                        jsPattern.append("%l");
                        showTime = true;
                    } else if ("KK".equals(token)) {
                        // there is no similar pattern --> use %I
                        jsPattern.append("%I");
                        showTime = true;
                    } else if ("mm".equals(token)) {
                        jsPattern.append("%M");
                        showTime = true;
                    } else if ("m".equals(token)) {
                        jsPattern.append("%M");
                        showTime = true;
                    } else if ("ss".equals(token)) {
                        jsPattern.append("%S");
                        showTime = true;
                    } else if ("s".equals(token)) {
                        jsPattern.append("%S");
                        showTime = true;
                    } else {
                        if (debug) {
                            System.err.println("Not mapped:" + token);
                        }
                    }

                    if (debug) {
                        System.err.println("token[" + tokenStart + ","
                                + tokenEnd + "]='" + token + "'");
                    }
                    tokenStart = -1;
                    tokenEnd = -1;
                }
            }

            if (tokenStart == -1 && tokenEnd == -1) {
                if (!skip && (quoted || PATTERN_CHARS.indexOf(aChar) == -1)) {
                    jsPattern.append(aChar);
                }
            }
        }

        return new CalendarPatternInfo(jsPattern.toString(), showTime);
    }
}
