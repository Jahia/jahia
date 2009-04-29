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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FacetBean implements Serializable {

    private static final long serialVersionUID = 827940036254584574L;

    public enum FacetType {
        FIELD_FACET, QUERY_FACET, CATEGORY_FACET;
    }
    
    private String name;

    private String propertyName;
    private FacetType facetType;
    private boolean multiple;
    
    private Map<String, FacetValueBean> facetValueBeans = new HashMap<String, FacetValueBean>();

    public FacetBean(String name, FacetType facetType) {
        super();
        this.name = name;
        this.facetType = facetType;
    }    
    
    public FacetBean(String name, String propertyName, FacetType facetType) {
        super();
        this.propertyName = propertyName;
        this.name = name;
        this.facetType = facetType;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPropertyName() {
        return propertyName;
    }
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public FacetType getFacetType() {
        return facetType;
    }

    public void setFacetType(FacetType facetType) {
        this.facetType = facetType;
    }

    public FacetValueBean addFacetValueBean(FacetValueBean facetValueBean) {
        return facetValueBeans.put(String.valueOf(facetValueBean.hashCode()), facetValueBean);
    }    
    
    public FacetValueBean getFacetValueBean(String facetValueId) {
        return facetValueBeans.get(facetValueId);
    }
    
    public Collection<FacetValueBean> getFacetValueBeans() {
        return facetValueBeans.values();
    }

    @Override
    public int hashCode() {
        return Math.abs(getName().hashCode());
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public boolean isMultiple() {
        return multiple;
    }    
   
}
