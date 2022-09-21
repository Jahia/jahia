/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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