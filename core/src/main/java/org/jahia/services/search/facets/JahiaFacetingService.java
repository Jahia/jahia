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

import java.util.BitSet;
import java.util.List;
import java.util.Map;

import org.jahia.data.containers.ContainerFilters;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.JahiaService;
import org.jahia.services.containers.ContainerQueryContext;

/**
 * Faceting Service.
 */
public abstract class JahiaFacetingService extends JahiaService {
    public abstract FacetBean createFacetFilter(String facetName,
            String propertyName, String facetNameForNoValue,
            ContainerQueryContext queryContext, ProcessingContext jParams,
            List<FacetValueBean> createdFacets) throws JahiaException;

    public abstract FacetBean createFacetFilter(String facetName,
            String facetValueName, Object[] dynamicNameArguments,
            ContainerFilters containerFilters,
            ContainerQueryContext queryContext, ProcessingContext jParams,
            List<FacetValueBean> createdFacets) throws JahiaException;

    public abstract BitSet applyFacetFilters(BitSet mainQueryBits,
            String filtersToApply, ContainerQueryContext queryContext,
            ProcessingContext jParams) throws JahiaException;

    public abstract Map<FacetValueBean, Integer> getHitsPerFacetValue(
            FacetBean facetBean, List<FacetValueBean> facetValues, BitSet mainQueryBits,
            ContainerQueryContext queryContext, String appliedFilters, ProcessingContext jParams)
            throws JahiaException;

    public abstract String[] getFacetFilterQueries(String filtersToApply)
            throws JahiaException;

    public abstract List<AppliedFacetFilters> getAppliedFacetFilters(
            String filtersToApply) throws JahiaException;

}
