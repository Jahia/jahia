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
public class SearchHitImpl implements SearchHit {

    private float score;
    private Object rawHit;
    private Map<String, List<Object>> fields;
    private SearchResult searchResult;

    public SearchHitImpl(Object rawHit){
        super();
        fields = new HashMap<String, List<Object>>();
        this.rawHit = rawHit;
    }

    public SearchHitImpl(float score, Map<String, List<Object>> fields){
        this.score = score;
        this.fields = fields;
        if ( this.fields == null ){
            fields = new HashMap<String, List<Object>>();
        }
    }

    /**
     * Returns the raw representation of the wrapped search Hit
     *
     * @return
     */
    public Object getRawHit(){
        return this.rawHit;        
    }

    /**
     * Returns the score
     *
     * @return float
     */
    public float getScore(){
        return this.score;
    }

    /**
     * Set the score
     *
     * @param score float
     */
    public void setScore(float score){
        this.score = score;
    }

    /**
     * Return an hastable of fieldname/values pair of information as they were
     * stored
     *
     * the key is the field name and the values is a List of string values
     *
     * @return
     */
    public Map<String, List<Object>> getFields (){
        if ( this.fields == null ){
            this.fields = new HashMap<String, List<Object>>();
        }
        return this.fields;
    }

    public void setFields (Map<String, List<Object>> fields){
        this.fields = fields;
    }

    /**
     * Return a List of values for the given field
     * @param fieldName String
     * @return List list of String
     */
    public List<Object> getValues(String fieldName){
        List<Object> values = new ArrayList<Object>();
        if ( fieldName != null ){
            values = getFields().get(fieldName);
        }
        if ( values == null ){
            values = Collections.emptyList();
        }
        return values;
    }

    /**
     * Return the first value available for the given field
     * @return String
     */
    public String getValue(String fieldName){
        List<Object> values = getValues(fieldName);
        if ( values.size() == 0 ){
            return "";
        }
        String value = (String)values.get(0);
        if ( value == null ) {
            return "";
        }
        return value;
    }

    /**
     * Returns the associated searchResult
     *
     * @return
     */
    public SearchResult getSearchResult(){
        return this.searchResult;
    }

    /**
     * Set the associated search result
     */
    public void setSearchResult(SearchResult searchResult){
        this.searchResult = searchResult;
    }

    /**
     * Returns the highlighter for this hit.
     *
     * @return The highlighter.
     */
    public CompassHighlighter highlighter(){
        CompassHighlighter highlighter = null;
        if ( this.searchResult != null ){
            highlighter = this.searchResult.highlighter(this);
        }
        return highlighter;
    }

}
