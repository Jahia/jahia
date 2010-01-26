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
package org.jahia.taglibs.query;

import java.util.Collections;
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
        final JspWriter out = pageContext.getOut();
        StringBuffer buff = new StringBuffer();
        try {
            List<AppliedFacetFilters> allAppliedFacetFilters = Collections.emptyList();
            //TODO implement new: ServicesRegistry.getInstance().getJahiaFacetingService().getAppliedFacetFilters(jParams.getParameter(getFilterQueryParamName()));
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
