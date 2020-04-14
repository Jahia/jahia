/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
