package org.jahia.services.search.analyzer;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.compass.core.Compass;
import org.compass.core.engine.SearchEngineFactory;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.spi.InternalCompass;
import org.jahia.registries.ServicesRegistry;

/**
 * Filters {@link org.jahia.services.search.analyzer.StandardTokenizer} with {@link org.jahia.services.search.analyzer.StandardFilter}, {@link
 * org.apache.lucene.analysis.LowerCaseFilter} and {@link org.apache.lucene.analysis.StopFilter}.
 *
 * @version $Id: StandardAnalyzer.java 16283 2007-01-29 14:44:59Z knguyen $
 */
public class HighlightingAnalyzer extends StandardAnalyzer {

    private Analyzer analyzer = null;
    private boolean initialized = false;

    /**
     * Builds an analyzer.
     */
    public HighlightingAnalyzer() {
        super();
    }

    public TokenStream tokenStream(String fieldName, Reader reader) {
        if (!initialized && analyzer == null) {
            Compass compass = ServicesRegistry.getInstance()
                    .getJahiaSearchService().getCompass();
            if (compass != null && compass instanceof InternalCompass) {
                InternalCompass internalCompass = (InternalCompass) compass;
                SearchEngineFactory searchEngineFactory = internalCompass
                        .getSearchEngineFactory();
                if (searchEngineFactory != null
                        && searchEngineFactory instanceof LuceneSearchEngineFactory) {
                    LuceneSearchEngineFactory luceneSearchEngineFactory = (LuceneSearchEngineFactory) searchEngineFactory;
                    analyzer = luceneSearchEngineFactory.getAnalyzerManager()
                            .getAnalyzerByAlias("jahiaIndexer");
                }
            }
            initialized = true;
        }
        if ( analyzer != null ){
            return analyzer.tokenStream(fieldName, reader);
        }
        return super.tokenStream(fieldName,reader);
    }
}
