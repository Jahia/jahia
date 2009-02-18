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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.taglibs.uicomponents.dateselector;

import org.apache.log4j.Logger;
import org.jahia.data.JahiaData;
import org.jahia.ajax.gwt.client.core.JahiaType;
import org.jahia.taglibs.internal.date.AbstractDateTag;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * @author Xavier Lawrence
 */
public class GWTDateTimePickerTag extends AbstractDateTag {
    private static transient final Logger logger = Logger.getLogger(GWTDateTimePickerTag.class);

    public static final String TENPLATE_CSS = "jahia-template-gxt";
    public static final String ADMIN_CSS = "jahia-admin-gxt";

    public static final String DATE_FIELD_TYPE = "DateField";

    protected boolean displayTime = false;
    protected String fieldName;
    protected String value;
    protected String datePattern;
    protected boolean readOnly = false;
    protected boolean shadow = false;
    protected boolean templateUsage = true;

    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }

    public void setDisplayTime(boolean displayTime) {
        this.displayTime = displayTime;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public void setShadow(boolean shadow) {
        this.shadow = shadow;
    }

    public void setTemplateUsage(boolean templateUsage) {
        this.templateUsage = templateUsage;
    }

    public int doStartTag() {
        final StringBuffer buf = new StringBuffer();
        final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        if (datePattern == null || datePattern.length() == 0) {
            if (displayTime) {
                datePattern = DATE_PATTERNE_TIME;
            } else {
                datePattern = DATE_PATTERNE_NO_TIME;
            }
        }
        /*if (jData.gui().isLogged()) {*/
            buf.append("<div ");
            buf.append("class=\"");
            if (cssClassName != null && cssClassName.length() > 0) {
                buf.append(templateUsage ? TENPLATE_CSS : ADMIN_CSS).append(" ").append(cssClassName);
            } else {
                buf.append(templateUsage ? TENPLATE_CSS : ADMIN_CSS);
            }
            buf.append("\" ");

            if (templateUsage) {
                buf.append(JahiaType.JAHIA_TYPE);
                buf.append("=\"");
                buf.append(JahiaType.DATE_FIELD);
                buf.append("\" ");
                buf.append("id=\"");
                buf.append(new Random().nextInt() + System.currentTimeMillis());
                buf.append("\" ");

            } else {
                buf.append("id=\"");
                buf.append(DATE_FIELD_TYPE);
                Integer counter = (Integer) request.getAttribute(DATE_FIELD_TYPE);
                if (counter == null) {
                    counter = 0;
                } else {
                    counter++;
                }
                request.setAttribute(DATE_FIELD_TYPE, counter);
                buf.append(counter);
                buf.append("\" ");
            }

            buf.append("datepattern=\"");
            buf.append(datePattern);
            buf.append("\" ");

            buf.append("displaytime=\"");
            buf.append(displayTime);
            buf.append("\" ");

            buf.append("readonly=\"");
            buf.append(readOnly);
            buf.append("\" ");

            buf.append("shadow=\"");
            buf.append(shadow);
            buf.append("\" ");

            if (value != null && value.length() > 0) {
                buf.append("value=\"");
                try {
                    buf.append(new SimpleDateFormat(datePattern, jData.getProcessingContext().getLocale()).parse(value).getTime());

                } catch (Exception e) {
                    buf.append(new Date().getTime());
                    logger.debug("Error in parsing date in GWTDateFieldTag", e);
                }
                buf.append("\" ");
            }

            buf.append("fieldname=\"");
            buf.append(fieldName);
            buf.append("\"></div>\n");

        /*} else {
            if (cssClassName != null && cssClassName.length() > 0) {
                buf.append("<div class=\"");
                buf.append(cssClassName);
                buf.append("\">");
            }
            buf.append("<input type=\"text\" name=\"");
            buf.append(fieldName);
            buf.append("\" value=\"");
            final SimpleDateFormat sdf = new SimpleDateFormat(datePattern);

            buf.append(sdf.format(new Date()));
            buf.append("\" />");
            buf.append("</div>\n");
        }*/

        try {
            pageContext.getOut().print(buf.toString());
        } catch (Exception e) {
            logger.error("Error in GWTDateFieldTag", e);
        }
        return SKIP_BODY;
    }

    public int doEndTag() {
        cssClassName = null;
        datePattern = null;
        displayTime = false;
        shadow = false;
        readOnly = false;
        templateUsage = true;
        fieldName = null;
        value = null;
        return EVAL_PAGE;
    }
}
