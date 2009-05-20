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
package org.jahia.taglibs.search;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.time.FastDateFormat;
import org.displaytag.decorator.TableDecorator;
import org.jahia.engines.search.FileHit;
import org.jahia.engines.calendar.CalendarHandler;

/**
 * Base decorator class for displaying a list of file search results.
 * 
 * @author Sergiy Shyrkov
 */
public class FileSearchTableDecorator extends TableDecorator {

    private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance(
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
                "/engines/images/types/").append(iconType).append(
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
