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

import java.util.List;

import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.search.facets.AppliedFacetFilters;
import org.jahia.services.search.facets.FacetBean;
import org.jahia.services.search.facets.FacetValueBean;

/**
 * Custom functions, which are exposed into the template scope.
 * 
 * @author Benjamin Papez
 */
public class Functions {

    public static boolean isFacetApplied(FacetBean facet, List<AppliedFacetFilters> appliedFacets) {
        boolean facetApplied = false;
        if (appliedFacets != null) {
            for (AppliedFacetFilters appliedFacetFilters : appliedFacets) {
                if (appliedFacetFilters.getFacetBean().equals(facet)) {
                    if (facet.isMultiple()) {
                        if (appliedFacetFilters.getFacetValueBeans().containsAll(facet.getFacetValueBeans())) {
                            facetApplied = true;
                        }
                    } else {
                        facetApplied = true;
                    }
                    break;
                }
            }
        }

        return facetApplied;
    }

    public static String getFacetDrillDownUrl(FacetBean facet,
            FacetValueBean facetValueBean, String filterQueryParamName)
            throws JahiaException {
        ProcessingContext jParams = Jahia.getThreadParamBean();
        String queryString = jParams.getQueryString();
        if (queryString != null) {
            int index = queryString.indexOf(filterQueryParamName);
            if (index > -1) {
                queryString = queryString.substring(0, index);
                index = jParams.getQueryString().indexOf("&", index);
                if (index > -1) {
                    queryString += jParams.getQueryString().substring(index);
                }
            }
        }

        StringBuffer drillDownUrl = new StringBuffer(jParams.getPage().getURL(
            jParams));
        drillDownUrl.append("?").append(
            queryString != null && queryString.length() > 0 ? queryString + "&"
                    : "").append(filterQueryParamName).append("=");
        Object forwardedFilter = jParams.getParameter(filterQueryParamName);
        if (forwardedFilter != null) {
            drillDownUrl.append(forwardedFilter);
        }
        drillDownUrl.append(facet.hashCode()).append("_").append(
            facetValueBean.hashCode()).append("_").append("\">");

        return drillDownUrl.toString();
    }

    public static String getDeleteFacetUrl(FacetBean facetBean,
            FacetValueBean facetValueBean, String filterQueryParamName)
            throws JahiaException {
        ProcessingContext jParams = Jahia.getThreadParamBean();
        StringBuffer deleteFacetUrl = new StringBuffer();
        String forwardedFilter = jParams.getParameter(filterQueryParamName);
        if (forwardedFilter != null) {
            String queryString = jParams.getQueryString();
            if (queryString != null) {
                int index = queryString.indexOf(filterQueryParamName);
                if (index > -1) {
                    queryString = queryString.substring(0, index);
                    index = jParams.getQueryString().indexOf("&", index);
                    if (index > -1) {
                        queryString += jParams.getQueryString().substring(index);
                    }
                }
            }
            
            deleteFacetUrl.append(jParams.getPage().getURL(jParams));
            
            String remainingFilter = forwardedFilter.replace(facetBean
                .hashCode()
                    + "_" + facetValueBean.hashCode() + "_", "");
            if (queryString != null && queryString.length() > 0
                    || remainingFilter.length() > 0) {
                deleteFacetUrl.append("?");
            }
            deleteFacetUrl.append(queryString != null
                    && queryString.length() > 0 ? queryString + "&" : "");
            if (remainingFilter.length() > 0) {
                deleteFacetUrl.append(filterQueryParamName).append("=").append(
                    remainingFilter);
            }
        }

        return deleteFacetUrl.toString();
    }
}
