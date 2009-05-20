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
