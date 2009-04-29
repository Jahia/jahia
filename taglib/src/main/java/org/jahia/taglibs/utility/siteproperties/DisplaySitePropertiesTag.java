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
package org.jahia.taglibs.utility.siteproperties;

import org.jahia.data.JahiaData;
import org.jahia.params.ProcessingContext;
import org.jahia.services.metadata.CoreMetadataConstant;
import org.jahia.services.sites.JahiaSite;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.engines.calendar.CalendarHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
public class DisplaySitePropertiesTag extends AbstractJahiaTag {

    public String dateFormat;

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    private static final transient org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(DisplaySitePropertiesTag.class);

    public int doStartTag() {
        try {
            final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
            final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
            final ProcessingContext jParams = jData.getProcessingContext();
            final JahiaSite theSite = jParams.getSite();
            final StringBuilder buff = new StringBuilder();
            ResourceBundle i18n = retrieveResourceBundle();

            buff.append("<ul>\n");
            buff.append("<li class=\"siteID\">");
            buff.append(i18n.getString("siteproperty.siteID"));
            buff.append(": ");
            buff.append(theSite.getID());
            buff.append("</li>\n");

            buff.append("<li class=\"siteKey\">");
            buff.append(i18n.getString("siteproperty.siteKey"));
            buff.append(": ");
            buff.append(theSite.getSiteKey());
            buff.append("</li>\n");

            buff.append("<li class=\"sitename\">");
            buff.append(i18n.getString("siteproperty.sitename"));
            buff.append(": ");
            buff.append(theSite.getTitle());
            buff.append("</li>\n");

            buff.append("<li class=\"servername\">");
            buff.append(i18n.getString("siteproperty.servername"));
            buff.append(": ");
            buff.append(theSite.getServerName());
            buff.append("</li>\n");

            buff.append("<li class=\"templates\">");
            buff.append(i18n.getString("siteproperty.templates"));
            buff.append(": ");
            buff.append(theSite.getTemplatePackageName());
            buff.append("</li>\n");

            buff.append("<li class=\"creationDate\">");
            buff.append(i18n.getString("siteproperty.creationDate"));
            buff.append(": ");
            final Date creationDate = theSite.getHomeContentPage().
                    getMetadataAsDate(CoreMetadataConstant.CREATION_DATE, jParams);
            if (creationDate != null) {
                final SimpleDateFormat sdf;
                if (dateFormat == null || dateFormat.length() == 0) {
                    sdf = new SimpleDateFormat(CalendarHandler.DEFAULT_DATE_FORMAT);
                } else {
                    sdf = new SimpleDateFormat(dateFormat);
                }
                buff.append(sdf.format(creationDate));
            } else {
                buff.append("-");
            }
            buff.append("</li>\n");
            buff.append("</ul>\n");

            final JspWriter out = pageContext.getOut();
            out.print(buff.toString());

        } catch (final Exception e) {
            logger.error(e, e);
        }
        return SKIP_BODY;
    }
}
