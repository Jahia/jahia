package org.apache.jackrabbit.core.query.lucene;

import org.jahia.services.content.nodetypes.ValueImpl;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Row;

public class CountRow implements Row {

    private long count;

    public CountRow(long count) {
        this.count = count;
    }

    public Value[] getValues() throws RepositoryException {
        return new Value[]{new ValueImpl(count)};
    }

    public Value getValue(String columnName) throws ItemNotFoundException, RepositoryException {
        return new ValueImpl(count);
    }

    public Node getNode() throws RepositoryException {
        return null;
    }

    public Node getNode(String selectorName) throws RepositoryException {
        return null;
    }

    public String getPath() throws RepositoryException {
        return null;
    }

    public String getPath(String selectorName) throws RepositoryException {
        return null;
    }

    public double getScore() throws RepositoryException {
        return 0;
    }

    public double getScore(String selectorName) throws RepositoryException {
        return 0;
    }
}
