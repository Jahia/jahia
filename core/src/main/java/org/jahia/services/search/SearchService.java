/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.search;

import java.util.Locale;

import org.jahia.services.JahiaService;
import org.jahia.services.render.RenderContext;
import org.jahia.services.usermanager.JahiaUser;

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
     * @param originalQuery original query string
     * @param siteKey current site key
     * @param locale current locale
     * @return a modified suggestion for the original query based on the spell
     *         checker dictionary. If the spelling is correct or the spell
     *         checker does not know how to correct the query <code>null</code>
     *         is returned.
     */
    public abstract Suggestion suggest(String originalQuery, String siteKey, Locale locale);
}
