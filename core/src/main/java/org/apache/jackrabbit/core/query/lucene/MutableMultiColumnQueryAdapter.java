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

//package org.apache.jackrabbit.core.query.lucene;
//
//import org.apache.jackrabbit.spi.Name;
//import org.apache.lucene.search.Query;
//import org.apache.lucene.search.Sort;
//import org.apache.lucene.search.SortField;
//
//import java.io.IOException;
//
///**
// * Same as MultiColumnQueryAdapter, but allows query to be modified
// */
//public class MutableMultiColumnQueryAdapter implements MultiColumnQuery {
//    /**
//     * The underlying lucene query.
//     */
//    private Query query;
//
//    /**
//     * The selector name for the query hits.
//     */
//    private final Name selectorName;
//
//    /**
//     * Creates a new adapter for the given <code>query</code>.
//     *
//     * @param query        a lucene query.
//     * @param selectorName the selector name for the query hits.
//     */
//    private MutableMultiColumnQueryAdapter(Query query, Name selectorName) {
//        this.query = query;
//        this.selectorName = selectorName;
//    }
//
//    public Query getQuery() {
//        return query;
//    }
//
//    public void setQuery(Query q) {
//        query = q;
//    }
//
//    /**
//     * Adapts the given <code>query</code>.
//     *
//     * @param query        the lucene query to adapt.
//     * @param selectorName the selector name for the query hits.
//     * @return a {@link MultiColumnQuery} that wraps the given lucene query.
//     */
//    public static MultiColumnQuery adapt(Query query, Name selectorName) {
//        return new MutableMultiColumnQueryAdapter(query, selectorName);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    public MultiColumnQueryHits execute(JackrabbitIndexSearcher searcher, Ordering[] orderings, long resultFetchHint)
//            throws IOException {
//        SortField[] fields = new SortField[orderings.length];
//        for (int i = 0; i < orderings.length; i++) {
//            fields[i] = orderings[i].getSortField();
//        }
//        return searcher.execute(query, new Sort(fields), resultFetchHint, selectorName);
//    }
//
//
//}
