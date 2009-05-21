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
//
//  Search_Engine
//  DJ  20.02.2001
//
//  getInstance()
//  authoriseRender()
//  renderLink()
//  needsJahiaData()
//  handleActions()
//

package org.jahia.engines.search;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.jahia.content.ContentPageKey;
import org.jahia.data.JahiaData;
import org.jahia.data.search.JahiaSearchHit;
import org.jahia.data.search.JahiaSearchResult;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.engines.EngineToolBox;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.search.JahiaSearchConstant;
import org.jahia.services.search.NumberPadding;
import org.jahia.services.search.PageSearchResultBuilderImpl;
import org.jahia.services.sites.SiteLanguageSettings;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.syndication.opensearch.FeedWriter;
import org.jahia.utils.JahiaTools;

/**
 * This engine is called when the user wants to do a search on the jahia site. It displays
 * sorted results using a JSP output which leaves every aspect of the search results design on
 * the JSP coder.
 * <p/>
 * This engine doesn't implement any search algorithm, it calls the JahiaSearchService for that.
 * Instead, this engine is here for interaction between the user and the service.
 * <p/>
 * The JSP template uses searchResult attribute to get the search results, and searchString to
 * see what the user searched for.
 *
 * @author David Jilli
 * @see org.jahia.services.search.JahiaSearchService
 */
public class Search_Engine implements JahiaEngine {

    /** The engine's name. */
    public static final String ENGINE_NAME = "search";

    public static final String DEFAULT_CONFIG = "default";
    public static final String PARAM_SEARCH_RESULTS = "searchResults";
    public static final String SEARCH_ALL_LANG = "all_lang";
    public static final String SEARCH_VIEW = "searchView";
    public static final String SEARCH_VIEW_ADVANCED = "advSearch";
    public static final String SEARCH_VIEW_SIMPLE = "simple";
    public static final String SEARCH_HANDLER = "searchHandler";
    public static final String SEARCH_REFINE_ATTRIBUTE = "searchRefineAttribute";
    public static final String RESET_SEARCH_REFINE_ATTRIBUTE = "resetSearchRefineAttribute";
    public static final String SEARCH_REFINE_ATTRIBUTE_SEP = "@@@@";
    public static final String SEARCH_OPTIONS_HANDLER = "searchOptionsHandler";
    public static final String START_PAGE_NODE = "startPageNode";
    public static final String SEARCH_JAHIA_SITE = "searchJahiaSite";
    public static final String FEED_TYPE = "feedType";

    public static final String MAX_PAGE_ITEMS = "maxPageItems";

    public static final String ADV_SEARCH_LICENCE = "org.jahia.engines.search.advancedSearch";

    // search screen
    public static final String EXECUTE_SCREEN = "execute";
    public static final String NAVIGUATE_RESULT_SCREEN = "naviguate_result";
    public static final String SAVE_SEARCH_SCREEN = "save_search";
    public static final String UPDATE_SEARCH_OPTIONS_SCREEN = "update_search_options";
    public static final String UPDATE_CUSTOM_CRITERIAS_SCREEN = "update_criterias_options";

    // output
    private static final String TEMPLATE_JSP = "/engines/search/searchresult.jsp";
    private static final String SEARCH_JSP_NAME = "searchresult.jsp";
    private EngineToolBox toolBox;

    private static final transient Logger logger = Logger.getLogger(Search_Engine.class);

    private static Map<String, Class<? extends SearchViewHandler>> SEARCH_VIEW_HANDLERS = new HashMap<String, Class<? extends SearchViewHandler>>();

    /**
     * constructor
     */
    public Search_Engine () {
        toolBox = EngineToolBox.getInstance ();
    }

    /**
     * authorises engine render
     */
    public boolean authoriseRender (ProcessingContext jParams) {
        return true;					// always allowed to render search
    }


    /**
     * renders link to pop-up window
     */
    public String renderLink (ProcessingContext jParams, Object theObj)
            throws JahiaException {
        String theUrl = jParams.composeEngineUrl (ENGINE_NAME, EMPTY_STRING);
        if (theObj != null)
            theUrl = theUrl + theObj;
        return jParams.encodeURL (theUrl);
    }


    /**
     * specifies if the engine needs the JahiaData object
     */
    public boolean needsJahiaData (ProcessingContext processingContext) {
        return true;
    }


    /**
     * handles the engine actions
     *
     * @param jParams a ParamBean object
     * @param jData   a JahiaData object (not mandatory)
     */
    public EngineValidationHelper handleActions (ProcessingContext jParams, JahiaData jData)
            throws JahiaException {

        // init engineMap
        Map<String, Object> engineMap = initEngineMap(jParams, jData);
        processScreen (jParams, jData, engineMap);

        // displays the screen
        toolBox.displayScreen (jParams, engineMap);

        return null;
    }

    /**
     * Retrieve the engine name.
     *
     * @return the engine name.
     */
    public final String getName () {
        return ENGINE_NAME;
    }

    protected Map<String, Object> initEngineMap(ProcessingContext jParams, JahiaData jData)
            throws JahiaException {

        String theScreen = jParams.getParameter ("screen");
        Map<String, Object> engineMap = null;
        if (theScreen == null){
            if (jParams.getParameter(SAVE_SEARCH_SCREEN) != null ) {
                theScreen = SAVE_SEARCH_SCREEN;
            } else {
                theScreen = EXECUTE_SCREEN;
            }
        }
        engineMap = (Map<String, Object>) jParams.getSessionState()
                .getAttribute ("engineMap");
        if ( engineMap == null ){
            engineMap = new HashMap<String, Object>();
        }
        String searchJahiaSite = (String)engineMap.get(SEARCH_JAHIA_SITE);
        if ( !jParams.getSiteKey().equals(searchJahiaSite) ){
            engineMap = new HashMap<String, Object>();
        }
        engineMap.put("screen",theScreen);
        engineMap.put(SEARCH_JAHIA_SITE,jParams.getSiteKey());
        jParams.getSessionState().setAttribute("engineMap",engineMap);
        jParams.setAttribute("engineMap", engineMap);

        if ( EXECUTE_SCREEN.equals(theScreen) ){

            // update search view
            String searchView = jParams.getParameter(SEARCH_VIEW);
            if ( searchView == null) {
                searchView = (String)engineMap.get(SEARCH_VIEW);
            }
            if ( searchView == null ){
                searchView = SEARCH_VIEW_SIMPLE;
            }
            /*
            // check that search view has changed or not
            String oldSearchView = (String)engineMap.get(SEARCH_VIEW);
            if ( !searchView.equals(oldSearchView) ){
                engineMap = new HashMap();
                jParams.getSessionState().setAttribute("engineMap",engineMap);
            }*/

            engineMap.put (SEARCH_VIEW, searchView);
            engineMap.put("screen",theScreen);

            // update query
            String searchString = jParams.getParameter ("search");
            if (searchString == null) {
                searchString = (String) engineMap.get ("searchString");
            }

            if (searchString == null) {
                searchString = EMPTY_STRING;
            } else {
                searchString = searchString.trim ();
            }

            // it's for document search
            if (searchString != null && jParams.getParameter("src_terms[0].term")==null ){
                jParams.setParameter("src_terms[0].term",searchString);
                jParams.setParameter("src_terms[0].fields.all","true");
                jParams.setParameter("src_mode","files");
            }

            engineMap.put ("searchString", searchString);
            if (!searchString.equals("")) {
                jParams.setParameter("freeSearch", searchString);
            }
            // update search refine attributes
            List<String> searchRefineAttributes = (List<String>)engineMap.get(SEARCH_REFINE_ATTRIBUTE);
            if ( searchRefineAttributes == null ){
                searchRefineAttributes = new ArrayList<String>();
                engineMap.put(SEARCH_REFINE_ATTRIBUTE,searchRefineAttributes);
            }
            String value = jParams.getParameter (SEARCH_REFINE_ATTRIBUTE);
            if (value != null) {
                if ( "reset".equals(value) ){
                    searchRefineAttributes.clear();
                } else if ( value.toLowerCase().startsWith(RESET_SEARCH_REFINE_ATTRIBUTE+SEARCH_REFINE_ATTRIBUTE_SEP) ){
                    try {
                        searchRefineAttributes.remove(value.substring((RESET_SEARCH_REFINE_ATTRIBUTE+SEARCH_REFINE_ATTRIBUTE_SEP).length()));
                    } catch ( Exception t ){
                    }
                } else if ( !"".equals(value.trim()) ){
                    if (!searchRefineAttributes.contains(value)){
                        searchRefineAttributes.add(value);
                    }
                }
            }

            // update search lang
            List<String> languageCodes = new ArrayList<String>();
            String[] languageCodesVal = jParams.getParameterValues ("searchlang");
            boolean isAllLang = JahiaTools.inValues (Search_Engine.SEARCH_ALL_LANG, languageCodesVal);
            if ( languageCodesVal != null ){
                if (!isAllLang) {
                    for (int i = 0; i < languageCodesVal.length; i++) {
                        languageCodes.add (languageCodesVal[i]);
                    }
                }
            }
            if ( languageCodes.isEmpty() && !isAllLang ){
                languageCodes.add (jParams.getLocale ().toString ());
            } else if(isAllLang) {
                for (SiteLanguageSettings siteLangSetting : jParams.getSite().getLanguageSettings()) {
                    if (siteLangSetting.isActivated()){
                        languageCodes.add(siteLangSetting.getCode());
                    }
                }
            } 
            engineMap.put ("searchLanguageCodes", languageCodes);

        }

        // maxPageItems
        Integer maxPageItems = (Integer)engineMap.get(MAX_PAGE_ITEMS);
        String value = jParams.getParameter(MAX_PAGE_ITEMS);
        if ( value != null) {
            try {
                maxPageItems = new Integer(value);
            } catch ( Exception t){
            }
        }
        if ( maxPageItems == null ){
            maxPageItems = new Integer(10);
        }
        engineMap.put (MAX_PAGE_ITEMS, maxPageItems);

        // check if a searchresult template exists in the template
        // directory, else uses the standard template found in the
        // jsp/engines directory
        engineMap.put (ENGINE_OUTPUT_FILE_PARAM, getResultsTemplatePath(jParams));

        engineMap.put (RENDER_TYPE_PARAM, new Integer (JahiaEngine.RENDERTYPE_FORWARD));
        engineMap.put (ENGINE_NAME_PARAM, ENGINE_NAME);
        engineMap.put (ENGINE_URL_PARAM, jParams.composeEngineUrl (ENGINE_NAME, EMPTY_STRING));
        engineMap.put ("jahiaBuild", new Integer (jParams.settings ().getBuildNumber ()));
        engineMap.put ("javascriptUrl", jParams.settings ().getJsHttpPath ());

        return engineMap;

    }

    private String getResultsTemplatePath(ProcessingContext ctx) {
        String path = TEMPLATE_JSP;

        JahiaTemplateManagerService templateMgr = ServicesRegistry
                .getInstance().getJahiaTemplateManagerService();

        JahiaTemplatesPackage templatePackage = templateMgr
                .getTemplatePackage(ctx.getSite().getTemplatePackageName());

        if (templatePackage.getSearchResultsPageName() != null) {
            path = templateMgr.resolveResourcePath(templatePackage
                    .getSearchResultsPageName(), templatePackage.getName());
        } else {
            path = TEMPLATE_JSP;
            String fileName;

            if ((ctx != null) && (ctx.getPage() != null)
                    && (ctx.getPage().getPageTemplate() != null)) {
                fileName = ctx.getPage().getPageTemplate().getSourcePath();
                if (fileName.lastIndexOf("/") != -1) {
                    fileName = fileName.substring(0,
                            fileName.lastIndexOf("/") + 1)
                            + SEARCH_JSP_NAME;
                    path = fileName;
                }
            }
        }

        return path;
    }


    /**
     *
     * @param jParams
     * @param jData
     * @param engineMap
     * @throws JahiaException
     */
    public void processScreen (ProcessingContext jParams, JahiaData jData, Map<String, Object> engineMap)
            throws JahiaException {

        String theScreen = (String)engineMap.get("screen");
        String searchView = (String)engineMap.get(SEARCH_VIEW);

        // load from session
        SearchViewHandler searchViewHandler = (SearchViewHandler)engineMap.get(SEARCH_HANDLER);

//        boolean advSearchLicenseEnabled = LicenseActionChecker
//                .isAuthorizedByLicense(ADV_SEARCH_LICENCE, 0);

//        if ( SEARCH_VIEW_ADVANCED.equals(searchView) && advSearchLicenseEnabled ){

        // check if a new searchHandler is request
        String searchHandlerName = jParams.getParameter(SEARCH_HANDLER);
        if (searchHandlerName!=null){
            if ( searchViewHandler != null
                    && !searchViewHandler.getName().equals(searchHandlerName) ){
                // reset old search handler
                searchViewHandler = null;
                engineMap.remove(PARAM_SEARCH_RESULTS);
            }
        } else {
            if ( searchViewHandler != null ){
                searchHandlerName = searchViewHandler.getName();
            }
        }

        if ( searchHandlerName == null ){
            searchHandlerName = DEFAULT_CONFIG;
        }
        if ( searchViewHandler == null ){
            searchViewHandler = getSearchViewHandler(searchHandlerName);
        }
        if ( searchViewHandler != null ){
            engineMap.put(SEARCH_HANDLER,searchViewHandler);
        } else {
            // no available search view handler, stay in simple view
            searchView = SEARCH_VIEW_SIMPLE;
            engineMap.remove(SEARCH_HANDLER);
            engineMap.put(SEARCH_VIEW,searchView);
        }
//        } else {
        // ensure to reset any search view handler
//            engineMap.remove(SEARCH_HANDLER);
//        }

        JahiaSearchResult searchResults = (JahiaSearchResult)engineMap.get(PARAM_SEARCH_RESULTS);
//        if ( SEARCH_VIEW_ADVANCED.equals(searchView) && advSearchLicenseEnabled ){
        searchViewHandler.init(jParams, engineMap);
        searchViewHandler.update(jParams, engineMap);
        if ( !theScreen.equals(NAVIGUATE_RESULT_SCREEN) ) {
            searchResults = searchViewHandler.search(jParams);
        }
        if (searchResults == null) {
            PageSearchResultBuilderImpl resultBuilder =
                    new PageSearchResultBuilderImpl(false);
            searchResults = new JahiaSearchResult(resultBuilder);
        }
//        } else {
        // simple search
//            boolean oneHitPerPage =
//                    ("true".equals(jParams.
//                    getParameter(PageSearchResultBuilderImpl.ONLY_ONE_HIT_BY_PAGE))
//                    || jParams.
//                    getParameter(PageSearchResultBuilderImpl.ONLY_ONE_HIT_BY_PAGE)==null);
//            PageSearchResultBuilderImpl resultBuilder =
//                    new PageSearchResultBuilderImpl(oneHitPerPage);
//            String searchString = (String)engineMap.get("searchString");
//            List languageCodes = (List)engineMap.get("searchLanguageCodes");
//            if ( theScreen.equals(EXECUTE_SCREEN) || searchResults == null || !searchResults.isValid() ){
//                String refineSearchQuery = buildSearchRefineQuery(
//                        (List)engineMap.get(SEARCH_REFINE_ATTRIBUTE));
//
//                StringBuffer query = new StringBuffer("(").append(searchString).append(")");
//                if ( refineSearchQuery != null && !"".equals(refineSearchQuery.trim()) ){
//                    query.append(" AND ( ").append(refineSearchQuery).append(" )");
//                }
//                String startPageNodeQuery = buildStartPageNodeQuery(jParams.getParameter("startPageNode"),jParams);
//                if ( startPageNodeQuery != null && !"".equals(startPageNodeQuery.trim()) ){
//                    query.append(" AND (").append(startPageNodeQuery).append(") ");
//                }
//                searchResults = ServicesRegistry
//                    .getInstance ().getJahiaSearchService ()
//                    .search (jParams.getSiteID(), StringEscapeUtils.unescapeHtml(query.toString()), jParams, languageCodes, resultBuilder);
//                if (searchResults == null) {
//                    searchResults = new JahiaSearchResult(resultBuilder);
//                }
//            }
//        }

        // TODO check if we really need to store the results in the engine map == session scope
        engineMap.put (PARAM_SEARCH_RESULTS, searchResults);
        ((ParamBean) jParams).getRequest().setAttribute(PARAM_SEARCH_RESULTS,
                searchResults);
        String feedType = jParams.getParameter(FEED_TYPE);
        if ( feedType != null && ( feedType.startsWith("rss")
                || feedType.startsWith("atom")) ) {
            try {
                String searchString = (String)engineMap.get("searchString");
                engineMap.put (RENDER_TYPE_PARAM, new Integer (-1));
                writeSyndFeed(feedType,(ParamBean)jParams,searchResults,
                        StringEscapeUtils.unescapeHtml(searchString),engineMap);
            } catch ( Throwable t ){
                throw new JahiaException("Error writing search result as Feed",
                        "Error writing search result as Feed",JahiaException.APPLICATION_ERROR,
                        JahiaException.ERROR_SEVERITY);
            }
        }
    }

    /**
     * Returns an implementation fo the search view handler by its name.
     *
     * @param name
     *            the search view handler name
     * @return an implementation fo the search view handler by its name
     */
    protected static SearchViewHandler getSearchViewHandler(String name) {
        if (name == null) {
            throw new IllegalArgumentException(
                    "Specified search view handler name is 'null'");
        }

        SearchViewHandler searchViewHandler = null;
        try {
            searchViewHandler = (SearchViewHandler) getSearchViewHandlerClass(
                    name).newInstance();
            searchViewHandler.setName(name);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Unable to create instance of search view handler class '"
                            + name
                            + "'. Check its value or the applicationcontext-basejahiaconfig.xml configuration.",
                    e);
        }

        return searchViewHandler;
    }

    private static Class<? extends SearchViewHandler> getSearchViewHandlerClass(String name) {
        Class<? extends SearchViewHandler> clazz = SEARCH_VIEW_HANDLERS.get(name);
        if (clazz == null) {
            synchronized (Search_Engine.class) {
                if (clazz == null) {
                    Properties settings = (Properties) SpringContextSingleton
                            .getInstance().getContext().getBean("searchConfig");
                    if (settings != null) {
                        String className = settings
                                .getProperty("org.jahia.engines.search.searchViewHandler."
                                        + name);
                        if (className == null) {
                            className = name;
                        }
                        try {
                            clazz = Class.forName(className).asSubclass(SearchViewHandler.class);
                        } catch (ClassNotFoundException e) {
                            throw new IllegalArgumentException(
                                    "Unable to find an implemenation for the "
                                            + " requested search view handler '"
                                            + name
                                            + "'. Check its value or the applicationcontext-basejahiaconfig.xml configuration.",
                                    e);
                        }
                        SEARCH_VIEW_HANDLERS.put(name, clazz);
                    }
                }
            }
        }

        return clazz;
    }

    /**
     * Returns a refine query string for the given list of refine search
     * attributes
     *
     * @param refineSearchAttribute
     * @return
     */
    public static String buildSearchRefineQuery(List<String> refineSearchAttribute){
        StringBuffer query = new StringBuffer();
        if ( refineSearchAttribute == null || refineSearchAttribute.isEmpty() ){
            return "";
        }
        String[] tokens = null;
        for ( String attr : refineSearchAttribute){
            try {
                tokens = JahiaTools.getTokens(attr,SEARCH_REFINE_ATTRIBUTE_SEP);
                if ( tokens.length >= 2 ){
                    if ( query.length() > 0 ){
                        query.append(" AND ");
                    }
                    query.append(tokens[0]).append(":").append(NumberPadding.pad(tokens[1]));
                } else {
                    logger.debug("Wrong search refine attribute format " + attr);
                }
            } catch ( Exception t ){
                logger.debug("Error parsing search refine attribute " + attr ,t);
            }
        }
        return query.toString();
    }

    // start page node
    /**
     * Returns a page path search scope based on the start page node
     *
     * @param startPageNode
     * @param startPageNode
     * @param context
     * @return
     */
    public static String buildStartPageNodeQuery(String startPageNode, ProcessingContext context){
        StringBuffer buffer = new StringBuffer();
        if ( startPageNode != null && !"".equals(startPageNode.trim()) ){
            try {
                ContentPage page = (ContentPage)ContentPage
                        .getContentObjectInstance(ContentPageKey.getInstance(startPageNode));
                if (page !=null && context != null){
                    String pagePath = page.getPagePathString(context);
                    if (pagePath!=null && !"".equals(pagePath.trim())){
                        pagePath +="*";
                        buffer.append(JahiaSearchConstant.METADATA_PAGE_PATH).append(":").append(pagePath);
                    }
                }
            } catch ( Exception t){
                logger.debug("Exception occured retrieving search start page node",t);
            }
        }
        return buffer.toString();
    }

    public static String buildSearchRefineAttribute( String attrName, String attrNameLabel,
                                                     String attrValue, String attrValueLabel){
        if ( attrName == null || attrValue == null || "".equals(attrName.trim()) || "".equals(attrValue.trim()) ){
            return "";
        }
        if ( attrNameLabel == null || "".equals(attrNameLabel.trim()) ){
            attrNameLabel = attrName;
        }
        if ( attrValueLabel == null || "".equals(attrValueLabel.trim()) ){
            attrValueLabel = attrValue;
        }
        StringBuffer buff = new StringBuffer(attrName);
        buff.append(Search_Engine.SEARCH_REFINE_ATTRIBUTE_SEP).append(attrValue)
                .append(Search_Engine.SEARCH_REFINE_ATTRIBUTE_SEP).append(attrNameLabel)
                .append(Search_Engine.SEARCH_REFINE_ATTRIBUTE_SEP).append(attrValueLabel);
        return buff.toString();

    }

    /**
     * Performs the search using specified criteria and returns the list of
     * search hits as view objects for being displayed in a template.
     *
     * @param criteria
     *            the search criteria
     * @param ctx
     *            current processing context
     * @return the list of search hits as view objects for being displayed in a
     *         template; if nothing is found, returns an empty list
     */
    public static List<Hit> search(SearchCriteria criteria,
                                   ProcessingContext ctx) {

        List<Hit> hits = Collections.emptyList();

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Performing search using criteria: " + criteria);
            }
            hits = getViewObjectList(Search_Engine.getSearchViewHandler(
                    criteria.getModeAutodetect().toString().toLowerCase())
                    .search(ctx), ctx);
            if (logger.isDebugEnabled()) {
                logger.info(hits.size() + " hits found");
            }
        } catch (JahiaException e) {
            logger.error("Error performing search using criteria: " + criteria,
                    e);
        }

        return hits;
    }

    /**
     * Creates a list of view objects from the specified Jahia search result
     * object.
     *
     * @param srcResult
     *            the Jahia search results object
     * @param ctx
     *            current processing context
     */
    private static List<Hit> getViewObjectList(JahiaSearchResult srcResult,
                                               ProcessingContext ctx) {

        List<Hit> hits = Collections.emptyList();

        if (srcResult != null && srcResult.results().size() > 0) {
            hits = new LinkedList<Hit>();
            for (JahiaSearchHit jahiaHit : srcResult.results()) {
                Hit hit = null;
                switch (jahiaHit.getType()) {
                    case JahiaSearchHit.WEBDAVFILE_TYPE:
                        hit = new FileHit(jahiaHit, ctx);
                        break;
                        
                    case JahiaSearchHit.FILE_TYPE:
                        hit = new PageHit(jahiaHit, ctx);
                        if (jahiaHit.getObject() != null) {
                            hit = new ReferencedFileHit(jahiaHit, ctx, hit);
                        }
                        break;                        

                    case JahiaSearchHit.PAGE_TYPE:
                        hit = new PageHit(jahiaHit, ctx);
                        break;

                    default:
                        hit = new PageHit(jahiaHit, ctx);
                        break;
                }

                if (hit != null) {
                    hits.add(hit);
                }
            }
        }

        return hits;
    }

    /**
     * Output results as RSS or ATOM directly to the output
     *
     * @param feedType
     * @param jParams
     * @param searchResult
     * @param engineMap
     * @throws IOException
     */
    protected void writeSyndFeed(String feedType,
                                 ParamBean jParams,
                                 JahiaSearchResult searchResult, String searchString,
                                 Map<String, Object> engineMap) throws IOException {

        FeedWriter feedWriter = new FeedWriter();
        HttpServletResponse resp = jParams.getRealResponse();
        OutputStream outputStream = resp.getOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(outputStream);
        feedWriter.write(feedType,jParams,searchResult,searchString,writer);
    }
}
