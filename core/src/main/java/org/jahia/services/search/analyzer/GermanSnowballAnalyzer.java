/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.de.GermanLightStemFilter;
import org.apache.lucene.analysis.de.GermanNormalizationFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;
import org.tartarus.snowball.ext.German2Stemmer;

/**
 * Filters {@link StandardTokenizer} with {@link StandardFilter}, {@link
 * LowerCaseFilter}, {@link StopFilter}, {@link SnowballFilter} for German and {@link org.apache.lucene.analysis.ASCIIFoldingFilter}.
 */
public class GermanSnowballAnalyzer extends StopwordAnalyzerBase {

    /**
     * Returns a set of default German-stopwords
     * @return a set of default German-stopwords
     */
    public static final Set<?> getDefaultStopSet(){
      return GermanAnalyzer.getDefaultStopSet();
    }

    /**
     * Contains the stopwords used with the {@link StopFilter}.
     */

    /**
     * Contains words that should be indexed but not stemmed.
     */
    private final Set<?> exclusionSet;

    /**
     * Builds an analyzer with the default stop words:
     * {@link #getDefaultStopSet()}.
     */
    public GermanSnowballAnalyzer(Version matchVersion) {
      this(matchVersion, GermanAnalyzer.getDefaultStopSet());
    }

    /**
     * Builds an analyzer with the given stop words
     *
     * @param matchVersion
     *          lucene compatibility version
     * @param stopwords
     *          a stopword set
     */
    public GermanSnowballAnalyzer(Version matchVersion, Set<?> stopwords) {
      this(matchVersion, stopwords, CharArraySet.EMPTY_SET);
    }

    /**
     * Builds an analyzer with the given stop words
     *
     * @param matchVersion
     *          lucene compatibility version
     * @param stopwords
     *          a stopword set
     * @param stemExclusionSet
     *          a stemming exclusion set
     */
    public GermanSnowballAnalyzer(Version matchVersion, Set<?> stopwords, Set<?> stemExclusionSet) {
      super(matchVersion, stopwords);
      exclusionSet = CharArraySet.unmodifiableSet(CharArraySet.copy(matchVersion, stemExclusionSet));
    }

    /**
     * Creates
     * {@link org.apache.lucene.analysis.ReusableAnalyzerBase.TokenStreamComponents}
     * used to tokenize all the text in the provided {@link Reader}.
     *
     * @return {@link org.apache.lucene.analysis.ReusableAnalyzerBase.TokenStreamComponents}
     *         built from a {@link StandardTokenizer} filtered with
     *         {@link StandardFilter}, {@link LowerCaseFilter}, {@link StopFilter}
     *         , {@link KeywordMarkerFilter} if a stem exclusion set is
     *         provided, {@link GermanNormalizationFilter} and {@link GermanLightStemFilter}
     */
    @Override
    protected TokenStreamComponents createComponents(String fieldName,
        Reader reader) {
      final Tokenizer source = new StandardTokenizer(matchVersion, reader);
      TokenStream result = new StandardFilter(matchVersion, source);
      result = new LowerCaseFilter(matchVersion, result);
      result = new StopFilter( matchVersion, result, stopwords);
      result = new KeywordMarkerFilter(result, exclusionSet);
      result = new SnowballFilter(result, new German2Stemmer());
      result = new ASCIIFoldingFilter(result);
      return new TokenStreamComponents(source, result);
    }
}
