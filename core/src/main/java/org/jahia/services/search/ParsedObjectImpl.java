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
