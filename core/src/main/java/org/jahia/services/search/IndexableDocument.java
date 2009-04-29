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
import java.util.List;
import java.io.Serializable;

/**
 *
 */
public interface IndexableDocument extends Serializable {

    /**
     * return the unique key identifier
     *
     * @return
     */
    public abstract String getKey ();

    /**
     * return the name of the key field
     *
     * @return
     */
    public abstract String getKeyFieldName ();

    /**
     * Set the unique key identifier
     *
     * @param key
     */
    public abstract void setKey (String key);

    /**
     * Set the name of the key field
     *
     * @param keyFieldName
     */
    public abstract void setKeyFieldName (String keyFieldName);

    /**
     * Return a map of key/value pair. The key is the field name, the value is a DocumentField
     *
     * @return
     */
    public abstract Map<String, DocumentField> getFields ();

    public abstract DocumentField getField(String name);

    /**
     * Add single value field
     *
     * @param name
     * @param val
     */
    public abstract void addFieldValue (String name, String val);

    /**
     * Add single value field
     *
     * @param name
     * @param vals
     */
    public abstract void addFieldValues (String name, String[] vals);

    /**
     * Set single value field
     *
     * @param name
     * @param val
     */
    public abstract void setFieldValue (String name, String val);

    /**
     * Set a multi-values field
     *
     * @param name
     * @param vals
     */
    public abstract void setFieldValues (String name, String[] vals);

    /**
     * Remove a field
     */
    public abstract void removeField (String name);

    public abstract List<Integer> getChildIndexableDocuments();
    
}