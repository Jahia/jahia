package org.apache.jackrabbit.core.query.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

/**
 * <code>JahiaDualAnalyzerQueryParser</code> extends the standard Jackrabbit query parser
 * and adds Jahia-specific customizations. It supports dual analyzers for
 * internationalization (i18n) purposes.
 *
 * <p>This parser allows for the use of both a standard analyzer and an optional
 * i18n analyzer. By default, the behavior is the same as if only one analyzer
 * was used. However, if full-text queries need to be compatible with both
 * non-i18n properties and i18n properties, a secondary i18nAnalyzer can be provided.
 * The result will be a dual-analyzed query, effectively combining the results
 * of text search analyzed by the i18n analyzer and the standard analyzer.</p>
 *
 * <p>Here is an example of how the dual analyzer system works:</p>
 * <p>A query searching for a xpath query:
 * <pre>//element(*,jmix:searchable)[(jcr:contains(.,'bulletin officiel')) and (@jcr:language='fr' or not(@jcr:language))]</pre>
 * <p>JCR-SQL2 equivalent:
 * <pre>select * from [jmix:searchable] as n where (contains(n.*,'bulletin officiel')) and (n.[jcr:language]='fr' or [jcr:language] is null)</pre>
 *
 * <p>Previously, the constraint about the fulltextsearch was transformed using the 'fr' analyzer only:</p>
 * <pre>+(+0:FULL:n:buletin +0:FULL:n:oficiel)</pre>
 *
 * <p>This resulted in non-i18n properties containing the queried tokens not being retrieved.
 * For example, a node with a non-i18n text property containing the text "bulletin officiel" was not retrieved.</p>
 *
 * <p>With this Jahia Dual Analyzer Query Parser the fulltextsearch constraint now transform to Lucene query:</p>
 * <pre>+(((+_:FULLTEXT:buletin +_:FULLTEXT:oficiel) (+_:FULLTEXT:bulletin +_:FULLTEXT:officiel))~1)</pre>
 *
 * <p>This allows retrieving both nodes:</p>
 * <ul>
 *   <li>with i18n properties indexed in 'fr' analyzed tokens</li>
 *   <li>with non-i18n properties indexed using brute query tokens</li>
 * </ul>
 */
public class JahiaDualAnalyzerQueryParser extends JackrabbitQueryParser {

    private JackrabbitQueryParser delegate;

    /**
     * Constructs a new <code>JahiaDualAnalyzerQueryParser</code> instance.
     *
     * @param fieldName the default field for query terms
     * @param i18nAnalyzer the optional internationalization analyzer to use for parsing, can be null
     * @param analyzer the default analyzer to use for parsing
     * @param synonymProvider the synonym provider
     * @param cache the per-query cache
     */
    protected JahiaDualAnalyzerQueryParser(String fieldName, Analyzer i18nAnalyzer, Analyzer analyzer, SynonymProvider synonymProvider, PerQueryCache cache) {
        super(fieldName, i18nAnalyzer != null ? i18nAnalyzer : analyzer, synonymProvider, cache);
        if (i18nAnalyzer != null) {
            this.delegate = new JackrabbitQueryParser(fieldName, analyzer, synonymProvider, cache);
        }
    }

    /**
     * Parses the given text search query.
     *
     * @param textsearch the text search query to parse
     * @return the parsed query
     * @throws ParseException if parsing fails
     */
    @Override
    public Query parse(String textsearch) throws ParseException {
        if (delegate != null) {
            BooleanQuery dualAnalyzerCombinedTextsearchQuery = new BooleanQuery();
            dualAnalyzerCombinedTextsearchQuery.setMinimumNumberShouldMatch(1);
            dualAnalyzerCombinedTextsearchQuery.add(super.parse(textsearch), BooleanClause.Occur.SHOULD);
            dualAnalyzerCombinedTextsearchQuery.add(delegate.parse(textsearch), BooleanClause.Occur.SHOULD);
            return dualAnalyzerCombinedTextsearchQuery;
        }
        return super.parse(textsearch);
    }
}