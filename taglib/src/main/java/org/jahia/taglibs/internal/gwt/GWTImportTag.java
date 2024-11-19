/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.taglibs.internal.gwt;

import static org.jahia.api.Constants.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.jahia.utils.WebUtils;
import org.slf4j.Logger;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

/**
 * Generates a script element for loading the GWT module.
 *
 * @author Khaled Tlili
 */
@SuppressWarnings("serial")
public class GWTImportTag extends AbstractJahiaTag {

    private static final transient Logger logger = org.slf4j.LoggerFactory.getLogger(GWTInitTag.class);

    private static final List<String> AVAILABLE_ANTHRACITE_LOCALE = Arrays.asList("en", "fr", "de");
    private static final String ANTHRACITE_LOCALE_FALLBACK = "en";

    private String module;

    @Override
    public int doStartTag() {
        try {
            pageContext.getRequest().setAttribute("jahia.engines.gwtModuleIncluded", Boolean.TRUE);
            pageContext.getOut().println(GWTIncluder.generateGWTImport(pageContext, getModule()));

            final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
            String theme = WebUtils.getUITheme(request);

            if (theme != null && !theme.equals("default")) {
                request.getSession().setAttribute(UI_THEME, theme);
                Locale uiLocale = getUILocale();
                String base = "/engines/" + theme + "/css/";
                if (pageContext.getServletContext().getResource(base + module + "_" + uiLocale.getLanguage() + ".css") != null) {
                    pageContext.setAttribute("themeLocale", "_" + uiLocale.getLanguage());
                } else if (pageContext.getServletContext().getResource(base + module + "_en.css") != null) {
                    pageContext.setAttribute("themeLocale", "_en");
                } else {
                    pageContext.setAttribute("themeLocale", "");
                }
                pageContext.setAttribute("theme", theme);
                pageContext.setAttribute("anthraciteUiLocale", resolveAnthraciteUiLocale(uiLocale));
            } else {
                request.getSession().setAttribute(UI_THEME, null);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return SKIP_BODY;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    @Override
    public int doEndTag() throws JspException {
        super.doEndTag();
        module = null;
        return EVAL_PAGE;
    }

    private String resolveAnthraciteUiLocale(Locale uiLocale) {
        if (AVAILABLE_ANTHRACITE_LOCALE.contains(uiLocale.getLanguage())) {
            return uiLocale.getLanguage();
        }

        return ANTHRACITE_LOCALE_FALLBACK;
    }
}
