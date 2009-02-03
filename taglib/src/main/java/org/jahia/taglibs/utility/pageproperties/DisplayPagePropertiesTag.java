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

package org.jahia.taglibs.utility.pageproperties;

import org.jahia.data.JahiaData;
import org.jahia.data.beans.I18nBean;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.metadata.CoreMetadataConstant;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.PageProperty;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.engines.calendar.CalendarHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Xavier Lawrence
 */
public class DisplayPagePropertiesTag extends AbstractJahiaTag {

    private static final transient org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(DisplayPagePropertiesTag.class);

    public String dateFormat;

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public int doStartTag() {
        try {
            final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
            final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
            final ProcessingContext jParams = jData.getProcessingContext();
            final ContentPage thePage = jParams.getContentPage();
            final StringBuilder buff = new StringBuilder(256);
            I18nBean i18n = getI18n();

            final SimpleDateFormat sdf;
            if (dateFormat == null || dateFormat.length() == 0) {
                sdf = new SimpleDateFormat(CalendarHandler.DEFAULT_DATE_FORMAT);
            } else {
                sdf = new SimpleDateFormat(dateFormat);
            }

            buff.append("<ul>\n");
            buff.append("<li class=\"pageID\">");
            buff.append(i18n.get("pageproperty.pageID"));
            buff.append(": ");
            buff.append(thePage.getID());
            buff.append("</li>\n");

            buff.append("<li class=\"author\">");
            buff.append(i18n.get("pageproperty.author"));
            buff.append(": ");
            buff.append(thePage.getMetadataValue(CoreMetadataConstant.CREATOR, jParams, ""));
            buff.append("</li>\n");

            buff.append("<li class=\"creationDate\">");
            buff.append(i18n.get("pageproperty.creationDate"));
            buff.append(": ");
            final Date creationDate = thePage.getMetadataAsDate(CoreMetadataConstant.CREATION_DATE, jParams);
            if (creationDate != null) {
                buff.append(sdf.format(creationDate));
            } else {
                buff.append("-");
            }
            buff.append("</li>\n");

            buff.append("<li class=\"lastModifier\">");
            buff.append(i18n.get("pageproperty.lastModifier"));
            buff.append(": ");
            buff.append(thePage.getMetadataValue(CoreMetadataConstant.LAST_CONTRIBUTOR, jParams, ""));
            buff.append("</li>\n");

            buff.append("<li class=\"lastModificationDate\">");
            buff.append(i18n.get("pageproperty.lastModificationDate"));
            buff.append(": ");
            final Date lastModifDate = thePage.getMetadataAsDate(CoreMetadataConstant.LAST_MODIFICATION_DATE, jParams);
            if (lastModifDate != null) {
                buff.append(sdf.format(lastModifDate));
            } else {
                buff.append("-");
            }
            buff.append("</li>\n");

            buff.append("<li class=\"lastPublisher\">");
            buff.append(i18n.get("pageproperty.lastPublisher"));
            buff.append(": ");
            buff.append(thePage.getMetadataValue(CoreMetadataConstant.LAST_PUBLISHER, jParams, ""));
            buff.append("</li>\n");

            buff.append("<li class=\"lastPublicationDate\">");
            buff.append(i18n.get("pageproperty.lastPublicationDate"));
            buff.append(": ");
            final Date lastPublishDate = thePage.getMetadataAsDate(CoreMetadataConstant.LAST_PUBLISHING_DATE, jParams);
            if (lastPublishDate != null) {
                buff.append(sdf.format(lastPublishDate));
            } else {
                buff.append("-");
            }
            buff.append("</li>\n");

            buff.append("<li class=\"urlKey\">");
            buff.append(i18n.get("pageproperty.urlKey"));
            buff.append(": ");
            buff.append(getURLKey(thePage));
            buff.append("</li>\n");
            buff.append("</ul>\n");

            final JspWriter out = pageContext.getOut();
            out.print(buff.toString());

        } catch (final Exception e) {
            logger.error(e, e);
        }
        return SKIP_BODY;
    }

    public int doEndTag() {
        dateFormat = null;
        return EVAL_PAGE;
    }

    protected String getURLKey(final ContentPage page) throws JahiaException {
        final PageProperty prop = page.getPageLocalProperty(PageProperty.PAGE_URL_KEY_PROPNAME);
        if (prop == null) {
            return "-";
        }
        return prop.getValue();
    }
}
