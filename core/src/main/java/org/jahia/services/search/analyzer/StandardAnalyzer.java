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

import java.io.IOException;
import java.io.Reader;
import java.util.Set;

import org.apache.lucene.analysis.ASCIIFoldingFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WordlistLoader;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

/**
 * Filters {@link StandardTokenizer} with {@link StandardFilter}, {@link LowerCaseFilter}, {@link StopFilter} and {@link ASCIIFoldingFilter}.
 * 
 */
public class StandardAnalyzer extends StopwordAnalyzerBase {
    /** Default maximum allowed token length */
    public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;

    private int maxTokenLength = DEFAULT_MAX_TOKEN_LENGTH;

    /** An unmodifiable set containing some common English words that are usually not
    useful for searching. */
    public static final Set<?> STOP_WORDS_SET = StopAnalyzer.ENGLISH_STOP_WORDS_SET; 

    /** Builds an analyzer with the given stop words.
     * @param matchVersion Lucene version to match See {@link
     * <a href="#version">above</a>}
     * @param stopWords stop words */
    public StandardAnalyzer(Version matchVersion, Set<?> stopWords) {
      super(matchVersion, stopWords);
    }

    /** Builds an analyzer with the default stop words ({@link
     * #STOP_WORDS_SET}).
     * @param matchVersion Lucene version to match See {@link
     * <a href="#version">above</a>}
     */
    public StandardAnalyzer(Version matchVersion) {
      this(matchVersion, STOP_WORDS_SET);
    }
    
    /** Builds an analyzer with the stop words from the given reader.
     * @see WordlistLoader#getWordSet(Reader, Version)
     * @param matchVersion Lucene version to match See {@link
     * <a href="#version">above</a>}
     * @param stopwords Reader to read stop words from */
    public StandardAnalyzer(Version matchVersion, Reader stopwords) throws IOException {
      this(matchVersion, WordlistLoader.getWordSet(stopwords, matchVersion));
    }

    /**
     * Set maximum allowed token length.  If a token is seen
     * that exceeds this length then it is discarded.  This
     * setting only takes effect the next time tokenStream or
     * reusableTokenStream is called.
     */
    public void setMaxTokenLength(int length) {
      maxTokenLength = length;
    }
      
    /**
     * @see #setMaxTokenLength
     */
    public int getMaxTokenLength() {
      return maxTokenLength;
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
      final StandardTokenizer src = new StandardTokenizer(matchVersion, reader);
      src.setMaxTokenLength(maxTokenLength);
      TokenStream tok = new StandardFilter(matchVersion, src);
      tok = new LowerCaseFilter(matchVersion, tok);
      tok = new StopFilter(matchVersion, tok, stopwords);
      tok = new ASCIIFoldingFilter(tok);      
      return new TokenStreamComponents(src, tok) {
        @Override
        protected boolean reset(final Reader reader) throws IOException {
          src.setMaxTokenLength(StandardAnalyzer.this.maxTokenLength);
          return super.reset(reader);
        }
      };
    }
}
