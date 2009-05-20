/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.search.analyzer;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

/**
 * Created by IntelliJ IDEA. User: hollis Date: 25 mai 2005 Time: 21:05:11 To change this template use File | Settings | File Templates.
 */
public abstract class SplitTokenFilter extends TokenFilter {

    protected SplitTokenFilter(TokenStream input) {
        super(input);
    }

    private StringTokenizer tokenizer = null;
    private Token current = null;

    /** Returns the next token in the stream, or null at EOS. */
    public Token next(final Token reusableToken) throws IOException {
        assert reusableToken != null;
        while (tokenizer != null && tokenizer.hasMoreTokens()) { // pop from stack
            Token nextToken = createToken(tokenizer.nextToken(), current,
                    reusableToken);
            if (nextToken != null) {
                return nextToken;
            }
        }

        Token nextToken = input.next(reusableToken);
        if (nextToken == null) {
            return null;
        }

        tokenizer = splitWords(nextToken);

        current = (Token) nextToken.clone();

        return nextToken;
    }

    protected abstract StringTokenizer splitWords(Token token);

    /**
     * Creates and returns a token for the given text of the current input token; Override for custom (stateless or stateful) behavior,
     * if desired.
     * 
     * @param synonym
     *            a synonym for the current token's term
     * @param current
     *            the current token from the underlying child stream
     * @param reusableToken
     *            the token to reuse
     * @return a new token, or null to indicate that the given synonym should be ignored
     */
    protected Token createToken(String text, Token current,
            final Token reusableToken) {
        Token token = new Token(text, current.startOffset(), current.endOffset());
        token.setPositionIncrement(0);
        return token;
    }
}
