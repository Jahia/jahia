/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
package org.jahia.services.search.analyzer;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.fr.ElisionFilter;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.fr.FrenchLightStemFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

import java.io.Reader;
import java.util.Set;

/**
 * Filters {@link StandardTokenizer} with {@link StandardFilter}, {@link LowerCaseFilter}, {@link StopFilter}, {@link SnowballFilter} for
 * French and {@link org.apache.lucene.analysis.ASCIIFoldingFilter}.
 */
public class FrenchSnowballAnalyzer extends StopwordAnalyzerBase {

    /**
     * Contains words that should be indexed but not stemmed.
     */
    private Set<?> excltable = CharArraySet.EMPTY_SET;

    /**
     * Builds an analyzer with the given stop words
     *
     * @param stopwords
     *            a stopword set
     */
    public FrenchSnowballAnalyzer(Version matchVersion) {
        this(matchVersion, FrenchAnalyzer.getDefaultStopSet());
    }

    /**
     * Builds an analyzer with the given stop words
     * 
     * @param matchVersion
     *            lucene compatibility version
     * @param stopwords
     *            a stopword set
     */
    public FrenchSnowballAnalyzer(Version matchVersion, Set<?> stopwords) {
        this(matchVersion, stopwords, CharArraySet.EMPTY_SET);
    }

    /**
     * Builds an analyzer with the given stop words
     * 
     * @param matchVersion
     *            lucene compatibility version
     * @param stopwords
     *            a stopword set
     * @param stemExclutionSet
     *            a stemming exclusion set
     */
    public FrenchSnowballAnalyzer(Version matchVersion, Set<?> stopwords, Set<?> stemExclutionSet) {
        super(matchVersion, stopwords);
        this.excltable = CharArraySet.unmodifiableSet(CharArraySet.copy(matchVersion, stemExclutionSet));
    }

    /**
     * Creates {@link org.apache.lucene.analysis.ReusableAnalyzerBase.TokenStreamComponents} used to tokenize all the text in the provided
     * {@link Reader}.
     * 
     * @return {@link org.apache.lucene.analysis.ReusableAnalyzerBase.TokenStreamComponents} built from a {@link StandardTokenizer} filtered
     *         with {@link StandardFilter}, {@link ElisionFilter}, {@link LowerCaseFilter}, {@link StopFilter}, {@link KeywordMarkerFilter}
     *         if a stem exclusion set is provided, and {@link FrenchLightStemFilter}
     */
    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        final Tokenizer source = new StandardTokenizer(matchVersion, reader);
        TokenStream result = new StandardFilter(matchVersion, source);
        result = new ElisionFilter(matchVersion, result);
        result = new LowerCaseFilter(matchVersion, result);
        result = new StopFilter(matchVersion, result, stopwords);
        if (!excltable.isEmpty()) {
            result = new KeywordMarkerFilter(result, excltable);
        }
        result = new SnowballFilter(result, new org.tartarus.snowball.ext.FrenchStemmer());
        result = new ASCIIFoldingFilter(result);
        return new TokenStreamComponents(source, result);
    }
}
