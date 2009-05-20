/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
//
//
//

package org.jahia.services.search;

import org.jahia.data.search.JahiaSearchResult;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;

import java.util.List;


/**
 * Jahia Searcher interface.
 *
 * @author Khue Nguyen <a href="mailto:khue@jahia.org">khue@jahia.org</a>
 */

public abstract class JahiaSearcher {

    /** The last performed query. */
    private String query;

    /** The last result. */
    private Object result;

    //--------------------------------------------------------------------------
    /**
     * Perform the search for a given query as String.
     * Return the results as a Object instance.
     *
     * @param query a valid query.
     * @param jParams the param bean.
     *
     * @return JahiaSearchResult result.
     */
    public abstract JahiaSearchResult search (String query, ProcessingContext jParams)
            throws JahiaException;

    //--------------------------------------------------------------------------
    /**
     * Return the last query performed by search(query) method call.
     *
     * @return Object the result, the result is an instance of JahiaSearchResult object.
     */
    public String getQuery () {
        return this.query;
    }

    //--------------------------------------------------------------------------
    /**
     * Set the last performed query.
     *
     * @param query the last performed query.
     */
    public void setQuery (String query) {
        this.query = query;
    }

    //--------------------------------------------------------------------------
    /**
     * Return the results returned by the last search(query) method call.
     *
     * @return Object the result, the result is an instance of JahiaSearchResult object.
     */
    public Object getResult () {
        return this.result;
    }

    //--------------------------------------------------------------------------
    /**
     * Set the result.
     *
     * @param result the result of the search result.
     */
    void setResult (Object result) {
        this.result = result;
    }

    //--------------------------------------------------------------------------
    /**
     * @return the list of languages code to search.
     */
    public abstract List<String> getLanguageCodes();


}
