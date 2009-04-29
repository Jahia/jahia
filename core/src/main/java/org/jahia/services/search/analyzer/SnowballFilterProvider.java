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

import org.compass.core.lucene.engine.analyzer.LuceneAnalyzerTokenFilterProvider;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.CompassException;
import org.compass.core.engine.SearchEngineException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.snowball.SnowballFilter;

public class SnowballFilterProvider implements LuceneAnalyzerTokenFilterProvider {

    private String snowballName;

    public void configure(CompassSettings settings) throws CompassException {
        snowballName = settings.getSetting(LuceneEnvironment.Analyzer.Snowball.NAME_TYPE);
        if (snowballName == null) {
            throw new SearchEngineException("When using a snowball analyzer, must set the + ["
                    + LuceneEnvironment.Analyzer.Snowball.NAME_TYPE + "] setting for it");
        }
    }

    public TokenFilter createTokenFilter(TokenStream tokenStream) {
        return new SnowballFilter(tokenStream, snowballName);
    }
}
