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

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import org.jahia.data.JahiaData;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.search.facets.AppliedFacetFilters;
import org.jahia.services.search.facets.FacetValueBean;
import org.jahia.taglibs.AbstractJahiaTag;

@SuppressWarnings("serial")
public class GetAppliedFacetFiltersTag extends AbstractJahiaTag {

    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
        .getLogger(GetHitsPerFacetValueTag.class);

    private String filterQueryParamName;
    private String appliedFacetsId;
    private boolean display = true;
    private String removeDisplayResource;

    public int doStartTag() throws JspException {
        int eval = super.doStartTag();
        getFacetFilters();
        return eval;
    }

    public int doEndTag() throws JspException {
        int result = super.doEndTag();
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.

        setFilterQueryParamName(null);
        setAppliedFacetsId(null);
        setDisplay(true);
        setRemoveDisplayResource(null);
        return result;
    }

    private void getFacetFilters() {
        final ServletRequest request = pageContext.getRequest();
        final JahiaData jData = (JahiaData) request
            .getAttribute("org.jahia.data.JahiaData");
        final ProcessingContext jParams = jData.getProcessingContext();
        final JspWriter out = pageContext.getOut();
        StringBuffer buff = new StringBuffer();
        try {
            List<AppliedFacetFilters> allAppliedFacetFilters = ServicesRegistry
                .getInstance().getJahiaFacetingService()
                .getAppliedFacetFilters(
                    jParams.getParameter(getFilterQueryParamName()));
            for (AppliedFacetFilters appliedFacetFilters : allAppliedFacetFilters) {
                for (FacetValueBean facetValueBean : appliedFacetFilters
                    .getFacetValueBeans()) {
                    buff.append(facetValueBean.getTitle());
                    buff.append("&nbsp;<a href=\"").append(
                        Functions.getDeleteFacetUrl(appliedFacetFilters
                            .getFacetBean(), facetValueBean,
                            getFilterQueryParamName()));
                    buff
                        .append("\">")
                        .append(
                            getRemoveDisplayResource() != null ? getRemoveDisplayResource()
                                    : "remove").append("</a>&nbsp;");
                }
            }
            out.print(buff.toString());
            if (getAppliedFacetsId() != null) {
                pageContext.setAttribute(getAppliedFacetsId(),
                    allAppliedFacetFilters, PageContext.REQUEST_SCOPE);
            }
            if (getBodyContent() != null) {
                bodyContent.writeOut(bodyContent.getEnclosingWriter());
            }
        } catch (Exception e) {
            logger.error("Error", e);
        }

    }

    public void setFilterQueryParamName(String filterQueryParamName) {
        this.filterQueryParamName = filterQueryParamName;
    }

    public String getFilterQueryParamName() {
        return filterQueryParamName;
    }

    public void setAppliedFacetsId(String appliedFacetsId) {
        this.appliedFacetsId = appliedFacetsId;
    }

    public String getAppliedFacetsId() {
        return appliedFacetsId;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }

    public boolean isDisplay() {
        return display;
    }

    public void setRemoveDisplayResource(String removeDisplayResource) {
        this.removeDisplayResource = removeDisplayResource;
    }

    public String getRemoveDisplayResource() {
        return removeDisplayResource;
    }
}
