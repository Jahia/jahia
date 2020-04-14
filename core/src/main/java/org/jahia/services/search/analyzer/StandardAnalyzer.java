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
