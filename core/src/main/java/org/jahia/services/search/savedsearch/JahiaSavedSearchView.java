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
package org.jahia.services.search.savedsearch;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Saved search view bean.
 * 
 * @author Sergiy Shyrkov
 */
public class JahiaSavedSearchView {

    private String contextId;

    private String name;

    private Integer savedSearchId;

    private Integer searchMode;

    private JahiaSavedSearchViewSettings settings;

    private String userKey;

    /**
     * Initializes an instance of this class.
     * 
     * @param searchMode
     * @param savedSearchId
     * @param contextId
     * @param userKey
     */
    public JahiaSavedSearchView(Integer searchMode, Integer savedSearchId,
            String contextId, String userKey) {
        super();
        this.searchMode = searchMode;
        this.savedSearchId = savedSearchId;
        this.contextId = contextId;
        this.userKey = userKey;
    }

    public String getContextId() {
        return contextId;
    }

    public String getHashCodeAsString(){
        return Integer.toString(this.hashCode());
    }

    public String getName() {
        return name;
    }

    public Integer getSavedSearchId() {
        return savedSearchId;
    }

    public Integer getSearchMode() {
        return searchMode;
    }

    public JahiaSavedSearchViewSettings getSettings() {
        return settings;
    }

    public String getUserKey() {
        return userKey;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append("searchMode").append(getSearchMode())
                .append("savedSearchId").append(getSavedSearchId())
                .append("contextId").append(getContextId())
                .append("userKey").append(getUserKey())
                .toHashCode();
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSavedSearchId(Integer savedSearchId) {
        this.savedSearchId = savedSearchId;
    }

    public void setSearchMode(Integer searchMode) {
        this.searchMode = searchMode;
    }
    
    public void setSettings(JahiaSavedSearchViewSettings settings) {
        this.settings = settings;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }
}
