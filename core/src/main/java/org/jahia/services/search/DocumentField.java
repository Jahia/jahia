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
                || JahiaSearchConstant.METADATA_FULLTEXT_SEARCH_FIELD.equals(fieldName)){
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
