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
public class SearchHitImpl implements SearchHit {

    private float score;
    private Object rawHit;
    private Map<String, List<Object>> fields;
    private SearchResult searchResult;
    private String excerpt;

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

    public String getExcerpt() {
        return excerpt;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }    
}
