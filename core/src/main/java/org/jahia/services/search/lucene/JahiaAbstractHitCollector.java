package org.jahia.services.search.lucene;

import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.search.JahiaSearchResultBuilder;
import org.jahia.services.search.SearchResult;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 2 mars 2007
 * Time: 17:59:31
 * To change this template use File | Settings | File Templates.
 */
public abstract class JahiaAbstractHitCollector extends HitCollector {

    protected Searcher searcher = null;
    protected JahiaSearchResultBuilder searchResultBuilder;

    protected Query query = null;

    public abstract void collect(int doc, float score);

    public abstract SearchResult getSearchResult(Query q) throws JahiaException ;

    public Searcher getSearcher() {
        return searcher;
    }

    public void setSearcher(Searcher searcher) {
        this.searcher = searcher;
    }

    public JahiaSearchResultBuilder getSearchResultBuilder() {
        return searchResultBuilder;
    }

    public void setSearchResultBuilder(JahiaSearchResultBuilder searchResultBuilder) {
        this.searchResultBuilder = searchResultBuilder;
    }

    /**
     * Query used for highlighting
     * @return
     */
    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    /**
     * clear internal search results to free memory
     */
    public abstract void clear();


}
