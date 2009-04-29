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

import org.apache.log4j.Logger;
import org.jahia.services.audit.LogsQuery;
import org.jahia.utils.JahiaTools;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.List;

/**
 * User: hollis
 * Date: 5 d�c. 2007
 * Time: 15:53:00
 */
@SuppressWarnings("serial")
public class UsernamesConstraintTag extends TagSupport {

    private static Logger logger =
        Logger.getLogger(LogsQueryTag.class);

    private String usernames;

    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger logger) {
        UsernamesConstraintTag.logger = logger;
    }

    public int doStartTag () {

        LogsQueryTag logQueryTag = (LogsQueryTag) findAncestorWithClass(this, LogsQueryTag.class);
        if (logQueryTag == null) {
            return SKIP_BODY;
        }
        if (usernames == null || usernames.trim().length() == 0){
            return SKIP_BODY;
        }
        LogsQuery logsQuery = logQueryTag.getLogsQuery();
        if ( logsQuery == null ){
            return SKIP_BODY;
        }
        List<String> names = JahiaTools.getTokensList(this.usernames," *+, *+");
        if ( names != null && !names.isEmpty() ){
            logsQuery.setUsernames(names);
        }
        return SKIP_BODY;
    }

    /**
     *
     * @return
     * @throws javax.servlet.jsp.JspException
     */
    public int doEndTag ()
        throws JspException {
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        usernames = null;
        id = null;
        return EVAL_PAGE;
    }

    public String getUsernames() {
        return usernames;
    }

    public void setUsernames(String usernames) {
        this.usernames = usernames;
    }

}