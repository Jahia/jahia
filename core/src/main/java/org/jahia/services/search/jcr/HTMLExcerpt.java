/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.search.jcr;

import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.query.lucene.AbstractExcerpt;
import org.apache.jackrabbit.core.query.lucene.FieldNames;
import org.apache.jackrabbit.core.query.lucene.TermFactory;
import org.apache.jackrabbit.core.query.lucene.Util;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermPositionVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.regex.Pattern;

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

    private static final Pattern APOS = Pattern.compile("&apos;");
    
    @SuppressWarnings("serial")
    public static final FieldSelector FULLTEXT = new FieldSelector() {
        public FieldSelectorResult accept(String fieldName) {
            if (FieldNames.FULLTEXT == fieldName) {
                return FieldSelectorResult.LOAD;
            } else {
                return FieldSelectorResult.NO_LOAD;
            }
        }
    };    


    @Override
    protected String createExcerpt(TermPositionVector tpv, String text,
                                   int maxFragments, int maxFragmentSize) throws IOException {
        // TODO Auto-generated method stub
        return JahiaHighlighter.highlight(tpv, getQueryTerms(), text,
                "<div>", "</div>", "...", " ", "<span class=\"searchHighlightedText\">", "</span>",
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
                    doc = reader.document(docNumber, FULLTEXT);
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
                final String excerpt = super.getExcerpt(id, maxFragments, maxFragmentSize);
                if (excerpt != null) {
                    return APOS.matcher(excerpt).replaceAll("&#39;");
                } else return "";
            }
        } finally {
            Util.closeOrRelease(reader);
        }
    }

}
