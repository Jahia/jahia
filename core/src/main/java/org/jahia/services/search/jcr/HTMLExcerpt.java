/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
                "<div>", "</div>", "<span>", "</span>", "<strong class=\"searchHighlightedText\">", "</strong>",
                maxFragments, maxFragmentSize / 2);
    }

}
