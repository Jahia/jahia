package org.jahia.services.search.analyzer;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.compass.core.CompassException;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.analyzer.LuceneAnalyzerTokenFilterProvider;

public class JahiaLuceneAnalyzerTokenFilterProvider implements
           LuceneAnalyzerTokenFilterProvider {
    private String filterName;

    public void configure(CompassSettings settings) throws CompassException {
        filterName = settings.getSetting(LuceneEnvironment.Analyzer.Snowball.NAME_TYPE);
        if (filterName == null) {
            throw new SearchEngineException("When using a Jahia analyzer, must set the + ["
                    + LuceneEnvironment.Analyzer.Snowball.NAME_TYPE + "] setting for it");
        }
    }

    public TokenFilter createTokenFilter(TokenStream tokenStream) {
        if ("comma".equals(filterName)) {
            return new TokenWithCommaFilter(tokenStream);
        } else if ("dot".equals(filterName)) {
            return new TokenWithDotFilter(tokenStream);
        } else if ("quote".equals(filterName)) {
            return new TokenWithQuoteFilter(tokenStream);
        } else if ("lowercase".equals(filterName)) {
            return new LowerCaseFilter(tokenStream);
        } else {
            return null;
        }
    }    

}
