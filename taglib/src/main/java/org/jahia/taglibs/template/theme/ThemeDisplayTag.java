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

package org.jahia.taglibs.template.theme;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.jahia.data.JahiaData;
import org.jahia.operations.valves.SkeletonAggregatorValve;
import org.jahia.operations.valves.ThemeValve;
import org.jahia.params.ProcessingContext;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.theme.ThemeService;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import java.io.IOException;

/**
 * @author David Griffon
 */
public class ThemeDisplayTag extends AbstractJahiaTag {
    private static transient final Category logger = Logger.getLogger(ThemeSelectorTag.class);
    private String defaultTheme = "";
    private String jahiaThemeCurrent = "";

    public void setDefaultTheme(String defaultTheme) {
        if (defaultTheme != null && !defaultTheme.trim().equals("")) {
            this.defaultTheme = defaultTheme;
        }
    }

    public String getDefaultTheme() {
        return this.defaultTheme;
    }

    public int doStartTag() throws JspException {
        try {
            final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
            final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
            final ProcessingContext jParams = jData.getProcessingContext();
            final JahiaSite theSite = jParams.getSite();

            final String themeValveAttribute = ThemeValve.THEME_ATTRIBUTE_NAME + "_" + theSite.getID();

            // form themeSelector process           
            jahiaThemeCurrent = (String) pageContext.getRequest().getAttribute(themeValveAttribute);
            if (jahiaThemeCurrent == null) {
                jahiaThemeCurrent = defaultTheme;
                pageContext.getRequest().setAttribute(themeValveAttribute, jahiaThemeCurrent);
            }

            request.setAttribute("currentTheme", jahiaThemeCurrent);

            // out CSS call
            final StringBuffer buffer = new StringBuffer();
            buffer.append("<!-- cache:vars var=\"").append(SkeletonAggregatorValve.THEME_VARIABLE).append("\" -->");
            buffer.append(ThemeService.getInstance().getCssLinks(jParams, theSite, jahiaThemeCurrent));
            buffer.append("<!-- /cache:vars -->");
            pageContext.getOut().println(buffer.toString());
        } catch (final IOException e) {
            logger.error("IOException rendering the theme", e);
        }
        return SKIP_BODY;
    }


    public int doEndTag() {
        defaultTheme = "";
        jahiaThemeCurrent = "";
        return EVAL_PAGE;
    }
}
