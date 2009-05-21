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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.ArrayList;

import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Column;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Constraint;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Ordering;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.QueryObjectModel;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.QueryObjectModelFactory;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Source;
import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.log4j.Logger;
import org.jahia.data.JahiaData;
import org.jahia.query.qom.JahiaQueryObjectModelConstants;
import org.jahia.query.qom.PropertyValueImpl;
import org.jahia.query.qom.QueryObjectModelFactoryImpl;
import org.jahia.query.qom.QueryObjectModelImpl;
import org.jahia.registries.ServicesRegistry;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.utils.JahiaTools;
import org.jahia.query.qom.QOMBuilder;

/**
 * This is the base tag for declaring Query object Model specified by the JSR-283.
 *
 * User: hollis
 * Date: 6 nov. 2007
 * Time: 15:42:29
 * To change this template use File | Settings | File Templates.
 */
public class QueryDefinitionTag extends AbstractJahiaTag {

    private static final long serialVersionUID = -2792055054804614561L;
    
    public static final String SET_ACTION = "set";
    public static final String APPEND_ACTION = "append";
    public static final String REMOVE_ACTION = "remove";

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(QueryDefinitionTag.class);

    private JahiaData jData;

    protected QOMBuilder qomBuilder;

    private QueryObjectModelFactory queryFactory;
    private ValueFactory valueFactory;

    private Properties properties;

    private QueryObjectModel queryObjectModel;

    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger logger) {
        QueryDefinitionTag.logger = logger;
    }


    public int doStartTag () throws JspException {

        ServletRequest request = pageContext.getRequest();
        jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        setProperties(new Properties());
        return EVAL_BODY_BUFFERED;
    }

    // Body is evaluated one time, so just writes it on standard output
    public int doAfterBody () {

        try {
            bodyContent.writeOut(bodyContent.getEnclosingWriter());
        } catch (IOException ioe) {
            logger.error("Error", ioe);
        }
        return EVAL_PAGE;
    }

    /**
     *
     * @return
     * @throws JspException
     */
    public int doEndTag ()
        throws JspException {

        if (getId() != null) {
            QueryObjectModel queryObjectModel = this.getQueryObjectModel();
            if (queryObjectModel != null) {
                pageContext.setAttribute(getId(), queryObjectModel,
                        PageContext.REQUEST_SCOPE);
            } else {
                pageContext.removeAttribute(getId(), PageContext.REQUEST_SCOPE);
            }
        }

        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        jData = null;
        queryFactory = null;
        valueFactory = null;
        queryObjectModel = null;
        id = null;
        properties = null;
        return EVAL_PAGE;
    }

    public QueryObjectModelFactory getQueryFactory() {
        if (queryFactory == null){
            try {
                qomBuilder = new QOMBuilder(jData.getProcessingContext(),properties);
                queryFactory = qomBuilder.getQomFactory();
            } catch ( Exception t ){
                logger.debug(t);
            }
        }
        return queryFactory;
    }

    public ValueFactory getValueFactory() {
        if (valueFactory == null){
            try {
                valueFactory = ServicesRegistry.getInstance().getQueryService()
                    .getValueFactory();
            } catch ( Exception t ){
                logger.debug(t);
            }
        }
        return valueFactory;
    }

    public QueryObjectModel getQueryObjectModel() {
        if (queryObjectModel == null){
            queryObjectModel = this.createQueryObjectModel();
        }
        return queryObjectModel;
    }

    public void setQueryObjectModel(QueryObjectModel queryObjectModel) {
        this.queryObjectModel = queryObjectModel;
    }

    public QueryObjectModel createQueryObjectModel() {
        QueryObjectModelFactory queryFactory = this.getQueryFactory();
        QueryObjectModel queryObjectModel = null;
        if ( queryFactory != null ){
            try {
                queryObjectModel = qomBuilder.createQOM();
                
                if (!getProperties().isEmpty() && queryObjectModel instanceof QueryObjectModelImpl) {
                    ((QueryObjectModelImpl)queryObjectModel).setProperties(getProperties());                    
                }
            } catch ( Exception t ){
                logger.warn(t);
            }
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
        return this.qomBuilder.getSource();
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

    public void addOrdering(String propertyName, boolean numberValue,
            String numberFormat, boolean metadata, String valueProviderClass,
            String order, boolean localeSensitive, String aliasNames) throws RepositoryException {
        Ordering ordering = null;
        QueryObjectModelFactory queryFactory = getQueryFactory();
        PropertyValueImpl propValue = (PropertyValueImpl) queryFactory
                .propertyValue(propertyName.trim());
        propValue.setNumberValue(numberValue);
        propValue.setMetadata(metadata);
        propValue.setNumberFormat(numberFormat);
        propValue.setValueProviderClass(valueProviderClass);
        propValue.setAliasNames(JahiaTools.getTokens(aliasNames,","));
                   
        if (queryFactory instanceof QueryObjectModelFactoryImpl) {
            QueryObjectModelFactoryImpl ourQueryFactory = (QueryObjectModelFactoryImpl) queryFactory;
            if (order != null
                    && order.length() > 0
                    && JahiaQueryObjectModelConstants.ORDER_DESCENDING == Integer
                            .parseInt(order)) {
                ordering = ourQueryFactory.descending(propValue,
                        localeSensitive);
            } else {
                ordering = ourQueryFactory
                        .ascending(propValue, localeSensitive);
            }
        } else {
            if (order != null
                    && order.length() > 0
                    && JahiaQueryObjectModelConstants.ORDER_DESCENDING == Integer
                            .parseInt(order)) {
                ordering = queryFactory.descending(propValue);
            } else {
                ordering = queryFactory.ascending(propValue);
            }
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
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public JahiaData getJData() {
        return jData;
    }

    public void setJData(JahiaData jData) {
        this.jData = jData;
    }

    public void andConstraint(Constraint c) throws Exception {
        this.qomBuilder.andConstraint(c);
    }

    public void orConstraint(Constraint c) throws Exception {
        this.qomBuilder.orConstraint(c);
    }

}