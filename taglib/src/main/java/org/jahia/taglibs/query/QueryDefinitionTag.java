/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.taglibs.query;

import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;
import javax.jcr.query.Query;
import javax.jcr.query.qom.Column;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;

import org.apache.jackrabbit.commons.query.QueryObjectModelBuilder;
import org.apache.jackrabbit.commons.query.QueryObjectModelBuilderRegistry;
import org.apache.taglibs.standard.tag.common.core.Util;
import org.drools.util.StringUtils;
import org.jahia.services.query.QOMBuilder;
import org.jahia.taglibs.jcr.AbstractJCRTag;

/**
 * This is the base tag for declaring Query object Model specified by the
 * JSR-283.
 * 
 * User: hollis Date: 6 nov. 2007 Time: 15:42:29
 */
public class QueryDefinitionTag extends AbstractJCRTag {

    private static final long serialVersionUID = -2792055054804614561L;

    private QOMBuilder qomBuilder;

    private String qomBeanName;

    private Object qom;

    private QueryObjectModel queryObjectModel;

    private int scope = PageContext.PAGE_SCOPE;

    private String var;

    private String statement;

    private long limit;
    private long offset;

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

                QueryObjectModel qom = getInitialQueryObjectModel();
                if (qom != null) {
                    qomBuilder.setSource(qom.getSource());
                    for (Column column : qom.getColumns()) {
                        qomBuilder.getColumns().add(column);
                    }
                    qomBuilder.andConstraint(qom.getConstraint());
                    for (Ordering ordering : qom.getOrderings()) {
                        qomBuilder.getOrderings().add(ordering);
                    }
                }
            } catch (RepositoryException e) {
                throw new JspTagException(e);
            }
        }
        return qomBuilder;
    }

    protected QueryObjectModel getInitialQueryObjectModel() throws RepositoryException {
        if (qomBeanName != null) {
            return (QueryObjectModel) pageContext.getAttribute(qomBeanName, scope);
        } else if (qom != null) {
            return (QueryObjectModel) qom;
        } else if (!StringUtils.isEmpty(statement)) {
            QueryObjectModelFactory qf = getJCRSession().getWorkspace().getQueryManager().getQOMFactory();
            ValueFactory vf = getJCRSession().getValueFactory();
            QueryObjectModelBuilder builder = QueryObjectModelBuilderRegistry.getQueryObjectModelBuilder(Query.JCR_SQL2);
            return builder.createQueryObjectModel(statement, qf, vf);
        }
        return null;
    }

    protected QueryObjectModel getQueryObjectModel() throws RepositoryException {
        if (queryObjectModel == null) {
            if (qomBuilder != null) {
                queryObjectModel = qomBuilder.createQOM();
            } else {
                final QueryObjectModel initialQOM = getInitialQueryObjectModel();
                if (initialQOM != null) {
                    queryObjectModel = initialQOM;
                }
            }
            if (queryObjectModel != null) {
                if (limit > 0) {
                    queryObjectModel.setLimit(limit);
                }
                if (offset > 0) {
                    queryObjectModel.setOffset(offset);
                }
            }
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
        qomBeanName = null;
        queryObjectModel = null;
        id = null;
        var = null;
        statement = null;
        scope = PageContext.PAGE_SCOPE;
        limit = 0;
        offset = 0;
        super.resetState();
    }

    public void setScope(String scope) {
        this.scope = Util.getScope(scope);
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public void setQomBeanName(String qomBeanName) {
        this.qomBeanName = qomBeanName;
    }

    public void setQom(Object qom) {
        this.qom = qom;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }
}