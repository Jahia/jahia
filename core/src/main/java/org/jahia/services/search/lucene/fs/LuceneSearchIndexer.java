/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
