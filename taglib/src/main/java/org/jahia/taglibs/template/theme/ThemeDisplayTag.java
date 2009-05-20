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
@SuppressWarnings("serial")
public class ThemeDisplayTag extends AbstractJahiaTag {
    private static transient final Category logger = Logger.getLogger(ThemeSelectorTag.class);
    private String defaultTheme = "default";
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
