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
