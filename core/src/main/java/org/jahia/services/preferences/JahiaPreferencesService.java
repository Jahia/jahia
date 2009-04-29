/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.preferences;


import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.JahiaService;
import org.jahia.services.content.JCRJahiaContentNode;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.cache.CacheService;
import org.jahia.services.preferences.exception.JahiaPreferenceProviderException;
import org.jahia.services.preferences.generic.GenericJahiaPreference;
import org.jahia.services.preferences.page.PageJahiaPreference;
import org.jahia.services.preferences.impl.JahiaPreferencesJCRProviders;
import org.jahia.registries.ServicesRegistry;

import javax.jcr.RepositoryException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

/**
 * User: jahia
 * Date: 19 mars 2008
 * Time: 11:39:09
 */
public class JahiaPreferencesService extends JahiaService {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JahiaPreferencesService.class);
    private static JahiaPreferencesService instance;
    private CacheService cacheService;
    private JCRStoreService jcrStoreService;

    private Map<String, String> providerTypes;
    private Map<String, JahiaPreferencesProvider> providers;
    private Map<Class, JahiaPreferencesProvider> providersByClass;

    public synchronized void start() throws JahiaInitializationException {
        logger.debug("** Initializing the Preferences Service ...");
        providers = new HashMap<String, JahiaPreferencesProvider>();
        providersByClass = new HashMap<Class, JahiaPreferencesProvider>();
        for (String providerType : providerTypes.keySet()) {
            String className = jcrStoreService.getDecorators().get(providerTypes.get(providerType));
            JahiaPreferencesJCRProviders provider;
            try {
                Class<? extends JCRNodeWrapper> aClass = Class.forName(className).asSubclass(JCRNodeWrapper.class);
                provider = createProvider(aClass);
                providersByClass.put(aClass, provider);
            } catch (ClassNotFoundException e) {
                provider = new JahiaPreferencesJCRProviders();
            }
            provider.setType(providerType);
            provider.setNodeType(providerTypes.get(providerType));
            provider.setJCRStoreService(jcrStoreService);
            providers.put(providerType, provider);
        }
    }

    public <T extends JCRNodeWrapper> JahiaPreferencesJCRProviders<T> createProvider(Class<T> c) {
        return new JahiaPreferencesJCRProviders<T>();
    }

    public synchronized void stop() {
        logger.debug("** Stop the Preferences Service ...");
    }

    public static synchronized JahiaPreferencesService getInstance() {
        if (instance == null) {
            instance = new JahiaPreferencesService();
        }
        return instance;
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

    public Map<String, String> getProviderTypes() {
        return providerTypes;
    }

    public void setProviderTypes(Map<String, String> providerTypes) {
        this.providerTypes = providerTypes;
    }

    public Map getProviders() {
        return providers;
    }

    public void setProviders(Map providers) {
        this.providers = providers;
    }

    /**
     * Delete all preferences of the current user
     *
     * @param processingContext
     */
    public void deleteCurrentUserPreferences(ProcessingContext processingContext) {
        deleteAllPreferencesByPrincipal(processingContext.getUser());
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
     *
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
     *
     */
    public JahiaPreferencesProvider<GenericJahiaPreference> getGenericPreferencesProvider() throws JahiaPreferenceProviderException {
        return getPreferencesProviderByType("simple");
    }

    /**
     * Get Page preference provider
     *
     * @return
     * @throws JahiaPreferenceProviderException
     *
     */
    public JahiaPreferencesProvider<PageJahiaPreference> getPagePreferencesProvider() throws JahiaPreferenceProviderException {
        return getPreferencesProviderByType("page");
    }

    /**
     * Get providers map.
     *
     * @return
     */
    private Map<String, JahiaPreferencesProvider> getProvidersMap() {
        return providers;
    }

    /**
     * Return the value associated with the given key using the generic preference provider.
     *
     * @param key     the key
     * @param jParams the processing context
     * @return the value
     */
    public String getGenericPreferenceValue(String key, ProcessingContext jParams) {
        try {

            JahiaPreference preference = getGenericPreferencesProvider().getJahiaPreference(jParams.getUser(), JahiaPreferencesXpathHelper.getSimpleXpath(key));
            if (preference != null) {
                try {
                    return ((GenericJahiaPreference) preference.getNode()).getPrefValue();
                } catch (RepositoryException e) {
                    logger.error("Preference provider was not found.", e);
                }
            }
        } catch (JahiaPreferenceProviderException e) {
            logger.error("Preference provider was not found.", e);
        }
        return null;
    }

    /**
     * Return the value associated with the given key using the generic preference provider.
     *
     * @param key          the key
     * @param defaultValue the default value to be returned in te preference was not found
     * @param jParams      the processing context
     * @return the value
     */
    public boolean getGenericPreferenceBooleanValue(String key, boolean defaultValue, ProcessingContext jParams) {
        String value = getGenericPreferenceValue(key, jParams);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    /**
     * Return the value associated with the given key using the page preference provider.
     *
     * @param key     the key
     * @param jParams the processing context
     * @return the value
     */
    public String getPagePreferenceValue(String key, ProcessingContext jParams) {
        try {
            JahiaPreference<PageJahiaPreference> preference = getPagePreferencesProvider().getJahiaPreference(jParams.getUser(), JahiaPreferencesXpathHelper.getPageXpath(jParams.getPageID(), key));
            if (preference != null) {
                return preference.getNode().getPrefValue();
            }
        } catch (Exception e) {
            logger.error("Preference provider was not found.", e);
        }
        return null;
    }


    /**
     * Set a preference value associated with the given key using the generic preference provider.
     *
     * @param prefName  the key
     * @param prefValue the value
     * @param jParams   the processing context
     */
    public void setGenericPreferenceValue(String prefName, String prefValue, ProcessingContext jParams) {
        try {
            if (jParams.getUser().getUsername().equals("guest")) {
                return;
            }
            JahiaPreferencesProvider<GenericJahiaPreference> basicProvider = getGenericPreferencesProvider();

            // create generic preference key
            JahiaPreference<GenericJahiaPreference> preference = basicProvider.getJahiaPreference(jParams.getUser(), JahiaPreferencesXpathHelper.getSimpleXpath(prefName));
            if (preference == null) {
                if(prefValue == null){
                    return;
                }

                preference = basicProvider.createJahiaPreferenceNode(jParams);
                preference.getNode().setPrefName(prefName);
            }else{
                // delete preference
                if(prefValue == null){
                    basicProvider.deleteJahiaPreference(preference);
                    return;
                }
            }

            // create genereic preference value
            preference.getNode().setPrefValue(prefValue);


            basicProvider.setJahiaPreference(preference);
        } catch (Exception e) {
            logger.error("Preference provider was not found.", e);
        }
    }

    /**
     * Set a preference value associated with the given key using the page preference provider.
     *
     * @param prefName  the key
     * @param prefValue the value
     * @param jParams   the processing context
     */
    public void setPagePreferenceValue(String prefName, String prefValue, ProcessingContext jParams) {
        try {
            if (jParams.getUser().getUsername().equals("guest")) {
                return;
            }
            JahiaPreferencesProvider<PageJahiaPreference> pageProvider = getPagePreferencesProvider();

            // create generic preference key
            JahiaPreference<PageJahiaPreference> preference = pageProvider.getJahiaPreference(jParams.getUser(), JahiaPreferencesXpathHelper.getPageXpath(jParams.getPageID(), prefName));
            if (preference == null) {
                preference = pageProvider.createJahiaPreferenceNode(jParams);
                PageJahiaPreference node = preference.getNode();
                node.setPrefName(prefName);
                ContentPage page = ServicesRegistry.getInstance().getJahiaPageService().lookupContentPage(jParams.getPageID(), false);
                String pageUUID = page.getUUID();
                node.setPageUUID(pageUUID);
            }

            // create genereic preference value
            preference.getNode().setPrefValue(prefValue);

            pageProvider.setJahiaPreference(preference);
        } catch (Exception e) {
            logger.error("Preference provider was not found.", e);
        }
    }

    /**
     * @param prefName the key
     * @param jParams  the processing context
     */
    public void deleteGenericPreferenceValue(String prefName, ProcessingContext jParams) {
        try {
            if (jParams.getUser().getUsername().equals("guest")) {
                return;
            }
            // create generic preference key
            JahiaPreference<GenericJahiaPreference> preference = getGenericPreferencesProvider().createJahiaPreferenceNode(jParams);
            preference.getNode().setPrefName(prefName);

            getGenericPreferencesProvider().deleteJahiaPreference(preference);
        } catch (Exception e) {
            logger.error("Preference provider was not found.", e);
        }
    }

    /**
     * @param prefName the key
     * @param jParams  the processing context
     */
    public void deletePagePreferenceValue(String prefName, ProcessingContext jParams) {
        try {
            if (jParams.getUser().getUsername().equals("guest")) {
                return;
            }

            // create generic preference key
            JahiaPreference<PageJahiaPreference> preference = getPagePreferencesProvider().createJahiaPreferenceNode(jParams);
            ContentPage page = ServicesRegistry.getInstance().getJahiaPageService().lookupContentPage(jParams.getPageID(), false);
            String pageUUID = page.getUUID();
            preference.getNode().setPageUUID(pageUUID);
            preference.getNode().setPrefName(prefName);

            getPagePreferencesProvider().deleteJahiaPreference(preference);
        } catch (Exception e) {
            logger.error("Preference provider was not found.", e);
        }
    }

}

