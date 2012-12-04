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

package org.jahia.services.search.facets;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.query.lucene.FieldNames;
import org.apache.jackrabbit.core.query.lucene.JahiaNodeIndexer;
import org.apache.jackrabbit.core.query.lucene.NamePathResolverImpl;
import org.apache.jackrabbit.core.query.lucene.NamespaceMappings;
import org.apache.jackrabbit.core.query.lucene.SearchIndex;
import org.apache.jackrabbit.core.query.lucene.hits.AbstractHitCollector;
import org.apache.jackrabbit.spi.commons.conversion.NamePathResolver;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.util.DocIdBitSet;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.util.OpenBitSetDISI;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.FacetParams;
<<<<<<< .working
import org.apache.solr.common.params.FacetParams.FacetRangeInclude;
import org.apache.solr.common.params.FacetParams.FacetRangeOther;
=======
import org.apache.solr.common.params.MapSolrParams;
>>>>>>> .merge-right.r43880
import org.apache.solr.common.params.RequiredSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.params.FacetParams.FacetDateOther;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.common.util.StrUtils;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.request.SimpleFacets;
import org.apache.solr.schema.BoolField;
import org.apache.solr.schema.DateField;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.SortableDoubleField;
import org.apache.solr.schema.SortableFloatField;
import org.apache.solr.schema.SortableIntField;
import org.apache.solr.schema.SortableLongField;
import org.apache.solr.schema.TrieField;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.util.DateMathParser;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.renderer.ChoiceListRenderer;
import org.jahia.services.content.nodetypes.renderer.ChoiceListRendererService;
import org.jahia.utils.LanguageCodeConverters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.EnumSet;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * A class that generates simple Facet information for a request.
 * 
 * More advanced facet implementations may compose or subclass this class to leverage any of it's functionality.
 */
public class SimpleJahiaJcrFacets {
    /**
     * The logger instance for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(SimpleJahiaJcrFacets.class);

    public static final String PROPNAME_INDEX_SEPARATOR = "#";
    
    private static final Pattern propertyFieldPrefix = Pattern.compile("\\d+:[0-9a-zA-Z]+\\[(.*)");    
    private static final Pattern nameFieldPrefix = Pattern.compile("(\\d+):(.*)");    

    /** The main set of documents all facet counts should be relative to */
    protected OpenBitSet docs;
    /** Configuration params behavior should be driven by */
    protected SolrParams params;
    protected SolrParams required;    
    /** Searcher to use for all calculations */
    protected IndexSearcher searcher;

    protected ResponseBuilder rb;

    protected SimpleOrderedMap<Object> facetResponse;

    public final Date NOW = new Date();

    // per-facet values
    SolrParams localParams; // localParams on this particular facet command
    String facetValue;      // the field to or query to facet on (minus local params)
    OpenBitSet base;            // the base docset for this particular facet
    String key;             // what name should the results be stored under    
    
    protected Analyzer defaultAnalyzer;
    /**
     * Name and Path resolver.
     */
    protected final NamePathResolver resolver;
    
    /**
     * Name and Path resolver.
     */
    protected final Session session;
    
    private NamespaceMappings nsMappings;

    public SimpleJahiaJcrFacets(IndexSearcher searcher, OpenBitSet docs,
            SolrParams params, SearchIndex index, Session session, NamespaceMappings nsMappings) {
        this.searcher = searcher;
        this.base = this.docs = docs;
        this.params = params;
        this.required = new RequiredSolrParams(params);
//        this.rb = rb;        
        this.resolver = NamePathResolverImpl.create(index.getNamespaceMappings());
        this.defaultAnalyzer = index.getTextAnalyzer();
        this.session = session;
        this.nsMappings = nsMappings;
    }

    @SuppressWarnings("unused")
    void parseParams(String type, String param) throws ParseException, IOException {
// TODO: Should we activate that and how ? - we have no request object in backend        
//        localParams = QueryParsing.getLocalParams(param, req.getParams());
        localParams = QueryParsing.getLocalParams(param, params);
        base = docs;
        facetValue = param;
        key = param;

        if (localParams == null) return;

        // remove local params unless it's a query
        if (type != FacetParams.FACET_QUERY) {
          facetValue = localParams.get(CommonParams.VALUE);
        }

        // reset set the default key now that localParams have been removed
        key = facetValue;

        // allow explicit set of the key
        key = localParams.get(CommonParams.OUTPUT_KEY, key);

        // figure out if we need a new base DocSet
        String excludeStr = localParams.get(CommonParams.EXCLUDE);
        if (excludeStr == null) return;
// TODO: Should we activate that and how ? - we have no request object in backend
//        Map tagMap = (Map)req.getContext().get("tags");
        Map<String, Object> tagMap = null;
        if (tagMap != null && rb != null) {
          List<String> excludeTagList = StrUtils.splitSmart(excludeStr,',');

          IdentityHashMap<Query,Boolean> excludeSet = new IdentityHashMap<Query,Boolean>();
          for (String excludeTag : excludeTagList) {
            Object olst = tagMap.get(excludeTag);
            // tagMap has entries of List<String,List<QParser>>, but subject to change in the future
            if (!(olst instanceof Collection)) continue;
            for (Object o : (Collection<?>)olst) {
              if (!(o instanceof QParser)) continue;
              QParser qp = (QParser)o;
              excludeSet.put(qp.getQuery(), Boolean.TRUE);
            }
          }
          if (excludeSet.size() == 0) return;

          List<Query> qlist = new ArrayList<Query>();

          // add the base query
          if (!excludeSet.containsKey(rb.getQuery())) {
            qlist.add(rb.getQuery());
          }

          // add the filters
          if (rb.getFilters() != null) {
            for (Query q : rb.getFilters()) {
              if (!excludeSet.containsKey(q)) {
                qlist.add(q);
              }
            }
          }

          // get the new base docset for this facet
          base = getDocIdSet(qlist, "");
        }

      }    
    
    /**
     * Looks at various Params to determing if any simple Facet Constraint count computations are desired.
     * 
     * @see #getFacetQueryCounts
     * @see #getFacetFieldCounts
     * @see #getFacetDateCounts
     * @see FacetParams#FACET
     * @return a NamedList of Facet Count info or null
     */
    public NamedList<Object> getFacetCounts() {
        // if someone called this method, benefit of the doubt: assume true
        if (!params.getBool(FacetParams.FACET, true)) {
            return null;
        }

        facetResponse = new SimpleOrderedMap<Object>();
        try {

            facetResponse.add("facet_queries", getFacetQueryCounts());
            facetResponse.add("facet_fields", getFacetFieldCounts());
            facetResponse.add("facet_dates", getFacetDateCounts());
            facetResponse.add("facet_ranges", getFacetRangeCounts());

        } catch (Exception e) {
            logger.warn("Exception during facet counts", e);
            addException("Exception during facet counts", e);
        }
        return facetResponse;
    }
    
    public void addException(String msg, Exception e) {
        List<String> exceptions = (List<String>)facetResponse.get("exception");
        if (exceptions == null) {
          exceptions = new ArrayList<String>();
          facetResponse.add("exception", exceptions);
        }

        String entry = msg + '\n' + SolrException.toStr(e);
        exceptions.add(entry);
      }    

    /**
     * Returns a list of facet counts for each of the facet queries specified in the params
     * 
     * @see FacetParams#FACET_QUERY
     */
    public NamedList<Object> getFacetQueryCounts() throws IOException, ParseException {

        NamedList<Object> res = new SimpleOrderedMap<Object>();

        /* Ignore CommonParams.DF - could have init param facet.query assuming
         * the schema default with query param DF intented to only affect Q.
         * If user doesn't want schema default for facet.query, they should be
         * explicit.
         */
        // SolrQueryParser qp = searcher.getSchema().getSolrQueryParser(null);

        String[] facetQs = params.getParams(FacetParams.FACET_QUERY);
        if (null != facetQs && 0 != facetQs.length) {
            Integer mincount = params.getFieldInt("", FacetParams.FACET_MINCOUNT);
            if (mincount == null) {
                Boolean zeros = params.getFieldBool("", FacetParams.FACET_ZEROS);
                // mincount = (zeros!=null && zeros) ? 0 : 1;
                mincount = (zeros != null && !zeros) ? 1 : 0;
                // current default is to include zeros.
            }            
            for (String q : facetQs) {
                try {
                    parseParams(FacetParams.FACET_QUERY, q);
                
                    QueryParser qp = new JahiaQueryParser(FieldNames.FULLTEXT, new KeywordAnalyzer());
                    qp.setLowercaseExpandedTerms(false);
                    Query qobj = qp.parse(q);
                    long count = OpenBitSet.intersectionCount(getDocIdSet(qobj, ""),
                            base);
                    if (count >= mincount) {
                        res.add(key, count);
                    }
                }
                catch (Exception e) {
                  String msg = "Exception during facet.query of " + q;
                  logger.warn(msg, e);
                  addException(msg , e);
                }
            }
        }

        return res;
    }

    public NamedList<Object> getTermCounts(String field, ExtendedPropertyDefinition epd, String fieldNameInIndex, String locale)
            throws IOException, RepositoryException {
        int offset = params.getFieldInt(field, FacetParams.FACET_OFFSET, 0);
        int limit = params.getFieldInt(field, FacetParams.FACET_LIMIT, 100);
        if (limit == 0)
            return new NamedList<Object>();
        Integer mincount = params.getFieldInt(field, FacetParams.FACET_MINCOUNT);
        if (mincount == null) {
            Boolean zeros = params.getFieldBool(field, FacetParams.FACET_ZEROS);
            // mincount = (zeros!=null && zeros) ? 0 : 1;
            mincount = (zeros != null && !zeros) ? 1 : 0;
            // current default is to include zeros.
        }
        boolean missing = params.getFieldBool(field, FacetParams.FACET_MISSING, false);
        // default to sorting if there is a limit.
        String sort = params.getFieldParam(field, FacetParams.FACET_SORT, limit>0 ? FacetParams.FACET_SORT_COUNT : FacetParams.FACET_SORT_INDEX);
        String prefix = params.getFieldParam(field, FacetParams.FACET_PREFIX);

        NamedList<Object> counts;
        SchemaField sf = new SchemaField(fieldNameInIndex, getType(epd));
        FieldType ft = sf.getType();        

        // determine what type of faceting method to use
        String method = params.getFieldParam(field, FacetParams.FACET_METHOD);
        boolean enumMethod = FacetParams.FACET_METHOD_enum.equals(method);
        if (method == null && ft instanceof BoolField) {
          // Always use filters for booleans... we know the number of values is very small.
          enumMethod = true;
        }
        boolean multiToken = epd.isMultiple();
// --> added by jahia as we don't use the UnInvertedField class yet due to not using SolrIndexSearcher        
       enumMethod = enumMethod && multiToken;
//        if (TrieField.getMainValuePrefix(ft) != null) {
//          // A TrieField with multiple parts indexed per value... currently only
//          // UnInvertedField can handle this case, so force it's use.
//          enumMethod = false;
//          multiToken = true;
//        }

        // unless the enum method is explicitly specified, use a counting method.
        if (enumMethod) {
            // Always use filters for booleans... we know the number of values is very small.
            counts = getFacetTermEnumCounts(searcher, docs, field, fieldNameInIndex, offset, limit, mincount,
                    missing, sort, prefix, epd.isInternationalized() ? (locale == null ? "" : locale): null, epd);
        } else {
//            if (multiToken) {
//                UnInvertedField uif = UnInvertedField.getUnInvertedField(field, searcher);
//                counts = uif.getCounts(searcher, base, offset, limit, mincount,missing,sort,prefix);
//              } else {
            // TODO: future logic could use filters instead of the fieldcache if
            // the number of terms in the field is small enough.
            counts = getFieldCacheCounts(searcher, docs, fieldNameInIndex, offset, limit, mincount, missing, sort,
                    prefix, epd.isInternationalized() ? (locale == null ? "" : locale): null, epd);
//              }
        }

        return counts;
    }

    /**
     * Returns a list of value constraints and the associated facet counts for each facet field specified in the params.
     * 
     * @see FacetParams#FACET_FIELD
     * @see #getFieldMissingCount
     * @see #getFacetTermEnumCounts
     */
    public NamedList<Object> getFacetFieldCounts() throws IOException {

        NamedList<Object> res = new SimpleOrderedMap<Object>();
        String[] facetFs = params.getParams(FacetParams.FACET_FIELD);
        if (null != facetFs) {
            for (String f : facetFs) {
                try {
                    String fieldName = StringUtils.substringBeforeLast(f, PROPNAME_INDEX_SEPARATOR);
                    String locale = params.getFieldParam(f, "facet.locale");
                    ExtendedPropertyDefinition epd = NodeTypeRegistry.getInstance().getNodeType(
                            params.get("f." + f + ".facet.nodetype")).getPropertyDefinition(
                            fieldName);
                    String fieldNameInIndex = getFieldNameInIndex(f, fieldName, epd, locale, true);
<<<<<<< .working
                    
                    parseParams(FacetParams.FACET_FIELD, f);
                   // TODO: can we use the key to add item in result like in original Solr ??? 
                    String termList = localParams == null ? null : localParams.get(CommonParams.TERMS);
                    if (termList != null) {
                        res.add(StringUtils.substringBeforeLast(f,
                                PROPNAME_INDEX_SEPARATOR)
                                + PROPNAME_INDEX_SEPARATOR + fieldNameInIndex,
                                ensureSorting(
                                        params.getFieldParam(f, "facet.sort"),
                                        getListedTermCounts(f, epd,
                                                fieldNameInIndex, locale,
                                                termList), epd, params
                                                .getFieldParam(f,
                                                        "facet.labelRenderer"),
                                        LanguageCodeConverters
                                                .getLocaleFromCode(locale)));
                    } else {
                        res.add(StringUtils.substringBeforeLast(f,
                                PROPNAME_INDEX_SEPARATOR)
                                + PROPNAME_INDEX_SEPARATOR + fieldNameInIndex,
                                ensureSorting(
                                        params.getFieldParam(f, "facet.sort"),
                                        getTermCounts(f, epd, fieldNameInIndex,
                                                locale), epd, params
                                                .getFieldParam(f,
                                                        "facet.labelRenderer"),
                                        LanguageCodeConverters
                                                .getLocaleFromCode(locale)));
                    }
                } catch (Exception e) {
=======
                    res.add(StringUtils.substringBeforeLast(f, PROPNAME_INDEX_SEPARATOR)
                            + PROPNAME_INDEX_SEPARATOR + fieldNameInIndex,
                            ensureSortingAndNameResolution(params.getFieldParam(f, "facet.sort"), getTermCounts(f, epd, fieldNameInIndex, locale), epd, params.getFieldParam(f, "facet.labelRenderer"), LanguageCodeConverters.getLocaleFromCode(locale), fieldNameInIndex));
                } catch (RepositoryException e) {
>>>>>>> .merge-right.r43880
                    logger.error("Cant display facets for: " + f, e);
                }
            }
        }
        return res;
    }
    
    private NamedList<Object> ensureSortingAndNameResolution(String fieldSort,
            NamedList<Object> values, ExtendedPropertyDefinition fieldPropertyType,
            String facetValueRenderer, Locale locale, String fieldName) {
        ChoiceListRenderer renderer = !StringUtils.isEmpty(facetValueRenderer) ? ChoiceListRendererService
                .getInstance().getRenderers().get(facetValueRenderer)
                : null;
        if (FieldNames.PROPERTIES.equals(fieldName)) {
            NamedList<Object> resolvedValues = new NamedList<Object>();
            for (Map.Entry<String, Object> facetValue : values) {
                String facetValueKey = facetValue.getKey(); 
                facetValueKey = propertyFieldPrefix.matcher(facetValueKey).replaceFirst("$1") + "##q->##" + facetValue.getKey();
                resolvedValues.add(facetValueKey, facetValue.getValue());
            }
            values = resolvedValues;
        }
        if (fieldPropertyType.getRequiredType() == PropertyType.NAME) {
            NamedList<Object> resolvedValues = new NamedList<Object>();
            for (Map.Entry<String, Object> facetValue : values) {
                String facetValueKey = facetValue.getKey();                
                Matcher matcher = nameFieldPrefix.matcher(facetValueKey);
                if (matcher.matches()) {
                    String nsPrefix = matcher.group(1);
                    try {
                        nsPrefix = session.getNamespacePrefix(nsMappings.getURI(nsPrefix));
                    } catch (RepositoryException e) {
                        // use the unconverted prefix
                    }
                    StringBuilder facetValueBuilder = new StringBuilder();
                    facetValueBuilder.append(nsPrefix);
                    if (facetValueBuilder.length() > 0) {
                        facetValueBuilder.append(":");
                    }
                    facetValueBuilder.append("$2");
                    facetValueKey = matcher.replaceFirst(facetValueBuilder.toString());
                    if (!FieldNames.PROPERTIES.equals(fieldName)) {
                        facetValueKey = facetValueKey + "##q->##" + facetValue.getKey(); 
                    }
                }
                resolvedValues.add(facetValueKey, facetValue.getValue());
            }
            values = resolvedValues;
        }
        if (values.size() > 1
                && (fieldSort != null && (fieldSort.equals("false") || fieldSort.equals("index")))
                && renderer != null) {
            try {
                SortedMap<String, Integer> sortedLabels = new TreeMap<String, Integer>();
                int i = 0;

                for (Map.Entry<String, Object> facetValue : values) {
                    sortedLabels.put(renderer.getStringRendering(locale,
                            fieldPropertyType, facetValue.getKey()), i++);
                }
                NamedList<Object> sortedValues = new NamedList<Object>();
                for (Integer index : sortedLabels.values()) {
                    sortedValues.add(values.getName(index), values.getVal(index));
                }
                values = sortedValues;
            } catch (RepositoryException e) {
                logger.warn("Repository exception while sorting label rendered facet values, fallback to default sorting", e);
            } catch (UnsupportedOperationException e) {
                logger.warn("Unsupported operation exception while sorting label rendered facet values, fallback to default sorting", e);                
            }
        }
        return values;
    }
    
    private NamedList<Object> getListedTermCounts(String field, ExtendedPropertyDefinition epd, String fieldNameInIndex, String locale, String termList) throws IOException {
        FieldType ft = getType(epd);
        List<String> terms = StrUtils.splitSmart(termList, ",", true);
        NamedList<Object> res = new NamedList<Object>();
        Term t = new Term(field);
        for (String term : terms) {
          String internal = ft.toInternal(term);
          int count = (int) OpenBitSet.intersectionCount(getDocIdSet(new TermQuery(t.createTerm(internal)), ""),
                  base);
          res.add(term, count);
        }
        return res;    
      }    

    /**
     * Returns a count of the documents in the set which do not have any terms for for the specified field.
     * 
     * @see FacetParams#FACET_MISSING
     */
    public int getFieldMissingCount(IndexSearcher searcher, OpenBitSet docs, String fieldName, String locale)
            throws IOException {
        Query query = null;
        if (StringUtils.isEmpty(locale)) {
            query = new TermRangeQuery(fieldName, null, null, false, false);
        } else {
            BooleanQuery booleanQuery = new BooleanQuery();
            booleanQuery.add(new TermRangeQuery(fieldName, null, null, false, false),
                    BooleanClause.Occur.MUST);
            booleanQuery.add(
                    new TermQuery(new Term(JahiaNodeIndexer.TRANSLATION_LANGUAGE, locale)),
                    BooleanClause.Occur.MUST);
            query = booleanQuery;
        }
        OpenBitSet hasVal = getDocIdSet(query, locale);
        return (int) OpenBitSet.andNotCount(docs, hasVal);
    }

    // first element of the fieldcache is null, so we need this comparator.
    private static final Comparator<String> nullStrComparator = new Comparator<String>() {
        public int compare(String o1, String o2) {
            if (o1 == null)
                return (o2 == null) ? 0 : -1;
            else if (o2 == null)
                return 1;
            return (o1.compareTo(o2));
        }
    };

    /**
     * Use the Lucene FieldCache to get counts for each unique field value in <code>docs</code>. The field must have at most one indexed
     * token per document.
     */
    public NamedList<Object> getFieldCacheCounts(IndexSearcher searcher, OpenBitSet docs,
            String fieldName, int offset, int limit, int mincount, boolean missing, String sort,
            String prefix, String locale, ExtendedPropertyDefinition epd) throws IOException {
        // TODO: If the number of terms is high compared to docs.size(), and zeros==false,
        // we should use an alternate strategy to avoid
        // 1) creating another huge int[] for the counts
        // 2) looping over that huge int[] looking for the rare non-zeros.
        //
        // Yet another variation: if docs.size() is small and termvectors are stored,
        // then use them instead of the FieldCache.
        //

        // TODO: this function is too big and could use some refactoring, but
        // we also need a facet cache, and refactoring of SimpleFacets instead of
        // trying to pass all the various params around.
        FieldType ft = getType(epd);
        NamedList<Object> res = new NamedList<Object>();

        FieldCache.StringIndex si = FieldCache.DEFAULT.getStringIndex(searcher.getIndexReader(),
                fieldName);
        final String[] terms = si.lookup;
        final int[] termNum = si.order;

        if (prefix != null && prefix.length() == 0)
            prefix = null;

        int startTermIndex, endTermIndex;
        if (prefix != null) {
            startTermIndex = Arrays.binarySearch(terms, prefix, nullStrComparator);
            if (startTermIndex < 0)
                startTermIndex = -startTermIndex - 1;
            // find the end term. \uffff isn't a legal unicode char, but only compareTo
            // is used, so it should be fine, and is guaranteed to be bigger than legal chars.
            endTermIndex = Arrays.binarySearch(terms, prefix + "\uffff\uffff\uffff\uffff",
                    nullStrComparator);
            endTermIndex = -endTermIndex - 1;
        } else {
            startTermIndex = 1;
            endTermIndex = terms.length;
        }

        final int nTerms = endTermIndex - startTermIndex;

        if (nTerms > 0 && docs.size() >= mincount) {

            // count collection array only needs to be as big as the number of terms we are
            // going to collect counts for.
            final int[] counts = new int[nTerms];

            DocIdSetIterator iter = docs.iterator();
            while (iter.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
                int term = termNum[iter.docID()];
                int arrIdx = term - startTermIndex;
                if (arrIdx >= 0 && arrIdx < nTerms)
                    counts[arrIdx]++;
            }

            // IDEA: we could also maintain a count of "other"... everything that fell outside
            // of the top 'N'

            int off = offset;
            int lim = limit >= 0 ? limit : Integer.MAX_VALUE;

            if (sort.equals(FacetParams.FACET_SORT_COUNT) || sort.equals(FacetParams.FACET_SORT_COUNT_LEGACY)) {
                int maxsize = limit > 0 ? offset + limit : Integer.MAX_VALUE - 1;
                maxsize = Math.min(maxsize, nTerms);
                final TreeSet<SimpleFacets.CountPair<String, Integer>> queue = new TreeSet<SimpleFacets.CountPair<String, Integer>>();
                int min = mincount - 1; // the smallest value in the top 'N' values
                for (int i = 0; i < nTerms; i++) {
                    int c = counts[i];
                    if (c > min) {
                        // NOTE: we use c>min rather than c>=min as an optimization because we are going in
                        // index order, so we already know that the keys are ordered. This can be very
                        // important if a lot of the counts are repeated (like zero counts would be).
                        queue.add(new SimpleFacets.CountPair<String, Integer>(terms[startTermIndex + i], c));
                        if (queue.size() >= maxsize) {
                            min = queue.last().val;
                            break;
                        }
                    }
                }
                // now select the right page from the results
                for (SimpleFacets.CountPair<String, Integer> p : queue) {
                    if (--off >= 0)
                        continue;
                    if (--lim < 0)
                        break;
                    res.add(ft.indexedToReadable(p.key), p.val);
                }
            } else {
                // add results in index order
                int i = 0;
                if (mincount <= 0) {
                    // if mincount<=0, then we won't discard any terms and we know exactly
                    // where to start.
                    i = off;
                    off = 0;
                }

                for (; i < nTerms; i++) {
                    int c = counts[i];
                    if (c < mincount || --off >= 0)
                        continue;
                    if (--lim < 0)
                        break;
                    res.add(ft.indexedToReadable(terms[startTermIndex+i]), c);
                }
            }
        }

        if (missing) {
            res.add(null, getFieldMissingCount(searcher, docs, fieldName, locale));
        }

        return res;
    }

    /**
     * Returns a list of terms in the specified field along with the corresponding count of documents in the set that match that constraint.
     * This method uses the FilterCache to get the intersection count between <code>docs</code> and the DocSet for each term in the filter.
     * 
     * @see FacetParams#FACET_LIMIT
     * @see FacetParams#FACET_ZEROS
     * @see FacetParams#FACET_MISSING
     */
    public NamedList<Object> getFacetTermEnumCounts(IndexSearcher searcher, OpenBitSet docs,
            String field, String fieldName, int offset, int limit, int mincount, boolean missing,
            String sort, String prefix, String locale, ExtendedPropertyDefinition epd) throws IOException {

        /*
         * :TODO: potential optimization... cache the Terms with the highest docFreq and try them first don't enum if we get our max from
         * them
         */

        // Minimum term docFreq in order to use the filterCache for that term.
        int minDfFilterCache = params.getFieldInt(field, FacetParams.FACET_ENUM_CACHE_MINDF, 0);

        IndexReader r = searcher.getIndexReader();
        FieldType ft = getType(epd);

        final int maxsize = limit >= 0 ? offset + limit : Integer.MAX_VALUE - 1;
        final TreeSet<SimpleFacets.CountPair<String, Integer>> queue = (sort.equals("count") || sort.equals("true")) ? new TreeSet<SimpleFacets.CountPair<String, Integer>>()
                : null;
        final NamedList<Object> res = new NamedList<Object>();

        int min = mincount - 1; // the smallest value in the top 'N' values
        int off = offset;
        int lim = limit >= 0 ? limit : Integer.MAX_VALUE;

        String startTerm = prefix == null ? "" : ft.toInternal(prefix);
        TermEnum te = r.terms(new Term(fieldName, startTerm));
        TermDocs td = r.termDocs();
        SolrIndexSearcher.TermDocsState tdState = new SolrIndexSearcher.TermDocsState();
        tdState.tenum = te;
        tdState.tdocs = td;        

        if (docs.size() >= mincount) {
            do {
                Term t = te.term();

                if (null == t || !t.field().equals(fieldName))
                    break;

                if (prefix != null && !t.text().startsWith(prefix))
                    break;

                int df = te.docFreq();

                // If we are sorting, we can use df>min (rather than >=) since we
                // are going in index order. For certain term distributions this can
                // make a large difference (for example, many terms with df=1).
                if (df > 0 && df > min) {
                    int c;

                    if (df >= minDfFilterCache) {
                        // use the filter cache
// TODO: use the new method ???                        
//                        docs.intersectionSize( searcher.getPositiveDocSet(new TermQuery(t), tdState) );
                        c = (int) OpenBitSet.intersectionCount(getDocIdSet(new TermQuery(t), locale), docs);
                    } else {
                        // iterate over TermDocs to calculate the intersection
                        td.seek(te);
                        c = 0;
                        while (td.next()) {
                            int doc = td.doc();
                            if (locale != null) {
                                doc = getMainDocIdForTranslations(searcher.getIndexReader()
                                        .document(doc), locale);
                            }
                            
                            if (docs.fastGet(doc)) {
                                c++;
                            }
                        }
                    }

                    if (sort.equals("count") || sort.equals("true")) {
                        if (c > min) {
                            queue.add(new SimpleFacets.CountPair<String, Integer>(t.text(), c));
                            if (queue.size() >= maxsize) {
                                min = queue.last().val;
                                break;
                            }
                        }
                    } else {
                        if (c >= mincount && --off < 0) {
                            if (--lim < 0)
                                break;
                            res.add(ft.indexedToReadable(t.text()), c);
                        }
                    }
                }
            } while (te.next());
        }

        if (sort.equals("count") || sort.equals("true")) {
            for (SimpleFacets.CountPair<String, Integer> p : queue) {
                if (--off >= 0)
                    continue;
                if (--lim < 0)
                    break;
                res.add(ft.indexedToReadable(p.key), p.val);
            }
        }

        if (missing) {
            res.add(null, getFieldMissingCount(searcher, docs, fieldName, locale));
        }

        te.close();
        td.close();

        return res;
    }

    public String getFieldNameInIndex(String field, String propertyFieldName, ExtendedPropertyDefinition epd, String langCode, boolean useJackrabbitField) {
        String fieldName = propertyFieldName;
        try {
            fieldName = resolver.getJCRName(NameFactoryImpl.getInstance().create(session.getNamespaceURI(epd.getPrefix()),
                    epd.getLocalName()));
            int idx = fieldName.indexOf(':');
            if (epd != null && epd.isFacetable()) {
                fieldName = fieldName.substring(0, idx + 1)
                        + JahiaNodeIndexer.FACET_PREFIX
                        + fieldName.substring(idx + 1);
            } else if ((epd == null || !epd.isFacetable())
                    && useJackrabbitField && params instanceof MapSolrParams) {
                String prefix = params.getFieldParam(field,
                        FacetParams.FACET_PREFIX);
                ((MapSolrParams) params).getMap().put(
                        "f." + field + '.' + FacetParams.FACET_PREFIX,
                        fieldName + (prefix != null ? prefix : ""));
                fieldName = FieldNames.PROPERTIES;
            } else {
                fieldName = fieldName.substring(0, idx + 1)
                        + FieldNames.FULLTEXT_PREFIX
                        + fieldName.substring(idx + 1);
            }
        } catch (RepositoryException e) {
            // will never happen
        }
        return fieldName;
    }

    /**
     * Returns a list of value constraints and the associated facet counts for each facet date field, range, and interval specified in the
     * SolrParams
     * 
     * @see FacetParams#FACET_DATE
     * @deprecated Use getFacetRangeCounts which is more generalized
     */
  @Deprecated
    public NamedList<Object> getFacetDateCounts() throws JahiaException, IOException,
            RepositoryException {

        final NamedList<Object> resOuter = new SimpleOrderedMap<Object>();
        final String[] fields = params.getParams(FacetParams.FACET_DATE);

        if (null == fields || 0 == fields.length) return resOuter;

        for (String f : fields) {
<<<<<<< .working
=======
            final NamedList<Object> resInner = new SimpleOrderedMap<Object>();
            String fieldName = StringUtils.substringBeforeLast(f, PROPNAME_INDEX_SEPARATOR);            
            ExtendedPropertyDefinition epd = NodeTypeRegistry.getInstance().getNodeType(params.get("f."+f+".facet.nodetype")).getPropertyDefinition(fieldName);
            String fieldNameInIndex = getFieldNameInIndex(f, fieldName, epd, params.getFieldParam(f,
                    "facet.locale"), false);
            
            resOuter.add(fieldName + PROPNAME_INDEX_SEPARATOR + fieldNameInIndex, resInner);
            
            if (!(epd.getRequiredType() == PropertyType.DATE)) {
                throw new JahiaException("Can not date facet on a field which is not a DateField: "
                        + f, "Can not date facet on a field which is not a DateField: " + f,
                        JahiaException.PARAMETER_ERROR, JahiaException.ERROR_SEVERITY);
            }
            Integer mincount = params.getFieldInt(f, FacetParams.FACET_MINCOUNT);
            if (mincount == null) {
                Boolean zeros = params.getFieldBool(f, FacetParams.FACET_ZEROS);
                // mincount = (zeros!=null && zeros) ? 0 : 1;
                mincount = (zeros != null && !zeros) ? 1 : 0;
                // current default is to include zeros.
            }
            
            final String startS = required.getFieldParam(f,
                    FacetParams.FACET_DATE_START);
            final Date start;
>>>>>>> .merge-right.r43880
            try {
                getFacetDateCounts(f, resOuter);
              } catch (Exception e) {
                String msg = "Exception during facet.date of " + f;
                logger.warn(msg, e);
                addException(msg , e);
              }            
        }

        return resOuter;
    }
  /**
   * @deprecated Use getFacetRangeCounts which is more generalized
   */
  @Deprecated
  public void getFacetDateCounts(String dateFacet, NamedList<Object> resOuter)
      throws IOException, ParseException, RepositoryException, JahiaException {  
      
      parseParams(FacetParams.FACET_DATE, dateFacet);
      String f = facetValue;      
      
      final NamedList<Object> resInner = new SimpleOrderedMap<Object>();
      String fieldName = StringUtils.substringBeforeLast(f, PROPNAME_INDEX_SEPARATOR);            
      ExtendedPropertyDefinition epd = NodeTypeRegistry.getInstance().getNodeType(params.get("f."+f+".facet.nodetype")).getPropertyDefinition(fieldName);
      String fieldNameInIndex = getFieldNameInIndex(fieldName, epd, params.getFieldParam(f,
              "facet.locale"));
      final SchemaField sf = new SchemaField(fieldNameInIndex, JahiaQueryParser.DATE_TYPE);
      
// TODO: Should we use the key now ?
      //    resOuter.add(key, resInner);
      resOuter.add(fieldName + PROPNAME_INDEX_SEPARATOR + fieldNameInIndex, resInner);
      
      if (!(epd.getRequiredType() == PropertyType.DATE)) {
          throw new SolrException
          (SolrException.ErrorCode.BAD_REQUEST,
              "Can not date facet on a field which is not a DateField: " + f);
      }
      Integer mincount = params.getFieldInt(f, FacetParams.FACET_MINCOUNT);
      if (mincount == null) {
          Boolean zeros = params.getFieldBool(f, FacetParams.FACET_ZEROS);
          // mincount = (zeros!=null && zeros) ? 0 : 1;
          mincount = (zeros != null && !zeros) ? 1 : 0;
          // current default is to include zeros.
      }
      
      final String startS = required.getFieldParam(f,
              FacetParams.FACET_DATE_START);
      final Date start;
      try {
          start = JahiaQueryParser.DATE_TYPE.parseMath(NOW, startS);
      } catch (SolrException e) {
          throw new SolrException
          (SolrException.ErrorCode.BAD_REQUEST,
              "date facet 'start' is not a valid Date string: " + startS, e);
      }
      final String endS = required.getFieldParam(f, FacetParams.FACET_DATE_END);
      Date end; // not final, hardend may change this
      try {
          end = JahiaQueryParser.DATE_TYPE.parseMath(NOW, endS);
      } catch (SolrException e) {
          throw new SolrException
          (SolrException.ErrorCode.BAD_REQUEST,
              "date facet 'end' is not a valid Date string: " + endS, e);
      }

      if (end.before(start)) {
          throw new SolrException
          (SolrException.ErrorCode.BAD_REQUEST,
              "date facet 'end' comes before 'start': "+endS+" < "+startS);
      }

      final String gap = required.getFieldParam(f, FacetParams.FACET_DATE_GAP);
      final DateMathParser dmp = new DateMathParser(DateField.UTC, Locale.US);
      dmp.setNow(NOW);
      
      final int minCount = params.getFieldInt(f,FacetParams.FACET_MINCOUNT, 0);

      String[] iStrs = params.getFieldParams(f,FacetParams.FACET_DATE_INCLUDE);
      // Legacy support for default of [lower,upper,edge] for date faceting
      // this is not handled by FacetRangeInclude.parseParam because
      // range faceting has differnet defaults
      final EnumSet<FacetRangeInclude> include = 
        (null == iStrs || 0 == iStrs.length ) ?
        EnumSet.of(FacetRangeInclude.LOWER, 
                   FacetRangeInclude.UPPER, 
                   FacetRangeInclude.EDGE)
        : FacetRangeInclude.parseParam(iStrs);
      
      try {
          Date low = start;
          while (low.before(end)) {
              dmp.setNow(low);
              String label = JahiaQueryParser.DATE_TYPE.toExternal(low);
              
              Date high = dmp.parseMath(gap);
              if (end.before(high)) {
                  if (params.getFieldBool(f, FacetParams.FACET_DATE_HARD_END,
                          false)) {
                      high = end;
                  } else {
                      end = high;
                  }
              }
              if (high.before(low)) {
                  throw new SolrException
                  (SolrException.ErrorCode.BAD_REQUEST,
                      "date facet infinite loop (is gap negative?)");
              }
              final boolean includeLower =
                  (include.contains(FacetRangeInclude.LOWER) ||
                      (include.contains(FacetRangeInclude.EDGE) && low.equals(start)));
              final boolean includeUpper =
                  (include.contains(FacetRangeInclude.UPPER) ||
                      (include.contains(FacetRangeInclude.EDGE) && high.equals(end)));
                  
              Query rangeQuery = JahiaQueryParser.DATE_TYPE.getRangeQuery(null, sf, low,high,includeLower,includeUpper);
              int count = rangeCount(rangeQuery);
              if (count >= minCount) {
// TODO: Can we use just label here ?                  
                  resInner.add(label + PROPNAME_INDEX_SEPARATOR + rangeQuery.toString(),
                          count);
              }
              low = high;
          }
      } catch (java.text.ParseException e) {
          throw new SolrException
          (SolrException.ErrorCode.BAD_REQUEST,
              "date facet 'gap' is not a valid Date Math string: " + gap, e);
      }

      // explicitly return the gap and end so all the counts are meaningful
      resInner.add("gap", gap);
      resInner.add("start", start);
      resInner.add("end", end);

      final String[] othersP = params.getFieldParams(f,
              FacetParams.FACET_DATE_OTHER);
      if (null != othersP && 0 < othersP.length) {
          final Set<FacetDateOther> others = EnumSet.noneOf(FacetDateOther.class);

          for (final String o : othersP) {
              others.add(FacetDateOther.get(o));
          }

          // no matter what other values are listed, we don't do
          // anything if "none" is specified.
          if (!others.contains(FacetDateOther.NONE)) {
              boolean all = others.contains(FacetDateOther.ALL);

              if (all || others.contains(FacetDateOther.BEFORE)) {
                  Query rangeQuery = JahiaQueryParser.DATE_TYPE.getRangeQuery(null, sf, null, start, false,
                          (include.contains(FacetRangeInclude.OUTER) ||
                                  (! (include.contains(FacetRangeInclude.LOWER) ||
                                      include.contains(FacetRangeInclude.EDGE)))));
                  int count = rangeCount(rangeQuery);
                  if (count >= minCount) {
                      resInner.add(FacetDateOther.BEFORE.toString()
                              + PROPNAME_INDEX_SEPARATOR + rangeQuery.toString(), count);
                  }
              }
              if (all || others.contains(FacetDateOther.AFTER)) {
                    Query rangeQuery = JahiaQueryParser.DATE_TYPE.getRangeQuery(null, sf,
                            end,
                            null,
                            (include.contains(FacetRangeInclude.OUTER) || (!(include
                                    .contains(FacetRangeInclude.UPPER) || include
                                    .contains(FacetRangeInclude.EDGE)))), false);
                  int count = rangeCount(rangeQuery);
                  if (count >= minCount) {
                      resInner.add(FacetDateOther.AFTER.toString() + PROPNAME_INDEX_SEPARATOR
                              + rangeQuery.toString(), count);
                  }
              }
              if (all || others.contains(FacetDateOther.BETWEEN)) {
                  Query rangeQuery = JahiaQueryParser.DATE_TYPE.getRangeQuery(null, sf, start, end,
                          (include.contains(FacetRangeInclude.LOWER) ||
                                  include.contains(FacetRangeInclude.EDGE)),
                              (include.contains(FacetRangeInclude.UPPER) ||
                                  include.contains(FacetRangeInclude.EDGE)));
                  int count = rangeCount(rangeQuery);
                  if (count >= minCount) {
                      resInner.add(FacetDateOther.BETWEEN.toString()
                              + PROPNAME_INDEX_SEPARATOR + rangeQuery.toString(), count);
                  }
              }
          }
      }  
  }

  /**
   * Returns a list of value constraints and the associated facet
   * counts for each facet numerical field, range, and interval
   * specified in the SolrParams
   *
   * @see FacetParams#FACET_RANGE
   */

  public NamedList<Object> getFacetRangeCounts() {
    final NamedList<Object> resOuter = new SimpleOrderedMap<Object>();
    final String[] fields = params.getParams(FacetParams.FACET_RANGE);

    if (null == fields || 0 == fields.length) return resOuter;

    for (String f : fields) {
      try {
        getFacetRangeCounts(f, resOuter);
      } catch (Exception e) {
        String msg = "Exception during facet.range of " + f;
        SolrException.logOnce(SolrCore.log, msg, e);
        addException(msg , e);
      }
    }

    return resOuter;
  }

  void getFacetRangeCounts(String facetRange, NamedList<Object> resOuter)
      throws IOException, ParseException, RepositoryException {

    parseParams(FacetParams.FACET_RANGE, facetRange);
    String f = facetValue;

    String fieldName = StringUtils.substringBeforeLast(f, PROPNAME_INDEX_SEPARATOR);            
    ExtendedPropertyDefinition epd = NodeTypeRegistry.getInstance().getNodeType(params.get("f."+f+".facet.nodetype")).getPropertyDefinition(fieldName);
    String fieldNameInIndex = getFieldNameInIndex(fieldName, epd, params.getFieldParam(f,
            "facet.locale"));
    SchemaField sf = new SchemaField(fieldNameInIndex, getType(epd));
    final FieldType ft = sf.getType();

    RangeEndpointCalculator<?> calc = null;

    if (ft instanceof TrieField) {
      final TrieField trie = (TrieField)ft;

      switch (trie.getType()) {
        case FLOAT:
          calc = new FloatRangeEndpointCalculator(sf);
          break;
        case DOUBLE:
          calc = new DoubleRangeEndpointCalculator(sf);
          break;
        case INTEGER:
          calc = new IntegerRangeEndpointCalculator(sf);
          break;
        case LONG:
          calc = new LongRangeEndpointCalculator(sf);
          break;
        default:
          throw new SolrException
              (SolrException.ErrorCode.BAD_REQUEST,
                  "Unable to range facet on tried field of unexpected type:" + f);
      }
    } else if (ft instanceof DateField) {
      calc = new DateRangeEndpointCalculator(sf, NOW);
    } else if (ft instanceof SortableIntField) {
      calc = new IntegerRangeEndpointCalculator(sf);
    } else if (ft instanceof SortableLongField) {
      calc = new LongRangeEndpointCalculator(sf);
    } else if (ft instanceof SortableFloatField) {
      calc = new FloatRangeEndpointCalculator(sf);
    } else if (ft instanceof SortableDoubleField) {
      calc = new DoubleRangeEndpointCalculator(sf);
    } else {
      throw new SolrException
          (SolrException.ErrorCode.BAD_REQUEST,
              "Unable to range facet on field:" + sf);
    }

    resOuter.add(key, getFacetRangeCounts(sf, f, calc));
  }

  private <T extends Comparable<T>> NamedList<Object> getFacetRangeCounts
    (final SchemaField sf, final String f,
     final RangeEndpointCalculator<T> calc) throws IOException {
    
    final NamedList<Object> res = new SimpleOrderedMap<Object>();
    final NamedList<Object> counts = new NamedList<Object>();
    res.add("counts", counts);

    final T start = calc.getValue(required.getFieldParam(f,FacetParams.FACET_RANGE_START));
    // not final, hardend may change this
    T end = calc.getValue(required.getFieldParam(f,FacetParams.FACET_RANGE_END));
    if (end.compareTo(start) < 0) {
      throw new SolrException
        (SolrException.ErrorCode.BAD_REQUEST,
         "range facet 'end' comes before 'start': "+end+" < "+start);
    }
    
    final String gap = required.getFieldParam(f, FacetParams.FACET_RANGE_GAP);
    // explicitly return the gap.  compute this early so we are more 
    // likely to catch parse errors before attempting math
    res.add("gap", calc.getGap(gap));
    
    final int minCount = params.getFieldInt(f,FacetParams.FACET_MINCOUNT, 0);
    
    final EnumSet<FacetRangeInclude> include = FacetRangeInclude.parseParam
      (params.getFieldParams(f,FacetParams.FACET_RANGE_INCLUDE));
    
    T low = start;
    
    while (low.compareTo(end) < 0) {
      T high = calc.addGap(low, gap);
      if (end.compareTo(high) < 0) {
        if (params.getFieldBool(f,FacetParams.FACET_RANGE_HARD_END,false)) {
          high = end;
        } else {
          end = high;
        }
      }
      if (high.compareTo(low) < 0) {
        throw new SolrException
          (SolrException.ErrorCode.BAD_REQUEST,
           "range facet infinite loop (is gap negative? did the math overflow?)");
      }
      
      final boolean includeLower = 
        (include.contains(FacetRangeInclude.LOWER) ||
         (include.contains(FacetRangeInclude.EDGE) && 
          0 == low.compareTo(start)));
      final boolean includeUpper = 
        (include.contains(FacetRangeInclude.UPPER) ||
         (include.contains(FacetRangeInclude.EDGE) && 
          0 == high.compareTo(end)));
      
      final String lowS = calc.formatValue(low);
      final String highS = calc.formatValue(high);

      Query rangeQ = sf.getType().getRangeQuery(null, sf, lowS, highS,
              includeLower,includeUpper);      
      final int count = rangeCount(rangeQ);
      if (count >= minCount) {
        counts.add(lowS + PROPNAME_INDEX_SEPARATOR + rangeQ.toString(), count);
      }
      
      low = high;
    }
    
    // explicitly return the start and end so all the counts 
    // (including before/after/between) are meaningful - even if mincount
    // has removed the neighboring ranges
    res.add("start", start);
    res.add("end", end);
    
    final String[] othersP =
      params.getFieldParams(f,FacetParams.FACET_RANGE_OTHER);
    if (null != othersP && 0 < othersP.length ) {
      Set<FacetRangeOther> others = EnumSet.noneOf(FacetRangeOther.class);
      
      for (final String o : othersP) {
        others.add(FacetRangeOther.get(o));
      }
      
      // no matter what other values are listed, we don't do
      // anything if "none" is specified.
      if (! others.contains(FacetRangeOther.NONE) ) {
        
        boolean all = others.contains(FacetRangeOther.ALL);
        final String startS = calc.formatValue(start);
        final String endS = calc.formatValue(end);

        if (all || others.contains(FacetRangeOther.BEFORE)) {
          // include upper bound if "outer" or if first gap doesn't already include it
            Query rangeQ = sf.getType().getRangeQuery(null,sf,null,startS,
                    false,
                    (include.contains(FacetRangeInclude.OUTER) ||
                     (! (include.contains(FacetRangeInclude.LOWER) ||
                         include.contains(FacetRangeInclude.EDGE)))));
            int count = rangeCount(rangeQ);            
            res.add(FacetRangeOther.BEFORE.toString(), count);
            counts.add(FacetRangeOther.BEFORE.toString() + PROPNAME_INDEX_SEPARATOR + rangeQ.toString(),
                  count);
        }
        if (all || others.contains(FacetRangeOther.AFTER)) {
          // include lower bound if "outer" or if last gap doesn't already include it
            Query rangeQ = sf.getType().getRangeQuery(null, sf,endS,null,
                    (include.contains(FacetRangeInclude.OUTER) ||
                            (! (include.contains(FacetRangeInclude.UPPER) ||
                                include.contains(FacetRangeInclude.EDGE)))),  
                           false);
            int count = rangeCount(rangeQ);            
            res.add(FacetRangeOther.AFTER.toString(), count);
            counts.add(FacetRangeOther.AFTER.toString() + PROPNAME_INDEX_SEPARATOR + rangeQ.toString(), count);
        }
        if (all || others.contains(FacetRangeOther.BETWEEN)) {
            Query rangeQ = sf.getType().getRangeQuery(null, sf,startS,endS,
                    (include.contains(FacetRangeInclude.LOWER) ||
                            include.contains(FacetRangeInclude.EDGE)),
                           (include.contains(FacetRangeInclude.UPPER) ||
                            include.contains(FacetRangeInclude.EDGE)));
            int count = rangeCount(rangeQ);
            res.add(FacetRangeOther.BETWEEN.toString(), count);
            counts.add(FacetRangeOther.BETWEEN.toString() + PROPNAME_INDEX_SEPARATOR + rangeQ.toString(),
                 count);
        }
      }
    }
    return res;
  }  
  
  /**
   * Macro for getting the numDocs of range over docs
   * @see SolrIndexSearcher#numDocs
   * @see TermRangeQuery
   */
  protected int rangeCount(SchemaField sf, String low, String high,
                           boolean iLow, boolean iHigh) throws IOException {
    Query rangeQ = sf.getType().getRangeQuery(null, sf,low,high,iLow,iHigh);
    return (int)OpenBitSet.intersectionCount(getDocIdSet(rangeQ, ""),
            base);
  }

  /**
   * @deprecated Use rangeCount(SchemaField,String,String,boolean,boolean) which is more generalized
   */
  @Deprecated
  protected int rangeCount(SchemaField sf, Date low, Date high,
                           boolean iLow, boolean iHigh) throws IOException {
    Query rangeQ = ((DateField)(sf.getType())).getRangeQuery(null, sf,low,high,iLow,iHigh);
    return (int)OpenBitSet.intersectionCount(getDocIdSet(rangeQ, ""),
            base);
  }
  
  
    /**
     * Macro for getting the numDocs of a ConstantScoreRangeQuery over docs
     * 
     * @see ConstantScoreRangeQuery
     */
    protected int rangeCount(Query rangeQuery) throws IOException {
        return (int) OpenBitSet.intersectionCount(getDocIdSet(rangeQuery, null),
                docs);
    }

    /**
     * Perhaps someday instead of having a giant "instanceof" case 
     * statement to pick an impl, we can add a "RangeFacetable" marker 
     * interface to FieldTypes and they can return instances of these 
     * directly from some method -- but until then, keep this locked down 
     * and private.
     */
    private static abstract class RangeEndpointCalculator<T extends Comparable<T>> {
      protected final SchemaField field;
      public RangeEndpointCalculator(final SchemaField field) {
        this.field = field;
      }

      /**
       * Formats a Range endpoint for use as a range label name in the response.
       * Default Impl just uses toString()
       */
      public String formatValue(final T val) {
        return val.toString();
      }
      /**
       * Parses a String param into an Range endpoint value throwing 
       * a useful exception if not possible
       */
      public final T getValue(final String rawval) {
        try {
          return parseVal(rawval);
        } catch (Exception e) {
          throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
                                  "Can't parse value "+rawval+" for field: " + 
                                  field.getName(), e);
        }
      }
      /**
       * Parses a String param into an Range endpoint. 
       * Can throw a low level format exception as needed.
       */
      protected abstract T parseVal(final String rawval) 
        throws java.text.ParseException;

      /** 
       * Parses a String param into a value that represents the gap and 
       * can be included in the response, throwing 
       * a useful exception if not possible.
       *
       * Note: uses Object as the return type instead of T for things like 
       * Date where gap is just a DateMathParser string 
       */
      public final Object getGap(final String gap) {
        try {
          return parseGap(gap);
        } catch (Exception e) {
          throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
                                  "Can't parse gap "+gap+" for field: " + 
                                  field.getName(), e);
        }
      }

      /**
       * Parses a String param into a value that represents the gap and 
       * can be included in the response. 
       * Can throw a low level format exception as needed.
       *
       * Default Impl calls parseVal
       */
      protected Object parseGap(final String rawval) 
        throws java.text.ParseException {
        return parseVal(rawval);
      }

      /**
       * Adds the String gap param to a low Range endpoint value to determine 
       * the corrisponding high Range endpoint value, throwing 
       * a useful exception if not possible.
       */
      public final T addGap(T value, String gap) {
        try {
          return parseAndAddGap(value, gap);
        } catch (Exception e) {
          throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
                                  "Can't add gap "+gap+" to value " + value +
                                  " for field: " + field.getName(), e);
        }
      }
      /**
       * Adds the String gap param to a low Range endpoint value to determine 
       * the corrisponding high Range endpoint value.
       * Can throw a low level format exception as needed.
       */
      protected abstract T parseAndAddGap(T value, String gap) 
        throws java.text.ParseException;

    }

    private static class FloatRangeEndpointCalculator 
      extends RangeEndpointCalculator<Float> {

      public FloatRangeEndpointCalculator(final SchemaField f) { super(f); }
      @Override
      protected Float parseVal(String rawval) {
        return Float.valueOf(rawval);
      }
      @Override
      public Float parseAndAddGap(Float value, String gap) {
        return new Float(value.floatValue() + Float.valueOf(gap).floatValue());
      }
    }
    private static class DoubleRangeEndpointCalculator 
      extends RangeEndpointCalculator<Double> {

      public DoubleRangeEndpointCalculator(final SchemaField f) { super(f); }
      @Override
      protected Double parseVal(String rawval) {
        return Double.valueOf(rawval);
      }
      @Override
      public Double parseAndAddGap(Double value, String gap) {
        return new Double(value.floatValue() + Double.valueOf(gap).floatValue());
      }
    }
    private static class IntegerRangeEndpointCalculator 
      extends RangeEndpointCalculator<Integer> {

      public IntegerRangeEndpointCalculator(final SchemaField f) { super(f); }
      @Override
      protected Integer parseVal(String rawval) {
        return Integer.valueOf(rawval);
      }
      @Override
      public Integer parseAndAddGap(Integer value, String gap) {
        return new Integer(value.intValue() + Integer.valueOf(gap).intValue());
      }
    }
    private static class LongRangeEndpointCalculator 
      extends RangeEndpointCalculator<Long> {

      public LongRangeEndpointCalculator(final SchemaField f) { super(f); }
      @Override
      protected Long parseVal(String rawval) {
        return Long.valueOf(rawval);
      }
      @Override
      public Long parseAndAddGap(Long value, String gap) {
        return new Long(value.intValue() + Long.valueOf(gap).intValue());
      }
    }
    private static class DateRangeEndpointCalculator 
      extends RangeEndpointCalculator<Date> {
      private final Date now;
      public DateRangeEndpointCalculator(final SchemaField f, 
                                         final Date now) { 
        super(f); 
        this.now = now;
        if (! (field.getType() instanceof DateField) ) {
          throw new IllegalArgumentException
            ("SchemaField must use filed type extending DateField");
        }
      }
      @Override
      public String formatValue(Date val) {
        return ((DateField)field.getType()).toExternal(val);
      }
      @Override
      protected Date parseVal(String rawval) {
        return ((DateField)field.getType()).parseMath(now, rawval);
      }
      @Override
      protected Object parseGap(final String rawval) {
        return rawval;
      }
      @Override
      public Date parseAndAddGap(Date value, String gap) throws java.text.ParseException {
        final DateMathParser dmp = new DateMathParser(DateField.UTC, Locale.US);
        dmp.setNow(value);
        return dmp.parseMath(gap);
      }
    }
        
    private OpenBitSet getDocIdSet(Query query, final String locale) {
        OpenBitSet docIds = null;
        try {
            final BitSet bitset = new BitSet();
            searcher.search(query, new AbstractHitCollector() {
                @Override
                public void collect(int docId, float scorer) {
                    if (locale != null) {
                        try {
                            int docMainDocId = getMainDocIdForTranslations(searcher.getIndexReader().document(docId, TRANSLATION_FIELDS), locale);
                            if (docMainDocId != -1) {
                                bitset.set(docMainDocId);                        
                            }
                        } catch (Exception e) {
                            logger.warn("Error getting index document while faceting", e);
                        }                        
                    }
                    bitset.set(docId);
                }
                
                @Override
                public boolean acceptsDocsOutOfOrder() {
                    return true;
                }
            });

            docIds = new OpenBitSetDISI(new DocIdBitSet(bitset).iterator(), bitset.size());
        } catch (IOException e) {
            logger.debug("Can't retrive bitset from hits", e);
        }
        return docIds;
    }
    
    public static final FieldSelector TRANSLATION_FIELDS = new FieldSelector() {
        private static final long serialVersionUID = -1570508136556374240L;

        /**
         * Accepts {@link FieldNames#UUID} and {@link FieldNames#PARENT}.
         *
         * @param fieldName the field name to check.
         * @return result.
         */
        public FieldSelectorResult accept(String fieldName) {
            if (JahiaNodeIndexer.TRANSLATED_NODE_PARENT == fieldName) {
                return FieldSelectorResult.LOAD;
            } else if (JahiaNodeIndexer.TRANSLATION_LANGUAGE == fieldName) {
                return FieldSelectorResult.LOAD;
            } else if (FieldNames.PARENT == fieldName) {
                return FieldSelectorResult.LOAD;
            } else {
                return FieldSelectorResult.NO_LOAD;
            }
        }
    };
    
    private static Query matchAllDocsQuery = new MatchAllDocsQuery();
    
    /**
     * Returns the set of document ids matching all queries.
     * This method is cache-aware and attempts to retrieve the answer from the cache if possible.
     * If the answer was not cached, it may have been inserted into the cache as a result of this call.
     * This method can handle negative queries.
     * <p>
     * The DocSet returned should <b>not</b> be modified.
     */
    public OpenBitSet getDocIdSet(List<Query> queries, String locale) throws IOException {
      if (queries==null) return null;
      if (queries.size()==1) return getDocIdSet(queries.get(0), locale);
      OpenBitSet answer=null;

      boolean[] neg = new boolean[queries.size()];
      OpenBitSet[] sets = new OpenBitSet[queries.size()];

      int smallestIndex = -1;
      int smallestCount = Integer.MAX_VALUE;
      for (int i=0; i<sets.length; i++) {
        Query q = queries.get(i);
        Query posQuery = getAbs(q);
        sets[i] = getPositiveDocSet(posQuery, locale);
        // Negative query if absolute value different from original
        if (q==posQuery) {
          neg[i] = false;
          // keep track of the smallest positive set.
          // This optimization is only worth it if size() is cached, which it would
          // be if we don't do any set operations.
          int sz = (int)sets[i].size();
          if (sz<smallestCount) {
            smallestCount=sz;
            smallestIndex=i;
            answer = sets[i];
          }
        } else {
          neg[i] = true;
        }
      }

      // if no positive queries, start off with all docs
      if (answer==null) answer = getPositiveDocSet(matchAllDocsQuery, locale);

      // do negative queries first to shrink set size
      for (int i=0; i<sets.length; i++) {
        if (neg[i]) answer.andNot(sets[i]);
      }

      for (int i=0; i<sets.length; i++) {
        if (!neg[i] && i!=smallestIndex) answer.intersect(sets[i]);
      }

      return answer;
    }
    
    /** Returns the original query if it was already a positive query, otherwise
     * return the negative of the query (i.e., a positive query).
     * <p>
     * Example: both id:10 and id:-10 will return id:10
     * <p>
     * The caller can tell the sign of the original by a reference comparison between
     * the original and returned query.
     * @param q
     * @return
     */
    static Query getAbs(Query q) {
      if (!(q instanceof BooleanQuery)) return q;
      BooleanQuery bq = (BooleanQuery)q;

      List<BooleanClause> clauses = bq.clauses();
      if (clauses.size()==0) return q;


      for (BooleanClause clause : clauses) {
        if (!clause.isProhibited()) return q;
      }

      if (clauses.size()==1) {
        // if only one clause, dispense with the wrapping BooleanQuery
        Query negClause = clauses.get(0).getQuery();
        // we shouldn't need to worry about adjusting the boosts since the negative
        // clause would have never been selected in a positive query, and hence would
        // not contribute to a score.
        return negClause;
      } else {
        BooleanQuery newBq = new BooleanQuery(bq.isCoordDisabled());
        newBq.setBoost(bq.getBoost());
        // ignore minNrShouldMatch... it doesn't make sense for a negative query

        // the inverse of -a -b is a OR b
        for (BooleanClause clause : clauses) {
          newBq.add(clause.getQuery(), BooleanClause.Occur.SHOULD);
        }
        return newBq;
      }
    }    
    
    // only handle positive (non negative) queries
    OpenBitSet getPositiveDocSet(Query q, final String locale) throws IOException {
        OpenBitSet answer;
        
//      if (filterCache != null) {
//        answer = filterCache.get(q);
//        if (answer!=null) return answer;
//      }
        final BitSet bitset = new BitSet();
        searcher.search(q, new AbstractHitCollector() {
            @Override
            public void collect(int docId, float scorer) {
                if (locale != null) {
                    try {
                        int docMainDocId = getMainDocIdForTranslations(searcher.getIndexReader().document(docId, TRANSLATION_FIELDS), locale);
                        if (docMainDocId != -1) {
                            bitset.set(docMainDocId);                        
                        }
                    } catch (Exception e) {
                        logger.warn("Error getting index document while faceting", e);
                    }
                }
                bitset.set(docId);
            }
            
            @Override
            public boolean acceptsDocsOutOfOrder() {
                return true;
            }
        });
        answer = new OpenBitSetDISI(new DocIdBitSet(bitset).iterator(), bitset.size());        
//      answer = getDocSetNC(q,null);
//      if (filterCache != null) filterCache.put(q,answer);
      return answer;
    }
    
    // inherit javadoc
    public int maxDoc() throws IOException {
      return searcher.getIndexReader().maxDoc();
    }    
    
    private int getMainDocIdForTranslations(Document doc, String locale) {
        int docId = -1;
        Field parentNode = doc.getField(JahiaNodeIndexer.TRANSLATED_NODE_PARENT);
        if (parentNode != null
                && (StringUtils.isEmpty(locale) || locale.equals(doc.getField(
                        JahiaNodeIndexer.TRANSLATION_LANGUAGE).stringValue()))) {
            try {
                String id = doc.getField(FieldNames.PARENT).stringValue();
                TermDocs docs = searcher.getIndexReader().termDocs(new Term(FieldNames.UUID, id));
                try {
                    if (docs.next()) {
                        return docs.doc();
                    } else {
                        throw new IOException("Node with id " + id + " not found in index");
                    }
                } finally {
                    docs.close();
                }                
            } catch (IOException e) {
                logger.debug("Can't retrive parent node of translation node", e);
            }
        }
        return docId;
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
                type = JahiaQueryParser.DOUBLE_TYPE;
                break;              
            case PropertyType.LONG:
                type = JahiaQueryParser.LONG_TYPE;
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
