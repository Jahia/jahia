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
