/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
