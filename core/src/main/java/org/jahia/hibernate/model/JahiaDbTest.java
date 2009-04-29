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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * @hibernate.class table="jahia_db_test"
 */
public class JahiaDbTest implements Serializable {

    /**
     * identifier field
     */
    private String testfield;

    /**
     * full constructor
     */
    public JahiaDbTest(String testfield) {
        this.testfield = testfield;
    }

    /**
     * default constructor
     */
    public JahiaDbTest() {
    }

    /**
     * @hibernate.id generator-class="assigned"
     * type="java.lang.String"
     * column="testfield"
     */
    public String getTestfield() {
        return this.testfield;
    }

    public void setTestfield(String testfield) {
        this.testfield = testfield;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("testfield", getTestfield())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        
        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaDbTest castOther = (JahiaDbTest) obj;
            return new EqualsBuilder()
                .append(this.getTestfield(), castOther.getTestfield())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getTestfield())
                .toHashCode();
    }

}
