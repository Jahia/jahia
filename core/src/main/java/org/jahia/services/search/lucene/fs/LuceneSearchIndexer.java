/*
 * Copyright 2002-2008 Jahia Ltd
 *
 * Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL), 
 * Version 1.0 (the "License"), or (at your option) any later version; you may 
 * not use this file except in compliance with the License. You should have 
 * received a copy of the License along with this program; if not, you may obtain 
 * a copy of the License at 
 *
 *  http://www.jahia.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
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
 * Date: 15 févr. 2005
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
                               Analyzer analyzer,
                               Properties config) throws JahiaException {
        super(localIndexing,analyzer,config);
        if ( indexDirectory != null ) {
            this.indexPath = indexDirectory.getAbsolutePath();
            FSDirectory fsDirectory = null;
            try {
                if ( !IndexReader.indexExists(indexDirectory) ){
                    fsDirectory = FSDirectory.getDirectory(indexDirectory,true);
                } else {
                    fsDirectory = FSDirectory.getDirectory(indexDirectory,false);
                }
                this.setIndexDirectory(fsDirectory);
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
