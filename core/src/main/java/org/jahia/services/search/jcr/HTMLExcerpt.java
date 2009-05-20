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
package org.jahia.services.search.jcr;

import java.io.IOException;

import org.apache.jackrabbit.core.query.lucene.AbstractExcerpt;
import org.apache.jackrabbit.core.query.lucene.DefaultHighlighter;
import org.apache.lucene.index.TermPositionVector;

/**
 * <code>HTMLExcerpt</code> creates a HTML excerpt with the following
 * format:
 * <pre>
 * &lt;div>
 *     &lt;span>&lt;strong class="searchHighlightedText">Jackrabbit&lt;/strong> implements both the mandatory XPath and optional SQL &lt;strong class="searchHighlightedText">query&lt;/strong> syntax.&lt;/span>
 *     &lt;span>Before parsing the XPath &lt;strong class="searchHighlightedText">query&lt;/strong> in &lt;strong class="searchHighlightedText">Jackrabbit&lt;/strong>, the statement is surrounded&lt;/span>
 * &lt;/div>
 * </pre>
 */
public class HTMLExcerpt extends AbstractExcerpt {

    @Override
    protected String createExcerpt(TermPositionVector tpv, String text,
            int maxFragments, int maxFragmentSize) throws IOException {
        // TODO Auto-generated method stub
        return DefaultHighlighter.highlight(tpv, getQueryTerms(), text,
                "<div>", "</div>", "<span>", "</span>", "<span class=\"searchHighlightedText\">", "</span>",
                maxFragments, maxFragmentSize / 2);
    }

}
