package org.apache.jackrabbit.core.query.lucene;

import org.jahia.services.content.impl.external.ValueImpl;

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

    @Override
    public Value[] getValues() throws RepositoryException {
        return new Value[]{new ValueImpl(count)};
    }

    @Override
    public Value getValue(String columnName) throws ItemNotFoundException, RepositoryException {
        return new ValueImpl(count);
    }

    @Override
    public Node getNode() throws RepositoryException {
        return null;
    }

    @Override
    public Node getNode(String selectorName) throws RepositoryException {
        return null;
    }

    @Override
    public String getPath() throws RepositoryException {
        return null;
    }

    @Override
    public String getPath(String selectorName) throws RepositoryException {
        return null;
    }

    @Override
    public double getScore() throws RepositoryException {
        return 0;
    }

    @Override
    public double getScore(String selectorName) throws RepositoryException {
        return 0;
    }
}
