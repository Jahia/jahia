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

 package org.jahia.services.search.valves;

import org.jahia.params.ProcessingContext;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.Valve;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.PageProperty;
import org.jahia.services.search.IndexableDocument;
import org.jahia.services.search.JahiaPageIndexableDocument;
import org.jahia.services.search.JahiaSearchConstant;
import org.jahia.services.search.SearchIndexationPipeline;
import org.jahia.services.version.EntryLoadRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 * @author not attributable
 * @version 1.0
 */
public class PageSearchIndexProcessValveImpl implements SearchIndexationPipeline, Valve {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (PageSearchIndexProcessValveImpl.class);

    public PageSearchIndexProcessValveImpl() {
    }

    /**
     * Create the IndexableDocument if the sourceObject is a Page
     * and if indexableDocument does not exist in the contextMap
     *
     * @param context
     * @param valveContext
     * @throws org.jahia.pipelines.PipelineException
     */
    public void invoke(Object context, ValveContext valveContext) throws PipelineException {
        Map<String, Object> contextMap = (Map<String, Object>) context;
        Object srcObject = contextMap.get(SOURCE_OBJECT);
        if ( srcObject == null || !(srcObject instanceof JahiaPage) ){
            valveContext.invokeNext(context);
            return;
        }
        List<IndexableDocument> docs = (List<IndexableDocument>)contextMap.get(INDEXABLE_DOCUMENTS);
        if ( docs == null ) {
            IndexableDocument doc = createDocument(contextMap,(JahiaPage)srcObject);
            String[] allValues = fillDocumentWithValues(contextMap,(JahiaPage)srcObject,doc);
            doc.addFieldValues(JahiaSearchConstant.CONTENT_FULLTEXT_SEARCH_FIELD,allValues);
            //doc.addFieldValues(JahiaSearchConstant.CONTENT_FULLTEXT_SEARCH_FIELD_FOR_QUERY_REWRITE,allValues);
            doc.addFieldValues(JahiaSearchConstant.ALL_FULLTEXT_SEARCH_FIELD,allValues);
            //doc.addFieldValues(JahiaSearchConstant.ALL_FULLTEXT_SEARCH_FIELD_FOR_QUERY_REWRITE,allValues);
            allValues = fillDocumentWithMetadatas(contextMap,(JahiaPage)srcObject,doc);
            doc.addFieldValues(JahiaSearchConstant.METADATA_FULLTEXT_SEARCH_FIELD,allValues);
            doc.addFieldValues(JahiaSearchConstant.ALL_FULLTEXT_SEARCH_FIELD,allValues);

            JahiaPage page = (JahiaPage)srcObject;
            try {
                EntryLoadRequest loadRequest = (EntryLoadRequest)contextMap
                        .get(SearchIndexationPipeline.LOAD_REQUEST);
                if ( loadRequest != null ){
                    String pagePath = SearchIndexProcessValveUtils
                        .buildContentPagePath(ContentPage.getPage(page.getID()),loadRequest.getWorkflowState());
                    if ( pagePath != null ){
                        doc.setFieldValue(JahiaSearchConstant.METADATA_PAGE_PATH,pagePath);
                    }
                }
            } catch ( Exception t ){
                logger.debug("Error building page path for page " + page.getID(),t);
            }
            docs = new ArrayList<IndexableDocument>();
            docs.add(doc);
        }
        contextMap.put(INDEXABLE_DOCUMENTS,docs);
        valveContext.invokeNext(context);
    }

    /**
     * By Default, create a JahiaPageIndexableDocument with the passed page.
     *
     * @param contextMap
     * @param page
     * @return
     */
    protected IndexableDocument createDocument(Map<String, Object> contextMap,
                                               JahiaPage page){
        EntryLoadRequest loadRequest = (EntryLoadRequest)contextMap
                .get(SearchIndexationPipeline.LOAD_REQUEST);
        if ( loadRequest == null ){
            return null;
        }
        JahiaPageIndexableDocument doc =
                new JahiaPageIndexableDocument(page,loadRequest);
        return doc;
    }

    /**
     * Load the page's title and add them as IndexableDocument's attributes.
     *
     * @param contextMap
     * @param page
     * @param doc
     * @return returns a String array of values used to store under the attribute IndexableDocument.CONTENT_FULLTEXT_SEARCH_FIELD
     */
    protected String[] fillDocumentWithValues(Map<String, Object> contextMap,
                                                  JahiaPage page,
                                              IndexableDocument doc){

        String[] values = new String[1];
        String title = page.getRawTitle();
        if ( title != null ){
            doc.setFieldValue(JahiaSearchConstant.TITLE,title);
            title = title.trim();
        } else {
            title = "";
        }
        values[0] = title;

        List<String> valuesList = new ArrayList<String>();
        valuesList.addAll(Arrays.asList(values));

        String[] urlKeys = fillDocumentWithPageURLKey(contextMap,page,doc);

        valuesList.addAll(Arrays.asList(urlKeys));

        values = (String[])valuesList.toArray(values);

        return values;
    }

    /**
     * Load the page's title and add them as IndexableDocument's attributes.
     *
     * @param contextMap
     * @param page
     * @param doc
     * @return returns a String array of values used to store under the attribute IndexableDocument.CONTENT_FULLTEXT_SEARCH_FIELD
     */
    protected String[] fillDocumentWithPageURLKey(  Map<String, Object> contextMap,
                                                    JahiaPage page,
                                                    IndexableDocument doc){

        String[] values = new String[1];
        try {
            String urlKey = page.getContentPage().getProperty(PageProperty.PAGE_URL_KEY_PROPNAME);
            if ( urlKey != null ){
                urlKey = urlKey.trim();
                doc.setFieldValue(JahiaSearchConstant.PAGE_URL_KEY,urlKey);
            } else {
                urlKey = "";
            }
            values[0] = urlKey;
        } catch ( Exception t ){
            logger.debug(t);
        }
        return values;
    }

    /**
     * Load the container's metadatas and add them as IndexableDocument's attributes.
     *
     * @param contextMap
     * @param page
     * @param doc
     */
    protected String[] fillDocumentWithMetadatas(   Map<String, Object> contextMap,
                                                    JahiaPage page,
                                                    IndexableDocument doc){

        ProcessingContext context = (ProcessingContext)contextMap
                .get(SearchIndexationPipeline.PROCESSING_CONTEXT);

        String[] values = null;
        EntryLoadRequest loadRequest = (EntryLoadRequest)contextMap
                .get(SearchIndexationPipeline.LOAD_REQUEST);
        if ( loadRequest == null ){
            return values;
        }
        try {
            ContentPage contentPage = ContentPage.getPage(page.getID());
            values = SearchIndexProcessValveUtils.loadContentMetadatas(contextMap,
                        contentPage,
                        loadRequest.getFirstLocale(true),
                        loadRequest.getWorkflowState(),doc, context);
        } catch ( Exception t){
            logger.debug("Exception occured when getting container' metadatas for indexation",t);
        }
        return values;
    }

    public void initialize() {
    }

}
