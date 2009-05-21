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