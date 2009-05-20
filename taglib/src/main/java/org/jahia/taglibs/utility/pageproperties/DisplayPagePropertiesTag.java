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
package org.jahia.taglibs.utility.pageproperties;

import org.jahia.data.JahiaData;
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
import java.util.ResourceBundle;

/**
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
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
            ResourceBundle i18n = retrieveResourceBundle();

            final SimpleDateFormat sdf;
            if (dateFormat == null || dateFormat.length() == 0) {
                sdf = new SimpleDateFormat(CalendarHandler.DEFAULT_DATE_FORMAT);
            } else {
                sdf = new SimpleDateFormat(dateFormat);
            }

            buff.append("<ul>\n");
            buff.append("<li class=\"pageID\">");
            buff.append(i18n.getString("pageproperty.pageID"));
            buff.append(": ");
            buff.append(thePage.getID());
            buff.append("</li>\n");

            buff.append("<li class=\"author\">");
            buff.append(i18n.getString("pageproperty.author"));
            buff.append(": ");
            buff.append(thePage.getMetadataValue(CoreMetadataConstant.CREATOR, jParams, ""));
            buff.append("</li>\n");

            buff.append("<li class=\"creationDate\">");
            buff.append(i18n.getString("pageproperty.creationDate"));
            buff.append(": ");
            final Date creationDate = thePage.getMetadataAsDate(CoreMetadataConstant.CREATION_DATE, jParams);
            if (creationDate != null) {
                buff.append(sdf.format(creationDate));
            } else {
                buff.append("-");
            }
            buff.append("</li>\n");

            buff.append("<li class=\"lastModifier\">");
            buff.append(i18n.getString("pageproperty.lastModifier"));
            buff.append(": ");
            buff.append(thePage.getMetadataValue(CoreMetadataConstant.LAST_CONTRIBUTOR, jParams, ""));
            buff.append("</li>\n");

            buff.append("<li class=\"lastModificationDate\">");
            buff.append(i18n.getString("pageproperty.lastModificationDate"));
            buff.append(": ");
            final Date lastModifDate = thePage.getMetadataAsDate(CoreMetadataConstant.LAST_MODIFICATION_DATE, jParams);
            if (lastModifDate != null) {
                buff.append(sdf.format(lastModifDate));
            } else {
                buff.append("-");
            }
            buff.append("</li>\n");

            buff.append("<li class=\"lastPublisher\">");
            buff.append(i18n.getString("pageproperty.lastPublisher"));
            buff.append(": ");
            buff.append(thePage.getMetadataValue(CoreMetadataConstant.LAST_PUBLISHER, jParams, ""));
            buff.append("</li>\n");

            buff.append("<li class=\"lastPublicationDate\">");
            buff.append(i18n.getString("pageproperty.lastPublicationDate"));
            buff.append(": ");
            final Date lastPublishDate = thePage.getMetadataAsDate(CoreMetadataConstant.LAST_PUBLISHING_DATE, jParams);
            if (lastPublishDate != null) {
                buff.append(sdf.format(lastPublishDate));
            } else {
                buff.append("-");
            }
            buff.append("</li>\n");

            buff.append("<li class=\"urlKey\">");
            buff.append(i18n.getString("pageproperty.urlKey"));
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
