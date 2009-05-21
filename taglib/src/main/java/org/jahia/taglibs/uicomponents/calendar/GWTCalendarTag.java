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
package org.jahia.taglibs.uicomponents.calendar;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.core.JahiaType;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.servlet.http.HttpServletRequest;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 31, 2008
 * Time: 4:08:40 PM
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class GWTCalendarTag extends AbstractJahiaTag {
    private static transient final Logger logger = Logger.getLogger(GWTCalendarTag.class);

    public static final String TENPLATE_CSS = "jahia-template-gxt";
    public static final String ADMIN_CSS = "jahia-admin-gxt";

    public static final String CALENDAR_TYPE = "Calendar";

    protected String callback="";
    protected String activeDate="";
    protected boolean templateUsage = true;

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public void setTemplateUsage(boolean templateUsage) {
        this.templateUsage = templateUsage;
    }

    public void setActiveDate(String activeDate) {
        this.activeDate = activeDate;
    }

    public int doStartTag() {
        final StringBuffer buf = new StringBuffer();
        final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();

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
            buf.append(JahiaType.CALENDAR);
            buf.append("\" ");
            buf.append("id=\"");
            buf.append(new Random().nextInt() + System.currentTimeMillis());
            buf.append("\" ");

        } else {
            buf.append("id=\"");
            buf.append(CALENDAR_TYPE);
            Integer counter = (Integer) request.getAttribute(CALENDAR_TYPE);
            if (counter == null) {
                counter = 0;
            } else {
                counter++;
            }
            request.setAttribute(CALENDAR_TYPE, counter);
            buf.append(counter);
            buf.append("\" ");
        }


        buf.append("callback=\"");
        buf.append(callback);
        buf.append("\" ");
        buf.append("activedate=\"");
        buf.append(activeDate);
        buf.append("\" ");
        buf.append("/>");
        try {
            pageContext.getOut().print(buf.toString());
        } catch (Exception e) {
            logger.error("Error in GWTDateFieldTag", e);
        }
        return SKIP_BODY;
    }

    public int doEndTag() {
        cssClassName = null;
        templateUsage = true;
        callback = "";
        activeDate = "";
        return EVAL_PAGE;
    }
}
