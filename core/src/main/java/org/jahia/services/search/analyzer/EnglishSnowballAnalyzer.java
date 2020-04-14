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

import java.io.Reader;
import java.util.Set;

import org.apache.lucene.analysis.ASCIIFoldingFilter;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.KeywordMarkerFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

/**
 * Filters {@link StandardTokenizer} with {@link StandardFilter}, {@link
 * LowerCaseFilter}, {@link StopFilter}, {@link SnowballFilter} for English and {@link org.apache.lucene.analysis.ASCIIFoldingFilter}.
 */
public class EnglishSnowballAnalyzer extends StopwordAnalyzerBase {
    private final Set<?> stemExclusionSet;

    /**
     * Returns an unmodifiable instance of the default stop words set.
     * 
     * @return default stop words set.
     */
    public static Set<?> getDefaultStopSet() {
        return DefaultSetHolder.DEFAULT_STOP_SET;
    }

    /**
     * Atomically loads the DEFAULT_STOP_SET in a lazy fashion once the outer class accesses the static final set the first time.;
     */
    private static class DefaultSetHolder {
        static final Set<?> DEFAULT_STOP_SET = org.apache.lucene.analysis.standard.StandardAnalyzer.STOP_WORDS_SET;
    }

    /**
     * Builds an analyzer with the default stop words: {@link #getDefaultStopSet}.
     */
    public EnglishSnowballAnalyzer(Version matchVersion) {
        this(matchVersion, DefaultSetHolder.DEFAULT_STOP_SET);
    }

    /**
     * Builds an analyzer with the given stop words.
     * 
     * @param matchVersion
     *            lucene compatibility version
     * @param stopwords
     *            a stopword set
     */
    public EnglishSnowballAnalyzer(Version matchVersion, Set<?> stopwords) {
        this(matchVersion, stopwords, CharArraySet.EMPTY_SET);
    }

    /**
     * Builds an analyzer with the given stop words. If a non-empty stem exclusion set is provided this analyzer will add a
     * {@link KeywordMarkerFilter} before stemming.
     * 
     * @param matchVersion
     *            lucene compatibility version
     * @param stopwords
     *            a stopword set
     * @param stemExclusionSet
     *            a set of terms not to be stemmed
     */
    public EnglishSnowballAnalyzer(Version matchVersion, Set<?> stopwords, Set<?> stemExclusionSet) {
        super(matchVersion, stopwords);
        this.stemExclusionSet = CharArraySet.unmodifiableSet(CharArraySet.copy(matchVersion, stemExclusionSet));
    }

    /**
     * Creates a {@link org.apache.lucene.analysis.ReusableAnalyzerBase.TokenStreamComponents} which tokenizes all the text in the provided
     * {@link Reader}.
     * 
     * @return A {@link org.apache.lucene.analysis.ReusableAnalyzerBase.TokenStreamComponents} built from an {@link StandardTokenizer}
     *         filtered with {@link StandardFilter}, {@link LowerCaseFilter}, {@link StopFilter} , {@link KeywordMarkerFilter} if a stem
     *         exclusion set is provided, {@link SnowballFilter} and {@link ASCIIFoldingFilter}.
     */
    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        final Tokenizer source = new StandardTokenizer(matchVersion, reader);
        TokenStream result = new StandardFilter(matchVersion, source);
        result = new EnglishPossessiveFilter(matchVersion, result);
        result = new LowerCaseFilter(matchVersion, result);
        result = new StopFilter(matchVersion, result, stopwords);
        if (!stemExclusionSet.isEmpty()) {
            result = new KeywordMarkerFilter(result, stemExclusionSet);
        }
        result = new SnowballFilter(result, "English");
        result = new ASCIIFoldingFilter(result);
        return new TokenStreamComponents(source, result);
    }
}
