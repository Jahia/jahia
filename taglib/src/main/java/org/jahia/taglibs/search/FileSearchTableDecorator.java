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

package org.jahia.taglibs.search;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.displaytag.decorator.TableDecorator;
import org.jahia.engines.search.FileHit;
import org.jahia.engines.calendar.CalendarHandler;

/**
 * Base decorator class for displaying a list of file search results.
 * 
 * @author Sergiy Shyrkov
 */
public class FileSearchTableDecorator extends TableDecorator {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat(
            CalendarHandler.DEFAULT_DATE_FORMAT);

    protected String formatDate(Date date) {
        return date != null ? DATE_FORMAT.format(date) : null;
    }

    public String getCreationDate() {
        return formatDate(getHit().getCreated());
    }

    protected FileHit getHit() {
        return (FileHit) getCurrentRowObject();
    }

    public String getIcon() {
        String iconType = getHit().getIconType();
        return new StringBuffer(64).append("<img src=\"").append(
                getRequest().getContextPath()).append(
                "/jsp/jahia/engines/images/types/").append(iconType).append(
                ".gif").append("\" alt=\"").append(iconType).append(
                "\" width=\"16\" height=\"16\" border=\"0\" title=\"").append(
                getHit().getContentType()).append("\"/>").toString();
    }

    public String getLastModified() {
        return formatDate(getHit().getLastModified());
    }

    protected HttpServletRequest getRequest() {
        return (HttpServletRequest) getPageContext().getRequest();
    }

}
