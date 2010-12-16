package org.apache.jackrabbit.core.query;

import org.apache.jackrabbit.core.query.lucene.join.SimpleQueryResult;

import javax.jcr.RepositoryException;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Simple query result implementation.
 */
public class JahiaSimpleQueryResult extends SimpleQueryResult {

    private RowIterator rowIterator;
    private List<Row> rows = new ArrayList<Row>();

    public JahiaSimpleQueryResult(String[] columnNames, String[] selectorNames, RowIterator rowIterator) {
        super(columnNames, selectorNames, rowIterator);
        this.rowIterator = rowIterator;
    }

    public RowIterator getRows() throws RepositoryException {
        return new Iterator();
    }

    private class Iterator implements RowIterator {
        private int position = 0;

        public Row nextRow() {
            if (position < rows.size()) {
                return rows.get(position++);
            }
            if (rowIterator.hasNext()) {
                rows.add(rowIterator.nextRow());
                return rows.get(position++);
            }
            throw new NoSuchElementException();
        }

        public void skip(long skipNum) {
            for (int i = 0; i < skipNum; i++) {
                nextRow();
            }
        }

        public long getSize() {
            return rowIterator.getSize();
        }

        public long getPosition() {
            return position;
        }

        public boolean hasNext() {
            return position < rows.size() || rowIterator.hasNext();
        }

        public Object next() {
            return nextRow();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
