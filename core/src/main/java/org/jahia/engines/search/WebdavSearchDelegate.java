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
 package org.jahia.engines.search;

import org.apache.webdav.lib.PropertyName;
import org.apache.webdav.lib.search.CompareOperator;
import org.apache.webdav.lib.search.SearchRequest;
import org.apache.webdav.lib.search.SearchScope;
import org.apache.webdav.lib.search.SearchExpression;
import org.apache.webdav.lib.search.expressions.*;
import org.apache.commons.lang.StringEscapeUtils;
import org.dom4j.Element;
import org.dom4j.Document;
import org.jahia.bin.Jahia;
import org.jahia.data.search.JahiaSearchResult;
import org.jahia.engines.calendar.CalendarHandler;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.query.dasl.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.search.WebDavSearchResultBuilderImpl;
import org.jahia.utils.JahiaTools;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 8 f?vr. 2005
 * Time: 15:02:10
 * To change this template use File | Settings | File Templates.
 */
public class WebdavSearchDelegate extends AdvSearchDelegate {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (WebdavSearchDelegate.class);

    final public String SAVE_SEARCH_HANDLER = "jahia.saveSearchHandler";

    public static final String UPDATE_DATE      = "updateDate";
    public static final String OCCURENCE        = "occurence";
    public static final String CONTENT_TYPE     = "contentType";

    public static final String SEARCH_IN_CONTENT_AND_FILENAME = "searchInContentAndFileName";
    public static final int CONTENT_AND_FILENAME = 1;
    public static final int CONTENT_ONLY = 2;
    public static final int FILENAME_ONLY = 3;
    public static final String RESOURCE_TYPE = "resourceType";
    public static final int FILE_RESOURCE_TYPE = 1;
    public static final int COLLECTION_RESOURCE_TYPE = 2;
    public static final int ALL_RESOURCE_TYPE = 3;
    public static final String DOCUMENT_AUTHOR  = "documentAuthor";
    public static final String DOCUMENT_TITLE   = "documentTitle";
    public static final String DOCUMENT_SUBJECT = "documentSubject";
    public static final String LOCATION = "webdavLocation";
    public static final String CATEGORIES         = "categories";
    public static final String SUB_CATEGORIES_ENABLED = "subCategoriesEnabled";
    public static final String FOLDER_PATHES      = "folderPathes";
    public static final String FOLDER_PATH_VALUES = "folderPathValues";
    public static final String FOLDER_PATH_VALUE = "folderPathValue";
    public static final String FOLDER_PATH_ALIAS = "folderPathAlias";
    public static final String FOLDER_PATH_SUBFOLDERS = "folderPathSubFolders";
    public static final String CREATION_DATE    = "creationDate";
    public static final String CREATION_SINCE_DATE = "creationSinceDate";
    public static final String FROM_DATE        = "fromDate";
    public static final String TO_DATE          = "toDate";
    public static final String MODIFICATION_DATE = "modificationDate";
    public static final String MODIFICATION_SINCE_DATE = "modificationSinceDate";
    public static final String EXPAND_MORE_OPTIONS = "expandMoreOptions";
    public static final String EXPAND_CUSTOM_OPTIONS = "expandCustomOptions";

    public static final String OPERATION = "operation";
    public static final String SEARCH_OPERATION = "search";
    public static final String SELECT_CATEGORY_OPERATION = "selectCategory";

    public static final String SORT_BY = "sortBy";
    public static final String SORT_ORDER = "sortOrder";

    public static final String CRITERIAS = "criterias";
    public static final String CRITERIA = "criteria";

    public static final String ALLPROP = "allprop";
    public static final String AND = "and";
    public static final String ASCENDING = "ascending";
    public static final String BASICSEARCH = "basicsearch";
    public static final String CASESENSITIVE = "casesensitive";
    public static final String CONTAINS = "contains";
    public static final String DEPTH = "depth";
    public static final String DESCENDING = "descending";
    public static final String EQ = "eq";
    public static final String EXCLUDE = "exclude";
    public static final String EXCLUDE_LASTPATHSEGEMENT = "exclude-lastpathsegment";
    public static final String FROM = "from";
    public static final String GT = "gt";
    public static final String GTE = "gte";
    public static final String HREF = "href";
    public static final String INFINITY = "infinity";
    public static final String INCLUDE_LASTPATHSEGEMENT = "include-lastpathsegment";
    public static final String ISCOLLECTION = "is-collection";
    public static final String ISDEFINED = "is-defined";
    public static final String ISPRINCIPAL = "is-principal";
    public static final String LIKE = "like";
    public static final String LIMIT = "limit";
    public static final String LITERAL = "literal";
    public static final String LT = "lt";
    public static final String LTE = "lte";
    public static final String NOT = "not";
    public static final String NOT_CONTAINS = "not-contains";
    public static final String NOT_EQ = "not-eq";
    public static final String NOT_GT = "not-gt";
    public static final String NOT_GTE = "not-gte";
    public static final String NOT_ISCOLLECTION = "not-is-collection";
    public static final String NOT_ISDEFINED = "not-is-defined";
    public static final String NOT_ISPRINCIPAL = "not-is-principal";
    public static final String NOT_LIKE = "not-like";
    public static final String NOT_LT = "not-lt";
    public static final String NOT_LTE = "not-lte";
    public static final String NOT_PROPCONTAINS = "not-propcontains";
    public static final String NRESULTS = "nresults";
    public static final String OR = "or";
    public static final String ORDER = "order";
    public static final String ORDERBY = "orderby";
    public static final String PROP = "prop";
    public static final String PROPCONTAINS = "propcontains";
    public static final String RESOURCETYPE = "resourcetype";
    public static final String RESULTSET = "resultset";
    public static final String SCOPE = "scope";
    public static final String SELECT = "select";
    public static final String WHERE = "where";

    public static final int TRUE = 1;
    public static final int FALSE = 0;
    public static final int UNKNOWN = -1;

    private String contentType;
    private int resourceType = FILE_RESOURCE_TYPE;
    private String author;
    private String title;
//    private String subject;
    private List<String> webdavLocations = new ArrayList<String>();
//    private List folderPathes = new ArrayList();
//    private Map categories = new HashMap();

//    private List subCategoriesEnabled = new ArrayList();
//    private Map folderPathValues = new HashMap();
//    private List folderPathAlias = new ArrayList();
//    private List folderPathSubFolders = new ArrayList();

    private String createdSinceDate = "";
    private CalendarHandler createdFromCal = null;
    private CalendarHandler createdToCal = null;

    private String modifiedSinceDate = "";
    private CalendarHandler modifiedFromCal = null;
    private CalendarHandler modifiedToCal = null;

    private boolean expandMoreOptions = false;
    private boolean expandCustomOptions = false;
    private int searchInContentAndFilename = CONTENT_AND_FILENAME ;

    private String operation = SEARCH_OPERATION;
    private String selectedCategoryProp = "";

    // very specific for GED demo
//    private String archivedStatus = "open";

    private String accessPermission = "read";

    private String sortBy = "";
    private String sortOrder = "asc";

    protected WebdavCriteriasHandler criteriasHandler;

    static public String[] comparators = {  CONTAINS,EQ,GT,GTE, LIKE,LT, LTE,NOT_CONTAINS,
                                            NOT_EQ,NOT_GT,NOT_GTE,NOT_LIKE, NOT_LT,NOT_LTE
                                            };
    static public List<String> comparatorsList = Arrays.asList(comparators);

    public WebdavSearchDelegate(AdvSearchViewHandler searchViewHandler){
        super(searchViewHandler);
        this.criteriasHandler = new WebdavCriteriasHandler();
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getSelectedCategoryProp() {
        return selectedCategoryProp;
    }

    public void setSelectedCategoryProp(String selectedCategoryProp) {
        this.selectedCategoryProp = selectedCategoryProp;
    }


    public void init(ProcessingContext jParams, Map<String, Object> engineMap)
    throws JahiaException {

        super.init(jParams, engineMap);

        if ( createdFromCal == null ){
            createdFromCal = getCalHandler("createdFromCal",jParams);
        }
        if ( createdToCal == null ){
            createdToCal = getCalHandler("createdToCal",jParams);
        }
        if ( modifiedFromCal == null ){
            modifiedFromCal = getCalHandler("modifiedFromCal",jParams);
        }
        if ( modifiedToCal == null ){
            modifiedToCal = getCalHandler("modifiedToCal",jParams);
        }

        if ( !this.getSearchViewHandler().isSearchModeChanged() ){

            String value = jParams.getParameter(OPERATION);
            if ( value != null && !"".equals(value.trim()) ){
                this.operation = value;
            }
            if ( "".equals(operation.trim()) ){
                this.operation = SEARCH_OPERATION;
            }
            value = jParams.getParameter("selectedCategoryProp");
            if ( value != null ){
                this.selectedCategoryProp = value;
            }

            // contentType
            value = jParams.getParameter(CONTENT_TYPE);
            if ( value != null ){
                contentType = value;
            }

            value = jParams.getParameter(RESOURCE_TYPE);
            if ( value != null ){
                try {
                    resourceType = Integer.parseInt(value);
                } catch ( Exception t ){
                    resourceType = FILE_RESOURCE_TYPE;
                }
            }

            value = jParams.getParameter(DOCUMENT_AUTHOR);
            if ( value != null ){
                author = StringEscapeUtils.escapeHtml(value);
            }

            value = jParams.getParameter(DOCUMENT_TITLE);
            if ( value != null ){
                title = value;
            }
//            value = jParams.getParameter(DOCUMENT_SUBJECT);
//            if ( value != null ){
//                subject = StringEscapeUtils.escapeHtml(value);
//            }
            // remaining metadata : comments / keywords
            value = jParams.getParameter(SEARCH_IN_CONTENT_AND_FILENAME);
            this.setSearchInContentAndFilename(value);

            value = jParams.getParameter(EXPAND_MORE_OPTIONS);
            if ( value != null ){
                this.setExpandMoreOptions("1".equals(value));
            }

            value = jParams.getParameter(EXPAND_CUSTOM_OPTIONS);
            if ( value != null ){
                this.setExpandCustomOptions("1".equals(value));
            }

            value = jParams.getParameter(EXPAND_CUSTOM_OPTIONS);
            if ( value != null ){
                this.setExpandCustomOptions("1".equals(value));
            }

            // very specific to GED demo
//            value = jParams.getParameter("archivedStatus");
//            if ( value != null ){
//                archivedStatus = value;
//            }

            // very specific to GED demo
            value = jParams.getParameter("accessPermission");
            if ( value != null ){
                accessPermission = value;
            }

            value = jParams.getParameter("sortBy");
            if ( value != null ){
                sortBy = value;
            }

            value = jParams.getParameter("sortOrder");
            if ( value != null ){
                sortOrder = value;
            }

//            // detect that the form is submitted
//            boolean webdavSearchFormSubmit = false;

            String[] values = jParams.getParameterValues(LOCATION);
            if ( values != null ){
                this.webdavLocations.clear();
                for ( int i=0; i<values.length; i++ ){
                    value = values[i].trim();
                    if ( !"".equals(value) ){
                        this.webdavLocations.add(StringEscapeUtils.escapeHtml(value));
                    }
                }
            }

            if ( contentType == null || "".equals(contentType.trim())){
                contentType = "any";
            }

            createdFromCal.update(jParams);
            createdToCal.update(jParams);
            modifiedFromCal.update(jParams);
            modifiedToCal.update(jParams);

            value = jParams.getParameter("creationSinceDate");
            if ( value != null ){
                this.createdSinceDate = StringEscapeUtils.escapeHtml(value);
            }

            value = jParams.getParameter("modificationSinceDate");
            if ( value != null ){
                this.modifiedSinceDate = StringEscapeUtils.escapeHtml(value);
            }

        }

        buildWebdavQuery(jParams);
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
        this.criteriasHandler.handleActions(jParams,engineMap);
        buildWebdavQuery(jParams);
    }

    public void buildWebdavQuery(ProcessingContext jParams){
        query = getQuery();
        if (query == null || query.length()==0){
            return;
        }
        String type;
        switch(resourceType) {
            case FILE_RESOURCE_TYPE:
                type="nt:file";
                break;
            case COLLECTION_RESOURCE_TYPE:
                type="nt:folder";
                break;
            default:
                type="nt:hierarchyNode";
        }
        String sql = "select * from "+type + " where";

        String where = "";
        for (Iterator<String> iterator = webdavLocations.iterator(); iterator.hasNext();) {
            String s = iterator.next();
            where += "jcr:path like '"+ s.substring(s.indexOf("/default")+"/default".length())+ "%'";
            if (iterator.hasNext()) {
                where += " or";
            }
        }
        if (where.length()>0) {
            sql += " ("+where+") and";
        }

        String contains;
        if (searchInContentAndFilename == CONTENT_AND_FILENAME) {
            contains = " (CONTAINS(jcr:content,'"+query+"') or jcr:path like '%"+query+"%')";
        } else if (searchInContentAndFilename == CONTENT_ONLY) {
            contains = " CONTAINS(jcr:content,'"+query+"')";
        } else {
            contains = " jcr:path like '%\"+query+\"%'";
        }
        sql += contains;
//        query = sql;

        String xpath = "//element(*,"+type+")";

        if (searchInContentAndFilename == CONTENT_AND_FILENAME || searchInContentAndFilename == FILENAME_ONLY) {
            xpath += "[jcr:contains(jcr:content,'"+query+"')]";
        }

        query = xpath;
    }

    /**
     * <D:select>...</D:select> clause
     *
     * @param searchRequest
     * @param jParams
     */
    public void addSelections(SearchRequest searchRequest, ProcessingContext jParams){
        // @todo: make customizable
        searchRequest.addSelection(new PropertyName(Constants.DAV,"allprop"));
    }


    /**
     * <D:from>...</D:from> clause
     *
     * @param jParams
     */
    public List<SearchScope> getScopes(ProcessingContext jParams){
        List<SearchScope> scopes = new ArrayList<SearchScope>();
        if ( this.webdavLocations.size() == 0 ){
            scopes.add(new SearchScope("shared"));
            scopes.add(new SearchScope("users"));
            scopes.add(new SearchScope("groups"));
        } else {
            String context = Jahia.getContextPath() + "/webdav";
            Iterator<String> iterator = this.webdavLocations.iterator();
            String location = null;
            while ( iterator.hasNext() ){
                location = iterator.next();
                if ( location.startsWith(context) ){
                    location = location.substring(context.length(),location.length());
                    if ( location.startsWith("/site/") ){
                        location = location.substring("/site/".length(),location.length());
                        location = location.substring(location.indexOf("/")+1,location.length());
                    }
                    SearchScope scope = new SearchScope(location);
                    scopes.add(scope);
                }
            }
        }
        return scopes;
    }

    /**
     *
     * @param searchRequest
     * @param jParams
     */
    public boolean addContentTypeClause(SearchRequest searchRequest, ProcessingContext jParams){

        CompareExpression exp = null;
        PropertyName propName = new PropertyName(Constants.DAV,"getcontenttype");
        String value = null;
        if ( !"any".equals(contentType)	&& (
                "pdf".equals(contentType)
                        || "doc".equals(contentType)
                        || "xls".equals(contentType)
                        || "ppt".equals(contentType) ) ){
            if ( "pdf".equals(contentType) ){
                value  = "application/pdf";
            } else if ( "doc".equals(contentType) ){
                value = "application/msword";
            } else if ( "xls".equals(contentType) ){
                value = "application/vnd.ms-excel";
            } else if ( "ppt".equals(contentType) ){
                value = "application/vnd.ms-powerpoint";
            }
        }
        if ( value != null ){
            exp = new CompareExpression(CompareOperator.EQ,propName,value);
            ((AndExpression)searchRequest.getWhereExpression()).add(exp);
            return true;
        }
        return false;
    }

    /**
     *
     * @param searchRequest
     * @param jParams
     */
//    public boolean addResourceTypeClause(SearchRequest searchRequest, ProcessingContext jParams){
//
//        PropertyName propName = new PropertyName(Constants.DAV, NodeRevisionDescriptor.RESOURCE_TYPE);
//        CompareExpression exp = new CompareExpression(CompareOperator.EQ,propName,NodeRevisionDescriptor.COLLECTION_TYPE);
//        if ( this.resourceType == FILE_RESOURCE_TYPE ){
//            ((AndExpression)searchRequest.getWhereExpression()).add(searchRequest.not(exp));
//            return true;
//        } else if ( this.resourceType == COLLECTION_RESOURCE_TYPE ){
//            ((AndExpression)searchRequest.getWhereExpression()).add(exp);
//            return true;
//        }
//        return false;
//    }

    /**
     *
     * @param searchRequest
     * @param jParams
     */
    public boolean addAuthorClause(SearchRequest searchRequest, ProcessingContext jParams){

        CompareExpression exp = null;
        PropertyName propName = new PropertyName(Constants.JAHIA_SLIDE,"Author");
        if ( author != null && !"".equals(author.trim()) ){
            exp = new CompareExpression(CompareOperator.LIKE,propName,"%"+StringEscapeUtils.unescapeHtml(author.toLowerCase())+"%");
            ((AndExpression)searchRequest.getWhereExpression()).add(exp);
            return true;
        }
        return false;
    }

    /**
     *
     * @param searchRequest
     * @param jParams
     */
    public boolean addTitleClause(SearchRequest searchRequest, ProcessingContext jParams){

        CompareExpression exp = null;
        PropertyName propName = new PropertyName(Constants.JAHIA_SLIDE,"Title");
        if ( title != null && !"".equals(title.trim()) ){
            exp = new CompareExpression(CompareOperator.LIKE,propName,"%"+StringEscapeUtils.unescapeHtml(title.toLowerCase())+"%");
            ((AndExpression)searchRequest.getWhereExpression()).add(exp);
            return true;
        }
        return false;
    }

//    /**
//     *
//     * @param searchRequest
//     * @param jParams
//     */
//    public boolean addSubjectClause(SearchRequest searchRequest, ProcessingContext jParams){
//
//        CompareExpression exp = null;
//        PropertyName propName = new PropertyName(Constants.JAHIA_SLIDE,"Subject");
//        if ( subject != null && !"".equals(subject.trim()) ){
//            exp = new CompareExpression(CompareOperator.LIKE,propName,"%"+StringEscapeUtils.unescapeHtml(subject.toLowerCase())+"%");
//            ((AndExpression)searchRequest.getWhereExpression()).add(exp);
//            return true;
//        }
//        return false;
//    }

    /**
     *
     * @param searchRequest
     * @param jParams
     */
    public boolean addCreatedSinceClause(SearchRequest searchRequest, ProcessingContext jParams){

        CompareExpression exp = null;
        PropertyName propName = new PropertyName(Constants.JAHIA_SLIDE,"CreationDate");

        SimpleDateFormat dateFormat = new SimpleDateFormat(
                CalendarHandler.DEFAULT_DATE_FORMAT, jParams.getLocale());
        Date date = null;
        if ( createdSinceDate != null
                && !"".equals(createdSinceDate.trim()) ){
            date = getSinceDate(createdSinceDate);
            if ( date != null ){
                exp = new CompareExpression(CompareOperator.GTE,propName,dateFormat.format(date));
                ((AndExpression)searchRequest.getWhereExpression()).add(exp);
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param searchRequest
     * @param jParams
     */
    public boolean addModifiedSinceClause(SearchRequest searchRequest, ProcessingContext jParams){

        CompareExpression exp = null;
        PropertyName propName = new PropertyName(Constants.JAHIA_SLIDE,"LastModifDate");

        SimpleDateFormat dateFormat = new SimpleDateFormat(
                CalendarHandler.DEFAULT_DATE_FORMAT, jParams.getLocale());
        Date date = null;
        if ( this.modifiedSinceDate != null
                && !"".equals(this.modifiedSinceDate.trim()) ){
            date = getSinceDate(this.modifiedSinceDate);
            if ( date != null ){
                exp = new CompareExpression(CompareOperator.GTE,propName,dateFormat.format(date));
                ((AndExpression)searchRequest.getWhereExpression()).add(exp);
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param searchRequest
     * @param jParams
     */
    public boolean addCreatedFromClause(SearchRequest searchRequest, ProcessingContext jParams){

        CompareExpression exp = null;
        PropertyName propName = new PropertyName(Constants.JAHIA_SLIDE,"CreationDate");

        SimpleDateFormat dateFormat = new SimpleDateFormat(
                CalendarHandler.DEFAULT_DATE_FORMAT, jParams.getLocale());
        Date date = null;
        if ( createdFromCal != null
                && createdFromCal.getDateLong().longValue() !=0 ){
            date = new Date(createdFromCal.getDateLong().longValue() + createdFromCal.getTimeZoneOffSet().longValue()
                + createdFromCal.getServerClientTimeDiff().longValue());

            exp = new CompareExpression(CompareOperator.GTE,propName,dateFormat.format(date));
            ((AndExpression)searchRequest.getWhereExpression()).add(exp);
            return true;
        }
        return false;
    }

    /**
     *
     * @param searchRequest
     * @param jParams
     */
    public boolean addCreatedToClause(SearchRequest searchRequest, ProcessingContext jParams){

        CompareExpression exp = null;
        PropertyName propName = new PropertyName(Constants.JAHIA_SLIDE,"CreationDate");

        SimpleDateFormat dateFormat = new SimpleDateFormat(
                CalendarHandler.DEFAULT_DATE_FORMAT, jParams.getLocale());
        Date date = null;
        if ( createdToCal != null
                && createdToCal.getDateLong().longValue() !=0 ){
            date = new Date(createdToCal.getDateLong().longValue() + createdToCal.getTimeZoneOffSet().longValue()
                + createdToCal.getServerClientTimeDiff().longValue());

            exp = new CompareExpression(CompareOperator.LTE,propName,dateFormat.format(date));
            ((AndExpression)searchRequest.getWhereExpression()).add(exp);
            return true;
        }
        return false;
    }

    /**
     *
     * @param searchRequest
     * @param jParams
     */
    public boolean addModifiedFromClause(SearchRequest searchRequest, ProcessingContext jParams){

        CompareExpression exp = null;
        PropertyName propName = new PropertyName(Constants.JAHIA_SLIDE,"LastModifDate");

        SimpleDateFormat dateFormat = new SimpleDateFormat(
                CalendarHandler.DEFAULT_DATE_FORMAT, jParams.getLocale());
        Date date = null;
        if ( modifiedFromCal != null
                && modifiedFromCal.getDateLong().longValue() !=0 ){
            date = new Date(modifiedFromCal.getDateLong().longValue() + modifiedFromCal.getTimeZoneOffSet().longValue()
                + modifiedFromCal.getServerClientTimeDiff().longValue());

            exp = new CompareExpression(CompareOperator.GTE,propName,dateFormat.format(date));
            ((AndExpression)searchRequest.getWhereExpression()).add(exp);
            return true;
        }
        return false;
    }

    /**
     *
     * @param searchRequest
     * @param jParams
     */
    public boolean addModifiedToClause(SearchRequest searchRequest, ProcessingContext jParams){

        CompareExpression exp = null;
        PropertyName propName = new PropertyName(Constants.JAHIA_SLIDE,"LastModifDate");

        SimpleDateFormat dateFormat = new SimpleDateFormat(
                CalendarHandler.DEFAULT_DATE_FORMAT, jParams.getLocale());
        Date date = null;
        if ( modifiedToCal != null
                && modifiedToCal.getDateLong().longValue() !=0 ){
            date = new Date(modifiedToCal.getDateLong().longValue() + modifiedToCal.getTimeZoneOffSet().longValue()
                + modifiedToCal.getServerClientTimeDiff().longValue());

            exp = new CompareExpression(CompareOperator.LTE,propName,dateFormat.format(date));
            ((AndExpression)searchRequest.getWhereExpression()).add(exp);
            return true;
        }
        return false;
    }

    /**
     *
     * @param searchRequest
     * @param jParams
     */
    public boolean addContainsClause(SearchRequest searchRequest, ProcessingContext jParams){

        AndExpression contentExp = new AndExpression();
        AndExpression filenameExp = new AndExpression();
        boolean clauseAdded = false;
        // free search
        if ( this.getFreeSearch() != null && !"".equals(this.getFreeSearch().trim()) ){
            contentExp.add(this.getContainsClause(StringEscapeUtils.unescapeHtml(this.getFreeSearch()),searchRequest,jParams));
            filenameExp.add(this.getDisplayNameClause(StringEscapeUtils.unescapeHtml(this.getFreeSearch()),searchRequest,jParams));
        }
        // exact phrase
        if ( this.getExactPhrase() != null && !"".equals(this.getExactPhrase().trim())){
            contentExp.add(this.getContainsClause(StringEscapeUtils.unescapeHtml(this.getExactPhrase().trim()),searchRequest,jParams));
            filenameExp.add(this.getDisplayNameClause(StringEscapeUtils.unescapeHtml(this.getExactPhrase().trim()),searchRequest,jParams));
        }

        // all words
        if ( this.getAllWord() != null && !"".equals(this.getAllWord().trim())){
            String [] allWords = JahiaTools.getTokens(StringEscapeUtils.unescapeHtml(this.getAllWord().trim())," ");
            String word = null;
            for ( int i=0; i<allWords.length; i++ ){
                word = allWords[i];
                if ( !"".equals(word) ){
                    contentExp.add(this.getContainsClause(word,searchRequest,jParams));
                    filenameExp.add(this.getDisplayNameClause(word,searchRequest,jParams));
                }
            }
        }
        // without words
        if ( this.getWithoutWord() != null && !"".equals(this.getWithoutWord().trim())){
            String [] words = JahiaTools.getTokens(StringEscapeUtils.unescapeHtml(this.getWithoutWord().trim())," ");
            String word = null;
            for ( int i=0; i<words.length; i++ ){
                word = words[i];
                if ( !"".equals(word) ){
                    contentExp.add(new NotExpression(this.getContainsClause(word,searchRequest,jParams)));
                    filenameExp.add(new NotExpression(this.getDisplayNameClause(word,searchRequest,jParams)));
                }
            }
        }

        // one of word
        if ( this.getOneOfWord() != null && !"".equals(this.getOneOfWord().trim())){
            String [] words = JahiaTools.getTokens(StringEscapeUtils.unescapeHtml(this.getOneOfWord().trim())," ");
            String word = null;
            OrExpression contentOrExp = new OrExpression();
            OrExpression filenameOrExp = new OrExpression();
            for ( int i=0; i<words.length; i++ ){
                word = words[i];
                if ( !"".equals(word) ){
                    contentOrExp.add(this.getContainsClause(word,searchRequest,jParams));
                    filenameOrExp.add(this.getDisplayNameClause(word,searchRequest,jParams));
                }
            }
            contentExp.add(contentOrExp);
            filenameExp.add(filenameOrExp);
        }
        if ( this.searchInContentAndFilename == CONTENT_ONLY ){
            if ( contentExp.getExpressions().hasNext() ){
                ((AndExpression)searchRequest.getWhereExpression()).add(contentExp);
                clauseAdded = true;
            }
        } else if ( this.searchInContentAndFilename == FILENAME_ONLY ) {
            if ( filenameExp.getExpressions().hasNext() ){
                ((AndExpression)searchRequest.getWhereExpression()).add(filenameExp);
                clauseAdded = true;
            }
        } else {
            OrExpression orExp = new OrExpression();
            if ( contentExp.getExpressions().hasNext() ){
                orExp.add(contentExp);
            }
            if ( filenameExp.getExpressions().hasNext() ){
                orExp.add(filenameExp);
            }
            if ( orExp.getExpressions().hasNext() ){
                ((AndExpression)searchRequest.getWhereExpression()).add(orExp);
                clauseAdded = true;
            }
        }

        return clauseAdded;
    }

    public ContainsExpression getContainsClause(String query, SearchRequest searchRequest,
                                                ProcessingContext jParams){
        return new ContainsExpression(query);
    }

    public SearchExpression getDisplayNameClause(String query, SearchRequest searchRequest,
                                                  ProcessingContext jParams){

        OrExpression filenameOrExp = new OrExpression();
//        filenameOrExp.add(
//            new CompareExpression(CompareOperator.LIKE,
//                new PropertyName(Constants.DAV, WebdavConstants.P_DISPLAYNAME),"%"+query.toLowerCase()+"%"));
        filenameOrExp.add(
            new CompareExpression(CompareOperator.LIKE,
                new PropertyName(Constants.DAV, "folderdisplayname"),"%"+query.toLowerCase()+"%"));
        return filenameOrExp;
    }

    /**
     * Very specific to GED demo
     *
     * @param searchRequest
     * @param jParams
     */
    public boolean addGEDClauses(SearchRequest searchRequest, ProcessingContext jParams){

//        if ( org.jahia.settings.SettingsBean.getInstance().isShowGEDProperties() ){
//            CompareExpression exp = null;
//            PropertyName propName = new PropertyName(FileNode.GED_NAMESPACE,"status");
//            if ( archivedStatus != null && !"".equals(archivedStatus.trim()) && !"all".equals(archivedStatus) ){
//                if ( "filed".equalsIgnoreCase(this.archivedStatus) ){
//                    exp = new CompareExpression(CompareOperator.EQ,propName,archivedStatus);
//                    ((AndExpression)searchRequest.getWhereExpression()).add(exp);
//                } else {
//                    exp = new CompareExpression(CompareOperator.EQ,propName,"filed");
//                    ((AndExpression)searchRequest.getWhereExpression()).add(searchRequest.not(exp));
//                }
//                return true;
//            }
//        }
        return false;
    }

    public JahiaSearchResult search(ProcessingContext jParams) throws JahiaException {

        Map<String, Object> engineMap = (Map<String, Object>)jParams.getAttribute("engineMap");
        String theScreen = (String)engineMap.get("screen");

        if (!Search_Engine.NAVIGUATE_RESULT_SCREEN.equals(theScreen)) {
            this.searchResult = ServicesRegistry.getInstance()
                    .getJahiaSearchService().fileSearch(this.getQuery(),
                            jParams.getUser());
        }
        if (this.searchResult == null) {
            this.searchResult = new JahiaSearchResult(
                    new WebDavSearchResultBuilderImpl(), false);
        }
        return this.searchResult;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getResourceType() {
        return resourceType;
    }

    public void setResourceType(int resourceType) {
        this.resourceType = resourceType;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

//    public String getSubject() {
//        return subject;
//    }
//
//    public void setSubject(String subject) {
//        this.subject = subject;
//    }
//
//    public String getArchivedStatus() {
//        return archivedStatus;
//    }
//
//    public void setArchivedStatus(String archivedStatus) {
//        this.archivedStatus = archivedStatus;
//    }

    public String getAccessPermission() {
        return accessPermission;
    }

    public void setAccessPermission(String accessPermission) {
        this.accessPermission = accessPermission;
    }

//    public List getFolderPathes() {
//        return folderPathes;
//    }
//
//    public List getFolderPathAsPropertyName() {
//        List props = new ArrayList();
//        if ( this.folderPathes != null && !this.folderPathes.isEmpty() ){
//            Iterator it = this.folderPathes.iterator();
//            while ( it.hasNext() ){
//                String[] tokens = JahiaTools.getTokens((String)it.next(),"@");
//                props.add(org.apache.slide.common.PropertyName.getPropertyName (tokens[1],tokens[0]));
//            }
//        }
//        return props;
//    }
//
//    public List getFolderPathAlias() {
//        return folderPathAlias;
//    }
//
//    public void setFolderPathAlias(List folderPathAlias) {
//        this.folderPathAlias = folderPathAlias;
//    }
//
//    public List getFolderPathSubFolders() {
//        return folderPathSubFolders;
//    }
//
//    public void setFolderPathSubFolders(List folderPathSubFolders) {
//        this.folderPathSubFolders = folderPathSubFolders;
//    }
//
//    public Map getCategories() {
//        return categories;
//    }
//
//    public void setCategories(Map categories) {
//        this.categories = categories;
//    }
//
//    public List getSubCategoriesEnabled() {
//        return subCategoriesEnabled;
//    }
//
//    public void setSubCategoriesEnabled(List subCategoriesEnabled) {
//        this.subCategoriesEnabled = subCategoriesEnabled;
//    }

    public List<String> getWebdavLocations() {
        return webdavLocations;
    }

    public void setWebdavLocations(List<String> webdavLocations) {
        this.webdavLocations = webdavLocations;
    }

    public CalendarHandler getCreatedFromCal() {
        return createdFromCal;
    }

    public void setCreatedFromCal(CalendarHandler createdFromCal) {
        this.createdFromCal = createdFromCal;
    }

    public CalendarHandler getCreatedToCal() {
        return createdToCal;
    }

    public void setCreatedToCal(CalendarHandler createdToCal) {
        this.createdToCal = createdToCal;
    }

    public CalendarHandler getModifiedFromCal() {
        return modifiedFromCal;
    }

    public void setModifiedFromCal(CalendarHandler modifiedFromCal) {
        this.modifiedFromCal = modifiedFromCal;
    }

    public CalendarHandler getModifiedToCal() {
        return modifiedToCal;
    }

    public void setModifiedToCal(CalendarHandler modifiedToCal) {
        this.modifiedToCal = modifiedToCal;
    }

    public String getCreatedSinceDate() {
        return createdSinceDate;
    }

    public void setCreatedSinceDate(String createdSinceDate) {
        this.createdSinceDate = createdSinceDate;
    }

    public String getModifiedSinceDate() {
        return modifiedSinceDate;
    }

    public void setModifiedSinceDate(String modifiedSinceDate) {
        this.modifiedSinceDate = modifiedSinceDate;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public int getSearchInContentAndFilename() {
        return searchInContentAndFilename;
    }

    public void setSearchInContentAndFilename(int val) {
        this.searchInContentAndFilename = val;
    }

    public void setSearchInContentAndFilename(String val) {
        if ( val == null ){
            return;
        }
        try {
            this.searchInContentAndFilename = Integer.parseInt(val);
        } catch ( Exception t ){
        }
    }

    public boolean getExpandMoreOptions() {
        return expandMoreOptions;
    }

    public void setExpandMoreOptions(boolean expandMoreOptions) {
        this.expandMoreOptions = expandMoreOptions;
    }

    public boolean getExpandCustomOptions() {
        return expandCustomOptions;
    }

    public void setExpandCustomOptions(boolean expandCustomOptions) {
        this.expandCustomOptions = expandCustomOptions;
    }

    public WebdavCriteriasHandler getCriteriasHandler() {
        return criteriasHandler;
    }

    public void setCriteriasHandler(WebdavCriteriasHandler criteriasHandler) {
        this.criteriasHandler = criteriasHandler;
    }

    public void appendSaveSearchDoc(ProcessingContext jParams, Element el)
    throws JahiaException {
        if ( this.contentType != null && !"".equals(this.contentType.trim()) ){
            el.addElement(CONTENT_TYPE).addText(this.contentType);
        }
        el.addElement(RESOURCE_TYPE).addText(String.valueOf(this.resourceType));

        if ( this.author != null && !"".equals(this.author.trim()) ){
            el.addElement(DOCUMENT_AUTHOR).addText(this.author);
        }
        if ( this.title != null && !"".equals(this.title.trim()) ){
            el.addElement(DOCUMENT_TITLE).addText(this.title);
        }
//        if ( this.subject != null && !"".equals(this.subject.trim()) ){
//            el.addElement(DOCUMENT_SUBJECT).addText(this.subject);
//        }
        // very specific to GED demo
//        if ( this.archivedStatus != null && !"".equals(this.archivedStatus.trim())
//                && !"all".equals(this.archivedStatus) ){
//            el.addElement("archivedStatus").addText(this.archivedStatus);
//        }

        // very specific to GED demo
        if ( this.accessPermission != null && !"".equals(this.accessPermission)
                && !"read".equals(this.accessPermission) ){
            el.addElement("accessPermission").addText(this.accessPermission);
        }

        el.addElement(SEARCH_IN_CONTENT_AND_FILENAME).addText(String.valueOf(this.searchInContentAndFilename));

//        if ( !this.folderPathes.isEmpty() ){
//            el.addElement(FOLDER_PATHES).addCDATA(StringUtils.join(this.folderPathes.toArray(),","));
//        }

//        if ( this.folderPathValues != null && this.folderPathValues.size()>0 ){
//            Iterator it = this.folderPathValues.keySet().iterator();
//            String key = null;
//            String value = null;
//            Element valuesElement = el.addElement(FOLDER_PATH_VALUES);
//            Element valueElement = null;
//            while ( it.hasNext() ){
//                key = (String)it.next();
//                value = (String)this.folderPathValues.get(key);
//                valueElement = valuesElement.addElement(VALUE_ELEMENT);
//                valueElement.addAttribute("folderPathName",key);
//                valueElement.addCDATA(value);
//            }
//        }

//        if ( !this.categories.isEmpty() ){
//            Iterator it = this.categories.keySet().iterator();
//            String key = null;
//            String value = null;
//            Element valuesElement = el.addElement(CATEGORIES);
//            Element valueElement = null;
//            while ( it.hasNext() ){
//                key = (String)it.next();
//                value = (String)this.categories.get(key);
//                valueElement = valuesElement.addElement(VALUE_ELEMENT);
//                valueElement.addAttribute("categoryName",key);
//                valueElement.addCDATA(value);
//            }
//        }

//        if ( !this.folderPathAlias.isEmpty() ){
//            el.addElement(FOLDER_PATH_ALIAS).addText(StringUtils.join(this.folderPathAlias.toArray(),","));
//        }

//        if ( !this.folderPathSubFolders.isEmpty() ){
//            el.addElement(FOLDER_PATH_SUBFOLDERS).addText(StringUtils.join(this.folderPathSubFolders.toArray(),","));
//        }

//        if ( !this.subCategoriesEnabled.isEmpty() ){
//            el.addElement(SUB_CATEGORIES_ENABLED).addText(StringUtils.join(this.subCategoriesEnabled.toArray(),","));
//        }

        if ( this.webdavLocations != null && this.webdavLocations.size()>0 ){
            Element domainsElement = el.addElement(LOCATION);
            Iterator<String> iterator =this.webdavLocations.iterator();
            while ( iterator.hasNext() ){
                domainsElement.addElement(AdvSearchViewHandler.VALUE_ELEMENT)
                        .addText(iterator.next());
            }
        }

        Element creationDate = el.addElement(CREATION_DATE);
        if ( this.createdFromCal != null && this.createdFromCal.getDateLong().longValue() > 0 ){
            creationDate.addElement(FROM_DATE).addText(String.valueOf(this.createdFromCal.getDateLong()));
        }
        if ( this.createdToCal != null && this.createdToCal.getDateLong().longValue() > 0 ){
            creationDate.addElement(TO_DATE).addText(String.valueOf(this.createdToCal.getDateLong()));
        }
        Element modifiedDate = el.addElement(MODIFICATION_DATE);
        if ( this.modifiedFromCal != null && this.modifiedFromCal.getDateLong().longValue() > 0 ){
            modifiedDate.addElement(FROM_DATE).addText(String.valueOf(this.modifiedFromCal.getDateLong()));
        }
        if ( this.modifiedToCal != null && this.modifiedToCal.getDateLong().longValue() > 0 ){
            modifiedDate.addElement(TO_DATE).addText(String.valueOf(this.modifiedToCal.getDateLong()));
        }
        Element dateEl = el.addElement(CREATION_SINCE_DATE);
        if ( this.createdSinceDate != null && !"".equals(this.createdSinceDate.trim()) ){
            dateEl.addText(this.createdSinceDate);
        }
        dateEl = el.addElement(MODIFICATION_SINCE_DATE);
        if ( this.modifiedSinceDate != null && !"".equals(this.modifiedSinceDate.trim()) ){
            dateEl.addText(this.modifiedSinceDate);
        }
        this.appendCustomCriterias(jParams,el);

        if ( this.sortBy != null && !"".equals(this.sortBy) ){
            el.addElement("sortBy").addText(this.sortBy);
        }

        if ( this.sortOrder != null && !"".equals(this.sortOrder) ){
            el.addElement("sortOrder").addText(this.sortOrder);
        }

    }

    public void appendCustomCriterias(ProcessingContext jParams, Element el)
    throws JahiaException {
        Element criteriasEl = el.addElement(CRITERIAS);
        List<CriteriaBean> criterias = this.criteriasHandler.getCriterias();
        for ( CriteriaBean criteria : criterias){
            if ( isCriteriaValid(criteria,true) ){
                Element criteriaEl = criteriasEl.addElement(CRITERIA);
                criteriaEl.addAttribute("criteriaComparator",criteria.getComparator());
                criteriaEl.addAttribute("criteriaOrder",String.valueOf(criteria.getOrder()));
                criteriaEl.addAttribute("criteriaIsDate",criteria.isDate()?"true":"false");
                criteriaEl.addElement("criteriaName").addCDATA(criteria.getName());
                criteriaEl.addElement("criteriaValue").addCDATA(String.valueOf(criteria.getValue()));
            }
        }
    }

    protected void useSaveSearchDoc(ProcessingContext jParams, Element root)
    throws JahiaException {
        try
        {
            if (root != null)
            {
                this.contentType = "";
                Element el = root.element(CONTENT_TYPE);
                if ( el != null ){
                    this.contentType = el.getText();
                }
                this.resourceType = FILE_RESOURCE_TYPE;
                el = root.element(RESOURCE_TYPE);
                if ( el != null ){
                    try {
                        this.resourceType = Integer.parseInt(el.getText());
                    } catch ( Exception t ){
                    }
                }
                this.author = "";
                el = root.element(DOCUMENT_AUTHOR);
                if ( el != null ){
                    this.author = el.getText();
                }
                this.title = "";
                el = root.element(DOCUMENT_TITLE);
                if ( el != null ){
                    this.title = el.getText();
                }
//                this.subject = "";
//                el = root.element(DOCUMENT_SUBJECT);
//                if ( el != null ){
//                    this.subject = el.getText();
//                }
                Element searchInContentAndFilename = root.element(SEARCH_IN_CONTENT_AND_FILENAME);
                if ( searchInContentAndFilename != null ){
                    this.setSearchInContentAndFilename(searchInContentAndFilename.getText());
                }

                // very specific to GED demo
//                this.archivedStatus = "open";
//                el = root.element("archivedStatus");
//                if ( el != null ){
//                    this.archivedStatus = el.getText();
//                    if ( "".equals(this.archivedStatus.trim()) ){
//                        this.archivedStatus = "open";
//                    }
//                }

                this.accessPermission = "read";
                el = root.element("accessPermission");
                if ( el != null ){
                    this.accessPermission = el.getText();
                    if ( "".equals(this.accessPermission.trim()) ){
                        this.accessPermission = "read";
                    }
                }

//                this.folderPathes = new ArrayList();
//                Element folderPathEl = root.element(FOLDER_PATHES);
//                if ( folderPathEl != null ){
//                    String[] values = StringUtils.split(folderPathEl.getText(),",");
//                    if ( values != null ){
//                        this.folderPathes.addAll(Arrays.asList(values));
//                    }
//                }

//                this.folderPathValues.clear();
//                el = root.element(FOLDER_PATH_VALUES);
//                if ( el != null ){
//                    List els = el.iterator();
//                    if ( els != null ){
//                        Iterator iterator = els.iterator();
//                        Element locationEl = null;
//                        String location = null;
//                        while ( iterator.hasNext() ){
//                            locationEl = (Element)iterator.next();
//                            location = locationEl.getText();
//                            this.folderPathValues.put(locationEl.attribute("folderPathName").getValue(),
//                                    location);
//                        }
//                    }
//                }
//
//                this.folderPathAlias.clear();
//                el = root.element(FOLDER_PATH_ALIAS);
//                if ( el != null ){
//                    String[] values = StringUtils.split(el.getText(),",");
//                    if ( values != null ){
//                        this.folderPathAlias.addAll(Arrays.asList(values));
//                    }
//                }
//
//                this.folderPathSubFolders.clear();
//                el = root.element(FOLDER_PATH_SUBFOLDERS);
//                if ( el != null ){
//                    String[] values = StringUtils.split(el.getText(),",");
//                    if ( values != null ){
//                        this.folderPathSubFolders.addAll(Arrays.asList(values));
//                    }
//                }
//
//                this.subCategoriesEnabled.clear();
//                el = root.element(SUB_CATEGORIES_ENABLED);
//                if ( el != null ){
//                    String[] values = StringUtils.split(el.getText(),",");
//                    if ( values != null ){
//                        this.subCategoriesEnabled.addAll(Arrays.asList(values));
//                    }
//                }
//
//                this.categories.clear();
//                el = root.element(CATEGORIES);
//                if ( el != null ){
//                    List els = el.iterator();
//                    if ( els != null ){
//                        Iterator iterator = els.iterator();
//                        String location = null;
//                        while ( iterator.hasNext() ){
//                            el = (Element)iterator.next();
//                            location = el.getText();
//                            this.categories.put(el.attribute("categoryName").getValue(),
//                                    location);
//                        }
//                    }
//                }

                this.webdavLocations.clear();
                el = root.element(LOCATION);
                if ( el != null ){
                    List<Element> els = el.elements();
                    if ( els != null ){
                        for ( Element locationEl : els ){
                            this.webdavLocations.add(locationEl.getText());
                        }
                    }
                }
                this.createdFromCal.reset();
                this.createdToCal.reset();
                Element creationDate = root.element(CREATION_DATE);
                if ( creationDate != null ){
                    Element boundDate = creationDate.element(FROM_DATE);
                    if ( boundDate != null ){
                        try {
                            long l = Long.parseLong(boundDate.getText());
                            this.createdFromCal.setDateLong(new Long(l));
                        } catch ( Exception t){
                        }
                    }
                    boundDate = creationDate.element(TO_DATE);
                    if ( boundDate != null ){
                        try {
                            long l = Long.parseLong(boundDate.getText());
                            this.createdToCal.setDateLong(new Long(l));
                        } catch ( Exception t){
                        }
                    }
                }
                this.modifiedFromCal.reset();
                this.modifiedToCal.reset();
                Element modifDate = root.element(MODIFICATION_DATE);
                if ( modifDate != null ){
                    Element boundDate = modifDate.element(FROM_DATE);
                    if ( boundDate != null ){
                        try {
                            long l = Long.parseLong(boundDate.getText());
                            this.modifiedFromCal.setDateLong(new Long(l));
                        } catch ( Exception t){
                        }
                    }
                    boundDate = modifDate.element(TO_DATE);
                    if ( boundDate != null ){
                        try {
                            long l = Long.parseLong(boundDate.getText());
                            this.modifiedToCal.setDateLong(new Long(l));
                        } catch ( Exception t){
                        }
                    }
                }
                this.createdSinceDate = "";
                el = root.element(CREATION_SINCE_DATE);
                if ( el != null ){
                    this.createdSinceDate = el.getText();
                }
                this.modifiedSinceDate = "";
                el = root.element(MODIFICATION_SINCE_DATE);
                if ( el != null ){
                    this.modifiedSinceDate = el.getText();
                }
                this.useCustomCriterias(jParams,root);

                this.sortBy = "sortBy";
                el = root.element("sortBy");
                if ( el != null ){
                    this.sortBy = el.getText().trim();
                }

                this.sortOrder = "sortOrder";
                el = root.element("sortOrder");
                if ( el != null ){
                    this.sortOrder = el.getText().trim();
                }
            }
        } catch ( Exception t ){
            logger.debug("Error paring value from JahiaSavedSearch Xml", t);
        }
    }

    protected void useCustomCriterias(ProcessingContext jParams, Element root)
    throws JahiaException {
        try
        {
            if (root != null)
            {
                this.getCriteriasHandler().getCriterias().clear();
                Element el = root.element(CRITERIAS);
                if ( el != null ){
                    List<Element> els = el.elements();
                    if ( els != null ){
                        for ( Element criteriaEl : els ){
                            try {
                                String name = criteriaEl.element("criteriaName").getText();
                                String value = criteriaEl.element("criteriaValue").getText();
                                String comparator = criteriaEl.attribute("criteriaComparator").getValue();
                                String orderStr = criteriaEl.attribute("criteriaOrder").getValue();
                                String isDateStr = criteriaEl.attribute("criteriaIsDate").getValue();
                                CriteriaBean criteriaBean = new CriteriaBean(name,value,comparator,
                                        Integer.parseInt(orderStr),true,false);
                                criteriaBean.setDate((isDateStr != null && "true".equals(isDateStr.trim().toLowerCase()) ));
                                this.getCriteriasHandler().getCriterias().add(criteriaBean);
                            } catch ( Exception t ){
                            }
                        }
                    }
                }
            }
        } catch ( Exception t ){
            logger.debug("Error paring value from JahiaSavedSearch Xml", t);
        }
    }

    protected boolean isCriteriaValid(CriteriaBean criteria, boolean allowEmptyStringValue){
        if ( criteria != null && criteria.getName() != null &&
                !"".equals(criteria.getName().trim())
                && criteria.getValue() != null
                && (allowEmptyStringValue || !"".equals(String.valueOf(criteria.getValue()).trim()))
                && criteria.getComparator() != null
                && comparatorsList.contains(criteria.getComparator()) ){
            return true;
        }
        return false;
    }

    private CalendarHandler getCalHandler(String calIdentifier, ProcessingContext jParams) {
        return new CalendarHandler(jParams.settings().getJahiaEnginesHttpPath(),
                calIdentifier,
                "advSearchForm",
                CalendarHandler.DEFAULT_DATE_FORMAT,
                0L,
                jParams.getLocale(),
                0L);
    }

    /**
     * Return a list of folder path as PropertyName
     * @param document
     * @return
     * @deprecated not supported after migration to JCR
     * @Deprecated not supported after migration to JCR
     */
    public static List<PropertyName> getFolderPaths(Document document) {
//        if ( document == null ){
//            return new ArrayList();
//        }
//        List list = new ArrayList();
//        try
//        {
//            Element root = document.getRootElement();
//            Element el = root.element(FOLDER_PATHES);
//            if (el != null)
//            {
//                String value = el.getText();
//                String[] tokens = StringUtils.split(value,",");
//                String folderPathName = null;
//                String[] nameTokens = null;
//                for ( int i=0; i<tokens.length; i++ ){
//                    folderPathName = tokens[i];
//                    nameTokens = JahiaTools.getTokens(folderPathName,"@");
////                    list.add(org.apache.slide.common.PropertyName.getPropertyName(nameTokens[1],nameTokens[0]));
//                }
//            }
//        }
//        catch (Exception t){
//            logger.debug("Error parsing JahiaSavedSearch xml",t);
//        }
//        return list;
        return Collections.emptyList();
    }


    private Date getSinceDate(String range){
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        cal.setTime(new Date());

        if ("today".equals(range)){
            cal.set( Calendar.HOUR_OF_DAY, 0 );
            cal.set( Calendar.MINUTE, 0);
            cal.set( Calendar.SECOND, 0);
        } else if ("1d".equals(range)) {
            cal.add(Calendar.DAY_OF_WEEK,-1);
        } else if ("2d".equals(range)) {
            cal.add(Calendar.DAY_OF_WEEK,-2);
        } else if ("3d".equals(range)) {
            cal.add(Calendar.DAY_OF_WEEK,-3);
        } else if ("thisweek".equals(range)) {
            cal.set(Calendar.DAY_OF_WEEK_IN_MONTH,cal.getFirstDayOfWeek());
            cal.set(Calendar.HOUR_OF_DAY,0);
            cal.set(Calendar.MINUTE,0);
            cal.set(Calendar.SECOND,0);
        } else if ("1w".equals(range)) {
            cal.add(Calendar.WEEK_OF_MONTH,-1);
        } else if ("2w".equals(range)) {
            cal.add(Calendar.WEEK_OF_MONTH,-2);
        } else if ("3w".equals(range)) {
            cal.add(Calendar.WEEK_OF_MONTH,-3);
        } else if ("thismonth".equals(range)) {
            cal.set(Calendar.DAY_OF_MONTH,1);
            cal.set(Calendar.HOUR_OF_DAY,0);
            cal.set(Calendar.MINUTE,0);
            cal.set(Calendar.SECOND,0);
        } else if ("1m".equals(range)) {
            cal.add( Calendar.MONTH, -1);
        } else if ("2m".equals(range)) {
            cal.add( Calendar.MONTH, -2);
        } else if ("3m".equals(range)) {
            cal.add( Calendar.MONTH, -3);
        } else if ("6m".equals(range)) {
            cal.add( Calendar.MONTH, -6);
        } else if ("thisyear".equals(range)) {
            cal.set(Calendar.MONTH,Calendar.JANUARY);
            cal.set(Calendar.DAY_OF_MONTH,1);
            cal.set(Calendar.HOUR_OF_DAY,0);
            cal.set(Calendar.MINUTE,0);
            cal.set(Calendar.SECOND,0);
        } else if ("1y".equals(range)) {
            cal.add(Calendar.YEAR, -1);
        }
        return cal.getTime();
    }

}