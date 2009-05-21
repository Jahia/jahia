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
 package org.jahia.services.search;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 15 f�vr. 2005
 * Time: 15:33:30
 * To change this template use File | Settings | File Templates.
 */
public abstract class SearchHandlerImpl extends SearchHandler {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(SearchHandlerImpl.class);

    public static String ALL_SEARCH_FIELD_NAME = "value";

    private SearchManager searchManager;

    private int siteId;    
    
    private String name;

    private String allSearchFieldName = ALL_SEARCH_FIELD_NAME;

    // a more human readable name
    private String title;

    private Map<String, SearchEventListener> listeners = new HashMap<String, SearchEventListener>();

    private Properties config = new Properties();

    private boolean readOnly;

    public SearchHandlerImpl(){
    }

    public SearchHandlerImpl(Properties config){
        if ( config != null ){
            this.config = config;
        }
    }

    public SearchHandlerImpl(SearchManager searchManager, Properties config){
        this(config);
        this.searchManager = searchManager;
    }

    /**
     * This method should be called, once the config and searchManager are set to allow
     * internal initialisation.
     *
     * @throws Exception
     */
    public abstract void init() throws Exception;

    public String getAllSearchFieldName() {
        return allSearchFieldName;
    }

    public void setAllSearchFieldName(String allSearchFieldName) {
        this.allSearchFieldName = allSearchFieldName;
    }

    public boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public Properties getConfig(){
        return this.config;
    }

    public void setConfig(Properties config){
        this.config = config;
        if ( this.config == null){
            this.config = new Properties();
        }
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public abstract SearchResult search(String query);

    public abstract SearchIndexer getIndexer();

    public SearchManager getSearchManager(){
        return this.searchManager;
    }

    public void setSearchManager(SearchManager searchManager){
        this.searchManager = searchManager;
    }

    public void registerListerer(String name,
                                 SearchEventListener listener){
        synchronized(listeners){
            if ( name != null && listener != null
                && !this.listeners.containsKey(name) ){
                this.listeners.put(name,listener);
            }
        }
    }

    public void unregisterListerer(String name){
        if ( name == null ){
            return;
        }
        synchronized(listeners){
            this.listeners.remove(name);
        }
    }

    /**
     *
     * @param ev
     * @param methodName a SearchListener method name
     */
    public void notify(SearchEvent ev, String methodName) {
        if ( ev == null || methodName == null ){
            return;
        }

        try {

            for (SearchEventListener listener : getAllListeners()) {
                Class<? extends SearchEventListener> theClass = listener.getClass();
                Class<? extends SearchEvent> eventClass = ev.getClass();
                Method theMethod = theClass.getMethod(methodName,
                        new Class[] { eventClass });
                if (theMethod != null) {
                    theMethod.invoke(listener, new Object[] { ev });
                }
            }

        } catch (NoSuchMethodException nsme) {
            String errorMsg =
                "NoSuchMethodException when trying to execute method " +
                methodName + "(" + nsme.getMessage() + ")";
            logger.error( errorMsg, nsme);
            //throw new Exception("Listener notification error",nsme);
        } catch (InvocationTargetException ite) {
            String errorMsg =
                "InvocationTargetException when trying to execute method " +
                methodName + "(" + ite.getTargetException().getMessage() + ")";
            logger.error( errorMsg, ite.getTargetException());
            //throw new Exception("Listener notification error",ite);
        } catch (IllegalAccessException iae) {
            String errorMsg =
                "IllegalAccessException when trying to execute method " +
                methodName + "(" + iae.getMessage() + ")";
            logger.error( errorMsg, iae);
            //throw new Exception("Listener notification error",iae);
        }
    }

    /**
     * Adding doc to default search handler registered as DEFAULT_SEARCH_HANDLER_NAME
     *
     * @param doc
     */
    public void addDocument(IndexableDocument doc){
        if ( doc == null ){
            return;
        }
        SearchIndexer indexer = getIndexer();
        if ( indexer != null ){
            indexer.addDocument(doc);
        }
    }

    /**
     * Removing doc from default search handler registered as DEFAULT_SEARCH_HANDLER_NAME
     *
     * @param doc
     */
    public void removeDocument(RemovableDocument doc){
        SearchIndexer indexer = getIndexer();
        if ( indexer != null ){
            indexer.removeDocument(doc);
        }
    }

    public void batchIndexing(List<RemovableDocument> toRemove, List<IndexableDocument> toAdd){
        SearchIndexer indexer = getIndexer();
        if ( indexer != null ){
            indexer.batchIndexing(toRemove, toAdd);
        }
    }

    public void synchronizedBatchIndexing(List<RemovableDocument> toRemove, List<IndexableDocument> toAdd){
        SearchIndexer indexer = getIndexer();
        if ( indexer != null ){
            indexer.synchronizedBatchIndexing(toRemove, toAdd);
        }
    }

    /***
     * returns all listeners in registry
     *
     * @return
     */
    public List<SearchEventListener> getAllListeners () {
        List<SearchEventListener> v = new ArrayList<SearchEventListener>();
        synchronized(listeners){
            for (SearchEventListener listener : listeners.values()) {
                if ( listener != null ){
                    v.add(listener);
                }
            }
        }
        return v;
    }

    public int getSiteId() {
        return siteId;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }

}
