package org.apache.jackrabbit.core.query.lucene;

import org.apache.lucene.index.IndexReader;

public class JahiaFilterMultiColumnQueryHits extends FilterMultiColumnQueryHits {
    private IndexReader reader = null;
    
    public JahiaFilterMultiColumnQueryHits(MultiColumnQueryHits hits, IndexReader reader) {
        super(hits);
        this.reader = reader;
    }

    public IndexReader getReader() {
        return reader;
    }

}
