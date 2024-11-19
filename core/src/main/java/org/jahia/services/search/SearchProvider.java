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

import org.jahia.services.render.RenderContext;

/**
 * Interface for different provider implementations used by the SearchService.
 *
 * @author Benjamin Papez
 *
 */
public interface SearchProvider {
    /**
     * Performs a search using the SearchCriteria object, which is created with using the
     * Jahia Search Tags or can also be created in Java classes by using the
     * SearchCriteriaFactory.
     *
     * @param criteria the passed SearchCriteria object, which includes all search criteria
     * @param context context object, containing information about current user, locale etc.
     * @return SearchResponse object with the list of hits matching the criteria
     */
    SearchResponse search(SearchCriteria criteria, RenderContext context);

    /**
     * Returns a modified suggestion for the original query based on the spell
     * checker dictionary. If the spelling is correct or the spell checker does
     * not know how to correct the query or the provider does not support this
     * feature <code>null</code> is returned.
     *
     * @param originalQuery original query string
     * @param context context object, containing information about current site, workspace, locale etc.
     * @param maxTermsToSuggest the maximum number of terms to suggest
     * @return a modified suggestion for the original query based on the spell
     *         checker dictionary. If the spelling is correct or the spell
     *         checker does not know how to correct the query <code>null</code>
     *         is returned.
     *
     * @deprecated Use SupportsSuggestion interface instead
     *
     */
    Suggestion suggest(String originalQuery, RenderContext context, int maxTermsToSuggest);

    String getName();

    /**
     * Checks whether this provider is enabled. This can be used to perform licensing checks for example.
     *
     * @return <code>true</code> if the provider is enabled, <code>false</code> otherwise.
     */
    boolean isEnabled();

    interface SupportsSuggestion {
        /**
         * Returns a modified suggestion for the original query based on the spell
         * checker dictionary. If the spelling is correct or the spell checker does
         * not know how to correct the query or the provider does not support this
         * feature <code>null</code> is returned.
         *
         * @param originalQuery original search criteria
         * @param context context object, containing information about current site, workspace, locale etc.
         * @param maxTermsToSuggest the maximum number of terms to suggest
         * @return a modified suggestion for the original query based on the spell
         *         checker dictionary. If the spelling is correct or the spell
         *         checker does not know how to correct the query <code>null</code>
         *         is returned.
         */
        Suggestion suggest(SearchCriteria originalQuery, RenderContext context, int maxTermsToSuggest);
    }
}
