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
package org.jahia.taglibs.jcr.query;

import org.apache.log4j.Logger;
import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.bin.Jahia;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.QueryResultAdapter;
import org.jahia.services.render.Resource;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.taglibs.query.QueryDefinitionTag;

import javax.jcr.RepositoryException;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.QueryObjectModel;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import java.io.IOException;
import java.security.Principal;
import java.util.Locale;

/**
 * Tag implementation for exposing a result of a QueryObjectModel query into the template
 * scope.
 */
@SuppressWarnings("serial")
public class JQOMTag extends QueryDefinitionTag {
    private static final Logger logger = Logger.getLogger(JCRSQLTag.class);

    private int scope = PageContext.PAGE_SCOPE;

    private String var;

    private String qomBeanName;

    public int doStartTag() throws JspException {
        return super.doStartTag();
    }

    // Body is evaluated one time, so just writes it on standard output
    public int doAfterBody() {
        int result = super.doAfterBody();

        if (this.getQomBeanName() != null && getId() != null) {
            pageContext.removeAttribute(getId(), PageContext.REQUEST_SCOPE);
        }
        try {
            bodyContent.writeOut(bodyContent.getEnclosingWriter());
        } catch (IOException ioe) {
            logger.error("Error:", ioe);
        }
        return result;
    }

    public int doEndTag() throws JspException {
        QueryObjectModel queryModel = this.getQomBeanName() == null ? this.getQueryObjectModel()
                : (QueryObjectModel) pageContext.getAttribute(this.getQomBeanName(), PageContext.REQUEST_SCOPE);

        final ProcessingContext ctx = getProcessingContext();
        if (ctx == null) {
            logger.error("ProcessingContext instance is null.");
        } else if (queryModel == null) {
            logger.error("QueryObjectModel instance is null.");
        } else {
            pageContext.setAttribute(var, findQueryResultByQOM(ctx.getUser(), queryModel), scope);
        }

        int result = super.doEndTag();
        resetState();
        return result;
    }

    /**
     * Find Node iterator by principal and QueryObjectModel.
     * 
     * @param p
     *            the principal
     * @param queryModel
     *            a QueryObjectModel to perform the JCR query
     * @return the {@link javax.jcr.NodeIterator} instance with the results of
     *         the query; returns empty iterator if nothing is found
     */
    private QueryResult findQueryResultByQOM(Principal p, QueryObjectModel queryModel) {
        QueryResult queryResult = null;
        if (logger.isDebugEnabled()) {
            logger.debug("Find node by qom [ " + queryModel.getStatement() + " ]");
        }
        if (p instanceof JahiaGroup) {
            logger.warn("method not implemented for JahiaGroup");
        } else {
            try {
                String workspace = null;
                Locale locale = Jahia.getThreadParamBean().getCurrentLocale();
                Resource currentResource = (Resource) pageContext.getAttribute("currentResource", PageContext.REQUEST_SCOPE);
                if (currentResource != null) {
                    workspace = currentResource.getWorkspace();
                    locale = currentResource.getLocale();
                }                
                JCRSessionWrapper session = ServicesRegistry.getInstance().getJCRStoreService().getThreadSession((JahiaUser) p, workspace, locale);
                queryResult = session.getWorkspace().execute(queryModel);                
                // execute query
                if (logger.isDebugEnabled()) {
                    logger.debug("Query[" + queryModel.getStatement() + "] --> found [" + queryResult + "] values.");
                }
            } catch (javax.jcr.ItemNotFoundException e) {
                logger.debug(e, e);
            } catch (javax.jcr.query.InvalidQueryException e) {
                logger.error("InvalidQueryException ---> [" + queryModel.getStatement() + "] is not valid.", e);
            } catch (RepositoryException e) {
                logger.error(e, e);
            }
        }
        return queryResult != null ? queryResult : new QueryResultAdapter();
    }

    public int getScope() {
        return scope;
    }

    public String getVar() {
        return var;
    }

    public String getQomBeanName() {
        return qomBeanName;
    }

    @Override
    protected void resetState() {
        super.resetState();
        scope = PageContext.PAGE_SCOPE;
        qomBeanName = null;
        var = null;
    }

    public void setScope(String scope) {
        this.scope = Util.getScope(scope);
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setQomBeanName(String qomBeanName) {
        this.qomBeanName = qomBeanName;
    }

}