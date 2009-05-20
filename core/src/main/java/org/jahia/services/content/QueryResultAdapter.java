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
package org.jahia.services.content;

import org.apache.jackrabbit.commons.iterator.RowIteratorAdapter;
import org.apache.jackrabbit.commons.iterator.NodeIteratorAdapter;

import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;
import javax.jcr.query.Row;
import javax.jcr.RepositoryException;
import javax.jcr.NodeIterator;
import javax.jcr.Node;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 13 nov. 2008
 * Time: 12:58:03
 * To change this template use File | Settings | File Templates.
 */
public class QueryResultAdapter implements QueryResult {

    private List<QueryResult> queryResults;
    private RowIteratorAdapter rowIterator;
    private NodeIteratorAdapter nodeIterator;

    /**
     * Wrapped query results that comes from different store
     *
     * @param queryResults
     */
    public QueryResultAdapter(List<QueryResult> queryResults) {
        this.queryResults = queryResults;
    }

    public String[] getColumnNames() throws RepositoryException {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public RowIterator getRows() throws RepositoryException {
        if (this.rowIterator == null){
            List<Row> rows = new ArrayList<Row>();
            for (QueryResult queryResult: queryResults){
                RowIterator rowIterator = queryResult.getRows();
                while(rowIterator.hasNext()){
                    rows.add(rowIterator.nextRow());
                }
            }
            this.rowIterator = new RowIteratorAdapter(rows);
        }
        return this.rowIterator;
    }

    public NodeIterator getNodes() throws RepositoryException {
        if (this.nodeIterator == null){
            List<Node> nodes = new ArrayList<Node>();
            for (QueryResult queryResult: queryResults){
                NodeIterator nodeIterator = queryResult.getNodes();
                while(nodeIterator.hasNext()){
                    nodes.add(nodeIterator.nextNode());
                }
            }
            this.nodeIterator = new NodeIteratorAdapter(nodes);
        }
        return this.nodeIterator;
    }
}
