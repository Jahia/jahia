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

 package org.jahia.services.search.lucene;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexDeletionPolicy;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.store.Directory;
import org.compass.core.Compass;
import org.compass.core.Property.Index;
import org.compass.core.engine.SearchEngineFactory;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.analyzer.LuceneAnalyzerManager;
import org.compass.core.lucene.util.LuceneUtils;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.spi.InternalCompass;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.search.DocumentField;
import org.jahia.services.search.IndexableDocument;
import org.jahia.services.search.JahiaSearchConstant;
import org.jahia.services.search.NumberPadding;
import org.jahia.services.search.RemovableDocument;
import org.jahia.services.search.SearchEvent;
import org.jahia.services.search.SearchHandler;
import org.jahia.services.search.SearchIndexer;
import org.jahia.utils.JahiaTools;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 15 f�vr. 2005
 * Time: 13:05:57
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractLuceneSearchIndexer implements SearchIndexer , Runnable {

    private static Logger logger =
            Logger.getLogger (AbstractLuceneSearchIndexer.class);

    /** The Lucene analyzer * */
    private Analyzer defaultAnalyzer;
    final private static String DEFAULT_ANALYZER = "default"; 
    private Map<String, Analyzer> analyzers = new HashMap<String, Analyzer>();
    
    private ResourceMapping resourceMapping;    

    private byte[] lock = new byte[0];

    private byte[] synchronizedlock = new byte[0];

    private Properties config = new Properties();

    // list of all removed/added fields to add/remove from the index in background
    private List<IndexableDocument> indexOrders = new ArrayList<IndexableDocument>(); // les ordres de add/remove

    private Thread backgroundIndexingThread;

    private boolean indexingThreadActivated = true;

    private boolean localIndexing = true;

    private SearchHandler searchHandler;

    protected Directory indexDirectory;

    private long optimizationInterval;

    private long luceneWriteLockTimeOut = IndexWriter.WRITE_LOCK_TIMEOUT;
    private int luceneMergeFactor = IndexWriter.DEFAULT_MERGE_FACTOR;
    private int luceneMinMergeDocs = 10;
    private int luceneMaxMergeDocs = IndexWriter.DEFAULT_MAX_MERGE_DOCS;
    private int luceneMaxBufferedDocs = IndexWriter.DEFAULT_MAX_BUFFERED_DOCS;
    private int luceneMaxFieldLength = IndexWriter.DEFAULT_MAX_FIELD_LENGTH;
    private boolean luceneUseCompoundFile = true;
    private IndexDeletionPolicy indexDelitionPolicy = null;
    private boolean luceneIndexerAutoCommit = true;        

    public AbstractLuceneSearchIndexer(){
    }

    public AbstractLuceneSearchIndexer( boolean localIndexing,
                                        Analyzer analyzer,
                                        Properties config) throws JahiaException {
        this.localIndexing = localIndexing;
        this.defaultAnalyzer = analyzer;
        if (this.defaultAnalyzer == null) {
            Compass compass = ServicesRegistry.getInstance()
                    .getJahiaSearchService().getCompass();
            if (compass != null && compass instanceof InternalCompass) {
                InternalCompass internalCompass = (InternalCompass) compass;
                SearchEngineFactory searchEngineFactory = internalCompass
                        .getSearchEngineFactory();
                if (searchEngineFactory != null
                        && searchEngineFactory instanceof LuceneSearchEngineFactory) {
                    LuceneSearchEngineFactory luceneSearchEngineFactory = (LuceneSearchEngineFactory) searchEngineFactory;
                    LuceneAnalyzerManager analyzerMgr = luceneSearchEngineFactory.getAnalyzerManager();
                    this.defaultAnalyzer = analyzerMgr.getAnalyzerByAliasMustExists("jahiaIndexer");
                    for (ResourceMapping compassMapping : luceneSearchEngineFactory
                            .getMapping().getRootMappings()) {
                        if (compassMapping.getAlias()
                                .startsWith("jahiaIndexer")) {
                            String key = DEFAULT_ANALYZER;
                            int index = compassMapping.getAlias().indexOf('_');
                            if (index != -1) {
                                key = compassMapping.getAlias().substring(
                                        index + 1);
                            }
                            analyzers.put(key, analyzerMgr
                                    .getAnalyzerByAlias(compassMapping
                                            .getAlias()));
                        }

                    }
                    
                    this.resourceMapping = luceneSearchEngineFactory.getMapping().getRootMappingByAlias("jahiaIndexer");                    
                }
            }
        }
        if ( config != null ){
            this.setConfig((Properties)config.clone());
        }

        this.optimizationInterval = JahiaTools.getTimeAsLong(config.getProperty("optimizationInterval",
                "12h"),"12h").longValue();
        initLuceneSettings();
    }

    public void start() throws Exception {

        if ( !IndexReader.indexExists(this.getIndexDirectory()) ){
            getIndexWriter (this.defaultAnalyzer, true);
        }

        // now let's remove any stale lock files if there were some.
        removeStaleLockFiles ();

        backgroundIndexingThread = new Thread(this,"LuceneSearchIndexer Background Thread");
        int priority = Thread.NORM_PRIORITY;
        String prop = this.getConfig().getProperty("org.apache.lucene.backgroundIndexing.priority");
        if ( prop != null){
            try {
                priority = Integer.parseInt(prop);
            } catch ( Exception t ){
            }
        }
        backgroundIndexingThread.setPriority(priority);
        backgroundIndexingThread.setDaemon(true);
        backgroundIndexingThread.start(); // start background thread
    }

    public boolean isLocalIndexing() {
        return localIndexing;
    }

    public void setLocalIndexing(boolean localIndexing) {
        this.localIndexing = localIndexing;
    }

    public Directory getIndexDirectory() {
        return indexDirectory;
    }

    public void setIndexDirectory(Directory indexDirectory) {
        this.indexDirectory = indexDirectory;
    }

    public Analyzer getAnalyzer() {
        return defaultAnalyzer;
    }

    public void setAnalyzer(Analyzer analyzer) {
        this.defaultAnalyzer = analyzer;
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

    public void addDocument(IndexableDocument document){
        if ( document == null ){
            return;
        }
        if ( this.localIndexing ){
             addToIndexOrder(document);
        }
        synchronized(lock){
            lock.notifyAll();
        }
        SearchEvent ev = new SearchEvent(this,document);
        this.getSearchHandler().notify(ev,"addDocument");
    }

    public void wakeUp(){
        synchronized(lock){
            lock.notifyAll();
        }
    }
    
    public void removeDocument(RemovableDocument document){
        if ( document == null ){
            return;
        }
        if ( this.localIndexing ){
             addToIndexOrder(document);
        }
        synchronized(lock){
            lock.notifyAll();
        }
        SearchEvent ev = new SearchEvent(this,document);
        this.getSearchHandler().notify(ev,"removeDocument");
    }

    public void batchIndexing(List<RemovableDocument> toRemove, List<IndexableDocument> toAdd){
        if ( !this.localIndexing ){
            return;
        }
        synchronized(this){
            this.indexOrders.addAll(toRemove);
            this.indexOrders.addAll(toAdd);
            synchronized(lock){
                lock.notifyAll();
            }
        }
    }

    public void synchronizedBatchIndexing(List<RemovableDocument> toRemove, List<IndexableDocument> toAdd){
        if ( !this.localIndexing ){
            return;
        }
        List<IndexableDocument> docs = new ArrayList<IndexableDocument>();
        docs.addAll(toRemove);
        docs.addAll(toAdd);
        synchronized(synchronizedlock){
            long indexingStartTime = System.currentTimeMillis();
            int indexOrderCount = docs.size ();
            int maxBufferedDoc = 50;
            try {
                maxBufferedDoc = Integer.parseInt(getConfig()
                        .getProperty(JahiaSearchConstant.LUCENE_MAX_BUFFERED_DOCS));
            } catch ( Exception t ){
            }
            Indexer indexer = new Indexer(maxBufferedDoc);
            for (ListIterator<IndexableDocument> it = docs.listIterator(); it.hasNext();) {
                IndexableDocument doc = it.next();
                it.remove();

                // okay now we have the next added/removed field, we process it!
                if (doc != null) {
                    try {
                        /*
                        if (doc instanceof RemovableDocument){
                            logger.info("syncrhonized indexation of content " + doc.getKey());
                        }*/
                        indexer.addDocument(doc);
                    } catch ( Exception t ){
                        logger.debug("Error addind document to Indexer=", t);
                    }
                }
            }

            try {
                indexer.storeInPersistance();
            } catch (Exception t) {
                logger.debug(
                    "Error calling storeInPersistance on indexer",
                    t);
            }

            if (indexOrderCount > 0) {
                SearchHandler searchHandler = getSearchHandler();
                searchHandler.notifyIndexUpdate();
                if (logger.isInfoEnabled()) {    
                    long indexingElapsedTime = System.currentTimeMillis() - indexingStartTime;
                    logger.info("Finished synchronized processing " + indexOrderCount
                            + " indexing orders in " + indexingElapsedTime
                            + "ms.");
                }
            }
        }
    }

    public void setSearchHandler(SearchHandler searchHandler){
        this.searchHandler = searchHandler;
    }

    public SearchHandler getSearchHandler(){
        return this.searchHandler;
    }

    //--------------------------------------------------------------------------
    /**
     * Background task that handles the remove/add fields to search engine from
     * the queue
     */
    public void run () {

        logger.info("search service is running in Thread name=" + Thread.currentThread().getName());

        int maxBufferedDoc = 50;
        try {
            maxBufferedDoc = Integer.parseInt(getConfig()
                    .getProperty(JahiaSearchConstant.LUCENE_MAX_BUFFERED_DOCS));
        } catch ( Exception t ){
        }
        Indexer indexer = new Indexer(maxBufferedDoc);
        long lastOptimizationTime = System.currentTimeMillis();

        while (!Thread.currentThread().isInterrupted() && indexingThreadActivated) {
            List<IndexableDocument> validDocs = null;
            long now = System.currentTimeMillis();
            IndexableDocument doc = null;

            int size = 0;

            synchronized(synchronizedlock){

                synchronized (this) {
                    /*
                    for ( int i=0; i<indexOrders.size(); i++ ){
                        doc = (IndexableDocument)indexOrders.get(i);
                        if ( doc instanceof RemovableDocument ) {
                            remDoc = (RemovableDocument)doc;
                            for ( int j=0; j<v.size() ; j++ ){
                                doc2 = (IndexableDocument)v.get(j);
                                if ( doc2 != null && !(doc2 instanceof RemovableDocument) ){
                                    addDoc = doc2;
                                    if ( remDoc.getKeyFieldName().equals(addDoc.getKeyFieldName())
                                            && remDoc.getKey().equals(addDoc.getKey()) ){
                                        // we are going to remove the same object, so we don't need to add it at all.
                                        // @todo: we should provide more efficient object equality check
                                        // i.e, on containerId, not only on comp_id
                                        v.setElementAt(null,j);
                                    }
                                }
                            }
                            // process remove first
                            v.insertElementAt(doc,0);
                        } else {
                            // process add at last
                            v.add(doc);
                        }
                    }*/
                    /*
                    validDocs = new ArrayList();
                    int size = v.size();
                    for ( int i=0; i<size ; i++ ){
                        doc = (IndexableDocument)indexOrders.get(i);
                        if ( doc != null){
                            validDocs.add(doc);
                        }
                    }
                    v = null;
                    */
                    validDocs = new ArrayList<IndexableDocument>();
                    validDocs.addAll(indexOrders);
                    indexOrders = new ArrayList<IndexableDocument>();
                }

                long indexingStartTime = System.currentTimeMillis();
                int indexOrderCount = validDocs.size ();

                for (ListIterator<IndexableDocument> it = validDocs.listIterator(); it.hasNext();) {
                    doc = it.next();
                    it.remove();                    

                    // okay now we have the next added/removed field, we process it!
                    if (doc != null) {
                        try {
                            indexer.addDocument(doc);
                        } catch ( Exception t ){
                            logger.debug("Error addind document to Indexer=", t);
                        }
                    }
                }

                try {
                    indexer.storeInPersistance();
                } catch (Exception t) {
                    logger.info(
                        "Error calling storeInPersistance on indexer",
                        t);
                }

                if (indexOrderCount > 0) {
                    SearchHandler searchHandler = getSearchHandler();
                    searchHandler.notifyIndexUpdate();
                    if (logger.isInfoEnabled()) {
                        long indexingElapsedTime = System.currentTimeMillis() - indexingStartTime;
                        logger.info("Finished processing " + indexOrderCount
                                + " indexing orders in " + indexingElapsedTime
                                + "ms.");
                    }
                }

                if ( ( now - lastOptimizationTime) > this.optimizationInterval ) {
                    lastOptimizationTime = now;
                }

                // FIXME : oops, As we are in a separate thread, this would may have sence
                // to terminate Connection ?
                //org.jahia.services.database.ConnectionDispenser.
                //        terminateConnection ();
                synchronized (this) {
                    size = indexOrders.size();
                }
                if (size == 0) {
                    synchronizedlock.notifyAll();
                }
            }
            if (size == 0) {
                synchronized (lock) {
                    try {
                        lock.wait(); // wait for next notify
                    }
                    catch (InterruptedException ie) {
                    }
                }
            }

        }
    }

    private synchronized void addToIndexOrder(IndexableDocument indObj){
        if ( indObj == null ){
            return;
        }
        this.indexOrders.add(indObj);
        /*
       IndexableDocument doc = null;
       int size = this.indexOrders.size();
       List result = new ArrayList();
       for ( int i=0; i<size; i++ ){
           doc = (IndexableDocument)this.indexOrders.get(i);
           if ( !(indObj instanceof RemovableDocument)
                   && !(doc instanceof RemovableDocument)
               && indObj.getKeyFieldName().equals(doc.getKeyFieldName())
               && indObj.getKey().equals(doc.getKey()) ){
              // same doc, ignore old doc
          } else if ( (indObj instanceof RemovableDocument)
                   && (doc instanceof RemovableDocument)
              && indObj.getKeyFieldName().equals(doc.getKeyFieldName())
              && indObj.getKey().equals(doc.getKey()) ){
              // same doc, ignore old doc
           } else {
               result.add(doc);
           }
       }
       result.add(indObj);
       this.indexOrders = result;
       */
    }

    //--------------------------------------------------------------------------
    /**
     * Remove an object from search engine in background
     */
    private void backgroundRemoveObjectFromSearchEngine (IndexableDocument indObj,
                                                         IndexReader reader) {

        // Create a term with the object key unique identifier.
        Term term = new Term (indObj.getKeyFieldName (),
                indObj.getKey ().toLowerCase());
        removeDoc (term, reader);
        term = new Term (indObj.getKeyFieldName (),
                indObj.getKey ());
        removeDoc (term, reader);
    }


    /**
     * Removes all *.lock files in the specified directory, going down
     * recursively. Make sure you call this ONLY on lucene managed directories.
     *
     */
    protected void removeStaleLockFiles (){
        try {
            if ( IndexReader.isLocked(this.getIndexDirectory()) ){
                IndexReader.unlock(this.getIndexDirectory());
            }
        } catch ( Exception t ){
            logger.debug("Exception removing locks",t);
        }
    }


    //--------------------------------------------------------------------------
    /**
     * Remove a doc containing a given term
     *
     * @param term
     * @param reader optional, use it if not null
     */
    private void removeDoc (Term term, IndexReader reader) {

        boolean closeReader = false;
        try {

            // Try to get the site's index if any.
            if ( reader == null ){
                closeReader = true;
                reader = getIndexReader();
            }

            if (reader == null)
                return;

            // Remove all documents containing the term.
            int nbDeleted = reader.deleteDocuments(term);
            logger.debug( "Field removed :" + term.field() + " ,  " + term.text() + " , "
                                   + String.valueOf(nbDeleted) + " Doc deleted ");
        } catch (Exception t) {
            logger.error ("Error while removing doc ", t);
        } finally {
            if ( closeReader ){
                closeIndexReader(reader);
            }
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Returns the IndexWriter for a given site.
     * Don't forget to close the returned index ramWriter to flush change to the index file !
     *
     * @param analyzer the analyzer to use.
     * @param create if true, create a new index and replace existing one.
     *
     * @return IndexWriter ramWriter, the IndexWriter, null on error.
     */
    private IndexWriter getIndexWriter (Analyzer analyzer,
                                        boolean create) throws JahiaException {
        IndexWriter writer = null;
        try {
            writer = new IndexWriter (this.getIndexDirectory(), this.isLuceneIndexerAutoCommit(),  analyzer,
                    create, this.getIndexDelitionPolicy());
            writer.setMaxBufferedDocs(this.getLuceneMaxBufferedDocs());
            writer.setMaxFieldLength(this.getLuceneMaxFieldLength());
            writer.setMaxMergeDocs(this.getLuceneMaxMergeDocs());
            writer.setMergeFactor(this.getLuceneMergeFactor());
            writer.setUseCompoundFile(this.getLuceneUseCompoundFile());
        } catch (Exception t) {
            logger.error (
                    "An IO Exception occured creating indexWriter for directory",
                    t);
        }
        return writer;
    }

    //--------------------------------------------------------------------------
    /**
     * Returns the IndexReader.
     * Don't forget to close the returned index reader to flush change to the index file !
     *
     * @return IndexReader reader, the IndexReader, null if not found.
     */
    public IndexReader getIndexReader ()
            throws IOException {
        return IndexReader.open(this.getIndexDirectory());
    }

    //--------------------------------------------------------------------------
    /**
     * Close an IndexWriter
     *
     * @param writer
     */
    private void closeIndexWriter (IndexWriter writer) {
        if (writer == null)
            return;

        try {
            writer.close ();
        } catch (Exception t) {
            logger.error ("Error while closing index ramWriter:", t);
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Close an IndexReader
     *
     * @param reader
     */
    private void closeIndexReader (IndexReader reader) {
        if (reader == null)
            return;

        try {
            reader.close ();
        } catch (Exception t) {
            logger.error ("Error while closing index reader:", t);
        }
    }

    public void shutdown(){
        logger.info("Shutting down indexer started");
        this.indexingThreadActivated = false;
        backgroundIndexingThread.interrupt();
        logger.info("...indexer shutdown done");
    }

    public Document getLuceneDocument(IndexableDocument indObj){
        if (indObj == null)
            return null;

        Document doc = new Document ();
        Map<String, DocumentField> fields = indObj.getFields ();
        if (fields != null) {
            for (DocumentField docField : fields.values()) {
                for (String originalValue : docField.getValues() ){
                    String name = docField.getName().toLowerCase();
                    // FIXME : should we call val.getValueAsString(Locale)
                    // @todo : number padding, we should be done somewhere else and customizable
                    String paddedValue = NumberPadding.pad(originalValue);
                    /*
                    if ( paddedValue != null ){
                        paddedValue = paddedValue.toLowerCase();
                    }*/
                    Index index = null;
                    float boost = 1;
                    if (resourceMapping != null) {
                        ResourcePropertyMapping propertyMapping = resourceMapping
                                .getResourcePropertyMapping(name);
                        if (propertyMapping != null) {
                            index = propertyMapping.getIndex();
                            boost = propertyMapping.getBoost();
                        }
                    }
                    if (docField.isKeyword() ) {
                        Field field = new Field(name, paddedValue, Field.Store.YES, index == null ? Field.Index.UN_TOKENIZED : LuceneUtils.getFieldIndex(index));
                        field.setBoost(boost);
                        doc.add (field);
                    } else if ( docField.isText() ) {
                        Field field = new Field(name, paddedValue, Field.Store.YES, index == null ? Field.Index.TOKENIZED : LuceneUtils.getFieldIndex(index));
                        field.setBoost(boost);
                        doc.add (field);
                        if ( paddedValue != originalValue && (index == null || !index.equals(Index.NO))){
                            field = new Field(name+JahiaSearchConstant.NO_PADDED_FIELD_POSTFIX, originalValue, Field.Store.NO, index == null ? Field.Index.TOKENIZED : LuceneUtils.getFieldIndex(index));
                            field.setBoost(boost);
                            doc.add (field);
                        }
                    } else if ( docField.isUnindexed() ){
                        Field field = new Field(name, paddedValue, Field.Store.YES, Field.Index.NO); 
                        field.setBoost(boost);
                        doc.add (field);
                    } else if ( docField.isUnstoredText() && (index == null || !index.equals(Index.NO))){
                        //doc.add (Field.UnStored(name.toLowerCase(), paddedValue));
                        // to simplify highlighting, we actually store field content in index
                        Field field = new Field(name, paddedValue, Field.Store.NO, index == null ? Field.Index.TOKENIZED : LuceneUtils.getFieldIndex(index));
                        field.setBoost(boost);
                        doc.add (field);
                        if ( paddedValue != originalValue ){
                            field = new Field(name+JahiaSearchConstant.NO_PADDED_FIELD_POSTFIX, originalValue, Field.Store.NO, index == null ? Field.Index.TOKENIZED : LuceneUtils.getFieldIndex(index));
                            field.setBoost(boost);
                            doc.add (field);
                        }
                    }
                }
            }
        }
        String name = indObj.getKeyFieldName ().toLowerCase(); 
        Index index = null;
        float boost = 1;
        if (resourceMapping != null) {
            ResourcePropertyMapping propertyMapping = resourceMapping
                    .getResourcePropertyMapping(name);
            if (propertyMapping != null) {
                index = propertyMapping.getIndex();
                boost = propertyMapping.getBoost();
            }
        }
        Field field = new Field(name, NumberPadding.pad(indObj.getKey ()),
                Field.Store.YES, index == null ? Field.Index.UN_TOKENIZED : LuceneUtils.getFieldIndex(index));
        field.setBoost(boost);
        doc.add (field);
        return doc;
    }

    public synchronized int getBufferedDocs(){
        return this.indexOrders.size();
    }

    public int getLuceneMaxBufferedDocs() {
        return luceneMaxBufferedDocs;
    }

    public void setLuceneMaxBufferedDocs(int luceneMaxBufferedDocs) {
        this.luceneMaxBufferedDocs = luceneMaxBufferedDocs;
    }

    public int getLuceneMaxFieldLength() {
        return luceneMaxFieldLength;
    }

    public void setLuceneMaxFieldLength(int luceneMaxFieldLength) {
        this.luceneMaxFieldLength = luceneMaxFieldLength;
    }

    public int getLuceneMaxMergeDocs() {
        return luceneMaxMergeDocs;
    }

    public void setLuceneMaxMergeDocs(int luceneMaxMergeDocs) {
        this.luceneMaxMergeDocs = luceneMaxMergeDocs;
    }

    public int getLuceneMergeFactor() {
        return luceneMergeFactor;
    }

    public void setLuceneMergeFactor(int luceneMergeFactor) {
        this.luceneMergeFactor = luceneMergeFactor;
    }

    public int getLuceneMinMergeDocs() {
        return luceneMinMergeDocs;
    }

    public void setLuceneMinMergeDocs(int luceneMinMergeDocs) {
        this.luceneMinMergeDocs = luceneMinMergeDocs;
    }

    public long getLuceneWriteLockTimeOut() {
        return luceneWriteLockTimeOut;
    }

    public void setLuceneWriteLockTimeOut(long luceneWriteLockTimeOut) {
        this.luceneWriteLockTimeOut = luceneWriteLockTimeOut;
    }

    public boolean getLuceneUseCompoundFile() {
        return luceneUseCompoundFile;
    }

    public void setLuceneUseCompoundFile(boolean luceneUseCompoundFile) {
        this.luceneUseCompoundFile = luceneUseCompoundFile;
    }

    protected void initLuceneSettings(){
        this.setLuceneMaxBufferedDocs(parseInt(this.config.getProperty(JahiaSearchConstant.LUCENE_MAX_BUFFERED_DOCS),
                this.getLuceneMaxBufferedDocs()));
        this.setLuceneMaxFieldLength(parseInt(this.config.getProperty(JahiaSearchConstant.LUCENE_MAX_FIELD_LENGTH),
                this.getLuceneMaxFieldLength()));
        this.setLuceneMaxMergeDocs(parseInt(this.config.getProperty(JahiaSearchConstant.LUCENE_MAX_MERGE_DOCS),
                this.getLuceneMaxMergeDocs()));
        this.setLuceneMergeFactor(parseInt(this.config.getProperty(JahiaSearchConstant.LUCENE_MERGE_FACTOR),
                this.getLuceneMergeFactor()));
        this.setLuceneMinMergeDocs(parseInt(this.config.getProperty(JahiaSearchConstant.LUCENE_MIN_MERGE_DOCS),
                this.getLuceneMinMergeDocs()));
        this.setLuceneWriteLockTimeOut(parseLong(this.config.getProperty(JahiaSearchConstant.LUCENE_WRITE_LOCK_TIMEOUT),
                this.getLuceneWriteLockTimeOut()));
        int maxClauseCount = BooleanQuery.getMaxClauseCount();
        try {
            maxClauseCount = Integer.parseInt(this.config.getProperty(JahiaSearchConstant.LUCENE_MAX_CLAUSE_COUNT));
        } catch ( Exception t){
        }
        if ( maxClauseCount >= 1024 ){
            BooleanQuery.setMaxClauseCount(maxClauseCount);
        }
        boolean useCompoundFile = this.getLuceneUseCompoundFile();
        try {
            useCompoundFile = !("0".equals(this.config.getProperty(JahiaSearchConstant.LUCENE_USE_COMPOUND_FILE)));
        } catch ( Exception t){
        }
        this.setLuceneUseCompoundFile(useCompoundFile);
        boolean isAutoCommit = this.isLuceneIndexerAutoCommit();
        try {
            isAutoCommit = !("0".equals(this.config.getProperty(JahiaSearchConstant.LUCENE_INDEXER_AUTO_COMMIT)));
        } catch ( Throwable t){
        }        
        this.setLuceneIndexerAutoCommit(isAutoCommit);
    }
    
    protected void initDeletionPolicy() {
        String deletionPolicy = this.config
                .getProperty(JahiaSearchConstant.LUCENE_INDEX_DELETION_POLICY);
        if (deletionPolicy != null
                && (deletionPolicy = deletionPolicy.trim()).length() > 0) {
            try {
                Class<? extends IndexDeletionPolicy> policyClass = Class
                        .forName(deletionPolicy).asSubclass(
                                IndexDeletionPolicy.class);
                IndexDeletionPolicy policyObj = null;
                if (deletionPolicy.indexOf("ExpirationTimeDeletionPolicy") != -1) {
                    String expirationTime = this.config
                            .getProperty(JahiaSearchConstant.LUCENE_INDEX_DELETION_EXPIRATION_TIME);
                    if (expirationTime == null
                            || expirationTime.trim().length() == 0) {
                        expirationTime = "60";
                    }
                    Class<?>[] parameterTypes = new Class<?>[] {
                            Directory.class, double.class };
                    Object[] initArgs = new Object[] {
                            this.getIndexDirectory(),
                            new Double(expirationTime) };
                    policyObj = policyClass.getConstructor(parameterTypes)
                            .newInstance(initArgs);
                } else {
                    policyObj = policyClass.newInstance();
                }

                this.setIndexDelitionPolicy(policyObj);
            } catch (ClassNotFoundException e) {
                logger.warn(e);
            } catch (IllegalAccessException e) {
                logger.warn(e);
            } catch (InstantiationException e) {
                logger.warn(e);
            } catch (NoSuchMethodException e) {
                logger.warn(e);
            } catch (InvocationTargetException e) {
                logger.warn(e);
            }
        }
    }

    private long parseLong(String value, long defaultValue){
        long result = defaultValue;
        try {
            result = Long.parseLong(value);
        } catch ( Exception t ){
        }
        return result;
    }

    private int parseInt(String value, int defaultValue){
        int result = defaultValue;
        try {
            result = Integer.parseInt(value);
        } catch ( Exception t ){
        }
        return result;
    }

    public IndexDeletionPolicy getIndexDelitionPolicy() {
        return indexDelitionPolicy;
    }

    public void setIndexDelitionPolicy(IndexDeletionPolicy indexDelitionPolicy) {
        this.indexDelitionPolicy = indexDelitionPolicy;
    }

    public boolean isLuceneIndexerAutoCommit() {
        return luceneIndexerAutoCommit;
    }

    public void setLuceneIndexerAutoCommit(boolean luceneIndexerAutoCommit) {
        this.luceneIndexerAutoCommit = luceneIndexerAutoCommit;
    }
    
    /**
     * Internal indexer
     *
     */
    protected class Indexer {
        private int UNDEFINED = 0;
        private int ADD = 1;
        private int REMOVE = 2;

        private int maxDocs = 50;
        private List<IndexableDocument> docs = new ArrayList<IndexableDocument>();
        private int lastOperation = 0;
        public Indexer(int maxDocs) {
            this.maxDocs = maxDocs;
            this.docs = new ArrayList<IndexableDocument>();
        }

        public synchronized void addDocument(IndexableDocument doc)
                throws IOException, JahiaException {
            if (doc == null) {
                return;
            }
            int requestOp = !(doc instanceof RemovableDocument) ? ADD : REMOVE;
            if (this.getLastOperation() == UNDEFINED) {
                this.setLastOperation(requestOp);
                docs.add(doc);
            } else if (this.getLastOperation() != requestOp) {
                storeInPersistance();
                this.setLastOperation(requestOp);
                docs.add(doc);
            } else {
                docs.add(doc);
            }

            if (docs.size() > maxDocs) {
                storeInPersistance();
            }
        }

        public synchronized void storeInPersistance()
                throws IOException, JahiaException {

            if (docs.isEmpty()) {
                this.setLastOperation(UNDEFINED);
                return;
            }
            
            List<Document> luceneDocs = new ArrayList<Document>();

            for (IndexableDocument doc : docs) {
                Document luceneDoc = getLuceneDocument(doc);
                if (luceneDoc != null) {
                    luceneDocs.add(luceneDoc);
                }
            }

            if (this.getLastOperation() == ADD) {

                if (logger.isInfoEnabled()) {
                    //logger.info("Last Operation: ADD");
                }
                //long startTime2 = System.currentTimeMillis();

                /*
                RAMDirectory ramDir = new RAMDirectory();
                IndexWriter ramWriter = new IndexWriter(ramDir, analyzer, true);
                //ramWriter.mergeFactor = 50;
                ramWriter.setMergeFactor(luceneMergeFactor);
                //ramWriter.minMergeDocs = 1000;
                ramWriter.setMaxBufferedDocs(luceneMaxBufferedDocs);
                ramWriter.setUseCompoundFile(false);
                luceneDoc = null;
                size = luceneDocs.size();
                for (int i = 0; i < size; i++) {
                    luceneDoc = (Document) luceneDocs.get(i);
                    if (luceneDoc != null) {
                        ramWriter.addDocument(luceneDoc);
                    }
                }

                if (logger.isInfoEnabled()) {
                    //logger.info(
                    //    "Finished adding docs to ramWriter in "
                    //            + String.valueOf(System.currentTimeMillis() - startTime2) + "ms.");
                }

                //startTime2 = System.currentTimeMillis();

                luceneDocs = null;
                docs = null;

                ramWriter.close();
                */


                IndexWriter fsWriter = null;
                try {
                    fsWriter = getIndexWriter(defaultAnalyzer, false);
                    if (fsWriter == null) {
                        fsWriter = getIndexWriter(defaultAnalyzer, true);
                    }
                    if (fsWriter != null) {
                        for (Document luceneDoc : luceneDocs) {
                            if (luceneDoc != null) {
                                Field languageCodeField = luceneDoc.getField("jahia.language_code");
                                Analyzer languageAnalyzer = null;
                                if (languageCodeField != null) {
                                    String languageCode = languageCodeField.stringValue();
                                    int index = languageCode.indexOf('_');
                                    if (index != -1) {
                                        languageCode = languageCode.substring(0, index); 
                                    }
                                    languageAnalyzer = analyzers.get(languageCode);
                                    if (getAnalyzer().equals(languageAnalyzer)){
                                        languageAnalyzer = null;
                                    }
                                    
                                }
                                if (languageAnalyzer == null) {
                                    fsWriter.addDocument(luceneDoc);
                                } else {
                                    fsWriter.addDocument(luceneDoc, languageAnalyzer);                                    
                                }
                            }
                        }

                        // use user's preference
                        /*
                        if ( fsWriter.getMergeFactor() < 30 ){
                            fsWriter.setMergeFactor(30);
                        }
                        if ( fsWriter.getMaxBufferedDocs() < 1000 ){
                            fsWriter.setMaxBufferedDocs(1000);
                        }*/
                        // Is there any reason ? Thread.yield();
                        //fsWriter.addIndexes(new Directory[]{ramDir});
                    }
                } catch (Exception t) {
                    logger.debug("Error adding doc from index", t);
                } finally {
                    closeIndexWriter(fsWriter);
                    //ramDir = null;
                    //ramWriter = null;
                    fsWriter = null;
                }

                if (logger.isInfoEnabled()) {
                    //logger.info(
                    //    "Finished adding docs to fsWriter in "
                    //            + String.valueOf(System.currentTimeMillis() - startTime2) + "ms.");
                }

            } else if (this.getLastOperation() == REMOVE) {
                if (logger.isInfoEnabled()) {
                    //logger.info("Last Operation: REMOVE");
                }

                //long startTime2 = System.currentTimeMillis();

                IndexReader reader = getIndexReader();
                for (IndexableDocument doc : docs) {
                    backgroundRemoveObjectFromSearchEngine(doc, reader);
                }
                closeIndexReader(reader);
                reader = null;

                if (logger.isInfoEnabled()) {
                    //logger.info(
                    //    "Finished removing docs in "
                    //            + String.valueOf(System.currentTimeMillis() - startTime2) + "ms.");
                }

            }
            this.docs = new ArrayList<IndexableDocument>();
            this.setLastOperation(UNDEFINED);
            /*
            long indexingElapsedTime = System.currentTimeMillis() - startTime;
            if (logger.isInfoEnabled()) {
                logger.info(
                    "Finished processing " + nbDocs + " docs in " + indexingElapsedTime + "ms.");
            }*/
        }

        public int getMaxDocs() {
            return maxDocs;
        }

        public void setMaxDocs(int maxDocs) {
            this.maxDocs = maxDocs;
        }

        public synchronized int getNbDocs() {
            return docs.size();
        }

        public synchronized List<IndexableDocument> getDocs() {
            return docs;
        }

        public synchronized void setDocs(List<IndexableDocument> docs) {
            this.docs = docs;
        }

        public int getLastOperation() {
            return lastOperation;
        }

        public void setLastOperation(int lastOperation) {
            this.lastOperation = lastOperation;
        }
    }
}