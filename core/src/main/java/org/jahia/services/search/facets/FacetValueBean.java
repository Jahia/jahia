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
