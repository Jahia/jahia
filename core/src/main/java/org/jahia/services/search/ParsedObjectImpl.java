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

import org.apache.commons.lang.ArrayUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;

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
public class ParsedObjectImpl implements ParsedObject {

    private SearchHit searchHit;
    private float score;
    private Map<String, String[]> fields;

    public ParsedObjectImpl(SearchHit searchHit){
        this.searchHit = searchHit;
        fields = new HashMap<String, String[]>();
    }

    public ParsedObjectImpl(SearchHit searchHit, float score, Map<String, String[]> fields){
        this.searchHit = searchHit;
        this.score = score;
        this.fields = fields;
        if ( this.fields == null ){
            fields = new HashMap<String, String[]>();
        }
    }

    /**
     * Returns the associated searchHit
     *
     * @return float
     */
    public SearchHit getSearchHit(){
        return searchHit;
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
     * Return an hashtable of fieldname/values pair of information as they were
     * stored through the search service
     *
     * the key is the field name and the values is an array of string values
     *
     * @return
     */
    public Map<String, String[]> getFields (){
        if ( this.fields == null ){
            this.fields = new HashMap<String, String[]>();
        }
        return this.fields;
    }

    public void setFields (Map<String, String[]> fields){
        this.fields = fields;
    }

    /**
     * Return an array of value for the given field
     * @param fieldName String
     * @return String[]
     */
    public String[] getValues(String fieldName){
        String[] values = new String[0];
        if ( fieldName != null ){
            values = (String[])getFields().get(fieldName);
        }
        if ( values == null ){
            values = ArrayUtils.EMPTY_STRING_ARRAY;
        }
        return values;
    }

    /**
     * Return the first value available for the given field
     * @return String
     */
    public String getValue(String fieldName){
        String[] values = getValues(fieldName);
        if ( values.length == 0 ){
            return "";
        }
        return values[0];
    }

    /**
     * Return the first value available for the given field
     * @return String
     */
    public String getLazyFieldValue(String fieldName){
        String[] values = (String[])getFields().get(fieldName);
        if (values == null || values.length == 0) {
            Fieldable field = ((Document) getSearchHit().getRawHit())
                    .getFieldable(fieldName);
            String fieldValue = "";
            if (field != null) {
                fieldValue = field.stringValue();
            }
            values = new String[] { fieldValue };
            getFields().put(fieldName, values);
        } 
        return values[0];
    }    
    
}
