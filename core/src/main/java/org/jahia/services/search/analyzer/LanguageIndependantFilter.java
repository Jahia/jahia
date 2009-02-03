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

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 25 mai 2005
 * Time: 21:05:11
 * To change this template use File | Settings | File Templates.
 */
public class LanguageIndependantFilter extends TokenFilter {

    private Token cleanToken;

    static char[] filteredChars = new char[]{'á', 'à', 'â', 'é', 'è', 'ê', 'ô', 'ù'};

    static char[] replacedChars = new char[]{'a', 'a', 'a', 'e', 'e', 'e', 'o', 'u'};


    public LanguageIndependantFilter(TokenStream in) {
        super(in);
    }

    public final Token next() throws IOException {
        if ( cleanToken != null ){
            Token tempToken = cleanToken;
            cleanToken = null;
            return tempToken;
        }
        Token t = input.next();
        if (t == null) {
            return null;
        }

        String text = t.termText();
        if ( text == null || text.length()==0 ){
            return t;
        }
        StringBuffer trimmed = new StringBuffer();
        boolean found = false;
        boolean diffToken = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            found = false;
            for (int j = 0; j < filteredChars.length; j++) {
                if (c == filteredChars[j]) {
                    trimmed.append(replacedChars[j]);
                    found = true;
                    diffToken = true;
                    break;
                }
            }
            if (!found) {
                trimmed.append(c);
            }
        }
        if ( diffToken ){
            cleanToken = new Token(trimmed.toString(), t.startOffset(), t.endOffset(), t.type());
            cleanToken.setPositionIncrement(0);
        }
        trimmed = null;
        return t;
    }

    public static String format(String text){
        if ( text == null || text.length()==0 ){
            return text;
        }
        StringBuffer trimmed = new StringBuffer();
        boolean found = false;
        boolean diffToken = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            found = false;
            for (int j = 0; j < filteredChars.length; j++) {
                if (c == filteredChars[j]) {
                    trimmed.append(replacedChars[j]);
                    found = true;
                    diffToken = true;
                    break;
                }
            }
            if (!found) {
                trimmed.append(c);
            }
        }
        if ( diffToken ){
            return trimmed.toString();
        }
        return text;
    }
}
