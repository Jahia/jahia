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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
            FacetBean facetBean, String facetValueName, BitSet mainQueryBits,
            ContainerQueryContext queryContext, ProcessingContext jParams)
            throws JahiaException;

    public abstract String[] getFacetFilterQueries(String filtersToApply)
            throws JahiaException;

    public abstract List<AppliedFacetFilters> getAppliedFacetFilters(
            String filtersToApply) throws JahiaException;

}
