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
package org.jahia.taglibs.internal.utility;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

import org.jahia.data.JahiaData;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.utils.JahiaConsole;

/**
 * Class JSToolsTag : includes the Jahia JavaScript source file(s)
 *
 * @author Jerome Tamiotti
 *         <p/>
 *         <p/>
 *         jsp:tag name="JSTools" body-content="empty"
 *         description="includes the Jahia JavaScript source file(s) in the current page.
 *         <p/>
 *         <p><attriInfo>These Javascript files are necessary for Jahia's popups and therefore should always be included.
 *         <p/>
 *         <p><b>Example :</b>
 *         <p/>
 *         <p>&lt;content:JSTools/&gt;
 *         <p>generates the following HTML:
 *         <p/>
 *         &lt;script type=\"text/javascript\" src=\"/jahia/javascript/jahia.js\"&gt;&lt;/script&gt;
 *         <p/>
 *         </attriInfo>"
 */
@SuppressWarnings("serial")
public class JSToolsTag extends AbstractJahiaTag {

    public int doStartTag() {

        final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        try {
            final JahiaData jData = (JahiaData) request.getAttribute(
                    "org.jahia.data.JahiaData");
            StringBuffer buf = new StringBuffer() ;
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
            final JspWriter out = pageContext.getOut();
            out.print(buf.toString());
        } catch (IOException ioe) {
            JahiaConsole.println("JSToolsTag: doStartTag ", ioe.toString());
        }
        return SKIP_BODY;
    }

}
