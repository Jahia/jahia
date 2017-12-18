/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.usermanager;

import java.io.Serializable;

/**
 * Criteria bean for group search.
 * 
 * @author Sergiy Shyrkov
 */
public class SearchCriteria implements Serializable {

    private static final long serialVersionUID = -1430066500087382606L;

    private String[] properties;

    private String[] providers;

    private String searchIn;

    private String searchString;

    private String siteKey;

    private String storedOn;

    public SearchCriteria() {
        super();
    }

    public SearchCriteria(String siteKey) {
        this();
        this.siteKey = siteKey;
    }

    public String[] getProperties() {
        return properties;
    }

    public String[] getProviders() {
        return providers;
    }

    public String getSearchIn() {
        return searchIn;
    }

    public String getSearchString() {
        return searchString;
    }

    public String getSiteKey() {
        return siteKey;
    }

    public String getStoredOn() {
        return storedOn;
    }

    public void setProperties(String[] properties) {
        this.properties = properties;
    }

    public void setProviders(String[] providers) {
        this.providers = providers;
    }

    public void setSearchIn(String searchIn) {
        this.searchIn = searchIn;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public void setSiteKey(String siteKey) {
        this.siteKey = siteKey;
    }

    public void setStoredOn(String storedOn) {
        this.storedOn = storedOn;
    }
}