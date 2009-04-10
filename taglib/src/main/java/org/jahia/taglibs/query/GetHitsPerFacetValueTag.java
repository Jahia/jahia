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

import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.jahia.data.JahiaData;
import org.jahia.data.fields.ExpressionMarker;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.containers.ContainerQueryBean;
import org.jahia.services.search.facets.FacetBean;
import org.jahia.services.search.facets.FacetValueBean;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.utils.i18n.ResourceBundleMarker;

@SuppressWarnings("serial")
public class GetHitsPerFacetValueTag extends AbstractJahiaTag {

    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(GetHitsPerFacetValueTag.class);

    private String mainQueryBeanId;
    private String facetValueName;
    private String facetBeanId;
    private String filterQueryParamName;

    public int doStartTag() throws JspException {
        int eval = super.doStartTag();
        processFaceting();
        return eval;
    }

    private String getExpandedValue(String val, ProcessingContext context) throws JahiaException {
        ResourceBundleMarker resMarker = ResourceBundleMarker.parseMarkerValue(val);
        if (resMarker == null) {
            // expression marker
            ExpressionMarker exprMarker = ExpressionMarker.parseMarkerValue(val, context);
            if (exprMarker != null) {
                try {
                    val = exprMarker.getValue();
                } catch (Exception t) {
                }
            }

            if (val == null) {
                val = "";
            }
        } else {
            val = resMarker.getValue(context.getLocale());

            if (val == null) {
                val = "";
            }
        }
        return val;
    }

    public int doEndTag() throws JspException {
        int result = super.doEndTag();
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        setMainQueryBeanId(null);
        setFacetBeanId(null);
        setFacetValueName(null);       
        setFilterQueryParamName(null);
        return result;
    }

    private void processFaceting() {
        final ServletRequest request = pageContext.getRequest();
        final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        final ProcessingContext jParams = jData.getProcessingContext();
        final JspWriter out = pageContext.getOut();
        try {
            FacetBean facetBean = (FacetBean) pageContext.findAttribute(getFacetBeanId());
            ContainerQueryBean mainQueryBean = getMainQueryBeanId() != null ? (ContainerQueryBean) pageContext
                    .findAttribute(getMainQueryBeanId()) : null;
            if (mainQueryBean != null) {
                StringBuffer buff = new StringBuffer();
                String queryString = jParams.getQueryString();
                if (queryString != null) {
                    int index = queryString.indexOf(getFilterQueryParamName());
                    if (index > -1) {
                        queryString = queryString.substring(0, index);
                        index = jParams.getQueryString().indexOf("&", index);
                        if (index > -1) {
                            queryString += jParams.getQueryString().substring(
                                    index);
                        }
                    }
                }
                for (Map.Entry<FacetValueBean, Integer> hitsForFacetValue : ServicesRegistry.getInstance()
                        .getJahiaFacetingService().getHitsPerFacetValue(facetBean, getFacetValueName(),
                                (mainQueryBean.getFilter() != null ? mainQueryBean.getFilter().bits() : null), mainQueryBean.getQueryContext(), jParams).entrySet()) {
                    int matchingHits = hitsForFacetValue.getValue();
                    if (getFilterQueryParamName() != null) {
                        buff.append("<a href=\"").append(jParams.getPage().getURL(jParams)).append("?").append(queryString != null ? queryString + "&": "").append(
                                getFilterQueryParamName()).append("=");
                        Object forwardedFilter = jParams.getParameter(getFilterQueryParamName());
                        if (forwardedFilter != null) {
                            buff.append(forwardedFilter);
                        }
                        buff.append(facetBean.hashCode()).append("_").append(hitsForFacetValue.getKey().hashCode())
                                .append("_").append("\">");
                    }
                    buff.append(getExpandedValue(hitsForFacetValue.getKey().getValue(), jParams));
                    if (getFilterQueryParamName() != null) {
                        buff.append("</a>");
                    }

                    buff.append("&nbsp;(").append(matchingHits).append(")<br/>");
                }
                out.print(buff.toString());
            }
            if (getBodyContent() != null) {
                bodyContent.writeOut(bodyContent.getEnclosingWriter());
            }
        } catch (Exception e) {
            logger.error("Error", e);
        }

    }

    public void setMainQueryBeanId(String mainQueryBeanId) {
        this.mainQueryBeanId = mainQueryBeanId;
    }

    public String getMainQueryBeanId() {
        return mainQueryBeanId;
    }

    public void setFacetBeanId(String facetBeanId) {
        this.facetBeanId = facetBeanId;
    }

    public String getFacetBeanId() {
        return facetBeanId;
    }

    public void setFacetValueName(String facetValueName) {
        this.facetValueName = facetValueName;
    }

    public String getFacetValueName() {
        return facetValueName;
    }

    public void setFilterQueryParamName(String filterQueryParamName) {
        this.filterQueryParamName = filterQueryParamName;
    }

    public String getFilterQueryParamName() {
        return filterQueryParamName;
    }
}
