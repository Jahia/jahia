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

 package org.jahia.services.search;

import org.compass.core.CompassHighlighter;

import java.util.*;

/**
 * <p>Title: Contains information returned by the search result as a map
 *           of fieldname/values pair.</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 *
 * @author Khue Nguyen
 * @version 1.0
 */
public interface SearchHit {

    /**
     * Returns the raw representation of the wrapped search Hit
     *
     * @return
     */
    public abstract Object getRawHit();

    /**
     * Returns the score
     *
     * @return float
     */
    public abstract float getScore();

    /**
     * Set the score
     *
     * @param score float
     */
    public abstract void setScore(float score);

    /**
     * Returns the excerpt
     *
     * @return String
     */
    public abstract String getExcerpt();

    /**
     * Set the excerpt
     *
     * @param excerpt String
     */
    public abstract void setExcerpt(String excerpt);        
    
    /**
     * Return an hastable of fieldname/values pair of information as they were
     * stored
     *
     * the key is a String and the values is an array of string values
     *
     * @return
     */
    public abstract Map<String, List<Object>> getFields ();

    /**
     * Return an array of value for the given field
     * @param fieldName String
     * @return List of FieldValue
     */
    public abstract List<Object> getValues(String fieldName);

    /**
     * Return the first value for the given field
     *
     * @param fieldName
     * @return
     */
    public abstract String getValue(String fieldName);

    /**
     * Returns the associated searchResult
     *
     * @return
     */
    public abstract SearchResult getSearchResult();

    /**
     * Returns the associated searchResult
     *
     * @return
     */
    public abstract void setSearchResult(SearchResult searchResult);

    /**
     * Returns the highlighter for this hit.
     *
     * @return The highlighter.
     * @throws org.compassframework.core.CompassException
     */
    public abstract CompassHighlighter highlighter();

}
