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
