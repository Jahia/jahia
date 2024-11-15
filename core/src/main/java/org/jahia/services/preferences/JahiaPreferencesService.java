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
package org.jahia.services.preferences;


import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaService;
import org.jahia.services.cache.CacheService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.preferences.exception.JahiaPreferenceProviderException;
import org.jahia.services.preferences.generic.GenericJahiaPreference;
import org.jahia.services.preferences.impl.JahiaPreferencesJCRProviders;

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Preference service for Jahia.
 * User: jahia
 * Date: 19 mars 2008
 * Time: 11:39:09
 */
public class JahiaPreferencesService extends JahiaService {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JahiaPreferencesService.class);
    private CacheService cacheService;
    private JCRStoreService jcrStoreService;

    private Map<String, String> providerTypes;
    private Map<String, JahiaPreferencesProvider> providers;
    private Map<Class, JahiaPreferencesProvider> providersByClass;

    private JahiaPreferencesService() {
    }

    public synchronized void start() throws JahiaInitializationException {
        logger.debug("** Initializing the Preferences Service ...");
        providers = new HashMap<String, JahiaPreferencesProvider>();
        providersByClass = new HashMap<Class, JahiaPreferencesProvider>();
        for (String providerType : providerTypes.keySet()) {
            final String nodeType = providerTypes.get(providerType);
            Class clazz = jcrStoreService.getDecorators().get(nodeType);
            JahiaPreferencesJCRProviders provider;
//            try {
            Class<? extends JCRNodeWrapper> aClass = clazz.asSubclass(JCRNodeWrapper.class);
            provider = createProvider(aClass);
            providersByClass.put(aClass, provider);
//            } catch (ClassNotFoundException e) {
//                provider = new JahiaPreferencesJCRProviders();
//            }
            provider.setType(providerType);
            provider.setNodeType(nodeType);
            provider.setJCRSessionFactory(jcrStoreService.getSessionFactory());
            providers.put(providerType, provider);
        }
    }

    public <T extends JCRNodeWrapper> JahiaPreferencesJCRProviders<T> createProvider(Class<T> c) {
        return new JahiaPreferencesJCRProviders<T>();
    }

    public synchronized void stop() {
        logger.debug("** Stop the Preferences Service ...");
    }

    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final JahiaPreferencesService INSTANCE = new JahiaPreferencesService();
    }

    public static JahiaPreferencesService getInstance() {
        return Holder.INSTANCE;
    }

    public CacheService getCacheService() {
        return cacheService;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public JCRStoreService getJcrStoreService() {
        return jcrStoreService;
    }

    public void setJcrStoreService(JCRStoreService jcrStoreService) {
        this.jcrStoreService = jcrStoreService;
    }

    public synchronized Map<String, String> getProviderTypes() {
        return Collections.unmodifiableMap(providerTypes);
    }

    public synchronized void setProviderTypes(Map<String, String> providerTypes) {
        this.providerTypes = providerTypes;
    }

    public Map getProviders() {
        return providers;
    }

    public void setProviders(Map providers) {
        this.providers = providers;
    }

    /**
     * Delete principal's preferences
     *
     * @param principal
     */
    public void deleteAllPreferencesByPrincipal(Principal principal) {
        Map<String, JahiaPreferencesProvider> allProviders = getProvidersMap();
        Iterator providersIt = allProviders.values().iterator();
        while (providersIt.hasNext()) {
            JahiaPreferencesProvider jahiaPreferencesProvider = (JahiaPreferencesProvider) providersIt.next();
            jahiaPreferencesProvider.deleteAllPreferencesByPrincipal(principal);
        }
    }


    /**
     * Get provider by type
     *
     * @param providerType
     * @return
     * @throws JahiaPreferenceProviderException
     */
    public JahiaPreferencesProvider getPreferencesProviderByType(String providerType) throws JahiaPreferenceProviderException {
        return providers.get(providerType);
    }

    public <T extends JCRNodeWrapper> JahiaPreferencesProvider<T> getPreferencesProviderByClass(Class<T> c) throws JahiaPreferenceProviderException {
        return providersByClass.get(c);
    }


    /**
     * Get Generic preference provider
     *
     * @return
     * @throws JahiaPreferenceProviderException
     */
    public JahiaPreferencesProvider<GenericJahiaPreference> getGenericPreferencesProvider() throws JahiaPreferenceProviderException {
        return getPreferencesProviderByType("simple");
    }

    /**
     * Get providers map.
     *
     * @return
     */
    private Map<String, JahiaPreferencesProvider> getProvidersMap() {
        return providers;
    }


}

