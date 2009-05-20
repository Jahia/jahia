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