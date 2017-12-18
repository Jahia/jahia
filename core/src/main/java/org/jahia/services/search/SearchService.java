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
package org.jahia.services.search;

import org.jahia.services.JahiaService;
import org.jahia.services.render.RenderContext;

/**
 * Abstract class for the new Jahia search service. 
 * 
 * In contrast to the query-services, which allow all kind of complex queries, the search
 * service is more simple and mainly takes care about fulltext (unstructured) search and 
 * metadata search.
 * 
 * The search service can forward the request to different SearchProvider implementations. The 
 * default SearchProvider is based on Jahia's JCR wrapper, which uses Apache Jackrabbit as main 
 * repository, but can itself integrate multiple mounted external repositories via the Jahia 
 * Universal Content Hub.
 * 
 * Alternatively it is possible to plug-in a search provider, which uses a crawled index.
 * Jahia will provide an implementation based on Apache Nutch crawler and an index 
 * maintained by Apache Solr.
 * 
 * It should also be possible to plug-in OpenSearch based search services as well as
 * individual plug-ins to third party search engines (like Google Appliance).
 *
 * @author Benjamin Papez
 * 
 */
public abstract class SearchService extends JahiaService {
    /**
     * Performs a search using the SearchCriteria object, which is created with using the
     * Jahia Search Tags or can also be created in Java classes by using the 
     * SearchCriteriaFactory.
     * 
     * @param criteria the passed SearchCriteria object, which includes all search criteria 
     * @param context context object, containing information about current user, locale etc.
     * @return SearchResponse object with the list of hits matching the criteria
     */
    public abstract SearchResponse search(SearchCriteria criteria, RenderContext context);
    
    /**
     * Returns a modified suggestion for the original query based on the spell
     * checker dictionary. If the spelling is correct or the spell checker does
     * not know how to correct the query or the provider does not support this
     * feature <code>null</code> is returned.
     * 
     * @param originalQuery original search criteria
     * @param context context object, containing information about current site, workspace, locale etc.
     * @param maxTermsToSuggest the maximum number of terms to be suggested 
     * @return a modified suggestion for the original query based on the spell
     *         checker dictionary. If the spelling is correct or the spell
     *         checker does not know how to correct the query <code>null</code>
     *         is returned.
     */
    public abstract Suggestion suggest(SearchCriteria originalQuery, RenderContext context, int maxTermsToSuggest);

    /**
     * Returns a modified suggestion for the original query based on the spell
     * checker dictionary. If the spelling is correct or the spell checker does
     * not know how to correct the query or the provider does not support this
     * feature <code>null</code> is returned.
     *
     * @param originalQuery original query string
     * @param context context object, containing information about current site, workspace, locale etc.
     * @param maxTermsToSuggest the maximum number of terms to be suggested
     * @return a modified suggestion for the original query based on the spell
     *         checker dictionary. If the spelling is correct or the spell
     *         checker does not know how to correct the query <code>null</code>
     *         is returned.
     * @deprecated use suggest(SearchCriteria originalQuery, RenderContext context, int maxTermsToSuggest)
     */
    public abstract Suggestion suggest(String originalQuery, RenderContext context, int maxTermsToSuggest);
}
