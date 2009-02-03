/*
 * Copyright 2002-2008 Jahia Ltd
 *
 * Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL), 
 * Version 1.0 (the "License"), or (at your option) any later version; you may 
 * not use this file except in compliance with the License. You should have 
 * received a copy of the License along with this program; if not, you may obtain 
 * a copy of the License at 
 *
 *  http://www.jahia.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

 package org.jahia.services.search.analyzer;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Lucene" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Lucene", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassSettings;
import org.compass.core.lucene.LuceneEnvironment;

/**
 * Filters {@link StandardTokenizer} with {@link StandardFilter}, {@link
 * LowerCaseFilter} and {@link StopFilter}.
 *
 * @version $Id$
 */
public class StandardAnalyzer extends Analyzer implements CompassConfigurable {

    /**
     * An array containing some common English words that are usually not
     * useful for searching.
     */
    private Set stopWords = new HashSet();

    /**
     * Specifies whether deprecated acronyms should be replaced with HOST type.
     * This is false by default to support backward compatibility.
     * 
     * @deprecated this should be removed in the next release (3.0).
     *
     * See https://issues.apache.org/jira/browse/LUCENE-1068
     */
    private boolean replaceInvalidAcronym = defaultReplaceInvalidAcronym;

    private static boolean defaultReplaceInvalidAcronym;
    
    // Default to false (fixed the bug), unless the system prop is set
    static {
      final String v = System.getProperty("org.apache.lucene.analysis.standard.StandardAnalyzer.replaceInvalidAcronym");
      if (v == null || v.equals("true"))
        defaultReplaceInvalidAcronym = true;
      else
        defaultReplaceInvalidAcronym = false;
    }
    /**
    *
    * @return true if new instances of StandardTokenizer will
    * replace mischaracterized acronyms
    *
    * See https://issues.apache.org/jira/browse/LUCENE-1068
    * @deprecated This will be removed (hardwired to true) in 3.0
    */
   public static boolean getDefaultReplaceInvalidAcronym() {
     return defaultReplaceInvalidAcronym;
   }

   /**
    *
    * @param replaceInvalidAcronym Set to true to have new
    * instances of StandardTokenizer replace mischaracterized
    * acronyms by default.  Set to false to preseve the
    * previous (before 2.4) buggy behavior.  Alternatively,
    * set the system property
    * org.apache.lucene.analysis.standard.StandardAnalyzer.replaceInvalidAcronym
    * to false.
    *
    * See https://issues.apache.org/jira/browse/LUCENE-1068
    * @deprecated This will be removed (hardwired to true) in 3.0
    */
   public static void setDefaultReplaceInvalidAcronym(boolean replaceInvalidAcronym) {
     defaultReplaceInvalidAcronym = replaceInvalidAcronym;
   }    
    
    /**
     * Builds an analyzer.
     */
    public StandardAnalyzer() {
    }

    /**
     * Builds an analyzer with the given stop words.
     */
    public StandardAnalyzer(String[] stopWords) {
        this.stopWords = StopFilter.makeStopSet(stopWords);
    }

    /**
     * Constructs a {@link StandardTokenizer} filtered by a {@link
     * StandardFilter}, a {@link LowerCaseFilter} and a {@link StopFilter}.
     */
    public TokenStream tokenStream(String fieldName, Reader reader) {
        StandardTokenizer tokenStream = new StandardTokenizer(reader, replaceInvalidAcronym);
        tokenStream.setMaxTokenLength(maxTokenLength);
        TokenStream result = new StandardFilter(tokenStream);
        result = new LowerCaseFilter(result);
        result = new StopFilter(result, stopWords);
        result = new TokenWithCommaFilter(result);
        result = new LanguageIndependantFilter(result);
        return result;
    }
    
    private static final class SavedStreams {
        StandardTokenizer tokenStream;
        TokenStream filteredTokenStream;
      }    

    /** Default maximum allowed token length */
    public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;

    private int maxTokenLength = DEFAULT_MAX_TOKEN_LENGTH;

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
    
    public TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException {
        SavedStreams streams = (SavedStreams) getPreviousTokenStream();
        if (streams == null) {
          streams = new SavedStreams();
          setPreviousTokenStream(streams);
          streams.tokenStream = new StandardTokenizer(reader);
          streams.filteredTokenStream = new StandardFilter(streams.tokenStream);
          streams.filteredTokenStream = new LowerCaseFilter(streams.filteredTokenStream);
          streams.filteredTokenStream = new StopFilter(streams.filteredTokenStream, stopWords);
        } else {
          streams.tokenStream.reset(reader);
        }
        streams.tokenStream.setMaxTokenLength(maxTokenLength);
        
        streams.tokenStream.setReplaceInvalidAcronym(replaceInvalidAcronym);

        return streams.filteredTokenStream;
      }

      /**
       *
       * @return true if this Analyzer is replacing mischaracterized acronyms in the StandardTokenizer
       *
       * See https://issues.apache.org/jira/browse/LUCENE-1068
       * @deprecated This will be removed (hardwired to true) in 3.0
       */
      public boolean isReplaceInvalidAcronym() {
        return replaceInvalidAcronym;
      }

      /**
       *
       * @param replaceInvalidAcronym Set to true if this Analyzer is replacing mischaracterized acronyms in the StandardTokenizer
       *
       * See https://issues.apache.org/jira/browse/LUCENE-1068
       * @deprecated This will be removed (hardwired to true) in 3.0
       */
      public void setReplaceInvalidAcronym(boolean replaceInvalidAcronym) {
          this.replaceInvalidAcronym = replaceInvalidAcronym;
      }
    
    public void configure(CompassSettings settings)
    throws CompassException {
        String[] stopWords = parseStopWords(settings,new String[]{});
        this.stopWords.addAll(Arrays.asList(stopWords));
    }

    public String[] parseStopWords(CompassSettings settings, String[] defaultStopWords) {
        String stopWords = settings.getSetting(LuceneEnvironment.Analyzer.STOPWORDS);
        if (stopWords == null) {
            return defaultStopWords;
        }
        boolean addStopWords = false;
        if (stopWords.startsWith("+")) {
            addStopWords = true;
            stopWords = stopWords.substring(1);
        }
        StringTokenizer st = new StringTokenizer(stopWords, ",");
        String[] arrStopWords = new String[st.countTokens()];
        for (int i = 0; st.hasMoreTokens(); i++) {
            arrStopWords[i] = st.nextToken().trim();
        }
        if (addStopWords) {
            String[] tempStopWords = arrStopWords;
            arrStopWords = new String[tempStopWords.length + defaultStopWords.length];
            System.arraycopy(defaultStopWords, 0, arrStopWords, 0, defaultStopWords.length);
            System.arraycopy(tempStopWords, 0, arrStopWords, defaultStopWords.length, tempStopWords.length);
        }
        return arrStopWords;
    }

    private ThreadLocal tokenStreams = new ThreadLocal();

    /** Used by Analyzers that implement reusableTokenStream
     *  to retrieve previously saved TokenStreams for re-use
     *  by the same thread. */
    protected Object getPreviousTokenStream() {
      return tokenStreams.get();
    }

    /** Used by Analyzers that implement reusableTokenStream
     *  to save a TokenStream for later re-use by the same
     *  thread. */
    protected void setPreviousTokenStream(Object obj) {
      tokenStreams.set(obj);
    }    
    
}
