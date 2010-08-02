package org.apache.jackrabbit.core.query.lucene;

import org.apache.jackrabbit.spi.Name;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;

import java.io.IOException;

/**
 * Same as MultiColumnQueryAdapter, but allows query to be modified
 */
public class MutableMultiColumnQueryAdapter implements MultiColumnQuery {
    /**
     * The underlying lucene query.
     */
    private Query query;

    /**
     * The selector name for the query hits.
     */
    private final Name selectorName;

    /**
     * Creates a new adapter for the given <code>query</code>.
     *
     * @param query        a lucene query.
     * @param selectorName the selector name for the query hits.
     */
    private MutableMultiColumnQueryAdapter(Query query, Name selectorName) {
        this.query = query;
        this.selectorName = selectorName;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query q) {
        query = q;
    }

    /**
     * Adapts the given <code>query</code>.
     *
     * @param query        the lucene query to adapt.
     * @param selectorName the selector name for the query hits.
     * @return a {@link MultiColumnQuery} that wraps the given lucene query.
     */
    public static MultiColumnQuery adapt(Query query, Name selectorName) {
        return new MutableMultiColumnQueryAdapter(query, selectorName);
    }

    /**
     * {@inheritDoc}
     */
    public MultiColumnQueryHits execute(JackrabbitIndexSearcher searcher, Ordering[] orderings, long resultFetchHint)
            throws IOException {
        SortField[] fields = new SortField[orderings.length];
        for (int i = 0; i < orderings.length; i++) {
            fields[i] = orderings[i].getSortField();
        }
        return searcher.execute(query, new Sort(fields), resultFetchHint, selectorName);
    }


}
