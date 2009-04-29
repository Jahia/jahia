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

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 15 f�vr. 2005
 * Time: 13:05:57
 * To change this template use File | Settings | File Templates.
 */
public abstract class SearchIndexerImpl {

    private SearchHandler searchHandler;

    public SearchIndexerImpl(){
    }

    public SearchIndexerImpl(SearchHandler searchHandler){
        this.searchHandler = searchHandler;
    }

    /**
     * Sub classes, should call notifyEvent after adding the document
     *
     * @param document
     */
    public abstract void addDocument(IndexableDocument document);

    /**
     * Sub classes, should call notifyEvent after removing the document
     *
     * @param document
     */
    public abstract void removeDocument(RemovableDocument document);

    public void setSearchHandler(SearchHandlerImpl searchHandlerImpl){
        this.searchHandler = searchHandlerImpl;
    }

    public SearchHandler getSearchHandler(){
        return this.searchHandler;
    }

    /**
     * Should be called by addDocument and removeDocument methods
     * to notify listeners
     *
     * @param ev
     * @param methodName
     * @throws Exception
     */
    public void notifyEvent(SearchEvent ev, String methodName) throws Exception {
        SearchHandler searchHandler = this.getSearchHandler();
        if ( searchHandler != null ){
            searchHandler.notify(ev, methodName);
        }
    }

}
