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

import org.jahia.services.containers.ContainerQueryBean;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 8 nov. 2007
 * Time: 13:08:23
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class CreateContainerQueryBeanTag extends AbstractJahiaTag {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(CreateContainerQueryBeanTag.class);

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
        if (getId() != null) {
            if (queryBean != null) {
                pageContext.setAttribute(getId(), queryBean);
            } else {
                pageContext.removeAttribute(getId(), PageContext.PAGE_SCOPE);
            }
        }
        return EVAL_BODY_BUFFERED;
    }

    public int doEndTag() throws JspException {
        id = null;
        return EVAL_PAGE;
    }
}