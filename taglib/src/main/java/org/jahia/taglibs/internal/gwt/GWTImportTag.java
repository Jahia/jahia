/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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

import java.io.IOException;
import java.util.Locale;

import org.jahia.api.Constants;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.jcr.RepositoryException;
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

    private String module;

    public int doStartTag() {
        try {
            pageContext.getRequest().setAttribute("jahia.engines.gwtModuleIncluded", Boolean.TRUE);
            pageContext.getOut().println(GWTIncluder.generateGWTImport(pageContext, getModule()));

            final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
            String theme = SettingsBean.getInstance().getPropertiesFile().getProperty("jahia.ui.theme");
            JahiaUser jahiaUser = (JahiaUser) request.getSession().getAttribute(Constants.SESSION_USER);

            if (request.getParameter("jahia.ui.theme") != null) {
                theme = request.getParameter("jahia.ui.theme");
                String userValue = jahiaUser.getProperty("jahia.ui.theme");
                if (userValue == null || !userValue.equals(theme)) {
                    try {
                        JCRSessionWrapper userSession = JCRSessionFactory.getInstance().getCurrentUserSession();
                        JCRUserNode user = (JCRUserNode) userSession.getNode(jahiaUser.getLocalPath());
                        user.setProperty("jahia.ui.theme", theme);
                        userSession.save();
                    } catch (RepositoryException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            } else if (jahiaUser != null && jahiaUser.getProperty("jahia.ui.theme") != null) {
                theme = jahiaUser.getProperty("jahia.ui.theme");
            }

            if (theme != null && !theme.equals("default")) {
                Locale uiLocale = getUILocale();
                String base = "/engines/" + theme + "/";
                if (pageContext.getServletContext().getResource(base + module + "_" + uiLocale.getLanguage() + ".css") != null) {
                    pageContext.setAttribute("themeLocale", "_" + uiLocale.toLanguageTag());
                } else if (pageContext.getServletContext().getResource(base + module + "_en.css") != null){
                    pageContext.setAttribute("themeLocale", "_en");
                } else {
                    pageContext.setAttribute("themeLocale", "");
                }
                pageContext.setAttribute("theme", theme);
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
}
