/*
 * Copyright 2002-2008 Jahia Ltd
 *
 * Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL), 
 * Version 1.0 (the "License"), or (at your option) any later version; you may 
 * not use this file except in compliance with the License. You should have 
 * received a copy of the License along with this program; if not, you may obtain 
 * a copy of the License at 
 *
 *  http://www.jahia.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package org.jahia.services.search.lucene.fs;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.compass.core.Compass;
import org.compass.core.config.RuntimeCompassSettings;
import org.compass.core.engine.SearchEngine;
import org.compass.core.engine.SearchEngineFactory;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.analyzer.LuceneAnalyzerManager;
import org.compass.core.lucene.util.ChainedFilter;
import org.compass.core.lucene.util.ChainedFilter.ChainedFilterType;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.spi.InternalCompass;
import org.jahia.content.ContentObject;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cluster.ClusterService;
import org.jahia.services.search.*;
import org.jahia.services.search.lucene.*;

import java.util.*;


/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 15 f�vr. 2005
 * Time: 18:47:47
 * To change this template use File | Settings | File Templates.
 */
public class LuceneSearchHandlerImpl extends SearchHandlerImpl {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(LuceneSearchHandlerImpl.class);

    private LuceneCoreSearcher coreSearcher;

    private FieldSelector fieldSelector;

    public LuceneSearchHandlerImpl() {
        super();
    }

    public LuceneSearchHandlerImpl(Properties config) {
        super(config);
    }

    public LuceneSearchHandlerImpl(SearchManager searchManager, Properties config) {
        super(searchManager, config);
    }

    /**
     * This method should be called, once the config and searchManager are set to allow
     * internal initialisation.
     *
     * @throws Exception
     */
    public void init() throws Exception {
        JahiaSearchService searchService = ServicesRegistry.getInstance().getJahiaSearchService();

        if (this.getFieldSelector() == null) {
            List<String> allowedFields = searchService.getFieldsToCopyToSearchHit();
            this.setFieldSelector(new SetNonLazyFieldSelector(allowedFields));
        }

        this.coreSearcher = new LuceneCoreSearcher(this, getConfig());
    }
    
    private ResourceMapping getResourceMapping() {
        ResourceMapping resourceMapping = null;
        Compass compass = ServicesRegistry.getInstance().getJahiaSearchService().getCompass();
        if (compass != null && compass instanceof InternalCompass) {
            InternalCompass internalCompass = (InternalCompass) compass;
            SearchEngineFactory searchEngineFactory = internalCompass.getSearchEngineFactory();
            if (searchEngineFactory != null && searchEngineFactory instanceof LuceneSearchEngineFactory) {
                LuceneSearchEngineFactory luceneSearchEngineFactory = (LuceneSearchEngineFactory) searchEngineFactory;
                resourceMapping = luceneSearchEngineFactory.getMapping().getRootMappingByAlias("jahiaSearcher");
            }
        }
        return resourceMapping;
    }
    
    private Analyzer getDefaultAnalyzer() {
        Analyzer defaultAnalyzer = null;
        Compass compass = ServicesRegistry.getInstance().getJahiaSearchService().getCompass();
        if (compass != null && compass instanceof InternalCompass) {
            InternalCompass internalCompass = (InternalCompass) compass;
            SearchEngineFactory searchEngineFactory = internalCompass.getSearchEngineFactory();
            if (searchEngineFactory != null && searchEngineFactory instanceof LuceneSearchEngineFactory) {
                LuceneSearchEngineFactory luceneSearchEngineFactory = (LuceneSearchEngineFactory) searchEngineFactory;
                LuceneAnalyzerManager analyzerMgr = luceneSearchEngineFactory.getAnalyzerManager();                
                defaultAnalyzer = analyzerMgr.getAnalyzerByAliasMustExists("jahiaSearcher");
            }
        }
        return defaultAnalyzer;
    }
    
    private Analyzer getAnalyzer(String languageCode) {
        Analyzer analyzer = null;
        Compass compass = ServicesRegistry.getInstance().getJahiaSearchService().getCompass();
        if (compass != null && compass instanceof InternalCompass) {
            InternalCompass internalCompass = (InternalCompass) compass;
            SearchEngineFactory searchEngineFactory = internalCompass.getSearchEngineFactory();
            if (searchEngineFactory != null && searchEngineFactory instanceof LuceneSearchEngineFactory) {
                LuceneSearchEngineFactory luceneSearchEngineFactory = (LuceneSearchEngineFactory) searchEngineFactory;
                LuceneAnalyzerManager analyzerMgr = luceneSearchEngineFactory.getAnalyzerManager();                
                ResourceMapping compassMapping = luceneSearchEngineFactory.getMapping().getRootMappingByAlias("jahiaSearcher_" + languageCode);
                if (compassMapping != null) {
                    analyzer = analyzerMgr.getAnalyzerByAlias(compassMapping.getAlias());
                }
            }
        }
        return analyzer;
    }

    public SearchResult search(String query) {
        SearchResult result = new SearchResultImpl();
        search(query, Collections.<String>emptyList(), result, null, null, null, null, null);
        return result;
    }

    public void search(String query, SearchResult collector) {
        search(query, Collections.<String>emptyList(), collector, null, null, null, null, null);
    }

    public void search(String query, List<String> languageCodes, SearchResult collector, Filter filter) {
        search(query, languageCodes, collector, null, filter, null, null, null);
    }

    public void search(String query, List<String> languageCodes, SearchResult collector,
                       JahiaAbstractHitCollector hitCollector) {
        search(query, languageCodes, collector, null, null, null, null, hitCollector);
    }
    
    public void search(String query, List<String> languageCodes, SearchResult collector, String[] filterQueries, JahiaAbstractHitCollector hitCollector) {
        search(query, languageCodes, collector, filterQueries, null, null, null, hitCollector);        
    }    

    public void search(String query, List<String> languageCodes, SearchResult collector, Sort sort) {
        search(query, languageCodes, collector, null, null, sort, null, null);
    }

    public void search(String query, List<String> languageCodes, SearchResult collector, Sort sort,
                       IndexReader reader) {
        search(query, languageCodes, collector, null, null, sort, reader, null);
    }

    public void search(String query, List<String> languageCodes, SearchResult collector, Filter filter,
                       Sort sort) {
        search(query, languageCodes, collector, null, filter, sort, null, null);
    }

    public void search(String query, List<String> languageCodes, SearchResult collector, String[] filterQueries, Sort sort, IndexReader reader) {
        search(query, languageCodes, collector, filterQueries, null, sort, reader, null);
    }        

    public void search(String query, List<String> languageCodes, SearchResult collector, Filter filter,
                       Sort sort, IndexReader reader,
                       JahiaAbstractHitCollector hitCollector) {
        search(query, languageCodes, collector, null, filter, sort, reader, hitCollector);
    }
    
    public void search(String query, List<String> languageCodes, SearchResult collector,
            String[] filterQueries, Filter filter, Sort sort, IndexReader reader,
            JahiaAbstractHitCollector hitCollector) {
        
        languageCodes = new ArrayList<String>(languageCodes);
        languageCodes.remove(ContentObject.SHARED_LANGUAGE);
        
        if (logger.isDebugEnabled()) {
            logger.debug("Query: " + query);
            if (filterQueries != null && filterQueries.length > 0) {
                
                for (int i = 0; i < filterQueries.length; i++ ) {
                   logger.debug("filterQuery " + i + ":" + filterQueries[i]);
                }   
            }
        }    
        
        LuceneQueryRequest req = null;
        boolean closeSearcher = false;
        Searcher searcher = null;
        try {
            /*
            QueryParser queryParser = new JahiaLuceneQueryParser(
                    getAllSearchFieldName(), ServicesRegistry.getInstance()
                    .getJahiaSearchService().getFieldScoreBoosts(),
                    ServicesRegistry.getInstance().getJahiaSearchService()
                            .getFieldsGrouping(), reader != null ? 0 : this.getSiteId(),
            */
            // we should use the field boost score of the site from which wthe searhc is launched even thought
            // is is a multiple index reader ( multi site )
            Analyzer analyzer = getDefaultAnalyzer();
            if (languageCodes.size() == 1) {
                Analyzer languageAnalyzer = getAnalyzerForLanguage(languageCodes.get(0));
                if (languageAnalyzer != null && !analyzer.equals(languageAnalyzer)) {
                    analyzer = languageAnalyzer;
                }
            }
            ResourceMapping resourceMapping = getResourceMapping();
            QueryParser queryParser = new JahiaLuceneQueryParser(
                    getAllSearchFieldName(), ServicesRegistry.getInstance().
                    getJahiaSearchService().getFieldsGrouping(), this.getSiteId(),
                    analyzer, resourceMapping);
            queryParser.setAllowLeadingWildcard(true);
            
            if (reader != null) {
                searcher = new JahiaIndexSearcher(coreSearcher, "multi", reader, false);
                closeSearcher = true;
                if (filterQueries != null && filterQueries.length > 0) {
                    StringBuffer queryBuffer = new StringBuffer();
                    if (query != null) {
                        queryBuffer.append(query);
                    }
                    for (int i = 0, size = filterQueries.length; i < size; i++) {
                        if (queryBuffer.length() > 0) {
                            queryBuffer.append(" AND ");
                        }
                        queryBuffer.append("(").append(filterQueries[i]).append(")");
                    }
                    query = queryBuffer.toString();
                }
            } else {
                req = new LuceneQueryRequest(coreSearcher);
                JahiaSearchBaseService.addReferenceToOpenLuceneQueryRequest(req);
                JahiaIndexSearcher jahiaSearcher = req.getSearcher();
                if (filterQueries != null && filterQueries.length > 0) {
                    if (filter != null || filterQueries.length > 1) {
                        Filter[] filterArray = new Filter[filterQueries.length + (filter != null ? 1 : 0)];
                        int i = 0;
                        for (int size = filterQueries.length; i < size; i++) {
                            filterArray[i] = jahiaSearcher.getFilter(queryParser.parse(filterQueries[i]));
                        }
                        if (filter != null) {
                            filterArray[i] = filter;
                        }
                        filter = new ChainedFilter(filterArray, ChainedFilterType.AND);
                    } else {
                        filter = jahiaSearcher.getFilter(queryParser.parse(filterQueries[0]));
                    }
                }
                searcher = jahiaSearcher;
                reader = jahiaSearcher.getReader();
            }
            
            Query q = null;
            if (query != null && query.length() > 0) {
                if (languageCodes.size() > 1) {
                    Map<Analyzer, Set<String>> analyzersToUse = new HashMap<Analyzer, Set<String>>();
                    for (String languageCode : languageCodes) {
                        Analyzer languageAnalyzer = ContentObject.SHARED_LANGUAGE.equals(languageCode) ? null : getAnalyzerForLanguage(languageCode);
                        if (languageAnalyzer == null) {
                            languageAnalyzer = analyzer;
                        }

                        Set<String> languages = analyzersToUse
                                .get(languageAnalyzer);
                        if (languages == null) {
                            languages = new HashSet<String>();
                        }
                        languages.add(languageCode);
                        analyzersToUse.put(languageAnalyzer, languages);
                    }
                    if (analyzersToUse.size() == 1 && analyzersToUse.keySet().iterator().next().equals(analyzer)) {
                        q = queryParser.parse(query);
                    } else {
                        // If search is done across several languages and if there are different
                        // analyzers configured per language, then also the query need to use
                        // different parsers with all the different analyzers
                        Query[] queries = new Query[analyzersToUse.size()];
                        int i = 0;
                        for (Map.Entry<Analyzer, Set<String>> languageAnalyzerMapping : analyzersToUse.entrySet()) {
                            StringBuffer queryBuffer = new StringBuffer(query.length() + 20);
                            queryBuffer.append(JahiaSearchConstant.LANGUAGE_CODE).append(":(");
                            StringBuffer languageCodeBuffer = new StringBuffer(languageAnalyzerMapping.getValue().size() * 3);
                            for (String languageCode : languageAnalyzerMapping.getValue()) {
                                if (languageCodeBuffer.length() > 0) {
                                    languageCodeBuffer.append(" ");
                                }
                                languageCodeBuffer.append(languageCode);
                            }
                            queryBuffer.append(languageCodeBuffer).append(" AND (").append(query).append(")");
                            if (languageAnalyzerMapping.getKey().equals(analyzer)) {
                                queries[i] = queryParser.parse(queryBuffer.toString());
                            } else {
                                QueryParser languageQueryParser = new JahiaLuceneQueryParser(
                                        getAllSearchFieldName(), ServicesRegistry.getInstance().
                                        getJahiaSearchService().getFieldsGrouping(), this.getSiteId(),
                                        languageAnalyzerMapping.getKey(), resourceMapping);
                                languageQueryParser.setAllowLeadingWildcard(true);
                                queries[i] = languageQueryParser.parse(queryBuffer.toString());
                            }
                            i++;
                        }
                        q = Query.mergeBooleanQueries(queries);
                    }
                } else {
                    q = queryParser.parse(query);
                }    
            } else {
                q = new ConstantScoreQuery(filter);
                filter = null;
            }
            
            SearchResult result = null;
            if (hitCollector != null) {
                hitCollector.setSearcher(searcher);
                searcher.search(q, filter, hitCollector);
                result = hitCollector.getSearchResult(q);
                if (logger.isDebugEnabled()) {
                    logger.debug("Number of results:" + result.results().size());
                }                    
                hitCollector.clear();
                collector.results().addAll(result.results());
            } else {
                Hits hits = searcher.search(q, filter, sort);
                if (logger.isDebugEnabled()) {
                    logger.debug("Number of results:" + hits.length());
                }                
                getSearchResult(collector, hits, q, reader);
            }
        } catch (Exception t) {
            logger.warn("Exception occured when performing search", t);
        } finally {
            // Removed as releasing references to opened lucene query request will be done
            // in finally clause of Jahia.service() method
            /*
            if (req != null) {
                req.close();
            }*/

            if (closeSearcher && searcher != null) {
                JahiaSearchBaseService.addReferenceToOpenSearcher(searcher);
                // Removed as releasing references to opened searcher will be done
                // in finally clause of Jahia.service() method
                /*
                try {
                    searcher.close();
                } catch (IOException e) {
                    logger.warn("cannot close IndexSearcher", e);
                }*/

            }
        }
    }
    
    private Analyzer getAnalyzerForLanguage(String languageCode) {
        int index = languageCode.indexOf('_');
        if (index != -1) {
            languageCode = languageCode.substring(0, index);
        }
        return getAnalyzer(languageCode);
    }

    public void notifyIndexUpdate() {
        try {
            coreSearcher.getSearcher(true, false, null);
        } catch (Exception e) {
            logger.warn("Failed to reopen index searcher/reader", e);
        }
        ClusterService clusterService = ServicesRegistry.getInstance()
                .getClusterService();
        SearchClusterMessage msg = new SearchClusterMessage(new IndexUpdatedMessage(
                clusterService.getServerId(), getName()));
        clusterService.sendMessage(msg);
    }

    public void shutdown() {
        if (this.getIndexer() != null) {
            this.getIndexer().shutdown();
        }
        this.getCoreSearcher().close();
    }

    /**
     * @param collector
     * @param hits
     * @param query
     * @param reader
     * @return
     * @throws Exception
     */
    protected void getSearchResult(SearchResult collector, Hits hits, Query query, IndexReader reader)
            throws Exception {

        InternalCompass internalCompass = null;
        LuceneSearchEngine searchEngine = null;
        Compass compass = ServicesRegistry.getInstance().getJahiaSearchService().getCompass();

        if (compass != null && compass instanceof InternalCompass) {
            internalCompass = (InternalCompass) compass;
            SearchEngine se = internalCompass.getSearchEngineFactory().openSearchEngine(new RuntimeCompassSettings(internalCompass.getSettings()));
            if (se instanceof LuceneSearchEngine) {
                searchEngine = (LuceneSearchEngine) se;
            }
        }

        if (hits == null || hits.length() == 0) {
            return;
        }
        
        SearchResult searchResult = searchEngine != null ? new LuceneSearchResult(
                reader, query, searchEngine)
                : new SearchResultImpl(false);    

        Set<String> distinctObjectKeys = new HashSet<String>();
        String objectKey = null;
        for (int i = 0, size = hits.length(); i < size; i++) {
            Document doc = reader.document(hits.id(i), getFieldSelector());

            try {
                objectKey = doc.getFieldable(JahiaSearchConstant.OBJECT_KEY).stringValue();
            } catch (Exception t) {
                continue;
            }
            LuceneSearchHit searchHit = new LuceneSearchHit(doc);
            Map<String, List<Object>> fieldsMap = new HashMap<String, List<Object>>();
            searchHit.setSearchResult(searchResult);
            searchHit.setScore(hits.score(i));
            searchHit.setDocNumber(i);
            
            for (Iterator<?> it = doc.getFields().iterator(); it.hasNext();) {
                Fieldable field = (Fieldable)it.next();
                String name = field.name();
                List<Object> list = fieldsMap.get(name);
                if (list == null) {
                    list = new ArrayList<Object>();
                    fieldsMap.put(name, list);
                }
                if ( !field.isBinary() && field instanceof Field ){ // by testing for Field instance, we avoid loading
                                                                    // value from all lazy Field
                    list.add(field.stringValue());
                }
            }
            searchHit.setFields(fieldsMap);
            if (distinctObjectKeys.contains(objectKey)) {
                continue;
            } else {
                distinctObjectKeys.add(objectKey);
            }
            if (!collector.add(searchHit)) {
                break;
            }
        }
    }

    public FieldSelector getFieldSelector() {
        return fieldSelector;
    }

    public void setFieldSelector(FieldSelector fieldSelector) {
        this.fieldSelector = fieldSelector;
    }

    public SearchIndexer getIndexer() {
        return coreSearcher.getIndexer();
    }

    public LuceneCoreSearcher getCoreSearcher() {
        return coreSearcher;
    }

    public Iterator<String> getTerms(final String query) {
        try {
            final String finalQuery;
            if (query.endsWith("*")) {
                finalQuery = query;
            } else {
                finalQuery = query + "*";
            }
            final Term queryTerm = new Term(JahiaSearchConstant.CONTENT_FULLTEXT_SEARCH_FIELD, finalQuery);
            final WildcardTermEnum results = new WildcardTermEnum(coreSearcher.getIndexReader(), queryTerm);
            final List<String> result = new ArrayList<String>();
            while (results.next()) {
                final Term t = results.term();
                result.add(t.text());
            }
            return result.iterator();
        } catch (Exception e) {
            logger.error("Error while fetching terms", e);
        }
        return null;
    }
}
