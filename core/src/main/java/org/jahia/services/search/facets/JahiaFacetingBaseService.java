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
package org.jahia.services.search.facets;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.queryParser.QueryParser;
import org.jahia.data.containers.ContainerFilterInterface;
import org.jahia.data.containers.ContainerFilters;
import org.jahia.data.containers.ContainerSearcherToFilterAdapter;
import org.jahia.data.fields.FieldTypes;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.data.search.JahiaSearchResult;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.params.ProcessingContext;
import org.jahia.query.qom.QueryModelTools;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.categories.Category;
import org.jahia.services.containers.ContainerQueryContext;
import org.jahia.services.search.ContainerSearcher;
import org.jahia.services.search.JahiaSearchConstant;
import org.jahia.utils.LanguageCodeConverters;

public class JahiaFacetingBaseService extends JahiaFacetingService {

    public static final String JAHIA_FACETS_CACHE = "JahiaFacetsCache";

    private CacheService cacheService = null;

    private Cache<String, FacetBean> facetBeansCache = null;

    /**
     * The unique instance of this service *
     */
    protected static JahiaFacetingBaseService theObject;

    /**
     * Returns the unique instance of this service.
     */
    public static JahiaFacetingService getInstance() {
        if (theObject == null) {
            synchronized (JahiaFacetingBaseService.class) {
                if (theObject == null) {
                    theObject = new JahiaFacetingBaseService();
                }
            }
        }
        return theObject;
    }

    @Override
    public void start() throws JahiaInitializationException {
        facetBeansCache = cacheService.createCacheInstance(JAHIA_FACETS_CACHE);
    }

    @Override
    public void stop() throws JahiaException {
        // TODO Auto-generated method stub

    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    private FacetBean getFacetBeanFromCache(String facetName) {
        return facetBeansCache.get(facetName);
    }

    private void addFacetBeanToCache(FacetBean facet) {
        facetBeansCache.put(String.valueOf(facet.hashCode()), facet);
    }

    private FacetBean getFacetBean(String facetNameId) {
        return getFacetBeanFromCache(facetNameId);
    }

    private FacetBean getFacetBeanByName(String facetName) {
        FacetBean facetBean = getFacetBeanFromCache(String.valueOf(Math.abs(facetName.hashCode())));
        if (facetBean == null) {
            facetBean = new FacetBean(facetName, FacetBean.FacetType.QUERY_FACET);
            addFacetBeanToCache(facetBean);
        }
        return facetBean;
    }

    private FacetBean getFacetBeanByName(String facetName, String propertyName, JahiaFieldDefinition fieldDef) {
        if (facetName == null || facetName.length() == 0) {
            facetName = propertyName;
        }
        FacetBean facetBean = getFacetBeanFromCache(String.valueOf(Math.abs(facetName.hashCode())));
        if (facetBean == null) {
            facetBean = new FacetBean(facetName, propertyName,
                    fieldDef.getType() == FieldTypes.CATEGORY ? FacetBean.FacetType.CATEGORY_FACET
                            : FacetBean.FacetType.FIELD_FACET);
            addFacetBeanToCache(facetBean);
        }
        return facetBean;
    }

    @Override
    public FacetBean createFacetFilter(String facetName, String propertyName, String facetNameForNoValue,
            ContainerQueryContext queryContext, ProcessingContext jParams, List<FacetValueBean> createdFacets) throws JahiaException {
        JahiaFieldDefinition fieldDef = QueryModelTools.getFieldDefinitionForPropertyName(propertyName, queryContext
                .getContainerDefinitionNames(), jParams);        
        FacetBean facetBean = null;

        if (fieldDef != null) {
            facetBean = getFacetBeanByName(facetName, propertyName, fieldDef);
            
            facetBean.setMultiple(fieldDef.getPropertyDefinition().isMultiple());
            
            String fieldName = QueryModelTools.getFieldNameForSearchEngine(propertyName, false, queryContext
                    .getContainerDefinitionNames(), jParams, QueryModelTools.FACETING_TYPE);
            String languageCode = LanguageCodeConverters.localeToLanguageTag(jParams.getLocale());
            if (fieldDef.getType() == FieldTypes.CATEGORY) {
                String rootCategory = fieldDef.getItemDefinition().getSelectorOptions().get("root");
                Category startCategory = rootCategory != null ? Category
                        .getCategory(rootCategory, jParams.getUser()) : null;
                for (Category category : startCategory != null ? startCategory
                        .getChildCategories(jParams.getUser()) : Category
                        .getRootCategories(jParams.getUser())) {
                    String categoryTitle = category.getTitle(jParams
                            .getLocale());
                    if (categoryTitle == null || categoryTitle.length() == 0) {
                        categoryTitle = category.getKey();
                    }
                    String filterQuery = fieldName + ":(\""
                            + QueryParser.escape(categoryTitle) + "\")";
                    FacetValueBean facetValueBean = new FacetValueBean(
                            facetBean, category.getKey(),
                            new Object[] { category }, filterQuery,
                            languageCode);
                    if (createdFacets != null) {
                        createdFacets.add(facetValueBean);
                    }
                    facetBean.addFacetValueBean(facetValueBean);
                }
            } else {
                for (String value : fieldDef.getPropertyDefinition().getValueConstraints()) {
                    String filterQuery = fieldName + ":(\"" + QueryParser.escape(value) + "\")";
                    FacetValueBean facetValueBean = new FacetValueBean(facetBean, value, null, filterQuery, languageCode);
                    if (createdFacets != null) {
                        createdFacets.add(facetValueBean);
                    }                    
                    facetBean.addFacetValueBean(facetValueBean);
                }
            }
            if (facetNameForNoValue != null && facetNameForNoValue.trim().length() > 0) {
                String filterQuery = fieldName.replace(JahiaSearchConstant.CONTAINER_FIELD_FACET_PREFIX,
                        JahiaSearchConstant.CONTAINER_EMPTY_FIELD_FACET_PREFIX)
                        + ":(no)";
                FacetValueBean facetValueBean = new FacetValueBean(facetBean, facetNameForNoValue, null, filterQuery, languageCode);
                if (createdFacets != null) {
                    createdFacets.add(facetValueBean);
                }
                facetBean.addFacetValueBean(facetValueBean);
            }
        }
        return facetBean;
    }

    @Override
    public FacetBean createFacetFilter(String facetName, String facetValueName, Object[] dynamicNameArguments, ContainerFilters containerFilters,
            ContainerQueryContext queryContext, ProcessingContext jParams, List<FacetValueBean> createdFacets) throws JahiaException {
        FacetBean facetBean = getFacetBeanByName(facetName);
        String query = "";
        String languageCode = LanguageCodeConverters.localeToLanguageTag(jParams.getLocale());        
        for (ContainerFilterInterface filter : containerFilters.getContainerFilters()) {
            if (filter instanceof ContainerSearcherToFilterAdapter) {
                if (query.length() > 0) {
                    query += " ";
                }
                query += ((ContainerSearcherToFilterAdapter) filter).getSearcher().getQuery();
            }
        }
        if (query.length() == 0) {
            query = containerFilters.getQuery();
        }
        FacetValueBean facetValueBean = new FacetValueBean(facetBean, facetValueName, dynamicNameArguments, query, languageCode);
        if (createdFacets != null) {
            createdFacets.add(facetValueBean);
        }        
        facetBean.addFacetValueBean(facetValueBean);

        return facetBean;
    }

    @Override
    public BitSet applyFacetFilters(BitSet mainQueryBits, String filtersToApply, ContainerQueryContext queryContext,
            ProcessingContext jParams) throws JahiaException {
        if (filtersToApply != null) {
            String[] filterIds = filtersToApply.split("_");
            if (filterIds.length >= 2) {
                String[] queries = new String[filterIds.length / 2];
                FacetBean facetBean = null;
                int i = 0;
                for (String filter : filterIds) {
                    if (facetBean == null) {
                        facetBean = getFacetBean(filter);
                    } else {
                        FacetValueBean facetValueBean = facetBean
                                .getFacetValueBean(filter);
                        if (facetValueBean != null) {
                            queries[i] = facetValueBean.getFilterQuery();
                            i++;
                        }
                        facetBean = null;
                    }
                }
                JahiaSearchResult sr = getSearcher(queryContext, jParams)
                        .search(queries, jParams);

                if (mainQueryBits == null) {
                    mainQueryBits = (BitSet) sr.bits().clone();
                } else {
                    mainQueryBits = (BitSet) mainQueryBits.clone();
                    mainQueryBits.and(sr.bits());
                }
            }
        }

        return mainQueryBits;
    }

    @Override
    public String[] getFacetFilterQueries(String filtersToApply) throws JahiaException {
        String[] queries = null;
        if (filtersToApply != null) {
            String[] filterIds = filtersToApply.split("_");
            queries = new String[filterIds.length / 2];
            FacetBean facetBean = null;
            int i = 0;
            for (String filter : filterIds) {
                if (facetBean == null) {
                    facetBean = getFacetBean(filter);
                } else {
                    FacetValueBean facetValueBean = facetBean.getFacetValueBean(filter);
                    if (facetValueBean != null) {
                        queries[i] = facetValueBean.getFilterQuery();
                        i++;
                    }
                    facetBean = null;
                }
            }
        }

        return queries;
    }

    @Override
    public List<AppliedFacetFilters> getAppliedFacetFilters(String filtersToApply) throws JahiaException {
        List<AppliedFacetFilters> appliedFacetFilters = new ArrayList<AppliedFacetFilters>();
        if (filtersToApply != null) {
            String[] filterIds = filtersToApply.split("_");
            AppliedFacetFilters currentFacetFilters = null;
            for (String filter : filterIds) {
                if (currentFacetFilters == null) {
                    FacetBean facetBean = getFacetBean(filter);
                    for (AppliedFacetFilters facetFilters : appliedFacetFilters) {
                        if (facetFilters.getFacetBean().equals(facetBean)) {
                            currentFacetFilters = facetFilters;
                            break;
                        }
                    }
                    if (currentFacetFilters == null) {
                        currentFacetFilters = new AppliedFacetFilters(facetBean);
                        appliedFacetFilters.add(currentFacetFilters);
                    }
                } else {
                    FacetValueBean facetValueBean = currentFacetFilters.getFacetBean().getFacetValueBean(filter);
                    if (facetValueBean != null) {
                        currentFacetFilters.addFacetValueBean(facetValueBean);
                    }
                    currentFacetFilters = null;
                }
            }
        }

        return appliedFacetFilters;
    }

    @Override
    public Map<FacetValueBean, Integer> getHitsPerFacetValue(
            FacetBean facetBean, List<FacetValueBean> facetValues,
            BitSet mainQueryBits, ContainerQueryContext queryContext,
            String appliedFilters, ProcessingContext jParams) throws JahiaException {
        Map<FacetValueBean, Integer> result = new LinkedHashMap<FacetValueBean, Integer>();

        ContainerSearcher searcher = getSearcher(queryContext, jParams);

        for (FacetValueBean facetValue : facetValues != null ? facetValues : facetBean.getFacetValueBeans()) {
            if (appliedFilters == null || !appliedFilters.contains(facetBean.hashCode() + "_" + facetValue.hashCode())) {
                JahiaSearchResult sr = searcher.search(facetValue.getFilterQuery(), jParams);
                int matchingHits = sr.getHitCount();
                if (mainQueryBits != null) {
                    BitSet queryBitSet = (BitSet) mainQueryBits.clone();
                    queryBitSet.and(sr.bits());
                    matchingHits = queryBitSet.cardinality();
                }
                result.put(facetValue, matchingHits);
            }
        }
        return result;
    }

    private ContainerSearcher getSearcher(ContainerQueryContext queryContext, ProcessingContext jParams) {
        List<String> definitions = queryContext.getContainerDefinitionsIncludingType(false);
        String[] definitionNames = new String[] {};
        if (definitions != null) {
            definitionNames = definitions.toArray(definitionNames);
        }

        Integer[] siteIds = null;
        if (queryContext.getSiteIDs() != null && queryContext.getSiteIDs().size() > 0) {
            siteIds = queryContext.getSiteIDs().toArray(new Integer[queryContext.getSiteIDs().size()]);
        } else {
            siteIds = new Integer[]{jParams.getSiteID()};
        }

        ContainerSearcher searcher = new ContainerSearcher(siteIds, definitionNames, "", jParams.getEntryLoadRequest());
        searcher.setCacheQueryResultsInBackend(true);

        return searcher;
    }

}
