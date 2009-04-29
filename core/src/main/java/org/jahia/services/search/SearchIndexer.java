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
 package org.jahia.services.search;

import java.util.List;


/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 15 f�vr. 2005
 * Time: 13:05:57
 * To change this template use File | Settings | File Templates.
 */
public interface SearchIndexer {

    /**
     * Add a document in the index
     *
     * @param document
     */
    public abstract void addDocument(IndexableDocument document);

    /**
     * Remove a document from the index
     *
     * @param document
     */
    public abstract void removeDocument(RemovableDocument document);

    /**
     * batch removing of RemovableDocument from toRemoveList and then adding IndexableDocuments of toAdd
     * @param toRemove
     * @param toAdd
     */
    public abstract void batchIndexing(List<RemovableDocument> toRemove, List<IndexableDocument> toAdd);

    /**
     * syncrhonized batch removing of RemovableDocument from toRemoveList and then adding IndexableDocuments of toAdd
     * @param toRemove
     * @param toAdd
     */
    public abstract void synchronizedBatchIndexing(List<RemovableDocument> toRemove, List<IndexableDocument> toAdd);

    /**
     * Set the search handler owner of this indexer
     *
     * @param searchHandler
     */
    public abstract void setSearchHandler(SearchHandler searchHandler);

    /**
     * Returns the search handler owner of this indexer
     *
     * @return
     */
    public abstract SearchHandler getSearchHandler();

    /**
     * Returns the number of documents buffered in indexation queue
     *
     * @return
     */
    public abstract int getBufferedDocs();

    /**
     * Notify the indexer
     *
     * @return
     */
    public abstract void wakeUp();
    

    /**
     * Start the indexer
     *
     * @throws Exception
     */
    public abstract void start() throws Exception;

    public abstract void shutdown();

}
