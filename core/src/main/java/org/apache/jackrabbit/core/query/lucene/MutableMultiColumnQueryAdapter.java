/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
