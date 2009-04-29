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
package org.jahia.taglibs.query;

import org.jahia.data.JahiaData;
import org.jahia.data.containers.ContainerSorterByLogsQuery;
import org.jahia.services.audit.LogsQuery;
import org.jahia.services.containers.ContainerQueryBean;
import org.jahia.services.containers.ContainerQueryContext;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 8 nov. 2007
 * Time: 13:08:23
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class CreateContainerSorterFromLogsQueryTag extends AbstractJahiaTag {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(CreateContainerSorterFromLogsQueryTag.class);

    private String logsQueryId;

    public int doStartTag() throws JspException {

        ContainerQueryTag containerQueryTag = (ContainerQueryTag) findAncestorWithClass(this, ContainerQueryTag.class);
        if (containerQueryTag == null) {
            return SKIP_BODY;
        }
        ContainerQueryBean queryBean = null;
        try {
            queryBean = containerQueryTag.getQueryBean(containerQueryTag.getJData());
        } catch ( Exception t ){
            logger.debug(t);    
            throw new JspException("Cannot create the ContainerQueryBean instance",t);
        }
        if (queryBean != null && logsQueryId != null && !"".equals(logsQueryId.trim())){
            LogsQuery logsQuery = (LogsQuery)pageContext.getAttribute(logsQueryId);
            if ( logsQuery != null ){
                logsQuery.setCheckACL(false);
                logsQuery.disableTimeBasedPublishingCheck();
                ContainerQueryContext context = queryBean.getQueryContext();
                ServletRequest request = pageContext.getRequest();
                JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
                try {
                    ContainerSorterByLogsQuery sorter =
                            new ContainerSorterByLogsQuery(context.getContainerListID(),
                            logsQuery,jData.getProcessingContext().getEntryLoadRequest());
                    queryBean.setSorter(sorter);
                } catch ( Exception t) {
                    logger.debug("Exception created ContainerSorterByLogsQuery",t);
                    throw new JspException("Exception created ContainerSorterByLogsQuery",t);
                }
            }
        }
        return EVAL_BODY_BUFFERED;
    }

    public int doEndTag() throws JspException {
        id = null;
        logsQueryId = null;
        return EVAL_PAGE;
    }

    public String getLogsQueryId() {
        return logsQueryId;
    }

    public void setLogsQueryId(String logsQueryId) {
        this.logsQueryId = logsQueryId;
    }

}