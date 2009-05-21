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
package org.jahia.services.search.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.Query;
import org.compass.core.Compass;
import org.compass.core.config.RuntimeCompassSettings;
import org.compass.core.engine.SearchEngine;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.spi.InternalCompass;
import org.jahia.bin.Jahia;
import org.jahia.content.CoreFilterNames;
import org.jahia.content.ObjectKey;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaObjectManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.params.AdvPreviewSettings;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.search.JahiaSearchConstant;
import org.jahia.services.search.SearchResult;
import org.jahia.services.search.SearchResultImpl;
import org.jahia.services.search.lucene.fs.JahiaIndexSearcher;
import org.jahia.services.timebasedpublishing.TimeBasedPublishingService;
import org.springframework.context.ApplicationContext;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 2 mars 2007
 * Time: 17:59:31
 * To change this template use File | Settings | File Templates.
 */
public class JahiaHitCollector extends JahiaAbstractHitCollector {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(JahiaHitCollector.class);

    private boolean onlyOneHitByPage = true;
    private int maxLuceneDocs = Integer.MAX_VALUE;
    private int maxHits = Integer.MAX_VALUE;
    private int maxHitsByPage = 1;
    private int maxPages = Integer.MAX_VALUE;
    private Map<String, List<Integer>> pageHits = new HashMap<String, List<Integer>>();
    private List<Integer> fileHits = new ArrayList<Integer>();
    private int maxHitsCount = 0;
    private int luceneDocsCount = 0;
    private boolean limitReached = false;
    private Map<Integer, Float> scores = new HashMap<Integer, Float>();
    private float maxScore;
    private Map<Integer, Document> docs = new HashMap<Integer, Document>();
    JahiaObjectManager jahiaObjectManager = null;
    private FieldSelector fieldSelector;
    private List<String> allowedFields;
    private boolean ignoreAcl = false;
    private boolean ignoreTbp = false;
    private Map<Integer, Boolean> aclCache = new HashMap<Integer, Boolean>();

    public JahiaHitCollector() {
        if (this.getFieldSelector() == null) {
            allowedFields = ServicesRegistry.getInstance()
                    .getJahiaSearchService().getFieldsToCopyToSearchHit();
            this.setFieldSelector(new SetNonLazyFieldSelector(allowedFields));
        }

        final ApplicationContext context = SpringContextSingleton.getInstance()
                .getContext();
        jahiaObjectManager = (JahiaObjectManager) context
                .getBean(JahiaObjectManager.class.getName());
    }

    public JahiaHitCollector(boolean onlyOneHitByPage,
                             int maxLuceneDocs,
                             int maxHits,
                             int maxHitsByPage,
                             int maxPages){
        this();
        this.onlyOneHitByPage = onlyOneHitByPage;
        this.maxLuceneDocs = maxLuceneDocs;
        this.maxHits = maxHits;
        this.maxHitsByPage = maxHitsByPage;
        this.maxPages = maxPages;
    }

    public JahiaHitCollector(boolean onlyOneHitByPage,
                             int maxLuceneDocs,
                             int maxHits,
                             int maxHitsByPage,
                             int maxPages,
                             boolean ignoreAcl,
                             boolean ignoreTbp){
        this();
        this.onlyOneHitByPage = onlyOneHitByPage;
        this.maxLuceneDocs = maxLuceneDocs;
        this.maxHits = maxHits;
        this.maxHitsByPage = maxHitsByPage;
        this.maxPages = maxPages;
        this.ignoreAcl = ignoreAcl;
        this.ignoreTbp = ignoreTbp;
    }

    /**
     *
     * @param doc
     * @param score
     */
    public void collect(int doc, float score) {
        if ( maxScore < score  ){
            maxScore = score;
        }
        if ( this.searcher == null ){
            return;
        }
        if ( limitReached ){
            return;
        }
        luceneDocsCount++;
        if ( this.maxLuceneDocs != -1 && this.luceneDocsCount >=this.maxLuceneDocs ){
            this.limitReached = true;
            return;
        }
        if ( !this.onlyOneHitByPage && maxHits != -1 && maxHitsCount>=maxHits ){
            this.limitReached = true;
            return;
        }
        if ( this.onlyOneHitByPage && maxHits != -1 && (this.fileHits.size()+this.pageHits.size()>=maxHits) ){
            this.limitReached = true;
            return;
        }

        try {
            boolean accessAllowed = true;
            Integer docId = new Integer(doc);
            Document luceneDoc = this.searcher.doc(doc, getFieldSelector());
            if ( luceneDoc != null ){
                if ( this.onlyOneHitByPage ){
                    String pageId = luceneDoc.get(JahiaSearchConstant.PAGE_ID);
                    String fieldId = luceneDoc.get(JahiaSearchConstant.FIELD_FIELDID);
                    boolean isFile = (fieldId != null && !"".equals(fieldId.trim()));
                    if ( pageId == null ){
                        pageId = luceneDoc.get(JahiaSearchConstant.ID);
                    }
                    if ( isFile ){
                        if ( this.fileHits.size()+this.pageHits.size()>=this.maxPages ){
                            // reached max pages of files
                            return;
                        }
                        accessAllowed = checkAccess(luceneDoc,jahiaObjectManager, ignoreAcl, ignoreTbp);
                        if ( accessAllowed ){
                            this.fileHits.add(docId);
                            docs.put(docId,luceneDoc);
                            scores.put(docId,new Float(score));
                        }
                    } else if ( pageId != null ){
                        List<Integer> hits = pageHits.get(pageId);
                        if ( hits == null ){
                            if ( maxPages != -1 && pageHits.size()>=maxPages ){
                                // reached max pages
                                return;
                            }
                            accessAllowed = checkAccess(luceneDoc,jahiaObjectManager, ignoreAcl, ignoreTbp);
                            if ( accessAllowed ){
                                hits = new ArrayList<Integer>();
                                pageHits.put(pageId,hits);
                            }
                        } else {
                            accessAllowed = checkAccess(luceneDoc,jahiaObjectManager, ignoreAcl, ignoreTbp);
                        }
                        if ( accessAllowed && (maxHitsByPage == -1 || hits.size()<=maxHitsByPage) ){
                            hits.add(docId);
                            docs.put(docId,luceneDoc);
                            scores.put(docId,new Float(score));
                        }
                    }
                } else {
                    accessAllowed = checkAccess(luceneDoc,jahiaObjectManager, ignoreAcl, ignoreTbp);
                    if ( accessAllowed ){
                        maxHitsCount++;
                        docs.put(docId,luceneDoc);
                        scores.put(docId,new Float(score));
                    }
                }
                luceneDoc = null;
            }
        } catch ( Exception t ){
            logger.debug("Exception collecting lucene hits");
        }
    }

    public SearchResult getSearchResult(Query q) throws JahiaException {
        LuceneSearchEngine searchEngine = null;
        Compass compass = ServicesRegistry.getInstance().getJahiaSearchService().getCompass();

        if (compass != null && compass instanceof InternalCompass) {
            InternalCompass internalCompass = (InternalCompass) compass;
            SearchEngine se = internalCompass.getSearchEngineFactory().openSearchEngine(new RuntimeCompassSettings(internalCompass.getSettings()));
            if (se instanceof LuceneSearchEngine) {
                searchEngine = (LuceneSearchEngine) se;
            }
        }
        
        SearchResult searchResult = searchEngine != null ? new LuceneSearchResult(
                ((JahiaIndexSearcher) this.searcher).getReader(), q,
                searchEngine)
                : new SearchResultImpl(false);

        float scoreNorm = this.maxScore > 1.0f ? 1.0f / this.maxScore : 1.0f;

        for (Map.Entry<Integer, Document> docEntry : docs.entrySet()) {
            Float score = scores.get(docEntry.getKey());

            LuceneSearchHit searchHit = new LuceneSearchHit(docEntry.getValue());
            Map<String, List<Object>> fieldsMap = new HashMap<String, List<Object>>();
            searchHit.setSearchResult(searchResult);
            searchHit.setScore(score.floatValue() * scoreNorm);
            searchHit.setDocNumber(docEntry.getKey().intValue());

            for (Iterator<?> it = docEntry.getValue().getFields().iterator(); it.hasNext();) {
                Fieldable field = (Fieldable) it.next();
                String name = field.name();
                List<Object> list = fieldsMap.get(name);
                if ( list == null ){
                    list = new ArrayList<Object>();
                    fieldsMap.put(name, list);
                }
                if ( !field.isBinary() && field instanceof Field ){ // by testing for Field instance, we avoid loading
                                                                    // value from all lazy Field
                    list.add(field.stringValue());
                }
            }
            searchHit.setFields(fieldsMap);
            searchResult.add(searchHit);
        }
        return searchResult;
    }

    public int getMaxHits() {
        return maxHits;
    }

    public void setMaxHits(int maxHits) {
        this.maxHits = maxHits;
    }

    public int getMaxHitsByPage() {
        return maxHitsByPage;
    }

    public void setMaxHitsByPage(int maxHitsByPage) {
        this.maxHitsByPage = maxHitsByPage;
    }

    public int getMaxPages() {
        return maxPages;
    }

    public void setMaxPages(int maxPages) {
        this.maxPages = maxPages;
    }

    public Map<String, List<Integer>> getPageHits() {
        return pageHits;
    }

    public void setPageHits(Map<String, List<Integer>> pageHits) {
        this.pageHits = pageHits;
    }

    public int getMaxHitsCount() {
        return maxHitsCount;
    }

    public void setMaxHitsCount(int maxHitsCount) {
        this.maxHitsCount = maxHitsCount;
    }

    public int getLuceneDocsCount() {
        return luceneDocsCount;
    }

    public void setLuceneDocsCount(int luceneDocsCount) {
        this.luceneDocsCount = luceneDocsCount;
    }

    public boolean checkAccess(Document luceneDoc, JahiaObjectManager jahiaObjectManager, boolean ignoreAcl, boolean ignoreTbp){
        boolean accessAllowed = true;
        // acl check
        Fieldable field = luceneDoc.getFieldable(JahiaSearchConstant.ACL_ID);
        if ( field != null ){
            try {
                if (!ignoreAcl) {
                    int aclID = Integer.parseInt(field.stringValue());
                    Boolean accessFlag = aclCache.get(aclID);
                    if (accessFlag != null) {
                        accessAllowed = accessFlag.booleanValue();
                    } else {
                        try {
                            accessAllowed = false;
                            accessFlag = Boolean.FALSE;
                            final JahiaBaseACL acl = JahiaBaseACL.getACL(aclID);
                            if (acl.getPermission(Jahia.getThreadParamBean()
                                    .getUser(), JahiaBaseACL.READ_RIGHTS)) {
                                accessAllowed = true;
                                accessFlag = Boolean.TRUE;
                            }
                        } catch (Exception ex) {
                            logger.debug("Exception checking hit access - ACL-ID: " + aclID);
                        }
                        aclCache.put(aclID, accessFlag);
                    }
                }
                if (accessAllowed) {
                    field = luceneDoc
                            .getFieldable(JahiaSearchConstant.OBJECT_KEY);
                    if (field != null) {
                        if (!ignoreTbp) {
                            ObjectKey objectKey = ObjectKey.getInstance(field
                                    .stringValue());
                            // Check for expired container
                            boolean disableTimeBasedPublishingFilter = Jahia
                                    .getThreadParamBean()
                                    .isFilterDisabled(
                                            CoreFilterNames.TIME_BASED_PUBLISHING_FILTER);
                            ProcessingContext context = Jahia
                                    .getThreadParamBean();
                            final TimeBasedPublishingService tbpServ = ServicesRegistry
                                    .getInstance()
                                    .getTimeBasedPublishingService();
                            if (!disableTimeBasedPublishingFilter) {
                                if (ParamBean.NORMAL.equals(context
                                        .getOperationMode())) {
                                    accessAllowed = tbpServ.isValid(objectKey,
                                            context.getUser(), context
                                                    .getEntryLoadRequest(),
                                            context.getOperationMode(),
                                            (Date) null);
                                } else if (ParamBean.PREVIEW.equals(context
                                        .getOperationMode())) {
                                    accessAllowed = tbpServ.isValid(objectKey,
                                            context.getUser(), context
                                                    .getEntryLoadRequest(),
                                            context.getOperationMode(),
                                            AdvPreviewSettings
                                                    .getThreadLocaleInstance());
                                }
                            }
                        }
                    }
                }
            } catch ( Exception t){
                logger.debug("Exception checking hit access", t);
                return false;
            }
        }
        return accessAllowed;
    }

    public void clear() {
        this.pageHits.clear();
        this.fileHits.clear();
        this.scores.clear();
        this.docs.clear();
        this.maxScore = 0f;
        this.maxHitsCount = 0;
        this.luceneDocsCount = 0;
        this.limitReached = false;
    }

    public FieldSelector getFieldSelector() {
        return fieldSelector;
    }

    public void setFieldSelector(FieldSelector fieldSelector) {
        this.fieldSelector = fieldSelector;
    }

}
