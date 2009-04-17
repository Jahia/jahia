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
public class ContentDefinitionNamesConstraintTag extends TagSupport {

    private static Logger logger =
        Logger.getLogger(LogsQueryTag.class);

    private String definitionNames;

    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger logger) {
        ContentDefinitionNamesConstraintTag.logger = logger;
    }

    public int doStartTag () {

        LogsQueryTag logQueryTag = (LogsQueryTag) findAncestorWithClass(this, LogsQueryTag.class);
        if (logQueryTag == null) {
            return SKIP_BODY;
        }
        if (definitionNames == null || definitionNames.trim().length() == 0){
            return SKIP_BODY;
        }
        LogsQuery logsQuery = logQueryTag.getLogsQuery();
        if ( logsQuery == null ){
            return SKIP_BODY;
        }
        List<String> names = JahiaTools.getTokensList(this.definitionNames, " *+, *+");
        if ( names != null && !names.isEmpty() ){
            logsQuery.setContentDefinitionNames(names);
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
        definitionNames = null;
        id = null;
        return EVAL_PAGE;
    }

    public String getDefinitionNames() {
        return definitionNames;
    }

    public void setDefinitionNames(String definitionNames) {
        this.definitionNames = definitionNames;
    }


}