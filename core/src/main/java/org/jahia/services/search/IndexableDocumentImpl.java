/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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