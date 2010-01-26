/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.taglibs.template.templatestructure;

import org.jahia.bin.Jahia;
import org.jahia.data.JahiaData;
import org.jahia.services.render.RenderContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletRequest;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 6 mars 2008 - 16:11:00
 */
public class DefaultIncludeProvider {

    public static final String XHTML = "xhtml";
    public static final String HTML = "html";
    public static final String XHTML_STRICT = "xhtml-strict";
    public static final String XHTML_TRANSITIONAL = "xhtml-transitional";
    public final static String HTML_STRICT = "html-strict";
    public final static String HTML_TRANSITIONAL = "html-transitional";

    public static String getDocType(final String doctype) {
        final String lowerCaseDocType = doctype.toLowerCase();
        if (XHTML.equals(lowerCaseDocType) || XHTML_STRICT.equals(lowerCaseDocType)) {
            return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n"
                 + " \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n";

        } else if (XHTML_TRANSITIONAL.equals(lowerCaseDocType)) {
            return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n"
                 + " \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n";

        } else if (HTML.equals(lowerCaseDocType) || HTML_STRICT.equals(lowerCaseDocType)) {
            return "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\"\n"
                 + " \"http://www.w3.org/TR/html4/strict.dtd\">\n";

        } else if (HTML_TRANSITIONAL.equals(lowerCaseDocType)) {
            return "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n"
                 + " \"http://www.w3.org/TR/html4/loose.dtd\">\n";

        } else {
            throw new IllegalArgumentException("Unknown DOCTYPE '" + doctype + "'");
        }
    }

    public static String getHtmlTag(final String doctype, final ServletRequest request) {
        if (XHTML_STRICT.equals(doctype) || XHTML_TRANSITIONAL.equals(doctype)) {
            // final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
            // final String languageCode = jData.getProcessingContext().getLocale().toString();
            RenderContext renderContext = (RenderContext) request.getAttribute("renderContext");
            final String languageCode = renderContext.getMainResource().getLocale().toString(); 
            return "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"" + languageCode + "\">";
        } else {
            return "<html>";
        }
    }

    public static String drawHttpJspContext(final HttpServletRequest req) {
        return req.getRequestURI().substring(0, req.getRequestURI().lastIndexOf("/"));
    }

    /**
     * Translation of the tag <content:JSTools/>
     *
     * @param request the current request
     * @param renderContext the current rendering context object.
     * @return the script include
     */
    public static String getJSToolsImport(final HttpServletRequest request, final RenderContext renderContext) {
        return getJSToolsImportCss(request) + getJSToolsImportJavaScript(renderContext);
    }

    /**
     * Translation of the tag <content:JSTools/>
     *
     * @param request the current request
     * @return the CSS include
     */
    public static String getJSToolsImportCss(HttpServletRequest request) {
        final StringBuilder buf = new StringBuilder();
        buf.append("<!--[if lte IE 6]>\n");
        buf.append("<style type=\"text/css\">\n");
        buf.append("img {\n");
        buf.append("behavior: url(\"");
        buf.append(request.getContextPath());
        buf.append("/css/pngbehavior.jsp\")");
        buf.append("}\n");
        buf.append("</style>\n");
        buf.append("<![endif]-->\n");
        return buf.toString();
    }

    /**
     * Translation of the tag <content:JSTools/>
     *
     * @param renderContext the current rendering context object.
     * @return the script include
     */
    public static String getJSToolsImportJavaScript(final RenderContext renderContext) {
        if (renderContext.isLoggedIn()) {
            final StringBuilder buf = new StringBuilder();
            buf.append("<script type=\"text/javascript\" src=\"");
            buf.append(Jahia.getSettings().getJsHttpPath());
            buf.append("\"></script>\n");
            return buf.toString();
        } else {
            return "";
        }
    }
}
