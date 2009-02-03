/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
