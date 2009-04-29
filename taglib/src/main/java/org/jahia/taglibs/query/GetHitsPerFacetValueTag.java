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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.jahia.data.JahiaData;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.containers.ContainerQueryBean;
import org.jahia.services.search.facets.FacetBean;
import org.jahia.services.search.facets.FacetValueBean;
import org.jahia.services.search.facets.HitsPerFacetValueBean;
import org.jahia.services.search.facets.JahiaFacetingService;
import org.jahia.taglibs.AbstractJahiaTag;

@SuppressWarnings("serial")
public class GetHitsPerFacetValueTag extends AbstractJahiaTag {

    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
        .getLogger(GetHitsPerFacetValueTag.class);

    private String mainQueryBeanId;
    private String facetValueBeanId;
    private String facetBeanId;
    private String filterQueryParamName;
    private boolean display = true;
    private String hitsPerFacetBeanId;

    public int doStartTag() throws JspException {
        int eval = super.doStartTag();
        processFaceting();
        return eval;
    }

    public int doEndTag() throws JspException {
        int result = super.doEndTag();
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        setMainQueryBeanId(null);
        setFacetBeanId(null);
        setFacetValueBeanId(null);
        setFilterQueryParamName(null);
        setHitsPerFacetBeanId(null);
        setDisplay(true);
        return result;
    }

    private void processFaceting() {
        final ServletRequest request = pageContext.getRequest();
        final JahiaData jData = (JahiaData) request
            .getAttribute("org.jahia.data.JahiaData");
        final ProcessingContext jParams = jData.getProcessingContext();
        try {
            FacetBean facetBean = (FacetBean) pageContext
                .findAttribute(getFacetBeanId());
            ContainerQueryBean mainQueryBean = getMainQueryBeanId() != null ? (ContainerQueryBean) pageContext
                .findAttribute(getMainQueryBeanId())
                    : null;
            if (mainQueryBean != null) {
                StringBuffer buff = isDisplay() ? new StringBuffer() : null;
                BitSet mainQueryFilter = null;
                BitSet facetedFilter = null;
                if (mainQueryBean.getFilter() != null) {
                    mainQueryFilter = mainQueryBean.getFilter().bits();
                }
                if (mainQueryFilter == null
                        && mainQueryBean.getSorter() != null) {
                    List<Integer> result = mainQueryBean.getSorter().result();
                    mainQueryFilter = new BitSet();
                    for (Integer bitIndex : result) {
                        mainQueryFilter.set(bitIndex);
                    }
                }
                JahiaFacetingService facetingService = ServicesRegistry
                    .getInstance().getJahiaFacetingService();
                if (mainQueryBean.getQueryContext().getFacetedFilterResult() != null) {
                    facetedFilter = mainQueryBean.getQueryContext()
                        .getFacetedFilterResult();
                } else {
                    facetedFilter = mainQueryBean.getQueryContext()
                        .getFacetFilterQueryParamName() != null ? facetingService
                        .applyFacetFilters(null, jParams
                            .getParameter(mainQueryBean.getQueryContext()
                                .getFacetFilterQueryParamName()), mainQueryBean
                            .getQueryContext(), jParams)
                            : null;
                }
                if (mainQueryFilter != null && facetedFilter != null) {
                    mainQueryFilter = (BitSet) mainQueryFilter.clone();
                    mainQueryFilter.and(facetedFilter);
                } else if (mainQueryFilter == null) {
                    mainQueryFilter = facetedFilter;
                }
                List<HitsPerFacetValueBean> hitsPerFacetValue = new ArrayList<HitsPerFacetValueBean>();
                for (Map.Entry<FacetValueBean, Integer> hitsForFacetValue : facetingService.getHitsPerFacetValue(
                        facetBean,
                        getFacetValueBeanId() != null ? (List<FacetValueBean>) pageContext
                                .findAttribute(getFacetValueBeanId()) : null, mainQueryFilter,
                        mainQueryBean.getQueryContext(),
                        getFilterQueryParamName() != null ? jParams.getParameter(getFilterQueryParamName()) : null,
                        jParams).entrySet()) {

                    HitsPerFacetValueBean bean = new HitsPerFacetValueBean(
                        hitsForFacetValue.getKey(), hitsForFacetValue
                            .getValue());

                    if (isDisplay()) {
                        String drilldownUrl = getFilterQueryParamName() != null ? Functions
                            .getFacetDrillDownUrl(facetBean, bean
                                .getFacetValue(), getFilterQueryParamName())
                                : null;
                        if (drilldownUrl != null) {
                            buff.append("<a href=\"").append(drilldownUrl);
                        }
                        buff.append(bean.getFacetValue().getTitle());
                        if (drilldownUrl != null) {
                            buff.append("</a>");
                        }

                        buff.append("&nbsp;(").append(bean.getNumberOfHits())
                            .append(")<br/>");
                    }

                    hitsPerFacetValue.add(bean);
                }
                if (getHitsPerFacetBeanId() != null) {
                    pageContext.setAttribute(getHitsPerFacetBeanId(),
                        hitsPerFacetValue, PageContext.REQUEST_SCOPE);
                }
                if (buff != null) {
                    pageContext.getOut().print(buff.toString());
                }
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

    public void setFacetValueBeanId(String facetValueBeanId) {
        this.facetValueBeanId = facetValueBeanId;
    }

    public String getFacetValueBeanId() {
        return facetValueBeanId;
    }

    public void setFilterQueryParamName(String filterQueryParamName) {
        this.filterQueryParamName = filterQueryParamName;
    }

    public String getFilterQueryParamName() {
        return filterQueryParamName;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }

    public boolean isDisplay() {
        return display;
    }

    public void setHitsPerFacetBeanId(String hitsPerFacetBeanId) {
        this.hitsPerFacetBeanId = hitsPerFacetBeanId;
    }

    public String getHitsPerFacetBeanId() {
        return hitsPerFacetBeanId;
    }
}
