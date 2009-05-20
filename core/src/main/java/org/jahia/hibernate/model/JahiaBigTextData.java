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
 package org.jahia.hibernate.model;

import java.io.Serializable;

/**
 * @hibernate.class table="jahia_bigtext_data"
 * @hibernate.cache usage="nonstrict-read-write"
 */
public class JahiaBigTextData implements Serializable {
    private String id;
    private String rawValues;

    public JahiaBigTextData() {
    }

    public JahiaBigTextData(String id, String rawValues) {
        this.id = id;
        this.rawValues = rawValues;
    }

    /**
     * @hibernate.id generator-class="assigned"
     * column="id_bigtext_data"
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * @hibernate.property column="raw_value" type="text" length="16777216"
     */
    public String getRawValues() {
        return rawValues;
    }

    public void setRawValues(String rawValues) {
        this.rawValues = rawValues;
    }
}
