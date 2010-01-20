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

import org.jahia.query.qom.QOMBuilder;
import org.jahia.registries.ServicesRegistry;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;
import javax.jcr.query.qom.*;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * This is the base tag for declaring Query object Model specified by the JSR-283.
 *
 * User: hollis
 * Date: 6 nov. 2007
 * Time: 15:42:29
 */
public class QueryDefinitionTag extends AbstractJahiaTag {

    private static final long serialVersionUID = -2792055054804614561L;
    
    public static final String SET_ACTION = "set";
    public static final String APPEND_ACTION = "append";
    public static final String REMOVE_ACTION = "remove";

    protected QOMBuilder qomBuilder;

    private QueryObjectModelFactory queryFactory;
    private ValueFactory valueFactory;

    private Properties properties;

    private QueryObjectModel queryObjectModel;

    /**
     *
     * @return
     * @throws JspException
     */
    public int doEndTag () throws JspException {

        try {
            if (getId() != null) {
                QueryObjectModel queryObjectModel;
                try {
                    queryObjectModel = this.getQueryObjectModel();
                } catch (RepositoryException e) {
                    throw new JspTagException(e);
                }
                if (queryObjectModel != null) {
                    pageContext.setAttribute(getId(), queryObjectModel,
                            PageContext.REQUEST_SCOPE);
                } else {
                    pageContext.removeAttribute(getId(), PageContext.REQUEST_SCOPE);
                }
            }
        } finally {
            resetState();
        }
        
        return EVAL_PAGE;
    }
    
    @Override
    protected void resetState() {
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        queryFactory = null;
        valueFactory = null;
        queryObjectModel = null;
        id = null;
        properties = null;
        
        super.resetState();
    }



    public QueryObjectModelFactory getQueryFactory() throws RepositoryException {
        if (queryFactory == null){
            qomBuilder = new QOMBuilder(getRenderContext().getMainResource().getLocale());
            queryFactory = qomBuilder.getQomFactory();
        }
        return queryFactory;
    }

    public ValueFactory getValueFactory() {
        if (valueFactory == null){
            valueFactory = ServicesRegistry.getInstance().getQueryService().getValueFactory();
        }
        return valueFactory;
    }

    public QueryObjectModel getQueryObjectModel() throws RepositoryException {
        if (queryObjectModel == null){
            queryObjectModel = this.createQueryObjectModel();
        }
        return queryObjectModel;
    }

    public void setQueryObjectModel(QueryObjectModel queryObjectModel) {
        this.queryObjectModel = queryObjectModel;
    }

    public QueryObjectModel createQueryObjectModel() throws RepositoryException {
        QueryObjectModelFactory queryFactory = this.getQueryFactory();
        QueryObjectModel queryObjectModel = null;
        if ( queryFactory != null ){
            queryObjectModel = qomBuilder.createQOM();
        }
        return queryObjectModel;
    }

    public Constraint getConstraint() {
        return this.qomBuilder.getConstraint();
    }

    public void setConstraint(Constraint constraint) {
        this.qomBuilder.setConstraint(constraint);
    }

    public Source getSource() {
        return qomBuilder != null ? qomBuilder.getSource() : null;
    }

    public void setSource(Source source) {
        this.qomBuilder.setSource(source);
    }

    public Ordering[] getOrderings() {
        Ordering[] orderings = new Ordering[]{};
        if (this.qomBuilder.getOrderings() != null && !this.qomBuilder.getOrderings().isEmpty()){
            orderings = this.qomBuilder.getOrderings().toArray(orderings);
        }
        return orderings;
    }

    public void setOrderings(Ordering[] orderings) {
        List<Ordering> orderingsList = new ArrayList<Ordering>();
        if (orderings != null){
            orderingsList = Arrays.asList(orderings);
        }
        this.qomBuilder.setOrderings(orderingsList);
    }

    public void addOrdering(String selectorName, String propertyName, String order) throws RepositoryException {
        Ordering ordering = null;
        QueryObjectModelFactory queryFactory = getQueryFactory();
        PropertyValue propValue = queryFactory.propertyValue(selectorName, propertyName.trim());

        if (order != null && order.length() > 0 && "desc".equals(order)) {
            ordering = queryFactory.descending(propValue);
        } else {
            ordering = queryFactory.ascending(propValue);
        }
        this.qomBuilder.addOrdering(ordering);
    }

    public Column[] getColumns() {
        if (this.qomBuilder.getColumns()== null || this.qomBuilder.getColumns().isEmpty()){
            return new Column[]{};
        }
        return this.qomBuilder.getColumns().toArray(new Column[] {});
    }

    public void setColumns(Column[] columns) {
        List<Column> columnsList = new ArrayList<Column>();
        if (columns != null){
            columnsList = Arrays.asList(columns);
        }
        this.qomBuilder.setColumns(columnsList);
    }

    public Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
        }
        return properties;
    }

    public void andConstraint(Constraint c) throws Exception {
        this.qomBuilder.andConstraint(c);
    }

    public void orConstraint(Constraint c) throws Exception {
        this.qomBuilder.orConstraint(c);
    }

}