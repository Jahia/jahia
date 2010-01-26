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

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;

import org.jahia.data.JahiaData;
import org.jahia.params.ProcessingContext;
import org.jahia.services.search.facets.FacetBean;
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
        try {
            FacetBean facetBean = (FacetBean) pageContext
                .findAttribute(getFacetBeanId());
            // TODO: implement new facet processing
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
