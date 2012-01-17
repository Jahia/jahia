/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.apache.jackrabbit.core.query.lucene;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.util.DocIdBitSet;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.util.OpenBitSetDISI;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.search.facets.SimpleJahiaJcrFacets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.NamespaceException;
import javax.jcr.RepositoryException;
import javax.jcr.query.qom.PropertyValue;
import javax.jcr.query.qom.Selector;
import java.io.IOException;
import java.util.*;

/**
 * Handle facet queries
 */
public class FacetHandler {
    /**
     * The logger instance for this class
     */
    private static final Logger log = LoggerFactory.getLogger(FacetHandler.class);
    private static final String RANGEFROM_INCLUSIVE_PREFIX = ":[";
    private static final String RANGEFROM_EXCLUSIVE_PREFIX = ":{";

    /**
     * The name of the facet function without prefix but with left parenthesis.
     */
    private static final String FACET_FUNC_LPAR = "facet(";

    /**
     * The start Name for the rep:facet function: rep:facet(
     */
    private static final Name REP_FACET_LPAR = NameFactoryImpl.getInstance().create(
            Name.NS_REP_URI, FACET_FUNC_LPAR);

    private static final String FIELD_SPECIFIC_PREFIX = "f.";



    // Facet stuff
    boolean facetsResolved = false;
    private Map<String, Long> _facetQuery = null;
    private List<FacetField> _facetFields = null;
    private List<FacetField> _limitingFacets = null;
    private List<FacetField> _facetDates = null;


    /**
     * Component context of the current session
     */
    protected final SessionImpl session;


    protected Selector selector;
    /**
     * The columns to select.
     */
    protected final Map<String, PropertyValue> columns;

    private int totalSize = 1;

    List<ScoreNode> nodes;

    SearchIndex index;

    public FacetHandler(Map<String, PropertyValue> columns, Selector selector, List<ScoreNode> nodes, SearchIndex index, SessionImpl session) {
        this.columns = columns;
        this.selector = selector;
        this.session = session;
        this.nodes = nodes;
        this.index = index;
        totalSize = nodes.size();
    }

    public boolean hasFacetFunctions() {
        boolean hasFacetRequest = false;
        for (String column : columns.keySet()) {
            if (isFacetFunction(column)) {
                hasFacetRequest = true;
                break;
            }
        }
        return hasFacetRequest;
    }

    /**
     * @param name a String.
     * @return <code>true</code> if <code>name</code> is the rep:facet function, <code>false</code> otherwise.
     */
    private boolean isFacetFunction(String name) {
        try {
            return name.trim().startsWith(session.getJCRName(REP_FACET_LPAR));
        } catch (NamespaceException e) {
            // will never happen
            return false;
        }
    }


    public void handleFacets(IndexReader reader) {
        IndexSearcher searcher = new IndexSearcher(reader);
        try {
            String facetFunctionPrefix = session.getJCRName(REP_FACET_LPAR);
            NamedList<Object> parameters = new NamedList<Object>();
            int counter = 0;
            Set<Integer> selectorIndexes = new HashSet<Integer>();
            for (Map.Entry<String, PropertyValue> column : columns.entrySet()) {
                if (isFacetFunction(column.getKey())) {
                    String facetOptions = StringUtils.substring(column.getKey(), StringUtils
                            .indexOf(column.getKey(), facetFunctionPrefix)
                            + facetFunctionPrefix.length(), StringUtils.lastIndexOf(column
                            .getKey(), ")"));

                    String propertyName = null;
                    if (!StringUtils.isEmpty(propertyName = StringUtils.substringAfter(facetOptions, FacetParams.FACET_FIELD + "=")) ||
                            !StringUtils.isEmpty(propertyName = StringUtils.substringAfter(facetOptions, "field="))) {
                        propertyName = StringUtils.substring(propertyName, 0, StringUtils.indexOfAny(
                                propertyName, "&)") >= 0 ? StringUtils.indexOfAny(propertyName,
                                "&)") : propertyName.length()) + SimpleJahiaJcrFacets.PROPNAME_INDEX_SEPARATOR + counter;
                    } else if (!StringUtils.isEmpty(propertyName = StringUtils.substringAfter(facetOptions, FacetParams.FACET_DATE + "=")) ||
                            !StringUtils.isEmpty(propertyName = StringUtils.substringAfter(facetOptions, "date="))) {
                        propertyName = StringUtils.substring(propertyName, 0, StringUtils.indexOfAny(
                                propertyName, "&)") >= 0 ? StringUtils.indexOfAny(propertyName,
                                "&)") : propertyName.length()) + SimpleJahiaJcrFacets.PROPNAME_INDEX_SEPARATOR + counter;
                    } else if (!StringUtils.contains(facetOptions, FacetParams.FACET_QUERY)) {
                        propertyName = column.getValue().getPropertyName() + SimpleJahiaJcrFacets.PROPNAME_INDEX_SEPARATOR + counter;
                        parameters.add((facetOptions.indexOf("&date.") >= 0 || facetOptions
                                .indexOf("facet.date.") >= 0) ? FacetParams.FACET_DATE
                                : FacetParams.FACET_FIELD, propertyName);
                    }

                    String nodeType = null;
                    for (String option : StringUtils.split(facetOptions, "&")) {
                        String key = StringUtils.substringBefore(option, "=");
                        String value = StringUtils.substringAfter(option, "=");
                        if ("key".equals(key)) {
                            //facetKey = value;
                        } else if ("nodetype".equals(key)) {
                            nodeType = value;
                        }
                    }
                    for (String option : StringUtils.split(facetOptions, "&")) {
                        String key = StringUtils.substringBefore(option, "=");
                        String value = StringUtils.substringAfter(option, "=");
                        int index = 0;
                        if (StringUtils.startsWith(key, FIELD_SPECIFIC_PREFIX)) {
                            index = FIELD_SPECIFIC_PREFIX.length() + StringUtils.substringBetween(key, ".", ".").length() + 1;
                        }
                        int indexOfFacetPrefix = StringUtils.indexOf(key, FacetParams.FACET + ".",
                                index);
                        if (indexOfFacetPrefix == index) {
                            index = FacetParams.FACET.length() + 1;
                        }
                        String facetOption = FacetParams.FACET + "." + StringUtils.substring(key, index);
                        if (facetOption.equals(FacetParams.FACET_QUERY)) {
                            if (value.split("(?<!\\\\):").length == 1
                                    && !StringUtils.isEmpty(column.getValue().getPropertyName())
                                    && !StringUtils.isEmpty(nodeType)
                                    && !column.getValue().getPropertyName().equals("rep:facet()")) {
                                ExtendedPropertyDefinition epd = NodeTypeRegistry.getInstance().getNodeType(nodeType).getPropertyDefinition(column.getValue().getPropertyName());
                                if (epd != null) {
                                    String fieldNameInIndex = getFieldNameInIndex(propertyName,
                                            epd, "");
                                    value = QueryParser.escape(fieldNameInIndex) + ":" + value;
                                }
                            }
                            parameters.add(facetOption, value);
                        } else if (facetOption.equals(FacetParams.FACET_FIELD) || facetOption.equals(FacetParams.FACET_DATE)) {
                            parameters.add(facetOption, propertyName);
                        } else {
                            parameters.add(FIELD_SPECIFIC_PREFIX + propertyName + "."
                                    + facetOption, value);
                        }
                    }
                    if (!StringUtils.isEmpty(propertyName)) {
                        String nodeTypeParam = FIELD_SPECIFIC_PREFIX + propertyName + "."
                                + FacetParams.FACET + ".nodetype";
                        if (parameters.get(nodeTypeParam) == null) {
                            parameters.add(nodeTypeParam, getNodeTypeFromSelector(column
                                    .getValue().getSelectorName(), column.getValue().getPropertyName()));
                        }
                    }
                    int i = 0;
//                    for (String selectorName : getSelectorNames()) {
//                        if (selectorName.equals(column.getValue().getSelectorName())) {
//                            selectorIndexes.add(i);
//                            break;
//                        }
//                        i++;
//                    }
                    counter++;
                }
            }

            SimpleJahiaJcrFacets facets = new SimpleJahiaJcrFacets(searcher,
                    transformToDocIdSet(nodes, reader, selectorIndexes), SolrParams
                            .toSolrParams(parameters), index, session);
            extractFacetInfo(facets.getFacetCounts());
        } catch (Exception ex) {
            log.warn("Problem creating facets: ", ex);
        } finally {
            try {
                searcher.close();
            } catch (IOException e) {
                log.warn("Unable to close searcher: " + e);
            }
        }
        return;
    }

    public String getFieldNameInIndex(String field, ExtendedPropertyDefinition epd, String langCode) {
        String fieldName = field;
        try {
            fieldName = session.getJCRName(NameFactoryImpl.getInstance().create(
                    session.getNamespaceURI(epd.getPrefix()),
                    epd.getLocalName()));
            int idx = fieldName.indexOf(':');
            fieldName = fieldName.substring(0, idx + 1)
                    + (epd != null && epd.isFacetable() ? JahiaNodeIndexer.FACET_PREFIX
                    : FieldNames.FULLTEXT_PREFIX)
                    + fieldName.substring(idx + 1);
        } catch (RepositoryException e) {
            // will never happen
        }
        return fieldName;
    }

    private String getNodeTypeFromSelector(String selectorName,
                                           String propertyName) throws RepositoryException {
        selectorName = StringUtils.removeEnd(selectorName, "translationAdded");
        Selector foundSelector = selector;
//        for (SelectorImpl selector : ((SourceImpl) qomTree.getSource()).getSelectors()) {
//            if (StringUtils.isEmpty(selectorName) || selectorName.equals(selector.getSelectorName())) {
//                foundSelector = selector;
//                break;
//            }
//        }
        return foundSelector.getNodeTypeName();
    }

    private void extractFacetInfo(NamedList<Object> info) {
        // Parse the queries
        _facetQuery = new LinkedHashMap<String, Long>();
        NamedList<Long> fq = (NamedList<Long>) info.get("facet_queries");
        if (fq != null) {
            for (Map.Entry<String, Long> entry : fq) {
                _facetQuery.put(entry.getKey(), entry.getValue());
            }
        }

        // Parse the facet info into fields
        // TODO?? The list could be <int> or <long>? If always <long> then we can switch to <Long>
        NamedList<NamedList<Number>> ff = (NamedList<NamedList<Number>>) info.get("facet_fields");
        if (ff != null) {
            _facetFields = new ArrayList<FacetField>(ff.size());
            _limitingFacets = new ArrayList<FacetField>(ff.size());

            long minsize = totalSize;
            for (Map.Entry<String, NamedList<Number>> facet : ff) {
                String key = StringUtils.substringBeforeLast(facet.getKey(),
                        SimpleJahiaJcrFacets.PROPNAME_INDEX_SEPARATOR);
                String fieldInIndex = StringUtils.substringAfterLast(facet.getKey(),
                        SimpleJahiaJcrFacets.PROPNAME_INDEX_SEPARATOR);
                FacetField f = new FacetField(key);
                for (Map.Entry<String, Number> entry : facet.getValue()) {
                    f.add(entry.getKey(), entry.getValue().longValue());
                    f.getValues().get(f.getValueCount() - 1).setFilterQuery(
                            ClientUtils.escapeQueryChars(fieldInIndex) + ":"
                                    + ClientUtils.escapeQueryChars(entry.getKey()));
                }

                _facetFields.add(f);
                FacetField nl = f.getLimitingFields(minsize);
                if (nl.getValueCount() > 0) {
                    _limitingFacets.add(nl);
                }
            }
        }

        // Parse date facets
        NamedList<NamedList<Object>> df = (NamedList<NamedList<Object>>) info.get("facet_dates");
        if (df != null) {
            // System.out.println(df);
            _facetDates = new ArrayList<FacetField>(df.size());
            for (Map.Entry<String, NamedList<Object>> facet : df) {
                // System.out.println("Key: " + facet.getKey() + " Value: " + facet.getValue());
                NamedList<Object> values = facet.getValue();
                String gap = (String) values.get("gap");
                Date end = (Date) values.get("end");
                FacetField f = new FacetField(StringUtils.substringBeforeLast(facet.getKey(),
                        SimpleJahiaJcrFacets.PROPNAME_INDEX_SEPARATOR), gap, end);

                for (Map.Entry<String, Object> entry : values) {
                    try {
                        String key = StringUtils.substringBeforeLast(entry.getKey(),
                                SimpleJahiaJcrFacets.PROPNAME_INDEX_SEPARATOR);
                        String query = StringUtils.substringAfterLast(entry.getKey(),
                                SimpleJahiaJcrFacets.PROPNAME_INDEX_SEPARATOR);
                        f.add(key, Long.parseLong(entry.getValue().toString()));
                        if (!StringUtils.isEmpty(query)) {
                            String rangePrefix = null;
                            if (query.contains(RANGEFROM_EXCLUSIVE_PREFIX)) {
                                rangePrefix = RANGEFROM_EXCLUSIVE_PREFIX;
                            } else if (query.contains(RANGEFROM_INCLUSIVE_PREFIX)) {
                                rangePrefix = RANGEFROM_INCLUSIVE_PREFIX;
                            }
                            if (!StringUtils.isEmpty(rangePrefix)) {
                                f.getValues().get(f.getValueCount() - 1).setFilterQuery(
                                        ClientUtils.escapeQueryChars(StringUtils.substringBefore(
                                                query, rangePrefix))
                                                + rangePrefix
                                                + StringUtils.substringAfter(query, rangePrefix));
                            }
                        }
                    } catch (NumberFormatException e) {
                        // Ignore for non-number responses which are already handled above
                    }
                }

                _facetDates.add(f);
            }
        }
    }

    private OpenBitSet transformToDocIdSet(List<ScoreNode> scoreNodeArrays, IndexReader reader, Set<Integer> selectorIndexes) {
        OpenBitSet docIds = null;
        try {
            BitSet bitset = new BitSet();
            for (ScoreNode node : scoreNodeArrays) {
                int i = 0;
//                for (ScoreNode node : scoreNodeArrays) {
                    if (node != null /*&& selectorIndexes.contains(i)*/) {
                        bitset.set(node.getDoc(reader));
                    }
                    i++;
//                }
            }
            docIds = new OpenBitSetDISI(new DocIdBitSet(bitset).iterator(), bitset.size());
        } catch (IOException e) {
            log.debug("Can't retrive bitset from hits", e);
        }
        return docIds;
    }


    public FacetRow getFacetsRow() {
        FacetRow row = new FacetRow();
        row.setFacetFields(_facetFields);
        row.setLimitingFacets(_limitingFacets);
        row.setFacetDates(_facetDates);
        row.setFacetQuery(_facetQuery);
        return row;
    }
}
