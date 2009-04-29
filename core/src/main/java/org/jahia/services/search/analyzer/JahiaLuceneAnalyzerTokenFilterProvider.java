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
