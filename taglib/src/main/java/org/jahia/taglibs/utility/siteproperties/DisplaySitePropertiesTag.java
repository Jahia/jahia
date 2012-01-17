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

package org.jahia.taglibs.utility.siteproperties;

import org.jahia.data.JahiaData;
import org.jahia.params.ProcessingContext;
import org.jahia.services.metadata.CoreMetadataConstant;
import org.jahia.services.sites.JahiaSite;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.utils.DateUtils;

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

    private static final transient org.slf4j.Logger logger =
            org.slf4j.LoggerFactory.getLogger(DisplaySitePropertiesTag.class);

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
            final Date creationDate = null;
            if (creationDate != null) {
                final SimpleDateFormat sdf;
                if (dateFormat == null || dateFormat.length() == 0) {
                    sdf = new SimpleDateFormat(DateUtils.DEFAULT_DATETIME_FORMAT);
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
            logger.error(e.getMessage(), e);
        }
        return SKIP_BODY;
    }
}
