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
