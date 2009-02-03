/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
