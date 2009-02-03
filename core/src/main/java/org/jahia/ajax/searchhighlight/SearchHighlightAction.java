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

 package org.jahia.ajax.searchhighlight;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.compass.core.Compass;
import org.compass.core.config.RuntimeCompassSettings;
import org.compass.core.engine.SearchEngine;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.spi.InternalCompass;
import org.jahia.ajax.AjaxAction;
import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.params.ParamBean;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.webdav.JahiaWebdavBaseService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * SearchHighlighting action
 * Returns the highlighted text for a given resource
 *
 */
public class SearchHighlightAction extends AjaxAction {

    private static final transient Logger logger = Logger.getLogger(SearchHighlightAction.class);

    private LuceneSearchEngine searchEngine;

    /**
     * constructor
     */
    public SearchHighlightAction() {
        super();
        SearchHighlightAction.logger.debug("initialisation of SearchHighlightAction action");

        InternalCompass internalCompass = null;
        Compass compass = ServicesRegistry.getInstance().getJahiaSearchService().getCompass();

        if ( searchEngine == null && compass != null && compass instanceof InternalCompass) {
            internalCompass = (InternalCompass) compass;
            SearchEngine se = internalCompass.getSearchEngineFactory().openSearchEngine(new RuntimeCompassSettings(internalCompass.getSettings()));
            if (se instanceof LuceneSearchEngine) {
                searchEngine = (LuceneSearchEngine) se;
            }
        }

    }

    /**
     * implentation method that will execute the AJAX Action
     *
     * @param mapping  Struts ActionMapping
     * @param form     Struts ActionForm
     * @param request  The current HttpServletRequest
     * @param response The HttpServletResponse linked to the current request
     * @return ActionForward    Struts ActionForward
     * @throws java.io.IOException
     * @throws javax.servlet.ServletException
     * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public ActionForward execute(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {

        try {
            final JahiaUser currentUser = (JahiaUser) request.getSession().getAttribute(ParamBean.SESSION_USER);
            final JahiaSite site = (JahiaSite) request.getSession().getAttribute("org.jahia.services.sites.jahiasite");

            if (currentUser == null || site == null )
            {
                SearchHighlightAction.logger.debug("Unauthorized attempt to use AJAX Struts Action - SearchHighlightAction");
                if (isValidUser(currentUser)) {
                    throw new JahiaForbiddenAccessException("Must have 'Admin' access");
                } else {
                    throw new JahiaForbiddenAccessException("Must be logged in");
                }
            }
            org.w3c.dom.Document doc = getRequestXmlDocument(request);
            if ( doc == null ){
                throw new JahiaBadRequestException("Wrong XML request");
            }
            String resURL = getStringValueFromDocument(doc, "resURL");
//            String searchQuery = getStringValueFromDocument(doc, "searchQuery");
            getStringValueFromDocument(doc, "searchFieldName");

//            final ProcessingContext jParams = retrieveProcessingContext(request, response);
            JCRNodeWrapper file = JahiaWebdavBaseService.getInstance()
                    .getDAVFileAccess(resURL, currentUser);
            if ( file != null ){
//                JcrResourceForHighLighting res = new JcrResourceForHighLighting(file,searchEngine);
//                JahiaCompassHighlighter highlighter = getCompassHighlighter(res,searchQuery,org.apache.slide.index.lucene.Index.CONTENT_FIELD_NAME);
//                String highlightedText = null;
//                if ( highlighter != null ){
//                    highlighter.setMaxNumFragments(2);
//                    highlightedText = highlighter.fragmentsWithSeparator(org.apache.slide.index.lucene.Index.CONTENT_FIELD_NAME);
//                }
//                if (highlightedText==null||"...".equals(highlightedText)||"".equals(highlightedText.trim())){
//                    highlightedText = res.get(org.apache.slide.index.lucene.Index.CONTENT_FIELD_NAME);
//                    if (highlightedText==null){
//                        highlightedText = "";
//                    }
//                    if (highlightedText.length()>200){
//                        highlightedText = highlightedText.substring(0,200);
//                    }
//                }
//                StringBuffer buf = new StringBuffer();
//                buf.append(XML_HEADER);
//                buf.append("<response>\n");
//                buf.append(buildXmlElement("highlightedText","<![CDATA[" + XmlUtils.removeNotValidXmlChars(highlightedText) + "]]>") );
//                buf.append("</response>\n");
//                sendResponse(buf.toString(), response);
            } else {
                throw new JahiaBadRequestException("Requested file '" + resURL
                        + "'not found");
            }
        } catch (Exception e) {
            handleException(e, request, response);
        }
        return null;


    }

    /**
     * Returns an highlighter for the hits.
     */

//    protected JahiaCompassHighlighter getCompassHighlighter(SlideResourceForHighLighting res,
//                                                                String queryStr, String fieldName){
//
//        LuceneSearchEngineHighlighter highlighter = null;
//        JahiaCompassHighlighter compassHighlighter = null;
//
//        if ( searchEngine == null || res == null ){
//            return null;
//        }
//        try {
//            IndexReader reader;
//            RAMDirectory ramDir;
//
//            ramDir = new RAMDirectory();
//            Analyzer analyzer = this.searchEngine.getSearchEngineFactory()
//                    .getAnalyzerManager().getAnalyzerByAlias("jahiaIndexer");
//
//            IndexWriter writer = new IndexWriter(ramDir, analyzer, true);
//
//            Map properties = res.getDAVProperties();
//            if ( queryStr != null && !"".equals(queryStr.trim()) ){
//                Iterator iterator = properties.keySet().iterator();
//                String key = null;
//                NodeProperty nodeProperty = null;
//                Document doc = new Document();
//                while ( iterator.hasNext() ){
//                    key = (String)iterator.next();
//                    nodeProperty = (NodeProperty)properties.get(key);
//                    String value = "";
//                    try {
//                        value = nodeProperty.getValue().toString();
//                    } catch ( Exception t ){
//                    }
//                    Field f = new Field(key, value,Field.Store.YES, Field.Index.TOKENIZED);
//                    doc.add(f);
//                }
//                writer.addDocument(doc);
//                writer.optimize();
//                writer.close();
//                reader = IndexReader.open(ramDir);
//
//                QueryParser queryParser = new QueryParser(fieldName,analyzer);
//                Query query = queryParser.parse(queryStr);
//                highlighter = new JahiaLuceneSearchEngineHighlighter(
//                        new LuceneSearchEngineQuery(this.searchEngine,query),
//                        new ArrayList(),
//                        reader,searchEngine,query.rewrite(reader));
//
//                doc = reader.document(0);
//                if ( doc != null ){
//                    compassHighlighter = ServicesRegistry.getInstance()
//                            .getJahiaSearchService().getCompassHighlighter(highlighter,
//                            new LuceneResource(doc,0,this.searchEngine));
//                }
//            }
//       } catch ( Exception t){
//              logger.error(t.getMessage(), t);
//        }
//        return compassHighlighter;
//    }

}
