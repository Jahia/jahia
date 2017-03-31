/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
