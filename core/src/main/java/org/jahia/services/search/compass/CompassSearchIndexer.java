/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

 package org.jahia.services.search.compass;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.compass.core.*;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.search.*;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 15 f�vr. 2005
 * Time: 13:05:57
 * To change this template use File | Settings | File Templates.
 */
public class CompassSearchIndexer implements SearchIndexer {

    static String SEARCH_HANDLER_NAME = "jahia.searchhandler_name";

    private static Logger logger =
            Logger.getLogger (CompassSearchIndexer.class);

    private Properties config = new Properties();

    private boolean localIndexing = true;

    private SearchHandler searchHandler;

    private Compass compass = null;

    public CompassSearchIndexer(){
    }

    public CompassSearchIndexer( boolean localIndexing,
                                 Properties config){
        this.localIndexing = localIndexing;
        this.setConfig(config);
    }

    public void start() throws Exception {
        compass = ServicesRegistry.getInstance()
                .getJahiaSearchService().getCompass();
    }

    public boolean isLocalIndexing() {
        return localIndexing;
    }

    public void setLocalIndexing(boolean localIndexing) {
        this.localIndexing = localIndexing;
    }

    public Properties getConfig() {
        return config;
    }

    public void setConfig(Properties config) {
        this.config = config;
        if ( this.config == null ){
            this.config = new Properties();
        }
    }

    /**
     * Returns an IndexReader. Always return null. Not supported.
     *
     * @return
     * @throws Exception
     */
    public IndexReader getIndexReader() throws Exception {
        return null;
    }

    public synchronized void addDocument(IndexableDocument document){
        if ( document == null ) {
            return;
        }
        if ( !this.localIndexing ){
            return;
        }
        final Resource res = ServicesRegistry.getInstance().getJahiaSearchService()
                .getCompassResourceConverter().getResourceFromIndexableDocument(document);
        if ( res ==  null ){
            return;
        }
        try {
            ResourceFactory factory = compass.getResourceFactory();
            Property prop = factory.createProperty(SEARCH_HANDLER_NAME,
                    this.getSearchHandler().getName()
                    ,Property.Store.YES,Property.Index.TOKENIZED);
            res.addProperty(prop);
            CompassTemplate template = new CompassTemplate(compass);
            template.create(res);
        } catch (Exception t) {
            logger.error ("Error while indexing object " + document.getKey () + ":", t);
        } finally {
        }
    }

    public synchronized void removeDocument(RemovableDocument document){
        if ( document == null ){
            return;
        }
        if ( !this.localIndexing ){
            return;
        }
        CompassTemplate template = new CompassTemplate(compass);
        CompassDetachedHits hits = template.find(document.getKeyFieldName()+":"+NumberPadding.pad(document.getKey())).detach();
        int size = hits.getLength();
        Resource res = null;
        for ( int i=0; i<size; i++ ){
            res = hits.resource(i);
            try {
                template.delete(res);
            } catch ( Exception t ){
                logger.debug("Exception deleting resource res",t);
            }
        }
    }

    public synchronized void batchIndexing(final List toRemove, final List toAdd){
        if ( !this.localIndexing ){
            return;
        }
        long indexingStartTime = System.currentTimeMillis();

        try {
            final CompassTemplate template = new CompassTemplate(compass);
            final ResourceFactory factory = compass.getResourceFactory();
            template.execute(CompassTransaction.TransactionIsolation.READ_COMMITTED,new CompassCallback() {
                public Object doInCompass(CompassSession session) throws CompassException {
                    Iterator iterator = toRemove.iterator();
                    IndexableDocument doc = null;
                    while ( iterator.hasNext() ){
                        doc = (RemovableDocument)iterator.next();
                        CompassDetachedHits hits = session.find(doc.getKeyFieldName()+":"+
                                NumberPadding.pad(doc.getKey())).detach();
                        int size = hits.getLength();
                        Resource res = null;
                        for ( int i=0; i<size; i++ ){
                            res = hits.resource(i);
                            try {
                                session.delete(res);
                            } catch ( Exception t ){
                                logger.debug("Exception deleting resource res",t);
                            }
                        }
                    }
                    return null;
                }
            } );
            template.execute(CompassTransaction.TransactionIsolation.BATCH_INSERT,new CompassCallback() {
                public Object doInCompass(CompassSession session) throws CompassException {
                    Iterator iterator = toAdd.iterator();
                    IndexableDocument doc = null;
                    Resource res = null;
                    while ( iterator.hasNext() ){
                        doc = (IndexableDocument)iterator.next();
                        res = ServicesRegistry.getInstance().getJahiaSearchService()
                                .getCompassResourceConverter().getResourceFromIndexableDocument(doc);
                        if ( res ==  null ){
                            continue;
                        }
                        Property prop = factory.createProperty(SEARCH_HANDLER_NAME,
                                getSearchHandler().getName()
                                ,Property.Store.YES,Property.Index.TOKENIZED);
                        res.addProperty(prop);
                        session.create(res);
                    }
                    return null;
                }
            } );
        } catch (Exception t) {
            logger.error ("Error indexing ", t);
        } finally {
        }

        long indexingElapsedTime = System.currentTimeMillis() - indexingStartTime;
        if (logger.isInfoEnabled()) {
            logger.info(
                "Finished processing " + (toRemove.size() + toAdd.size()) +
                " indexing orders in " + indexingElapsedTime + "ms.");
        }
    }

    /**
     * syncrhonized batch removing of RemovableDocument from toRemoveList and then adding IndexableDocuments of toAdd
     * @param toRemove
     * @param toAdd
     */
    public void synchronizedBatchIndexing(List toRemove, List toAdd){
        // @COMPASS NOT UP TO DATE ANYMORE
    }
    
    public void setSearchHandler(SearchHandler searchHandler){
        this.searchHandler = searchHandler;
    }

    public SearchHandler getSearchHandler(){
        return this.searchHandler;
    }

    public int getBufferedDocs(){
        return -1; // not supported yet
    }

    public void wakeUp(){
    }

    public void shutdown(){
    }
}
