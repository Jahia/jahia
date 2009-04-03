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

import java.util.BitSet;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.apache.lucene.queryParser.QueryParser;
import org.jahia.data.JahiaData;
import org.jahia.data.containers.ContainerFilterInterface;
import org.jahia.data.containers.ContainerSearcherToFilterAdapter;
import org.jahia.data.fields.ExpressionMarker;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.data.search.JahiaSearchResult;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.query.qom.QueryModelTools;
import org.jahia.services.containers.ContainerQueryBean;
import org.jahia.services.containers.ContainerQueryContext;
import org.jahia.services.search.ContainerSearcher;
import org.jahia.utils.i18n.ResourceBundleMarker;

@SuppressWarnings("serial")
public class GetHitsPerFacetTag extends QueryDefinitionTag {

    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(GetHitsPerFacetTag.class);

    private String queryBeanID;
    private String facetQueryBeanID;    
    private String facetPropertyName;
    private String facetTitle;    
    private ContainerQueryBean queryBean;
    private ContainerQueryBean facetQueryBean;    
    private JahiaFieldDefinition fieldDef;

    public String getQueryBeanID() {
        return queryBeanID;
    }

    public void setQueryBeanID(String queryBeanID) {
        this.queryBeanID = queryBeanID;
    }

    public int doStartTag() throws JspException {
        int eval = super.doStartTag();

        if (getQueryBeanID() != null && getQueryBeanID().length() > 0) {
            queryBean = (ContainerQueryBean) pageContext
                    .findAttribute(getQueryBeanID());
        }

        return eval;
    }

    // Body is evaluated one time, so just writes it on standard output
    public int doAfterBody() {
        return EVAL_PAGE;
    }

    private String getExpandedValue(String val, ProcessingContext context)
            throws JahiaException {
        ResourceBundleMarker resMarker = ResourceBundleMarker
                .parseMarkerValue(val);
        if (resMarker == null) {
            // expression marker
            ExpressionMarker exprMarker = ExpressionMarker.parseMarkerValue(
                    val, context);
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
        processFaceting();
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        queryBeanID = null;
        queryBean = null;
        facetPropertyName = null;
        facetTitle = null;
        facetQueryBeanID = null;
        facetQueryBean = null;
        return result;
    }

    public String getFacetPropertyName() {
        return facetPropertyName;
    }

    public void setFacetPropertyName(String facetPropertyName) {
        this.facetPropertyName = facetPropertyName;
    }

    public ContainerQueryBean getQueryBean() {
        return queryBean;
    }
    
    private void processFaceting () {
        final ServletRequest request = pageContext.getRequest();
        final JahiaData jData = (JahiaData) request
                .getAttribute("org.jahia.data.JahiaData");
        final ProcessingContext jParams = jData.getProcessingContext();
        final JspWriter out = pageContext.getOut();
        try {
            if (getFacetPropertyName() != null && getQueryBean() != null) {
                fieldDef = QueryModelTools.getFieldDefinitionForPropertyName(
                        getFacetPropertyName(), getQueryBean()
                                .getQueryContext(), jParams);
                if (fieldDef != null) {
                    ContainerQueryContext queryContext = getQueryBean()
                            .getQueryContext();

                    List<String> definitions = queryContext
                            .getContainerDefinitionsIncludingType(false);
                    String[] definitionNames = new String[] {};
                    if (definitions != null) {
                        definitionNames = definitions.toArray(definitionNames);
                    }

                    Integer[] siteIds = new Integer[] {};
                    if (queryContext.getSiteIDs() != null) {
                        siteIds = queryContext.getSiteIDs().toArray(siteIds);
                    }
                    ContainerSearcher searcher = new ContainerSearcher(siteIds,
                            definitionNames, "", jParams.getEntryLoadRequest());

                    searcher.setCacheQueryResultsInBackend(true);
                    String fieldName = QueryModelTools
                            .getFieldNameForSearchEngine(
                                    getFacetPropertyName(), false,
                                    getQueryBean().getQueryContext(), jParams,
                                    QueryModelTools.FACETING_TYPE);

                    for (String value : fieldDef.getPropertyDefinition()
                            .getValueConstraints()) {
                        JahiaSearchResult result = searcher.search(fieldName
                                + ":(\"" + QueryParser.escape(value) + "\")",
                                jParams);
                        
                        int matchingHits = result.getHitCount();
                        if (getQueryBean().getFilter() != null) {
                            BitSet queryBitSet = (BitSet)getQueryBean().getFilter().bits().clone();
                            queryBitSet.and(result.bits());
                            matchingHits = queryBitSet.cardinality();
                        }
                        
                        out.print(getExpandedValue(value, jParams) + "&nbsp;("
                                + matchingHits + ")<br/>");
                    }
                }
            } else if (getFacetQueryBean() != null && getFacetQueryBean().getFilter() != null) {
                BitSet facetBits = getFacetQueryBean().getFilter().bits();
                if (facetBits == null) {
                    for (ContainerFilterInterface containerFilter : getFacetQueryBean().getFilter().getContainerFilters()) {
                        if (containerFilter instanceof ContainerSearcherToFilterAdapter) {
                            ((ContainerSearcherToFilterAdapter) containerFilter)
                                    .getSearcher()
                                    .setCacheQueryResultsInBackend(true);
                        }
                    }
                    facetBits = getFacetQueryBean().getFilter().doFilter();
                }
                int matchingHits = facetBits.cardinality();
                if (getQueryBean().getFilter() != null) {
                    BitSet queryBitSet = (BitSet)getQueryBean().getFilter().bits().clone();
                    queryBitSet.and(facetBits);
                    matchingHits = queryBitSet.cardinality();
                }
                
                out.print(getFacetTitle() + "&nbsp;("
                        + matchingHits + ")<br/>");
            }
            if (getBodyContent() != null) {
                bodyContent.writeOut(bodyContent.getEnclosingWriter());
            }
        } catch (Exception e) {
            logger.error("Error", e);
        }

    }

    public String getFacetTitle() {
        return facetTitle;
    }

    public void setFacetTitle(String facetTitle) {
        this.facetTitle = facetTitle;
    }

    public String getFacetQueryBeanID() {
        return facetQueryBeanID;
    }

    public void setFacetQueryBeanID(String facetQueryBeanID) {
        this.facetQueryBeanID = facetQueryBeanID;
    }

    public ContainerQueryBean getFacetQueryBean() {
        return facetQueryBean;
    }

    public void setFacetQueryBean(ContainerQueryBean facetQueryBean) {
        this.facetQueryBean = facetQueryBean;
    }

}
