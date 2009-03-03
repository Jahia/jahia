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

 package org.jahia.engines.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.services.search.savedsearch.JahiaSavedSearch;
import org.jahia.utils.JahiaTools;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 28 f�vr. 2006
 * Time: 17:02:09
 * To change this template use File | Settings | File Templates.
 */
public class SearchOptionsHandler {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (SearchOptionsHandler.class);

    // search operation
    public static final String MSG = "searchOptionsMsgs";
    public static final String OPERATION_PARAM = "operation";
    public static final String EDIT_SEARCH_OPERATION = "editSearch";
    public static final String SAVE_SEARCH_OPERATION = "saveSearch";
    public static final String DELETE_SEARCH_OPERATION = "deleteSearch";
    public static final String SELECT_SAVE_SEARCH_OPERATION = "selectSaveSearch";

    public static final String OPERATION_VIEW = "SearchOptionsHandler.view";

    public static final String SAVED_SEARCH = "savedSearch";
    public static final String SAVED_SEARCHES = "savedSearches";

    public static final String NAME_REQUIRED = "org.jahia.engines.search.savesearch.nameRequired";
    public static final String DELETED_SUCCESSFULLY = "org.jahia.engines.search.savesearch.deletedSuccessfully";
    public static final String SAVED_SUCCESSFULLY = "org.jahia.engines.search.savesearch.savedSuccessfully";

    public static final String ERROR_CODE = "100";

    private JahiaSavedSearch savedSearch;
    private SearchViewHandler searchViewHandler;
    private List<JahiaSavedSearch> savedSearches;
    private String operation;
    private boolean mySavedSearchesOnly = false;


    public SearchOptionsHandler(SearchViewHandler searchViewHandler){
        this.searchViewHandler = searchViewHandler;
    }

    /**
     *
     * @param jParams
     * @param engineMap
     * @throws JahiaException
     */
    public void handleActions (ProcessingContext jParams, Map<String, Object> engineMap)
            throws JahiaException {

        String theScreen = (String)engineMap.get("screen");
        operation = JahiaTools.getStrParameter(jParams,OPERATION_PARAM,"");
        engineMap.put(OPERATION_VIEW,"edit");

        if ( Search_Engine.UPDATE_SEARCH_OPTIONS_SCREEN.equals(theScreen) ){
            String value = jParams.getParameter("mySavedSearchesOnly");
            mySavedSearchesOnly = ( value != null && "true".equals(value) );
        }
        engineMap.put("mySavedSearchesOnly",Boolean.valueOf(mySavedSearchesOnly));
        engineMap.remove(MSG);
        if ( EDIT_SEARCH_OPERATION.equals(operation) ) {
            handleDisplaySearch(jParams,engineMap);
        } else if ( SAVE_SEARCH_OPERATION.equals(operation) ){
            handleSaveSearch(jParams,engineMap);
        } else if ( SELECT_SAVE_SEARCH_OPERATION.equals(operation) ){
            handleSelectSavedSearch(jParams,engineMap);
        } else if ( DELETE_SEARCH_OPERATION.equals(operation) ){
            handleDeleteSavedSearch(jParams,engineMap);
        }

        savedSearches = this.searchViewHandler.getSavedSearches();
        if ( savedSearches == null ){
            savedSearches = new ArrayList<JahiaSavedSearch>();
        }
        engineMap.put(SearchOptionsHandler.SAVED_SEARCHES,savedSearches);
        if ( savedSearch != null ){
            engineMap.put(SearchOptionsHandler.SAVED_SEARCH,savedSearch);
        } else {
            engineMap.remove(SearchOptionsHandler.SAVED_SEARCH);
        }
    }

    /**
     *
     * @param jParams
     * @param engineMap
     * @throws JahiaException
     */
    public void handleFormSubmit (ProcessingContext jParams, Map<String, Object> engineMap)
            throws JahiaException {

        if (savedSearch != null){
            String value = jParams.getParameter("searchName");
            if ( value != null ){
                savedSearch.setTitle(value.trim());
            }
            value = jParams.getParameter("searchDescr");
            if ( value != null ){
                savedSearch.setDescr(value.trim());
            }
        }
    }

    /**
     *
     * @param jParams
     * @param engineMap
     * @throws JahiaException
     */
    public void handleDisplaySearch (ProcessingContext jParams, Map<String, Object> engineMap)
            throws JahiaException {

        if (savedSearch == null){
            String searchDoc = this.searchViewHandler.getSaveSearchDoc(jParams);
            // save a new Search
            savedSearch = new JahiaSavedSearch(new Integer(-1),"","",
                    searchDoc,new Long(System.currentTimeMillis()),
                    jParams.getUser().getUserKey(),
                    searchViewHandler.getClass().getName(),jParams.getSiteID(),null);
        }
        handleFormSubmit(jParams, engineMap);
    }

    /**
     *
     * @param jParams
     * @param engineMap
     * @throws JahiaException
     */
    public void handleSaveSearch (ProcessingContext jParams, Map<String, Object> engineMap)
            throws JahiaException {
        if (savedSearch == null){
            String searchDoc = this.searchViewHandler.getSaveSearchDoc(jParams);
            // save a new Search
            savedSearch = new JahiaSavedSearch(new Integer(-1),"","",
                    searchDoc,new Long(System.currentTimeMillis()),
                    jParams.getUser().getUserKey(),
                    searchViewHandler.getClass().getName(),jParams.getSiteID(),null);
        }        
        if (savedSearch != null){
            handleFormSubmit(jParams,engineMap);
            if ( savedSearch.getAcl() != null ){
                String value = jParams.getParameter("isPublic");
                if ( "true".equals(value) ){
                    savedSearch.allowGuest(true);
                } else if ( "false".equals(value)) {
                    savedSearch.allowGuest(false);
                }
            }
            if ( "".equals(savedSearch.getTitle()) ){
                this.addMsg(jParams,engineMap, JahiaResourceBundle.getJahiaInternalResource(NAME_REQUIRED,jParams.getLocale(),"Name required"));
            } else {
                try {
                    ServicesRegistry.getInstance().getJahiaSearchService()
                            .saveSearch(savedSearch,searchViewHandler,jParams);
                    this.addMsg(jParams,engineMap,JahiaResourceBundle.getJahiaInternalResource(SAVED_SUCCESSFULLY,jParams.getLocale(),"Saved successfully"));
                } catch ( Exception t ){
                    logger.debug("An error occured when saving the search",t);
                    this.addMsg(jParams,engineMap,"An error occured when saving the search");
                }
            }
        }
    }

    /**
     *
     * @param jParams
     * @param engineMap
     * @throws JahiaException
     */
    public void handleDeleteSavedSearch (ProcessingContext jParams, Map<String, Object> engineMap)
            throws JahiaException {
        if (savedSearch != null){
            handleFormSubmit(jParams,engineMap);
            try {
                ServicesRegistry.getInstance().getJahiaSearchService()
                        .deleteSearch(savedSearch.getId());
                this.savedSearch = null;
                this.addMsg(jParams,engineMap,JahiaResourceBundle.getJahiaInternalResource(DELETED_SUCCESSFULLY,jParams.getLocale(),"Deleted successfully"));
                engineMap.put(OPERATION_VIEW,"delete_success");
            } catch ( Exception t ){
                logger.debug("An error occured when deleging the search",t);
                this.addMsg(jParams,engineMap,"An error occured when deleting the search");
            }
        }
    }

    /**
     *
     * @param jParams
     * @param engineMap
     * @throws JahiaException
     */
    public void handleSelectSavedSearch (ProcessingContext jParams, Map<String, Object> engineMap)
            throws JahiaException {
        String value = jParams.getParameter("savedSearches");
        if ( value != null && value.length() > 0){
            try {
                JahiaSavedSearch savedSearch = ServicesRegistry.getInstance()
                        .getJahiaSearchService().getSavedSearch(value);
                if ( savedSearch != null ){
                    this.savedSearch = savedSearch;
                    this.searchViewHandler.useSavedSearch(jParams,this.savedSearch);
                }
            } catch ( Exception t ){
            }
        } else if ( value != null && "0".equals(value) ){
            // unselect
            this.savedSearch = null;
        }
    }

    public JahiaSavedSearch getSavedSearch() {
        return savedSearch;
    }

    public void setSavedSearch(JahiaSavedSearch savedSearch) {
        this.savedSearch = savedSearch;
    }

    public SearchViewHandler getSearchViewHandler() {
        return searchViewHandler;
    }

    public void setSearchViewHandler(SearchViewHandler searchViewHandler) {
        this.searchViewHandler = searchViewHandler;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    private void addMsg(ProcessingContext jParams,
                        Map<String, Object> engineMap,
                        String msg){
        if ( msg == null ){
            return;
        }
        List<String> l = (List<String>)engineMap.get(MSG);
        if ( l == null ){
            l = new ArrayList<String>();
            engineMap.put(MSG,l);
        }
        l.add(msg);
    }
}
