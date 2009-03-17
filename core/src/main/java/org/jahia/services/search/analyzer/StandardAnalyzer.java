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

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Set;


import org.apache.lucene.analysis.ISOLatin1AccentFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WordlistLoader;
import org.apache.lucene.analysis.standard.StandardFilter;

/**
 * Filters {@link StandardTokenizer} with {@link StandardFilter}, {@link
 * LowerCaseFilter} and {@link StopFilter}.
 *
 * @version $Id$
 */
public class StandardAnalyzer extends org.apache.lucene.analysis.standard.StandardAnalyzer {

    
    /** Builds an analyzer with the default stop words ({@link #STOP_WORDS}). */
    public StandardAnalyzer() {
      super();
    }

    /** Builds an analyzer with the given stop words. */
    public StandardAnalyzer(Set<?> stopWords) {
        super(stopWords);
    }

    /** Builds an analyzer with the given stop words. */
    public StandardAnalyzer(String[] stopWords) {
        super(stopWords);
    }

    /** Builds an analyzer with the stop words from the given file.
     * @see WordlistLoader#getWordSet(File)
     */
    public StandardAnalyzer(File stopWords) throws IOException {
        super(stopWords);
    }

    /** Builds an analyzer with the stop words from the given reader.
     * @see WordlistLoader#getWordSet(Reader)
     */
    public StandardAnalyzer(Reader stopWords) throws IOException {
        super(stopWords);
    }

    /**
     * Constructs a {@link StandardTokenizer} filtered by a {@link
     * StandardFilter}, a {@link LowerCaseFilter} and a {@link StopFilter}.
     */
    public TokenStream tokenStream(String fieldName, Reader reader) {
        TokenStream result = super.tokenStream(fieldName, reader);
        result = new TokenWithCommaFilter(result);
        result = new ISOLatin1AccentFilter(result);
        return result;
    }    
}
