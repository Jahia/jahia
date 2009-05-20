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