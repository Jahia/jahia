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
package org.jahia.taglibs.query.logs;

import org.apache.log4j.Logger;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.audit.LogsQuery;
import org.jahia.services.audit.LogsQueryByCriteria;
import org.jahia.services.audit.display.LogEntryItem;
import org.jahia.services.audit.display.LogsResultList;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 5 d�c. 2007
 * Time: 15:53:00
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class LogsQueryTag extends AbstractJahiaTag {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(LogsQueryTag.class);

    private JahiaData jData;

    private LogsQuery logsQuery;

    private String logsQueryName;

    private int dbMaxLimit;

    private int maxSize;

    private String uniqueContentObject = "true";

    private int timeBasedPublishingLoadFlag;

    private String checkACL = "true";

    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger logger) {
        LogsQueryTag.logger = logger;
    }

    public LogsQuery getLogsQuery() {
        return logsQuery;
    }

    public void setLogsQuery(LogsQueryByCriteria logsQuery) {
        this.logsQuery = logsQuery;
    }

    public int doStartTag () {

        ServletRequest request = pageContext.getRequest();
        jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");

        initLogsQuery();

        if (getId() != null) {
            if (logsQuery!= null) {
                pageContext.setAttribute(getId(), logsQuery);
            } else {
                pageContext.removeAttribute(getId(), PageContext.PAGE_SCOPE);
            }
        }
        return EVAL_BODY_BUFFERED;
    }

    // Body is evaluated one time, so just writes it on standard output
    public int doAfterBody () {
        try {
            bodyContent.writeOut(bodyContent.getEnclosingWriter());
        } catch (IOException ioe) {
            logger.error("Error:", ioe);
        }
        return EVAL_PAGE;
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
        jData = null;
        logsQuery = null;
        checkACL = "true";
        uniqueContentObject = "true";
        dbMaxLimit = 0;
        maxSize = 0;
        timeBasedPublishingLoadFlag = 0;
        id = null;

        return EVAL_PAGE;
    }

    public String getLogsQueryName() {
        return logsQueryName;
    }

    public void setLogsQueryName(String logsQueryName) {
        this.logsQueryName = logsQueryName;
    }

    public int getDbMaxLimit() {
        return dbMaxLimit;
    }

    public void setDbMaxLimit(int dbMaxLimit) {
        this.dbMaxLimit = dbMaxLimit;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public String getUniqueContentObject() {
        return uniqueContentObject;
    }

    public void setUniqueContentObject(String uniqueContentObject) {
        this.uniqueContentObject = uniqueContentObject;
    }

    public int getTimeBasedPublishingLoadFlag() {
        return timeBasedPublishingLoadFlag;
    }

    public void setTimeBasedPublishingLoadFlag(int timeBasedPublishingLoadFlag) {
        this.timeBasedPublishingLoadFlag = timeBasedPublishingLoadFlag;
    }

    public String getCheckACL() {
        return checkACL;
    }

    public void setCheckACL(String checkACL) {
        this.checkACL = checkACL;
    }

    protected void initLogsQuery(){
        try {
            logsQuery = ServicesRegistry.getInstance()
                    .getJahiaAuditLogManagerService().getLogsQuery(this.logsQueryName);
            if ( logsQuery == null ){
                return;
            }
            if ( this.dbMaxLimit > 0 ){
                this.logsQuery.setDBMaxLimit(dbMaxLimit);
            }
            if ( this.maxSize > 0 ){
                this.logsQuery.setMaxSize(maxSize);
            }
            if ( this.timeBasedPublishingLoadFlag > 0 ){
                this.getLogsQuery().setTimeBasedPublishingLoadFlag(this.timeBasedPublishingLoadFlag);
            }
            this.getLogsQuery().setCheckACL("true".equals(this.checkACL));
            this.getLogsQuery().setUniqueContentObject("true".equals(this.uniqueContentObject));
        } catch ( Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void initQuery() throws JahiaException {
        if ( logsQuery != null ){
            logsQuery.init(jData.getProcessingContext());
        }
    }

    public LogsResultList<LogEntryItem> executeQuery() throws JahiaException {
        if ( logsQuery == null ){
            return null;
        }
        return logsQuery.executeQuery(jData.getProcessingContext());
    }
}
