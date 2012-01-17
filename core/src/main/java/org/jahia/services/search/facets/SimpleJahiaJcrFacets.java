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
import org.apache.jackrabbit.core.query.lucene.SearchIndex;
import org.apache.jackrabbit.spi.commons.conversion.NamePathResolver;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
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
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.RequiredSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.params.FacetParams.FacetDateOther;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.schema.DateField;
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
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.EnumSet;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

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

    /** The main set of documents all facet counts should be relative to */
    protected OpenBitSet docs;
    /** Configuration params behavior should be driven by */
    protected SolrParams params;
    /** Searcher to use for all calculations */
    protected IndexSearcher searcher;

    protected Analyzer defaultAnalyzer;
    /**
     * Name and Path resolver.
     */
    protected final NamePathResolver resolver;
    
    /**
     * Name and Path resolver.
     */
    protected final Session session;    

    public SimpleJahiaJcrFacets(IndexSearcher searcher, OpenBitSet docs,
            SolrParams params, SearchIndex index, Session session) {
        this.searcher = searcher;
        this.docs = docs;
        this.params = params;
        this.resolver = NamePathResolverImpl.create(index.getNamespaceMappings());
        this.defaultAnalyzer = index.getTextAnalyzer();
        this.session = session;
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

        NamedList<Object> res = new SimpleOrderedMap<Object>();
        try {

            res.add("facet_queries", getFacetQueryCounts());
            res.add("facet_fields", getFacetFieldCounts());
            res.add("facet_dates", getFacetDateCounts());

        } catch (Exception e) {
            logger.warn("Exception during facet counts", e);
            res.add("exception", e.toString());
        }
        return res;
    }

    /**
     * Returns a list of facet counts for each of the facet queries specified in the params
     * 
     * @see FacetParams#FACET_QUERY
     */
    public NamedList<Object> getFacetQueryCounts() throws IOException, ParseException {

        NamedList<Object> res = new SimpleOrderedMap<Object>();

        /*
         * Ignore SolrParams.DF - could have init param facet.query assuming the schema default with query param DF intented to only affect
         * Q. If user doesn't want schema default for facet.query, they should be explicit.
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
                QueryParser qp = new JahiaQueryParser(FieldNames.FULLTEXT, new KeywordAnalyzer());
                qp.setLowercaseExpandedTerms(false);
                Query qobj = qp.parse(q);
                long count = OpenBitSet.intersectionCount(getDocIdSetForHits(searcher.search(qobj), ""),
                        docs);
                if (count >= mincount) {
                    res.add(q, count);
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
        boolean sort = params.getFieldBool(field, FacetParams.FACET_SORT, limit > 0);
        String prefix = params.getFieldParam(field, FacetParams.FACET_PREFIX);

        NamedList<Object> counts;

        if (epd.isMultiple() || epd.getIndex() == ExtendedPropertyDefinition.INDEXED_TOKENIZED
                || epd.getRequiredType() == PropertyType.BOOLEAN) {
            // Always use filters for booleans... we know the number of values is very small.
            counts = getFacetTermEnumCounts(searcher, docs, field, fieldNameInIndex, offset, limit, mincount,
                    missing, sort, prefix, epd.isInternationalized() ? (locale == null ? "" : locale): null);
        } else {
            // TODO: future logic could use filters instead of the fieldcache if
            // the number of terms in the field is small enough.
            counts = getFieldCacheCounts(searcher, docs, fieldNameInIndex, offset, limit, mincount, missing, sort,
                    prefix, epd.isInternationalized() ? (locale == null ? "" : locale): null);
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
                    String fieldNameInIndex = getFieldNameInIndex(fieldName, epd, locale);
                    res.add(StringUtils.substringBeforeLast(f, PROPNAME_INDEX_SEPARATOR)
                            + PROPNAME_INDEX_SEPARATOR + fieldNameInIndex,
                            ensureSorting(params.getFieldParam(f, "facet.sort"), getTermCounts(f, epd, fieldNameInIndex, locale), epd, params.getFieldParam(f, "facet.labelRenderer"), LanguageCodeConverters.getLocaleFromCode(locale)));
                } catch (RepositoryException e) {
                    logger.error("Cant display facets for: " + f, e);
                }
            }
        }
        return res;
    }
    
    private NamedList<Object> ensureSorting(String fieldSort,
            NamedList<Object> values, ExtendedPropertyDefinition fieldPropertyType,
            String facetValueRenderer, Locale locale) {
        ChoiceListRenderer renderer = !StringUtils.isEmpty(facetValueRenderer) ? ChoiceListRendererService
                .getInstance().getRenderers().get(facetValueRenderer)
                : null;
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

    /**
     * Returns a count of the documents in the set which do not have any terms for for the specified field.
     * 
     * @see FacetParams#FACET_MISSING
     */
    public int getFieldMissingCount(IndexSearcher searcher, OpenBitSet docs, String fieldName, String locale)
            throws IOException {
        Query query = null;
        if (StringUtils.isEmpty(locale)) {
            query = new ConstantScoreRangeQuery(fieldName, null, null, false, false);
        } else {
            BooleanQuery booleanQuery = new BooleanQuery();
            booleanQuery.add(new ConstantScoreRangeQuery(fieldName, null, null, false, false),
                    BooleanClause.Occur.MUST);
            booleanQuery.add(
                    new TermQuery(new Term(JahiaNodeIndexer.TRANSLATION_LANGUAGE, locale)),
                    BooleanClause.Occur.MUST);
            query = booleanQuery;
        }
        OpenBitSet hasVal = getDocIdSetForHits(searcher.search(query), locale);
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
            String fieldName, int offset, int limit, int mincount, boolean missing, boolean sort,
            String prefix, String locale) throws IOException {
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
            while (iter.next()) {
                int term = termNum[iter.doc()];
                int arrIdx = term - startTermIndex;
                if (arrIdx >= 0 && arrIdx < nTerms)
                    counts[arrIdx]++;
            }

            // IDEA: we could also maintain a count of "other"... everything that fell outside
            // of the top 'N'

            int off = offset;
            int lim = limit >= 0 ? limit : Integer.MAX_VALUE;

            if (sort) {
                int maxsize = limit > 0 ? offset + limit : Integer.MAX_VALUE - 1;
                maxsize = Math.min(maxsize, nTerms);
                final TreeSet<CountPair<String, Integer>> queue = new TreeSet<CountPair<String, Integer>>();
                int min = mincount - 1; // the smallest value in the top 'N' values
                for (int i = 0; i < nTerms; i++) {
                    int c = counts[i];
                    if (c > min) {
                        // NOTE: we use c>min rather than c>=min as an optimization because we are going in
                        // index order, so we already know that the keys are ordered. This can be very
                        // important if a lot of the counts are repeated (like zero counts would be).
                        queue.add(new CountPair<String, Integer>(terms[startTermIndex + i], c));
                        if (queue.size() >= maxsize) {
                            min = queue.last().val;
                            break;
                        }
                    }
                }
                // now select the right page from the results
                for (CountPair<String, Integer> p : queue) {
                    if (--off >= 0)
                        continue;
                    if (--lim < 0)
                        break;
                    res.add(p.key, p.val);
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
                    res.add(terms[startTermIndex + i], c);
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
            boolean sort, String prefix, String locale) throws IOException {

        /*
         * :TODO: potential optimization... cache the Terms with the highest docFreq and try them first don't enum if we get our max from
         * them
         */

        // Minimum term docFreq in order to use the filterCache for that term.
        int minDfFilterCache = params.getFieldInt(field, FacetParams.FACET_ENUM_CACHE_MINDF, 0);

        IndexReader r = searcher.getIndexReader();

        final int maxsize = limit >= 0 ? offset + limit : Integer.MAX_VALUE - 1;
        final TreeSet<CountPair<String, Integer>> queue = sort ? new TreeSet<CountPair<String, Integer>>()
                : null;
        final NamedList<Object> res = new NamedList<Object>();

        int min = mincount - 1; // the smallest value in the top 'N' values
        int off = offset;
        int lim = limit >= 0 ? limit : Integer.MAX_VALUE;

        String startTerm = prefix == null ? "" : prefix; // ft.toInternal(prefix);
        TermEnum te = r.terms(new Term(fieldName, startTerm));
        TermDocs td = r.termDocs();

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
                        c = (int) OpenBitSet.intersectionCount(getDocIdSetForHits(searcher
                                .search(new TermQuery(t)), locale), docs);
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

                    if (sort) {
                        if (c > min) {
                            queue.add(new CountPair<String, Integer>(t.text(), c));
                            if (queue.size() >= maxsize) {
                                min = queue.last().val;
                                break;
                            }
                        }
                    } else {
                        if (c >= mincount && --off < 0) {
                            if (--lim < 0)
                                break;
                            res.add(t.text(), c);
                        }
                    }
                }
            } while (te.next());
        }

        if (sort) {
            for (CountPair<String, Integer> p : queue) {
                if (--off >= 0)
                    continue;
                if (--lim < 0)
                    break;
                res.add(p.key, p.val);
            }
        }

        if (missing) {
            res.add(null, getFieldMissingCount(searcher, docs, fieldName, locale));
        }

        te.close();
        td.close();

        return res;
    }

    public String getFieldNameInIndex(String field, ExtendedPropertyDefinition epd, String langCode) {
        String fieldName = field;
        try {
            fieldName = resolver.getJCRName(NameFactoryImpl.getInstance().create(session.getNamespaceURI(epd.getPrefix()),
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

    /**
     * Returns a list of value constraints and the associated facet counts for each facet date field, range, and interval specified in the
     * SolrParams
     * 
     * @see FacetParams#FACET_DATE
     */
    public NamedList<Object> getFacetDateCounts() throws JahiaException, IOException,
            RepositoryException {

        final SolrParams required = new RequiredSolrParams(params);
        final NamedList<Object> resOuter = new SimpleOrderedMap<Object>();
        final String[] fields = params.getParams(FacetParams.FACET_DATE);
        final Date NOW = new Date();

        if (null == fields || 0 == fields.length)
            return resOuter;

        for (String f : fields) {
            final NamedList<Object> resInner = new SimpleOrderedMap<Object>();
            String fieldName = StringUtils.substringBeforeLast(f, PROPNAME_INDEX_SEPARATOR);            
            ExtendedPropertyDefinition epd = NodeTypeRegistry.getInstance().getNodeType(params.get("f."+f+".facet.nodetype")).getPropertyDefinition(fieldName);
            String fieldNameInIndex = getFieldNameInIndex(fieldName, epd, params.getFieldParam(f,
                    "facet.locale"));
            
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
            try {
                start = JahiaQueryParser.DATE_TYPE.parseMath(NOW, startS);
            } catch (SolrException e) {
                throw new JahiaException(
                        "date facet 'start' is not a valid Date string: " + startS,
                        "date facet 'start' is not a valid Date string: " + startS,
                        JahiaException.PARAMETER_ERROR, JahiaException.ERROR_SEVERITY, e);
            }
            final String endS = required.getFieldParam(f, FacetParams.FACET_DATE_END);
            Date end; // not final, hardend may change this
            try {
                end = JahiaQueryParser.DATE_TYPE.parseMath(NOW, endS);
            } catch (SolrException e) {
                throw new JahiaException("date facet 'end' is not a valid Date string: " + endS,
                        "date facet 'end' is not a valid Date string: " + endS,
                        JahiaException.PARAMETER_ERROR, JahiaException.ERROR_SEVERITY, e);
            }

            if (end.before(start)) {
                throw new JahiaException("date facet 'end' comes before 'start': " + endS + " < "
                        + startS,
                        "date facet 'end' comes before 'start': " + endS + " < " + startS,
                        JahiaException.PARAMETER_ERROR, JahiaException.ERROR_SEVERITY);
            }

            final String gap = required.getFieldParam(f, FacetParams.FACET_DATE_GAP);
            final DateMathParser dmp = new DateMathParser(DateField.UTC, Locale.US);
            dmp.setNow(NOW);
            try {

                Date low = start;
                while (low.before(end)) {
                    dmp.setNow(low);
                    final String lowI = JahiaQueryParser.DATE_TYPE.toInternal(low);
                    final String label = JahiaQueryParser.DATE_TYPE.indexedToReadable(lowI, true);
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
                        throw new JahiaException("date facet infinite loop (is gap negative?)",
                                "date facet infinite loop (is gap negative?)",
                                JahiaException.PARAMETER_ERROR, JahiaException.ERROR_SEVERITY);
                    }
                    final String highI = JahiaQueryParser.DATE_TYPE.toInternal(high);
                    Query rangeQuery = getRangeQuery(fieldNameInIndex, lowI, highI, true, true);
                    int count = rangeCount(rangeQuery);
                    if (count >= mincount) {
                        resInner.add(label + PROPNAME_INDEX_SEPARATOR + rangeQuery.toString(),
                                count);
                    }
                    low = high;
                }
            } catch (java.text.ParseException e) {
                throw new JahiaException(
                        "date facet 'gap' is not a valid Date Math string: " + gap,
                        "date facet 'gap' is not a valid Date Math string: " + gap,
                        JahiaException.PARAMETER_ERROR, JahiaException.ERROR_SEVERITY, e);
            }

            // explicitly return the gap and end so all the counts are meaningful
            resInner.add("gap", gap);
            resInner.add("end", end);

            final String[] othersP = params.getFieldParams(f,
                    FacetParams.FACET_DATE_OTHER);
            if (null != othersP && 0 < othersP.length) {
                Set<FacetDateOther> others = EnumSet.noneOf(FacetDateOther.class);

                for (final String o : othersP) {
                    others.add(FacetDateOther.get(o));
                }

                // no matter what other values are listed, we don't do
                // anything if "none" is specified.
                if (!others.contains(FacetDateOther.NONE)) {
                    final String startI = JahiaQueryParser.DATE_TYPE.toInternal(start);
                    final String endI = JahiaQueryParser.DATE_TYPE.toInternal(end);

                    boolean all = others.contains(FacetDateOther.ALL);

                    if (all || others.contains(FacetDateOther.BEFORE)) {
                        Query rangeQuery = getRangeQuery(fieldNameInIndex, null, startI, false,
                                false);
                        int count = rangeCount(rangeQuery);
                        if (count >= mincount) {
                            resInner.add(FacetDateOther.BEFORE.toString()
                                    + PROPNAME_INDEX_SEPARATOR + rangeQuery.toString(), count);
                        }
                    }
                    if (all || others.contains(FacetDateOther.AFTER)) {
                        Query rangeQuery = getRangeQuery(fieldNameInIndex, endI, null, false, false);
                        int count = rangeCount(rangeQuery);
                        if (count >= mincount) {
                            resInner.add(FacetDateOther.AFTER.toString() + PROPNAME_INDEX_SEPARATOR
                                    + rangeQuery.toString(), count);
                        }
                    }
                    if (all || others.contains(FacetDateOther.BETWEEN)) {
                        Query rangeQuery = getRangeQuery(fieldNameInIndex, startI, endI, true, true);
                        int count = rangeCount(rangeQuery);
                        if (count >= mincount) {
                            resInner.add(FacetDateOther.BETWEEN.toString()
                                    + PROPNAME_INDEX_SEPARATOR + rangeQuery.toString(), count);
                        }
                    }
                }
            }
        }

        return resOuter;
    }

    /**
     * Macro for getting the numDocs of a ConstantScoreRangeQuery over docs
     * 
     * @see ConstantScoreRangeQuery
     */
    protected int rangeCount(Query rangeQuery) throws IOException {
        return (int) OpenBitSet.intersectionCount(getDocIdSetForHits(searcher.search(rangeQuery), null),
                docs);
    }

    protected Query getRangeQuery(String field, String low, String high, boolean iLow, boolean iHigh) {
        return new ConstantScoreRangeQuery(field, low, high, iLow, iHigh);
    }

    /**
     * A simple key=>val pair whose natural order is such that <b>higher</b> vals come before lower vals. In case of tie vals, then
     * <b>lower</b> keys come before higher keys.
     */
    public static class CountPair<K extends Comparable<? super K>, V extends Comparable<? super V>>
            implements Comparable<CountPair<K, V>> {

        public CountPair(K k, V v) {
            key = k;
            val = v;
        }

        public K key;
        public V val;

        public int hashCode() {
            return key.hashCode() ^ val.hashCode();
        }

        public boolean equals(Object o) {
            return (o instanceof CountPair) && (0 == this.compareTo((CountPair<K, V>) o));
        }

        public int compareTo(CountPair<K, V> o) {
            int vc = o.val.compareTo(val);
            return (0 != vc ? vc : key.compareTo(o.key));
        }
    }

    private OpenBitSet getDocIdSetForHits(Hits hits, String locale) {
        OpenBitSet docIds = null;
        try {
            BitSet bitset = new BitSet();
            int docId;
            for (Iterator<Hit> it = hits.iterator(); it.hasNext();) {
                if (locale == null) {
                    docId = it.next().getId();
                } else {
                    Hit hit = it.next();
                    Document doc = hit.getDocument();
                    docId = getMainDocIdForTranslations(doc, locale);
                    if (docId == -1) {
                        docId = hit.getId();
                    } else {
                        bitset.set(hit.getId());                        
                    }
                }
                bitset.set(docId);
            }
            docIds = new OpenBitSetDISI(new DocIdBitSet(bitset).iterator(), bitset.size());
        } catch (IOException e) {
            logger.debug("Can't retrive bitset from hits", e);
        }
        return docIds;
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
}
