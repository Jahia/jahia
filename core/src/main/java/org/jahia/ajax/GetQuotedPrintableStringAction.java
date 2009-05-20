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
package org.jahia.ajax;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jahia.utils.JahiaString;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class GetQuotedPrintableStringAction extends AjaxAction {
    protected static final String VALUE = "value";

    private static final transient Logger logger = Logger
            .getLogger(GetQuotedPrintableStringAction.class);

    public ActionForward execute(final ActionMapping mapping,
                                 final ActionForm form,
                                 final HttpServletRequest request,
                                 final HttpServletResponse response)
            throws IOException, ServletException {
        try {
            final String value = new String(getParameter(request, VALUE).getBytes(CHARSET), CHARSET);
            final String result = JahiaString.encodeToQP(value, CHARSET);
            if (logger.isDebugEnabled()) {
                logger.debug("value �� : " + value + ", result: " + result);
            }

            final Document resp = getNewDocument(request);
            final Element root = resp.createElement("GetQuotedPrintableStringResp");
            final Element item = resp.createElement(VALUE);
            final StringBuffer buff = new StringBuffer(result.length() + 12);
            for (int i=0; i<result.length(); i++) {
                final char c = result.charAt(i);
                if (c == ' ') {
                    buff.append("|20");
                } else if (c == '=') {
                    buff.append('|');
                } else {
                    buff.append(c);
                }
            }
            item.setAttribute(VALUE, buff.toString());

            root.appendChild(item);
            resp.appendChild(root);
            sendResponse(resp, response);
        } catch (final Exception e) {
            handleException(e, request, response);
        }
        return null;
    }
}
