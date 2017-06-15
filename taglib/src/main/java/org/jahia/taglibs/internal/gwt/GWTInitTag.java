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

import org.jahia.api.Constants;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.LanguageCodeConverters;
import org.slf4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.ajax.gwt.utils.GWTInitializer;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.Locale;

/**
 * Simple Tag that should be called in the header of the HTML page. It create a javascript object that
 * contains some jahia parameters in order to use the Google Web Toolkit.
 *
 * @author Khaled Tlili
 */
@SuppressWarnings("serial")
public class GWTInitTag extends AbstractJahiaTag {

    private static final transient Logger logger = org.slf4j.LoggerFactory.getLogger(GWTInitTag.class);
    
    private String locale;
    private String uilocale;

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void setUilocale(String uilocale) {
        this.uilocale = uilocale;
    }

    /**
     * Create a javascript object that
     * contains some jahia parameters  in order to use the Google Web Toolkit.<br>
     * Usage example inside a JSP:<br>
     * <!--
     * <%@ page language="java" contentType="text/html;charset=UTF-8" %>
     * <html>
     * <head>
     * <ajax:initGWT/>
     * </head>
     * -->
     * ...
     *
     * @return SKIP_BODY
     */
    public int doStartTag() {
        try {
            final JspWriter out = pageContext.getOut();
            final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
            final HttpSession session = request.getSession();

            out.print(GWTInitializer.generateInitializerStructure(request, session,
                    StringUtils.isEmpty(locale) ? null : LanguageCodeConverters.languageCodeToLocale(locale),
                    StringUtils.isEmpty(uilocale) ? null : LanguageCodeConverters.languageCodeToLocale(uilocale))) ;

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return SKIP_BODY;
    }

    @Override
    public int doEndTag() throws JspException {
        locale = null;
        uilocale = null;
        return super.doEndTag();
    }
}
