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

import java.io.IOException;
import java.util.Stack;
import java.util.StringTokenizer;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.jahia.services.search.JahiaSearchConstant;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 25 mai 2005
 * Time: 21:05:11
 * To change this template use File | Settings | File Templates.
 */
public class TokenWithDotFilter extends TokenFilter {

    private static final String ACRONYM_TYPE = StandardTokenizerImpl.TOKEN_TYPES[StandardTokenizerImpl.ACRONYM];
    private static final String EMAIL_TYPE = StandardTokenizerImpl.TOKEN_TYPES[StandardTokenizerImpl.EMAIL];
    private static final String HOST_TYPE = StandardTokenizerImpl.TOKEN_TYPES[StandardTokenizerImpl.HOST];

    private Stack splittedWords;

    public TokenWithDotFilter(TokenStream in) {
        super(in);
        splittedWords = new Stack();
    }

    public final Token next() throws IOException {
        if (splittedWords.size() > 0) {
            return (Token) splittedWords.pop();
        }
        Token t = input.next();
        if (t == null) {
            return null;
        }
        splitWords(t);
        return t;
    }

    private void splitWords(Token t) {
        if (t.type() == ACRONYM_TYPE || t.type() == EMAIL_TYPE
                || t.type() == HOST_TYPE) {
            String termText = t.termText();
            if (termText.startsWith(JahiaSearchConstant.JAHIA_PREFIX)) {
                return;
            }

            if (termText.indexOf(".") != -1) {
                StringTokenizer st = new StringTokenizer(termText, ".");
                Token token = null;
                String text = null;
                while (st.hasMoreTokens()) {
                    text = st.nextToken();
                    if (text.length() > 1) {
                        token = new Token(text, t.startOffset(), t.endOffset());
                        token.setPositionIncrement(0);
                        splittedWords.push(token);
                    }
                }
            }
        }
    }

}
