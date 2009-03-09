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

import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 15 f�vr. 2005
 * Time: 16:32:40
 * To change this template use File | Settings | File Templates.
 */
public class DocumentField implements Serializable {

    private static final long serialVersionUID = 5080427507953799172L;
    public static final int TEXT = 0;
    public static final int KEYWORD = 1;
    public static final int UNINDEXED = 2;
    
    private boolean unstored = false;

    private String name;
    private List<String> values = new ArrayList<String>();

    private int type = TEXT;

    private float weight = 1.0f;

    public DocumentField(String name){
        this.name = name;
        if ( isUnstoredText(name) ){
            setUnstored(true);
        }
    }

    public DocumentField(String name, List<String> values){
        this(name);
        if ( values != null ){
            this.values = values;
        }
    }

    private boolean isUnstoredText(String fieldName){
        if ( JahiaSearchConstant.ALL_FULLTEXT_SEARCH_FIELD.equals(fieldName)
                || JahiaSearchConstant.ALL_FULLTEXT_SEARCH_FIELD_FOR_QUERY_REWRITE.equals(fieldName)
                || JahiaSearchConstant.CONTENT_FULLTEXT_SEARCH_FIELD.equals(fieldName)
                || JahiaSearchConstant.FILE_CONTENT_FULLTEXT_SEARCH_FIELD.equals(fieldName)
                || JahiaSearchConstant.CONTENT_FULLTEXT_SEARCH_FIELD_FOR_QUERY_REWRITE.equals(fieldName)
                || JahiaSearchConstant.METADATA_FULLTEXT_SEARCH_FIELD_FOR_QUERY_REWRITE.equals(fieldName)
                || JahiaSearchConstant.METADATA_FULLTEXT_SEARCH_FIELD.equals(fieldName) 
                || fieldName.startsWith(JahiaSearchConstant.CONTAINER_FIELD_SORT_PREFIX)
                || fieldName.startsWith(JahiaSearchConstant.CONTAINER_FIELD_FACET_PREFIX)){
            return true;
        }
        return false;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void setUnstored(boolean unstored) {
        this.unstored = unstored;
    }    

    public List<String> getValues() {
        synchronized(values){
            if ( this.values == null ){
                this.values = new ArrayList<String>();
            }
            return values;
        }
    }

    public void setValues(List<String> values) {
        synchronized(values){
            this.values = values;
        }
    }

    public void addValue(String value){
        if ( value != null ){
            this.getValues().add(value);
        }
    }

    public void clearValue(){
        synchronized(values){
            this.values = new ArrayList<String>();
        }
    }

    public boolean isText() {
        return ( type == TEXT);
    }

    public boolean isKeyword() {
        return ( type == KEYWORD);
    }

    public boolean isUnstored() {
        return unstored;
    }

    public boolean isUnindexed() {
        return ( type == UNINDEXED);
    }

}
