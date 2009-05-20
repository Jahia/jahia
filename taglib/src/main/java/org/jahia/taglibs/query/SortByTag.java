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

    private String propertyName;

    private String numberFormat;
    private String numberValue;
    private String valueProviderClass;
    private String isMetadata;
    private String aliasNames;    

    private String order;
    private String localeSensitive = "false";

    public int doStartTag() throws JspException {
        queryModelDefTag = (QueryDefinitionTag) findAncestorWithClass(this, QueryDefinitionTag.class);
        if (queryModelDefTag == null || queryModelDefTag.getQueryFactory()==null) {
            return SKIP_BODY;
        }
        if (this.propertyName == null || this.propertyName.trim().equals("")){
            return EVAL_BODY_BUFFERED;
        }
        try {
            this.queryModelDefTag.addOrdering(getPropertyName(), "true".equals(getNumberValue()), getNumberFormat(), "true".equals(getMetadata()), getValueProviderClass(), getOrder(), "true".equals(getLocaleSensitive()), getAliasNames());
        } catch ( Exception t ){
            logger.debug("Error creating ordering clause",t);
            throw new JspException("Error creating Ordering node in SortBy Tag",t);
        }
        return EVAL_BODY_BUFFERED;
    }

    public int doEndTag() throws JspException {
        queryModelDefTag = null;
        propertyName = null;
        propertyName = null;
        numberFormat = null;
        numberValue = null;
        isMetadata = null;
        valueProviderClass = null;
        order = null;
        localeSensitive = null;
        aliasNames = null;        
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

    public void setAliasNames(String aliasNames) {
        this.aliasNames = aliasNames;
    }

    public String getAliasNames() {
        return aliasNames;
    }

}