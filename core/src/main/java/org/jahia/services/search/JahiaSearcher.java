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
