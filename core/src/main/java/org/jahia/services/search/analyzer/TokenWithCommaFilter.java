package org.jahia.services.search.analyzer;

import java.util.StringTokenizer;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

/**
 * Created by IntelliJ IDEA. User: hollis Date: 25 mai 2005 Time: 21:05:11 To change this template use File | Settings | File Templates.
 */
public class TokenWithCommaFilter extends SplitTokenFilter {

    public TokenWithCommaFilter(TokenStream in) {
        super(in);
    }

    protected StringTokenizer splitWords(Token t) {
        String termText = t.termText();
        return termText.indexOf(",") != -1 ? new StringTokenizer(termText, ",")
                : null;
    }
}
