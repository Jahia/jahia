/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.rules.RulesListener;
import org.jahia.services.render.RenderContext;
import org.jahia.services.search.exception.InvalidSearchProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of the {@link SearchService}.
 *
 * @author Benjamin Papez
 */
public class SearchServiceImpl extends SearchService implements InitializingBean, ApplicationListener<ApplicationEvent> {
    private List<SearchProvider> availableSearchProviders = new ArrayList<SearchProvider>();
    private SearchProvider selectedSearchProvider;
    private SearchProvider defaultSearchProvider;

    private static Logger logger = LoggerFactory.getLogger(SearchServiceImpl.class);

    // Search settings
    private SearchSettings settings;

    public SearchSettings getSettings() {
        return settings;
    }

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

    /**
     * This event is fired when the changes in search server settings are detected (notification from other cluster nodes).
     *
     */
    public static class SearchSettingsChangedEvent extends ApplicationEvent {
        private static final long serialVersionUID = -2547399042743406556L;

        public SearchSettingsChangedEvent(Object source) {
            super(source);
        }
    }

    @Override
    public SearchResponse search(SearchCriteria criteria, RenderContext context) {

        // get stored response for current search criteria from the HTTP request attribute
        SearchResponse response = SearchUtils.getStoredSearchResponse(criteria, context.getRequest());

        if (response == null) {
            // execute the search
            response = getCurrentProvider().search(criteria, context);

            // store search response in request attribute
            SearchUtils.storeSearchResponse(criteria, response, context.getRequest());
        }

        return response;
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
    public Suggestion suggest(SearchCriteria originalQuery, RenderContext context, int maxTermsToSuggest) {
        if (getCurrentProvider() instanceof SearchProvider.SupportsSuggestion) {
            return ((SearchProvider.SupportsSuggestion) getCurrentProvider()).suggest(originalQuery, context, maxTermsToSuggest);
        } else {
            return getCurrentProvider().suggest(originalQuery.getTerms().get(0).getTerm(), context, maxTermsToSuggest);
        }
    }

    @Override
    public Suggestion suggest(String originalQuery, RenderContext context, int maxTermsToSuggest) {
        SearchCriteria sc = new SearchCriteria();
        sc.setTerm(originalQuery);
        SearchCriteria.CommaSeparatedMultipleValue value = new SearchCriteria.CommaSeparatedMultipleValue();
        value.setValue(context.getSite().getSiteKey());
        sc.setSites(value);
        return suggest(sc, context, maxTermsToSuggest);
    }

    /**
     * Execute rules on the list of hit objects.
     *
     * @param searchHits, list of JCRNodeHit objects
     * @param context, current rendering context
     */
    public static void executeURLModificationRules(
            List<? extends Hit> searchHits, RenderContext context) {
        Map<String, Object> globals = new HashMap<String, Object>();
        globals.put("renderContext", context);
        globals.put("urlService", SearchURLService.getInstance());
        RulesListener.getInstance(context.getMainResource().getWorkspace()).executeRules(searchHits,
                globals);
        return;
    }

    /**
     * Execute rules on a single search hit object.
     *
     * @param searchHit, JCRNodeHit object
     * @param context, current rendering context
     */
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
    public SearchProvider getCurrentProvider() {
        return selectedSearchProvider != null ? selectedSearchProvider : defaultSearchProvider;
    }

    /**
     * Returns an instance of the search provider for handling query requests.
     *
     * @return an instance of the search provider for handling query requests
     */
    protected SearchProvider getProvider(String name) {
        if(defaultSearchProvider != null && defaultSearchProvider.getName().equals(name)){
            return defaultSearchProvider;
        }

        if(availableSearchProviders != null){
            for(SearchProvider searchProvider : availableSearchProviders){
                if(searchProvider.getName().equals(name)){
                    return searchProvider;
                }
            }
        }
        return null;
    }

    /**
     * Register the given search provider in the available search providers
     *
     * @param searchProvider
     * @throws InvalidSearchProviderException
     */
    public void registerSearchProvider(SearchProvider searchProvider) throws InvalidSearchProviderException {
        if(searchProvider != null && StringUtils.isNotEmpty(searchProvider.getName())){
            if(getProvider(searchProvider.getName()) == null){
                availableSearchProviders.add(searchProvider);

                // reselect provider
                if(searchProvider.getName().equals(settings.getCurrentProvider())){
                    selectedSearchProvider = searchProvider;
                }
            }else {
                throw new InvalidSearchProviderException("Unable to register search provider with the name \"" + searchProvider.getName() + "\", search provider with this name already exist");
            }
        }else {
            throw new InvalidSearchProviderException("Search provider need to be not null and named");
        }
    }

    /**
     * Unregister the given search provider in the available search providers
     *
     * @param searchProvider
     * @throws InvalidSearchProviderException
     */
    public void unregisterSearchProvider(SearchProvider searchProvider) throws InvalidSearchProviderException {
        if(searchProvider != null && StringUtils.isNotEmpty(searchProvider.getName())){
            SearchProvider retrievedSearchProvider = getProvider(searchProvider.getName());
            if(retrievedSearchProvider != null){
                //if current selected provider is the one to unregistered fallback to default provider
                if(selectedSearchProvider != null && selectedSearchProvider.getName().equals(retrievedSearchProvider.getName())){
                    selectedSearchProvider = defaultSearchProvider;
                }

                availableSearchProviders.remove(retrievedSearchProvider);
            }else {
                throw new InvalidSearchProviderException("Unable to unregistered Search provider with the name \"" + searchProvider.getName() + "\", no search provider found");
            }
        }else {
            throw new InvalidSearchProviderException("Search provider need to be not null and named");
        }
    }

    /**
     * Return the list of available provider names
     *
     * @return
     */
    public List<String> getAvailableProviders() {
        List<String> result = new ArrayList<String>();
        result.add(defaultSearchProvider.getName());
        for (SearchProvider searchProvider : availableSearchProviders){
            result.add(searchProvider.getName());
        }
        return result;
    }

    /**
     * Select the given provider as server search provider
     *
     * @param name
     * @return
     */
    private boolean selectSearchProvider(String name) {
        if(StringUtils.isNotEmpty(name)){
            SearchProvider searchProvider = getProvider(name);
            if(searchProvider != null){
                selectedSearchProvider = searchProvider;
                return true;
            }
        }
        return false;
    }

    protected void load() {
        settings = new SearchSettings();
        try {
            // read search settings
            settings = JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, Constants.EDIT_WORKSPACE, null, new JCRCallback<SearchSettings>() {
                public SearchSettings doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    SearchSettings cfg = new SearchSettings();

                    JCRNodeWrapper searchSettingNode = null;
                    try {
                        searchSettingNode = session.getNode("/settings/search-settings");
                        cfg.setCurrentProvider(searchSettingNode.hasProperty("j:provider") ? searchSettingNode.getProperty("j:provider")
                                .getString() : null);
                        if (cfg.getCurrentProvider() != null && !cfg.getCurrentProvider().equals(settings.getCurrentProvider())) {
                            selectSearchProvider(cfg.getCurrentProvider());
                        }
                    } catch (PathNotFoundException e) {
                        cfg.setCurrentProvider(getDefaultSearchProvider().getName());
                        store(cfg, session);
                    }

                    return cfg;
                }
            });
        } catch (RepositoryException e) {
            logger.error("Error reading search settings from the repository.", e);
        }
    }

    public void store(final SearchSettings cfg) {
        try {
            // store search settings
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    store(cfg, session);
                    return Boolean.TRUE;
                }
            });
            load();
        } catch (RepositoryException e) {
            logger.error("Error storing search settings into the repository.", e);
        }
    }

    protected void store(SearchSettings cfg, JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper searchSettingNode = null;
        try {
            searchSettingNode = session.getNode("/settings/search-settings");
        } catch (PathNotFoundException e) {
            if (session.nodeExists("/settings")) {
                searchSettingNode = session.getNode("/settings").addNode("search-settings",
                        "jnt:searchServerSettings");
            } else {
                searchSettingNode = session.getNode("/").addNode("settings", "jnt:globalSettings")
                        .addNode("search-settings", "jnt:searchServerSettings");
            }
            searchSettingNode.setAclInheritanceBreak(true);
        }

        if(selectSearchProvider(cfg.getCurrentProvider())){
            searchSettingNode.setProperty("j:provider", cfg.getCurrentProvider());
            session.save();
        }
    }

    public void onApplicationEvent(ApplicationEvent evt) {
        if (evt instanceof JahiaContextLoaderListener.RootContextInitializedEvent || evt instanceof SearchSettingsChangedEvent) {
            load();
        }
    }

    public void afterPropertiesSet() throws Exception {

    }

    public void setDefaultSearchProvider(SearchProvider defaultSearchProvider) {
        this.defaultSearchProvider = defaultSearchProvider;
    }

    protected SearchProvider getDefaultSearchProvider() {
        return defaultSearchProvider;
    }
}
