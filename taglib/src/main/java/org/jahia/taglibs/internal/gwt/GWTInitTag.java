/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.commons.lang.StringUtils;
import org.jahia.ajax.gwt.utils.GWTInitializer;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.utils.LanguageCodeConverters;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;

/**
 * Simple Tag that should be called in the header of the HTML page. It create a javascript object that
 * contains some jahia parameters in order to use the Google Web Toolkit.
 *
 * @author Khaled Tlili
 */
@SuppressWarnings("serial")
public class GWTInitTag extends AbstractJahiaTag {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(GWTInitTag.class);

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
            final HttpServletResponse response = (HttpServletResponse) pageContext.getResponse();
            final HttpSession session = request.getSession();

            out.print(GWTInitializer.generateInitializerStructure(request, response, session,
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
