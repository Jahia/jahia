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
