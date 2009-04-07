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

package org.jahia.services.search.facets;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class FacetBean implements Serializable {

    private static final long serialVersionUID = 827940036254584574L;

    public enum FacetType {
        FIELD_FACET, QUERY_FACET;
    }
    
    private String name;

    private String propertyName;
    private FacetType facetType;
    
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
   
}
