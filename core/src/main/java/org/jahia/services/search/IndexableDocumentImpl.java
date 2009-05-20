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

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 *
 */
public class IndexableDocumentImpl implements IndexableDocument {

    private static final long serialVersionUID = 8939611071639730994L;

    private String key;

    private String keyFieldName;

    private Map<String, DocumentField> fields = new HashMap<String, DocumentField>();

    private List<Integer> childIndexableDocuments = new ArrayList<Integer>();

    public IndexableDocumentImpl(){

    }

    public IndexableDocumentImpl(String keyFieldName,
                                 String key){
        this.keyFieldName = keyFieldName;
        this.key = key;
    }

    /**
     * return the unique key identifier
     *
     * @return key
     */
    public String getKey (){
        return this.key;
    }

    /**
     * Set the unique key identifier
     *
     * @param key
     */
    public void setKey (String key){
        this.key = key;
    }

    public String getKeyFieldName() {
        return keyFieldName;
    }

    public void setKeyFieldName(String keyFieldName) {
        this.keyFieldName = keyFieldName;
    }

    /**
     * Return a map of key/value pair. The key is the field name, the value is a DocumentField
     *
     * @return fields
     */
    public Map<String, DocumentField> getFields (){
        synchronized(fields){
            if ( fields == null ){
                fields = new HashMap<String, DocumentField>();
            }
            return this.fields;
        }
    }

    public DocumentField getField(String name){
        return this.getFields().get(name);
    }

    /**
     * Add single value field
     *
     * @param name
     * @param val
     */
    public void addFieldValue (String name, String val){
        if ( name == null || val == null ){
            return;
        }
        DocumentField docField  = this.getField(name);
        if ( docField == null ){
            docField = new DocumentField(name);
            this.fields.put(name,docField);
        }
        docField.addValue(val);
    }

    /**
     * Add field values
     *
     * @param name
     * @param vals
     */
    public void addFieldValues (String name, String[] vals){
        if ( name == null ){
            return;
        }
        for ( int i=0; i<vals.length; i++ ){
            this.addFieldValue(name,(String)vals[i]);
        }
    }

    /**
     * Set single value field
     *
     * @param name
     * @param val
     */
    public void setFieldValue (String name, String val){
        if ( name == null ){
            return;
        }
        DocumentField docField = this.getField(name);
        if ( docField != null ){
            docField.clearValue();
        }
        if ( val != null ){
            this.addFieldValue(name,val);
        }
    }

    /**
     * Set a multi-values field
     *
     * @param name
     * @param vals
     */
    public void setFieldValues (String name, String[] vals){
        if ( name == null ){
            return;
        }
        DocumentField docField = this.getField(name);
        if ( docField != null ){
            docField.clearValue();
        }
        if ( vals != null ){
            this.addFieldValues(name,vals);
        }
    }

    /**
     * Remove a field
     */
    public void removeField (String name){
        if ( name != null ){
            this.getFields().remove(name);
        }
    }

    public List<Integer> getChildIndexableDocuments() {
        return childIndexableDocuments;
    }

    public void setChildIndexableDocuments(List<Integer> childIndexableDocuments) {
        this.childIndexableDocuments = childIndexableDocuments;
    }
    
}