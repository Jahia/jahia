/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.search.jcr;

import java.io.IOException;

import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.query.lucene.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermPositionVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    /**
     * Logger instance for this class.
     */
    private static final Logger log = LoggerFactory.getLogger(HTMLExcerpt.class);


    @Override
    protected String createExcerpt(TermPositionVector tpv, String text,
            int maxFragments, int maxFragmentSize) throws IOException {
        // TODO Auto-generated method stub
        return DefaultHighlighter.highlight(tpv, getQueryTerms(), text,
                "<div>", "</div>", "<span>", "</span>", "<span class=\"searchHighlightedText\">", "</span>",
                maxFragments, maxFragmentSize / 2);
    }

    @Override
    public String getExcerpt(NodeId id, int maxFragments, int maxFragmentSize) throws IOException {
        IndexReader reader = index.getIndexReader();
        try {
            Term idTerm = TermFactory.createUUIDTerm(id.toString());
            TermDocs tDocs = reader.termDocs(idTerm);
            int docNumber;
        Document doc;
        try {
            if (tDocs.next()) {
                docNumber = tDocs.doc();
                doc = reader.document(docNumber);
            } else {
                // node not found in index
                return null;
            }
        } finally {
            tDocs.close();
        }
        Fieldable[] fields = doc.getFieldables(FieldNames.FULLTEXT);
            if (fields.length == 0) {
                // Avoid to return all index entries as excerpt
                log.debug("Fulltext field not stored, using {}",
                        JahiaExcerptProvider.class.getName());
                JahiaExcerptProvider exProvider = new JahiaExcerptProvider();
                exProvider.init(query, index);
                return exProvider.getExcerpt(id, maxFragments, maxFragmentSize);

            } else {
                return super.getExcerpt(id, maxFragments, maxFragmentSize);
            }
        } finally {
            Util.closeOrRelease(reader);
        }
    }

}
