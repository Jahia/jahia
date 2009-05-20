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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 25 juin 2007
 * Time: 11:51:26
 * To change this template use File | Settings | File Templates.
 */
public class JahiaLanguagesStatesPK extends CachedPK implements Serializable {
    /**
     * identifier field
     */
    private String objectkey;

    private String languageCode;


    public JahiaLanguagesStatesPK(String objectkey, String languageCode) {
        this.objectkey = objectkey;
        this.languageCode = languageCode;
    }

    public JahiaLanguagesStatesPK() {
    }

    /**
     * @hibernate.property column="objectkey" type="java.lang.String" length="40"
     */
    public String getObjectkey() {
        return objectkey;
    }

    public void setObjectkey(String objectkey) {
        this.objectkey = objectkey;
    }

    /**
     * @hibernate.property column="language_code" type="java.lang.String" length="10"
     */
    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String effectiveToString() {
        return new ToStringBuilder(this)
                .append("objectKey", getObjectkey())
                .append("languageCode", getLanguageCode())
                .toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaLanguagesStatesPK castOther = (JahiaLanguagesStatesPK) obj;
            return new EqualsBuilder()
                .append(this.getObjectkey(), castOther.getObjectkey())
                .append(this.getLanguageCode(), castOther.getLanguageCode())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getObjectkey())
                .append(getLanguageCode())
                .toHashCode();
    }

}