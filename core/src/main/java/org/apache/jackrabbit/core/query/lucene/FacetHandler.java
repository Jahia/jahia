/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.apache.jackrabbit.core.query.lucene;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.util.OpenBitSet;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.RangeFacet;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.schema.FieldType;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.search.facets.JahiaQueryParser;
import org.jahia.services.search.facets.SimpleJahiaJcrFacets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.NamespaceException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.query.qom.PropertyValue;
import javax.jcr.query.qom.Selector;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handle facet queries
 */
public class FacetHandler {
    public static final int FACET_COLUMNS = 0x1;
    public static final int ONLY_FACET_COLUMNS = 0x2;

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

    private static final Pattern valueWithQuery = Pattern.compile("(.*)##q\\->##(.*)");

    // Facet stuff
    private Map<String, Long> _facetQuery = null;
    private List<FacetField> _facetFields = null;
    private List<FacetField> _limitingFacets = null;
    private List<FacetField> _facetDates = null;
    private List<RangeFacet> _facetRanges = null;

    /**
     * Component context of the current session
     */
    protected final SessionImpl session;


    protected Selector selector;
    /**
     * The columns to select.
     */
    protected final Map<String, PropertyValue> columns;

    private long totalSize = 1;

    OpenBitSet docIdSet;

    SearchIndex index;

    NamespaceMappings nsMappings;

    public FacetHandler(Map<String, PropertyValue> columns, Selector selector, OpenBitSet docIdSet, SearchIndex index, SessionImpl session, NamespaceMappings nsMappings) {
        this.columns = columns;
        this.selector = selector;
        this.session = session;
        this.docIdSet = docIdSet;
        this.index = index;
        this.nsMappings = nsMappings;
        totalSize = docIdSet.cardinality();
    }

    public static int hasFacetFunctions(Map<String, PropertyValue> columns, SessionImpl session) {
        int hasFacetRequest = !columns.isEmpty() ? ONLY_FACET_COLUMNS : 0;
        for (String column : columns.keySet()) {
            if (isFacetFunction(column, session)) {
                hasFacetRequest |= FACET_COLUMNS;
            } else {
                hasFacetRequest &= ~ONLY_FACET_COLUMNS;
            }
        }
        return hasFacetRequest;
    }

    /**
     * @param name a String.
     * @return <code>true</code> if <code>name</code> is the rep:facet function, <code>false</code> otherwise.
     */
    private static boolean isFacetFunction(String name, SessionImpl session) {
        try {
            return name.trim().startsWith(session.getJCRName(REP_FACET_LPAR));
        } catch (NamespaceException e) {
            // will never happen
            return false;
        }
    }

    /**
     * Process facets information for the specified IndexReader.
     *
     * @param reader
     */
    public void handleFacets(IndexReader reader) {
        IndexSearcher searcher = new IndexSearcher(reader);
        try {
            String facetFunctionPrefix = session.getJCRName(REP_FACET_LPAR);
            NamedList<Object> parameters = new NamedList<Object>();

            int counter = 0;
            for (Map.Entry<String, PropertyValue> column : columns.entrySet()) {
                if (isFacetFunction(column.getKey(), session)) {
                    extractFacetParameters(facetFunctionPrefix, parameters, counter, column);
                    counter++;
                }
            }

            SolrParams solrParams = SolrParams.toSolrParams(parameters);
            SimpleJahiaJcrFacets facets = new SimpleJahiaJcrFacets(searcher, docIdSet, solrParams, index, session, nsMappings);
            extractFacetInfo(facets.getFacetCounts(), solrParams);
        } catch (Exception ex) {
            log.warn("Problem creating facets: ", ex);
        } finally {
            try {
                searcher.close();
            } catch (IOException e) {
                log.warn("Unable to close searcher: " + e);
            }
        }
    }

    private void extractFacetParameters(String facetFunctionPrefix, NamedList<Object> parameters, int counter, Map.Entry<String, PropertyValue> column) throws RepositoryException {
        // first extract options from rep:facet() from column key
        final String key = column.getKey();
        final String facetOptions = key.substring(key.indexOf(facetFunctionPrefix) + facetFunctionPrefix.length(),
                key.lastIndexOf(")"));

        // remember nodetype and query values if encountered so that we can process them once the whole facet is parsed
        String nodeType = null;
        List<String> unparsedQueries = null;

        // loop invariants
        final String columnPropertyName = column.getValue().getPropertyName();
        final String propertyName = columnPropertyName + SimpleJahiaJcrFacets.PROPNAME_INDEX_SEPARATOR + counter;

        // we can assert the facet type by checking whether the the options String contains date or range, otherwise, the type is field
        final boolean isQuery = facetOptions.contains(FacetParams.FACET_QUERY);
        String facetType = FacetParams.FACET_FIELD; // default facet type
        if (isQuery) {
            facetType = FacetParams.FACET_QUERY;
        } else if (facetOptions.contains("date")) {
            facetType = FacetParams.FACET_DATE;
        } else if (facetOptions.contains("range")) {
            facetType = FacetParams.FACET_RANGE;
        }
        parameters.add(facetType, propertyName);

        // populate parameters
        // each parameter name/value pair is separated from the next one by & so split on this
        final String[] paramPairs = StringUtils.split(facetOptions, "&");
        for (String paramPair : paramPairs) {
            // for each pair, extract the name and value separated by =
            int separator = paramPair.indexOf('=');
            if (separator >= 0) { // todo: what should we do if a pair doesn't have an equal sign in it?
                final String paramName = paramPair.substring(0, separator);
                final String paramValue = paramPair.substring(separator + 1);

                // some parameters need to be specially processed and not be added as others so process them and exit current iteration when encountered
                if (paramName.equals("nodetype")) {
                    nodeType = paramValue; // remember node type value for later processing
                    continue;
                } else if (paramName.contains("query")) {
                    if (unparsedQueries == null) {
                        unparsedQueries = new LinkedList<String>();
                    }
                    unparsedQueries.add(paramValue); // remember query value for later processing
                    continue;
                }

                // create full parameter name and add its value to the parameters
                String facetOption = getFacetOption(paramName);
                parameters.add(getFullParameterName(propertyName, facetOption), paramValue);
            }
        }

        // node type parameter
        if (StringUtils.isEmpty(nodeType)) {
            // if we didn't have a node type specified in the given options, extract it from the selector name and create the associated parameter
            nodeType = getNodeTypeFromSelector(column.getValue().getSelectorName(), columnPropertyName);
        }

        // only add node type parameter if we're not dealing with a query
        if (!isQuery) {
            parameters.add(getFullParameterName(propertyName, getFacetOption("nodetype")), nodeType);
        }

        // deal with embedded query if needed, at this point, nodeType will have been either extracted or asserted from selector name
        if (unparsedQueries != null) {
            ExtendedPropertyDefinition epd = NodeTypeRegistry.getInstance().getNodeType(nodeType).getPropertyDefinition(columnPropertyName);

            for (String unparsedQuery : unparsedQueries) {
                if (unparsedQuery.split("(?<!\\\\):").length == 1 && !columnPropertyName.equals("rep:facet()")) {
                    if (epd != null) {
                        String fieldNameInIndex = getFieldNameInIndex(propertyName, epd, "");
                        unparsedQuery = QueryParser.escape(fieldNameInIndex) + ":" + unparsedQuery;
                    }
                }
                parameters.add(getFullParameterName(propertyName, "query"), unparsedQuery);
            }
        }
    }

    private String getFullParameterName(String propertyName, String facetOption) {
        return FIELD_SPECIFIC_PREFIX + propertyName + "." + facetOption;
    }

    private String getFacetOption(String paramName) {
        final String prefix = FacetParams.FACET + ".";
        return paramName.startsWith(prefix) ? paramName : prefix + paramName;
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

    private void extractFacetInfo(NamedList<Object> info, SolrParams solrParams) {
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
        Map<String,FieldType> fieldTypeMap = new HashMap<>();
        if (ff != null) {
            _facetFields = new ArrayList<FacetField>(ff.size());
            _limitingFacets = new ArrayList<FacetField>(ff.size());
            long minsize = totalSize;
            for (Map.Entry<String, NamedList<Number>> facet : ff) {
                String propertyName = StringUtils.substringBefore(facet.getKey(),
                        SimpleJahiaJcrFacets.PROPNAME_INDEX_SEPARATOR);
                String fieldInIndex = StringUtils.substringBetween(facet.getKey(),
                        SimpleJahiaJcrFacets.PROPNAME_INDEX_SEPARATOR);
                String fieldKey = StringUtils.substringAfterLast(facet.getKey(),
                        SimpleJahiaJcrFacets.PROPNAME_INDEX_SEPARATOR);
                if(!fieldTypeMap.containsKey(propertyName)) {
                    try {
                        //Find a key like f.field_name#unknownumber.facet.nodetype
                        Pattern facetNodetype = Pattern.compile("f\\." + propertyName + "#[0-9]+\\.facet\\.nodetype");
                        String nodetypeName = null;
                        Iterator<String> parameterNamesIterator = solrParams.getParameterNamesIterator();
                        while (parameterNamesIterator.hasNext()) {
                            String next = parameterNamesIterator.next();
                            if (facetNodetype.matcher(next).matches()) {
                                nodetypeName = solrParams.get(next);
                                break;
                            }
                        }
                        ExtendedPropertyDefinition epd = NodeTypeRegistry.getInstance().getNodeType(nodetypeName).getPropertyDefinition(propertyName);
                        fieldTypeMap.put(propertyName, getType(epd));
                    } catch (RepositoryException e) {
                        throw new JahiaRuntimeException(e);
                    }
                }
                FacetField f = new FacetField(StringUtils.isEmpty(fieldKey) ? propertyName : fieldKey);
                for (Map.Entry<String, Number> entry : facet.getValue()) {
                    String facetValue = entry.getKey();
                    String query = fieldTypeMap.get(propertyName).toInternal(entry.getKey());
                    Matcher matcher = valueWithQuery.matcher(facetValue);
                    if (matcher.matches()) {
                        query = matcher.group(2);
                        facetValue = matcher.replaceFirst("$1");
                    }
                    f.add(facetValue, entry.getValue().longValue());
                    f.getValues().get(f.getValueCount() - 1).setFilterQuery(
                            ClientUtils.escapeQueryChars(fieldInIndex) + ":"
                                    + ClientUtils.escapeQueryChars(query));
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

        // Parse range facets
        NamedList<NamedList<Object>> rf = (NamedList<NamedList<Object>>) info.get("facet_ranges");
        if (rf != null) {
            // System.out.println(df);
            _facetRanges = new ArrayList<RangeFacet>(rf.size());
            for (Map.Entry<String, NamedList<Object>> facet : rf) {
                NamedList<Object> values = facet.getValue();
                Object rawGap = values.get("gap");

                RangeFacet rangeFacet;
                if (rawGap instanceof Number) {
                    Number gap = (Number) rawGap;
                    Number start = (Number) values.get("start");
                    Number end = (Number) values.get("end");

                    Number before = (Number) values.get("before");
                    Number after = (Number) values.get("after");

                    rangeFacet = new RangeFacet.Numeric(StringUtils.substringBeforeLast(facet.getKey(),
                            SimpleJahiaJcrFacets.PROPNAME_INDEX_SEPARATOR), start, end, gap, before, after);
                } else {
                    String gap = (String) rawGap;
                    Date start = (Date) values.get("start");
                    Date end = (Date) values.get("end");

                    Number before = (Number) values.get("before");
                    Number after = (Number) values.get("after");

                    rangeFacet = new RangeFacet.Date(StringUtils.substringBeforeLast(facet.getKey(),
                            SimpleJahiaJcrFacets.PROPNAME_INDEX_SEPARATOR), start, end, gap, before, after);
                }

                NamedList<Integer> counts = (NamedList<Integer>) values.get("counts");
                for (Map.Entry<String, Integer> entry : counts) {
                    try {
                        String key = StringUtils.substringBeforeLast(entry.getKey(),
                                SimpleJahiaJcrFacets.PROPNAME_INDEX_SEPARATOR);
                        String query = StringUtils.substringAfterLast(entry.getKey(),
                                SimpleJahiaJcrFacets.PROPNAME_INDEX_SEPARATOR);

                        rangeFacet.addCount(key, entry.getValue());

                        if (!StringUtils.isEmpty(query)) {
                            String rangePrefix = null;
                            if (query.contains(RANGEFROM_EXCLUSIVE_PREFIX)) {
                                rangePrefix = RANGEFROM_EXCLUSIVE_PREFIX;
                            } else if (query.contains(RANGEFROM_INCLUSIVE_PREFIX)) {
                                rangePrefix = RANGEFROM_INCLUSIVE_PREFIX;
                            }
                            if (!StringUtils.isEmpty(rangePrefix)) {
                                ((RangeFacet.Count) rangeFacet.getCounts().get(rangeFacet.getCounts().size() - 1)).setFilterQuery(
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

                _facetRanges.add(rangeFacet);
            }
        }
    }

    public FacetRow getFacetsRow() {
        FacetRow row = new FacetRow();
        row.setFacetFields(_facetFields);
        row.setLimitingFacets(_limitingFacets);
        row.setFacetDates(_facetDates);
        row.setRangeFacets(_facetRanges);
        row.setFacetQuery(_facetQuery);
        return row;
    }

    private FieldType getType(ExtendedPropertyDefinition epd) {
        FieldType type = null;
        switch (epd.getRequiredType()) {
            case PropertyType.BINARY:
                type = JahiaQueryParser.BINARY_TYPE;
                break;
            case PropertyType.BOOLEAN:
                type = JahiaQueryParser.BOOLEAN_TYPE;
                break;
            case PropertyType.DATE:
                type = JahiaQueryParser.DATE_TYPE;
                break;
            case PropertyType.DOUBLE:
                type = JahiaQueryParser.SORTABLE_DOUBLE_TYPE;
                break;
            case PropertyType.LONG:
                type = JahiaQueryParser.SORTABLE_LONG_TYPE;
                break;
            case PropertyType.NAME:
                type = JahiaQueryParser.STRING_TYPE;
                break;
            case PropertyType.PATH:
                type = JahiaQueryParser.STRING_TYPE;
                break;
            case PropertyType.REFERENCE:
                type = JahiaQueryParser.STRING_TYPE;
                break;
            case PropertyType.STRING:
                type = JahiaQueryParser.STRING_TYPE;
                break;
            case PropertyType.URI:
                type = JahiaQueryParser.STRING_TYPE;
                break;
            case PropertyType.WEAKREFERENCE:
                type = JahiaQueryParser.STRING_TYPE;
                break;
            case PropertyType.DECIMAL:
                throw new UnsupportedOperationException();
        }
        return type;
    }
}
