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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 15 f�vr. 2005
 * Time: 12:26:04
 * To change this template use File | Settings | File Templates.
 */
public class SearchManager {

    private static org.apache.log4j.Logger logger =
                org.apache.log4j.Logger.getLogger(SearchManager.class);

    static public String DEFAULT_SEARCH_HANDLER_NAME = "default";

    static public String DEFAULT_SEARCH_HANDLER_CLASS
            = "org.jahia.services.search.lucene.fs.LuceneSearchHandlerImpl";

    private JahiaSearchService searchServ;

    private Properties searchConfig = new Properties();

    private Map<String, SearchHandler> searchHandlers = new HashMap<String, SearchHandler>();

    public SearchManager(Properties searchConfig, JahiaSearchService searchServ) throws Exception {
        if ( searchConfig != null ){
            this.searchConfig = searchConfig;
        }
        this.searchServ = searchServ;
        // instance the default search handler
        // this.createSearchHandler(DEFAULT_SEARCH_HANDLER_NAME, "Default Search Handler", this.searchConfig);
    }

    public Properties getSearchConfig(){
        return this.searchConfig;
    }

    public Map<String, SearchHandler> getSearchHandlers(){
        return this.searchHandlers;
    }

    /**
     * Create a search handler based on the default implementation if it doesn't actually exist.
     * if created, register it
     *
     * @param name
     * @param title
     * @param config settings passed to the searchHandler
     * @return
     * @throws Exception
     */
    public SearchHandler createSearchHandler(String name,
                                             String title,
                                             int siteId,
                                             Properties config)
    throws Exception {
        SearchHandler sh = createSearchHandler(name,title,siteId,null,config);
        if ( sh != null ){
            this.searchServ.addShutdownable(sh.getName(),sh);
        }
        return sh;
    }

    /**
     * Create a search handler based on the default implementation if it doesn't actually exist.
     * if created, register it
     *
     * @param name
     * @param title
     * @param className
     * @param config settings passed to the searchHandler
     * @return
     * @throws Exception
     */
    public SearchHandler createSearchHandler(String name,
                                             String title,
                                             int siteId,                                             
                                             String className,
                                             Properties config)
    throws Exception {
        synchronized(searchHandlers) {
            SearchHandler searchHandler = this.getSearchHandler(name);
            if ( searchHandler == null ){
                if ( className == null ){
                    className = this.getSearchConfig().getProperty("defaultSearchHandlerClass");
                    if ( className == null ){
                        className = DEFAULT_SEARCH_HANDLER_CLASS;
                    }
                }

                try {
                    Class<? extends SearchHandlerImpl> c = Class.forName(className).asSubclass(SearchHandlerImpl.class);
                    searchHandler = (SearchHandlerImpl)c.newInstance();
                    if ( searchHandler != null ){
                        searchHandler.setSiteId(siteId);
                        searchHandler.setName(name);
                        searchHandler.setTitle(title);
                        searchHandler.setSearchManager(this);
                        searchHandler.setConfig(config);
                        searchHandler.init();
                        this.registerSearchHandler(searchHandler);
                        this.searchServ.addShutdownable(searchHandler.getName(),searchHandler);
                    }
                } catch ( ClassNotFoundException cnfe ){
                    logger.warn(cnfe);
                } catch ( InstantiationException ie ){
                    logger.warn(ie);
                } catch ( IllegalAccessException iae ){
                    logger.warn(iae);
                }
            }
            return searchHandler;
        }
    }

    /**
     * Register a new search handler only if there isn't an existing one with this name in the registry.
     * It's name should not be null.
     *
     * @param searchHandler
     * @throws Exception thrown if trying to register a search handler which name already exists in the
     *                          registry.
     */
    public void registerSearchHandler(SearchHandler searchHandler)
    throws Exception {
        synchronized(this.searchHandlers) {
            if ( searchHandler != null
                && searchHandler.getName() != null ){

                if ( this.searchHandlers.containsKey(searchHandler.getName()) ){
                    throw new Exception("You cannot register a search handler with the name "
                            + searchHandler.getName()
                            + ", because there is already a registered one of same name" );
                }
                searchHandler.setSearchManager(this);
                this.searchHandlers.put(searchHandler.getName(),
                                            searchHandler);
            }
        }
    }

    /**
     * unregister a search handler from the registry
     *
     * @param name
     */
    public void unregisterHandler(String name){
        synchronized(this.searchHandlers){
            if ( name != null ){
                this.searchHandlers.remove(name);
            }
        }
    }

    /**
     * Returns a search handler given a name
     *
     * @param name
     * @return
     */
    public SearchHandler getSearchHandler(String name){
        synchronized(this.searchHandlers){
            return (SearchHandler)this.searchHandlers.get(name);
        }
    }

}
