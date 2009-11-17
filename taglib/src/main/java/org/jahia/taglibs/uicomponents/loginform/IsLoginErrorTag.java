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
package org.jahia.taglibs.uicomponents.loginform;

import java.io.IOException;

import org.jahia.taglibs.ValueJahiaTag;
import org.jahia.data.JahiaData;
import org.jahia.params.ProcessingContext;
import org.jahia.params.valves.LoginEngineAuthValveImpl;
import org.apache.log4j.Logger;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
public class IsLoginErrorTag extends ValueJahiaTag {
    private static final transient Logger logger = Logger.getLogger(IsLoginErrorTag.class);

    public int doStartTag() {
        final JahiaData jData = getJahiaData();
        final ProcessingContext jParams = getProcessingContext();
        final String valveResult = (String) jParams.getAttribute(LoginEngineAuthValveImpl.VALVE_RESULT);
        if (valveResult != null && !LoginEngineAuthValveImpl.OK.equals(valveResult)) {
            if (getVar() != null) {
                pageContext.setAttribute(getVar(), valveResult);
            }
            if (getValueID() != null) {
                pageContext.setAttribute(getValueID(), valveResult);
            }
            return EVAL_BODY_BUFFERED;            
        } else {
            return SKIP_BODY;
        }
    }

    public int doAfterBody() {
        final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        if (!jData.gui().isLogged()) {
            final JspWriter out = bodyContent.getEnclosingWriter();
            try {
                bodyContent.writeOut(out);
            } catch (IOException ioe) {
                logger.error("Error:", ioe);
            }
        }
        return SKIP_BODY;
    }


    public int doEndTag() throws JspException {
        resetState();
        return EVAL_PAGE;
    }

    @Override
    protected void resetState() {
        super.resetState();
    }
}
