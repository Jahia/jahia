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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.taglibs.query.logs;

import org.jahia.services.audit.display.LogEntryItem;
import org.jahia.services.audit.display.LogsResultList;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 7 nov. 2007
 * Time: 15:36:14
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class ExecuteLogsQueryTag extends AbstractJahiaTag {

    private LogsQueryTag logQueryTag = null;

    private String scope;

    public int doStartTag() {
        logQueryTag = (LogsQueryTag) findAncestorWithClass(this, LogsQueryTag.class);
        if (logQueryTag == null) {
            return SKIP_BODY;
        }
        try {
            LogsResultList<LogEntryItem> result = logQueryTag.executeQuery();
            if (getId() != null) {
                if (result!= null) {
                    pageContext.setAttribute(getId(), result, getSessionScope());
                } else {
                    pageContext.removeAttribute(getId(), getSessionScope());
                }
            }
        } catch ( Exception t ){

        }
        return EVAL_BODY_BUFFERED;
    }

    public int doAfterBody() throws JspException {
        try {
            getBodyContent().writeOut(getPreviousOut());
        } catch (IOException ioe) {
            throw new JspTagException();
        }
        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        logQueryTag = null;
        scope = null;
        return EVAL_PAGE;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    protected int getSessionScope(){
        if ( "page".equals(this.scope) ){
            return PageContext.PAGE_SCOPE;
        } else if ( "request".equals(this.scope) ){
            return PageContext.REQUEST_SCOPE;
        } else if ( "session".equals(this.scope) ){
            return PageContext.SESSION_SCOPE;
        } else if ( "application".equals(this.scope) ){
            return PageContext.APPLICATION_SCOPE;
        }
        return PageContext.PAGE_SCOPE;
    }
}