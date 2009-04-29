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
package org.jahia.services.search.facets;

import java.io.Serializable;

import org.jahia.bin.Jahia;
import org.jahia.params.ProcessingContext;
import org.jahia.services.categories.Category;
import org.jahia.utils.JahiaTools;

public class FacetValueBean implements Serializable {

    private static final long serialVersionUID = 7376877535833721067L;
    
    private FacetBean facetBean;
    private String value;
    private Object[] valueArguments;    
    private String filterQuery;    
    private String languageCode;    
    
    public FacetValueBean(FacetBean facetBean, String value, Object[] valueArguments, String filterQuery, String languageCode) {
        super();
        this.facetBean = facetBean;        
        this.value = value;
        this.setValueArguments(valueArguments);
        this.filterQuery = filterQuery;        
        this.languageCode = languageCode;        
    }
    public String getId() {
        return String.valueOf(hashCode());
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    public String getFilterQuery() {
        return filterQuery;
    }
    public void setFilterQuery(String filterQuery) {
        this.filterQuery = filterQuery;
    }
    @Override
    public int hashCode() {
        return Math.abs(getFilterQuery().hashCode());
    }
    public void setValueArguments(Object[] valueArguments) {
        this.valueArguments = valueArguments;
    }
    public Object[] getValueArguments() {
        return valueArguments;
    }
    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }
    public String getLanguageCode() {
        return languageCode;
    }
    
    public String getTitle() {
        ProcessingContext jParams = Jahia.getThreadParamBean();

        String title = "";
        if (facetBean.getFacetType() == FacetBean.FacetType.CATEGORY_FACET) {
            Category category = ((Category) getValueArguments()[0]);
            title = category.getTitle(jParams.getLocale(), category.getKey());
        } else {
            title = JahiaTools.getExpandedValue(getValue(), getValueArguments(), jParams, jParams.getLocale());
        }

        return title;
    }

}
