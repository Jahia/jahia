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

 import java.util.StringTokenizer;

 import org.apache.lucene.analysis.Token;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.standard.StandardTokenizer;
 import org.jahia.services.search.JahiaSearchConstant;

 /**
  * Created by IntelliJ IDEA.
  * User: hollis
  * Date: 25 mai 2005
  * Time: 21:05:11
  * To change this template use File | Settings | File Templates.
  */
 public class TokenWithDotFilter extends SplitTokenFilter {

     private static final String ACRONYM_TYPE = StandardTokenizer.TOKEN_TYPES[StandardTokenizer.ACRONYM];
     private static final String EMAIL_TYPE = StandardTokenizer.TOKEN_TYPES[StandardTokenizer.EMAIL];
     private static final String HOST_TYPE = StandardTokenizer.TOKEN_TYPES[StandardTokenizer.HOST];

     public TokenWithDotFilter(TokenStream in) {
         super(in);
     }

     protected StringTokenizer splitWords(Token t) {
         StringTokenizer st = null;
         if (t.type() == ACRONYM_TYPE || t.type() == EMAIL_TYPE
                 || t.type() == HOST_TYPE) {
             String termText = t.termText();
             if (!termText.startsWith(JahiaSearchConstant.JAHIA_PREFIX)
                     && termText.indexOf(".") != -1) {
                 st = new StringTokenizer(termText, ".");
             }
         }
         return st;
     }
 }
