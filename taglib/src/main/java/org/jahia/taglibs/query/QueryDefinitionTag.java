/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.taglibs.query;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.commons.query.QueryObjectModelBuilder;
import org.apache.jackrabbit.commons.query.QueryObjectModelBuilderRegistry;
import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.services.query.QOMBuilder;
import org.jahia.taglibs.jcr.AbstractJCRTag;

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

/**
 * This is the base tag for declaring Query object Model specified by the
 * JSR-283.
 * <p/>
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
     * @throws JspTagException in case of QOM processing errors
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
