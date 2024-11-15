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
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import java.util.Map;

/**
 * Helper class for generating script element with the GWT module.
 *
 * @author Romain Felden
 */
public class GWTIncluder {

    public static final String GWT_MODULE_PATH = "/gwt";

    /**
     * Generate the import string for a given module.
     *
     * @param pageContext the page context to format the path
     * @param module      the fully qualified module name
     * @return the string to write to html
     */
    public static String generateGWTImport(PageContext pageContext, String module) {
        final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        final HttpServletResponse response = (HttpServletResponse) pageContext.getResponse();
        return generateGWTImport(request, response, module);
    }

    public static String generateGWTImport(HttpServletRequest request, HttpServletResponse response, String module ) {
        StringBuilder ret = new StringBuilder();
        final String gwtModulePath = response.encodeURL(new StringBuilder(64).append(request.getContextPath()).append(GWT_MODULE_PATH + "/")
                .append(module).append("/").append(module)
                .append(".nocache.js").toString());
        return ret.append("<script id='jahia-gwt' type='text/javascript' src='").append(gwtModulePath).append("'></script>\n").toString();
    }

    /**
     * Get place holder for a jahiaModule  .
     * Example: <div jahiaType="categoriesPiker" start="/root" id="cat_2"/>
     * @param templateUsage true means that the module is used in a template
     * @param cssClassName  the css class name
     * @param jahiaType   the jahiaType
     * @param id  the id
     * @param extraParams map of extra parameter. Example {("start","/root"}
     * @return place holder for a jahiaModule
     */
    public static String generateJahiaModulePlaceHolder(boolean templateUsage, String cssClassName, String jahiaType, String id, Map<String, Object> extraParams) {
        // css depending on type of module
        StringBuilder css = new StringBuilder();
        if (templateUsage) {
            css.append("jahia-template-gxt");
        } else {
            css.append("jahia-admin-gxt");
        }
        if (jahiaType != null) {
            css.append(" ").append(jahiaType).append("-gxt");
        }
        if (cssClassName != null) {
            css.append(" ").append(cssClassName);
        }

        final StringBuilder outBuf = new StringBuilder("<div class=\"").append(css).append("\" id=\"").append(id).append("\" ");
        if (jahiaType != null) {
            outBuf.append("jahiatype=\"");
            outBuf.append(jahiaType);
            outBuf.append("\"");
            outBuf.append(" ");
            outBuf.append(getParam(extraParams));
            outBuf.append("></div>\n");
        } else {
            outBuf.append(extraParams);
            outBuf.append("></div>\n");
        }
        return outBuf.toString();
    }

    protected static String getParam(Map<String, Object> extraParams) {
        final StringBuilder outBuf = new StringBuilder();
        for (String name : extraParams.keySet()) {
            Object value = extraParams.get(name);
            if (value == null) {
                value = "";
            }
            outBuf.append(name).append("=\"").append(value).append("\" ");
        }
        return outBuf.toString();
    }


}
