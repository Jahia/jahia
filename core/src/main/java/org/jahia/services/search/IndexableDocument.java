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