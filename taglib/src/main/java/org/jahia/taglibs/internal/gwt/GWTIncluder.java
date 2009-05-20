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
package org.jahia.taglibs.internal.gwt;

import org.jahia.ajax.gwt.client.core.JahiaType;

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
        StringBuilder ret = new StringBuilder();
        final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        final HttpServletResponse response = (HttpServletResponse) pageContext.getResponse();
        final String gwtModulePath = response.encodeURL(new StringBuilder(64).append(request.getContextPath()).append(GWT_MODULE_PATH + "/")
                .append(module).append("/").append(module)
                .append(".nocache.js").toString());
        ret.append("<script id='jahia-gwt' type='text/javascript' src='").append(gwtModulePath).append("'></script>\n");
        return ret.toString();
    }

     /**
     * Get place holder for a jahiaModule  .
     * Example: <div jahiaType="categoriesPiker" start="/root" id="cat_2"/>
     * @param templateUsage true means that the module is used in a template
     * @param cssClassName  the css class name
     * @param jahiaType   the jahiaType
     * @param id  the id
     * @param extraParams map of extra parameter. Example {("start","/root"}
     * @return
     */
    public static String generateJahiaModulePlaceHolder(boolean templateUsage, String cssClassName, String jahiaType, String id, Map<String, Object> extraParams) {
        // css depending on type of module
        StringBuffer css = new StringBuffer();
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

        final StringBuffer outBuf = new StringBuffer("<div class=\"" + css + "\" id=\"").append(id).append("\" ");
        if (jahiaType != null) {
            outBuf.append(JahiaType.JAHIA_TYPE);
            outBuf.append("=\"");
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
        final StringBuffer outBuf = new StringBuffer();
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
