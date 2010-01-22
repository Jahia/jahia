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
package org.jahia.taglibs.query;

import javax.jcr.RepositoryException;
import javax.jcr.query.qom.QueryObjectModel;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;

import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.services.query.QOMBuilder;
import org.jahia.taglibs.AbstractJCRTag;

/**
 * This is the base tag for declaring Query object Model specified by the
 * JSR-283.
 * 
 * User: hollis Date: 6 nov. 2007 Time: 15:42:29
 */
public class QueryDefinitionTag extends AbstractJCRTag {

    private static final long serialVersionUID = -2792055054804614561L;

    private QOMBuilder qomBuilder;

    private QueryObjectModel queryObjectModel;

    private int scope = PageContext.PAGE_SCOPE;

    private String var;

    /**
     * @return
     * @throws JspException
     */
    @Override
    public int doEndTag() throws JspException {
        try {
            pageContext.setAttribute(getVar(), getQueryObjectModel(), getScope());
        } catch (RepositoryException e) {
            throw new JspTagException(e);
        } finally {
            resetState();
        }

        return EVAL_PAGE;
    }

    /**
     * Returns current QOM builder instance.
     * 
     * @return an instance of current {@link QOMBuilder}
     * @throws JspTagException 
     */
    public QOMBuilder getQOMBuilder() throws JspTagException {
        if (qomBuilder == null) {
            try {
                qomBuilder = new QOMBuilder(getJCRSession().getWorkspace().getQueryManager().getQOMFactory(), getJCRSession().getValueFactory());
            } catch (RepositoryException e) {
                throw new JspTagException(e);
            }
        }
        return qomBuilder;
    }

    protected QueryObjectModel getQueryObjectModel() throws RepositoryException {
        if (queryObjectModel == null) {
            queryObjectModel = qomBuilder.createQOM();
        }
        return queryObjectModel;
    }

    protected int getScope() {
        return scope;
    }

    protected String getVar() {
        return var;
    }

    @Override
    protected void resetState() {
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        qomBuilder = null;
        queryObjectModel = null;
        id = null;
        var = null;
        scope = PageContext.PAGE_SCOPE;
        super.resetState();
    }

    public void setScope(String scope) {
        this.scope = Util.getScope(scope);
    }

    public void setVar(String var) {
        this.var = var;
    }

}