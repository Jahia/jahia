package org.jahia.services.search.analyzer;

import java.io.IOException;
import java.util.Stack;
import java.util.StringTokenizer;

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
public class TokenWithCommaFilter extends TokenFilter {

    private Stack splittedWords;

    public TokenWithCommaFilter(TokenStream in) {
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
        String termText = t.termText();
        if (termText.indexOf(",") != -1) {            
            StringTokenizer st = new StringTokenizer(termText, ",");
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
