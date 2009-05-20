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
public interface ParsedObject {

    /**
     * Returns the associated searchHit
     *
     * @return float
     */
    public abstract SearchHit getSearchHit();

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
     * Return an hastable of fieldname/values pair of information as they were
     * stored by the search engine
     *
     * the key is a String and the values is an array of string values
     *
     * @return
     */
    public abstract Map<String, String[]> getFields ();

    /**
     * Return an array of value for the given field
     * @param fieldName String
     * @return String[]
     */
    public abstract String[] getValues(String fieldName);

    /**
     * Return the first value for the given field
     * @return String
     */
    public abstract String getValue(String fieldName);

    /**
     * Return the first value for the given field and load
     * the field first, if it is not loaded. Notice that lazy
     * loading will only work if the same IndexReader is still
     * open.
     * @return String
     */
    public abstract String getLazyFieldValue(String fieldName);    
}
