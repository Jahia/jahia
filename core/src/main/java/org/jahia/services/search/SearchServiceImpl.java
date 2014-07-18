/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.services.search;

import org.apache.commons.lang.StringUtils;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.content.rules.RulesListener;
import org.jahia.services.render.RenderContext;
import org.jahia.services.search.exception.InvalidSearchProviderException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of the {@link SearchService}.
 *
 * @author Benjamin Papez
 */
public class SearchServiceImpl extends SearchService {
    private List<SearchProvider> availableSearchProviders = new ArrayList<SearchProvider>();
    private SearchProvider selectedSearchProvider;
    private SearchProvider defaultSearchProvider;

    /**
     * Returns the unique instance of this service.
     */
    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final SearchServiceImpl INSTANCE = new SearchServiceImpl();
    }

    public static SearchServiceImpl getInstance() {
        return Holder.INSTANCE;
    }

    private SearchServiceImpl() {
    }

    @Override
    public SearchResponse search(SearchCriteria criteria, RenderContext context) {
        return getProvider().search(criteria, context);
    }

    @Override
    public void start() throws JahiaInitializationException {
        // do nothing
    }

    @Override
    public void stop() throws JahiaException {
        // do nothing
    }

    @Override
    public Suggestion suggest(String originalQuery, RenderContext context, int maxTermsToSuggest) {
        return getProvider().suggest(originalQuery, context, maxTermsToSuggest);
    }

    public static void executeURLModificationRules(
            Hit<?> searchHit, RenderContext context) {
        Map<String, Object> globals = new HashMap<String, Object>();
        globals.put("renderContext", context);
        globals.put("urlService", SearchURLService.getInstance());
        RulesListener.getInstance(context.getMainResource().getWorkspace()).executeRules(searchHit,
                globals);
        return;
    }

    /**
     * Returns an instance of the search provider for handling query requests.
     *
     * @return an instance of the search provider for handling query requests
     */
    protected SearchProvider getProvider() {
        return selectedSearchProvider != null ? selectedSearchProvider : defaultSearchProvider;
    }

    private SearchProvider getProvider(String name) {
        if(availableSearchProviders != null){
            for(SearchProvider searchProvider : availableSearchProviders){
                if(searchProvider.getName().equals(name)){
                    return searchProvider;
                }
            }
        }
        return null;
    }

    public void registerSearchProvider(SearchProvider searchProvider) throws InvalidSearchProviderException {
        if(searchProvider != null && StringUtils.isNotEmpty(searchProvider.getName())){
            if(getProvider(searchProvider.getName()) == null){
                availableSearchProviders.add(searchProvider);
            }else {
                throw new InvalidSearchProviderException("Unable to register search provider with the name \"" + searchProvider.getName() + "\", search provider with this name already exist");
            }
        }else {
            throw new InvalidSearchProviderException("Search provider need to be not null and named");
        }
    }

    public void unregisterSearchProvider(SearchProvider searchProvider) throws InvalidSearchProviderException {
        if(searchProvider != null && StringUtils.isNotEmpty(searchProvider.getName())){
            SearchProvider retreivedSearchProvider = getProvider(searchProvider.getName());
            if(retreivedSearchProvider != null){
                //if current selected provider is the one to unregistered fallback to default provider
                if(selectedSearchProvider != null && selectedSearchProvider.getName().equals(retreivedSearchProvider.getName())){
                    selectedSearchProvider = defaultSearchProvider;
                }

                availableSearchProviders.remove(retreivedSearchProvider);
            }else {
                throw new InvalidSearchProviderException("Unable to unregistered Search provider with the name \"" + searchProvider.getName() + "\", no search provider found");
            }
        }else {
            throw new InvalidSearchProviderException("Search provider need to be not null and named");
        }
    }

    public void setDefaultSearchProvider(SearchProvider defaultSearchProvider) {
        this.defaultSearchProvider = defaultSearchProvider;
    }

    public SearchProvider getDefaultSearchProvider() {
        return defaultSearchProvider;
    }

    public boolean selectSearchProvider(String name) {
        if(StringUtils.isNotEmpty(name)){
            SearchProvider searchProvider = getProvider(name);
            if(searchProvider != null){
                selectedSearchProvider = searchProvider;
                return true;
            }
        }
        return false;
    }

    public SearchProvider getSelectedSearchProvider() {
        return selectedSearchProvider;
    }
}
