/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.taglibs.facet;

import org.apache.commons.collections.KeyValue;
import org.apache.commons.collections.keyvalue.DefaultKeyValue;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.RangeFacet;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.query.QueryResultWrapper;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.PropertyDefinition;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Custom facet functions, which are exposed into the template scope.
 *
 * @author Benjamin Papez
 */
public class Functions {

    private static final transient Logger logger = org.slf4j.LoggerFactory.getLogger(Functions.class);

    private static final String FACET_PARAM_DELIM = "###";
    private static final Pattern FACET_PARAM_DELIM_PATTERN = Pattern.compile(FACET_PARAM_DELIM);
    private static final String FACET_DELIM = "|||";
    private static final String FACET_NODE_TYPE = "jnt:facet";
    public static final String ESCAPED_FACET_DELIM = "\\|\\|\\|";
    private static final Pattern FILTER_STRING_PATTERN = Pattern.compile(ESCAPED_FACET_DELIM);

    /**
     * Get a list of applied facets
     *
     * @param filterString the already decoded filter String from the query parameter
     * @return a Map with the facet group as key and a KeyValue with the facet value as key and the query as value
     * @see org.jahia.taglibs.functions.Functions#decodeUrlParam(String)
     */
    public static Map<String, List<KeyValue>> getAppliedFacetFilters(String filterString) {
        Map<String, List<KeyValue>> appliedFacetFilters = new LinkedHashMap<String, List<KeyValue>>();
        if (!StringUtils.isEmpty(filterString)) {
            for (String filterInstance : FILTER_STRING_PATTERN.split(filterString)) {
                String[] filterTokens = FACET_PARAM_DELIM_PATTERN.split(filterInstance);
                if (filterTokens.length == 3) {
                    List<KeyValue> filterList = appliedFacetFilters.get(filterTokens[0]);
                    if (filterList == null) {
                        filterList = new ArrayList<KeyValue>();
                        appliedFacetFilters.put(filterTokens[0], filterList);
                    }
                    filterList.add(new DefaultKeyValue(filterTokens[1], filterTokens[2]));
                }
            }
        }
        return appliedFacetFilters;
    }

    /**
     * Check whether a facet is currently applied to the query
     *
     * @param facetName     the facet name to check
     * @param appliedFacets variable retrieved from {@link Functions#getAppliedFacetFilters(String)}
     * @param propDef       property definition if facet is a field/date facet
     * @return true if facet is applied otherwise false
     */
    public static boolean isFacetApplied(String facetName, Map<String, List<KeyValue>> appliedFacets,
                                         PropertyDefinition propDef) {
        boolean facetApplied = false;
        if (appliedFacets != null && appliedFacets.containsKey(facetName)) {
            if (propDef == null || (propDef != null && !propDef.isMultiple())) {
                facetApplied = true;
            }
        }
        return facetApplied;
    }

    /**
     * Check whether a facet value is currently applied to the query
     *
     * @param facetValueObj the facet value object to check (either FacetField.Count or Map.Entry<String, Long>)
     * @param appliedFacets variable retrieved from {@link Functions#getAppliedFacetFilters(String)}
     * @return true if facet value is applied otherwise false
     */
    public static boolean isFacetValueApplied(Object facetValueObj,
                                              Map<String, List<KeyValue>> appliedFacets) {
        boolean facetValueApplied = false;
        if (facetValueObj != null) {
            String facetKey = null;
            String facetValue = null;
            try {
                if (facetValueObj instanceof FacetField.Count) {
                    FacetField.Count facetCount = (FacetField.Count) facetValueObj;
                    facetKey = facetCount.getFacetField().getName();
                    facetValue = facetCount.getName();
                } else if (facetValueObj instanceof RangeFacet.Count) {            
                    RangeFacet.Count facetCount = (RangeFacet.Count) facetValueObj;
                    facetKey = facetCount.getRangeFacet().getName();
                    facetValue = facetCount.getValue().toString();
                } else if (facetValueObj instanceof Map.Entry<?, ?>) {
                    @SuppressWarnings("unchecked")
                    Map.Entry<String, Long> facetCount = (Map.Entry<String, Long>) facetValueObj;
                    facetKey = facetCount.getKey();
                    facetValue = facetCount.getValue().toString();
                } else {
                    throw new IllegalArgumentException(
                            "Passed parameter is not of a valid facet value type");
                }
            } catch (ClassCastException e) {
                throw new IllegalArgumentException(
                        "Passed parameter is not of a valid facet value type", e);
            }
            if (appliedFacets != null && appliedFacets.containsKey(facetKey)) {
                for (KeyValue facet : appliedFacets.get(facetKey)) {
                    if (facet.getKey().equals(facetValue)) {
                        facetValueApplied = true;
                        break;
                    }
                }
            }
        }
        return facetValueApplied;
    }

    /**
     * Create the drill down URL for a facet value
     *
     * @param facetValueObj either FacetField.Count or a Map.Entry for the facet value to create
     *                      the URL for applying this facet value
     * @param queryString   the current facet filter URL query parameter
     * @return the new facet filter URL query parameter
     */
    public static String getFacetDrillDownUrl(Object facetValueObj, String queryString) {
        StringBuilder builder = new StringBuilder();
        try {
            if (facetValueObj instanceof FacetField.Count) {
                FacetField.Count facetValue = (FacetField.Count) facetValueObj;
                builder.append(facetValue.getFacetField().getName()).append(FACET_PARAM_DELIM).append(facetValue.getName()).append(
                        FACET_PARAM_DELIM).append(facetValue.getAsFilterQuery());
            } else if (facetValueObj instanceof RangeFacet.Count) {
                RangeFacet.Count facetValue = (RangeFacet.Count) facetValueObj;
                builder.append(facetValue.getRangeFacet().getName()).append(FACET_PARAM_DELIM).append(facetValue.getValue()).append(
                        FACET_PARAM_DELIM).append(facetValue.getAsFilterQuery());                
            } else if (facetValueObj instanceof Map.Entry<?, ?>) {
                @SuppressWarnings("unchecked")
                Map.Entry<String, Long> facetValue = (Map.Entry<String, Long>) facetValueObj;
                builder.append(facetValue.getKey()).append(FACET_PARAM_DELIM).append(facetValue.getKey()).append(FACET_PARAM_DELIM).append(facetValue.getKey());
            } else {
                throw new IllegalArgumentException(
                        "Passed parameter is not of a valid facet value type");
            }
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(
                    "Passed parameter is not of a valid facet value type", e);
        }
        String facetValueFilter = builder.toString();
        if (!StringUtils.contains(queryString, facetValueFilter) && queryString != null) {
            builder = new StringBuilder(queryString.length() + facetValueFilter.length() + 1);
            builder.append(queryString).append(queryString.length() == 0 ? "" : FACET_DELIM).append(facetValueFilter);
        }

        return builder.toString();
    }

    /**
     * Create the URL to remove the given facet from the facet filter query parameter
     *
     * @param facetFilterObj one Map.Entry in the applied facet filter Map corresponding to the value in the next paramter
     * @param facetValue     the applied facet value, which need to be removed again
     * @param queryString    the current facet filter URL query parameter
     * @return the new facet filter URL query parameter
     * @deprecated Use {@link #getDeleteFacetUrl(org.apache.commons.collections.KeyValue, String)} instead
     */
    public static String getDeleteFacetUrl(Object facetFilterObj, KeyValue facetValue, String queryString) {
        return getDeleteFacetUrl(facetValue, queryString);
    }

    /**
     * Create the URL to remove the given facet from the facet filter query parameter
     *
     * @param facetValue  the applied facet value, which need to be removed again
     * @param queryString the current facet filter URL query parameter
     * @return the new facet filter URL query parameter
     */
    public static String getDeleteFacetUrl(KeyValue facetValue, String queryString) {
        // retrieve all facet Strings from query
        final String[] facets = FILTER_STRING_PATTERN.split(queryString);

        // rebuild a new query String omitting the facet String corresponding to the facet value we want to remove
        StringBuilder newQueryString = new StringBuilder(queryString.length());
        int index = 0;
        final int newFacetNumber = facets.length - 1;
        for (String facet : facets) {
            if (!facet.contains(facetValue.getValue().toString())) {
                newQueryString.append(facet);

                // only append the facet delim if we're not processing the last facet String
                if (index++ != newFacetNumber - 1) {
                    newQueryString.append(FACET_DELIM);
                }
            }
        }
        return newQueryString.toString();
    }

    /**
     * Check whether there is an unapplied facet existing in the query. Useful in order to determine
     * whether a title/label should be displayed or not.
     *
     * @param result        the Jahia QueryResultWrapper object holding query results
     * @param appliedFacets variable retrieved from {@link Functions#getAppliedFacetFilters(String)}
     * @return true if unapplied facet exists otherwise false
     */
    public static boolean isUnappliedFacetExisting(QueryResultWrapper result,
                                                   Map<String, List<KeyValue>> appliedFacets) {
        if (result.getFacetFields() != null) {
            for (FacetField facetField : result.getFacetFields()) {
                if (facetField.getValueCount() > 0) {
                    for (FacetField.Count facetCount : facetField.getValues()) {
                        if (!isFacetValueApplied(facetCount, appliedFacets)) {
                            return true;
                        }
                    }
                }
            }
        }
        if (result.getFacetDates() != null) {
            for (FacetField facetField : result.getFacetDates()) {
                if (facetField.getValueCount() > 0) {
                    for (FacetField.Count facetCount : facetField.getValues()) {
                        if (!isFacetValueApplied(facetCount, appliedFacets)) {
                            return true;
                        }
                    }
                }
            }
        }
        if (result.getFacetQuery() != null) {
            for (Map.Entry<String, Long> facetCount : result.getFacetQuery().entrySet()) {
                if (!isFacetValueApplied(facetCount, appliedFacets)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check whether there is an unapplied facet value existing in the facet. Useful in order to determine
     * whether a title/label should be displayed or not.
     *
     * @param facetField    the FacetField object holding all facet values for the facet field
     * @param appliedFacets variable retrieved from {@link Functions#getAppliedFacetFilters(String)}
     * @return true if unapplied facet value exists otherwise false
     */
    public static boolean isUnappliedFacetValueExisting(FacetField facetField, Map<String, List<KeyValue>> appliedFacets) {
        if (facetField.getValueCount() > 0) {
            for (FacetField.Count facetCount : facetField.getValues()) {
                if (!isFacetValueApplied(facetCount, appliedFacets)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Check whether there is an unapplied range facet value existing in the facet. Useful in order to determine
     * whether a title/label should be displayed or not.
     *
     * @param rangeFacet    the RangeFacet object holding all facet values for the facet field
     * @param appliedFacets variable retrieved from {@link Functions#getAppliedFacetFilters(String)}
     * @return true if unapplied range facet value exists otherwise false
     */
    public static boolean isUnappliedRangeFacetValueExisting(RangeFacet<?, ?> rangeFacet, Map<String, List<KeyValue>> appliedFacets) {
        for (RangeFacet.Count facetCount : rangeFacet.getCounts()) {
            if (!isFacetValueApplied(facetCount, appliedFacets)) {
                return true;
            }
        }
        return false;
    }    

    /**
     * Get the drill down prefix for a hierarchical facet value
     *
     * @param hierarchicalFacet the hierarchical facet value
     * @return the prefix
     */
    public static String getDrillDownPrefix(String hierarchicalFacet) {
        int pathStart = hierarchicalFacet.indexOf("/");
        if (pathStart > 0) {
            try {
                int i = Integer.parseInt(hierarchicalFacet.substring(0, pathStart));
                return (i + 1) + hierarchicalFacet.substring(pathStart);
            } catch (NumberFormatException e) {
            }
        }
        return hierarchicalFacet;
    }

    /**
     * Get the facet property definitions necessary to build the filter query
     *
     * @param facet the facet node
     * @return the list of property definitions
     */
    public static List<ExtendedPropertyDefinition> getPropertyDefinitions(JCRNodeWrapper facet) {
        Map<String, ExtendedPropertyDefinition> propDefMap = new LinkedHashMap<String, ExtendedPropertyDefinition>();
        try {
            ExtendedNodeType primaryNodeType = facet.getPrimaryNodeType();
            if (!primaryNodeType.isNodeType(FACET_NODE_TYPE)) {
                throw new IllegalArgumentException("The specified node is not a facet");
            }
            propDefMap.putAll(primaryNodeType.getDeclaredPropertyDefinitionsAsMap());

            for (ExtendedNodeType primarySuperType : primaryNodeType.getPrimarySupertypes()) {
                Map<String, ExtendedPropertyDefinition> superPropDefMap = primarySuperType.getDeclaredPropertyDefinitionsAsMap();
                if (FACET_NODE_TYPE.equals(primarySuperType.getName())) {
                    for (String propName : superPropDefMap.keySet()) {
                        propDefMap.remove(propName);
                    }
                    break;
                } else {
                    for (String propName : superPropDefMap.keySet()) {
                        if (!propDefMap.containsKey(propName)) {
                            propDefMap.put(propName, superPropDefMap.get(propName));
                        }
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<ExtendedPropertyDefinition>(propDefMap.values());
    }

    /**
     * Get the index prefixed path of a hierarchical facet root. For example, 1/sites/systemsite/categories.
     * @deprecated use getIndexPrefixedPath(facePath, workspace)
     * @param facetPath the hierarchical facet path
     * @return the index prefixed path
     */
    
    public static String getIndexPrefixedPath(final String facetPath) {
    	return getIndexPrefixedPath(facetPath, null);
    }
    /**
     * Get the index prefixed path of a hierarchical facet root. For example, 1/sites/systemsite/categories.

     * @param facetPath the hierarchical facet path
     * @param workspace current workspace (null = default workspace)
     * @return the index prefixed path
     */    
    public static String getIndexPrefixedPath(final String facetPath, final String workspace) {
        try {
        
            return JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(JCRSessionFactory.getInstance().getCurrentUser(), workspace, JCRSessionFactory.getInstance().getCurrentLocale(), new JCRCallback<String>() {
                public String doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    int prefix = 1; 
                    JCRNodeWrapper node = session.getNode(facetPath);
                    String typeName = node.getPrimaryNodeTypeName();
                    while (typeName.equals(node.getParent().getPrimaryNodeTypeName())) {
                        prefix++;
                        node = node.getParent();
                    }
                    return prefix + facetPath;
                }
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return facetPath;
    }

}
