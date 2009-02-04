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

package org.jahia.taglibs.query;

import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Ordering;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.QueryObjectModelFactory;
import org.jahia.query.qom.JahiaQueryObjectModelConstants;
import org.jahia.query.qom.PropertyValueImpl;
import org.jahia.query.qom.QueryObjectModelFactoryImpl;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.servlet.jsp.JspException;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 7 nov. 2007
 * Time: 15:33:24
 * To change this template use File | Settings | File Templates.
 */
public class SortByTag extends AbstractJahiaTag {

    private static final long serialVersionUID = 7747723525104918964L;

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(SortByTag.class);

    private QueryDefinitionTag queryModelDefTag = null;
    private QueryObjectModelFactory queryFactory = null;

    private String propertyName;

    private String numberFormat;
    private String numberValue;
    private String valueProviderClass;
    private String isMetadata;

    private String order;
    private String localeSensitive = "false";

    public int doStartTag() throws JspException {
        queryModelDefTag = (QueryDefinitionTag) findAncestorWithClass(this, QueryDefinitionTag.class);
        if (queryModelDefTag == null || queryModelDefTag.getQueryFactory()==null) {
            return SKIP_BODY;
        }
        this.queryFactory = queryModelDefTag.getQueryFactory();
        if (this.propertyName == null || this.propertyName.trim().equals("")){
            return EVAL_BODY_BUFFERED;
        }
        Ordering ordering = null;
        try {
            PropertyValueImpl propValue = (PropertyValueImpl)this.queryFactory.propertyValue(this.getPropertyName().trim());
            propValue.setNumberValue("true".equals(this.getNumberValue()));
            propValue.setMetadata("true".equals(this.getMetadata()));
            propValue.setNumberFormat(this.getNumberFormat());
            propValue.setValueProviderClass(this.valueProviderClass);
            if (this.queryFactory instanceof QueryObjectModelFactoryImpl) {
                QueryObjectModelFactoryImpl ourQueryFactory = (QueryObjectModelFactoryImpl) this.queryFactory;
                if (JahiaQueryObjectModelConstants.ORDER_DESCENDING == Integer
                        .parseInt(order)) {
                    ordering = ourQueryFactory.descending(propValue, Boolean
                            .valueOf(this.getLocaleSensitive()).booleanValue());
                } else {
                    ordering = ourQueryFactory.ascending(propValue, Boolean
                            .valueOf(this.getLocaleSensitive()).booleanValue());
                }
            } else {
                if (JahiaQueryObjectModelConstants.ORDER_DESCENDING == Integer
                        .parseInt(order)) {
                    ordering = this.queryFactory.descending(propValue);
                } else {
                    ordering = this.queryFactory.ascending(propValue);
                }
            }

            this.queryModelDefTag.addOrdering(ordering);
        } catch ( Exception t ){
            logger.debug("Error creating ordering clause",t);
            throw new JspException("Error creating Ordering node in SortBy Tag",t);
        }
        return EVAL_BODY_BUFFERED;
    }

    public int doEndTag() throws JspException {
        queryModelDefTag = null;
        queryFactory = null;
        propertyName = null;
        propertyName = null;
        numberFormat = null;
        numberValue = null;
        isMetadata = null;
        valueProviderClass = null;
        order = null;
        localeSensitive = null;
        return EVAL_PAGE;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getNumberFormat() {
        return numberFormat;
    }

    public void setNumberFormat(String numberFormat) {
        this.numberFormat = numberFormat;
    }

    public String getNumberValue() {
        return numberValue;
    }

    public void setNumberValue(String numberValue) {
        this.numberValue = numberValue;
    }

    public String getMetadata() {
        return isMetadata;
    }

    public void setMetadata(String metadata) {
        isMetadata = metadata;
    }

    public String getValueProviderClass() {
        return valueProviderClass;
    }

    public void setValueProviderClass(String valueProviderClass) {
        this.valueProviderClass = valueProviderClass;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getLocaleSensitive() {
        return localeSensitive;
    }

    public void setLocaleSensitive(String localeSensitive) {
        this.localeSensitive = localeSensitive;
    }

}