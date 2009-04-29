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
/*
 * Copyright (c) 2004 Your Corporation. All Rights Reserved.
 */

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jahia.services.search.lucene.fs;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.BooleanQuery;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.search.JahiaSearchConstant;
import org.jahia.services.search.JahiaSearchService;
import org.jahia.services.search.SearchIndexer;
import org.jahia.services.search.lucene.fs.LuceneSearchIndexer;
import org.jahia.utils.RefCounted;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LuceneCoreSearcher {
    public static final String version="1.0";  
    
    private static Logger logger = Logger.getLogger(LuceneCoreSearcher.class);
    
    public static final String DEFAULT_INDEX_DIRECTORY = System.getProperty("java.io.tmpdir");

    private String indexDirectory = "";    
    
    // The current searcher used to service queries.
    // Don't access this directly!!!! use getSearcher() to
    // get it (and it will increment the ref count at the same time)
    private RefCounted _searcher;    
    private int onDeckSearchers;  // number of searchers preparing
    private int maxWarmingSearchers;  // max number of on-deck searchers allowed    
    private Object searcherLock = new Object();  // the sync object for the searcher
    
    private final Properties searchConfig;
    
    private final Properties filterCacheConfig;

    final ExecutorService searcherExecutor = Executors.newSingleThreadExecutor();    

    private SearchIndexer indexer = null;    
    
    public LuceneCoreSearcher(LuceneSearchHandlerImpl searchHandler, Properties properties) throws Exception {
        this.searchConfig = properties;
        
        this.filterCacheConfig = ServicesRegistry.getInstance().getJahiaSearchService().getFilterCacheConfig();
        
        indexDirectory = properties.getProperty("indexDirectory");
        
        String readOnly = properties.getProperty("readOnly");
        if ( readOnly != null ){
            searchHandler.setReadOnly("true".equalsIgnoreCase(readOnly));
        }

        int maxClauseCount = BooleanQuery.getMaxClauseCount();
        try {
            maxClauseCount = Integer.parseInt(properties.getProperty(JahiaSearchConstant.LUCENE_MAX_CLAUSE_COUNT));
        } catch ( Exception t){
        }
        if ( maxClauseCount >= 1024 ){
            BooleanQuery.setMaxClauseCount(maxClauseCount);
        }

        if ( !searchHandler.getReadOnly() ){
            File f = new File(indexDirectory);
            this.setIndexer(new LuceneSearchIndexer(f, true, properties));
            this.getIndexer().setSearchHandler(searchHandler);
            this.getIndexer().start();
        }        
        this.maxWarmingSearchers = Integer.parseInt(properties.getProperty("maxWarmingSearchers", Integer.toString(Integer.MAX_VALUE)));
    }
    
    public RefCounted getSearcher() throws JahiaException {
        try {
          return getSearcher(false,true,null);
        } catch (IOException e) {
          logger.error("Error getting searcher", e);
          return null;
        }
      }
    

    /**
     * Get a {@link JahiaIndexSearcher} or start the process of creating a new one.
     * <p>
     * The registered searcher is the default searcher used to service queries.
     * A searcher will normally be registered after all of the warming
     * and event handlers (newSearcher or firstSearcher events) have run.
     * In the case where there is no registered searcher, the newly created searcher will
     * be registered before running the event handlers (a slow searcher is better than no searcher).
     *
     * <p>
     * If <tt>forceNew==true</tt> then
     *  A new searcher will be opened and registered regardless of whether there is already
     *    a registered searcher or other searchers in the process of being created.
     * <p>
     * If <tt>forceNew==false</tt> then:<ul>
     *   <li>If a searcher is already registered, that searcher will be returned</li>
     *   <li>If no searcher is currently registered, but at least one is in the process of being created, then
     * this call will block until the first searcher is registered</li>
     *   <li>If no searcher is currently registered, and no searchers in the process of being registered, a new
     * searcher will be created.</li>
     * </ul>
     * <p>
     * If <tt>returnSearcher==true</tt> then a {@link RefCounted}&lt;{@link JahiaIndexSearcher}&gt; will be returned with
     * the reference count incremented.  It <b>must</b> be decremented when no longer needed.
     * <p>
     * If <tt>waitSearcher!=null</tt> and a new {@link JahiaIndexSearcher} was created,
     * then it is filled in with a Future that will return after the searcher is registered.  The Future may be set to
     * <tt>null</tt> in which case the JahiaIndexSearcher created has already been registered at the time
     * this method returned.
     * <p>
     * @param forceNew           if true, force the open of a new index searcher regardless if there is already one open.
     * @param returnSearcher     if true, returns a {@link JahiaIndexSearcher} holder with the refcount already incremented.
     * @param waitSearcher       if non-null, will be filled in with a {@link Future} that will return after the new searcher is registered.
     * @throws IOException
     */
    public RefCounted getSearcher(boolean forceNew, boolean returnSearcher, final Future<Object>[] waitSearcher) throws IOException, JahiaException {
      // it may take some time to open an index.... we may need to make
      // sure that two threads aren't trying to open one at the same time
      // if it isn't necessary.

      synchronized (searcherLock) {
        // see if we can return the current searcher
        if (_searcher!=null && !forceNew) {
          if (returnSearcher) {
            _searcher.incref();
            return _searcher;
          } else {
            return null;
          }
        }

        // check to see if we can wait for someone else's searcher to be set
        if (onDeckSearchers>0 && !forceNew && _searcher==null) {
          try {
            searcherLock.wait();
          } catch (InterruptedException e) {
            logger.info("Lock interrupted", e);
          }
        }

        // check again: see if we can return right now
        if (_searcher!=null && !forceNew) {
          if (returnSearcher) {
            _searcher.incref();
            return _searcher;
          } else {
            return null;
          }
        }

        // At this point, we know we need to open a new searcher...
        // first: increment count to signal other threads that we are
        //        opening a new searcher.
        onDeckSearchers++;
        if (onDeckSearchers < 1) {
          // should never happen... just a sanity check
          logger.fatal("ERROR!!! onDeckSearchers is " + onDeckSearchers);
          onDeckSearchers=1;  // reset
        } else if (onDeckSearchers > maxWarmingSearchers) {
          onDeckSearchers--;
          String msg="Error opening new searcher. exceeded limit of maxWarmingSearchers="+maxWarmingSearchers + ", try again later.";
          logger.warn(""+ msg);
          // HTTP 503==service unavailable, or 409==Conflict
          throw new JahiaException("Service unavailable", "Service unavailable", JahiaException.UNAVAILABLE_ERROR, JahiaException.ERROR_SEVERITY);
        } else if (onDeckSearchers > 1) {
          logger.info("PERFORMANCE WARNING: Overlapping onDeckSearchers=" + onDeckSearchers);
        }
      }

      // open the index synchronously
      // if this fails, we need to decrement onDeckSearchers again.
      JahiaIndexSearcher tmp;
      try {
          tmp = new JahiaIndexSearcher(this, "main", this.indexDirectory, true);          
      } catch (Exception th) {
        synchronized(searcherLock) {
          onDeckSearchers--;
          // notify another waiter to continue... it may succeed
          // and wake any others.
          searcherLock.notify();
        }
        // need to close the searcher here??? we shouldn't have to.
        throw new RuntimeException(th);
      }

      final JahiaIndexSearcher newSearcher=tmp;

      RefCounted currSearcherHolder=null;
      final RefCounted newSearchHolder=newHolder(newSearcher);
      if (returnSearcher) newSearchHolder.incref();

      // a signal to decrement onDeckSearchers if something goes wrong.
      final boolean[] decrementOnDeckCount=new boolean[1];
      decrementOnDeckCount[0]=true;

      try {

        boolean alreadyRegistered = false;
        synchronized (searcherLock) {
          if (_searcher == null) {
              JahiaSearchService searchService = ServicesRegistry.getInstance().getJahiaSearchService();
            // if there isn't a current searcher then we may
            // want to register this one before warming is complete instead of waiting.
            if (Boolean.valueOf(searchService.getConfig().getProperty("useColdSearcher")).booleanValue()) {
              registerSearcher(newSearchHolder);
              decrementOnDeckCount[0]=false;
              alreadyRegistered=true;
            }
          } else {
            // get a reference to the current searcher for purposes of autowarming.
            currSearcherHolder=_searcher;
            currSearcherHolder.incref();
          }
        }


        final JahiaIndexSearcher currSearcher = currSearcherHolder==null ? null : (JahiaIndexSearcher)currSearcherHolder.get();

        //
        // Note! if we registered the new searcher (but didn't increment it's
        // reference count because returnSearcher==false, it's possible for
        // someone else to register another searcher, and thus cause newSearcher
        // to close while we are warming.
        //
        // Should we protect against that by incrementing the reference count?
        // Maybe we should just let it fail?   After all, if returnSearcher==false
        // and newSearcher has been de-registered, what's the point of continuing?
        //

        Future<Object> future=null;

        // warm the new searcher based on the current searcher.
        // should this go before the other event handlers or after?
        if (currSearcher != null) {
          future = searcherExecutor.submit(
                  new Callable<Object>() {
                    public Object call() throws Exception {
                      try {
                        newSearcher.warm(currSearcher);
                      } catch (Exception e) {
                        logger.error("Error warming JahiaIndexSearcher",e);
                      }
                      return null;
                    }
                  }
          );
        }

        // WARNING: this code assumes a single threaded executor (that all tasks
        // queued will finish first).
        final RefCounted currSearcherHolderF = currSearcherHolder;
        if (!alreadyRegistered) {
          future = searcherExecutor.submit(
                  new Callable<Object>() {
                    public Object call() throws Exception {
                      try {
                        // signal that we no longer need to decrement
                        // the count *before* registering the searcher since
                        // registerSearcher will decrement even if it errors.
                        decrementOnDeckCount[0]=false;
                        registerSearcher(newSearchHolder);
                      } catch (Exception e) {
                        logger.error("Error registering searcher", e);
                      } finally {
                        // we are all done with the old searcher we used
                        // for warming...
                        if (currSearcherHolderF!=null) currSearcherHolderF.decref();
                      }
                      return null;
                    }
                  }
          );
        }

        if (waitSearcher != null) {
          waitSearcher[0] = future;
        }

        // Return the searcher as the warming tasks run in parallel
        // callers may wait on the waitSearcher future returned.
        return returnSearcher ? newSearchHolder : null;

      } catch (Exception e) {
        logger.error("Error creating and registering index searcher",e);
        if (currSearcherHolder != null) currSearcherHolder.decref();

        synchronized (searcherLock) {
          if (decrementOnDeckCount[0]) {
            onDeckSearchers--;
          }
          if (onDeckSearchers < 0) {
            // sanity check... should never happen
            logger.fatal("ERROR!!! onDeckSearchers after decrement=" + onDeckSearchers);
            onDeckSearchers=0; // try and recover
          }
          // if we failed, we need to wake up at least one waiter to continue the process
          searcherLock.notify();
        }

        // since the indexreader was already opened, assume we can continue on
        // even though we got an exception.
        return returnSearcher ? newSearchHolder : null;
      }

    }

    private RefCounted newHolder(JahiaIndexSearcher newSearcher) {
        RefCounted holder = new RefCounted(newSearcher) {
          public void close() {
            try {
              ((JahiaIndexSearcher)resource).close();
            } catch (IOException e) {
              logger.fatal("Error closing searcher", e);
            }
          }
        };
        holder.incref();  // set ref count to 1 to account for this._searcher
        return holder;
      }    

    // Take control of newSearcherHolder (which should have a reference count of at
    // least 1 already.  If the caller wishes to use the newSearcherHolder directly
    // after registering it, then they should increment the reference count *before*
    // calling this method.
    //
    // onDeckSearchers will also be decremented (it should have been incremented
    // as a result of opening a new searcher).
    private void registerSearcher(RefCounted newSearcherHolder) throws IOException {
      synchronized (searcherLock) {
        try {
          if (_searcher != null) {
            _searcher.decref();   // dec refcount for this._searcher
            _searcher=null;
          }

          _searcher = newSearcherHolder;
          JahiaIndexSearcher newSearcher = (JahiaIndexSearcher)newSearcherHolder.get();

          newSearcher.register(); // register subitems (caches)
          logger.info("Registered new searcher " + newSearcher);

        } catch (Exception e) {
          logger.error(e);
        } finally {
          // wake up anyone waiting for a searcher
          // even in the face of errors.
          onDeckSearchers--;
          searcherLock.notifyAll();
        }
      }
    }       
    
    //--------------------------------------------------------------------------
    /**
     *
     * @return
     * @throws java.io.IOException
     */
    public IndexReader getIndexReader () throws Exception {

        IndexReader reader = null;
        File indexDirFile = new File (this.indexDirectory);
        if (indexDirFile.exists ()) {
            reader = IndexReader.open (this.indexDirectory);
        } else {
            logger.warn (
                    "Cannot read index because directory "
                    + this.indexDirectory
                    + " does not exist, will be created upon first full site indexing...");
            return null;
        }
        return reader;
    }

    //--------------------------------------------------------------------------
    /**
     * Close a IndexReader
     *
     * @param reader the index reader
     */
    public void closeIndexReader (IndexReader reader) {
        if (reader == null)
            return;

        try {
            reader.close ();
        } catch (Exception t) {
            logger.error ("Error while closing index reader:", t);
        }
    }
    
    public SearchIndexer getIndexer() {
        return this.indexer;
    }

    public void setIndexer(SearchIndexer indexer) {
        this.indexer = indexer;
    }    
    
    public void closeSearcher() {
        logger.info("Closing main searcher on request.");
        synchronized (searcherLock) {
          if (_searcher != null) {
            _searcher.decref();   // dec refcount for this._searcher
            _searcher=null;
          }
        }
      }    
    
    public void close() {
        logger.info("CLOSING LuceneCoreSearcher!");
        try {
          closeSearcher();
        } catch (Exception e) {
          logger.warn(e);
        }
        try {
          searcherExecutor.shutdown();
        } catch (Exception e) {
          logger.warn(e);
        }
        try {
//          updateHandler.close();
        } catch (Exception e) {
          logger.warn(e);
        }
      }    
    
    protected void finalize() { close(); }
    
    public Properties getSearchConfig() {
        return searchConfig;
    }

    public Properties getFilterCacheConfig() {
        return filterCacheConfig;
    }    
}
