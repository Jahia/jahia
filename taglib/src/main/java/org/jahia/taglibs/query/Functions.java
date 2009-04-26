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
