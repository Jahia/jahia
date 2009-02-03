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

import org.dom4j.Element;
import org.jahia.bin.Jahia;
import org.jahia.content.ContentPageKey;
import org.jahia.data.search.JahiaSearchResult;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.search.JahiaSearchBaseService;
import org.jahia.services.search.JahiaSearchConstant;
import org.jahia.services.search.NumberPadding;
import org.jahia.services.search.PageSearchResultBuilderImpl;
import org.jahia.services.search.SearchHandler;
import org.jahia.services.search.savedsearch.JahiaSavedSearch;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 8 f?vr. 2005
 * Time: 15:02:10
 * To change this template use File | Settings | File Templates.
 */
public class WebSiteSearchDelegate extends AdvSearchDelegate {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (WebSiteSearchDelegate.class);

    final public String SAVE_SEARCH_HANDLER = "jahia.saveSearchHandler";

    public static final String SEARCH_DOMAIN    = "searchDomain";
    public static final String UPDATE_DATE      = "updateDate";
    public static final String OCCURENCE        = "occurence";
    public static final String CONTENT_TYPE     = "contentType";
    public static final String START_PAGE_NODE = "startPageNode";

    private List<String> domains = new ArrayList<String>();

    private String updateDate;
    private String occurence;
    private String contentType;
    private WebdavSearchDelegate webdavSearch;
    private String startPageNode;

    public WebSiteSearchDelegate(AdvSearchViewHandler searchViewHandler){
        super(searchViewHandler);
        webdavSearch = new WebdavSearchDelegate(searchViewHandler);
    }

    public void init(ProcessingContext jParams, Map<String, Object> engineMap)
    throws JahiaException {

        super.init(jParams,engineMap);

        if ( !this.getSearchViewHandler().isSearchModeChanged() ){
            String[] values = jParams.getParameterValues(WebSiteSearchDelegate.SEARCH_DOMAIN);
            if ( values != null ){
                this.domains = new ArrayList<String>();
                for ( int i=0 ;i<values.length; i++ ){
                    this.domains.add(values[i]);
                }
            }

            // max update date range
            String value  = jParams.getParameter("updateDate");
            if ( value != null ){
                updateDate = value;
            }

            // occurence
            value  = jParams.getParameter("occurence");
            if ( value != null ){
                occurence = value;
            }

            // contentType
            value  = jParams.getParameter("contentType");
            if ( value != null ){
                contentType = value;
            }

            // start page node
            value  = jParams.getParameter("startPageNode");
            if ( value != null ){
                startPageNode = value;
            }
        }

        if ( updateDate == null || "".equals(updateDate.trim())){
            updateDate = "anytime";
        }

        if ( occurence == null || "".equals(occurence.trim())){
            occurence = JahiaSearchConstant.ALL_FULLTEXT_SEARCH_FIELD;
        }

        if ( contentType == null || "".equals(contentType.trim())){
            contentType = "any";
        }
        buildWebSiteQuery();
        webdavSearch.init(jParams, engineMap);
    }

    /**
     * handles search operations like search option update
     *
     * @param jParams
     * @param engineMap
     */
    public void update(ProcessingContext jParams, Map<String, Object> engineMap)
    throws JahiaException{
        super.update(jParams,engineMap);
        buildWebSiteQuery();
        webdavSearch.update(jParams, engineMap);
    }

    public void buildWebSiteQuery(){
        String query = this.getQuery();
        String advOptions = this.buildAdvancedOptions();
        if ( "".equals(query) ) {
            query = advOptions;
        } else if ( advOptions.length()>0 ){
            query = "(" + query + ") AND (" + advOptions + ")";
        }
        if ( !"".equals(occurence) && !"".equals(query) ){
            query = occurence + ":(" + query + ")";
        }
        this.setQuery(query);
    }

    public JahiaSearchResult search(ProcessingContext jParams) throws JahiaException {

        Map<String, Object> engineMap = (Map<String, Object>)jParams.getAttribute("engineMap");
        String theScreen = (String)engineMap.get("screen");

        if (  !Search_Engine.NAVIGUATE_RESULT_SCREEN.equals(theScreen) ){

            if (this.getResultBuilder() == null) {
                boolean oneHitPerPage =
                        ("true".equals(jParams.
                        getParameter(PageSearchResultBuilderImpl.ONLY_ONE_HIT_BY_PAGE)));
                this.setResultBuilder(new PageSearchResultBuilderImpl(oneHitPerPage));
            }
            List<String> ar = new ArrayList<String>();
            if ( this.domains.contains("anywhere") ){
                Map<String, SearchHandler> searchHandlers = ServicesRegistry.getInstance()
                        .getJahiaSearchService().getSearchManager().getSearchHandlers();
                for ( String name : searchHandlers.keySet() ){
                    if ( !name.equals(JahiaSearchBaseService.WEBDAV_SEARCH) ){
                        ar.add(name);
                    }
                }
            } else if ( this.domains.size()==0 ){
                SearchHandler searchHandler = ServicesRegistry.getInstance()
                        .getJahiaSearchService().getSearchHandler(jParams.getSiteID());
                if ( searchHandler != null ){
                    ar.add(searchHandler.getName());
                    this.domains.add(searchHandler.getName());
                }
            } else {
                ar.addAll(domains);
            }
            String[] searchHandlers = new String[ar.size()];
            ar.toArray(searchHandlers);

            String refineSearchQuery = Search_Engine.buildSearchRefineQuery((List<String>)engineMap.get(Search_Engine.SEARCH_REFINE_ATTRIBUTE));
            StringBuffer queryBuffer = new StringBuffer("(").append(this.getQuery()).append(")");
            if ( refineSearchQuery != null && !"".equals(refineSearchQuery.trim()) ){
                queryBuffer.append(" AND ( ").append(refineSearchQuery).append(" )");
            }
            this.searchResult = ServicesRegistry
                .getInstance ().getJahiaSearchService ()
                .search(searchHandlers, queryBuffer.toString(), webdavSearch.getQuery(),
                        jParams, this.getLanguageCodes(),
                        this.getResultBuilder());
        }
        if (this.searchResult == null) {
            this.searchResult = new JahiaSearchResult(new
                PageSearchResultBuilderImpl());
        }
        return this.searchResult;
    }

    public List<String> getDomains() {
        return domains;
    }

    public void setDomains(List<String> domains) {
        this.domains = domains;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    public String getOccurence() {
        return occurence;
    }

    public void setOccurence(String occurence) {
        this.occurence = occurence;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getStartPageNode() {
        return startPageNode;
    }

    public void setStartPageNode(String startPageNode) {
        this.startPageNode = startPageNode;
    }

    private Date getUpdateDateRange(String range){
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        cal.setTime(new Date());

        if ("today".equals(range)){
            cal.set( Calendar.HOUR_OF_DAY, 0 );
            cal.set( Calendar.MINUTE, 0);
            cal.set( Calendar.SECOND, -1);
        } else if ("lastWeek".equals(range)) {
            cal.set( Calendar.WEEK_OF_MONTH, -1);
        } else if ("3m".equals(range)) {
            cal.set( Calendar.MONTH, -3);
        } else if ("6m".equals(range)) {
            cal.set( Calendar.MONTH, -6);
        } else if ("year".equals(range)) {
            cal.set( Calendar.YEAR, -1);
        }
        return cal.getTime();
    }

    protected String buildAdvancedOptions(){

        StringBuffer advOptions = new StringBuffer();

        if ( !"anytime".equals(updateDate) ){
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(TimeZone.getTimeZone("UTC"));
            cal.setTime(new Date());

            Date maxUpdateDate = getUpdateDateRange(updateDate);
            advOptions.append("(");
            advOptions.append(JahiaSearchConstant.METADATA_PREFIX+"lastmodified:[");
            advOptions.append(NumberPadding.pad(maxUpdateDate.getTime()));
            advOptions.append(" TO ");
            advOptions.append(NumberPadding.pad(cal.getTime().getTime()));
            advOptions.append("])");
        }

        // contentType
        if ( !"any".equals(contentType)	){
            if ( advOptions.length()>0 ){
                advOptions.append(" AND (");
            } else {
                advOptions.append("(");
            }
            if ( "pdf".equals(contentType) ){
                advOptions.append(JahiaSearchConstant.FILE_CONTENT_TYPE+":");
                advOptions.append("application/pdf");
            } else if ( "doc".equals(contentType) ){
                advOptions.append(JahiaSearchConstant.FILE_CONTENT_TYPE+":");
                advOptions.append("application/msword");
            } else if ( "xls".equals(contentType) ){
                advOptions.append(JahiaSearchConstant.FILE_CONTENT_TYPE+":");
                advOptions.append("application/vnd.ms-excel");
            } else if ( "ppt".equals(contentType) ){
                advOptions.append(JahiaSearchConstant.FILE_CONTENT_TYPE+":");
                advOptions.append("application/vnd.ms-powerpoint");
            }
            advOptions.append(")");
        }

        // start page node
        if ( startPageNode != null && !"".equals(startPageNode.trim()) ){
            try {
                ContentPage page = (ContentPage)ContentPage
                    .getContentObjectInstance(ContentPageKey.getInstance(startPageNode));
                if (page !=null && Jahia.getThreadParamBean() != null){
                    String pagePath = page.getPagePathString(Jahia.getThreadParamBean());
                    if (pagePath!=null && !"".equals(pagePath.trim())){
                        pagePath +="*";
                        if ( advOptions.length()>0 ){
                            advOptions.append(" AND ");
                        } else {
                            advOptions.append(" ");
                        }
                        advOptions.append(JahiaSearchConstant.PAGE_PATH).append(":")
                                .append(pagePath).append(" ");
                    }
                }
            } catch ( Exception t){
                logger.debug("Exception occured retrieving search start page node",t);
            }
        }
        return advOptions.toString();
    }

    public void appendSaveSearchDoc(ProcessingContext jParams, Element el)
    throws JahiaException {

        if ( domains != null && domains.size()>0 ){
            Element domainsElement = el.addElement(SEARCH_DOMAIN);
            Iterator<String> iterator = domains.iterator();
            while ( iterator.hasNext() ){
                domainsElement.addElement(AdvSearchViewHandler.VALUE_ELEMENT)
                        .addText(iterator.next());
            }
        }
        if ( this.occurence != null && !"".equals(this.occurence.trim()) ){
            el.addElement(OCCURENCE).addText(this.occurence);
        }
        if ( this.updateDate != null && !"".equals(this.updateDate.trim()) ){
            el.addElement(UPDATE_DATE).addText(this.updateDate);
        }
        if ( this.contentType != null && !"".equals(this.contentType.trim()) ){
            el.addElement(CONTENT_TYPE).addText(this.contentType);
        }
        if ( this.startPageNode != null && !"".equals(this.startPageNode.trim()) ){
            el.addElement(START_PAGE_NODE).addText(this.startPageNode);
        }

    }

    public void useSavedSearch(ProcessingContext jParams,
                               JahiaSavedSearch savedSearch) {
        super.useSavedSearch(jParams, savedSearch);
        webdavSearch.useSavedSearch(jParams, savedSearch);
    }


    protected void useSaveSearchDoc(ProcessingContext jParams, Element root)
    throws JahiaException {
        try
        {
            if (root != null)
            {
                Element el = root.element(SEARCH_DOMAIN);
                if ( el != null ){
                    List<Element> els = el.elements();
                    if ( els != null ){
                        this.domains.clear();
                        for ( Element domainEl : els ){
                            this.domains.add(domainEl.getText());
                        }
                    }
                }
                Element occurenceEl = root.element(OCCURENCE);
                if ( occurenceEl != null ){
                    this.occurence = occurenceEl.getText();
                }
                Element updateDateEl = root.element(UPDATE_DATE);
                if ( updateDateEl != null ){
                    this.updateDate = updateDateEl.getText();
                }
                Element contentTypeEl = root.element(CONTENT_TYPE);
                if ( contentTypeEl != null ){
                    this.contentType = contentTypeEl.getText();
                }
                Element startPageNode = root.element(START_PAGE_NODE);
                if ( startPageNode != null ){
                    this.startPageNode = contentTypeEl.getText();
                }
            }
        } catch ( Exception t ){
            logger.debug("Error paring value from JahiaSavedSearch Xml", t);
        }
    }
}
