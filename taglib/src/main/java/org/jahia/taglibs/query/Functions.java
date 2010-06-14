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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.jcr.nodetype.PropertyDefinition;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.KeyValue;
import org.apache.commons.collections.keyvalue.DefaultKeyValue;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.response.FacetField;

/**
 * Custom functions, which are exposed into the template scope.
 * 
 * @author Benjamin Papez
 */
public class Functions {

    private static final Logger logger = Logger.getLogger(Functions.class);    
    
    private static final String FACET_PARAM_DELIM = "###";
    private static final String FACET_DELIM = "|||";
    
    public static Map<String, List<KeyValue>> getAppliedFacetFilters(String filterString) {
        Map<String, List<KeyValue>> appliedFacetFilters = new LinkedHashMap<String, List<KeyValue>>();        
        if (!StringUtils.isEmpty(filterString)) {
            for (String filterInstance : filterString.split("\\|\\|\\|")) {
                String[] filterTokens = filterInstance.split(FACET_PARAM_DELIM);
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
                } else if (facetValueObj instanceof Map.Entry<?, ?>) {
                    Map.Entry<String, Long> facetCount = (Map.Entry<String, Long>) facetValueObj;
                    facetKey = facetCount.getKey();
                    facetValue = facetCount.getValue().toString();
                } else {
                    throw new IllegalArgumentException(
                            "Passed parameter is not of type org.apache.solr.client.solrj.response.FacetField.Count");
                }
            } catch (ClassCastException e) {
                throw new IllegalArgumentException(
                        "Passed parameter is not of type org.apache.solr.client.solrj.response.FacetField.Count", e);
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
    
    public static String getFacetDrillDownUrl(Object facetValueObj, String queryString) {
        StringBuilder builder = new StringBuilder();
        try {
            if (facetValueObj instanceof FacetField.Count) {
                FacetField.Count facetValue = (FacetField.Count) facetValueObj;
                builder.append(facetValue.getFacetField().getName()).append(FACET_PARAM_DELIM).append(facetValue.getName()).append(
                        FACET_PARAM_DELIM).append(facetValue.getAsFilterQuery());
            } else if (facetValueObj instanceof Map.Entry<?, ?>) {
                Map.Entry<String, Long> facetValue = (Map.Entry<String, Long>) facetValueObj;
                builder.append(facetValue.getKey()).append(FACET_PARAM_DELIM).append(facetValue.getKey()).append(FACET_PARAM_DELIM).append(facetValue.getKey());
            } else {
                throw new IllegalArgumentException(
                        "Passed parameter is not of type org.apache.solr.client.solrj.response.FacetField.Count");                
            }
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(
                    "Passed parameter is not of type org.apache.solr.client.solrj.response.FacetField.Count", e);
        }
        String facetValueFilter = builder.toString();
        if (!StringUtils.contains(queryString, facetValueFilter)) {
            builder = new StringBuilder(queryString.length() + facetValueFilter.length() + 1);
            builder.append(queryString).append(queryString.length() == 0 ? "" : FACET_DELIM).append(facetValueFilter);
        }

        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    public static String getDeleteFacetUrl(Object facetFilterObj, KeyValue facetValue, String queryString) {
        Map.Entry<String, List<KeyValue>> facetFilter;
        try {
            facetFilter = (Map.Entry<String, List<KeyValue>>)facetFilterObj;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Passed parameter is not of type java.util.Map.Entry", e);
        } 
        StringBuilder builder = new StringBuilder();
        builder.append(facetFilter.getKey()).append(FACET_PARAM_DELIM).append(facetValue.getKey())
                .append(FACET_PARAM_DELIM).append(facetValue.getValue());
        String facetValueFilter = builder.toString();
        int index = StringUtils.indexOf(queryString, facetValueFilter);
        if (index != -1) {
            queryString = queryString.replace(
                    (index >= FACET_DELIM.length()
                            && queryString.regionMatches(index - FACET_DELIM.length(), FACET_DELIM, 0, FACET_DELIM
                                    .length()) ? FACET_DELIM : "")
                            + facetValueFilter, "");
        }
        return queryString;
    }
    
    public static String encodeFacetUrlParam(String inputString) {
        if (StringUtils.isEmpty(inputString)) {
            return inputString;
        }
        // Compress the bytes
        byte[] output = new byte[2048];
        Deflater compresser = new Deflater();
        try {
            compresser.setInput(inputString.getBytes("UTF-8"));
            compresser.finish();
            int compressedDataLength = compresser.deflate(output);
            byte[] copy = new byte[compressedDataLength];
            System.arraycopy(output, 0, copy, 0,
                             Math.min(output.length, compressedDataLength));
            return Base64.encodeBase64URLSafeString(copy);            
        } catch (UnsupportedEncodingException e) {
            logger.warn("Not able to encode facet URL: " + inputString, e);
        }
        
        return inputString;
    }
    
    public static String decodeFacetUrlParam(String inputString) {
        if (StringUtils.isEmpty(inputString)) {
            return inputString;
        }        
        byte[] input = Base64.decodeBase64(inputString);
        // Decompress the bytes
        Inflater decompresser = new Inflater();
        decompresser.setInput(input, 0, input.length);
        byte[] result = new byte[2048];
        String outputString = "";
        try {
            int resultlength = decompresser.inflate(result);
            decompresser.end();        
            outputString = new String(result, 0, resultlength, "UTF-8");            
        } catch (DataFormatException e) {
            logger.warn("Not able to decode facet URL: " + inputString, e);
        } catch (UnsupportedEncodingException e) {
            logger.warn("Not able to decode facet URL: " + inputString, e);
        }
        return outputString;
    }
    
    public static String escapeIllegalJCRChars(String inputString) {
        return Text.escapeIllegalJcrChars(inputString);
    }     
}
