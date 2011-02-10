package org.apache.jackrabbit.core.query;

import org.apache.jackrabbit.commons.iterator.RowIteratorAdapter;
import org.apache.jackrabbit.core.query.lucene.FacetRow;
import org.apache.jackrabbit.core.query.lucene.LuceneQueryFactory;
import org.apache.jackrabbit.core.query.lucene.join.QueryEngine;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import javax.jcr.query.qom.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Override QueryEngine :
 *  - add facet handling in execute()
 */
public class JahiaQueryEngine extends QueryEngine {


    public JahiaQueryEngine(Session session, LuceneQueryFactory lqf, Map<String, Value> variables)
            throws RepositoryException {
        super(session, lqf, variables);
    }

    /**
     * Override QueryEngine.execute()
     */
    protected QueryResult execute(
            Column[] columns, Selector selector, Constraint constraint,
            Ordering[] orderings, long offset, long limit)
            throws RepositoryException {
        Map<String, NodeType> selectorMap = getSelectorNames(selector);
        String[] selectorNames =
            selectorMap.keySet().toArray(new String[selectorMap.size()]);

        Map<String, PropertyValue> columnMap =
            getColumnMap(columns, selectorMap);
        String[] columnNames =
            columnMap.keySet().toArray(new String[columnMap.size()]);

        try {
            final List<Row> rowsList = lqf.execute(columnMap, selector, constraint);
            // Added by jahia
            QueryResult result;
            if (rowsList.size() > 0 && rowsList.get(0) instanceof FacetRow) {
                FacetRow facets = (FacetRow) rowsList.remove(0);
                RowIterator rows = new RowIteratorAdapter(rowsList);
                result = new FacetedQueryResult(columnNames, selectorNames, rows, facets);
                QueryResult r = sort(result, orderings, offset, limit);
                if (r != result) {
                    result = new FacetedQueryResult(columnNames, selectorNames, r.getRows(), facets);
                }
            } else {
                RowIterator rows = new RowIteratorAdapter(rowsList);
                result = new JahiaSimpleQueryResult(columnNames, selectorNames, rows);
                QueryResult r = sort(result, orderings, offset, limit);
                if (r != result) {
                    result = new JahiaSimpleQueryResult(columnNames, selectorNames, r.getRows());
                }
            }
            return result;
            // End
        } catch (IOException e) {
            throw new RepositoryException(
                    "Failed to access the query index", e);
        }
    }

    @Override
    protected QueryResult execute(Column[] columns, Join join, Constraint constraint, Ordering[] orderings, long offset, long limit) throws RepositoryException {
        QueryResult result = super.execute(columns, join, constraint, orderings, offset, limit);
        result = new JahiaSimpleQueryResult(result.getColumnNames(), result.getSelectorNames(), result.getRows());
        return result;
    }

    @Override
    public QueryResult execute(Column[] columns, Source source, Constraint constraint, Ordering[] orderings, long offset, long limit) throws RepositoryException {
        QueryResult result = super.execute(columns, source, constraint, orderings, offset, limit);
        result = new JahiaSimpleQueryResult(result.getColumnNames(), result.getSelectorNames(), result.getRows());
        return result;
    }
}
