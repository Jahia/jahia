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
 package org.jahia.services.search.lucene.fs;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.jahia.services.search.lucene.AbstractLuceneSearchIndexer;
import org.jahia.exceptions.JahiaException;

import java.util.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 15 f�vr. 2005
 * Time: 13:05:57
 * To change this template use File | Settings | File Templates.
 */
public class LuceneSearchIndexer extends AbstractLuceneSearchIndexer {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (LuceneSearchIndexer.class);

    private String indexPath = "";
    
    public LuceneSearchIndexer(){
        super();
    }

    public LuceneSearchIndexer(File indexDirectory,
                               boolean localIndexing,
                               Properties config) throws JahiaException {
        super(localIndexing,config);
        if ( indexDirectory != null ) {
            this.indexPath = indexDirectory.getAbsolutePath();
            try {
                this.setIndexDirectory(FSDirectory.getDirectory(indexDirectory));
            } catch ( IOException ioe ) {
                logger.debug("Error opening FSDirectory", ioe);
            }
        }
        initDeletionPolicy();        
    }

    /**
     * Create a new IndexWriter
     *
     * @param analyzer
     * @return
     */
    protected IndexWriter createIndexWriter(Analyzer analyzer) throws Exception {
        return new IndexWriter(this.indexPath,analyzer,true);
    }

    /**
     * Returns the IndexReader for read only ( search ) .
     *
     * @return reader, the IndexReader, null if not found.
     */
    public IndexReader getIndexReaderForRead() throws Exception {
        return IndexReader.open(this.getIndexDirectory());
    }

}
