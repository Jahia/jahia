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
package org.jahia.taglibs.template.templatestructure;

import org.jahia.data.JahiaData;

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
            final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
            final String languageCode = jData.getProcessingContext().getLocale().toString();
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
     * @param jData   jahia data
     * @return the script include
     */
    public static String getJSToolsImport(final HttpServletRequest request, final JahiaData jData) {
        final StringBuilder buf = new StringBuilder();
        if (jData.gui().isLogged()) {
            buf.append("<script type=\"text/javascript\" src=\"");
            buf.append(jData.getProcessingContext().settings().getJsHttpPath());
            buf.append("\"></script>\n");
        }
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
     * Translation of <%@ include file="/javascript/serverconfig_js.inc"%>
     *
     * @param request the request
     * @return the script to include
     */
    public static String getServerConfigImport(final HttpServletRequest request) {
        final String jahiaContextPath = request.getContextPath();
        final String jahiaMainServletPath = jahiaContextPath + org.jahia.bin.Jahia.getServletPath();
        final StringBuilder buf = new StringBuilder("<script type=\"text/javascript\" language=\"JavaScript\">\n");
        buf.append("var jahiaMainServletPath=\"").append(jahiaMainServletPath).append("\";\n");
        buf.append("var jahiaContextPath =\"").append(jahiaContextPath).append("\";\n");
        buf.append("var jahiaCoreWebdavPath =\"").append(jahiaContextPath).append("/webdav\";\n");
        buf.append("</script>\n");
        return buf.toString();
    }

}
