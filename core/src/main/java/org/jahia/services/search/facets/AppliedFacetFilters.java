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
import java.util.ArrayList;
import java.util.List;

public class AppliedFacetFilters implements Serializable {

    private static final long serialVersionUID = -8809327269707046435L;
    
    private FacetBean facetBean;
    private List<FacetValueBean> facetValueBeans = new ArrayList<FacetValueBean>();

    public AppliedFacetFilters(FacetBean facetBean) {
        super();
        this.facetBean = facetBean;
    }

    public FacetBean getFacetBean() {
        return facetBean;
    }

    public boolean addFacetValueBean(FacetValueBean facetValueBean) {
        return facetValueBeans.add(facetValueBean);
    }

    public List<FacetValueBean> getFacetValueBeans() {
        return facetValueBeans;
    }
    
    @Override
    public int hashCode() {
        return facetBean.hashCode();
    }        
}
