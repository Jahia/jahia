/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.search;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Represents a spell checker corrected query (suggestion) that can be used to
 * implement "Did you mean" kind of searches.
 *
 * @author Sergiy Shyrkov
 * @since 6.5
 */
public class Suggestion {

    private String originalQuery;

    private String suggestedQuery;

    private List<String> allSuggestions;

    /**
     * Initializes an instance of this class.
     *
     * @param originalQuery
     * @param suggestedQuery
     */
    public Suggestion(String originalQuery, String suggestedQuery) {
        super();
        this.originalQuery = originalQuery;
        this.suggestedQuery = suggestedQuery;
    }

    /**
     * Initializes an instance of this class.
     *
     * @param originalQuery
     * @param suggestedQuery
     * @param allSuggestions
     */
    public Suggestion(String originalQuery, String suggestedQuery, List<String> allSuggestions) {
        super();
        this.originalQuery = originalQuery;
        this.suggestedQuery = suggestedQuery;
        this.allSuggestions = allSuggestions;
    }

    /**
     * Returns the original query string.
     *
     * @return the original query string
     */
    public String getOriginalQuery() {
        return originalQuery;
    }

    /**
     * Returns the suggested query string.
     *
     * @return the suggested query string
     */
    public String getSuggestedQuery() {
        return suggestedQuery;
    }

    /**
     * Returns a list of suggested query terms.
     *
     * @return a list of suggested query terms
     */
    public List<String> getAllSuggestions() {
        return allSuggestions;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public void setSuggestedQuery(String suggestedQuery) {
        this.suggestedQuery = suggestedQuery;
    }
}
