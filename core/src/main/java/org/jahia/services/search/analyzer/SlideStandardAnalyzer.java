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

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

import java.io.Reader;

/**
 * Filters {@link org.jahia.services.search.analyzer.StandardTokenizer} with {@link org.jahia.services.search.analyzer.StandardFilter}, {@link
 * LowerCaseFilter} and {@link org.apache.lucene.analysis.StopFilter}.
 *
 * @version $Id: StandardAnalyzer.java 12541 2005-12-22 14:21:45Z knguyen $
 */
public class SlideStandardAnalyzer extends StandardAnalyzer {

    /**
     * Builds an analyzer.
     */
    public SlideStandardAnalyzer() {
        super();
    }

    /**
     * Constructs a {@link StandardTokenizer} filtered by a {@link
     * StandardFilter}, a {@link org.apache.lucene.analysis.LowerCaseFilter} and a {@link org.apache.lucene.analysis.StopFilter}.
     */
    public TokenStream tokenStream(String fieldName, Reader reader) {
        TokenStream result = new StandardTokenizer(reader);
        result = new StandardFilter(result);
        result = new LowerCaseFilter(result);
        result = new LanguageIndependantFilter(result);
        result = new TokenWithQuoteFilter(result);
        return result;
    }

}
