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
package org.jahia.hibernate.model.jahiasavedsearch;

import org.jahia.hibernate.model.CachedPK;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * @author Hibernate CodeGenerator
 */
public class JahiaSavedSearchViewPK extends CachedPK implements Serializable {

    /**
     *
     */
    private Integer searchMode;

    /**
     *
     */
    private Integer savedSearchId;

    /**
     *
     */
    private String contextId;

    /**
     *
     */
    private String userKey;

    /**
     * full constructor
     */
    public JahiaSavedSearchViewPK(Integer searchMode,
                                  Integer savedSearchId,
                                  String contextId,
                                  String userKey) {
        this.searchMode = searchMode;
        this.savedSearchId = savedSearchId;
        this.contextId = contextId;
        this.userKey = userKey;
        if ( this.userKey != null &&
                this.userKey.startsWith(JahiaUserManagerService.GUEST_USERNAME+":")){
            // guest user is equivalent to no user at all
            this.userKey = "";
        }
    }

    /**
     * default constructor
     */
    public JahiaSavedSearchViewPK() {
    }

    /**
     * @hibernate.property column="smode_savedsearchview"
     */
    public Integer getSearchMode() {
        return this.searchMode;
    }

    public void setSearchMode(Integer searchMode) {
        updated();
        this.searchMode = searchMode;
    }

    /**
     * @hibernate.property column="ctnid_savedsearchview"
     * length="100"
     */
    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    /**
     * @hibernate.property column="searchid_savedsearchview"
     */
    public Integer getSavedSearchId() {
        return savedSearchId;
    }

    public void setSavedSearchId(Integer saveSearchId) {
        this.savedSearchId = saveSearchId;
    }

    /**
     * @hibernate.property column="userkey_savedsearchview"
     * length="200"
     */
    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
        if ( this.userKey != null &&
                this.userKey.startsWith(JahiaUserManagerService.GUEST_USERNAME+":")){
            // guest user is equivalent to no user at all
            this.userKey = "";
        }
    }

    public String effectiveToString() {
        return new ToStringBuilder(this)
            .append("searchMode", getSearchMode())
            .append("savedSearchId", getSavedSearchId())
            .append("contextId", getContextId())
            .append("userKey", getUserKey())
            .toString();
    }

    public String getHashCodeAsString(){
        return String.valueOf(this.hashCode());
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaSavedSearchViewPK castOther = (JahiaSavedSearchViewPK) obj;
            return new EqualsBuilder()
                .append(this.getSearchMode(), castOther.getSearchMode())
                .append(this.getSavedSearchId(), castOther.getSavedSearchId())
                .append(this.getContextId(), castOther.getContextId())
                .append(this.getUserKey(), castOther.getUserKey())
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append("searchMode").append(getSearchMode())
                .append("savedSearchId").append(getSavedSearchId())
                .append("contextId").append(getContextId())
                .append("userKey").append(getUserKey())
                .toHashCode();
    }

    /**
     *
     * @param searchMode
     * @param savedSearchId
     * @param contextId
     * @param userKey
     * @return
     */
    public static JahiaSavedSearchViewPK getInstance(Integer searchMode, Integer savedSearchId,
                                                     String contextId, String userKey){
        if ( searchMode == null ){
            searchMode = new Integer(0);
        }
        if ( savedSearchId == null ){
            savedSearchId = new Integer(0);
        }
        if ( contextId == null ){
            contextId = "";
        }
        if ( userKey == null ){
            userKey = "";
        }
        return new JahiaSavedSearchViewPK(searchMode,savedSearchId,contextId,userKey);
    }
    
    @Override
    public String toString() {
        return ReflectionToStringBuilder.reflectionToString(this);
    }
}
