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
package org.jahia.services.search.lucene;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ISOLatin1AccentFilter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.jahia.services.search.JahiaSearchConstant;
import org.jahia.services.search.NumberPadding;

/**
 * Jahia QueryParser that is used to rewrite lucene query in order to support score boost factor.
 * 
 */
public class JahiaLuceneQueryParser extends QueryParser {

    public static float METADATA_SCORE_BOOST = 2;

    private ResourceMapping resourceMapping = null;
    private Map<String, Set<String>> fieldsGrouping = null;
    private int siteId;

    /**
     * Creates a MultiFieldQueryParser.
     * 
     * <p>
     * It will, when parse(String query) is called, construct a query like this (assuming the query consists of two terms and you specify
     * the two fields <code>title</code> and <code>body</code>):
     * </p>
     * 
     * <code>
     * (title:term1 body:term1) (title:term2 body:term2)
     * </code>
     * 
     * <p>
     * When setDefaultOperator(AND_OPERATOR) is set, the result will be:
     * </p>
     * 
     * <code>
     * +(title:term1 body:term1) +(title:term2 body:term2)
     * </code>
     * 
     * <p>
     * In other words, all the query's terms must appear, but it doesn't matter in what fields they appear.
     * </p>
     */
    public JahiaLuceneQueryParser(String field,
            Map<String, Set<String>> fieldsGrouping, int siteId, Analyzer analyzer, ResourceMapping resourceMapping) {
        super(field, analyzer);
        this.resourceMapping = resourceMapping;
        this.fieldsGrouping = fieldsGrouping;
        this.siteId = siteId;
    }

    protected Query getFieldQuery(String field, String queryText, int slop)
            throws ParseException {
        String paddedText = NumberPadding.pad(queryText);
        queryText = NumberPadding.unpad(queryText);
        boolean addPadded = !paddedText.equals(queryText);
        float boost = 1;
        if (field != null) {
            Set<String> l = fieldsGrouping.get(siteId > 0 ? siteId + "_"
                    + field : field);
            if (l == null || l.isEmpty()) {
                ResourcePropertyMapping mapping = resourceMapping
                        .getResourcePropertyMapping(field);
                if (mapping != null) {
                    boost = mapping.getBoost();
                    if (boost != 1) {
                        l = new HashSet<String>();
                        l.add(field);
                    }
                }
            }
            if (l != null && !l.isEmpty()) {
                Vector<BooleanClause> clauses = new Vector<BooleanClause>();

                for (String value : l) {
                    ResourcePropertyMapping mapping = resourceMapping
                            .getResourcePropertyMapping(value);
                    boost = mapping != null ? mapping.getBoost() : 1;

                    Query q = super.getFieldQuery(value, queryText);
                    if (q != null) {
                        Query paddedQuery = addPadded ? super.getFieldQuery(
                                value, paddedText) : null;
                        q.setBoost(boost);
                        if (addPadded) {
                            paddedQuery.setBoost(boost);
                        }

                        if (q instanceof PhraseQuery) {
                            ((PhraseQuery) q).setSlop(slop);
                            if (addPadded) {
                                ((PhraseQuery) paddedQuery).setSlop(slop);
                            }
                        }
                        if (q instanceof MultiPhraseQuery) {
                            ((MultiPhraseQuery) q).setSlop(slop);
                            if (addPadded) {
                                ((MultiPhraseQuery) paddedQuery).setSlop(slop);
                            }
                        }
                        clauses.add(new BooleanClause(q,
                                BooleanClause.Occur.SHOULD));
                        if (addPadded) {
                            clauses.add(new BooleanClause(paddedQuery,
                                    BooleanClause.Occur.SHOULD));
                        }
                    }
                }
                if (clauses.isEmpty()) // happens for stopwords
                    return null;
                return getBooleanQuery(clauses, true);
            }
        }
        if (addPadded) {
            Vector<BooleanClause> clauses = new Vector<BooleanClause>();
            Query q = super.getFieldQuery(field, queryText);
            Query paddedQuery = super.getFieldQuery(field, paddedText);
            clauses.add(new BooleanClause(q, BooleanClause.Occur.SHOULD));
            clauses.add(new BooleanClause(paddedQuery,
                    BooleanClause.Occur.SHOULD));
            return getBooleanQuery(clauses, true);
        }

        return super.getFieldQuery(field, queryText);
    }

    protected Query getFieldQuery(String field, String queryText)
            throws ParseException {
        return getFieldQuery(field, queryText, 0);
    }

    protected Query getFuzzyQuery(String field, String termStr,
            float minSimilarity) throws ParseException {
        float boost = 1;
        String paddedText = NumberPadding.pad(termStr);
        String queryText = NumberPadding.unpad(termStr);
        boolean addPadded = !paddedText.equals(queryText);
        if (field != null) {
            Set<String> l = fieldsGrouping.get(siteId > 0 ? siteId + "_"
                    + field : field);
            if (l == null || l.isEmpty()) {
                ResourcePropertyMapping mapping = resourceMapping
                        .getResourcePropertyMapping(field);
                if (mapping != null) {
                    boost = mapping.getBoost();
                    if (boost != 1) {
                        l = new HashSet<String>();
                        l.add(field);
                    }
                }
            }
            if (l != null && !l.isEmpty()) {
                Vector<BooleanClause> clauses = new Vector<BooleanClause>();

                for (String value : l) {
                    ResourcePropertyMapping mapping = resourceMapping
                            .getResourcePropertyMapping(value);
                    if (mapping != null) {
                        boost = mapping.getBoost();
                    }
                    Query q = super.getFuzzyQuery(value, queryText,
                            minSimilarity);
                    Query paddedQ = addPadded ? super.getFuzzyQuery(value,
                            paddedText, minSimilarity) : null;
                    q.setBoost(boost);
                    if (addPadded) {
                        paddedQ.setBoost(boost);
                    }
                    clauses
                            .add(new BooleanClause(q,
                                    BooleanClause.Occur.SHOULD));
                    if (addPadded) {
                        clauses.add(new BooleanClause(paddedQ,
                                BooleanClause.Occur.SHOULD));
                    }
                }
                return getBooleanQuery(clauses, true);
            }
        }
        if (addPadded) {
            Query q = super.getFuzzyQuery(field, queryText, minSimilarity);
            Query paddedQ = super.getFuzzyQuery(field, paddedText,
                    minSimilarity);
            Vector<BooleanClause> clauses = new Vector<BooleanClause>();
            clauses.add(new BooleanClause(q, BooleanClause.Occur.SHOULD));
            clauses.add(new BooleanClause(paddedQ, BooleanClause.Occur.SHOULD));
            return getBooleanQuery(clauses, true);
        }
        return super.getFuzzyQuery(field, termStr, minSimilarity);
    }

    protected Query getPrefixQuery(String field, String termStr)
            throws ParseException {
        termStr = removeAccents(termStr);
        String paddedText = NumberPadding.pad(termStr);
        String queryText = NumberPadding.unpad(termStr);
        boolean addPadded = !paddedText.equals(queryText);
        float boost = 1;
        if (field != null) {
            Set<String> l = new HashSet<String>();
            Set<String> set = fieldsGrouping.get(siteId > 0 ? siteId + "_"
                    + field : field);
            if (set != null) {
                l.addAll(set);
            }
            if (l.isEmpty()) {
                ResourcePropertyMapping mapping = resourceMapping
                        .getResourcePropertyMapping(field);
                if (mapping != null) {
                    boost = mapping.getBoost();
                    if (boost != 1) {
                        l.add(field);
                    }
                }
            }

            boolean isNumeric = StringUtils.isNumeric(termStr);
            if (isNumeric && !l.isEmpty()) {
                Set<String> withUnpaddedFields = new HashSet<String>();
                for (String paddedFieldName : l) {
                    withUnpaddedFields.add(paddedFieldName
                            + JahiaSearchConstant.NO_PADDED_FIELD_POSTFIX);
                }
                l.addAll(withUnpaddedFields);
            }
            if (!l.isEmpty()) {
                Vector<BooleanClause> clauses = new Vector<BooleanClause>();
                for (String value : l) {
                    ResourcePropertyMapping mapping = resourceMapping
                            .getResourcePropertyMapping(value
                                    .endsWith(JahiaSearchConstant.NO_PADDED_FIELD_POSTFIX) ? value
                                    .substring(
                                            0,
                                            value
                                                    .indexOf(JahiaSearchConstant.NO_PADDED_FIELD_POSTFIX) - 1)
                                    : value);
                    if (mapping != null) {
                        boost = mapping.getBoost();
                    }

                    Query q = super.getPrefixQuery(value, queryText);

                    Query paddedQ = addPadded ? super.getPrefixQuery(value,
                            paddedText) : null;

                    q.setBoost(boost);
                    if (addPadded) {
                        paddedQ.setBoost(boost);
                    }
                    clauses
                            .add(new BooleanClause(q,
                                    BooleanClause.Occur.SHOULD));
                    if (addPadded) {
                        clauses.add(new BooleanClause(paddedQ,
                                BooleanClause.Occur.SHOULD));
                    }
                }
                return getBooleanQuery(clauses, true);
            }
        }
        if (addPadded) {
            Query q = super.getPrefixQuery(field, queryText);
            Query paddedQ = super.getPrefixQuery(field, paddedText);
            Vector<BooleanClause> clauses = new Vector<BooleanClause>();
            clauses.add(new BooleanClause(q, BooleanClause.Occur.SHOULD));
            clauses.add(new BooleanClause(paddedQ, BooleanClause.Occur.SHOULD));
            Query unpaddedFieldQ = super.getPrefixQuery(field, queryText);
            clauses.add(new BooleanClause(unpaddedFieldQ,
                    BooleanClause.Occur.SHOULD));
            return getBooleanQuery(clauses, true);
        }
        return super.getPrefixQuery(field, termStr);
    }
    
    private String removeAccents (String text) {
        char[] term = text.toCharArray();
        
        for(int i=0, length = text.length();i<length;i++) {
            final char c = term[i];
            if (c >= '\u00c0' && c <= '\u0178') {
                (new ISOLatin1AccentFilter(null)).removeAccents(term, text.length());
              text = new String(term);
              break;
            }
          }
        return text;
    }

    protected Query getWildcardQuery(String field, String termStr)
            throws ParseException {
        float boost = 1;
        String paddedText = NumberPadding.pad(termStr);
        String queryText = NumberPadding.unpad(termStr);
        boolean addPadded = !paddedText.equals(queryText);
        if (field != null) {
            Set<String> l = fieldsGrouping.get(siteId > 0 ? siteId + "_"
                    + field : field);
            if (l == null || l.isEmpty()) {
                ResourcePropertyMapping mapping = resourceMapping
                        .getResourcePropertyMapping(field);
                if (mapping != null) {
                    boost = mapping.getBoost();
                    if (boost != 1) {
                        l = new HashSet<String>();
                        l.add(field);
                    }
                }
            }
            if (l != null && !l.isEmpty()) {
                Vector<BooleanClause> clauses = new Vector<BooleanClause>();
                for (String value : l) {
                    ResourcePropertyMapping mapping = resourceMapping
                            .getResourcePropertyMapping(value);
                    if (mapping != null) {
                        boost = mapping.getBoost();
                    }
                    Query q = super.getWildcardQuery(value, queryText);
                    Query paddedQ = addPadded ? super.getWildcardQuery(value,
                            paddedText) : null;
                    q.setBoost(boost);
                    if (addPadded) {
                        paddedQ.setBoost(boost);
                    }
                    clauses
                            .add(new BooleanClause(q,
                                    BooleanClause.Occur.SHOULD));
                    if (addPadded) {
                        clauses.add(new BooleanClause(paddedQ,
                                BooleanClause.Occur.SHOULD));
                    }

                }
                return getBooleanQuery(clauses, true);
            }
        }
        if (addPadded) {
            Query q = super.getWildcardQuery(field, queryText);
            Query paddedQ = super.getWildcardQuery(field, paddedText);
            Vector<BooleanClause> clauses = new Vector<BooleanClause>();
            clauses.add(new BooleanClause(q, BooleanClause.Occur.SHOULD));
            clauses.add(new BooleanClause(paddedQ, BooleanClause.Occur.SHOULD));
            return getBooleanQuery(clauses, true);
        }
        return super.getWildcardQuery(field, termStr);
    }

    protected Query getRangeQuery(String field, String part1, String part2,
            boolean inclusive) throws ParseException {
        part1 = NumberPadding.pad(part1);
        part2 = NumberPadding.pad(part2);
        float boost = 1;
        if (field != null) {
            Set<String> l = fieldsGrouping.get(siteId > 0 ? siteId + "_"
                    + field : field);
            if (l == null || l.isEmpty()) {
                ResourcePropertyMapping mapping = resourceMapping
                        .getResourcePropertyMapping(field);
                if (mapping != null) {
                    boost = mapping.getBoost();

                    if (boost != 1) {
                        l = new HashSet<String>();
                        l.add(field);
                    }
                }
            }
            if (l != null && !l.isEmpty()) {
                Vector<BooleanClause> clauses = new Vector<BooleanClause>();
                for (String value : l) {
                    ResourcePropertyMapping mapping = resourceMapping
                            .getResourcePropertyMapping(value);
                    if (mapping != null) {
                        boost = mapping.getBoost();
                    }

                    Query q = super.getRangeQuery(field, part1, part2,
                            inclusive);
                    q.setBoost(boost);

                    clauses.add(new BooleanClause(super.getRangeQuery(value,
                            part1, part2, inclusive),
                            BooleanClause.Occur.SHOULD));
                }
                return getBooleanQuery(clauses, true);
            }
        }
        return super.getRangeQuery(field, part1, part2, inclusive);
    }

    /**
     * Parses a query, searching on the fields specified. Use this if you need to specify certain fields as required, and others as
     * prohibited.
     * <p>
     * 
     * <pre>
     * Usage:
     * &lt;code&gt;
     * String[] fields = {&quot;filename&quot;, &quot;contents&quot;, &quot;description&quot;};
     * BooleanClause.Occur[] flags = {BooleanClause.Occur.SHOULD,
     *                BooleanClause.Occur.MUST,
     *                BooleanClause.Occur.MUST_NOT};
     * MultiFieldQueryParser.parse(&quot;query&quot;, fields, flags, analyzer);
     * &lt;/code&gt;
     * </pre>
     *<p>
     * The code above would construct a query:
     * 
     * <pre>
     * &lt;code&gt;
     * (filename:query) +(contents:query) -(description:query)
     * &lt;/code&gt;
     * </pre>
     * 
     * @param query
     *            Query string to parse
     * @param fields
     *            Fields to search on
     * @param flags
     *            Flags describing the fields
     * @param analyzer
     *            Analyzer to use
     * @throws org.apache.lucene.queryParser.ParseException
     *             if query parsing fails
     * @throws org.apache.lucene.queryParser.TokenMgrError
     *             if query parsing fails
     * @throws IllegalArgumentException
     *             if the length of the fields array differs from the length of the flags array
     */
    public static Query parse(String query, String[] fields,
            BooleanClause.Occur[] flags, Analyzer analyzer)
            throws ParseException {
        if (fields.length != flags.length)
            throw new IllegalArgumentException("fields.length != flags.length");
        BooleanQuery bQuery = new BooleanQuery();
        for (int i = 0; i < fields.length; i++) {
            QueryParser qp = new QueryParser(fields[i], analyzer);
            Query q = qp.parse(query);
            bQuery.add(q, flags[i]);
        }
        return bQuery;
    }

    /**
     * Parses a query, searching on the fields specified. Use this if you need to specify certain fields as required, and others as
     * prohibited.
     * <p>
     * 
     * <pre>
     * Usage:
     * &lt;code&gt;
     * String[] query = {&quot;query1&quot;, &quot;query2&quot;, &quot;query3&quot;};
     * String[] fields = {&quot;filename&quot;, &quot;contents&quot;, &quot;description&quot;};
     * BooleanClause.Occur[] flags = {BooleanClause.Occur.SHOULD,
     *                BooleanClause.Occur.MUST,
     *                BooleanClause.Occur.MUST_NOT};
     * MultiFieldQueryParser.parse(query, fields, flags, analyzer);
     * &lt;/code&gt;
     * </pre>
     *<p>
     * The code above would construct a query:
     * 
     * <pre>
     * &lt;code&gt;
     * (filename:query1) +(contents:query2) -(description:query3)
     * &lt;/code&gt;
     * </pre>
     * 
     * @param queries
     *            Queries string to parse
     * @param fields
     *            Fields to search on
     * @param flags
     *            Flags describing the fields
     * @param analyzer
     *            Analyzer to use
     * @throws org.apache.lucene.queryParser.ParseException
     *             if query parsing fails
     * @throws org.apache.lucene.queryParser.TokenMgrError
     *             if query parsing fails
     * @throws IllegalArgumentException
     *             if the length of the queries, fields, and flags array differ
     */
    public static Query parse(String[] queries, String[] fields,
            BooleanClause.Occur[] flags, Analyzer analyzer)
            throws ParseException {
        if (!(queries.length == fields.length && queries.length == flags.length))
            throw new IllegalArgumentException(
                    "queries, fields, and flags array have have different length");
        BooleanQuery bQuery = new BooleanQuery();
        for (int i = 0; i < fields.length; i++) {
            QueryParser qp = new QueryParser(fields[i], analyzer);
            Query q = qp.parse(queries[i]);
            bQuery.add(q, flags[i]);
        }
        return bQuery;
    }

}
